#include <jni.h>
#include <string>
#include <android/log.h>
#include "libmobi/mobi.h"

#define TAG "NativeMobiReaderJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static jfieldID nativeMOBIDataPtr = nullptr;
static jfieldID nativeMOBIRawmlPtr = nullptr;

int injectNativeMOBIDataPtr(JNIEnv *env, jobject thiz, MOBIData *m) {
    jclass clazz = env->FindClass("com/wolf2/reader/reader/MobiFileReader");
    if (clazz == nullptr) {
        return JNI_ERR;
    }
    nativeMOBIDataPtr = env->GetFieldID(clazz, "nativeMOBIDataPtr", "J");
    if (nativeMOBIDataPtr == nullptr) {
        return JNI_ERR;
    }
    env->SetLongField(thiz, nativeMOBIDataPtr, reinterpret_cast<jlong>(m));
    return JNI_OK;
}

int injectNativeMOBIRawmlPtr(JNIEnv *env, jobject thiz, MOBIRawml *rawml) {
    jclass clazz = env->FindClass("com/wolf2/reader/reader/MobiFileReader");
    if (clazz == nullptr) {
        return JNI_ERR;
    }
    nativeMOBIRawmlPtr = env->GetFieldID(clazz, "nativeMOBIRawmlPtr", "J");
    if (nativeMOBIRawmlPtr == nullptr) {
        return JNI_ERR;
    }
    env->SetLongField(thiz, nativeMOBIRawmlPtr, reinterpret_cast<jlong>(rawml));
    return JNI_OK;
}

MOBIData *getNativeMOBIDataPtr(JNIEnv *env, jobject thiz) {
    jlong handle = env->GetLongField(thiz, nativeMOBIDataPtr);
    auto m = reinterpret_cast<MOBIData *>(handle);
    return m;
}

MOBIRawml *getNativeMOBIRawmlPtr(JNIEnv *env, jobject thiz) {
    jlong handle = env->GetLongField(thiz, nativeMOBIRawmlPtr);
    auto rawml = reinterpret_cast<MOBIRawml *>(handle);
    return rawml;
}

static int write_file(const unsigned char *buffer, const size_t len, const char *path) {
    errno = 0;
    FILE *file = fopen(path, "wb");
    if (file == NULL) {
        int errsv = errno;
        LOGE("Could not open file for writing: %s (%s)\n", path, strerror(errsv));
        return -1;
    }
    size_t n = fwrite(buffer, 1, len, file);
    if (n != len) {
        int errsv = errno;
        LOGE("Error writing to file: %s (%s)\n", path, strerror(errsv));
        fclose(file);
        return -1;
    }
    fclose(file);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeInit(JNIEnv *env, jobject thiz, jstring path) {
    MOBIData *m = mobi_init();
    if (m == nullptr) {
        LOGE("mobi_init error");
        return MOBI_INIT_FAILED;
    }
    const char *file_path = env->GetStringUTFChars(path, nullptr);
    if (file_path == nullptr) {
        LOGI("file_path is null");
        return MOBI_FILE_NOT_FOUND;
    }
    LOGI("fopen path:%s", file_path);
    FILE *file = fopen(file_path, "rb");
    if (file == nullptr) {
        LOGE("fopen error");
        mobi_free(m);
        return MOBI_FILE_NOT_FOUND;
    }
    MOBI_RET mobi_ret = mobi_load_file(m, file);
    fclose(file);
    if (mobi_ret != MOBI_SUCCESS) {
        mobi_free(m);
        return MOBI_ERROR;
    }
    MOBIRawml *rawml = mobi_init_rawml(m);
    if (rawml == nullptr) {
        mobi_free_rawml(rawml);
        return MOBI_ERROR;
    }
    mobi_ret = mobi_parse_rawml(rawml, m);
    if (mobi_ret != MOBI_SUCCESS) {
        mobi_free_rawml(rawml);
        return MOBI_ERROR;
    }
    injectNativeMOBIDataPtr(env, thiz, m);
    injectNativeMOBIRawmlPtr(env, thiz, rawml);

    return MOBI_SUCCESS;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeDestroy(JNIEnv *env, jobject thiz) {
    LOGI("--------nativeDestroy--------");
    auto *m = getNativeMOBIDataPtr(env, thiz);
    if (m) {
        mobi_free(m);
        delete m;
        env->SetLongField(thiz, nativeMOBIDataPtr, 0L);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeGetTitle(JNIEnv *env, jobject thiz) {
    auto *m = getNativeMOBIDataPtr(env, thiz);
    const char *title = "";
    if (m) {
        title = mobi_meta_get_title(m);
    }
    jstring ret = env->NewStringUTF(title);
    return ret;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeGetAuthor(JNIEnv *env, jobject thiz) {
    auto *m = getNativeMOBIDataPtr(env, thiz);
    const char *author = "";
    if (m) {
        author = mobi_meta_get_author(m);
    }
    jstring ret = env->NewStringUTF(author);
    return ret;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeGetCoverImage(JNIEnv *env, jobject thiz) {
    auto *m = getNativeMOBIDataPtr(env, thiz);
    if (m) {
        MOBIPdbRecord *record = nullptr;
        MOBIExthHeader *exth = mobi_get_exthrecord_by_tag(m, EXTH_COVEROFFSET);
        if (exth) {
            uint32_t offset = mobi_decode_exthvalue((unsigned char *) exth->data, exth->size);
            size_t first_resource = mobi_get_first_resource_record(m);
            size_t uid = first_resource + offset;
            LOGE("CoverImage Index:%zu", uid);
            record = mobi_get_record_by_seqnumber(m, uid);
        }
        if (record == nullptr || record->size < 4) {
            LOGE("CoverImage not found\n");
            return nullptr;
        }
        const unsigned char jpg_magic[] = "\xff\xd8\xff";
        const unsigned char gif_magic[] = "\x47\x49\x46\x38";
        const unsigned char png_magic[] = "\x89\x50\x4e\x47\x0d\x0a\x1a\x0a";
        const unsigned char bmp_magic[] = "\x42\x4d";
        char ext[4] = "raw";
        if (memcmp(record->data, jpg_magic, 3) == 0) {
            LOGE(ext, sizeof(ext), "%s", "jpg");
        } else if (memcmp(record->data, gif_magic, 4) == 0) {
            LOGE(ext, sizeof(ext), "%s", "gif");
        } else if (record->size >= 8 && memcmp(record->data, png_magic, 8) == 0) {
            LOGE(ext, sizeof(ext), "%s", "png");
        } else if (record->size >= 6 && memcmp(record->data, bmp_magic, 2) == 0) {
            const size_t bmp_size = (uint32_t) record->data[2] | ((uint32_t) record->data[3] << 8) |
                                    ((uint32_t) record->data[4] << 16) |
                                    ((uint32_t) record->data[5] << 24);
            if (record->size == bmp_size) {
                LOGE(ext, sizeof(ext), "%s", "bmp");
            }
        }

        if (record->size > static_cast<size_t>(std::numeric_limits<jsize>::max())) {
            LOGE("CoverImage size exceeds JNI limit");
            return nullptr;
        }
        auto length = static_cast<jsize>(record->size);
        jbyteArray ret = env->NewByteArray(length);
        if (ret == nullptr) {
            return nullptr;
        }
        env->SetByteArrayRegion(ret, 0, length, reinterpret_cast<jbyte *>(record->data));
        return ret;
    }
    return nullptr;
}

jobject createPageContent(JNIEnv *env, jobject thiz, size_t uid) {
    jclass cls = env->FindClass("com/wolf2/reader/mode/entity/book/PageContent");
    if (cls == nullptr) {
        return nullptr;
    }
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
    if (constructor == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jstring href = env->NewStringUTF("");
    jobject pageContent = env->NewObject(cls, constructor, href, (jint) uid);
    env->DeleteLocalRef(href);
    env->DeleteLocalRef(cls);
    return pageContent;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeGetPageContents(JNIEnv *env, jobject thiz) {
    jclass arrayListClz = env->FindClass("java/util/ArrayList");
    if (arrayListClz == nullptr) {
        return nullptr;
    }
    jmethodID constructor = env->GetMethodID(arrayListClz, "<init>", "()V");
    jobject arrayList = env->NewObject(arrayListClz, constructor);
    if (arrayList == nullptr) {
        return nullptr;
    }
    jmethodID addMethod = env->GetMethodID(arrayListClz, "add", "(Ljava/lang/Object;)Z");
    if (addMethod == nullptr) {
        return nullptr;
    }
    MOBIRawml *rawml = getNativeMOBIRawmlPtr(env, thiz);
    if (!rawml) {
        return nullptr;
    }
    int markupCount = 0;
    MOBIPart *markup = rawml->markup;
    while (markup != nullptr) {
        markupCount++;
        jobject pageContent = createPageContent(env, thiz, markup->uid);
        env->CallBooleanMethod(arrayList, addMethod, pageContent);
        env->DeleteLocalRef(pageContent);
        markup = markup->next;
    }
    LOGE("markupCount:%d", markupCount);
    return arrayList;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeGetResourceData(JNIEnv *env, jobject thiz,
                                                                  jint uid) {
    MOBIRawml *rawml = getNativeMOBIRawmlPtr(env, thiz);
    if (rawml == nullptr) {
        return nullptr;
    }
    MOBIPart *resource = mobi_get_resource_by_uid(rawml, uid);
    jbyteArray ret = env->NewByteArray(resource->size);
    if (ret == nullptr) {
        return nullptr;
    }
    env->SetByteArrayRegion(ret, 0, resource->size, reinterpret_cast<jbyte *>(resource->data));
    return ret;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeGetMarkupData(JNIEnv *env, jobject thiz,
                                                                jint uid) {
    MOBIRawml *rawml = getNativeMOBIRawmlPtr(env, thiz);
    if (rawml == nullptr) {
        return nullptr;
    }
    MOBIPart *resource = mobi_get_markup_by_uid(rawml, uid);
    jbyteArray ret = env->NewByteArray(resource->size);
    if (ret == nullptr) {
        return nullptr;
    }
    env->SetByteArrayRegion(ret, 0, resource->size, reinterpret_cast<jbyte *>(resource->data));
    return ret;
}




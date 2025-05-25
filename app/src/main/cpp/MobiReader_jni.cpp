#include <jni.h>
#include <string>
#include <android/log.h>
#include "libmobi/mobi.h"

#define TAG "NativeMobiReaderJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static jfieldID nativeMOBIDataPtr = nullptr;

//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
//    LOGI("--------JNI_OnLoad--------");
//    JNIEnv *env;
//    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
//        return JNI_ERR;
//    }
//    jclass clazz = env->FindClass("com/wolf2/reader/reader/MobiFileReader");
//    if (clazz == nullptr) {
//        return JNI_ERR;
//    }
//    nativeMOBIDataPtr = env->GetFieldID(clazz, "nativeMOBIDataPtr", "J");
//    if (nativeMOBIDataPtr == nullptr) {
//        return JNI_ERR;
//    }
//    return JNI_VERSION_1_6;
//}

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
    env->SetLongField(thiz, nativeMOBIDataPtr, reinterpret_cast<jlong>(m));

//    MOBIRawml *rawml = mobi_init_rawml(m);
//    if (rawml == nullptr) {
//        mobi_free(m);
//        return MOBI_ERROR;
//    }
//
//    mobi_ret = mobi_parse_rawml(rawml, m);
//    if (mobi_ret != MOBI_SUCCESS) {
//        mobi_free(m);
//        mobi_free_rawml(rawml);
//        return MOBI_ERROR;
//    }








//
//    mobi_free_rawml(rawml);
//    mobi_free(m);

    return MOBI_SUCCESS;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_nativeDestroy(JNIEnv *env, jobject thiz) {
    LOGI("--------nativeDestroy--------");
    jlong handle = env->GetLongField(thiz, nativeMOBIDataPtr);
    auto *m = reinterpret_cast<MOBIData *>(handle);
    if (m) {
        mobi_free(m);
        delete m;
        env->SetLongField(thiz, nativeMOBIDataPtr, 0);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_getTitle(JNIEnv *env, jobject thiz) {
    jlong handle = env->GetLongField(thiz, nativeMOBIDataPtr);
    auto *m = reinterpret_cast<MOBIData *>(handle);
    const char *title = "";
    if (m) {
        title = mobi_meta_get_title(m);
    }
    jstring ret = env->NewStringUTF(title);
    return ret;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_getAuthor(JNIEnv *env, jobject thiz) {
    jlong handle = env->GetLongField(thiz, nativeMOBIDataPtr);
    auto *m = reinterpret_cast<MOBIData *>(handle);
    const char *author = "";
    if (m) {
        author = mobi_meta_get_author(m);
    }
    jstring ret = env->NewStringUTF(author);
    return ret;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_wolf2_reader_reader_MobiFileReader_getCoverImage(JNIEnv *env, jobject thiz) {
    jlong handle = env->GetLongField(thiz, nativeMOBIDataPtr);
    auto *m = reinterpret_cast<MOBIData *>(handle);
    if (m) {
        MOBIPdbRecord *record = nullptr;
        MOBIExthHeader *exth = mobi_get_exthrecord_by_tag(m, EXTH_COVEROFFSET);
        if (exth) {
            uint32_t offset = mobi_decode_exthvalue((unsigned char *) exth->data, exth->size);
            size_t first_resource = mobi_get_first_resource_record(m);
            size_t uid = first_resource + offset;
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

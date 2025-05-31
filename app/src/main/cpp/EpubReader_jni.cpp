#include <jni.h>
#include <string>
#include <android/log.h>
#include "EPUB3Processor/EPUB3.h"

#define TAG "NativeEpubReaderJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static jfieldID nativeEPUB3RefPtr = nullptr;

int injectNativeEPUB3RefPtr(JNIEnv *env, jobject thiz, EPUB3Ref epub3Ref) {
    jclass clazz = env->FindClass("com/wolf2/reader/reader/EpubFileReader");
    if (clazz == nullptr) {
        return JNI_ERR;
    }
    nativeEPUB3RefPtr = env->GetFieldID(clazz, "nativeEPUB3RefPtr", "J");
    if (nativeEPUB3RefPtr == nullptr) {
        return JNI_ERR;
    }
    env->SetLongField(thiz, nativeEPUB3RefPtr, reinterpret_cast<jlong>(epub3Ref));
    return JNI_OK;
}

EPUB3Ref getNativeEPUB3Ref(JNIEnv *env, jobject thiz) {
    jlong handle = env->GetLongField(thiz, nativeEPUB3RefPtr);
    auto epub3Ref = reinterpret_cast<EPUB3Ref>(handle);
    return epub3Ref;
}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeInit(JNIEnv *env, jobject thiz, jstring path) {
    const char *file_path = env->GetStringUTFChars(path, nullptr);
    LOGI("nativeInit: %s", file_path);
    if (file_path == nullptr) {
        LOGI("file_path is null");
        return kEPUB3FileNotFoundInArchiveError;
    }
    EPUB3Error error = kEPUB3Success;
    EPUB3Ref epub3Ref = EPUB3CreateWithArchiveAtPath(file_path, &error);
    if (epub3Ref == nullptr) {
        return kEPUB3ArchiveUnavailableError;
    }
    injectNativeEPUB3RefPtr(env, thiz, epub3Ref);
    return kEPUB3Success;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeDestroy(JNIEnv *env, jobject thiz) {
    LOGI("--------nativeDestroy--------");
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    if (epub3Ref) {
        EPUB3Release(epub3Ref);
        delete epub3Ref;
        env->SetLongField(thiz, nativeEPUB3RefPtr, 0);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetTitle(JNIEnv *env, jobject thiz) {
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    char *title = nullptr;
    if (epub3Ref) {
        title = EPUB3CopyTitle(epub3Ref);
    }
    return env->NewStringUTF(title);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetAuthor(JNIEnv *env, jobject thiz) {
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    char *creator = nullptr;
    if (epub3Ref) {
        creator = EPUB3CopyAuthor(epub3Ref);
    }
    return env->NewStringUTF(creator);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetCoverImage(JNIEnv *env, jobject thiz) {
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    if (epub3Ref == nullptr) {
        return nullptr;
    }
    void *imageData = nullptr;
    uint32_t dataSize = 0;
    EPUB3CopyCoverImage(epub3Ref, &imageData, &dataSize);
    jbyteArray ret = env->NewByteArray(dataSize);
    if (ret == nullptr) {
        return nullptr;
    }
    env->SetByteArrayRegion(ret, 0, dataSize, reinterpret_cast<jbyte *>(imageData));
    free(imageData);
    return ret;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetCoverImageHref(JNIEnv *env, jobject thiz) {
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    char *coverImagePath = nullptr;
    if (epub3Ref) {
        coverImagePath = EPUB3CopyCoverImagePath(epub3Ref);
    }
    return env->NewStringUTF(coverImagePath);
}

jobject createPageContent(JNIEnv *env, jobject thiz, char *resourceHref) {
    jclass cls = env->FindClass("com/wolf2/reader/mode/entity/book/PageContent");
    if (cls == nullptr) {
        return nullptr;
    }
    jmethodID constructor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
    if (constructor == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jstring href = env->NewStringUTF(resourceHref);
    if (href == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jobject pageContent = env->NewObject(cls, constructor, href, 0);
    env->DeleteLocalRef(href);
    env->DeleteLocalRef(cls);
    return pageContent;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetPageContents(JNIEnv *env, jobject thiz) {
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
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    if (epub3Ref == nullptr) {
        return nullptr;
    }
    int32_t resourceCount = EPUB3CountOfSequentialResources(epub3Ref);
    LOGI("pageCount: %d", resourceCount);
    const char **resources = new const char *[resourceCount];
    EPUB3GetPathsOfSequentialResources(epub3Ref, resources);
    for (int i = 0; i < resourceCount; ++i) {
        const char *href = resources[i];
        jobject pageContent = createPageContent(env, thiz, (char *) href);
        env->CallBooleanMethod(arrayList, addMethod, pageContent);
        env->DeleteLocalRef(pageContent);
    }
    return arrayList;
}

jobject
createChapter(JNIEnv *env, jobject thiz, char *title, char *pageHref, char *parent) {
    jclass cls = env->FindClass("com/wolf2/reader/mode/entity/book/Chapter");
    if (cls == nullptr) {
        return nullptr;
    }
    jmethodID constructor = env->GetMethodID(cls, "<init>",
                                             "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (constructor == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jstring titleStr = env->NewStringUTF(title);
    if (titleStr == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jstring pageHrefStr = env->NewStringUTF(pageHref);
    if (pageHrefStr == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jstring parentStr = env->NewStringUTF(parent);
    if (parentStr == nullptr) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }
    jobject chapter = env->NewObject(cls, constructor, titleStr, pageHrefStr, parentStr);
    env->DeleteLocalRef(titleStr);
    env->DeleteLocalRef(pageHrefStr);
    env->DeleteLocalRef(parentStr);
    env->DeleteLocalRef(cls);
    return chapter;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetChapter(JNIEnv *env, jobject thiz) {
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
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    if (epub3Ref == nullptr) {
        return nullptr;
    }
    int32_t tocCount = EPUB3CountOfTocRootItems(epub3Ref);
    LOGI("tocCount: %d", tocCount);
    auto *tocItems = new EPUB3TocItemRef[tocCount];
    EPUB3GetTocRootItems(epub3Ref, tocItems);
    for (int i = 0; i < tocCount; i++) {
        EPUB3TocItemRef curToc = tocItems[i];
        char *chapterTitle = EPUB3TocItemCopyTitle(curToc);
        char *chapterPath = EPUB3TocItemCopyPath(curToc);
        char *parent = "";
        jobject chapter = createChapter(env, thiz, chapterTitle, chapterPath, parent);
        env->CallBooleanMethod(arrayList, addMethod, chapter);
        env->DeleteLocalRef(chapter);
    }
    return arrayList;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_wolf2_reader_reader_EpubFileReader_nativeGetResourceData(JNIEnv *env, jobject thiz,
                                                                  jstring href) {
    EPUB3Ref epub3Ref = getNativeEPUB3Ref(env, thiz);
    if (epub3Ref == nullptr) {
        return nullptr;
    }
    const char *filePath = env->GetStringUTFChars(href, nullptr);
    void *buffer = nullptr;
    uint32_t bufferSize = 0;
    uint32_t bytesCopied;
    EPUB3CopyFileIntoBuffer_(epub3Ref, &buffer, &bufferSize, &bytesCopied,
                             filePath);
    jbyteArray ret = env->NewByteArray(bufferSize);
    if (ret == nullptr) {
        return nullptr;
    }
    env->SetByteArrayRegion(ret, 0, bufferSize, reinterpret_cast<jbyte *>(buffer));
    free(buffer);
    return ret;
}
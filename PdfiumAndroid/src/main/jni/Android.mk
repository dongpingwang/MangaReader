LOCAL_PATH := $(call my-dir)

#Prebuilt libraries
include $(CLEAR_VARS)
LOCAL_MODULE := aospPdfium

ARCH_PATH = $(TARGET_ARCH_ABI)

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libmodpdfium.so

include $(PREBUILT_SHARED_LIBRARY)

#libmodft2
include $(CLEAR_VARS)
LOCAL_MODULE := libmodft2

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libmodft2.so

include $(PREBUILT_SHARED_LIBRARY)

#libmodpng
include $(CLEAR_VARS)
LOCAL_MODULE := libmodpng

LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libmodpng.so

include $(PREBUILT_SHARED_LIBRARY)

#Main JNI library
include $(CLEAR_VARS)
LOCAL_MODULE := jniPdfium
LOCAL_CFLAGS += -DHAVE_PTHREADS
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES := aospPdfium libmodft2 libmodpng  # Removed libmodc++_shared
LOCAL_LDLIBS += -llog -landroid -ljnigraphics -lc++  # Explicitly link libc++
LOCAL_SRC_FILES := $(LOCAL_PATH)/src/mainJNILib.cpp
LOCAL_CPPFLAGS += -stdlib=libc++
LOCAL_CPP_FEATURES += rtti exceptions
include $(BUILD_SHARED_LIBRARY)
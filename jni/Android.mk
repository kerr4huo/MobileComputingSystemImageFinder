LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := nonfree_prebuilt
LOCAL_SRC_FILES := libnonfree.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := opencv_java_prebuilt
LOCAL_SRC_FILES := libopencv_java.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
# Modify LOCAL_C_INCLUDES with your path to OpenCV for Android.
LOCAL_C_INCLUDES:= ../../sdk/native/jni/include
#LOCAL_C_INCLUDES:= ../../../../../NVPACK/OpenCV-2.4.10-android-sdk/sdk/native/jni/include
LOCAL_MODULE    := nonfree_jni
LOCAL_CFLAGS    := -Werror -O3 -ffast-math
LOCAL_LDLIBS    += -llog -ldl 
LOCAL_SHARED_LIBRARIES := nonfree_prebuilt opencv_java_prebuilt
LOCAL_SRC_FILES := nonfree_jni.cpp
include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#include ../../../../../NVPACK/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk
#LOCAL_MODULE    := mixed_sample
#LOCAL_CFLAGS    := -Werror -O3 -ffast-math
#LOCAL_LDLIBS +=  -llog -ldl
#LOCAL_SHARED_LIBRARIES := nonfree_prebuilt opencv_java_prebuilt
#LOCAL_SRC_FILES := jni_part.cpp
#include $(BUILD_SHARED_LIBRARY)
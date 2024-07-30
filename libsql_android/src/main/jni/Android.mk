LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := libsql_android/$(TARGET_ARCH_ABI)/liblibsql_android.so
LOCAL_MODULE := add_prebuilt
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES  := uselib.c
LOCAL_MODULE     :=  use-lib
LOCAL_SHARED_LIBRARIES := add_prebuilt
include $(BUILD_SHARED_LIBRARY)
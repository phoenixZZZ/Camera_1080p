LOCAL_PATH := $(call my-dir)

#ffmpeg

include $(CLEAR_VARS)
LOCAL_MODULE := avcodec-56-prebuilt
LOCAL_SRC_FILES := prebuilt/libavcodec-56.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avdevice-56-prebuilt
LOCAL_SRC_FILES := prebuilt/libavdevice-56.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avfilter-4-prebuilt
LOCAL_SRC_FILES := prebuilt/libavfilter-5.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avformat-56-prebuilt
LOCAL_SRC_FILES := prebuilt/libavformat-56.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  avutil-54-prebuilt
LOCAL_SRC_FILES := prebuilt/libavutil-54.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  avswresample-1-prebuilt
LOCAL_SRC_FILES := prebuilt/libswresample-1.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  swscale-3-prebuilt
LOCAL_SRC_FILES := prebuilt/libswscale-3.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  libpostproc-53-prebuilt
LOCAL_SRC_FILES := prebuilt/libpostproc-53.so
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES += libutils
include $(PREBUILT_SHARED_LIBRARY)

#tutk
include $(CLEAR_VARS)
LOCAL_MODULE :=  AVAPIs
LOCAL_SRC_FILES := tutk/libAVAPIs.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  IOTCAPIs
LOCAL_SRC_FILES := tutk/libIOTCAPIs.so
include $(PREBUILT_SHARED_LIBRARY)

# goolink
include $(CLEAR_VARS)
LOCAL_MODULE :=  liba4.2.2
LOCAL_SRC_FILES := goolink/libasp.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  libanativehelper
LOCAL_SRC_FILES := goolink/libanativehelper.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  libglnkio
LOCAL_SRC_FILES := goolink/libglnkav.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE :=  liblibffmpeg
LOCAL_SRC_FILES := goolink/libffmpeg.mx.so
include $(PREBUILT_SHARED_LIBRARY)

#H264DecodeUtil.c
include $(CLEAR_VARS)  
LOCAL_MODULE    := h264decodeutil  
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_H264DecodeUtil.c  
#LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -Wno-sign-compare -Wno-switch -Wno-pointer-sign -DHAVE_NEON=1 \
#      -mfpu=neon -mfloat-abi=softfp -fPIC -DANDROID 
#LOCAL_LDLIBS :=-L$(NDK_PLATFORMS_ROOT)/$(TARGET_PLATFORM)/arch-arm/usr/lib \
#-L$(LOCAL_PATH) -llog -ljnigraphics -lz -ldl -lgcc
LOCAL_LDLIBS += -llog -ljnigraphics -lz

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt libpostproc-53-prebuilt
include $(BUILD_SHARED_LIBRARY) 

#AudioDecode.c
include $(CLEAR_VARS)  
LOCAL_MODULE    := AudioDecode  
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_AudioDecode.c
#LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -Wno-sign-compare -Wno-switch -Wno-pointer-sign -DHAVE_NEON=1 \
#      -mfpu=neon -mfloat-abi=softfp -fPIC -DANDROID 
#LOCAL_LDLIBS :=-L$(NDK_PLATFORMS_ROOT)/$(TARGET_PLATFORM)/arch-arm/usr/lib \
#-L$(LOCAL_PATH) -llog -ljnigraphics -lz -ldl -lgcc
LOCAL_LDLIBS += -llog -ljnigraphics -lz
 
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt libpostproc-53-prebuilt
include $(BUILD_SHARED_LIBRARY)
##RecordVideo.c
#include $(CLEAR_VARS)  
#LOCAL_MODULE    := RecordVideo  
#LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_RecordVideo.c
##LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -Wno-sign-compare -Wno-switch -Wno-pointer-sign -DHAVE_NEON=1 \
##      -mfpu=neon -mfloat-abi=softfp -fPIC -DANDROID 
##LOCAL_LDLIBS :=-L$(NDK_PLATFORMS_ROOT)/$(TARGET_PLATFORM)/arch-arm/usr/lib \
##-L$(LOCAL_PATH)  -llog -ljnigraphics -lz -ldl -lgcc
#LOCAL_LDLIBS += -llog -ljnigraphics -lz
#
#LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
#LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt libpostproc-53-prebuilt
#include $(BUILD_SHARED_LIBRARY)

#RtspAudio
include $(CLEAR_VARS)  
LOCAL_MODULE    := RtspAudio  
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_RtspAudio.c 
LOCAL_LDLIBS += -llog -ljnigraphics -lz 
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
LOCAL_CFLAGS += -D_ARM_ 
LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt libpostproc-53-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt libavutil-54.so -pthread  
TARGET_ARCH_ABI :=armeabi-v7a
LOCAL_ARM_MODE := arm    
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)    
# 閲囩敤NEON浼樺寲鎶�湳    
LOCAL_ARM_NEON := true    
endif    
include $(BUILD_SHARED_LIBRARY) 

#RtspRecordVideo.c
include $(CLEAR_VARS)  
LOCAL_MODULE    := RtspRecordVideo  
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_RtspRecordVideo.c
LOCAL_LDLIBS += -llog -ljnigraphics -lz 
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
LOCAL_CFLAGS += -D_ARM_
LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt
TARGET_ARCH_ABI :=armeabi-v7a
LOCAL_ARM_MODE := arm    
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)    
# 閲囩敤NEON浼樺寲鎶�湳    
LOCAL_ARM_NEON := true    
endif    
include $(BUILD_SHARED_LIBRARY)

#OpenCV.c
include $(CLEAR_VARS)
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC
include E:\Git\Camera\FFmpeg_package_SourceFile\IPCamera\OpenCV\native\jni\OpenCV.mk
LOCAL_MODULE    := OpenCV_Test
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_OpenCV.cpp \
					./SIFT/sift.c	\
					./SIFT/kdtree.c \
					./SIFT/minpq.c \
					./SIFT/xform.c \
					./SIFT/imgfeatures.c	\
					./SIFT/utils.c
					
LOCAL_LDLIBS +=  -llog -ldl
TARGET_ARCH_ABI :=armeabi-v7a
LOCAL_ARM_MODE := arm  
include $(BUILD_SHARED_LIBRARY)

#RtspFromFFMPEG
include $(CLEAR_VARS)  
LOCAL_MODULE    := RtspFromFFMPEG  
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_RtspFromFFMPEG.c \
				   com_jiuan_it_ipc_utils_RtspRecordVideo.c \
				   thread.c \
				   cubf_core.cpp \
				   g711codec.c
#				   g7.c \
#				   g711.c
LOCAL_LDLIBS += -llog -ljnigraphics -lz 
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
LOCAL_CFLAGS += -D_ARM_ 
LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt libpostproc-53-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt libavutil-54.so -pthread  
TARGET_ARCH_ABI :=armeabi-v7a
LOCAL_ARM_MODE := arm    
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)    

LOCAL_ARM_NEON := true    
endif    
include $(BUILD_SHARED_LIBRARY) 


#CombineVideo
include $(CLEAR_VARS)  
LOCAL_MODULE    := CombineVideo  
LOCAL_SRC_FILES := com_jiuan_it_ipc_utils_CombineVideo.c \
				   thread.c \
				   cubf_core.cpp \
				   g711codec.c
#				   g7.c \
#				   g711.c
LOCAL_LDLIBS += -llog -ljnigraphics -lz 
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include 
LOCAL_CFLAGS += -D_ARM_ 
LOCAL_SHARED_LIBRARIES := libutils avutil-54-prebuilt avswresample-1-prebuilt avcodec-56-prebuilt swscale-3-prebuilt avformat-56-prebuilt libpostproc-53-prebuilt avfilter-4-prebuilt avdevice-56-prebuilt libavutil-54.so -pthread  
TARGET_ARCH_ABI :=armeabi-v7a
LOCAL_ARM_MODE := arm    
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)    

LOCAL_ARM_NEON := true    
endif    
include $(BUILD_SHARED_LIBRARY) 



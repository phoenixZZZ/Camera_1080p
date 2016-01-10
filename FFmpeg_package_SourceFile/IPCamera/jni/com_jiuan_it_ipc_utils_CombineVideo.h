#include <jni.h>

#ifndef _Included_com_jiuan_it_ipc_utils_CombineVideo
#define _Included_com_jiuan_it_ipc_utils_CombineVideo
#ifdef __cplusplus
extern "C" {
#endif

#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavfilter/avfiltergraph.h"
#include "libavfilter/buffersink.h"
#include "libavfilter/buffersrc.h"
#include "libavutil/avutil.h"
#include "libavutil/opt.h"
#include "libavutil/pixdesc.h"
#include "libswresample/swresample.h"
#include "libavutil/fifo.h"
#include "libavutil/audio_fifo.h"

int CombineVideo_OpenInput(const char* inFileName1, const char* inFileName2);

int CombineVideo_OpenOutput(const char* outFileName, int isAudio);

JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_CombineVideo_combine(JNIEnv *, jobject, jstring, jstring, jstring, jint);

//int CombineVideo(const char* inFileName1, const char* inFileName2, const char* outFileName);

#ifdef __cplusplus
}
#endif
#endif

#include"com_jiuan_it_ipc_utils_AudioDecode.h"
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#include <libavcodec/avcodec.h>
#include "libswresample/swresample.h"

//#include "arm_neon.h"

#define  LOG_TAG    "AudioDecode"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define AVCODEC_MAX_AUDIO_FRAME_SIZE 192000

AVCodec* codec;
AVCodecContext *codec_ctx;
AVFrame *pFrame;
struct SwrContext* swrContext = NULL;
uint8_t *out_buffer;
/*
 * Class:     com_jiuan_it_ipc_utils_AudioDecode
 * Method:    init
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_AudioDecode_init(
		JNIEnv *env, jobject obj) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	avcodec_register_all(); //初始化libavformat/libavcodec
	av_register_all(); //注册所有解码器
	pFrame = av_frame_alloc();
	if (!pFrame) {
		LOGE("Could not allocate Audio frame\n");
		return -1;
	}
	out_buffer = (uint8_t *) av_malloc(AVCODEC_MAX_AUDIO_FRAME_SIZE); //申请内存
	return 1;
}

/*
 * Class:     com_jiuan_it_ipc_utils_AudioDecode
 * Method:    findDecoder
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_AudioDecode_findAACDecoder(
		JNIEnv *env, jobject obj) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	codec = avcodec_find_decoder(AV_CODEC_ID_AAC);
	if (!codec) {
		LOGE("Codec not found AACDecoder\n");
		return -1;
	}
	codec_ctx = avcodec_alloc_context3(codec);
	if (codec_ctx == NULL) {
		LOGE("avcodec_alloc_context3 failed\n");
	} else {
		codec_ctx->codec_type = AVMEDIA_TYPE_AUDIO;
		codec_ctx->frame_size = 1024;
		codec_ctx->channels = 1;
		codec_ctx->sample_rate = 8000;
		codec_ctx->sample_fmt = AV_SAMPLE_FMT_S16;
	}
	if (avcodec_open2(codec_ctx, codec, NULL) < 0) {
		LOGE("could not open codec\n");
		return -1;
	}
	swrContext = swr_alloc();
	if (swrContext == NULL) {
		LOGE("设置上下文出错");
	}
	int64_t in_channel_layout = av_get_default_channel_layout(
			codec_ctx->channels);
	swrContext = swr_alloc_set_opts(swrContext,
			AV_CH_LAYOUT_STEREO/*pFrame->channel_layout*/, // out channel layout
			AV_SAMPLE_FMT_S16, // out sample format
			8000, // out sample rate
			in_channel_layout/*pFrame->channel_layout*/, // in channel layout
			codec_ctx->sample_fmt, // in sample format
			codec_ctx->sample_rate, // in sample rate
			0, // log offset
			NULL); // log context

	if (swrContext == NULL) {
		LOGE("设置参数出错");
		return -1;
	}
	int re = swr_init(swrContext);
	if (re < 0) {
		LOGE("初始化结果：%d", re);
		return -1;
	}

	return 1;
}
/*
 * Class:     com_jiuan_it_ipc_utils_AudioDecode
 * Method:    findG711aDecoder
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_AudioDecode_findG711aDecoder(
		JNIEnv *env, jobject obj) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);

	codec = avcodec_find_decoder(AV_CODEC_ID_PCM_ALAW);
	if (!codec) {
		LOGE("Codec not found G711aDecoder\n");
		return -1;
	}
	codec_ctx = avcodec_alloc_context3(codec);
	if (codec_ctx == NULL) {
		LOGE("avcodec_alloc_context3 failed\n");
	} else {
		codec_ctx->codec_type = AVMEDIA_TYPE_AUDIO;
		codec_ctx->frame_size = 1024;
		codec_ctx->channels = 1;
		codec_ctx->sample_rate = 8000;
		codec_ctx->sample_fmt = AV_SAMPLE_FMT_S16;
	}
	if (avcodec_open2(codec_ctx, codec, NULL) < 0) {
		LOGE("could not open codec\n");
		return -1;
	}
	swrContext = swr_alloc(); //分配SwrContext
	if (swrContext == NULL) {
		LOGE("设置上下文出错");
	}

	int64_t in_channel_layout = av_get_default_channel_layout(
			codec_ctx->channels);
	swrContext = swr_alloc_set_opts(swrContext,
			AV_CH_LAYOUT_STEREO/*pFrame->channel_layout*/, // out channel layout
			AV_SAMPLE_FMT_S16, // out sample format
			8000, // out sample rate
			in_channel_layout/*pFrame->channel_layout*/, // in channel layout
			codec_ctx->sample_fmt, // in sample format
			codec_ctx->sample_rate, // in sample rate
			0, // log offset
			NULL); // log context

	if (swrContext == NULL) {
		LOGE("设置参数出错");
		return -1;
	}
	int re = swr_init(swrContext);
	if (re < 0) {
		LOGE("初始化结果：%d", re);
		return -1;
	}

	return 1;
}

/*
 * Class:     com_jiuan_it_ipc_utils_AudioDecode
 * Method:    aacDecode
 * Signature: ([BI)[B
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_AudioDecode_aacDecode(
		JNIEnv *env, jobject obj, jbyteArray byteArray, jint len,
		jbyteArray outData) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	AVPacket avpkt;
	av_init_packet(&avpkt);
	int out_size = 0;
	if (byteArray == NULL) {
		LOGE("%s-------%d 没有接收到数据", __FUNCTION__, __LINE__);
		exit(1);
	}
	jbyte * Buf = (jbyte*) (*env)->GetByteArrayElements(env, byteArray, 0);
	if (Buf == NULL) {
		LOGE("%s-------%d未存储成功", __FUNCTION__, __LINE__);
		exit(1);
	}
	avpkt.size = len;
	avpkt.data = Buf;
	avpkt.pts = 8000;
	if (byteArray != NULL) {
		(*env)->ReleaseByteArrayElements(env, byteArray, Buf, 0);
	}
	int bufsize = 0;
	int l = avcodec_decode_audio4(codec_ctx, pFrame, &out_size, &avpkt);
	LOGD("解码返回值%d", l);
	if (l <= 0) {
		LOGE("Error while decoding\n");

	}
	if (out_size > 0) {
		bufsize = av_samples_get_buffer_size(NULL,
				av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO),
				pFrame->nb_samples, AV_SAMPLE_FMT_S16, 1);
		int error = swr_convert(swrContext, &out_buffer,
				AVCODEC_MAX_AUDIO_FRAME_SIZE, (const uint8_t **) pFrame->data,
				pFrame->nb_samples);
		if (error < 0) {
			LOGE("转码出错");
			return -1;
		} else {
			LOGD("error的值%d", error);
		}
		(*env)->SetByteArrayRegion(env, outData, 0, bufsize, out_buffer);
		LOGD("解码完成一帧");
	} else {
		LOGE("No Saving Frame\n");
		return -1;
	}
	av_free_packet(&avpkt);
	return bufsize;
}

/*
 * Class:     com_jiuan_it_ipc_utils_AudioDecode
 * Method:    g711aDecode
 * Signature: ([BI)[B
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_AudioDecode_g711aDecode(
		JNIEnv *env, jobject obj, jbyteArray byteArray, jint len,
		jbyteArray outData) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	AVPacket avpkt;
	int bufsize = -1;
	int out_size = 0;
	LOGE("1111111111111111111111111");
	av_init_packet(&avpkt);
	LOGE("2222222222222222222222222");
	if (byteArray == NULL) {
		LOGE("%s-------%d 没有接收到数据", __FUNCTION__, __LINE__);
		bufsize = -1;
		goto ErrLab;
	}
	LOGD("解码返回值");
	jbyte * Buf = (jbyte*) (*env)->GetByteArrayElements(env, byteArray, 0);
	if (Buf == NULL) {
		LOGE("%s-------%d未存储成功", __FUNCTION__, __LINE__);
		bufsize = -1;
		goto ErrLab;
	}

	avpkt.size = len;
	avpkt.data = Buf;
	avpkt.pts = 8000;
	int l = avcodec_decode_audio4(codec_ctx, pFrame, &out_size, &avpkt);
	if (l <= 0) {
		LOGE("Error while decoding\n");
		bufsize = -1;
		goto ErrLab;
	}
	if (out_size > 0) {
		bufsize = av_samples_get_buffer_size(NULL,
				av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO),
				pFrame->nb_samples, AV_SAMPLE_FMT_S16, 1);
		int error = swr_convert(swrContext, &out_buffer,
				AVCODEC_MAX_AUDIO_FRAME_SIZE, (const uint8_t **) pFrame->data,
				pFrame->nb_samples);
		if (error < 0) {
			LOGE("转码出错");
			bufsize = -1;
			goto ErrLab;
		} else {
			LOGD("error的值%d", error);
		}
		(*env)->SetByteArrayRegion(env, outData, 0, bufsize, out_buffer);
		LOGD("音频解码完成一帧,%d\n", bufsize);
	} else {
		LOGE("No Saving Frame\n");
		bufsize = -1;
		goto ErrLab;
	}

ErrLab:
	av_free_packet(&avpkt);
	return bufsize;
}

/*
 * Class:     com_jiuan_it_ipc_utils_AudioDecode
 * Method:    close
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_AudioDecode_close(
		JNIEnv *env, jobject obj) {
	//av_free_packet(&avpkt);
	if (codec_ctx) avcodec_free_context(&codec_ctx);
	if (pFrame) av_frame_free(&pFrame);
	if (swrContext) swr_free(&swrContext);
	if (out_buffer) av_free(out_buffer);
	return 1;

}


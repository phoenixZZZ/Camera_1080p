#include "com_jiuan_it_ipc_utils_RtspRecordVideo.h"

#include <string.h>
#include <stdio.h>
#include <time.h>
#include <sys/time.h>
#include "libavutil/pixfmt.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include <android/log.h>


#define  LOG_TAG    "RtspRecordVideo"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
typedef struct AV_RecordVideo_Content
{
	AVFormatContext *o_fmt_ctx;
	AVStream *o_video_stream;
	int last_pts;
	int last_dts;
	AVRational timebase_1MHz;
	AVRational timebase_9000Hz;
	int IsFirst;
}AV_RecordVideo_Content;

AV_RecordVideo_Content *obj = NULL;

JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspRecordVideo_start(JNIEnv *env,jobject ob,jstring str)
{
	int ret = -1;

	char *inFilePath = NULL;
	inFilePath = (*env)->GetStringUTFChars(env, str, NULL);
	obj = (AV_RecordVideo_Content *)malloc(sizeof(AV_RecordVideo_Content));

	obj->o_fmt_ctx = NULL;
	obj->o_video_stream = NULL;
	obj->last_pts = 0;
	obj->last_dts = 0;

	avcodec_register_all();
	av_register_all();
	int re = avformat_alloc_output_context2(&obj->o_fmt_ctx, NULL, NULL, inFilePath); //初始化输出文件
	if (!obj->o_fmt_ctx || re < 0) {
		return -1;
	}

	obj->o_video_stream = avformat_new_stream(obj->o_fmt_ctx, NULL);
	{
		AVCodecContext *c;
		c = obj->o_video_stream->codec;
		c->bit_rate = 400000; //90000
		c->codec_id = AV_CODEC_ID_H264;
		c->codec_type = AVMEDIA_TYPE_VIDEO;
		c->time_base.num = 1;
		c->time_base.den = 90000; //90000
		c->width = 1280;
		c->height = 720;
		c->pix_fmt = AV_PIX_FMT_YUV420P;
		c->flags = 0;
		c->flags |= CODEC_FLAG_GLOBAL_HEADER;
		c->me_range = 0;
		c->max_qdiff = 3;
		c->gop_size = 12;
		c->qmin = 2;
		c->qmax = 31;
		c->qcompress = 0.50000000;
	}

    avio_open(&obj->o_fmt_ctx->pb, inFilePath, AVIO_FLAG_WRITE);
    //avformat_write_header(obj->o_fmt_ctx, NULL);

    obj->timebase_1MHz.num = 1;
    obj->timebase_1MHz.den = 1000000;

    obj->timebase_9000Hz.num = 1;
    obj->timebase_9000Hz.den = 90000;

    obj->last_pts = 50000;
    obj->last_dts = 50000;

    obj->IsFirst = 1;

	return 0;
}
//(void* indata, int iSize, void *extradata, int extradata_size, int IsIFrame);
JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspRecordVideo_record(
		JNIEnv *env, jobject ob, jbyteArray byteArray, jint in,jbyteArray extradata, jint extradata_size,jint IsIFrame)

{
	AVPacket i_pkt;
    int pts = 0;
    int dts = 0;
	av_init_packet(&i_pkt);
	i_pkt.size = 0;
	i_pkt.data = NULL;

	jbyte * Buf = (jbyte*) (*env)->GetByteArrayElements(env, byteArray, 0);
	i_pkt.size = in;
	i_pkt.data = Buf; //(uint8_t *)indata
	i_pkt.pts = 0;
	i_pkt.dts = 0;
	if (byteArray != NULL) {
				(*env)->ReleaseByteArrayElements(env, byteArray, Buf, 0);
	}
	if (obj->IsFirst)
	{
		if (IsIFrame)
		{
			AVCodecContext *c;
			jbyte * buf = (jbyte*) (*env)->GetByteArrayElements(env, extradata, 0);
			c = obj->o_video_stream->codec;
			c->extradata =  buf;//(uint8_t *)extradata;
			c->extradata_size = extradata_size;
    		avformat_write_header(obj->o_fmt_ctx, NULL);
    		obj->IsFirst = 0;
    		if (extradata != NULL) {
    			(*env)->ReleaseByteArrayElements(env, extradata, buf, 0);
    		}
		}
		else
		{
			av_free_packet(&i_pkt);
			return 0;
		}
	}

	i_pkt.flags |= AV_PKT_FLAG_KEY;
	i_pkt.pts = av_rescale_q(obj->last_pts, obj->timebase_1MHz, obj->timebase_9000Hz);
	i_pkt.dts = av_rescale_q(obj->last_dts, obj->timebase_1MHz, obj->timebase_9000Hz);
	i_pkt.stream_index = 0;

	av_interleaved_write_frame(obj->o_fmt_ctx, &i_pkt);

	obj->last_dts += 50000;
	obj->last_pts += 50000;

	av_free_packet(&i_pkt);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspRecordVideo_stop(JNIEnv *env, jobject ob)
{

	if (obj)
	{

		if (obj->o_fmt_ctx)
		{
			av_write_trailer(obj->o_fmt_ctx);
			avcodec_close(obj->o_fmt_ctx->streams[0]->codec);
			av_freep(&obj->o_fmt_ctx->streams[0]->codec);
			av_freep(&obj->o_fmt_ctx->streams[0]);
			avio_close(obj->o_fmt_ctx->pb);
			av_free(obj->o_fmt_ctx);
		}
		free(obj);

	}

	return 0;
}

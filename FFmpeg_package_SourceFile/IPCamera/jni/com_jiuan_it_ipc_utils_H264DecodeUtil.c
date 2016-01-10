#include"com_jiuan_it_ipc_utils_H264DecodeUtil.h"
#include <string.h>
#include <stdio.h>
#include  <time.h>
#include <sys/time.h>
#include <libavutil/pixfmt.h>
#include <android/log.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
//#include "arm_neon.h"

#define  LOG_TAG    "H264DecodeUtil"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
AVCodecContext *pCodecCtx = NULL;
AVFrame *pFrame, *pFrameYUV = NULL;
AVCodec *pCodec = NULL;
int y_size = 0, frame_count = 0, num = 0;

int isPicture = 0;
const jbyte *name;

struct SwsContext *img_convert_ctx = NULL;
uint8_t *out_buffer = NULL;
FILE *Photo;

FILE *fp_yuv = NULL, *output = NULL;
/*
 * Class:     com_jiuan_it_ipc_utils_H264DecodeUtil
 * Method:    registeriInit
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_H264DecodeUtil_registeriInit(
		JNIEnv *env, jobject obj) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	av_register_all();
	avcodec_register_all();

	pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
	if (!pCodec) {
		LOGE("Codec not found\n");
		return -1;
	}
	pCodecCtx = avcodec_alloc_context3(pCodec);
	if (!pCodecCtx) {
		LOGE("Could not allocate video codec context\n");
		return -1;
	}
	/* open it */
	if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
		LOGE("Could not open codec\n");
		return -1;
	}
	pFrame = av_frame_alloc();
	pFrameYUV = av_frame_alloc();
	if (!pFrame) {
		LOGE("Could not allocate video frame\n");
		return -1;
	}
	return 0;

}
/*
 * Class:     com_jiuan_it_ipc_utils_H264DecodeUtil
 * Method:    saveJPG
 * Signature: (Ljava/lang/String;)Ljava/lang/Boolean;
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_H264DecodeUtil_saveJPG(
		JNIEnv * env, jobject jo, jstring str) {
	 isPicture = 1;
	 LOGE("111111");
	 name = (*env)->GetStringUTFChars(env, str, NULL);
	 LOGE("222222");
     if (name == NULL) { //不要忘记检测，否则分配内存失败会抛出异常
	 	LOGE("333333");
	    return 0; /* OutOfMemoryError already thrown */
	}
     LOGE("444444");
//	Photo = fopen(name, "wb+");
//	(*env)->ReleaseStringUTFChars(env, str, name);
	if (takeJPG() == 0) {
		isPicture = 0;
		return 1;
	}
	LOGE("555555");
	return 0;
}
/*
 * Class:     com_jiuan_it_ipc_utils_H264DecodeUtil
 * Method:    decode
 * Signature: ([BI)[B
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_H264DecodeUtil_decode(
		JNIEnv *env, jobject jo, jbyteArray in, jint nalLen, jintArray width,
		jintArray height, jbyteArray outData) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	AVPacket packet;
	av_init_packet(&packet);
	int len, got_frame;
	int got_output = 0;
	int iErrFrame = 0;
	int y_size = pCodecCtx->width * pCodecCtx->height;
	int ret = -1;
	//jbyteArray jarrRV = NULL;

	LOGD("%d-------%d", pCodecCtx->width, pCodecCtx->height);

	if (in == NULL) {
		LOGE("%s-------%d not data", __FUNCTION__, __LINE__);
		ret = -1;
		goto ErrLab;
	}

	jbyte * Buf = (jbyte*) (*env)->GetByteArrayElements(env, in, 0);
	if (Buf == NULL) {
		LOGE("%s-------%d", __FUNCTION__, __LINE__);
		ret = -1;
		goto ErrLab;
	}

	packet.size = nalLen;
	packet.data = Buf;

	// 解码视频流AVPacket，返回值 len>=0时正常否则失败，got_frame>0时表示解码到了pFrame，可以对其进行处理
	len = avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, &packet);
	if (len < 0) {
		LOGE("Error while decoding frame\n");
		LOGE("解码返回值len为：%d", len);
		iErrFrame = 1;
		ret = -1;
		goto ErrLab;
	}

	if (pFrame->key_frame && iErrFrame && pFrame->decode_error_flags == 0)
	{
		iErrFrame = 0;
	}

	LOGD("fmt=%d", pCodecCtx->pix_fmt);
	//查看解码后的数据格式

	if (got_frame > 0&& !iErrFrame && pFrame->decode_error_flags == 0) {
		//转码输出
		LOGD("Saving Frame\n");
		if(!img_convert_ctx){
			int size = avpicture_get_size(AV_PIX_FMT_RGB24, pCodecCtx->width,
							pCodecCtx->height);
			LOGD("size%dwidth%dheight%d", size, pCodecCtx->width, pCodecCtx->height);
//			int sizeyuv = avpicture_get_size(AV_PIX_FMT_YUV420P, pCodecCtx->width,
//					pCodecCtx->height);
//			LOGD("sizeyuv%d", sizeyuv);
			out_buffer = (uint8_t *) av_malloc(size); //申请内存
			//使用参数初始化SwsContext结构体并降低分辨率

			avpicture_fill((AVPicture *) pFrameYUV, out_buffer, AV_PIX_FMT_RGB24,
					pCodecCtx->width, pCodecCtx->height);
			img_convert_ctx = sws_getContext(pCodecCtx->width, pCodecCtx->height,
					pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height, AV_PIX_FMT_RGB24,
					SWS_FAST_BILINEAR, NULL, NULL, NULL); //申请空间
		}

		//使用Libswscale进行格式转换
		sws_scale(img_convert_ctx, (const uint8_t* const *) pFrame->data,
				pFrame->linesize, 0, pCodecCtx->height, pFrameYUV->data,
				pFrameYUV->linesize); // sws_scale()：转换一帧图像。

		//outData = (*env)->NewByteArray(env, pCodecCtx->width * pCodecCtx->height * 3);
		(*env)->SetByteArrayRegion(env, outData, 0, pFrame->width * pFrame->height * 3,
				(jbyte*) pFrameYUV->data[0]);

	} else {
		LOGE("No Saving Frame\n");
		LOGE("got_frame的值为：%d", got_frame);
		ret = -1;
		goto ErrLab;
	}

	jint w[1], h[1];
	w[0] = pCodecCtx->width;
	h[0] = pCodecCtx->height;
	(*env)->SetIntArrayRegion(env, width, 0, 1, w);
	(*env)->SetIntArrayRegion(env, height, 0, 1, h);
	ret = 0;

ErrLab:
	if(out_buffer) {
		av_free(out_buffer);
	}

	if(img_convert_ctx) {
		sws_freeContext(img_convert_ctx); //释放SwsContext结构体。
	}

	if (Buf) {
		(*env)->ReleaseByteArrayElements(env, in, Buf, 0);
	}
	out_buffer = NULL;
	img_convert_ctx = NULL;
	return ret;
}

/*
 * Class:     com_jiuan_it_ipc_utils_H264DecodeUtil
 * Method:    close
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_H264DecodeUtil_close(
		JNIEnv *env, jobject obj) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);

	if(out_buffer) av_free(out_buffer);
	if(img_convert_ctx) sws_freeContext(img_convert_ctx); //释放SwsContext结构体。
	if (pFrame) av_free(pFrame);
	if (pFrameYUV) av_free(pFrameYUV);
	if (pCodecCtx) avcodec_close(pCodecCtx);
	return 0;
}

int takeJPG() {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	AVFormatContext* pFormatCtx;
	AVOutputFormat* fmt;
	AVStream* video_st;
	AVCodecContext* pCodecCtx;
	AVCodec* pCodec;
	AVFrame* picture;
	uint8_t* picture_buf;
	AVPacket pkt;
	int y_size;
	int got_picture = 0;
	int size;

	int ret = 0;

	int in_w = 1280, in_h = 720; //YUV's width and height
//	    const char* out_file = "mnt/sdcard/IPC/Picture/cuc_view_encode.jpg";    //Output file

	av_register_all();

	//Method 1
	pFormatCtx = avformat_alloc_context();
	//Guess format
	fmt = av_guess_format("mjpeg", NULL, NULL);
	pFormatCtx->oformat = fmt;
	//Output URL
	LOGD("%s-------%s", "Output URL为", name);
	if (avio_open(&pFormatCtx->pb, name, AVIO_FLAG_READ_WRITE) < 0) {
		LOGE("Couldn't open output file.");
		return -1;
	}

	//Method 2. More simple
	//avformat_alloc_output_context2(&pFormatCtx, NULL, NULL, out_file);
	//fmt = pFormatCtx->oformat;

	video_st = avformat_new_stream(pFormatCtx, 0);
	if (video_st == NULL) {
		LOGE("video_st==NULL");
		return -1;
	}
	pCodecCtx = video_st->codec;
	pCodecCtx->codec_id = fmt->video_codec;
	pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
	pCodecCtx->pix_fmt = AV_PIX_FMT_YUVJ420P;

	pCodecCtx->width = in_w;
	pCodecCtx->height = in_h;

	pCodecCtx->time_base.num = 1;
	pCodecCtx->time_base.den = 25;
	//Output some information
	av_dump_format(pFormatCtx, 0, name, 1);

	pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
	if (!pCodec) {
		LOGE("Codec not found pCodec");
		return -1;
	}
	if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
		LOGE("Could not open codec.");
		return -1;
	}
	picture = av_frame_alloc();
	size = avpicture_get_size(pCodecCtx->pix_fmt, pCodecCtx->width,
			pCodecCtx->height);
	picture_buf = (uint8_t *) av_malloc(size);
	if (!picture_buf) {
		LOGE("av_malloc出错");
		return -1;
	}
	avpicture_fill((AVPicture *) picture, picture_buf, pCodecCtx->pix_fmt,
			pCodecCtx->width, pCodecCtx->height);
	if (pFrame->data[0] != NULL) {
		//Read YUV
		picture->data[0] = pFrame->data[0]; // Y
		picture->data[1] = pFrame->data[1]; // U
		picture->data[2] = pFrame->data[2]; // V
	} else {
		LOGE("666666");
		return -1;
	}

	//Write Header
	avformat_write_header(pFormatCtx, NULL);

	y_size = pCodecCtx->width * pCodecCtx->height;
	av_new_packet(&pkt, y_size * 3);

	//Encode
	ret = avcodec_encode_video2(pCodecCtx, &pkt, picture, &got_picture);
	if (ret < 0) {
		LOGE("Encode Error.\n");
		return -1;
	}
	if (got_picture == 1) {
		pkt.stream_index = video_st->index;
		ret = av_write_frame(pFormatCtx, &pkt);
	}

	av_free_packet(&pkt);
	//Write Trailer
	av_write_trailer(pFormatCtx);

	LOGE("Encode Successful.\n");
	if (video_st) {
		avcodec_close(video_st->codec);
		av_free(picture_buf);
	}
	avio_close(pFormatCtx->pb);
	avformat_free_context(pFormatCtx);

	return 0;
}

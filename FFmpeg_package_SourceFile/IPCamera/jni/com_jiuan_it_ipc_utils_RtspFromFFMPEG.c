/*
 * com_example_myrtspplayer_RtspFromFFMPEG.c
 *
 *  Created on: 2015-7-22
 *      Author: hongchao
 */

#include <jni.h>
#include <string.h>
#include <stdio.h>
#include "com_jiuan_it_ipc_utils_RtspFromFFMPEG.h"
#include "com_jiuan_it_ipc_utils_RtspRecordVideo.h"

#include <android/log.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <libavutil/log.h>
#include "cbuf.h"

//Output FFmpeg's av_log()
void custom_log(void *ptr, int level, const char* fmt, va_list vl){
    FILE *fp=fopen("mnt/sdcard/RTSP/FFmpeg.txt","a+");
    if(fp){
        vfprintf(fp,fmt,vl);
        fflush(fp);
        fclose(fp);
    }
}

#define CBUF_FLAG 1
#define  LOG_TAG    "RtspFromFFMPEG"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define AVCODEC_MAX_AUDIO_FRAME_SIZE 192000
#define FRAME_DELEY_VALUE_MIN 4400000000000000
#define FRAME_DELEY_VALUE_MAX 4600000000000000

AVFormatContext *pFormatCtx;
int i, videoindex, audioindex;
AVCodecContext *pCodecCtx_video;
AVCodec *pCodec_video;
AVCodecContext *pCodecCtx_audio;
AVCodec *pCodec_audio;
uint8_t *out_buffer_video;
uint8_t *out_buffer_audio;
//AVPacket *packet;
//static struct SwsContext *img_convert_ctx;
struct SwsContext *img_convert_ctx = NULL;
SwrContext* aud_convert_ctx = NULL;
AVDictionary *avdic = NULL;
//FILE *fp_yuv = NULL;
int y_size;
AVFrame *pFrameYUV;
AVRational timebase_pri;

static cbuf_t  m_cbuf_video;
static cbuf_t  m_cbuf_audio;
static int m_line_finish = 0;
static volatile int IsClose = 0;
static volatile int iErrFrame_gloab = 0;
static int64_t iLast_PTS = 0;
static int iStartFlag = 0;
static int64_t iPts_Video;
static int64_t iPts_Audio;
static int iCount_audio = 0;
static int iCurrentWidth = 0;
static int iCurrentHeight = 0;
/*
 * Class:     com_example_myrtspplayer_RtspFromFFMPEG
 * Method:    init
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_init(
		JNIEnv *env, jobject obj, jstring javaString,
		jint timebase_seekDen, jint timebase_seekNum, jint isAudio, jintArray iRelFlag) {

	pFormatCtx = NULL;
	i = 0;
	videoindex = 0;
	audioindex = 0;
	pCodecCtx_video = NULL;
	pCodec_video = NULL;
	pCodecCtx_audio = NULL;
	pCodec_audio = NULL;
	out_buffer_video = NULL;
	out_buffer_audio = NULL;
	img_convert_ctx = NULL;
	aud_convert_ctx = NULL;
	avdic = NULL;
	y_size = 0;
	pFrameYUV = NULL;
//	int iWidth = 0;
//	int iHeight = 0;

	int ret = -1;
	int iArr[1];

	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
    av_log_set_callback(custom_log);

	av_register_all(); //注册组件
	avformat_network_init(); //支持网络流
	pFormatCtx = avformat_alloc_context(); //初始化AVFormatContext
	pFrameYUV = av_frame_alloc(); 
	if (pFrameYUV == NULL)
	{
		ret = -1;
		goto ErrLab;
	}

	//获得RTSP地址
	const char *url = (*env)->GetStringUTFChars(env, javaString, 0);
	LOGD("接收java中的地址是：%s", url);

	ret = avformat_open_input(&pFormatCtx, url, NULL, NULL);
	if (ret != 0) { //打开文件
		LOGE("无法打开文件,函数返回值是：%s\n", url);
		ret = -1;
		goto ErrLab;
	} else {
		LOGD("打开了视频源\n");
	}

	if (avformat_find_stream_info(pFormatCtx, NULL) < 0) //查找流信息
	{
		LOGE("找不到流信息\n");
		ret = -1;
		goto ErrLab;
	} else {
		LOGD("找到流信息\n");
	}

	videoindex = -1;
	LOGD("pFormatCtx->nb_streams的值是:%d\n", pFormatCtx->nb_streams);
	for (i = 0; i <= pFormatCtx->nb_streams; i++)
	{
		if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			videoindex = i;
			break;
		}
	}

	if (videoindex == -1) {
		LOGE("没有找到视频流\n");
		ret = -1;
		goto ErrLab;
	} else {
		LOGD("找到视频流，%d\n", videoindex);
	}

	if (isAudio)
	{
		for (i = 0; i <= pFormatCtx->nb_streams; i++)
		{
			if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
				audioindex = i;
				break;
			}
		}

		if (audioindex == -1) {
			LOGE("没有找到音频流\n");
			ret = -1;
			goto ErrLab;
		} else {
			LOGD("找到音频流，%d\n", audioindex);
		}
	}

	// 得到视频流编码的上下文指针
	pCodecCtx_video = pFormatCtx->streams[videoindex]->codec;
	pCodec_video = avcodec_find_decoder(pCodecCtx_video->codec_id); //在库里面查找支持该格式的解码器
	if (pCodec_video == NULL) {
		LOGE("找不到解码器\n");
		ret = -1;
		goto ErrLab;
	} else {
		LOGD("找到解码器\n");
	}

	if (avcodec_open2(pCodecCtx_video, pCodec_video, NULL) < 0) //打开解码器
	{
		LOGE("打不开解码器\n");
		ret = -1;
		goto ErrLab;
	} else {
		LOGD("打开解码器\n");
	}

	if (isAudio)
	{
		// 得到音频流编码的上下文指针
		pCodecCtx_audio = pFormatCtx->streams[audioindex]->codec;
		pCodec_audio = avcodec_find_decoder(pCodecCtx_audio->codec_id); //在库里面查找支持该格式的解码器
		if (pCodec_audio == NULL) {
			LOGE("找不到解码器\n");
			ret = -1;
			goto ErrLab;
		} else {
			LOGD("找到解码器\n");
		}

		if (avcodec_open2(pCodecCtx_audio, pCodec_audio, NULL) < 0) //打开解码器
		{
			LOGE("打不开解码器\n");
			ret = -1;
			goto ErrLab;
		} else {
			LOGD("打开解码器\n");
		}
	}

	out_buffer_video = (uint8_t *) av_malloc(avpicture_get_size(AV_PIX_FMT_RGB24, pCodecCtx_video->width, pCodecCtx_video->height));
//	avpicture_fill((AVPicture *) pFrameYUV, out_buffer_video, AV_PIX_FMT_RGB24, 1280, 720); //填充AVFrame
//	//像素格式转换。pFrame转换为pFrameYUV。
//	img_convert_ctx = sws_getContext(pCodecCtx_video->width,
//			pCodecCtx_video->height, pCodecCtx_video->pix_fmt, 1280, 720,
//			AV_PIX_FMT_RGB24, SWS_BILINEAR, NULL, NULL, NULL);
	if (pCodecCtx_video->width == 0 || pCodecCtx_video->height == 0)
	{
		LOGE("视频宽高不正常！\n");
			ret = -1;
			goto ErrLab;
		}

	if (pCodecCtx_video->width == 0 || pCodecCtx_video->height == 0)
	{
		LOGE("视频的高宽值不正常！\n");
		ret = -1;
		goto ErrLab;
	}

	avpicture_fill((AVPicture *) pFrameYUV, out_buffer_video, AV_PIX_FMT_RGB24, pCodecCtx_video->width, pCodecCtx_video->height); //填充AVFrame
	//像素格式转换。pFrame转换为pFrameYUV。
	img_convert_ctx = sws_getContext(pCodecCtx_video->width,
			pCodecCtx_video->height, pCodecCtx_video->pix_fmt, pCodecCtx_video->width, pCodecCtx_video->height,
			AV_PIX_FMT_RGB24, SWS_BILINEAR, NULL, NULL, NULL);

	if (img_convert_ctx == NULL) {
		LOGE("Cannot initialize the conversion context!\n");
		ret = -1;
		goto ErrLab;
	}

	y_size = pCodecCtx_video->width * pCodecCtx_video->height;
	av_dump_format(pFormatCtx, 0, url, 0);

	if (isAudio)
	{
		out_buffer_audio = (uint8_t *) av_malloc(AVCODEC_MAX_AUDIO_FRAME_SIZE*2);
		aud_convert_ctx = swr_alloc(); //分配SwrContext
		if (aud_convert_ctx == NULL) {
			LOGE("设置上下文出错");
			ret = -1;
			goto ErrLab;
		}

		aud_convert_ctx = swr_alloc_set_opts(aud_convert_ctx,
											AV_CH_LAYOUT_MONO/*pFrame->channel_layout*/, // out channel layout
											AV_SAMPLE_FMT_S16, // out sample format
											16000, // out sample rate
											av_get_default_channel_layout(pCodecCtx_audio->channels),//av_frame_get_channels(frame)
											pCodecCtx_audio->sample_fmt, // in sample format
											pCodecCtx_audio->sample_rate, // in sample rate
											0, // log offset
											NULL); // log context

		swr_init(aud_convert_ctx);
	}

	cbuf_init(&m_cbuf_video);
	cbuf_init(&m_cbuf_audio);

	IsClose = 0;
	m_line_finish = 0;
	iErrFrame_gloab = 0;
	iLast_PTS = 0;
	iStartFlag = 0;
	iPts_Video = AV_NOPTS_VALUE;
	iPts_Audio = AV_NOPTS_VALUE;

    timebase_pri.den = timebase_seekDen * 2; //1000 * 2
    timebase_pri.num = timebase_seekNum; //1

    iCurrentWidth = pCodecCtx_video->width;
    iCurrentHeight = pCodecCtx_video->height;

    if (iCurrentWidth == 1280){
    	iArr[0] = 3;
    	(*env)->SetIntArrayRegion(env, iRelFlag, 0, 1, iArr);
    }
    else if (iCurrentWidth == 640){
    	iArr[0] = 4;
    	(*env)->SetIntArrayRegion(env, iRelFlag, 0, 1, iArr);
    }
    else if (iCurrentWidth == 320){
    	iArr[0] = 5;
    	(*env)->SetIntArrayRegion(env, iRelFlag, 0, 1, iArr);
    }

	(*env)->ReleaseStringUTFChars(env, javaString, url);
	ret = 0;

ErrLab:
	if (ret == -1) Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_close(env, obj);
	return ret;
}

/*
 * Class:     com_example_myrtspplayer_RtspFromFFMPEG
 * Method:    close
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_close(
		JNIEnv *env, jobject obj) {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	if(out_buffer_video) av_free(out_buffer_video);
	if(out_buffer_audio) av_free(out_buffer_audio);
	if(img_convert_ctx) sws_freeContext(img_convert_ctx);
	if(aud_convert_ctx) swr_free(&aud_convert_ctx);
	if(pFrameYUV) av_frame_free(&pFrameYUV);
	if(pCodecCtx_video) avcodec_close(pCodecCtx_video);
	if(pCodecCtx_audio) avcodec_close(pCodecCtx_audio);
	if(pFormatCtx) avformat_close_input(&pFormatCtx);
	cbuf_destroy(&m_cbuf_video);
	cbuf_destroy(&m_cbuf_audio);
	LOGD("%s-------%d离开该函数", __FUNCTION__, __LINE__);
	return 1;
}

int AVPacket_cbuf_free(cbuf_t *cbuf)
{
    AVPacket *pkt = NULL;
    while (!cbuf_empty(cbuf))
    {
        pkt = (AVPacket *)cbuf_dequeue(cbuf);
        if (pkt)
        {
            av_free_packet(pkt);
        }
    }
    cbuf_destroy(cbuf);
    cbuf_init(cbuf);
    
    return 0;
}


 JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_thread_1RecvPacket
   (JNIEnv *env, jobject obj, jlong seek_pos, jint seek_req, jint isAudio)
 {
	LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
 	AVPacket *packet = (AVPacket *)av_malloc(sizeof(AVPacket));
 	av_init_packet(packet);
 	int index = 0;
 	int ret = -1;

 	//调整视频的播放位置的处理（快进、后退）
	if (seek_req) {
        int64_t seek_target = seek_pos;
        int64_t seek_min = INT64_MIN;
        int64_t seek_max = INT64_MAX;
        
        //LOGE("--------*********-----------seek_pos:%lld\n", seek_pos);
        //把APP的时间戳转换成设备的时间戳
        //int64_t seek_conv_target = av_rescale_q(seek_target, pCodecCtx_video->time_base, timebase_pri);
        //把设备的时间戳转换成APP的时间戳
        int64_t seek_conv_target = av_rescale_q(seek_target, timebase_pri, pCodecCtx_video->time_base);
        LOGE("--------*********-----------timebase_pri:%d\n", timebase_pri.den);
        LOGE("--------*********-----------time_base:%d\n", pCodecCtx_video->time_base.den);
		ret = avformat_seek_file(pFormatCtx, videoindex, seek_min, seek_conv_target, seek_max, 0);
		LOGE("--------*********-----------seek_conv_target:%lld\n", seek_conv_target);
		if (ret >= 0) {
			AVPacket_cbuf_free(&m_cbuf_video);
			AVPacket_cbuf_free(&m_cbuf_audio);
			ret = 1;
			goto Errlab;
		}
	}

	LOGD("av_read_frameing................\n");
	if (av_read_frame(pFormatCtx, packet)>=0)
	{
		if(packet->stream_index==videoindex)
		{
			//LOGE("packet_pts_video->pts = %f\n", packet.pts * av_q2d(pCodecCtx_video->time_base));
			LOGD("av_read_frame:OK\n");
			//AVPacket *packet_cpy = (AVPacket *)av_malloc(sizeof(AVPacket));
			//av_copy_packet(packet_cpy, &packet);
			cbuf_enqueue(&m_cbuf_video, packet);
			//LOGE("thread_1RecvPacket.m_cbuf.size:%d\n", m_cbuf_video.size);
			//av_free_packet(&packet);
		}
		else if(packet->stream_index==audioindex && isAudio)
		{
			//LOGE("packet_pts_audio->pts = %f\n", packet.pts * av_q2d(pCodecCtx_audio->time_base));
			LOGD("av_read_audio_frame:OK\n");
			//AVPacket *packet_cpy = (AVPacket *)av_malloc(sizeof(AVPacket));
			//av_copy_packet(packet_cpy, &packet);
			cbuf_enqueue(&m_cbuf_audio, packet);
			//LOGE("thread_1RecvPacket.m_cbuf.size:%d\n", m_cbuf_audio.size);
			//av_free_packet(&packet);
		}
		else
		{
			av_free_packet(packet);
		}
	 	ret = 0;
	}
	else
	{
		if (cbuf_empty(&m_cbuf_video)){
			LOGE("cbuf_empty:OK\n");
			ret = -1;
		}else {
			ret = 0;
		}

	}

Errlab:
 	return ret;
 }

 JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_thread_1RecvPacket_1Mediacodec
 	 (JNIEnv *env, jobject obj)
 {
 	return 0;
 }

 JNIEXPORT int JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_thread_1DecodePacket
   (JNIEnv *env, jobject obj, jbyteArray jarrRV, jdoubleArray pts_video, jint IsRecord,
	jint IsHaveStop, jlong TimeStamp_Stop, jlongArray TimeStampPri, jint isVideoFile)
 {
	//LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
 	AVPacket *packet = NULL;
 	AVFrame* pFrame = NULL;
 	int ret;
 	int got_picture;
 	double pts_arr[1];
 	int64_t curVideoPts[1];
 	int extra_size[1];
	packet = (AVPacket *)cbuf_dequeue(&m_cbuf_video);
	if (packet == NULL)
	{
		LOGE("m_cbuf_video is NULL\n");
		return -1;
	}
	pFrame = av_frame_alloc();
	ret = avcodec_decode_video2(pCodecCtx_video, pFrame, &got_picture, packet);
	if(ret <= 0)
	{
		LOGE("Decode Error.（解码错误）\n");
		iErrFrame_gloab = 1;
		ret = -1;
		goto ErrLab;
	}

	if(packet->pts != AV_NOPTS_VALUE) {
		pts_arr[0] = packet->pts * av_q2d(pCodecCtx_video->time_base);
		(*env)->SetDoubleArrayRegion(env, pts_video, 0, 1, pts_arr);
		curVideoPts[0] = av_rescale_q(packet->pts, pCodecCtx_video->time_base, timebase_pri);
		(*env)->SetLongArrayRegion(env, TimeStampPri, 0, 1, curVideoPts);
		LOGE("curVideoPts[0]##################%lld\n", curVideoPts[0]);
	} else {
		pts_arr[0] = 0;
		(*env)->SetDoubleArrayRegion(env, pts_video, 0, 1, pts_arr);
	}

	if(pFrame->coded_picture_number == 0)
	{
		iStartFlag = 1;
		if (isVideoFile == 1){
			if (iCurrentWidth != pFrame->width)
			{
				if (pFrame->width == 1280){
					ret = 3;
				}
				else if (pFrame->width == 640){
					ret = 4;
				}
				else if (pFrame->width == 320){
					ret = 5;
				}
				AVPacket_cbuf_free(&m_cbuf_video);
				AVPacket_cbuf_free(&m_cbuf_audio);
				goto ErrLab;
			}

	//		 LOGD("img_convert_ctx-------%d进入该函数", __LINE__);
			 sws_scale(img_convert_ctx,
			 		(const uint8_t* const *) pFrame->data, pFrame->linesize,
			 		0, pCodecCtx_video->height, pFrameYUV->data,
			 		pFrameYUV->linesize);
	//		 LOGD("img_convert_ctx-------%d离开该函数", __LINE__);

			 if (jarrRV == NULL) {
			 	LOGE("Cannot allocate JNI Byte Array");
			 	iErrFrame_gloab = 1;
			 	ret = -1;
			 	goto ErrLab;
			 }

			 (*env)->SetByteArrayRegion(env, jarrRV, 0, pFrame->width * pFrame->height * 3,
			 							(jbyte*) pFrameYUV->data[0]);
		}
		LOGE("成功解码一帧\n");
		ret = 1;
		goto ErrLab;
	}

	if (iLast_PTS + 1 != pFrame->coded_picture_number && !iErrFrame_gloab)
	{
		LOGD("pFrame->coded_picture_number = %d , iLast_PTS = %d, P-Frame is Error\n", pFrame->coded_picture_number, iLast_PTS);
		iErrFrame_gloab = 1;
		ret = NULL;
		goto ErrLab;
	}

	if (pFrame->key_frame && iErrFrame_gloab && pFrame->coded_picture_number > iLast_PTS)
	{
		LOGD("pFrame->coded_picture_number = %d , iLast_PTS = %d, This is a I Frame\n", pFrame->coded_picture_number, iLast_PTS);
		iLast_PTS = pFrame->coded_picture_number;
		iErrFrame_gloab = 0;
	}

	if(got_picture >= 0 && !iErrFrame_gloab && pFrame->decode_error_flags == 0)
	{
		iLast_PTS = pFrame->coded_picture_number;

		if (isVideoFile == 0){
			if (iStartFlag == 1 && !(pFrame->key_frame))
			{
				LOGD("-----------------iStartFlag-------------------------");
				iErrFrame_gloab = 1;
				ret = -1;
				goto ErrLab;
			}
			else if (iStartFlag == 1 && pFrame->key_frame)
			{
				LOGD("-----------------iStartFlag-------------------------");
				iStartFlag = 0;
			}
		}

		if (iCurrentWidth != pFrame->width)
		{
			if (pFrame->width == 1280){
				ret = 3;
			}
			else if (pFrame->width == 640){
				ret = 4;
			}
			else if (pFrame->width == 320){
				ret = 5;
			}
			AVPacket_cbuf_free(&m_cbuf_video);
			AVPacket_cbuf_free(&m_cbuf_audio);
			goto ErrLab;
		}

		LOGD("img_convert_ctx-------%d,img_convert_ctx-------%d\n", pFrame->width, pFrame->height);
		//LOGD("img_convert_ctx-------%d进入该函数", __LINE__);
		ret = sws_scale(img_convert_ctx,
				(const uint8_t* const *) pFrame->data, pFrame->linesize,
				0, pCodecCtx_video->height, pFrameYUV->data,
				pFrameYUV->linesize);

		if (ret < 0)
		{
			LOGE("sws_scale is error!!");
			iErrFrame_gloab = 1;
			ret = -1;
			goto ErrLab;
		}

		if (jarrRV == NULL) {
			LOGE("Cannot allocate JNI Byte Array");
			iErrFrame_gloab = 1;
			ret = -1;
			goto ErrLab;
		}

		jsize len = (*env)->GetArrayLength(env, jarrRV);
		LOGD("img_convert_ctx-------%d离开该函数", len);

		(*env)->SetByteArrayRegion(env, jarrRV, 0, pFrame->width*pFrame->height*3, (jbyte*) pFrameYUV->data[0]);

		LOGD("1111img_convert_ctx-------%d离开该函数", pFrameYUV->linesize);

		if (IsRecord)
		{
			int64_t seek_conv_CurPktPts = av_rescale_q(packet->pts, pCodecCtx_video->time_base, timebase_pri);
			if (IsHaveStop && seek_conv_CurPktPts > TimeStamp_Stop)
			{
				ret = 2;
				goto ErrLab;
			}
			jbyteArray jarrExtradata;
			jbyteArray jarrRV_Record;

			jarrExtradata = (*env)->NewByteArray(env, pCodecCtx_video->extradata_size);
			(*env)->SetByteArrayRegion(env, jarrExtradata, 0, pCodecCtx_video->extradata_size,
										(jbyte*) pCodecCtx_video->extradata);
			jarrRV_Record = (*env)->NewByteArray(env, packet->size);
			(*env)->SetByteArrayRegion(env, jarrRV_Record, 0, packet->size, (jbyte*) packet->data);
			Java_com_jiuan_it_ipc_utils_RtspRecordVideo_record(env, obj, jarrRV_Record, packet->size,
															jarrExtradata, pCodecCtx_video->extradata_size, 1);
			//(*env)->ReleaseByteArrayElements(env, jarrExtradata, Buf, 0);
			(*env)->DeleteLocalRef(env, jarrExtradata);
			(*env)->DeleteLocalRef(env, jarrRV_Record);
		}
		LOGD("1111img_convert_ctx-------%d,img_convert_ctx-------%d\n", pFrame->width, pFrame->height);
		ret = 0;
	}
	else
	{
		iLast_PTS = pFrame->coded_picture_number;
		LOGE("Decode Error.（解码错误）----%d-----%d-----%d \n",got_picture, iErrFrame_gloab, pFrame->decode_error_flags);
		iErrFrame_gloab = 1;
		ret = -1;
		goto ErrLab;
	}

ErrLab:

	if(packet) av_free_packet(packet);
	if(pFrame) av_frame_free(&pFrame);
	return ret;
 }

 JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_thread_1DecodePacket_1audio
   (JNIEnv *env, jobject obj, jbyteArray jarrRV, jdoubleArray pts_audio)
 {
	//LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
	AVPacket *packet = NULL;
	AVFrame* pFrame = NULL;
	int ret = 0;
	int got_audio;
	double pts_arr[1];

	packet = (AVPacket *)cbuf_dequeue(&m_cbuf_audio);
	if (packet == NULL)
	{
		//LOGE("m_cbuf_audio is NULL\n");
		ret = -1;
		goto ErrLab;
	}

	//avcodec_decode_audio4
	pFrame = av_frame_alloc();
	ret = avcodec_decode_audio4(pCodecCtx_audio, pFrame, &got_audio, packet);
	if(ret <= 0){
		LOGE("Decode Error.（解码错误）\n");
		ret = -1;
		goto ErrLab;
	}

	if(packet->pts != AV_NOPTS_VALUE) {
		int64_t seek_conv_CurPktPts = av_rescale_q(packet->pts, pCodecCtx_audio->time_base, pCodecCtx_video->time_base);
		pts_arr[0] = seek_conv_CurPktPts * av_q2d(pCodecCtx_video->time_base);
		//pts_arr[0] = packet->pts * av_q2d(pCodecCtx_audio->time_base);
		(*env)->SetDoubleArrayRegion(env, pts_audio, 0, 1, pts_arr);
	} else {
		pts_arr[0] = 0;
		(*env)->SetDoubleArrayRegion(env, pts_audio, 0, 1, pts_arr);
	}

	if (got_audio > 0)
	{
		int error = swr_convert(aud_convert_ctx, &out_buffer_audio,
								AVCODEC_MAX_AUDIO_FRAME_SIZE,
								(const uint8_t **) pFrame->data,
								pFrame->nb_samples);

		int bufsize = av_samples_get_buffer_size(NULL,av_get_channel_layout_nb_channels(AV_CH_LAYOUT_MONO),pFrame->nb_samples,AV_SAMPLE_FMT_S16, 1);

		(*env)->SetByteArrayRegion(env, jarrRV, 0, bufsize, out_buffer_audio);
		ret = bufsize;

		LOGD("解码完成一帧,当前i值为：%d",bufsize);
		//ret = 0;
	}
	else
	{
		ret = -1;
		goto ErrLab;
	}

ErrLab:
	if(packet) av_free_packet(packet);
	if(pFrame) av_frame_free(&pFrame);
	return ret;
 }

 JNIEXPORT jint JNICALL Java_com_example_myrtspplayer_RtspFromFFMPEG_thread_1MediaCodecPacket
   (JNIEnv *env, jobject obj, jbyteArray jarrRV)
 {
	int ret = 0;
	int got_picture;
	AVPacket *packet = NULL;

	if(cbuf_empty(&m_cbuf_video))
	{
		ret = -1;
		return ret;
	}

	packet = (AVPacket *)cbuf_dequeue(&m_cbuf_video);
	if (packet == NULL)
	{
		LOGE("AVPacket is Error!!");
		ret = -1;
		return ret;
	}

	if (jarrRV == NULL) {
		LOGE("Cannot allocate JNI Byte Array");
		ret = -1;
	}

	(*env)->SetByteArrayRegion(env, jarrRV, 0, packet->size,
							(jbyte*)packet->data);

	ret = packet->size;
	LOGE("packet->size:%d", ret);

ErrLab:
	av_free_packet(packet);
	LOGD("成功返回一帧数据！\n");
	return ret;
 }

 JNIEXPORT jlong JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_ConvertTimeBase
   (JNIEnv *env, jobject obj, jlong timestamp, jint srctimebase, jint dsttimebase)
 {
	 AVRational timebase_src;
	 AVRational timebase_dst;
	 int64_t srcTimeStamp = timestamp;

	 timebase_src.den = srctimebase * 2;
	 timebase_src.num = 1;

	 timebase_dst.den = dsttimebase * 2;
	 timebase_dst.num = 1;

     int64_t seek_conv_target = av_rescale_q(srcTimeStamp, timebase_src, timebase_dst);

     return seek_conv_target;
 }

 JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_PCM2G711a
//    (JNIEnv *env, jobject obj, jbyteArray SpeechData, jbyteArray BitstreamData, jint srclen)
  (JNIEnv *env, jobject obj, jbyteArray indata, jbyteArray outdata, jint datalen)
  {
//	int dstlen;
//	jbyte * speech = (jbyte*)(*env)->GetByteArrayElements(env, SpeechData, 0);
//	jbyte * bitstream= (jbyte*)(*env)->GetByteArrayElements(env, BitstreamData, 0);
//
//	g711a_Encode(speech, bitstream, srclen, &dstlen);
//
//	(*env)->ReleaseByteArrayElements(env, SpeechData, speech, 0);
//	(*env)->ReleaseByteArrayElements(env, BitstreamData, bitstream, 0);
//	return dstlen;

	int ret = -1;

	if(indata && datalen > 0)
	{
		jbyte* inStrArr = (jbyte*)(*env)->GetByteArrayElements(env, indata, 0);
		jbyte* outStrArr= (jbyte*)(*env)->GetByteArrayElements(env, outdata, 0);

		ret = PCM2G711a((char*)inStrArr, (char*)outStrArr, (int)datalen, 0);
//		if (ret > 0)
//		{
//			LOGE("RtspFromFFMPEG_PCM2G711a: datalen %d, ret: %d \n", datalen, ret);
//			(*env)->SetByteArrayRegion(env, outdata, 0, ret, outStrArr);
//		}

		(*env)->ReleaseByteArrayElements(env, indata, inStrArr, 0);
		(*env)->ReleaseByteArrayElements(env, outdata, outStrArr, 0);
	}

 	 return ret;

  }

 JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_G711a2PCM
 (JNIEnv *env, jobject obj, jbyteArray indata, jbyteArray outdata, jint datalen)
 //(JNIEnv* env, jobject thiz, jbyteArray BitstreamData, jbyteArray SpeechData, jint srclen)
 {
	int ret = -1;

	if(indata && datalen > 0)
	{
		jbyte* inStrArr = (jbyte*)(*env)->GetByteArrayElements(env, indata, 0);
		jbyte* outStrArr= (jbyte*)(*env)->GetByteArrayElements(env, outdata, 0);

		ret = G711a2PCM((char*)inStrArr, (char*)outStrArr, (int)datalen, 0);
//		if (ret > 0)
//		{
//			LOGE("RtspFromFFMPEG_PCM2G711a: datalen %d, ret: %d \n", datalen, ret);
//			(*env)->SetByteArrayRegion(env, outdata, 0, ret, outStrArr);
//		}

		(*env)->ReleaseByteArrayElements(env, indata, inStrArr, 0);
		(*env)->ReleaseByteArrayElements(env, outdata, outStrArr, 0);
	}

 	return ret;
//	int dstlen = 0;
//	jbyte * bitstream= (jbyte*)(*env)->GetByteArrayElements(env, BitstreamData, 0);
//	jbyte * speech = (jbyte*)(*env)->GetByteArrayElements(env, SpeechData, 0);
//
//	g711a_Decode(bitstream, speech, srclen, &dstlen);
//
//	(*env)->ReleaseByteArrayElements(env, BitstreamData, bitstream, 0);
//	(*env)->ReleaseByteArrayElements(env, SpeechData, speech, 0);
//	return dstlen;
  }

 JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspFromFFMPEG_ClearAudioBuffer(JNIEnv *env, jobject obj)
 {
	 AVPacket_cbuf_free(&m_cbuf_audio);
	 return 0;
 }

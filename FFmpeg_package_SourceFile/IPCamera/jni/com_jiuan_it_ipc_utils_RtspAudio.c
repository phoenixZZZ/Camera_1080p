#include "com_jiuan_it_ipc_utils_RtspAudio.h"
#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include "libswresample/swresample.h"
#include "libavutil/log.h"
#include "cbuf.h"
//
//#include "errno.h"
//#include "libavutil/avutil.h"

//Output FFmpeg's av_log()
void custom_log(void *ptr, int level, const char* fmt, va_list vl) {
	FILE *fp = fopen("mnt/sdcard/RTSP/FFmpeg-audio.txt", "a+");
	if (fp) {
		vfprintf(fp, fmt, vl);
		fflush(fp);
		fclose(fp);
	}
}


#define AVCODEC_MAX_AUDIO_FRAME_SIZE 192000

AVFormatContext *pFormatCtx;
int i, videoindex;
AVCodecContext *pCodecCtx_video;
AVCodec *pCodec_video;
uint8_t *out_buffer_video;
AVPacket packet;
AVFrame *pFrame;
//static struct SwsContext *img_convert_ctx;
SwrContext* swrContext = NULL;
AVDictionary *avdic = NULL;
//FILE *g711_out = NULL,*pcm_out = NULL;
int y_size;
int i =0;
static cbuf_t  m_cbuf;
static int m_line_finish = 0;

/*
 * Class:     com_jiuan_it_ipc_utils_RtspAudio
 * Method:    init
 * Signature: (Ljava/lang/String;)I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspAudio_init(
		JNIEnv *env, jobject obj, jstring javaString) {
	//LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);

	//	fp_yuv = fopen("mnt/sdcard/RTSP/RTSPoutput.yuv", "wb+");
	//	if (!fp_yuv) {
	//		LOGE("Codec not open fp_yuv\n");
	//		return -1;
	//	}
	av_register_all(); //注册组件
	avformat_network_init(); //支持网络流
	pFormatCtx = avformat_alloc_context(); //初始化AVFormatContext
	pFrame = av_frame_alloc(); //存储解码后AVFrame
//	pFrameYUV = av_frame_alloc(); //存储转换后AVFrame（为什么要转换？后文解释）
	const char *url = (*env)->GetStringUTFChars(env, javaString, 0);
//	char url[]="mnt/sdcard/RTSP/IPC_20150611173121.aac";
	//LOGD("接收java中的地址是：%s", url);

	int r = avformat_open_input(&pFormatCtx, url, NULL, &avdic);
	if (r != 0) { //打开文件
		//LOGE("无法打开文件,函数返回值是：%d\n", r);
		return -1;
	} else {
		//LOGD("打开了视频源\n");
	}
	if (avformat_find_stream_info(pFormatCtx, &avdic) < 0) //查找流信息
			{
		//LOGE("找不到流信息\n");
		return -1;
	} else {
		//LOGD("找到流信息\n");
	}
	videoindex = -1;
	//LOGD("pFormatCtx->nb_streams的值是:%d\n", pFormatCtx->nb_streams);
	for (i = 0; i <= pFormatCtx->nb_streams; i++) //遍历文件的流，找到第一个音频流，并记录流的编码信息
		if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
			videoindex = i;
			break;
		}
	if (videoindex == -1) {
		//LOGE("没有找到音频流\n");
		return -1;
	} else {
		//LOGD("找到音频流\n");
	}
	// 得到音频流编码的上下文指针
	pCodecCtx_video = pFormatCtx->streams[videoindex]->codec;
	//LOGD("codec_id的值为：%d", pCodecCtx_video->codec_id);
	pCodec_video = avcodec_find_decoder(pCodecCtx_video->codec_id); //在库里面查找支持该格式的解码器
	if (pCodec_video == NULL) {
		//LOGE("找不到解码器\n");
		return -1;
	} else {
		//LOGD("找到解码器\n");
	}
	if (avcodec_open2(pCodecCtx_video, pCodec_video, NULL) < 0) //打开解码器
			{
		//LOGE("打不开解码器\n");
		return -1;
	} else {
		//LOGD("打开解码器\n");
	}
	out_buffer_video = (uint8_t *) av_malloc(AVCODEC_MAX_AUDIO_FRAME_SIZE*2);

	swrContext = swr_alloc(); //分配SwrContext
	if (swrContext == NULL) {
		//LOGE("设置上下文出错");
	}

//	g711_out = fopen("mnt/sdcard/RTSP/g711_out.g711", "wb+");
//	if (!g711_out) {
//		LOGE("Codec not open g711_out.g711\n");
//	}
//	pcm_out = fopen("mnt/sdcard/RTSP/pcm_out.pcm", "wb+");
//		if (!g711_out) {
//			LOGE("Codec not open pcm_out.pcm\n");
//		}

//
//	avpicture_fill((AVPicture *) pFrameYUV, out_buffer, AV_PIX_FMT_RGB24, 352,
//			288); //填充AVFrame

//	y_size = pCodecCtx->width * pCodecCtx->height;
//	LOGD("文件信息-----------------------------------------\n");
//	av_dump_format(pFormatCtx, 0, url, 0);
//	LOGD("-------------------------------------------------\n");

	//cbuf_init(&m_cbuf);
	m_line_finish = 0;

	(*env)->ReleaseStringUTFChars(env, javaString, url);

	return 1;
}

/*
 * Class:     com_jiuan_it_ipc_utils_RtspAudio
 * Method:    decode
 * Signature: ()[B
 */JNIEXPORT jbyteArray JNICALL Java_com_jiuan_it_ipc_utils_RtspAudio_decode(
		JNIEnv *env, jobject obj) {
	//LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);

//	packet = (AVPacket *) malloc(sizeof(&AVPacket)); //存储解码前数据包AVPacket
	av_init_packet(&packet);
//	av_new_packet(packet, y_size);

	//-----------------------------
	int ret, got_picture, out_size = 0;

	//------------------------------
	jbyteArray outData;

	if (av_read_frame(pFormatCtx, &packet) >= 0) //循环获取压缩数据包AVPacket
			{
		//LOGD("packet->stream_index值为：%d videoindex值为：%d\n", packet.stream_index, videoindex);
		// Is this a packet from the video stream?
		if (packet.stream_index == videoindex) {
            i++;
//            fwrite( packet.data, 1, packet.size, g711_out); //保存接收到的音频

			while (packet.size > 0) {
				int len = 1;
				len = avcodec_decode_audio4(pCodecCtx_video, pFrame, &out_size,
						&packet);
				//LOGD("解码返回值%d", len);
				if (len <= 0) {
					//LOGE("Error while decoding\n");
					return NULL;
				}
				if (out_size > 0) {
//					 fwrite((const uint8_t **)pFrame->data, 1, len, pcm_out); //保存解码直接获取数据

					//转码
//						swrContext = swr_alloc_set_opts(swrContext, pFrame->channel_layout, // out channel layout
//								AV_SAMPLE_FMT_S16, // out sample format
//								pFrame->sample_rate, // out sample rate
//								pFrame->channel_layout, // in channel layout
//								AV_SAMPLE_FMT_FLTP, // in sample format
//								pFrame->sample_rate, // in sample rate
//								0, // log offset
//								NULL); // log context
					int bufsize = av_samples_get_buffer_size(NULL,AV_CH_LAYOUT_STEREO ,pFrame->nb_samples,AV_SAMPLE_FMT_S16, 1);
					//LOGI( "bufsize: %d\n", bufsize);//bufsize: 3456
					//LOGI( "Bitrate:\t %3d\n", pFormatCtx->bit_rate);//Bitrate:	   0
					//LOGI("Decoder Name:\t %s\n", pCodecCtx_video->codec->long_name);//Decoder Name:	 PCM A-law / G.711 A-law
					//LOGI("Channels:\t %d\n", pCodecCtx_video->channels);//Channels:	 1
					//LOGI("Sample per Second\t %d \n", pCodecCtx_video->sample_rate);//Sample per Second	 8000
					//LOGI("sample_fmts：%d", (int)(*(pCodec_video->sample_fmts)));//sample_fmts：1
					//LOGI("pFrame->nb_samples：%d", pFrame->nb_samples);//pFrame->nb_samples：576
					//LOGI("pFrame->sample_rate：%d", pFrame->sample_rate);//pFrame->sample_rate：8000
					//LOGI("channel_layout：%d", pFrame->channel_layout);//channel_layout：0
					//LOGI("channel_layout新：%d", (int)av_get_default_channel_layout(pCodecCtx_video->channels));//4
					//LOGI("channel_layout新新：%d", av_frame_get_channels(pFrame));//1
					//LOGI("sample_rate：%d", pFrame->sample_rate);//8000
					swrContext = swr_alloc();
					swrContext = swr_alloc_set_opts(swrContext,
													AV_CH_FRONT_LEFT/*pFrame->channel_layout*/, // out channel layout
													AV_SAMPLE_FMT_S16P, // out sample format
													8000, // out sample rate
													av_frame_get_channels(pFrame),//av_frame_get_channels(frame)
													*(pCodec_video->sample_fmts), // in sample format
													pFrame->sample_rate, // in sample rate
													0, // log offset
													NULL); // log context
//					swrContext = swr_alloc_set_opts(swrContext,
//							                    AV_CH_LAYOUT_STEREO/*pFrame->channel_layout*/, // out channel layout
//												AV_SAMPLE_FMT_S16, // out sample format
//												44100, // out sample rate
//												av_get_default_channel_layout(pCodecCtx->channels)/*pFrame->channel_layout*/, // in channel layout
//												pCodecCtx->sample_fmt, // in sample format
//												pCodecCtx->sample_rate, // in sample rate
//												0, // log offset
//												NULL); // log context

					if (swrContext == NULL) {
						//LOGE("设置参数出错");
						return NULL;
					}
					int re = swr_init(swrContext);
					if (re < 0) {
						//LOGE("初始化结果：%d", re);
						return NULL;
					}
					int error = swr_convert(swrContext, &out_buffer_video,
											AVCODEC_MAX_AUDIO_FRAME_SIZE,
											(const uint8_t **) pFrame->data,
											pFrame->nb_samples);
					if (error < 0) {
						//LOGE("转码出错");
						return NULL;
					} else {
						//LOGD("error的值%d", error);
					}

					outData = (*env)->NewByteArray(env, bufsize);
					(*env)->SetByteArrayRegion(env, outData, 0, bufsize, out_buffer_video);

					//LOGD("解码完成一帧,当前i值为：%d",i);
				} else {
					//LOGE("No Saving Frame\n");
					return NULL;
				}
				packet.size -= len;
				packet.data += len;

			}

//				return outData; //////
		} else {
			//LOGE("This is not a packet from the video stream!\n");
			return NULL;
		}

	}
	av_free_packet(&packet);
//	sws_freeContext(img_convert_ctx);
	//LOGD("成功返回一帧音频数据！\n");
	return outData; //////

}

JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspAudio_thread_1RecvPacket
   (JNIEnv *env, jobject obj)
{

}

/*
 * Class:     com_jiuan_it_ipc_utils_RtspAudio
 * Method:    close
 * Signature: ()I
 */JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspAudio_close(
		JNIEnv *env, jobject obj) {
	//LOGD("%s-------%d进入该函数", __FUNCTION__, __LINE__);
//		fclose(fp_yuv);
	free(out_buffer_video);
//	av_free(pFrameYUV);
	av_free(pFrame);
	avcodec_close(pCodecCtx_video);
	av_free(pCodecCtx_video);
	swr_free(&swrContext);
//	fclose(g711_out);
//	fclose(pcm_out);
	avformat_close_input(&pFormatCtx);
	return 1;
}


#include "com_jiuan_it_ipc_utils_CombineVideo.h"


AVFormatContext *in1_fmtctx = NULL;
AVFormatContext *in2_fmtctx = NULL;
AVFormatContext *out_fmtctx = NULL;
AVStream *out_video_stream = NULL;
AVStream *out_audio_stream = NULL;
int video_stream_index = -1;
int audio_stream_index = -1;

int CombineVideo_OpenInput(const char* inFileName1, const char* inFileName2)
{
	int ret = -1;
	if ((ret = avformat_open_input(&in1_fmtctx, inFileName1, NULL, NULL)) < 0)
	{
		ret = -1;
		goto ErrLab;
	}

	if ((ret = avformat_find_stream_info(in1_fmtctx, NULL)) < 0)
	{
		ret = -1;
		goto ErrLab;
	}

	if ((ret = avformat_open_input(&in2_fmtctx, inFileName2, NULL, NULL)) < 0)
	{
		ret = -1;
		goto ErrLab;
	}

	if ((ret = avformat_find_stream_info(in2_fmtctx, NULL)) < 0)
	{
		ret = -1;
		goto ErrLab;
	}
	ret = 0;

ErrLab:
	return ret;
}

int CombineVideo_OpenOutput(const char* outFileName, int isAudio)
{
	int ret = -1;
	int i = 0;
	if ((ret = avformat_alloc_output_context2(&out_fmtctx, NULL, NULL, outFileName)) < 0)
	{
		ret = -1;
		goto ErrLab;
	}

	//new stream for out put
	for (i = 0; i < in1_fmtctx->nb_streams; i++)
	{
		if (in1_fmtctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO)
		{
			video_stream_index = i;
			out_video_stream = avformat_new_stream(out_fmtctx, NULL);
			if (!out_video_stream)
			{
				ret = -1;
				goto ErrLab;
			}
			if ((ret = avcodec_copy_context(out_video_stream->codec, in1_fmtctx->streams[i]->codec)) < 0)
			{
				ret = -1;
				goto ErrLab;
			}
			out_video_stream->codec->codec_tag = 0;
			if(out_fmtctx->oformat->flags & AVFMT_GLOBALHEADER)
			{
				out_video_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
			}
		}
		else if (in1_fmtctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO && isAudio)
		{
			audio_stream_index = i;
			out_audio_stream = avformat_new_stream(out_fmtctx, NULL);
			if (!out_audio_stream)
			{
				ret = -1;
				goto ErrLab;
			}
			if ((ret = avcodec_copy_context(out_audio_stream->codec, in1_fmtctx->streams[i]->codec)) < 0)
			{
				ret = -1;
				goto ErrLab;
			}
			out_audio_stream->codec->codec_tag = 0;
			if(out_fmtctx->oformat->flags & AVFMT_GLOBALHEADER)
			{
				out_audio_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
			}
		}
	}

	//open output file
	if (!(out_fmtctx->oformat->flags & AVFMT_NOFILE))
	{
		if ((ret = avio_open(&out_fmtctx->pb, outFileName, AVIO_FLAG_WRITE)) < 0)
		{
			ret = -1;
			goto ErrLab;
		}
	}

	//write out  file header
	if ((ret = avformat_write_header(out_fmtctx, NULL)) < 0)
	{
		ret = -1;
		goto ErrLab;
	}
	ret = 0;
ErrLab:
	return ret;
}

JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_CombineVideo_combine(JNIEnv *env, jobject obj,
																jstring inFileName1, 
																jstring inFileName2, 
																jstring outFileName,
																jint isAudio)
{
	const char *strInName1 = (*env)->GetStringUTFChars(env, inFileName1, 0);
	const char *strInName2 = (*env)->GetStringUTFChars(env, inFileName2, 0);
	const char *strOutName = (*env)->GetStringUTFChars(env, outFileName, 0);

	in1_fmtctx = NULL;
	in2_fmtctx = NULL;
	out_fmtctx = NULL;
	out_video_stream = NULL;
	out_audio_stream = NULL;
	video_stream_index = -1;
	audio_stream_index = -1;

	int ret = -1;

	av_register_all();	
	if (0 > CombineVideo_OpenInput(strInName1, strInName2))
	{
		ret = -1;
		goto ErrLab;
	}
	
	if(0 > CombineVideo_OpenOutput(strOutName,isAudio))
	{
		ret = -1;
		goto ErrLab;
	}

	AVFormatContext *input_ctx = in1_fmtctx;
	AVPacket pkt;
	int64_t pts_v = 0;
	int64_t pts_a = 0;
	int64_t dts_v = 0;
	int64_t dts_a = 0;
	while(1)
	{
		if(0 > av_read_frame(input_ctx, &pkt))
		{
			if (input_ctx == in1_fmtctx)
			{
				float vedioDuraTime, audioDuraTime;

				//calc the first media dura time
				if (isAudio) {
					vedioDuraTime = ((float)input_ctx->streams[video_stream_index]->time_base.num /
						(float)input_ctx->streams[video_stream_index]->time_base.den) * ((float)pts_v);
					audioDuraTime = ((float)input_ctx->streams[audio_stream_index]->time_base.num /
						(float)input_ctx->streams[audio_stream_index]->time_base.den) * ((float)pts_a);
				}
                else
                {
                    vedioDuraTime = ((float)input_ctx->streams[video_stream_index]->time_base.num /
                                     (float)input_ctx->streams[video_stream_index]->time_base.den) * ((float)pts_v);
                    audioDuraTime = 0;
                }

                //当第一个视频文件和第二个视频文件的timebase不一致的时候，需要将其进行timebase变换。
				if (input_ctx->streams[pkt.stream_index]->time_base.den != in2_fmtctx->streams[pkt.stream_index]->time_base.den)
				{
					pts_v = av_rescale_q_rnd(pts_v, input_ctx->streams[pkt.stream_index]->time_base,
						in2_fmtctx->streams[pkt.stream_index]->time_base, (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
					dts_v = av_rescale_q_rnd(dts_v, input_ctx->streams[pkt.stream_index]->time_base,
						in2_fmtctx->streams[pkt.stream_index]->time_base, (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
				}

				//calc the pts and dts end of the first media
				if (audioDuraTime > vedioDuraTime)
				{
					dts_v = pts_v = audioDuraTime / ((float)input_ctx->streams[video_stream_index]->time_base.num / 
						(float)input_ctx->streams[video_stream_index]->time_base.den);
					dts_a++;
					pts_a++;
				}
				else
				{
					if (isAudio) {
						dts_a = pts_a = vedioDuraTime / ((float)input_ctx->streams[audio_stream_index]->time_base.num /
							(float)input_ctx->streams[audio_stream_index]->time_base.den);
						dts_v++;
						pts_v++;
					}
                    else
                    {
                        dts_a = pts_a = vedioDuraTime;
                        dts_v++;
                        pts_v++;
                    }
				}
				input_ctx = in2_fmtctx;
				continue;
			}
			break;
		}

		if (pkt.stream_index == video_stream_index)
		{
			if (input_ctx == in2_fmtctx)
			{
				pkt.pts += pts_v;
				pkt.dts += dts_v;
			}
			else
			{
				pts_v = pkt.pts;
				dts_v = pkt.dts;
			}
		}
		else if (pkt.stream_index == audio_stream_index)
		{
			if (input_ctx == in2_fmtctx)
			{
				pkt.pts += pts_a;
				pkt.dts += dts_a;
			}
			else
			{
				pts_a = pkt.pts;
				dts_a = pkt.dts;
			}
		}

        if (pkt.stream_index == video_stream_index)
        {
			pkt.pts = av_rescale_q_rnd(pkt.pts, input_ctx->streams[pkt.stream_index]->time_base,
				out_fmtctx->streams[pkt.stream_index]->time_base, AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX);
			pkt.dts = av_rescale_q_rnd(pkt.dts, input_ctx->streams[pkt.stream_index]->time_base,
				out_fmtctx->streams[pkt.stream_index]->time_base, AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX);
			pkt.pos = -1;

			if (av_interleaved_write_frame(out_fmtctx, &pkt) < 0)
			{
				printf( "Error muxing packet\n");
				//break;
			}
		}
		av_free_packet(&pkt);		
	}

	av_write_trailer(out_fmtctx);
	ret = 0;

ErrLab:
	avformat_close_input(&in1_fmtctx);
	avformat_close_input(&in2_fmtctx);

	if (out_fmtctx && !(out_fmtctx->oformat->flags & AVFMT_NOFILE))
		avio_close(out_fmtctx->pb);

	avformat_free_context(out_fmtctx);
	return ret;
}

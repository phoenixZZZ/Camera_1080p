package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 音频格式选择
 */
public class AudioFormatSend extends  SendModel implements Parcelable {

    //音视频格式
    public static final  int  MEDIA_CODEC_UNKNOWN=0;

    public static final  int  MEDIA_CODEC_VIDEO_MPEG4=1;

    public static final  int  MEDIA_CODEC_VIDEO_H263=2;

    public static final  int  MEDIA_CODEC_VIDEO_H264=3;

    public static final  int  MEDIA_CODEC_VIDEO_MJPEG=4;

    public static final  int  MEDIA_CODEC_AUDIO_AAC=5;

    public static final  int  MEDIA_CODEC_AUDIO_G711U=6;

    public static final  int  MEDIA_CODEC_AUDIO_G711A=7;

    public static final  int  MEDIA_CODEC_AUDIO_ADPCM=8;

    public static final  int  MEDIA_CODEC_AUDIO_PCM=9;

    public static final  int  MEDIA_CODEC_AUDIO_SPEEX=10;

    public static final  int  MEDIA_CODEC_AUDIO_MP3=11;

    public static final  int  MEDIA_CODEC_AUDIO_G726=12;

    @SerializedName("MediaCodecType")
    private int mediaType;  //要设置的音视频格式代码

    public static final Creator<AudioFormatSend> CREATOR = new Creator<AudioFormatSend>() {
        @Override
        public AudioFormatSend createFromParcel(Parcel in) {
            AudioFormatSend model = new AudioFormatSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.mediaType = in.readInt();

            return model;
        }

        @Override
        public AudioFormatSend[] newArray(int size) {
            return new AudioFormatSend[size];
        }
    };

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(idcp);
        parcel.writeString(header);
        parcel.writeStringArray(target);
        parcel.writeInt(mark);
        parcel.writeStringArray(server);
        parcel.writeStringArray(client);
        parcel.writeInt(channel);
        parcel.writeInt(mediaType);
    }
}
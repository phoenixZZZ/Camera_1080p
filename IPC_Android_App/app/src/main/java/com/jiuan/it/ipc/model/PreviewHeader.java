package com.jiuan.it.ipc.model;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class PreviewHeader implements Parcelable {

    private String 	 header; 	//数据帧起始头部，值为0x00AA00AA 4
    private int 	 pktSize; 	//数据帧大小（包括数据帧头部和数据帧有效图像数据）4
    private String   namepre;	//此数据帧所在预览文件名称 32
    private String   namerec;	//此数据帧对应录像文件名称 32
    private int 	 seqNumpre;  	//此数据帧在整个预览文件中的帧序号（由0开始） 4
    private int 	 seqNumrec;  	//此数据帧在对应同一个录像文件的所有数据帧中的相对序列号（每个录像文件的第一个数据帧帧序号为0） 4
    //private long 	 dataTime; 		//数据的获取时间 8
    private long    dataTimeSingle; //单个文件时间戳 8
    private long    dataTimeWhole; //全局时间戳 8
    private int 	 timeBaseNum;		//timebase分子 4
    private int 	 timeBaseDen;			//timebase分母 4
    private long	 checksum;   	//crc32校验值 4
    private int 	 bytesSent;  	//数据帧有效图像数据长度 4
    private String   image;   //预览图像
    public static final Creator<PreviewHeader> CREATOR = new Creator<PreviewHeader>() {
        @Override
        public PreviewHeader createFromParcel(Parcel in) {
            PreviewHeader model = new PreviewHeader();
            model.header = in.readString();
            model.pktSize = in.readInt();
            model.namepre = in.readString();
            model.namerec = in.readString();
            model.seqNumpre = in.readInt();
            model.seqNumrec = in.readInt();
            //model.dataTime = in.readLong();
            model.dataTimeSingle = in.readLong();
            model.dataTimeWhole = in.readLong();
            model.timeBaseNum = in.readInt();
            model.timeBaseDen = in.readInt();
            model.checksum = in.readLong();
            model.bytesSent = in.readInt();
            model.image = in.readString();
            return model;
        }

        @Override
        public PreviewHeader[] newArray(int size) {
            return new PreviewHeader[size];
        }
    };

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getPktSize() {
        return pktSize;
    }

    public void setPktSize(int pktSize) {
        this.pktSize = pktSize;
    }

    public String getNamepre() {
        return namepre;
    }

    public void setNamepre(String namepre) {
        this.namepre = namepre;
    }

    public String getNamerec() {
        return namerec;
    }

    public void setNamerec(String namerec) {
        this.namerec = namerec;
    }

    public int getSeqNumpre() {
        return seqNumpre;
    }

    public void setSeqNumpre(int seqNumpre) {
        this.seqNumpre = seqNumpre;
    }

    public int getSeqNumrec() {
        return seqNumrec;
    }

    public void setSeqNumrec(int seqNumrec) {
        this.seqNumrec = seqNumrec;
    }

    public long getDataTimeSingle() {
        return dataTimeSingle;
    }

    public void setDataTimeSingle(long dataTimeSingle) {
        this.dataTimeSingle = dataTimeSingle;
    }

    public long getDataTimeWhole() {
        return dataTimeWhole;
    }

    public void setDataTimeWhole(long dataTimeWhole) {
        this.dataTimeWhole = dataTimeWhole;
    }

    public int getTimeBaseNum() {
        return timeBaseNum;
    }

    public void setTimeBaseNum(int timeBaseNum) {
        this.timeBaseNum = timeBaseNum;
    }

    public int getTimeBaseDen() {
        return timeBaseDen;
    }

    public void setTimeBaseDen(int timeBaseDen) {
        this.timeBaseDen = timeBaseDen;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(int bytesSent) {
        this.bytesSent = bytesSent;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(header);
        dest.writeInt(pktSize);
        dest.writeString(namepre);
        dest.writeString(namerec);
        dest.writeInt(seqNumpre);
        dest.writeInt(seqNumrec);
        dest.writeLong(dataTimeSingle);
        dest.writeLong(dataTimeWhole);
        dest.writeInt(timeBaseNum);
        dest.writeInt(timeBaseDen);
        dest.writeLong(checksum);
        dest.writeInt(bytesSent);
        dest.writeString(image);
    }
}

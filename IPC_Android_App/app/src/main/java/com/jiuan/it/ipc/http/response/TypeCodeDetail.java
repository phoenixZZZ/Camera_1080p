package com.jiuan.it.ipc.http.response;


import com.google.gson.annotations.SerializedName;

public class TypeCodeDetail  {

    @SerializedName("FilePath")
    private String filePath; //照片的路径

    @SerializedName("SeqNum")
    private int seqNum;  //顺序号


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }
}

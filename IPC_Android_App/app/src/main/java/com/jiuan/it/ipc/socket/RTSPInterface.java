package com.jiuan.it.ipc.socket;


public interface RTSPInterface {
    /**
     * 发送Socket信息
     * @param header
     * @param type
     */
    boolean sendData(String header,int type);
}

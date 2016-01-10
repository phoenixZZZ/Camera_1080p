package com.jiuan.it.ipc.socket;

import android.content.Context;
import android.os.RemoteException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IBackService;
import com.jiuan.it.ipc.http.response.FileList;
import com.jiuan.it.ipc.model.AlarmCtrlSend;
import com.jiuan.it.ipc.model.ByeSend;
import com.jiuan.it.ipc.model.GetControlSend;
import com.jiuan.it.ipc.model.GetVersionSend;
import com.jiuan.it.ipc.model.InviteSend;
import com.jiuan.it.ipc.model.LEDCtrlSend;
import com.jiuan.it.ipc.model.PTZAngleQuerySend;
import com.jiuan.it.ipc.model.PTZCommandSend;
import com.jiuan.it.ipc.model.PTZQuerySend;
import com.jiuan.it.ipc.model.PTZSetSend;
import com.jiuan.it.ipc.model.PanoramicCtrlSend;
import com.jiuan.it.ipc.model.PreviewCtrlSend;
import com.jiuan.it.ipc.model.RecodeStatusSend;
import com.jiuan.it.ipc.model.RecorderSend;
import com.jiuan.it.ipc.model.SendModel;
import com.jiuan.it.ipc.model.StreamCtrlSend;
import com.jiuan.it.ipc.model.UpdateVersionSend;
import com.jiuan.it.ipc.tools.NetManager;
import com.jiuan.it.ipc.tools.Tools;
import java.util.Date;

public class SocketSend implements RTSPInterface {

    private Context mContext;

    private IBackService mIBackService;

    private int clientPort = 1001;

    private int channelId = 1;

    private int mark = 1;

    private String id = "1.0";

    private int commandStep = 0;  //设备转动步数参数

    private int pictureCount = 8;   //客户端请求的图片数量

    private int commandAngle = 30;  //设备转动角度

    public  int ctrlCount = 10;  //所有录像列表数

    public  String ctrlBase =""; //操作类型

    public  String ctrlName ="";  //目录名称

    public  String ctrlNameStart ="" ; //起始帧对应的录像文件名称

    public  String ctrlNameEnd =""; //结束帧对应的录像文件名称

    public String  last;  //固件上传时间

    public   FileList fileList ;  //固件文件列表

    // 正常响应
    private static final String OK = "200";
    //设备SD卡剩余容量过低
    private static final String LOW_SDCARD = "248";
    //设备上没有SD卡
    private static final String NO_SDCARD = "249";
    //处于录制中时接收到开始录制命令
    private static final String WRONG_LOGIC = "250";
    //错误的请求
    private static final String BAD_REQUEST = "400";
    //发送的请求，未被服务端接受
    private static final String NOT_ACCEPTABLE = "406";
    //当前请求的目标对象忙
    private static final String BUSY_HERE = "486";
    //当前请求被挂起，暂时未执行
    private static final String REQUEST_PENDING = "491";
    //当前请求类型，服务端不识别
    private static final String UNDECIPHERABLE = "493";
    //服务端内部发生错误
    private static final String INTERNAL_SERVER_ERROR = "500";
    //当前处理请求超时（默认超时时间60s）
    private static final String SERVER_TIME_OUT = "504";

    public SocketSend(Context context,IBackService ibackService){
        this.mContext=context;
        this.mIBackService=ibackService;

    }

    /**
     * 发送信息
     * @param header
     * @param type
     */
    @Override
    public boolean sendData(String header,int type) {
        boolean isSend = false;
        String  content = "";
        switch (header) {
            case Config.INVITE:
                InviteSend invite = new InviteSend();
                setBase(invite, Config.INVITE);
                invite.setType(type);
                invite.setPort(0);
                invite.setIsEncryptConn(0);
                content =  new Gson().toJson(invite) ;
                break;
            case Config.BYE:
                ByeSend bye = new ByeSend();
                setBase(bye,Config.BYE);
                bye.setType(type);
                content =  new Gson().toJson(bye) ;
                break;
            case Config.RECORDERSTART:
                RecorderSend recorderStart = new RecorderSend();
                recorderStart.setTimeStamp(new Date().getTime()/1000); //当前时间毫秒级
                setBase(recorderStart, Config.RECORDERSTART);
                content =  new Gson().toJson(recorderStart) ;
                break;
            case Config.RECORDERSTOP:
                RecorderSend recorderStop = new RecorderSend();
                setBase(recorderStop,Config.RECORDERSTOP);
                content =  new Gson().toJson(recorderStop) ;
                break;
            case Config.STREAMCTRL:
                StreamCtrlSend stream = new StreamCtrlSend();
                setBase(stream,Config.STREAMCTRL);
                stream.setStreamType(type);
                content =  new Gson().toJson(stream) ;
                break;
            case Config.LEDCTRL:
                LEDCtrlSend led = new LEDCtrlSend();
                setBase(led,Config.LEDCTRL);
                led.setLedType(type);
                content =  new Gson().toJson(led) ;
                break;
            case Config.ALARMCTRL:
                AlarmCtrlSend alarm = new AlarmCtrlSend();
                setBase(alarm,Config.ALARMCTRL);
                alarm.setAlarmType(type);
                content =  new Gson().toJson(alarm) ;
                break;
            case Config.PTZCOMMAND:
                PTZCommandSend ptz = new PTZCommandSend();
                setBase(ptz, Config.PTZCOMMAND);
                ptz.setCommandType(type);
                ptz.setCommandStep(commandStep);
                ptz.setCommandAngle(commandAngle);
                content =  new Gson().toJson(ptz) ;
                break;
            case Config.REQUESTALLPICTURE:
                PanoramicCtrlSend panoramic = new PanoramicCtrlSend();
                setBase(panoramic, Config.REQUESTALLPICTURE);
                panoramic.setPictureCount(pictureCount);
                content =  new Gson().toJson(panoramic) ;
                break;
            case Config.PREVIEWCTRL:
                PreviewCtrlSend preview = new PreviewCtrlSend();
                setBase(preview,Config.PREVIEWCTRL);
                preview.setCtrlType(type);
                preview.setCount(ctrlCount);
                preview.setFtp(new String[]{Tools.getWifiIp(mContext), String.valueOf(Config.FTP_PORT)});
                if(ctrlBase!=null){
                    preview.setCtrlBase(ctrlBase);
                }
                if(ctrlName!=null){
                    preview.setCtrlName(ctrlName);
                }
                if(ctrlNameStart!=null){
                    preview.setCtrlNameStart(ctrlNameStart);
                }
                if(ctrlNameEnd!=null){
                    preview.setCtrlNameEnd(ctrlNameEnd);
                }
                content =  new Gson().toJson(preview) ;
                break;
            case Config.GETIPCVERSION:
                GetVersionSend getVersion = new GetVersionSend();
                setBase(getVersion,Config.GETIPCVERSION);
                content =  new Gson().toJson(getVersion) ;
                break;
            case Config.UPDATEIPCVERSION:
                UpdateVersionSend updateVersion = new UpdateVersionSend();
                setBase(updateVersion, Config.UPDATEIPCVERSION);
                updateVersion.setCurVersion(last);
                updateVersion.setUpdateFileName(fileList.getName());
                updateVersion.setUpdateVersion(fileList.getVer());
                updateVersion.setUpdateFileUrl(fileList.getUrl());
                updateVersion.setCrc(fileList.getCRC());
                updateVersion.setLength(Integer.valueOf(String.valueOf(fileList.getLength())));
                content =  new Gson().toJson(updateVersion) ;
                break;
            case Config.GETIPCISCONTROL:
                GetControlSend control = new GetControlSend();
                setBase(control, Config.GETIPCISCONTROL);
                content =  new Gson().toJson(control) ;
                break;
            case Config.IPCRECODERSTATUE:
                RecodeStatusSend recodeStatus = new RecodeStatusSend();
                setBase(recodeStatus,Config.IPCRECODERSTATUE);
                content =  new Gson().toJson(recodeStatus) ;
                break;
            case Config.IPCPTZANGLEQUERY:
                PTZAngleQuerySend angleQuerySend = new PTZAngleQuerySend();
                setBase(angleQuerySend,Config.IPCPTZANGLEQUERY);
                content =  new Gson().toJson(angleQuerySend) ;
                break;
            case Config.IPCPTZQUERY:
                PTZQuerySend querySend = new PTZQuerySend();
                setBase(querySend,Config.IPCPTZQUERY);
                content =  new Gson().toJson(querySend) ;
                break;
            case Config.IPCPTZANGLESET:
                PTZSetSend setSend = new PTZSetSend();
                setBase(setSend,Config.IPCPTZANGLESET);
                setSend.setAngleSet(type);
                content =  new Gson().toJson(setSend) ;
                break;
        }
        try {
            isSend = mIBackService.sendMessage(content);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return isSend;
    }

    /**
     * 公共参数设置
     * @param socket
     * @param header
     */
    private void setBase(SendModel socket,String header){
        socket.setHeader(header);
        socket.setIdcp(id);
        socket.setMark(mark);
        socket.setChannel(channelId);
        socket.setTarget(new String[]{Config.SOCKET_SERVER, String.valueOf(Config.SOCKET_PORT)});
        socket.setServer(new String[]{Config.SOCKET_SERVER, String.valueOf(Config.SOCKET_PORT)});
        socket.setClient(new String[]{NetManager.getWifiIp(mContext), String.valueOf(clientPort)});
    }



    /**
     * 把Json格式的字符串转换成实体类型的方法.
     * 注意:实体类中的变量需要用@SerializedName注释
     */
    public static final <T> T get(String value, Class<T> classOfT) {
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            return gson.fromJson(value, classOfT);
        } catch (JsonSyntaxException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    public  static  String showError(String error){
        String name = "";
        switch (error){
            case LOW_SDCARD:
             name ="设备SD卡剩余容量过低";
             break;
            case NO_SDCARD:
                name ="设备上没有SD卡";
                break;
            case WRONG_LOGIC:
                name ="处于录制中时接收到开始录制命令";
                break;
            case BAD_REQUEST:
                name ="错误的请求";
                break;
            case NOT_ACCEPTABLE:
                name ="发送的请求，未被服务端接受";
                break;
            case BUSY_HERE:
                name ="当前请求的目标对象忙";
                break;
            case REQUEST_PENDING:
                name ="当前请求被挂起，暂时未执行";
                break;
            case UNDECIPHERABLE:
                name ="当前请求类型，服务端不识别";
                break;
            case INTERNAL_SERVER_ERROR:
                name ="服务端内部发生错误";
                break;
            case SERVER_TIME_OUT:
                name ="当前处理请求超时";
                break;
        }
        return name;
    }


}

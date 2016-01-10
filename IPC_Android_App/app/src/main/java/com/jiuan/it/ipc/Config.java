package com.jiuan.it.ipc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jiuan.it.ipc.model.GlobalBean;
import com.jiuan.it.ipc.model.LoginInfo;
import com.jiuan.it.ipc.service.TimeService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Config {

    public static float XROT = 0f;  //翻转角度

    /**FIR地址*/
    public final  static String FIR_TOKEN = "3f90c95ce9a5ca1f2d99eb49126a51ac";

    /**微信分享*/
    public final static String AppID ="wxfb9be2e875ae6b0f" ;

    public final static String AppSecret ="903f98cf154e9f4423a3727b8886e5ff" ;

    /**摄像头WIFI密码、加密方式*/

    public final static String white_Password ="12345678" ;

    public final static int  white_Type =3 ; //热点类型1-无密码 / 2-WEP密码验证（未测试）/ 3-WAP或WAP2 PSK密码验证
     /**服务器地址类型*/

    public static final int URL_DEV = 1;

    public static final int URL_TEST = 2;

    public static final int URL_OFFICIAL = 3;

    public static final String SECURITY_CODE = "-6Qg5TyljKlKD~qLu,Y22!UR++4Kep+$";

     /**RTSP操作指令*/

    //视频查看及音频收听、发送（对讲）
    public static  final  String INVITE = "INVITE";

    //关闭音频接收线程
    public static final  String  BYE = "BYE";

    //控制设备录制-开启
    public static final  String  RECORDERSTART = "RECORDERSTART";

    //控制设备录制-停止
    public static final  String  RECORDERSTOP = "RECORDERSTOP";

    //音频格式选择
    public static final  String  AUDIOFORMAT  = "AUDIOFORMAT";

    //控制视频质量
    public static final  String  STREAMCTRL  = "STREAMCTRL";

    //控制摄像头转动
    public static final  String  PTZCOMMAND = "PTZCOMMAND";

    //控制LED灯
    public static final  String  LEDCTRL = "LEDCTRL";

    //控制报警开关
    public static final  String  ALARMCTRL = "ALARMCTRL";

    //全景图片功能
    public static final  String  REQUESTALLPICTURE = "REQUESTALLPICTURE";

    //快速预览功能
    public static final  String  PREVIEWCTRL = "PREVIEWCTRL";

    //摄像头的固件版本
    public static final  String  GETIPCVERSION = "GETIPCVERSION";

    //下载地址发送给远端设备
    public static final  String  UPDATEIPCVERSION = "UPDATEIPCVERSION";

    //远端录像的状态
    public static final  String  IPCRECODERSTATUE = "IPCRECODERSTATUE";

    //锁定控制（查询）
    public static final  String  GETIPCISCONTROL = "GETIPCISCONTROL";

    //锁定控制（通知）
    public static final  String  NOTICECONTROL = "NOTICECONTROL";

    //录像停止通知消息
    public static final  String  NOTICERECORDSTOP = "NOTICERECORDSTOP";

    //客户端数量达到上限通知消息
    public static final  String  NOTICECLIENTMAX = "NOTICECLIENTMAX";

    //控制摄像头转动(360度云台)
    public static final  String  IPCPTZANGLESET = "IPCPTZANGLESET";

    //控制摄像头转动紧急停止(360度云台)
    public static final  String  IPCPTZSTOP = "IPCPTZSTOP";

    //控制摄像头回到零点位置(360度云台)
    public static final  String  IPCPTZZERO = "IPCPTZZERO";

    //查询当前云台位置角度值(360度云台)
    public static final  String  IPCPTZANGLEQUERY = "IPCPTZANGLEQUERY";

    //查询有无云台(360度云台)
    public static final  String  IPCPTZQUERY = "IPCPTZQUERY";

    /**文件夹命名*/
    public final static String IMAGE ="Image" ;//图片文件夹

    public final static String VIDEO ="Video" ;//视频文件夹

    public final static String PANORAMA ="Panorama" ;  //全景文件夹

    public final static String CACHE ="Cache" ;  //缓存文件夹


    //Socket
    public static  String SOCKET_SERVER = "192.168.100.100";

    public static  String  DEVICE_CODE =  null;

    public static  String  FINFO =  null;

    public static  int  SOCKET_PORT = 1666;

    public final static int SOCKET_READ_TIME_OUT = 60 * 1000;//// 默认timeout 时间 60s

    public final static int SOCKET_HEART_SECOND =2 * 1000  ;  //心跳包发送间隔时间

    public static  int  UDP_PORT = 1667;

    public static  int  FTP_PORT = 1668;


    //goolink 账号
    public  static String mUserName ="admin";

    public  static String mPassword ="admin";


    public static  int number ;

    /*设备信息*/

    /**
     * 服务器访问信息保存地址
     */
    public static final String PREF_GLOBAL = "pref_global";

    /**
     * 登录信息保存地址
     */
    public static final String PREF_LOGIN = "pref_login";

    /**
     * 登录信息键
     */
    public static final String LOGIN_KEY = "login_key";

    /**
     * 服务器访问信息键
     */
    public static final String GLOBAL_KEY = "global_key";


    /**
     * 设置登录信息
     */
    public static void setLoginInfo(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(Config.PREF_LOGIN, Context.MODE_PRIVATE);
        preferences.edit().putString(Config.LOGIN_KEY, value).apply();
    }

    /**
     * 获得登录信息
     */
    public static LoginInfo getLoginInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Config.PREF_LOGIN, Context.MODE_PRIVATE);
        String value = preferences.getString(Config.LOGIN_KEY, "");
        if (!TextUtils.isEmpty(value)) {
            Gson gson = new Gson();
            return gson.fromJson(value, LoginInfo.class);
        }
        return null;
    }

    /**
     * 设置访问信息
     */
    public static void setGlobal(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(Config.PREF_GLOBAL, Context.MODE_PRIVATE);
        preferences.edit().putString(Config.GLOBAL_KEY, value).apply();
    }

    /**
     * 获得访问信息
     */
    public static GlobalBean getGlobal(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Config.PREF_GLOBAL, Context.MODE_PRIVATE);
        String value = preferences.getString(Config.GLOBAL_KEY, "");
        if (!TextUtils.isEmpty(value)) {
            Gson gson = new Gson();
            return gson.fromJson(value, GlobalBean.class);
        }
        return null;
    }
    /**
     * 清空登陆信息
     */
    public static void cleanLogin(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Config.PREF_LOGIN, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        cleanGlobal(context);
    }
    /**
     * 清空访问信息
     */
    public static void cleanGlobal(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Config.PREF_GLOBAL, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

    }
    /**
     *  SOCKET连接失败
     */
    public static final String ACTION_SOCKET_FAILURE  = "com.jiuan.it.ipc.intent.action.ACTION_SOCKET_FAILURE";

    /**
     *  SOCKET连接成功
     */
    public static final String ACTION_SOCKET_SUCCESS  = "com.jiuan.it.ipc.intent.action.ACTION_SOCKET_SUCCESS";

    /**
     *  设备直连失败
     */
    public static final String ACTION_RTSP_FAILURE  = "com.jiuan.it.ipc.intent.action.ACTION_RTSP_FAILURE";

    /**
     *  设备离线
     */
    public static final String ACTION_DEVICE_FAILURE  = "com.jiuan.it.ipc.intent.action.ACTION_DEVICE_FAILURE";

    /**
     *  设备直连信息接收
     */
    public static final String ACTION_RTSP_RECEIVE = "com.jiuan.it.ipc.intent.action.ACTION_RTSP_RECEIVE";

    /**
     * 广播信息
     */
    public static final String ACTION_EXTERNAL_MESSAGE = "com.jiuan.it.ipc.intent.action.ACTION_EXTERNAL_MESSAGE";

    /**
     * 心跳包信息
     * */
    public static final String HEART_BEAT_ACTION="com.jiuan.it.ipc.intent.action.HEART_BEAT_ACTION";

    /**
     * 开启连接
     * */
    public static final String START_CONNECT="com.jiuan.it.ipc.intent.action.START_CONNECT";

    /**
     * 停止连接
     * */
    public static final String STOP_CONNECT="com.jiuan.it.ipc.intent.action.STOP_CONNECT";

    /**
     * 开启搜索
     * */
    public static final String START_SEARCH="com.jiuan.it.ipc.intent.action.START_SEARCH";

}

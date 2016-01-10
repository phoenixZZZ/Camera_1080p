package com.jiuan.it.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IBackService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;


public class BackService extends Service  {

    private static final String TAG = "BackService";

    private static final long HEART_BEAT_RATE = 30 * 1000;

    private static final long HEART_BEAT_RATE_REV = 40 * 1000;

    private ReadThread mReadThread;

    private LocalBroadcastManager mLocalBroadcastManager;

    private WeakReference<Socket> mSocket;

    private boolean heart = false;

    private int sleep = 3000;
    // For heart Beat
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            Log.d("打印当前线程",""+Thread.currentThread());
            sendMsg("keepalive");//就发送一个\r\n过去 如果发送失败，就重新初始化一个socket
            if(heart){
                mHandler.removeCallbacks(heartBeatRunnable); // 停止
            }else {
                mHandler.postDelayed(this, HEART_BEAT_RATE); // 开始
            }

        }
    };


    //private long sendTime = 0L;
    private long revTime = 0L;
    private IBackService.Stub iBackService = new IBackService.Stub() {

        public boolean sendMessage(String message) throws RemoteException {
            return sendMsg(message);
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return iBackService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new InitSocketThread().start();
        mLocalBroadcastManager= LocalBroadcastManager.getInstance(this);

    }
    public boolean sendMsg(String msg) {
        if (null == mSocket || null == mSocket.get()) {
            return false;
        }
        Socket soc = mSocket.get();
        OutputStream os = null;
        try {
            if (!soc.isClosed() && !soc.isOutputShutdown()) {
                os = soc.getOutputStream();
                String message = msg + "\r\n";
                os.write(message.getBytes());
                os.flush();
            }
            Log.e(TAG, "发送：" + msg);
        } catch (SocketException e) {
           /* Intent intent=new Intent(Config.ACTION_DEVICE_FAILURE);
            intent.putExtra("TYPE","SocketException"+e.toString());
            mLocalBroadcastManager.sendBroadcast(intent);*/
            return false;
        } catch (IOException e) {
            Intent intent=new Intent(Config.ACTION_DEVICE_FAILURE);
            intent.putExtra("TYPE","IOException"+e.toString());
            mLocalBroadcastManager.sendBroadcast(intent);
            return false;
        }catch (Exception e) {
            Intent intent=new Intent(Config.ACTION_DEVICE_FAILURE);
            intent.putExtra("TYPE","Exception"+e.toString());
            mLocalBroadcastManager.sendBroadcast(intent);
        }
        return true;
    }

    private void initSocket() {//初始化Socket
        try {
            Socket socket = new Socket();
            SocketAddress socAddress = new InetSocketAddress(Config.SOCKET_SERVER, Config.SOCKET_PORT);
            socket.connect(socAddress, sleep);
            if(socket.isConnected()){
                Intent intent=new Intent(Config.ACTION_SOCKET_SUCCESS);
                mLocalBroadcastManager.sendBroadcast(intent);
            }
            mSocket = new WeakReference<Socket>(socket);
            mReadThread = new ReadThread(socket);
            mReadThread.start();
            mHandler.postDelayed(heartBeatRunnable, 0);//初始化成功后，就准备发送心跳包
        }catch (ConnectException se){ //连接异常
            Intent intent=new Intent(Config.ACTION_SOCKET_FAILURE);
            mLocalBroadcastManager.sendBroadcast(intent);
        }catch (SocketTimeoutException e) { //连接超时
            Intent intent=new Intent(Config.ACTION_SOCKET_FAILURE);
            mLocalBroadcastManager.sendBroadcast(intent);
        }catch (IOException e) {
            Intent intent=new Intent(Config.ACTION_SOCKET_FAILURE);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (sk!=null&&!sk.isClosed()) {
                    sk.close();
                }
                sk = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }

    // Thread to read content from Socket
    class ReadThread extends Thread {
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mWeakSocket = new WeakReference<Socket>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown()
                            && isStart && ((length = is.read(buffer)) != -1)) {
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer,
                                    length)).trim();
                            Log.e(TAG, "接收："+message);
                            //收到服务器过来的消息，就通过Broadcast发送出去
                            if(message.equals("keepalive")){ //连接正常
                                if(revTime==0){
                                    revTime= System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
                                }
                                if (System.currentTimeMillis() - revTime > HEART_BEAT_RATE_REV) {
                                    heart = true;
                                    Intent intent=new Intent(Config.ACTION_DEVICE_FAILURE);
                                    intent.putExtra("TYPE","HeartClosed");
                                    mLocalBroadcastManager.sendBroadcast(intent);
                                }else {
                                    revTime= System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
                                }
                            }else {
                                //其他消息回复
                                Intent intent=new Intent(Config.ACTION_RTSP_RECEIVE);
                                intent.putExtra(Config.ACTION_EXTERNAL_MESSAGE, message);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        // MediaPlayer对象的stop()方法
        heart = true;
        if (null != mSocket && null != mSocket.get()) {
            Socket soc = mSocket.get();
            try {
                soc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onUnbind(intent);
    }

}

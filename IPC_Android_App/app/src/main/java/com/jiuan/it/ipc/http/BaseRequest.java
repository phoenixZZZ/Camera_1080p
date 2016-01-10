package com.jiuan.it.ipc.http;
import com.innovation.android.library.http.InnovationRequestImpl;
import com.jiuan.it.ipc.Config;

public abstract class BaseRequest extends InnovationRequestImpl {

    public static final String PATH;

    static {
        switch (com.jiuan.it.ipc.Flavors.URL_TYPE) {
            case Config.URL_DEV:
                PATH = "http://intellihousedev.chinacloudapp.cn/";
                break;
            case Config.URL_TEST:
                PATH = "";
                break;
            case Config.URL_OFFICIAL:
                PATH = "";
                break;
            default:
                PATH = "";
                break;
        }
    }

}

package com.innovation.android.library.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Locale;

public class HeadInfo {

    public static final String AGENT = "Agent";

    public static final String AGENTID = "AgentID";

    public static final String HW = "Hw";

    public static final String HWVER = "HwVer";

    public static final String FW = "Fw";

    public static final String FWVER = "FwVer";

    public static final String OS = "Os";

    public static final String OSVER = "OsVer";

    public static final String APP = "App";

    public static final String APPVER = "AppVer";

    public static final String APPID = "AppID";

    public static final String LANGUAGE = "Language";

    public static final String LOCALE = "Locale";

    public static final String ACCOUNT = "Account";

    public static final String TOKEN = "Token";

    private String agent;// 设备名称

    private String agentID;// 设备ID

    private String hw;// 硬件名称

    private String hwVer;// 硬件版本

    private String fw;// 固件名称

    private String fwVer;// 固件版本

    private String os;// Android

    private String osVer;// 系统版本

    private String app;// 包名

    private String appVer;// 应用版本

    private String appID;// 应用唯一ID

    private String language;// 语言

    private String locale;// 国家

    private String account;// 帐号

    private String token;// 推送

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getHw() {
        return hw;
    }

    public void setHw(String hw) {
        this.hw = hw;
    }

    public String getHwVer() {
        return hwVer;
    }

    public void setHwVer(String hwVer) {
        this.hwVer = hwVer;
    }

    public String getFw() {
        return fw;
    }

    public void setFw(String fw) {
        this.fw = fw;
    }

    public String getFwVer() {
        return fwVer;
    }

    public void setFwVer(String fwVer) {
        this.fwVer = fwVer;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVer() {
        return osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static final class Builder {

        private String agent;

        private String agentID;

        private String hw;

        private String hwVer;

        private String fw;

        private String fwVer;

        private String os;

        private String osVer;

        private String app;

        private String appVer;

        private String appID;

        private String language;

        private String locale;

        private String account;

        private String token;

        public Builder(Context context) {

            agent = Build.MODEL;
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            agentID = telephonyManager.getDeviceId();
            hw = Build.HARDWARE;
            hwVer = Build.SERIAL;
            fw = Build.DISPLAY;
            fwVer = Build.ID;
            os = Build.VERSION.RELEASE;
            osVer = "" + Build.VERSION.SDK_INT;
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                app = packageInfo.packageName;
                appVer = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Locale defaultLocale = Locale.getDefault();
            language = defaultLocale.getLanguage();
            locale = defaultLocale.getCountry();
        }

        public HeadInfo build() {
            HeadInfo headInfo = new HeadInfo();
            headInfo.setAgent(agent);
            headInfo.setAgentID(agentID);
            headInfo.setHw(hw);
            headInfo.setHwVer(hwVer);
            headInfo.setFw(fw);
            headInfo.setFwVer(fwVer);
            headInfo.setOs(os);
            headInfo.setOsVer(osVer);
            headInfo.setApp(app);
            headInfo.setAppVer(appVer);
            headInfo.setAppID(appID);
            headInfo.setLanguage(language);
            headInfo.setLocale(locale);
            headInfo.setAccount(account);
            headInfo.setToken(token);
            return headInfo;
        }

        public Builder os(String os){
            this.os = os;
            return this;
        }

        public Builder app(String app) {
            this.app = app;
            return this;
        }

        public Builder appID(String appID) {
            this.appID = appID;
            return this;
        }

        public Builder account(String account) {
            this.account = account;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

    }
}

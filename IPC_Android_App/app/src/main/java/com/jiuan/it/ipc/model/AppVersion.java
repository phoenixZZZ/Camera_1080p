package com.jiuan.it.ipc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 2015/12/28.
 */
public class AppVersion {

    @SerializedName("name")
    private String name;

    @SerializedName("version")
    private String version;

    @SerializedName("changeLog")
    private String changeLog;

    @SerializedName("updated_at")
    private String updated_at;

    @SerializedName("versionShort")
    private String versionShort;

    @SerializedName("installUrl")
    private String installUrl;

    @SerializedName("update_url")
    private String update_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getVersionShort() {
        return versionShort;
    }

    public void setVersionShort(String versionShort) {
        this.versionShort = versionShort;
    }

    public String getInstallUrl() {
        return installUrl;
    }

    public void setInstallUrl(String installUrl) {
        this.installUrl = installUrl;
    }

    public String getUpdate_url() {
        return update_url;
    }

    public void setUpdate_url(String update_url) {
        this.update_url = update_url;
    }
}

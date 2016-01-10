package com.jiuan.it.ipc.model;

import java.util.Map;

/**
 * Created by admin on 2015/11/26.
 */
public class VideoDownload {

    private String name; //文件名称
    private Map<String,Boolean> exists; //文件存在标记

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getExists() {
        return exists;
    }

    public void setExists(Map<String, Boolean> exists) {
        this.exists = exists;
    }
}

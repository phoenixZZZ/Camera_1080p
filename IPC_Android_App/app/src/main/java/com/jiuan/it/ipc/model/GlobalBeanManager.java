package com.jiuan.it.ipc.model;

import android.content.Context;

import com.jiuan.it.ipc.common.db.ZnkDBHelper;

public class GlobalBeanManager {
    // GlobalBean
    //private static GlobalBean globalBean;

    private static ZnkDBHelper dbHelper;




    public static ZnkDBHelper getDbHelper(Context context) {

        if (dbHelper == null) {
            dbHelper = new ZnkDBHelper(context);
        }

        return dbHelper;
    }

}
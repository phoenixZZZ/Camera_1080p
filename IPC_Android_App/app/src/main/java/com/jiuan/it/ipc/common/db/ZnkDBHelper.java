package com.jiuan.it.ipc.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ZnkDBHelper extends SQLiteOpenHelper {

    //数据库名称
    private static final String name = "znkDBCacheTest01.db";
    //数据库版本
    private static final int version = 1;

    public ZnkDBHelper(Context context) {

        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.e("ZnkDBHelper", "onCreate");

        db.execSQL("CREATE TABLE IF NOT EXISTS m_user (UserID varchar(30) primary key, Hguid varchar(30), Phone varchar(11), Logo varchar(200), UserName varchar(50), CreateTime varchar(60), UpdateTime varchar(60))");

        db.execSQL("CREATE TABLE IF NOT EXISTS m_device (EqID varchar(30) primary key, Hguid varchar(30), IsOwner INTEGER(1), EqType INTEGER(1), EquipName varchar(30), EquipAlias varchar(30), EquipAvatar varchar(200), CreateTime varchar(60), UpdateTime varchar(60))");

        db.execSQL("CREATE TABLE IF NOT EXISTS m_device_circle (CircleID varchar(30) primary key, Hguid varchar(30), Name varchar(30), Avatar varchar(200), IsCreator char(1), CreateTime varchar(60), UpdateTime varchar(60))");

        db.execSQL("CREATE TABLE IF NOT EXISTS m_friend (FID varchar(30) primary key, Hguid varchar(30), Phone varchar(11), Avatar varchar(200), Name varchar(30), CreateTime varchar(60), UpdateTime varchar(60))");

        db.execSQL("CREATE TABLE IF NOT EXISTS t_device_circle_detail (CircleID varchar(30), MemberID varchar(30), ClassifyCode char(1), CreateTime varchar(60), UpdateTime varchar(60), primary key (CircleID, MemberID))");

        db.execSQL("CREATE TABLE IF NOT EXISTS t_device_message (MessageID varchar(30) primary key, UserID varchar(30) NOT NULL, Direction INTEGER(1), Text varchar(200), ToMessageID varchar(30), Time varchar(60))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.e("ZnkDBHelper", "onUpgrade");

    }

}

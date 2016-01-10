package com.jiuan.it.ipc.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import com.jiuan.it.ipc.model.GlobalBeanManager;

import java.util.List;

/**
 * Created by SunYongqiang on 2015/8/30.
 */
public abstract class DBUpdateUtil<T> {

    public static final int RESULT_NORMAL = 0;
    public static final int RESULT_ERROR_TABLE_REQUIRED = 1;
    public static final int RESULT_ERROR_TRANSACTION = -1;

    private List<T> insertList = null;

    private List<T> updateList = null;

    private List<T> deleteList = null;

    private String tableName = null;

    private ZnkDBHelper dbHelper;

    private Context context;

    public DBUpdateUtil(Context context, String tableName) {
        this.context = context;
        this.tableName = tableName;
    }

    public DBUpdateUtil(Context context) {
        this(context, "");
    }

    public List<T> getInsertList() {
        return insertList;
    }

    public void setInsertList(List<T> insertList) {
        this.insertList = insertList;
    }

    public List<T> getUpdateList() {
        return updateList;
    }

    public void setUpdateList(List<T> updateList) {
        this.updateList = updateList;
    }

    public List<T> getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(List<T> deleteList) {
        this.deleteList = deleteList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    protected abstract ContentValues insertData(T item);

    protected abstract UpdateSupport updateData(T item);

    protected abstract DeleteWhere inputDeleteWhere(T item);

    public int updateToDB() {

        int result = RESULT_NORMAL;

        if (tableName == null || tableName.length() == 0) {
            result = RESULT_ERROR_TABLE_REQUIRED;
        }

        dbHelper = GlobalBeanManager.getDbHelper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            // 插入数据
            if (insertList != null && insertList.size() > 0) {

                ContentValues insertValues = null;

                for (T item : insertList) {

                    insertValues = insertData(item);

                    db.insert(tableName, null, insertValues);
                }
            }

            // 更新数据
            if (updateList != null && updateList.size() > 0) {

                UpdateSupport updateSupport = null;

                for (T item : updateList) {

                    updateSupport = updateData(item);

                    db.update(tableName, updateSupport.getUpdateData(),
                            updateSupport.getUpdateCondition(), updateSupport.getUpdateArgs());
                }
            }

            // 删除数据
            if (deleteList != null && deleteList.size() > 0) {

                DeleteWhere dw = null;

                for (T item : deleteList) {

                    dw = inputDeleteWhere(item);

                    if (dw != null) {
                        db.delete(tableName, dw.getWhereCondition(), dw.getWhereArgs());
                    }
                }
            }

            db.setTransactionSuccessful();

        } catch (Exception e) {
            result = RESULT_ERROR_TRANSACTION;
        } finally {
            db.endTransaction();
            db.close();
        }

        return result;
    }

    public class UpdateSupport {

        private ContentValues updateData;

        private String updateCondition;

        private String[] updateArgs;

        public ContentValues getUpdateData() {
            return updateData;
        }

        public void setUpdateData(ContentValues updateData) {
            this.updateData = updateData;
        }

        public String getUpdateCondition() {
            return updateCondition;
        }

        public void setUpdateCondition(String updateCondition) {
            this.updateCondition = updateCondition;
        }

        public String[] getUpdateArgs() {
            return updateArgs;
        }

        public void setUpdateArgs(String[] updateArgs) {
            this.updateArgs = updateArgs;
        }
    }

    public class DeleteWhere {
        private String whereCondition;

        private String[] whereArgs;

        public String getWhereCondition() {
            return whereCondition;
        }

        public void setWhereCondition(String whereCondition) {
            this.whereCondition = whereCondition;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }

        public void setWhereArgs(String[] whereArgs) {
            this.whereArgs = whereArgs;
        }
    }
}

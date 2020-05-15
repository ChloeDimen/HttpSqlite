package com.dimen.httpsqlite.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * 文件名：com.dimen.customsqlite.db
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/16
 */
public class BaseDaoFactory {

    //数据库的路径
    private String sqliteDatabasePath;


    private SQLiteDatabase mSQLiteDatabase;

    public static BaseDaoFactory mBaseDaoFactory = new BaseDaoFactory();

    public BaseDaoFactory() {
        sqliteDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "user.db";
        openDatabase();


    }

    public synchronized <T extends BaseDao<M>, M> T getDataHelper(Class<T> clazz, Class<M> entityClazz) {

        BaseDao baseDao = null;
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entityClazz, mSQLiteDatabase);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        //强转的T是BaseDao，赋值的是IBaseDao
        return (T) baseDao;
    }

    private void openDatabase() {
        this.mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath, null);

    }

    public static BaseDaoFactory getInstane() {
        return mBaseDaoFactory;

    }

}

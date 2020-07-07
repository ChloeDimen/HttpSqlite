package com.dimen.httpsqlite.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, BaseDao> map = Collections.synchronizedMap(new HashMap<String, BaseDao>());

    private BaseDaoFactory() {
      //  sqliteDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ "user.db";
        File file=new File(Environment.getExternalStorageDirectory(),"update");
        if (!file.exists()) {
            file.mkdirs();
        }
        sqliteDatabasePath = file.getAbsolutePath() + "/user.db";
        openDatabase();


    }

    public synchronized <T extends BaseDao<M>, M> T getDataHelper(Class<T> clazz, Class<M> entityClazz) {

        BaseDao baseDao = null;
        if (map.get(clazz.getSimpleName()) != null) {
            return (T) map.get(clazz.getSimpleName());
        }
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entityClazz, mSQLiteDatabase);
            map.put(clazz.getSimpleName(), baseDao);
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

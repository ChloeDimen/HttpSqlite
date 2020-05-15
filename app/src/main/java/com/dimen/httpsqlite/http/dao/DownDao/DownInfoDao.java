package com.dimen.httpsqlite.http.dao.DownDao;

import android.database.Cursor;

import com.dimen.httpsqlite.db.BaseDao;
import com.dimen.httpsqlite.db.annotion.DbTable;
import com.dimen.httpsqlite.http.download.DownloadItemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 文件名：com.dimen.httpsqlite.http.dao.DownDao
 * 描    述：给外界提供方法
 * 作    者：Dimen
 * 时    间：2020/5/15
 */

public class DownInfoDao extends BaseDao<DownloadItemInfo> {

    //保持线程安全，保存应该下载的集合，不包括已经下载成功的。
    private List<DownloadItemInfo> mDownloadItemInfoList = Collections.synchronizedList(new ArrayList<DownloadItemInfo>());
    private DownloadInfoComparator mDownloadInfoComparator = new DownloadInfoComparator();


    @Override
    protected String createTable() {
        return "create table if not exists  t_downloadInfo(" + "id Integer primary key, " + "url TEXT not null," + "filePath TEXT not null, " + "displayName TEXT, " + "status Integer, " + "totalLen Long, " + "currentLen Long," + "startTime TEXT," + "finishTime TEXT," + "userId TEXT, " + "httpTaskType TEXT," + "priority  Integer," + "stopMode Integer," + "downloadMaxSizeKey TEXT," + "unique(filePath))";
    }

    @Override
    public List query(String sql) {
        return null;
    }
    /**
     * 生成下载id
     *
     * @return 返回下载id
     */
    private Integer generateRecordId()
    {
        int maxId = 0;
        String sql = "select max(id)  from " +getTableName() ;
        synchronized (DownInfoDao.class)
        {
            Cursor cursor = this.database.rawQuery(sql,null);
            if(cursor.moveToNext())
            {
                String[] colmName=cursor.getColumnNames();

                int index=cursor.getColumnIndex("max(id)");
                if(index!=-1)
                {
                    Object value =cursor.getInt(index);
                    if (value != null)
                    {
                        maxId = Integer.parseInt(String.valueOf(value));
                    }
                }
            }

        }
        return maxId + 1;
    }
    /**
     * 比较器
     */
    class DownloadInfoComparator implements Comparator<DownloadItemInfo> {

        @Override
        public int compare(DownloadItemInfo o1, DownloadItemInfo o2) {
            return o1.getId() - o2.getId();
        }
    }
}

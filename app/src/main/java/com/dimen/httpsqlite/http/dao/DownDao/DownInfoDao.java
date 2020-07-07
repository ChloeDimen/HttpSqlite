package com.dimen.httpsqlite.http.dao.DownDao;

import android.database.Cursor;

import com.dimen.httpsqlite.db.BaseDao;
import com.dimen.httpsqlite.db.annotion.DbTable;
import com.dimen.httpsqlite.http.download.DownloadItemInfo;
import com.dimen.httpsqlite.http.enums.DownloadStatus;
import com.dimen.httpsqlite.http.enums.DownloadStopMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    private Integer generateRecordId() {
        int maxId = 0;
        String sql = "select max(id)  from " + getTableName();
        synchronized (DownInfoDao.class) {
            Cursor cursor = this.database.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                String[] colmName = cursor.getColumnNames();

                int index = cursor.getColumnIndex("max(id)");
                if (index != -1) {
                    Object value = cursor.getInt(index);
                    if (value != null) {
                        maxId = Integer.parseInt(String.valueOf(value));
                    }
                }
            }

        }
        return maxId + 1;
    }

    /**
     * 根据下载地址和下载文件路径查找下载记录
     *
     * @param url      下载地址
     * @param filePath 下载文件路径
     * @return
     */
    public DownloadItemInfo findRecord(String url, String filePath) {

        synchronized (DownInfoDao.class) {
            for (DownloadItemInfo record : mDownloadItemInfoList) {
                if (record.getUrl().equals(url) && record.getFilePath().equals(filePath)) {
                    return record;
                }
            }
            /**
             * 内存集合找不到
             * 就从数据库中查找
             */
            DownloadItemInfo where = new DownloadItemInfo();
            where.setUrl(url);
            where.setFilePath(filePath);
            List<DownloadItemInfo> resultList = super.query(where);
            if (resultList.size() > 0) {
                return resultList.get(0);
            }
            return null;
        }
    }

    /**
     * 根据 下载文件路径查找下载记录
     * <p>
     * 下载地址
     *
     * @param filePath 下载文件路径
     * @return
     */
    public List<DownloadItemInfo> findRecord(String filePath) {
        synchronized (DownInfoDao.class) {
            DownloadItemInfo where = new DownloadItemInfo();
            where.setFilePath(filePath);
            List<DownloadItemInfo> resultList = super.query(where);
            return resultList;
        }

    }

    /**
     * 根据id查找下载记录对象
     *
     * @param recordId
     * @return
     */
    public DownloadItemInfo findRecordById(int recordId) {
        synchronized (DownInfoDao.class) {
            for (DownloadItemInfo record : mDownloadItemInfoList) {
                if (record.getId() == recordId) {
                    return record;
                }
            }

            DownloadItemInfo where = new DownloadItemInfo();
            where.setId(recordId);
            List<DownloadItemInfo> resultList = super.query(where);
            if (resultList.size() > 0) {
                return resultList.get(0);
            }
            return null;
        }

    }

    /**
     * 根据id从内存中移除下载记录
     *
     * @param downId 下载id
     * @return true标示删除成功，否则false
     */
    public boolean removeRecordFromMemery(int downId) {
        synchronized (DownloadItemInfo.class) {
            for (int i = 0; i < mDownloadItemInfoList.size(); i++) {
                if (mDownloadItemInfoList.get(i).getId() == downId) {
                    mDownloadItemInfoList.remove(i);
                    break;
                }
            }
            return true;
        }
    }

    /**
     * 根据下载地址和下载文件路径查找下载记录
     * <p>
     * 下载地址
     *
     * @param filePath 下载文件路径
     * @return
     */
    public DownloadItemInfo findSigleRecord(String filePath) {
        List<DownloadItemInfo> downloadInfoList = findRecord(filePath);
        if (downloadInfoList.isEmpty()) {
            return null;
        }
        return downloadInfoList.get(0);
    }

    /**
     * 添加下载记录
     *
     * @param url         下载地址
     * @param filePath    下载文件路径
     * @param displayName 文件显示名
     * @param priority    小组优先级
     *                    TODO
     * @return 下载id
     */
    public DownloadItemInfo addRecrod(String url, String filePath, String displayName, int priority) {
        synchronized (DownInfoDao.class) {
            DownloadItemInfo existDownloadInfo = findRecord(url, filePath);
            if (existDownloadInfo == null) {
                DownloadItemInfo record = new DownloadItemInfo();
                record.setId(generateRecordId());
                record.setUrl(url);
                record.setFilePath(filePath);
                record.setDisplayName(displayName);
                record.setStatus(DownloadStatus.waitting.getValue());
                record.setTotalLen(0L);
                record.setCurrentLen(0L);
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                record.setStartTime(dateFormat.format(new Date()));
                record.setFinishTime("0");
                record.setPriority(priority);
                super.insert(record);
                mDownloadItemInfoList.add(record);
                return record;
            }
            return null;
        }
    }

    /**
     * 更新下载记录
     *
     * @param record 下载记录
     * @return
     */
    public int updateRecord(DownloadItemInfo record) {
        DownloadItemInfo where = new DownloadItemInfo();
        where.setId(record.getId());
        int result = 0;
        synchronized (DownInfoDao.class) {
            try {
                result = super.update(record, where);
            } catch (Throwable e) {
            }
            if (result > 0) {
                for (int i = 0; i < mDownloadItemInfoList.size(); i++) {
                    if (mDownloadItemInfoList.get(i).getId().intValue() == record.getId()) {
                        mDownloadItemInfoList.set(i, record);
                        break;
                    }
                }
            }
        }
        return result;
    }

    //根据下载优先级调度自动退出的下载记录
    public List<DownloadItemInfo> findAllAutoCancelRecords() {

        List<DownloadItemInfo> resultList = new ArrayList<>();
        synchronized (DownInfoDao.class) {
            DownloadItemInfo downloadItemInfo = null;
            for (int i = 0; i < mDownloadItemInfoList.size(); i++) {
                downloadItemInfo = mDownloadItemInfoList.get(i);
                if (downloadItemInfo.getStatus() != DownloadStatus.failed.getValue() /*&& DownloadStatus.finish.getValue()
                        && downloadItemInfo.getStopMode() == DownloadStopMode.auto.getValue()*/) {
                    resultList.add(downloadItemInfo);
                }
            }
        }
        if (!resultList.isEmpty()) {
            Collections.sort(resultList, mDownloadInfoComparator);
        }
        return resultList;
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

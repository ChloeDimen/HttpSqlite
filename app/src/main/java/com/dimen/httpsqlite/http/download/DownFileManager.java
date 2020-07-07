package com.dimen.httpsqlite.http.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.dimen.httpsqlite.db.BaseDaoFactory;
import com.dimen.httpsqlite.http.HttpTask;
import com.dimen.httpsqlite.http.RequestHodler;
import com.dimen.httpsqlite.http.ThreadPoolManager;
import com.dimen.httpsqlite.http.dao.DownDao.DownInfoDao;
import com.dimen.httpsqlite.http.download.interfaces.IDownloadCallable;
import com.dimen.httpsqlite.http.download.interfaces.IDownloadServiceCallable;
import com.dimen.httpsqlite.http.enums.DownloadStatus;
import com.dimen.httpsqlite.http.enums.DownloadStopMode;
import com.dimen.httpsqlite.http.enums.Priority;
import com.dimen.httpsqlite.http.interfaces.IHttpListener;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

/**
 * 文件名：com.dimen.customhttp.http.download
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/28
 */
public class DownFileManager implements IDownloadServiceCallable {
    private static final String TAG = "DownFileManager";
    //    private  static
    private byte[] lock = new byte[0];
    DownInfoDao downLoadDao = BaseDaoFactory.getInstane().
            getDataHelper(DownInfoDao.class, DownloadItemInfo.class);
    SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    /**
     * 观察者模式
     */
    private final List<IDownloadCallable> applisteners = new CopyOnWriteArrayList<IDownloadCallable>();
    /**
     * 怎在下载的所有任务
     */
    private static List<DownloadItemInfo> downloadFileTaskList = new CopyOnWriteArrayList();

    Handler handler = new Handler(Looper.getMainLooper());

    public int download(String url) {
        String[] preFix = url.split("/");
        return this.download(url, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + preFix[preFix.length - 1]);
    }

    public int download(String url, String filePath) {
        String[] preFix = url.split("/");
        String displayName = preFix[preFix.length - 1];
        return this.download(url, filePath, displayName);
    }

    public int download(String url, String filePath, String displayName) {
        return this.download(url, filePath, displayName, Priority.middle);
    }

    public int download(String url, String filePath, String displayName, Priority priority) {
        if (priority == null) {
            priority = Priority.low;
        }
        File file = new File(filePath);
        DownloadItemInfo downloadItemInfo = null;

        downloadItemInfo = downLoadDao.findRecord(url, filePath);
        //没下载
        if (downloadItemInfo == null) {
            /**
             * 根据文件路径查找
             */
            List<DownloadItemInfo> samesFile = downLoadDao.findRecord(filePath);
            /**
             * 大于0  表示下载
             */
            if (samesFile.size() > 0) {
                DownloadItemInfo sameDown = samesFile.get(0);
                if (sameDown.getCurrentLen() == sameDown.getTotalLen()) {
                    synchronized (applisteners) {
                        for (IDownloadCallable downloadCallable : applisteners) {
                            downloadCallable.onDownloadError(sameDown.getId(), 2, "文件已经下载了");
                        }
                    }

                }
            }
            /**
             * 插入数据库
             */
            downloadItemInfo = downLoadDao.addRecrod(url, filePath, displayName, priority.getValue());
            if (downloadItemInfo != null) {
                synchronized (applisteners) {
                    for (IDownloadCallable downloadCallable : applisteners) {
                        //通知应用层  数据库被添加了
                        downloadCallable.onDownloadInfoAdd(downloadItemInfo.getId());
                    }
                }
            }
            downloadItemInfo = downLoadDao.findRecord(url, filePath);
            if (isDowning(file.getAbsolutePath())) {
                synchronized (applisteners) {
                    for (IDownloadCallable downloadCallable : applisteners) {
                        downloadCallable.onDownloadError(downloadItemInfo.getId(), 4, "正在下载，请不要重复添加");
                    }
                }
                return downloadItemInfo.getId();
            }

            if (downloadItemInfo != null) {
                downloadItemInfo.setPriority(priority.getValue());
                //判断数据库存的 状态是否是完成
                if (downloadItemInfo.getStatus() != DownloadStatus.finish.getValue()) {
                    if (downloadItemInfo.getTotalLen() == 0L || file.length() == 0L) {
                        Log.i(TAG, "还未开始下载");
                        downloadItemInfo.setStatus(DownloadStatus.failed.getValue());
                    }
                    //判断数据库中 总长度是否等于文件长度
                    if (downloadItemInfo.getTotalLen() == file.length() && downloadItemInfo.getTotalLen() != 0) {
                        downloadItemInfo.setStatus(DownloadStatus.finish.getValue());
                        synchronized (applisteners) {
                            for (IDownloadCallable downloadCallable : applisteners) {
                                try {
                                    downloadCallable.onDownloadError(downloadItemInfo.getId(), 4, "已经下载了");
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                /**
                 * 更新
                 */
                downLoadDao.updateRecord(downloadItemInfo);
            }

            /**
             * 判断是否已经下载完成
             */
            if (downloadItemInfo.getStatus() == DownloadStatus.finish.getValue()) {
                Log.i(TAG, "已经下载完成  回调应用层");
                final int downId = downloadItemInfo.getId();
                synchronized (applisteners) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (IDownloadCallable downloadCallable : applisteners) {
                                downloadCallable.onDownloadStatusChanged(downId, DownloadStatus.finish);
                            }
                        }
                    });
                }
                downLoadDao.removeRecordFromMemery(downId);
                return downloadItemInfo.getId();
            }//之前的下载 状态为暂停状态
            List<DownloadItemInfo> allDowning = downloadFileTaskList;
            //当前下载不是最高级  则先退出下载
            if (priority != Priority.high) {
                for (DownloadItemInfo downling : allDowning) {
                    //从下载表中  获取到全部正在下载的任务
                    downling = downLoadDao.findSigleRecord(downling.getFilePath());

                    if (downloadItemInfo != null && downloadItemInfo.getPriority() == Priority.high.getValue()) {
                        if (downloadItemInfo.getFilePath().equals(downling.getFilePath())) {
                            return downloadItemInfo.getId();
                        }
                    }
                }
            }
            //
            reallyDown(downloadItemInfo);
            if (priority == Priority.high || priority == Priority.middle) {
                synchronized (allDowning) {
                    for (DownloadItemInfo downloadItemInfo1 : allDowning) {
                        if (!downloadItemInfo.getFilePath().equals(downloadItemInfo1.getFilePath())) {
                            DownloadItemInfo downingInfo = downLoadDao.findSigleRecord(downloadItemInfo1.getFilePath());
                            if (downingInfo != null) {
                                pause(downloadItemInfo.getId(), DownloadStopMode.auto);
                            }
                        }
                    }
                }
                return downloadItemInfo.getId();
            }
            return -1;
        }


        return -1;

    }

    /**
     * 判断当前是否正在下载
     *
     * @param absolutePath
     * @return
     */
    private boolean isDowning(String absolutePath) {
        for (DownloadItemInfo downloadItemInfo : downloadFileTaskList) {
            if (downloadItemInfo.getFilePath().equals(absolutePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加观察者
     *
     * @param downloadCallable
     */
    public void setDownCallable(IDownloadCallable downloadCallable) {
        synchronized (applisteners) {
            applisteners.add(downloadCallable);
        }

    }


    public void reallyDown(String url) {

        synchronized (lock) {
            String[] preFixs = url.split("/");
            String afterFix = preFixs[preFixs.length - 1];

            File file = new File(Environment.getExternalStorageDirectory(), afterFix);
            //实例化DownloadItem
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo(url, file.getAbsolutePath());

            RequestHodler requestHodler = new RequestHodler();
            //设置请求下载的策略
            IHttpServer httpService = new FileDownHttpServer();
            //得到请求头的参数 map
            Map<String, String> map = httpService.getHttpHeadMap();
            /**
             * 处理结果的策略
             */
            IHttpListener httpListener = new DownloadListener(downloadItemInfo, this, httpService);

            requestHodler.setHttpListener(httpListener);
            requestHodler.setHttpServer(httpService);
            requestHodler.setUrl(url);

            HttpTask httpTask = new HttpTask(requestHodler);
            try {
                ThreadPoolManager.getInstance().execute(new FutureTask<Object>(httpTask, null));
            } catch (InterruptedException e) {

            }

        }


    }

    /**
     * 停止
     *
     * @param downloadId
     * @param mode
     */
    public void pause(int downloadId, DownloadStopMode mode) {
        if (mode == null) {
            mode = DownloadStopMode.auto;
        }
        final DownloadItemInfo downloadInfo = downLoadDao.findRecordById(downloadId);
        if (downloadInfo != null) {
            // 更新停止状态
            if (downloadInfo != null) {
                downloadInfo.setStopMode(mode.getValue());
                downloadInfo.setStatus(DownloadStatus.pause.getValue());
                downLoadDao.updateRecord(downloadInfo);
            }
            for (DownloadItemInfo downing : downloadFileTaskList) {
                if (downloadId == downing.getId()) {
                    downing.getHttpTask().pause();
                }
            }
        }
    }

    /**
     * 下载
     */
    public DownloadItemInfo reallyDown(DownloadItemInfo downloadItemInfo) {
        synchronized (lock) {
            //实例化DownloadItem
            RequestHodler requestHodler = new RequestHodler();
            //设置请求下载的策略
            IHttpServer httpService = new FileDownHttpServer();
            //得到请求头的参数 map
            Map<String, String> map = httpService.getHttpHeadMap();
            /**
             * 处理结果的策略
             */
            IHttpListener httpListener = new DownloadListener(downloadItemInfo, this, httpService);

            requestHodler.setHttpListener(httpListener);
            requestHodler.setHttpServer(httpService);
            /**
             *  bug  url
             */
            requestHodler.setUrl(downloadItemInfo.getUrl());

            HttpTask httpTask = new HttpTask(requestHodler);
            downloadItemInfo.setHttpTask(httpTask);

            /**
             * 添加
             */
            downloadFileTaskList.add(downloadItemInfo);
            httpTask.start();

        }

        return downloadItemInfo;

    }


    @Override
    public void onDownloadStatusChanged(DownloadItemInfo downloadItemInfo) {
        DownloadItemInfo sigleRecord = downLoadDao.findSigleRecord(downloadItemInfo.getFilePath());
        if (sigleRecord != null) {
            sigleRecord.setStatus(downloadItemInfo.getStatus());
            downLoadDao.updateRecord(downloadItemInfo);
            synchronized (applisteners) {
                for (IDownloadCallable applistener : applisteners) {
                    applistener.onDownloadStatusChanged(sigleRecord.getId(), DownloadStatus.waitting);
                }
            }
        }

    }

    @Override
    public void onTotalLengthReceived(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onCurrentSizeChanged(DownloadItemInfo downloadItemInfo, double downLenth, long speed) {
        Log.i(TAG, "下载速度：" + speed / 1000 + "k/s");
        Log.i(TAG, "-----路径  " + downloadItemInfo.getFilePath() + "  下载长度  " + downLenth + "   速度  " + speed);
    }

    @Override
    public void onDownloadSuccess(DownloadItemInfo downloadItemInfo) {
        Log.i(TAG, "下载成功    路劲  " + downloadItemInfo.getFilePath() + "  url " + downloadItemInfo.getUrl());
        DownloadItemInfo sigleRecord = downLoadDao.findSigleRecord(downloadItemInfo.getFilePath());
        if (sigleRecord != null) {
            sigleRecord.setCurrentLen(new File(downloadItemInfo.getFilePath()).length());
            sigleRecord.setFinishTime(dateFormat.format(new Date()));
            sigleRecord.setStopMode(DownloadStopMode.hand.getValue());
            sigleRecord.setStatus(DownloadStatus.finish.getValue());
            downLoadDao.updateRecord(sigleRecord);

            synchronized (applisteners) {
                for (IDownloadCallable applistener : applisteners) {
                    applistener.onDownloadSuccess(sigleRecord.getId());
                }
            }
        }
        //自动恢复其他下载
        resumeAutoCancelItem();
    }

    private void resumeAutoCancelItem() {
        List<DownloadItemInfo> allAutoCancellist = downLoadDao.findAllAutoCancelRecords();
        List<DownloadItemInfo> notDownloadinglist = new ArrayList<>();
        for (DownloadItemInfo downloadItemInfo : allAutoCancellist) {
            if (!isDowning(downloadItemInfo.getFilePath())) {
                notDownloadinglist.add(downloadItemInfo);
            }

        }
        for (DownloadItemInfo downloadItemInfo : notDownloadinglist) {
            if (downloadItemInfo.getPriority() == Priority.high.getValue()) {
                resumeItem(downloadItemInfo.getId(), Priority.high);
                return;
            } else if (downloadItemInfo.getPriority() == Priority.middle.getValue()) {
                resumeItem(downloadItemInfo.getId(), Priority.middle);
                return;
            }
        }
    }

        //恢复下载
        private void resumeItem(int  id, Priority priority) {

        DownloadItemInfo recordById = downLoadDao.findRecordById(id);
        if (recordById == null) {
            return;
        }
            if (priority == null) {
                priority=Priority.getInstance(recordById.getPriority()==null ? Priority.low.getValue():Priority.high.getValue());
            }
            File file = new File(recordById.getFilePath());
            recordById.setStopMode(DownloadStopMode.auto.getValue());
            downLoadDao.updateRecord(recordById);
            download(recordById.getUrl(), file.getPath(), null, priority);
    }

    @Override
    public void onDownloadPause(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onDownloadError(DownloadItemInfo downloadItemInfo, int var2, String var3) {

    }
}

package com.dimen.httpsqlite.http.download.interfaces;


import com.dimen.httpsqlite.http.download.DownloadItemInfo;

/**
 * 文件名：com.dimen.customhttp.http.download.interfaces
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/28
 */
public interface IDownloadServiceCallable {
    void onDownloadStatusChanged(DownloadItemInfo downloadItemInfo);

    void onTotalLengthReceived(DownloadItemInfo downloadItemInfo);

    void onCurrentSizeChanged(DownloadItemInfo downloadItemInfo, double downLenth, long speed);

    void onDownloadSuccess(DownloadItemInfo downloadItemInfo);

    void onDownloadPause(DownloadItemInfo downloadItemInfo);

    void onDownloadError(DownloadItemInfo downloadItemInfo, int var2, String var3);


}

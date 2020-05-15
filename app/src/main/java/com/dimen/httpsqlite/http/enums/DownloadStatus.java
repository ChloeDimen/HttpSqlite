package com.dimen.httpsqlite.http.enums;

/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/29
 */
public enum DownloadStatus {
    waitting(0),

    starting(1),

    downloading(2),

    pause(3),

    finish(4),

    failed(5)
            ;

    private int value;
    private   DownloadStatus(int value)
    {
        this.value=value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}

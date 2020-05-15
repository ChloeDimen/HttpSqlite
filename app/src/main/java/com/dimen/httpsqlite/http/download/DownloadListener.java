package com.dimen.httpsqlite.http.download;

import android.os.Handler;
import android.os.Looper;


import com.dimen.httpsqlite.http.enums.DownloadStatus;
import com.dimen.httpsqlite.http.download.interfaces.IDownListener;
import com.dimen.httpsqlite.http.download.interfaces.IDownloadServiceCallable;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

import org.apache.http.HttpEntity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 文件名：com.dimen.customhttp.http.download
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/28
 */
public class DownloadListener implements IDownListener {

    private DownloadItemInfo mDownloadItemInfo;
    private File mFile;
    private String url;
    private long breakPoint;
    private IDownloadServiceCallable mDownloadServiceCallable;

    private IHttpServer mHttpServer;
    /**
     * 得到主线程
     */
    private Handler handler=new Handler(Looper.getMainLooper());
    public void addHttpHeader(Map<String, String> headerMap) {

        long length=getFile().length();
        if(length>0L)
        {
            headerMap.put("RANGE","bytes="+length+"-");
        }


    }

    public DownloadListener(DownloadItemInfo downloadItemInfo, IDownloadServiceCallable downloadServiceCallable, IHttpServer httpServer) {
        mDownloadItemInfo = downloadItemInfo;
        mDownloadServiceCallable = downloadServiceCallable;
        mHttpServer = httpServer;
        mFile = new File(downloadItemInfo.getFilePath());
        //已经下载的长度
        breakPoint = mFile.length();
    }

    public DownloadListener(DownloadItemInfo downloadItemInfo) {
        mDownloadItemInfo = downloadItemInfo;
    }


    @Override
    public void setHttpServer(IHttpServer httpServer) {

    }

    @Override
    public void setCanclCalle() {

    }

    @Override
    public void setPauseCallble() {

    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        //用于计算每秒多少k
        long speed = 0L;
        //花费时间
        long useTime = 0L;
        //下载的长度
        long getLen = 0L;
        //接受的长度
        long receiveLen = 0L;
        boolean bufferLen = false;
        //得到下载的长度
        long dataLength = httpEntity.getContentLength();
        //单位时间下载的字节数
        long calcSpeedLen = 0L;
        //总数
        long totalLength = this.breakPoint + dataLength;
        //更新数量
        this.receviceTotalLength(totalLength);
        //更新状态
        this.downloadStatusChange(DownloadStatus.downloading);
        byte[] buffer = new byte[1024];
        int count = 0;
        long currentTime = System.currentTimeMillis();
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        try {
            if (!makeDir(this.getFile().getParentFile())) {
                mDownloadServiceCallable.onDownloadError(mDownloadItemInfo,1,"创建文件夹失败");
            } else {
                fos = new FileOutputStream(this.getFile(), true);
                bos = new BufferedOutputStream(fos);
                int length = 1;
                while ((length = inputStream.read(buffer)) != -1) {
                    if (this.getHttpService().isCancle()) {
                        mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 1, "用户取消了");
                        return;
                    }

                    if (this.getHttpService().isPause()) {
                        mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 2, "用户暂停了");
                        return;
                    }

                    bos.write(buffer, 0, length);
                    getLen += (long) length;
                    receiveLen += (long) length;
                    calcSpeedLen += (long) length;
                    ++count;
                    if (receiveLen * 10L / totalLength >= 1L || count >= 5000) {
                        currentTime = System.currentTimeMillis();
                        useTime = currentTime - startTime;
                        startTime = currentTime;
                        speed = 1000L * calcSpeedLen / useTime;
                        count = 0;
                        calcSpeedLen = 0L;
                        receiveLen = 0L;
                        //应该保存到数据库
                        this.downloadLengthChange(this.breakPoint + getLen, totalLength, speed);
                    }
                }
                bos.close();
                inputStream.close();
                if (dataLength != getLen) {
                    mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 3, "下载长度不相等");
                } else {
                    this.downloadLengthChange(this.breakPoint + getLen, totalLength, speed);
                    this.mDownloadServiceCallable.onDownloadSuccess(mDownloadItemInfo.copy());
                }
            }
        } catch (IOException ioException) {
            if (this.getHttpService() != null) {
//                this.getHttpService().abortRequest();
            }
            return;
        } catch (Exception e) {
            if (this.getHttpService() != null) {
//                this.getHttpService().abortRequest();
            }
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }

                if (httpEntity != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadLengthChange(final long downlength, final long totalLength, final long speed) {
        mDownloadItemInfo.setCurrentLength(downlength);
        if(mDownloadServiceCallable!=null)
        {
            final DownloadItemInfo copyDownItenIfo=mDownloadItemInfo.copy();
            synchronized (this.mDownloadServiceCallable)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onCurrentSizeChanged(copyDownItenIfo,downlength/totalLength,speed);
                    }
                });
            }

        }

    }

    /**
     * 创建文件夹的操作
     * @param parentFile
     * @return
     */
    private boolean makeDir(File parentFile) {
        return parentFile.exists()&&!parentFile.isFile()
                ?parentFile.exists()&&parentFile.isDirectory():
                parentFile.mkdirs();
    }
    /**
     * 更改下载时的状态
     * @param downloading
     */
    private void downloadStatusChange(DownloadStatus downloading) {
        mDownloadItemInfo.setStatus(downloading.getValue());
        final DownloadItemInfo copyDownloadItemInfo=mDownloadItemInfo.copy();
        if(mDownloadServiceCallable!=null)
        {
            synchronized (this.mDownloadServiceCallable)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onDownloadStatusChanged(copyDownloadItemInfo);
                    }
                });
            }
        }
    }

    /**
     * 回调  长度的变化
     * @param totalLength
     */
    private void receviceTotalLength(long totalLength) {
        mDownloadItemInfo.setCurrentLength(totalLength);
        final DownloadItemInfo copyDownloadItemInfo=mDownloadItemInfo.copy();
        if(mDownloadServiceCallable!=null)
        {
            synchronized (this.mDownloadServiceCallable)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onTotalLengthReceived(copyDownloadItemInfo);
                    }
                });
            }
        }

    }

    @Override
    public void OnFail() {

    }

    public IHttpServer getHttpService() {
        return mHttpServer;
    }

    public File getFile() {
        return mFile;
    }
}

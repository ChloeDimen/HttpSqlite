package com.dimen.httpsqlite.http;


import com.dimen.httpsqlite.http.interfaces.IHttpListener;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public class RequestHodler<T> {
    //执行下载类
    private IHttpServer mHttpServer;

    //获取数据回调结果类
    private IHttpListener mHttpListener;

    //请求参数对应的实体
    private T requestInfo;

    private String url;

    public IHttpServer getHttpServer() {
        return mHttpServer;
    }

    public void setHttpServer(IHttpServer httpServer) {
        mHttpServer = httpServer;
    }

    public IHttpListener getHttpListener() {
        return mHttpListener;
    }

    public void setHttpListener(IHttpListener httpListener) {
        mHttpListener = httpListener;
    }

    public T getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(T requestInfo) {
        this.requestInfo = requestInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

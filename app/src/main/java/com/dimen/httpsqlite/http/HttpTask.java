package com.dimen.httpsqlite.http;



import com.alibaba.fastjson.JSON;
import com.dimen.httpsqlite.http.interfaces.IHttpListener;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

import java.io.UnsupportedEncodingException;

/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public class HttpTask <T> implements Runnable{

    private IHttpServer mIHttpServer;
    public HttpTask(RequestHodler<T> requestHolder){
        mIHttpServer = requestHolder.getHttpServer();
        mIHttpServer.setHttpListener(requestHolder.getHttpListener());
        mIHttpServer.setUrl(requestHolder.getUrl());


        //增加方法
        IHttpListener listener = requestHolder.getHttpListener();
        listener.addHttpHeader(mIHttpServer.getHttpHeadMap());
        try {
            T request = requestHolder.getRequestInfo();
            if (request!=null){
                String requestInfo = JSON.toJSONString(request);
                mIHttpServer.setRequestData(requestInfo.getBytes("UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void run() {
        mIHttpServer.execute();
    }
}

package com.dimen.httpsqlite.http.interfaces;

import java.util.Map;

/**
 * 文件名：com.dimen.customhttp.http.interfaces
 * 描    述：获取网络
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public interface IHttpServer {
    //设置Url
    void setUrl(String url);

    //执行获取网络
    void execute();

    //设置处理接口
    void setHttpListener(IHttpListener listener);

    //设置请求参数
    void setRequestData(byte[] requestData);

    boolean isPause();
    void pause();

    //获取请求头的方法
    Map<String, String> getHttpHeadMap();


    boolean cancle();
    //
    boolean isCancle();




}

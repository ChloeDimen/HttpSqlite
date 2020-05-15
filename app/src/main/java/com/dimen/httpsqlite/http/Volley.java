package com.dimen.httpsqlite.http;



import com.dimen.httpsqlite.http.interfaces.IDataListener;
import com.dimen.httpsqlite.http.interfaces.IHttpListener;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

import java.util.concurrent.FutureTask;

/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public class Volley {

    /**
     *
     * @param <T>请求参数类型
     * @param <M>响应参数类型
     *           暴露给调用层
     */
    public static <T,M> void sendRequest(T requestInfo, String url, Class<M> response, IDataListener listener){

        RequestHodler<T> requestHodler = new RequestHodler<>();
        requestHodler.setUrl(url);

        requestHodler.setRequestInfo(requestInfo);
        IHttpServer httpServer = new JsonHttpService();
        IHttpListener httpListener = new JsonDealListener<>(response, listener);
        requestHodler.setHttpListener(httpListener);
        requestHodler.setHttpServer(httpServer);
        //请求参数赋值
        requestHodler.setRequestInfo(requestInfo);
        HttpTask<T> httpTask = new HttpTask<>(requestHodler);
        try {
            ThreadPoolManager.getInstance().execute(new FutureTask<Object>(httpTask, null));
        } catch (InterruptedException e) {
            listener.onFail();
        }
    }


}

package com.dimen.httpsqlite.http.download;

import android.util.Log;


import com.dimen.httpsqlite.http.interfaces.IHttpListener;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 文件名：com.dimen.customhttp.http.download
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/28
 */
public class FileDownHttpServer implements IHttpServer {
    private static final String TAG = "FileDownHttpServer";
    private HttpClient mHttpClient = new DefaultHttpClient();

    private String url;
    private IHttpListener mIHttpListener;
    //请求头,线程安全
    private Map<String, String> headerMap = Collections.synchronizedMap(new HashMap<String, String>());
    private HttpPost mHttpPost;

    private byte[] requestData;
    //获取网络的回调
    private HttpResponseHandler mHttpResponseHandler = new HttpResponseHandler();

    @Override
    public void setUrl(String url) {

        this.url = url;
    }

    @Override
    public void execute() {
        mHttpPost = new HttpPost(url);
        //请求头的参数
        constrcutHeader();

      //  ByteArrayEntity byteArrayEntity = new ByteArrayEntity(requestData);
       // mHttpPost.setEntity(byteArrayEntity);

        try {
            mHttpClient.execute(mHttpPost, mHttpResponseHandler);
        } catch (IOException e) {
            mIHttpListener.OnFail();
        }
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    private void constrcutHeader() {
      Iterator<String> iterator = headerMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = headerMap.get(key);
            Log.i(TAG, "请求头现象: " + key + " value=" + value);
            mHttpPost.addHeader(key,value);

        }

      /*  for (String key : headerMap.keySet()) {
            String value = headerMap.get(key);
        }*/
       /* Iterator<Map.Entry<String, String>> iterator = headerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
        }*/

       /*//适合容量大的
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
        }*/
    }

    @Override
    public void setHttpListener(IHttpListener listener) {
        this.mIHttpListener = listener;
    }

    @Override
    public void setRequestData(byte[] requestData) {
        this.requestData = requestData;
    }

    @Override
    public boolean isPause() {
        return false;
    }

    @Override
    public void pause() {

    }

    @Override
    public Map<String, String> getHttpHeadMap() {
        return headerMap;
    }

    @Override
    public boolean cancle() {
        return false;
    }

    @Override
    public boolean isCancle() {
        return false;
    }

    private class HttpResponseHandler extends BasicResponseHandler {

        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            //响应码
            int code  = response.getStatusLine().getStatusCode();
            Log.i(TAG, "响应码: " +code);
            if (code == 200||206==code) {
                mIHttpListener.onSuccess(response.getEntity());
            } else {
                mIHttpListener.OnFail();
            }
            return null;
        }
    }
}

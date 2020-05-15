package com.dimen.httpsqlite.http;



import com.dimen.httpsqlite.http.interfaces.IHttpListener;
import com.dimen.httpsqlite.http.interfaces.IHttpServer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Map;

/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public class JsonHttpService implements IHttpServer {

    private IHttpListener mIHttpListener;


    private HttpClient mHttpClient = new DefaultHttpClient();
    private HttpPost mHttpPost;
    private String url;
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
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(requestData);
        mHttpPost.setEntity(byteArrayEntity);

        try {
            mHttpClient.execute(mHttpPost, mHttpResponseHandler);
        } catch (IOException e) {
           mIHttpListener.OnFail();
        }
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
        return null;
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
            if (code == 200) {
                mIHttpListener.onSuccess(response.getEntity());
            } else {
                mIHttpListener.OnFail();
            }
            return null;
        }
    }
}

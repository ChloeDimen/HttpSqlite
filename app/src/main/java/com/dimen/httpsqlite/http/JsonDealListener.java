package com.dimen.httpsqlite.http;



import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.dimen.httpsqlite.http.interfaces.IDataListener;
import com.dimen.httpsqlite.http.interfaces.IHttpListener;

import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;


/**
 * 文件名：com.dimen.customhttp.http
 * 描    述：M 对应响应类
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public class JsonDealListener<M> implements IHttpListener {
    private Class<M> responese;
    private IDataListener<M> mIDataListener;


    Handler mHandler = new Handler(Looper.getMainLooper());

    public JsonDealListener(Class<M> responese, IDataListener<M> IDataListener) {
        this.responese = responese;
        mIDataListener = IDataListener;
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
           /*
            得到网络返回的数据
            子线程
             */
            String content = getContent(inputStream);
            final M m = JSON.parseObject(content, responese);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mIDataListener.onSuccess(m);
                }
            });

        } catch (IOException e) {
            mIDataListener.onFail();
        }

    }


    @Override
    public void OnFail() {
        mIDataListener.onFail();
    }

    @Override
    public void addHttpHeader(Map<String, String> headerMap) {

    }


    private String getContent(InputStream inputStream) {

        String content = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            try {
                while ((line = bufferedReader.readLine()) != null) {

                    stringBuilder.append(line + "\n");

                }
            } catch (IOException e) {

                mIDataListener.onFail();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mIDataListener.onFail();
        }
        return content;
    }

}

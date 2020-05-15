package com.dimen.httpsqlite.http.interfaces;

import org.apache.http.HttpEntity;

import java.util.Map;

/**
 * 文件名：com.dimen.customhttp.http.interfaces
 * 描    述：处理结果
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public interface IHttpListener
{
    void onSuccess(HttpEntity httpEntity);

    void OnFail();
    void addHttpHeader(Map<String, String> headerMap);

}

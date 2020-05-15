package com.dimen.httpsqlite.http.interfaces;

/**
 * 文件名：com.dimen.customhttp.http.interfaces
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/27
 */
public interface IDataListener<M>
{
    //回调结果给调用层
    void onSuccess(M m);

    void onFail();
}

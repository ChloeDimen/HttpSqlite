package com.dimen.httpsqlite.db;

import java.util.List;

/**
 * 文件名：com.dimen.customsqlite.db
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/16
 */
public interface IBaseDao<T>
{

    /**
     * 插入数据
     *
     * @param entity
     * @return
     */
    Long insert(T entity);

    /**
     * 删除数据
     *
     * @param where 条件变量
     * @return
     */
    int delete(T where);

    /**
     * 查询数据
     *
     * @param where
     * @return
     */
    List<T> query(T where);

    /**
     * 查询数据
     *
     * @param where      条件变量
     * @param orderBy    排序方式
     * @param startIndex 查询的位置
     * @param limit      查询的现在
     * @return
     */
    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);


    List<T>  query(String sql);

    /**
     * 更新数据
     *
     * @param entity
     * @param where
     * @return
     */
    int update(T entity, T where);

}

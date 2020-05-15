package com.dimen.httpsqlite.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


import com.dimen.httpsqlite.db.annotion.DbFiled;
import com.dimen.httpsqlite.db.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件名：com.dimen.customsqlite.db
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/16
 */
public abstract class BaseDao<T> implements IBaseDao<T>
{
    private static final String TAG = "BaseDao";
    /**
     * ]
     * 持有数据库操作类的引用
     */
    protected SQLiteDatabase database;
    /**
     * 保证实例化一次
     */
    private boolean isInit = false;
    /**
     * 持有操作数据库表所对应的java类型
     */
    private Class<T> entityClass;
    /**
     * 维护表名与成员变量名的映射关系
     * key--->表名
     * value -->Field
     */

    private HashMap<String, Field> cacheMap;

    private String tableName;

    public String getTableName() {
        return tableName;
    }

    protected boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase)
    {


        if (!isInit)
        {
            entityClass = entity;
            database = sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class) == null)
            {
                tableName = entity.getClass().getSimpleName();
            } else
            {
                tableName = entity.getAnnotation(DbTable.class).value();
            }
            if (!database.isOpen())
            {
                return false;
            }
            if (!TextUtils.isEmpty(createTable()))
            {
                database.execSQL(createTable());
            }
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
        }
        return isInit;
    }

    /**
     * 维护映射关系
     */
    private void initCacheMap()
    {
        //第一条数据   查0个数据
        String sql = "Select * from " + this.tableName + " limit 1 ,0";
        Cursor cursor = null;
        try
        {
            cursor = database.rawQuery(sql, null);
            //表的列名数组
            String[] columnNames = cursor.getColumnNames();
            //拿到Filed数组,只能获取到public的成员变量
            // Field[] columnFields = entityClass.getFields();
            Field[] columnFields = entityClass.getDeclaredFields();
            for (Field field : columnFields)
            {
                field.setAccessible(true);
            }

            /**
             * 开始找对应关系
             */
            for (String columnName : columnNames)
            {
                /**
                 * 如果找到对应的Filed就赋值给他
                 * User
                 */
                Field colmunFiled = null;
                for (Field field : columnFields)
                {
                    String fieldName = null;
                    if (field.getAnnotation(DbFiled.class) != null)
                    {
                        fieldName = field.getAnnotation(DbFiled.class).value();
                    } else
                    {
                        fieldName = field.getName();
                    }
                    /**
                     * 如果表的列名 等于了  成员变量的注解名字
                     */
                    if (columnName.equals(fieldName))
                    {
                        colmunFiled = field;
                        break;
                    }
                }
                //找到了对应关系
                if (colmunFiled != null)
                {
                    cacheMap.put(columnName, colmunFiled);
                }

            }
        } catch (Exception e)
        {

        } finally
        {
            cursor.close();
        }


    }

    @Override
    public Long insert(T entity)
    {
        Map<String, String> map = getValues(entity);
        ContentValues contentValues = getContentValues(map);
        long result = database.insert(tableName, null, contentValues);
        return result;
    }


    /**
     * 将map转换为ContentValues
     *
     * @param map
     * @return
     */
    private ContentValues getContentValues(Map<String, String> map)
    {
        ContentValues contentValues = new ContentValues();
        Set keySet = map.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext())
        {
            String key = iterator.next();
            String value = map.get(key);
            if (value != null)
            {
                contentValues.put(key, value);
            }
        }

        return contentValues;
    }

    private Map<String, String> getValues(T entity)
    {
        HashMap<String, String> result = new HashMap<>();
        Iterator<Field> fieldIterator = cacheMap.values().iterator();
        while (fieldIterator.hasNext())
        {
            //循环遍历 映射map 的Field
            Field colmunToField = fieldIterator.next();

            String cacheKey = null;
            String cacheValues = null;
            //判断是否有注解
            if (colmunToField.getAnnotation(DbFiled.class) != null)
            {
                cacheKey = colmunToField.getAnnotation(DbFiled.class).value();


            } else
            {
                cacheKey = colmunToField.getName();
            }

            try
            {
                if (null == colmunToField.get(entity))
                {
                    continue;
                }
                cacheValues = colmunToField.get(entity).toString();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            result.put(cacheKey, cacheValues);
        }


        return result;
    }

    /**
     * 封装修改语句
     */
    class Condition
    {

        //查询条件  name=? &&password=?
        private String whereClause;
        private String[] whereArgs;


        public Condition(Map<String, String> whereClause)
        {
            ArrayList list = new ArrayList();
            StringBuilder stringBuilder = new StringBuilder();
            //and不报错
            stringBuilder.append("1=1");
            Set keys = whereClause.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                String value = whereClause.get(key);
                if (value != null)
                {
                    //"1=1 and name=? and password=?" 拼接条件查询语句
                    stringBuilder.append(" and " + key + " =?");
                    //?的值
                    list.add(value);
                }
            }
            this.whereClause = stringBuilder.toString();
            this.whereArgs = (String[]) list.toArray(new String[list.size()]);

        }

        public String getWhereClause()
        {
            return whereClause;
        }

        public String[] getWhereArgs()
        {
            return whereArgs;
        }
    }


    /**
     * 更新数据
     *
     * @param entity
     * @param where
     * @return
     */


    @Override
    public int update(T entity, T where)
    {
        int result = -1;
        Map values = getValues(entity);
        //将条件变量转换map
        Map whereClause = getValues(where);
        Condition condition = new Condition(whereClause);
        ContentValues contentValues = getContentValues(values);
        result = database.update(tableName, contentValues, condition.getWhereClause(), condition.getWhereArgs());

        return result;
    }

    @Override
    public int delete(T where)
    {
        int result = -1;

        //将条件变量转换map
        Map whereClause = getValues(where);
        Condition condition = new Condition(whereClause);

        result = database.delete(tableName, condition.getWhereClause(), condition.getWhereArgs());
        return result;
    }

    @Override
    public List<T> query(T where)
    {
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit)


    {

        Map whereClause = getValues(where);
        Condition condition = new Condition(whereClause);
        // String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy

        String limitString = null;
        if (limitString != null && limit != null)
        {
            limitString = startIndex + " ," + limit;
        }

        Cursor cursor = database.query(tableName, null, condition.getWhereClause(), condition.getWhereArgs(), null, null, orderBy, limitString);

        List<T> result = getResult(cursor, where);
        cursor.close();

        return result;
    }


    private List<T> getResult(Cursor cursor, T where)
    {

        ArrayList list = new ArrayList();
        Object item;

        while (cursor.moveToNext())
        {
            try
            {
                item = where.getClass().newInstance();

                Iterator iterator = cacheMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    /**
                     * 得到列名
                     */
                    String colomunName = (String) entry.getKey();
                    /**
                     * 然后以列名拿到  列名在游标的位子
                     */
                    Integer columnIndex = cursor.getColumnIndex(colomunName);
                    Field field = (Field) entry.getValue();

                    Class<?> type = field.getType();

                    if (columnIndex!=-1)
                    {
                        if (type == String.class)
                        {
                            //反射方式赋值
                            field.set(item, cursor.getString(columnIndex));
                        }else if(type==Double.class)
                        {
                            field.set(item,cursor.getDouble(columnIndex));
                        }else  if(type==Integer.class)
                        {
                            field.set(item,cursor.getInt(columnIndex));
                        }else if(type==Long.class)
                        {
                            field.set(item,cursor.getLong(columnIndex));
                        }else  if(type==byte[].class)
                        {
                            field.set(item,cursor.getBlob(columnIndex));
                            /*
                            不支持的类型
                             */
                        }else {
                            continue;
                        }

                    }


                }
                list.add(item);

            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 创建表
     *
     * @return
     */
    protected abstract String createTable();
}

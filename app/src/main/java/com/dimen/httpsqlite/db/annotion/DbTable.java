package com.dimen.httpsqlite.db.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件名：com.dimen.customsqlite.db.annotion
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbTable {
    String value();

}

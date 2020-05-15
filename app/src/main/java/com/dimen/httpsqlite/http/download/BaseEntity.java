package com.dimen.httpsqlite.http.download;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 文件名：com.dimen.customhttp.http.download
 * 描    述：
 * 作    者：Dimen
 * 时    间：2020/4/29
 */
public class BaseEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public BaseEntity() {
    }

    public T copy() {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Object result = objectInputStream.readObject();
            return (T) result;
        } catch (IOException io) {
            io.printStackTrace();
        } catch (ClassNotFoundException classNot) {
            classNot.printStackTrace();
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

        }

        return null;
    }


}

package com.dimen.httpsqlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dimen.httpsqlite.http.download.DownFileManager;

public class MainActivity extends AppCompatActivity {
    public static final String url = "http://192.168.20.183:8080/app/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void login(View view) {
        DownFileManager downFileService=new DownFileManager();
        downFileService.download(url+"VLC-Android-3.1.1-armeabi-v7a.apk");
    }
}

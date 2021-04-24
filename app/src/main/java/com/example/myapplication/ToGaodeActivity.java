package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ToGaodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_gaode);
        Intent i1 = new Intent(Intent.ACTION_VIEW);

// 展示地图

        i1.setData(Uri.parse("http://map.baidu.com/mobile/"));

        startActivity(i1);

//网页应用调起Android百度地图方式举例


    }
}

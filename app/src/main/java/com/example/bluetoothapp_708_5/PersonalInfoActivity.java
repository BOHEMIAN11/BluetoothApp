package com.example.bluetoothapp_708_5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonalInfoActivity extends AppCompatActivity {

    private int id;

    private String strName;

    private String strCommunity;

    private String strUser;

    private String strAge;

    private String strSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_info);

        TextView username = findViewById(R.id.UserNameEdit);
        TextView community = findViewById(R.id.community);
        TextView user = findViewById(R.id.userid);
        TextView date = findViewById(R.id.date);
        TextView sex = findViewById(R.id.sex);
        Button submit = findViewById(R.id.submit);
        Intent intent = getIntent();
        id = Integer.parseInt(intent.getStringExtra("id"));
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PersonalInfoActivity.this.finish();
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new  Request.Builder()
                        .url("http://192.168.181.212:9001/phone/selectById/"+id)
                        .get()
                        .build();

                try {

                    Response response = client.newCall(request).execute();
                    String i = response.body().string();
                    String res = i.substring(1,i.length()-1);
                    JSONObject jsonObject = JSONObject.parseObject(res);
                    strName = jsonObject.getString("olderName");
                    strCommunity = jsonObject.getString("communityName");
                    strUser = jsonObject.getString("username");
                    strAge = jsonObject.getString("olderAge");
                    strSex = jsonObject.getString("sex");
                    System.out.println(jsonObject);
                    System.out.println(strName+strCommunity+strUser+strAge+strSex);


                } catch (IOException e) {
                    System.out.println("e");
                }

            }
        });
        thread.start();
        try {
            thread.join();//解决线程同步导致主线程结束子线程未完成的问题
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        username.setText(strName);
        community.setText(strCommunity);
        user.setText(strUser);
        date.setText(strAge);
        sex.setText(strSex);
    }
}

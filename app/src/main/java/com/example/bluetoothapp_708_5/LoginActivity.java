package com.example.bluetoothapp_708_5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String userInfo;

    private int code;

    private String message;

    private String olderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Button button1 = findViewById(R.id.SignUpButton);
        Button button2 = findViewById(R.id.LoginButton);
        EditText username = findViewById(R.id.UserNameEdit);
        EditText password = findViewById(R.id.PassWordEdit);
        String lusername = username.getText().toString().trim();
        String lpassword = password.getText().toString().trim();

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strUserName = username.getText().toString().trim();
                String strPassword = password.getText().toString().trim();
                userInfo = "{\"olderName\":"+"\""+strUserName+"\""+
                        ", \"password\":"+"\""+strPassword+"\""+
                        "}";

                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("http://192.168.181.212:9001/phone/login")
                                    .post(RequestBody.create(MediaType.parse("application/json"),userInfo))
                                    .build();
                            Response response = client.newCall(request).execute();
                            String result = response.body().string();
                            JSONObject jsonObject = JSONObject.parseObject(result);
                            message = jsonObject.getString("message");
                            code = jsonObject.getIntValue("code");
                            olderId = jsonObject.getString("total");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                });
                thread1.start();
                try {
                    thread1.join();
                } catch (InterruptedException e) {
                    System.out.println("error");
                    throw new RuntimeException(e);
                }


                if (code == 1){
                    System.out.println(code);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("olderId",olderId);
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }else {
                    System.out.println(code);
                    System.out.println(message);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
}

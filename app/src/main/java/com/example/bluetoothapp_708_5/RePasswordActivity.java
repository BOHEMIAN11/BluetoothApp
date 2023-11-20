package com.example.bluetoothapp_708_5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RePasswordActivity extends AppCompatActivity {
    private String json;

    private int id=1;

    private String strOldPassword;

    private String strNewPassword;

    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.repassword);
        TextView oldPassword = findViewById(R.id.password);
        TextView newPassword = findViewById(R.id.rePassword);

        Button submit = findViewById(R.id.submit);
        Button back =findViewById(R.id.back);
        Intent intent=getIntent();

        id = Integer.parseInt(intent.getStringExtra("id"));

        json = "{\"id\":"+id+
                ", \"password\":"+"\""+strOldPassword+"\""+
                ", \"newPassword\":"+"\""+strNewPassword+"\""+
                "}";
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strOldPassword = oldPassword.getText().toString().trim();
                strNewPassword = newPassword.getText().toString().trim();
                json = "{\"id\":"+id+
                        ", \"password\":"+"\""+strOldPassword+"\""+
                        ", \"newPassword\":"+"\""+strNewPassword+"\""+
                        "}";
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("http://192.168.181.212:9001/phone/upPassword")
                                    .post(RequestBody.create(MediaType.parse("application/json"),json))
                                    .build();
                            Response response = client.newCall(request).execute();
                            assert response.body() != null;
                            String res = response.body().string();
                            result = Integer.parseInt(res);
                            System.out.println(result);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (result==1){
                    Toast.makeText(RePasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    RePasswordActivity.this.finish();
                }else {
                    Toast.makeText(RePasswordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();

                }
            }

        });

    }

}

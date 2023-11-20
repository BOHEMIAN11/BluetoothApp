package com.example.bluetoothapp_708_5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        TextView info = findViewById(R.id.p_info);
        TextView permission = findViewById(R.id.permission);
        TextView password = findViewById(R.id.password);
        TextView notice = findViewById(R.id.notice);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                intent.putExtra("id",intent.getStringExtra("olderId"));
                intent.setClass(SettingActivity.this, PersonalInfoActivity.class);
                startActivity(intent);
            }
        });
        permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this,PermissionActivity.class);
                startActivity(intent);
            }
        });
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                intent.putExtra("id",intent.getStringExtra("olderId"));
                intent.setClass(SettingActivity.this,RePasswordActivity.class);
                startActivity(intent);

            }
        });
        notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this,NoticeActivity.class);
                startActivity(intent);
            }
        });

    }

}

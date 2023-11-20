package com.example.bluetoothapp_708_5;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private Calendar calendar;

    private DatePickerDialog datePickerDialog;

    private String userInfo;

    private String olderSex="1";

    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //关联activity_register.xml
        setContentView(R.layout.register);

        // 关联用户名、密码、确认密码、邮箱和注册、返回登录按钮
        EditText userName = findViewById(R.id.UserNameEdit);
        EditText passWord = findViewById(R.id.PassWordEdit);
        EditText passWordAgain = findViewById(R.id.PassWordAgainEdit);
        TextView date = findViewById(R.id.date);
        Button signUpButton = findViewById(R.id.SignUpButton);
        Button backLoginButton = findViewById(R.id.BackLoginButton);
        Spinner sex = findViewById(R.id.sex);

        //日期选择器
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                System.out.println("年-->" + year + "月-->"
                                        + monthOfYear + "日-->" + dayOfMonth);
                                date.setText(year + "-" + monthOfYear + "-"
                                        + dayOfMonth);
                            }
                        }, calendar.get(Calendar.YEAR), calendar
                        .get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        //性别spinner选择器
        sex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String s = RegisterActivity.this.getResources().getStringArray(R.array.sex)[i];//获取对应选项的值，i=0则为男，i=1则为女
                if(i == 0){
                    olderSex = "0";
                }else {
                    olderSex = "1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 立即注册按钮监听器
        signUpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strUserName = userName.getText().toString().trim();
                        String strPassWord = passWord.getText().toString().trim();
                        String strPassWordAgain = passWordAgain.getText().toString().trim();
                        Date birthday = transferString2Date(date.getText().toString());
                        userInfo = "{\"olderName\":"+"\""+strUserName+"\""+
                                ", \"password\":"+"\""+strPassWord+"\""+
                                ", \"address\":"+"\""+date.getText().toString().trim()+"\""+
                                ", \"sex\":"+"\""+olderSex+"\""+
                                "}";
                        //注册格式粗检
                        if (strUserName.length() > 10) {
                            Toast.makeText(RegisterActivity.this, "用户名长度必须小于10！", Toast.LENGTH_SHORT).show();
                        } else if (strUserName.length() == 0) {
                            Toast.makeText(RegisterActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                        } else if (strPassWord.length() > 16) {
                            Toast.makeText(RegisterActivity.this, "密码长度必须小于16！", Toast.LENGTH_SHORT).show();
                        } else if (strPassWord.length() < 6) {
                            Toast.makeText(RegisterActivity.this, "密码长度必须大于6！", Toast.LENGTH_SHORT).show();
                        } else if (!strPassWord.equals(strPassWordAgain)) {
                            Toast.makeText(RegisterActivity.this, "两次密码输入不一致！", Toast.LENGTH_SHORT).show();
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        OkHttpClient client = new OkHttpClient();
                                        Request request = new Request.Builder()
                                                .url("http://192.168.181.212:9001/phone/register")
                                                .post(RequestBody.create(MediaType.parse("application/json"),userInfo))
                                                .build();
                                        Response response = client.newCall(request).execute();
                                        System.out.println(userInfo);
                                        System.out.println("response="+response.body().string());
                                        result = response.body().toString();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            // 跳转到登录界面
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);

                        }
                    }
                }
        );
        // 返回登录按钮监听器
        backLoginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // 跳转到登录界面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
        );

    }

    //string转date
    public static Date transferString2Date(String s) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (ParseException e) {
            //LOGGER.error("时间转换错误, string = {}", s, e);
        }
        return date;
    }


}

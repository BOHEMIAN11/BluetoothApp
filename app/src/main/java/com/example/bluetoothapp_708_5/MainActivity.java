package com.example.bluetoothapp_708_5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private static final int SEND_SMS = 100;
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    BluetoothDevice _device = null;     //蓝牙设备
    private BluetoothSocket _socket;
    boolean bRun = true;
    boolean bThread = false;
    private InputStream is;    //输入流，用来接收蓝牙数据
    private String smsg = "";    //显示用数据缓存

    private String temperature;

    private String bloodOxygen;

    private String heartRate;

    private String internal;

    private String olderId;

    private double[] saveTemp={35.0,37.5};
    private double saveBlood=80.0;
    private double[] saveHeart={55,120};

    private int warning=0;

    private int infoNum=0;

    private String phone="";
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        olderId = intent.getStringExtra("olderId");
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }

        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // 设置设备可以被搜索
        new Thread(){
            @SuppressLint("MissingPermission")
            public void run(){
                if(_bluetooth.isEnabled()==false) {
                    _bluetooth.enable();
                }
            }
        }.start();
    }

    /**
     * 重写事件分发
     */
    @Override
    public  boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                v.clearFocus();//清除Edittext的焦点从而让光标消失
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        boolean vinstanceof;
        if (v !=null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationOnScreen(l);;
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getRawX() > left &&event.getRawX() < right
                    && event.getRawY()> top && event.getRawY() < bottom) {
                //点击EditText的时候不做隐藏处理
                return false;
            } else {
                return true;
            }
        }
        //如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     * @param token
     */
    private void hideKeyboard(IBinder token) {

        if (token !=null) {
            //若token不为空则获取输入法管理器使其隐藏输入法键盘
            InputMethodManager im =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 得到蓝牙设备句柄
                    _device = _bluetooth.getRemoteDevice(address);

                    // 用服务号得到socket
                    try{
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    }catch(IOException e){
                        Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                    }
                    //连接socket
                    try{
                        _socket.connect();
                        Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                    }catch(IOException e){
                        try{
                            Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                            _socket.close();
                            _socket = null;
                        }catch(IOException ee){
                            Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }

                    //打开接收线程
                    try{
                        is = _socket.getInputStream();   //得到蓝牙数据输入流
                    }catch(IOException e){
                        Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(bThread==false){
                        readThread.start();
                        bThread=true;
                    }else{
                        bRun = true;
                    }
                }
                break;
            default:break;
        }
    }

    //接收数据线程
    Thread readThread=new Thread(){

        public void run(){
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            //接收线程
            while(true){
                try{
                    while(is.available()==0){
                        while(bRun == false){}
                    }
                    while(true){
                        if(!bThread)//跳出循环
                            return;

                        num = is.read(buffer);         //读入数据
                        n=0;
                        System.out.println(num);
                        for(i=0;i<num;i++){
                            if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
                                buffer_new[n] = 0x0a;
                                i++;
                            }else{
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new,0,n);
                        smsg=s;   //写入接收缓存
                        if(is.available()==0)break;  //短时间没有数据才跳出进行显示
                    }
                    //发送显示消息，进行显示刷新
                    handler.sendMessage(handler.obtainMessage());
                }catch(IOException e){
                }
            }
        }
    };

    //消息处理队列
    @SuppressLint("HandlerLeak")
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            int intIndex1 = smsg.indexOf("$hear:");
            if(intIndex1!=-1) {
                String str = smsg;
                TextView textView = findViewById(R.id.textViewHeartrate);
                heartRate = str.substring(intIndex1+6,str.indexOf("#",intIndex1));
                textView.setText(heartRate);//显示数据
                if (Double.parseDouble(heartRate)<saveHeart[0] || Double.parseDouble(heartRate)>saveHeart[1]){
                    textView.setTextColor(Color.rgb(254, 0, 0));
                }else {
                    textView.setTextColor(Color.rgb(0, 254, 0));
                }

            }

            int intIndex2 = smsg.indexOf("$spo2:");
            if(intIndex2!=-1) {
                String str = smsg;
                TextView textView = findViewById(R.id.textViewSPO2);
                bloodOxygen = str.substring(intIndex2+6,str.indexOf("#",intIndex2));
                textView.setText(bloodOxygen);   //显示数据
                if (Double.parseDouble(bloodOxygen)<saveBlood){
                    textView.setTextColor(Color.rgb(254, 0, 0));
                }else {
                    textView.setTextColor(Color.rgb(0, 254, 0));
                }

            }

            int intIndex3 = smsg.indexOf("$temp:");
            if(intIndex3!=-1) {
                String str = smsg;
                TextView textView = findViewById(R.id.textViewTemp);
                temperature = str.substring(intIndex3+6,str.indexOf("#",intIndex3));
                textView.setText(temperature);   //显示数据
                if (Double.parseDouble(temperature)<saveTemp[0] || Double.parseDouble(temperature)>saveTemp[1]){
                    textView.setTextColor(Color.rgb(254, 0, 0));
                }else {
                    textView.setTextColor(Color.rgb(0, 254, 0));
                }
            }

            double numTemperature = Double.parseDouble(temperature);
            double numBloodOxygen = Double.parseDouble(bloodOxygen);
            double numHeartRate = Double.parseDouble(heartRate);
            String state="正常";
            System.out.println(saveBlood+"++"+saveTemp[0]+"++"+saveTemp[1]+"++"+saveHeart[0]+"++"+saveHeart[1]);
            if(numHeartRate<saveHeart[0] || numHeartRate>saveHeart[1] || numTemperature>saveTemp[1] || numBloodOxygen<saveBlood){
                warning++;
                state="异常";

                if (warning==30){
                    requestPermission();
                    warning();
                    warning=0;
                }
            }
            internal ="{\"temperature\":\""+temperature+"\""+
                    ", \"bloodOxygen\":\""+bloodOxygen+"\""+
                    ", \"heartRate\":\""+heartRate+"\""+
                    ", \"olderId\":\""+olderId+"\"" +
                    ", \"istate\":\""+state+"\"}";
            infoNum++;
            if (infoNum==20){
                uploadHealthData();
                infoNum=0;
            }
            smsg="";
        }
    };


    @SuppressLint("MissingPermission")
    public void onConnectButtonClicked(View view) {
        if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_SHORT).show();
            _bluetooth.enable();
            return;
        }
        //如未连接设备则打开DeviceListActivity进行设备搜索
        if(_socket==null){
            Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
        }
    }

    public void onDisconnectionClicked(View view) {
        //关闭连接socket
        if(_socket!=null) {
            try {
                Toast.makeText(this, " 蓝牙断开", Toast.LENGTH_SHORT).show();

                bRun = false;
                //is.close();
                _socket.close();
                _socket = null;

            }
            catch (IOException e) {}
        }
    }

    public void onExitButtonClicked(View view) {
        //---安全关闭蓝牙连接再退出，避免报异常----//
        if(_socket!=null){
            //关闭连接socket
            try{
                bRun = false;
                //is.close();
                _socket.close();
                _socket = null;
            }catch(IOException e){}
        }
        finish();     //退出APP
    }
    public void onSettingClicked(View view){

        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        intent.putExtra("olderId",olderId);
        startActivity(intent);
    }


    public void onSendButtonClicked(View view) {
        int i=0;
        int n=0;

        if(_socket==null){
            Toast.makeText(this, "请先连接蓝牙模块", Toast.LENGTH_SHORT).show();
            return;
        }
        TextView edit0 = findViewById(R.id.setHeartrateMin);
        saveHeart[0]=Double.parseDouble(edit0.getText().toString());
        TextView edit1 = findViewById(R.id.setHeartrateMax);
        saveHeart[1]=Double.parseDouble(edit1.getText().toString());
        TextView edit2 = findViewById(R.id.setTempMin);
        saveTemp[0]=Double.parseDouble(edit2.getText().toString());
        TextView edit3 = findViewById(R.id.setTempMax);
        saveTemp[1]=Double.parseDouble(edit3.getText().toString());
        TextView edit4 = findViewById(R.id.setSpo2Min);
        saveBlood=Double.parseDouble(edit4.getText().toString());

        if(edit0.getText().length()==0){
            Toast.makeText(this, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if(edit1.getText().length()==0){
            Toast.makeText(this, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if(edit2.getText().length()==0){
            Toast.makeText(this, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if(edit3.getText().length()==0){
            Toast.makeText(this, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if(edit4.getText().length()==0){
            Toast.makeText(this, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流

            byte[] bos1 = edit0.getText().toString().getBytes();
            byte[] bos2 = edit1.getText().toString().getBytes();
            byte[] bos3 = edit2.getText().toString().getBytes();
            byte[] bos4 = edit3.getText().toString().getBytes();
            byte[] bos5 = edit4.getText().toString().getBytes();

            byte[] send_bos = new byte[150];

            send_bos[i++]='h';send_bos[i++]='e';send_bos[i++]='a';send_bos[i++]='r';
            send_bos[i++]='_';send_bos[i++]='m';send_bos[i++]='i';send_bos[i++]='n';
            send_bos[i++]=':';
            n = i;
            for(i=0;i<bos1.length;i++) {
                send_bos[n+i]=bos1[i];
            }
            n += i;
            send_bos[n++]=',';
            send_bos[n++]='h';send_bos[n++]='e';send_bos[n++]='a';send_bos[n++]='r';
            send_bos[n++]='_';send_bos[n++]='m';send_bos[n++]='a';send_bos[n++]='x';
            send_bos[n++]=':';

            for(i=0;i<bos2.length;i++) {
                send_bos[n+i]=bos2[i];
            }
            n += i;
            send_bos[n++]=',';
            send_bos[n++]='t';send_bos[n++]='e';send_bos[n++]='m';send_bos[n++]='p';
            send_bos[n++]='_';send_bos[n++]='m';send_bos[n++]='i';send_bos[n++]='n';
            send_bos[n++]=':';

            for(i=0;i<bos3.length;i++) {
                send_bos[n+i]=bos3[i];
            }
            n += i;
            send_bos[n++]=',';
            send_bos[n++]='t';send_bos[n++]='e';send_bos[n++]='m';send_bos[n++]='p';
            send_bos[n++]='_';send_bos[n++]='m';send_bos[n++]='a';send_bos[n++]='x';
            send_bos[n++]=':';

            for(i=0;i<bos4.length;i++) {
                send_bos[n+i]=bos4[i];
            }
            n += i;
            send_bos[n++]=',';
            send_bos[n++]='s';send_bos[n++]='p';send_bos[n++]='o';send_bos[n++]='2';
            send_bos[n++]='_';send_bos[n++]='m';send_bos[n++]='i';send_bos[n++]='n';
            send_bos[n++]=':';

            for(i=0;i<bos5.length;i++) {
                send_bos[n+i]=bos5[i];
            }
            n += i;

            send_bos[n++]=0x0d;
            send_bos[n++]=0x0a;

            os.write(send_bos);

        }catch(IOException e){
        }
    }

    public void uploadHealthData(){


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                    OkHttpClient client=new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.181.212:9001/phone/health")
                            .post(RequestBody.create(MediaType.parse("application/json"),internal))
                            .build();
                    Response response = client.newCall(request).execute();
                }catch (Exception e){
                    e.printStackTrace();

                }


            }
        });
        Thread thread1 =new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client1 = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.181.212:9001/phone/getPhone/"+olderId)
                            .get()
                            .build();
                    Response response = client1.newCall(request).execute();
                    phone=response.body().string();
                    System.out.println(phone);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread1.start();

    }
    // wifi下获取本地网络IP地址（局域网地址）
    public static String getLocalIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            @SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
            return ipAddress;
        }
        return "";
    }

    public void warning(){
        Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
        Ringtone mRingtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mRingtone.play();
    }

    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
            ((ip >> 8) & 0xFF) + "." +
            ((ip >> 16) & 0xFF) + "." +
            (ip >> 24 & 0xFF);
    }


    private void requestPermission() {
        //判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS);
                return;
            } else {
                sendSMSS();
                //已有权限
            }
        } else {
            //API 版本在23以下
        }
    }

    /**
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMSS();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "SEND_SMS Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //发送短信
    private void sendSMSS() {
        String content = "用户健康异常";
        if (!StringUtils.isEmpty(content) && !StringUtils.isEmpty(phone)) {
            SmsManager manager = SmsManager.getDefault();
            ArrayList<String> strings = manager.divideMessage(content);
            for (int i = 0; i < strings.size(); i++) {
                manager.sendTextMessage(phone, null, content, null, null);
            }
            Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "手机号或内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

    }


}

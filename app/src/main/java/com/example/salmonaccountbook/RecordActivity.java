package com.example.salmonaccountbook;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    public String ie = "";
    public String type = "";
    public String date = "";
    public double money = 0;
    public String remarks = "无备注";
    public String username = "";
    private TextView tv_record_date;
    private ImageView yuyin;
    private static final int RECORD_AUDIO = 1;
    private XunFeiUtil XunfeiUtil = new XunFeiUtil();

    private Calendar calendar;//用来装日期
    private DatePickerDialog dialog;


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);

        final EditText et_record_money = findViewById(R.id.et_record_money);
        final EditText et_record_remarks = findViewById(R.id.et_record_remarks);
        tv_record_date = findViewById(R.id.tv_record_date);
        Button btn_record_submit = findViewById(R.id.btn_record_submit);
        Button btn_record_cancel = findViewById(R.id.btn_record_cancel);

        final RadioGroup rg_ie = findViewById(R.id.rg_ie);
        final RadioGroup rg_type = findViewById(R.id.rg_type);
        rg_type.setOnCheckedChangeListener(this);
        rg_ie.setOnCheckedChangeListener(this);



        //语音听写
        LinearLayout layout_record = findViewById(R.id.layout_record);
        //获取语音控件
        yuyin = findViewById(R.id.yuyin);
        //申请录音权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},RECORD_AUDIO);
        }
        //初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5caca3ab");
        yuyin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XunfeiUtil.showSpeechDialog(RecordActivity.this, new XunFeiUtil.onRecognizerResult() {
                    @Override
                    public void onSuccess(String result) {
                        et_record_remarks.setText(result);// 设置输入框的文本
                        et_record_remarks.requestFocus(); //请求获取焦点
                        et_record_remarks.setSelection(et_record_remarks.length());//把光标定位末尾

                        Yuyin_Chuli yuyin_chuli = new Yuyin_Chuli(result);

                        if(yuyin_chuli.isincome()){
                            rg_ie.check(R.id.rb_income);
                        }else{
                            rg_ie.check(R.id.rb_expenditure);
                        }

                        if(yuyin_chuli.rtnType().equals("服装")){
                            rg_type.check(R.id.rb_fuzhuang);
                        }else if(yuyin_chuli.rtnType().equals("餐饮")){
                            rg_type.check(R.id.rb_canyin);
                        }else if(yuyin_chuli.rtnType().equals("出行")){
                            rg_type.check(R.id.rb_chuxing);
                        }else{
                            rg_type.check(R.id.rb_qita);
                        }
                        double m = yuyin_chuli.rtnMoney();
                        if(m!=Double.parseDouble("0")){
                            et_record_money.setText(String.valueOf(m));
                        }

                    }

                    @Override
                    public void onFaild(JSONException e) {
                        //UtilTools.toast(RecordActivity.this, "onFaild e=" + e);
                        Toast.makeText(RecordActivity.this, "onFaild e=" + e, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }
                });

            }
        });


        if(MainActivity.flag==0){
            layout_record.setBackgroundColor(Color.parseColor("#00ffffff"));
        }else if(MainActivity.flag==1){
            layout_record.setBackgroundColor(Color.parseColor("#58000000"));
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        LinearLayout ll_record_choosedate = findViewById(R.id.ll_record_choosedate);


        rg_type.setOnCheckedChangeListener(this);
        rg_ie.setOnCheckedChangeListener(this);



        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date0 = new Date(System.currentTimeMillis());
        date = simpleDateFormat.format(date0);
        tv_record_date.setText(date+" (点击可选择日期)");


        btn_record_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ll_record_choosedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                dialog = new DatePickerDialog(RecordActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                System.out.println("年-->" + year + "月-->"
                                        + monthOfYear + "日-->" + dayOfMonth);
                                tv_record_date.setText(String.valueOf(year*10000+(monthOfYear+1)*100+dayOfMonth));
                                date = String.valueOf(year*10000+(monthOfYear+1)*100+dayOfMonth);
                            }
                        }, calendar.get(Calendar.YEAR), calendar
                        .get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        btn_record_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(date.equals("")){
                    Toast.makeText(RecordActivity.this,"你还未选择日期！",Toast.LENGTH_LONG).show();
                }
                else if(ie.equals("")){
                    Toast.makeText(RecordActivity.this,"你还未选择收支！",Toast.LENGTH_LONG).show();
                }
                else  if(type.equals("")){
                    Toast.makeText(RecordActivity.this,"你还未选择类型！",Toast.LENGTH_LONG).show();
                }
                else if (et_record_money.getText().toString().equals("")){
                    Toast.makeText(RecordActivity.this,"你还未输入金额！",Toast.LENGTH_LONG).show();
                }
                else{
                    if(et_record_remarks.getText().toString().equals("")==false){
                        remarks = et_record_remarks.getText().toString();
                    }
                    username = LoginActivity.username;
                    money = Double.parseDouble(et_record_money.getText().toString());
                    List<Data> dataList = DataSupport.where("username = ? and date = ? and type = ? and ie = ? and money = ? and remarks = ?",username,date,type,ie,Double.toString(money),remarks).find(Data.class);
                    if (dataList.isEmpty()==false){
                        final AlertDialog dialog = new AlertDialog.Builder(RecordActivity.this)
                                .setTitle("提示")
                                .setMessage("你已经存储过至少一条一模一样的账目，如果删除其中一条的话，相同的几条账目会同时删除！（你可以更换备注来区别账目信息）确定要继续保存吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Data data = new Data();
                                        data.setIe(ie);
                                        data.setDate(Long.parseLong(date));
                                        data.setMoney(money);
                                        data.setType(type);
                                        data.setRemarks(remarks);
                                        data.setUsername(username);
                                        data.save();
                                        Toast.makeText(RecordActivity.this,"保存成功！",Toast.LENGTH_LONG).show();
                                        onBackPressed();
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                }).create();
                        dialog.show();
                    }
                    else{
                        Data data = new Data();
                        data.setIe(ie);
                        data.setDate(Long.parseLong(date));
                        data.setMoney(money);
                        data.setType(type);
                        data.setRemarks(remarks);
                        data.setUsername(username);
                        data.save();
                        Toast.makeText(RecordActivity.this,"保存成功！",Toast.LENGTH_LONG).show();
                        List<Data> dataList2 = DataSupport.where("username = ? and date = ? and ie = ?",username,date,"expenditure")
                                .find(Data.class);
                        double total2 = 0;
                        for(Data data1:dataList2){
                            total2 += data1.getMoney();
                        }
                        List<Person> people1 = DataSupport.where("username = ?",LoginActivity.username).find(Person.class);
                        Person person = people1.get(0);
                        String plan = person.getPlan();
                        if(plan!=null){
                            if(total2>=(Double.parseDouble(plan)/6)){
                                sendExpenditureExceptionMessage(total2,date);
                            }
                        }
                        onBackPressed();
                    }
                }
            }
        });
    }

    public void onCheckedChanged(RadioGroup group, int checkedId){
        switch (checkedId){
            case R.id.rb_income:
                ie = "income";
                break;
            case R.id.rb_expenditure:
                ie = "expenditure";
                break;
            case R.id.rb_fuzhuang:
                type = "服装";
                break;
            case R.id.rb_canyin:
                type = "餐饮";
                break;
            case R.id.rb_chuxing:
                type = "出行";
                break;
            case R.id.rb_qita:
                type = "其他";
                break;
        }
    }


    //异常支出
    public void sendExpenditureExceptionMessage(double total,String date){
        /**
         * 发送通知
         */
        Intent intent  = new Intent(this,DateActivity.class);
        intent.putExtra("date",date);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "default";
            String channelName = "默认通知";
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(IeActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.jp)
                .setContentTitle("检测到异常支出！")
                .setContentText("您在"+date+"这天共消费：￥"+total+"，点此查看详情")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(1, notification);
    }

}

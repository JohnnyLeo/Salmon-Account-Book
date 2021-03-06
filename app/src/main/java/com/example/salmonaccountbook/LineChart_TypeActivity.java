package com.example.salmonaccountbook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LineChart_TypeActivity extends AppCompatActivity {
    LineChart lineChart;
    String username = LoginActivity.username;
    Calendar calendar = Calendar.getInstance();
    int month = calendar.get(Calendar.MONTH)+1;
    int year = calendar.get(Calendar.YEAR);
    final long month_date_min = year*10000+month*100+1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linechart);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }

        Intent intent = getIntent();
        String name = intent.getStringExtra("type");

        lineChart = findViewById(R.id.linechart);

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i <= 31; i++) {
            long date = month_date_min;
            date += i;
            List<Data> dataList1 = DataSupport.where("username = ? and date = ? and type = ?", username, String.valueOf(date),name)
                    .find(Data.class);
            float total1 = 0;
            for (Data data : dataList1) {
                if(data.getIe().equals("income")){
                    total1 += data.getMoney();
                }else{
                    total1 -= data.getMoney();
                }

            }
            entries.add(new Entry(i + 1, total1));
        }
        LineDataSet set = new LineDataSet(entries, "Money");
        set.setColor(Color.parseColor("#00A600"));         //设置线条颜色
        set.setDrawValues(true);                                      //设置显示数据点值
        set.setValueTextColor(Color.parseColor("#46A3FF"));//设置显示值的字体颜色
        set.setValueTextSize(15);                                   //字体大小
        set.setLineWidth(2);                                        //线条宽度
        set.setCircleColor(Color.parseColor("#0080FF"));//圆点颜色

        LineData lineData = new LineData(set);
        lineChart.setData(lineData);
        lineChart.invalidate();                                                //刷新显示绘图
        lineChart.setBackgroundColor(Color.parseColor("#FFE6D9"));//背景颜色

    }


}

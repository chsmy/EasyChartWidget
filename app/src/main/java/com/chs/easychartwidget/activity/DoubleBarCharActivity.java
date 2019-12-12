package com.chs.easychartwidget.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.DoubleBarEntity;
import com.chs.easychartwidget.widget.DoubleBarChart;

import java.util.ArrayList;
import java.util.List;

public class DoubleBarCharActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_bar_char);
        DoubleBarChart doubleBarChart = findViewById(R.id.barchart);
        List<DoubleBarEntity> list = new ArrayList<>();
        list.add(new DoubleBarEntity("1月",1000,2000));
        list.add(new DoubleBarEntity("2月",1000,1500));
        list.add(new DoubleBarEntity("3月",1000,1600));
        list.add(new DoubleBarEntity("4月",1000,1800));
        list.add(new DoubleBarEntity("5月",1000,2400));
        list.add(new DoubleBarEntity("6月",1000,1200));
        list.add(new DoubleBarEntity("7月",1000,1300));
        list.add(new DoubleBarEntity("8月",1000,1500));
        list.add(new DoubleBarEntity("9月",1000,1700));
        list.add(new DoubleBarEntity("10月",1000,2000));
        doubleBarChart.setData(list, Color.parseColor("#6FC5F4"),Color.parseColor("#78DA9F"));
    }
}

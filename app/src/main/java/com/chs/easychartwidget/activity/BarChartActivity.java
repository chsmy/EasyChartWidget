package com.chs.easychartwidget.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.ChartEntity;
import com.chs.easychartwidget.widget.BarChart;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2016/9/6 16:07
 * 邮箱：657083984@qq.com
 */
public class BarChartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        BarChart barChart = (BarChart) findViewById(R.id.chart);
        List<ChartEntity> data = new ArrayList<>();
        for(int i =0;i<20;i++){
            data.add(new ChartEntity(String.valueOf(i), (float) (Math.random()*1000)));
        }
        barChart.setData(data);
        barChart.setOnItemBarClickListener(new BarChart.OnItemBarClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(BarChartActivity.this,"点击了："+position,Toast.LENGTH_SHORT).show();
            }
        });
    }
}

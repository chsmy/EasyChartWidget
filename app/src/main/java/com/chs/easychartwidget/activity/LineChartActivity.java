package com.chs.easychartwidget.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.ChartEntity;
import com.chs.easychartwidget.widget.LineChart;
import com.chs.easychartwidget.widget.LineChartNew;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2016/9/6 16:07
 * 邮箱：657083984@qq.com
 */
public class LineChartActivity extends AppCompatActivity {
    Handler mHandler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);
        LineChart lineChart = (LineChart) findViewById(R.id.chart);
        List<ChartEntity> data = new ArrayList<>();
        for(int i =0;i<20;i++){
            data.add(new ChartEntity("item"+String.valueOf(i), (float) (Math.random()*1000)));
        }
        lineChart.setData(data);
        lineChart.startAnimation(2000);
        final LineChartNew lineChartNew = (LineChartNew) findViewById(R.id.chart_1);
//        //模拟延时加载
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {

                List<ChartEntity> datas = new ArrayList<>();
                for(int i =0;i<50;i++){
                    datas.add(new ChartEntity(String.valueOf(i), (float) (Math.random()*1000)));
                }
                lineChartNew.setData(datas,false);
                lineChartNew.startAnimation(6000);
//            }
//        },100);

        final LineChartNew lineChartNew1 = (LineChartNew) findViewById(R.id.chart_2);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                List<ChartEntity> datas1 = new ArrayList<>();
                for(int i =0;i<20;i++){
                    datas1.add(new ChartEntity("item"+String.valueOf(i), (float) (Math.random()*1000)));
                }
                lineChartNew1.setData(datas1,true);
                lineChartNew1.startAnimation(6000);
            }
        },1000);

    }
}

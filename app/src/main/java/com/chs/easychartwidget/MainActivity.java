package com.chs.easychartwidget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chs.easychartwidget.activity.BarChartActivity;
import com.chs.easychartwidget.activity.HollowPieChartActivity;
import com.chs.easychartwidget.activity.HollowPieChartNewActivity;
import com.chs.easychartwidget.activity.LineChartActivity;
import com.chs.easychartwidget.activity.PieChartActivity;
import com.chs.easychartwidget.activity.ScaleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.btn_line_chart).setOnClickListener(this);
        findViewById(R.id.btn_bar_chart).setOnClickListener(this);
        findViewById(R.id.btn_path).setOnClickListener(this);
        findViewById(R.id.btn_path_pie).setOnClickListener(this);
        findViewById(R.id.btn_scale).setOnClickListener(this);
        findViewById(R.id.btn_path_pie_hollow).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch(view.getId()){
            case R.id.btn_line_chart:
                intent = new Intent(this, LineChartActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_bar_chart:
                intent = new Intent(this, BarChartActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_path:
                intent = new Intent(this, PieChartActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_path_pie:
                intent = new Intent(this, HollowPieChartActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_path_pie_hollow:
                intent = new Intent(this, HollowPieChartNewActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_scale:
                intent = new Intent(this, ScaleActivity.class);
                startActivity(intent);
                break;
        }
    }
}

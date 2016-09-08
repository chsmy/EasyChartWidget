package com.chs.easychartwidget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.chs.easychartwidget.activity.BarChartActivity;
import com.chs.easychartwidget.activity.LineChartActivity;

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
        }
    }
}

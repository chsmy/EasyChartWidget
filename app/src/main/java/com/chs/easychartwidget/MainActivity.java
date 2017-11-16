package com.chs.easychartwidget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chs.easychartwidget.activity.BarChartActivity;
import com.chs.easychartwidget.activity.CombineChartActivity;
import com.chs.easychartwidget.activity.HollowPieChartActivity;
import com.chs.easychartwidget.activity.HollowPieChartNewActivity;
import com.chs.easychartwidget.activity.LineChartActivity;
import com.chs.easychartwidget.activity.PieChartActivity;
import com.chs.easychartwidget.activity.ScaleActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toLineChart(View view) {
        Intent intent = new Intent(this, LineChartActivity.class);
        startActivity(intent);
    }

    public void toBarChart(View view) {
        Intent intent = new Intent(this, BarChartActivity.class);
        startActivity(intent);
    }

    public void toPieChart(View view) {
        Intent intent = new Intent(this, PieChartActivity.class);
        startActivity(intent);
    }

    public void toHollowPieChart(View view) {
        Intent intent = new Intent(this, HollowPieChartActivity.class);
        startActivity(intent);
    }

    public void toNewHollowPieChart(View view) {
        Intent intent = new Intent(this, HollowPieChartNewActivity.class);
        startActivity(intent);
    }

    public void toScaleView(View view) {
        Intent intent = new Intent(this, ScaleActivity.class);
        startActivity(intent);
    }

    public void toCombineChart(View view) {
        Intent intent = new Intent(this, CombineChartActivity.class);
        startActivity(intent);
    }
}

package com.chs.easychartwidget.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.widget.CirclePercentView;
import com.chs.easychartwidget.widget.PercentViewTow;

public class PercentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_percent);
        PercentViewTow percent = findViewById(R.id.percent);
        PercentViewTow percent1 = findViewById(R.id.percent1);
        percent.setScales(0.6f);

        percent1.setScales(0.6f);
        percent1.setGradient(true);

        CirclePercentView circlePercentView = findViewById(R.id.circle_percent);
        CirclePercentView circlePercentView1 = findViewById(R.id.circle_percent1);

        circlePercentView.setPercentage(50f);

        circlePercentView1.setPercentage(50f);
        circlePercentView1.setGradient(false);
    }
}

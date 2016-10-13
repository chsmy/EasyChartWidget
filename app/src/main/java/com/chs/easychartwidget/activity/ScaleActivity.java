package com.chs.easychartwidget.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.widget.ScaleView;

/**
 * 作者：chs on 2016/9/6 16:07
 * 邮箱：657083984@qq.com
 */
public class ScaleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale);
        ScaleView scaleView = (ScaleView) findViewById(R.id.scale_view);
        //实际显示是跟传入的数值反序
        scaleView.setScales(new double[]{0.4, 0.3, 0.15, 0.15});
    }
}

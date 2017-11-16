package com.chs.easychartwidget.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.BarChartBean;
import com.chs.easychartwidget.widget.CombineChart;

import java.util.ArrayList;
import java.util.List;

public class CombineChartActivity extends AppCompatActivity implements View.OnClickListener {
    CombineChart combineChart;
    String[] rightYLabels;
    String[] rightYLabels1;
    boolean change = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine_chart);
        combineChart = (CombineChart) findViewById(R.id.cb_chart);
        rightYLabels = new String[]{"0级", "5级", "10级", "%0rh", "50%rh", "100%rh", "-50" + getString(R.string.degree_centigrade),
                "0" + getString(R.string.degree_centigrade), "50" + getString(R.string.degree_centigrade)};
        rightYLabels1 = new String[]{"%0rh", "50%rh", "100%rh", "-50" + getString(R.string.degree_centigrade),
                "0" + getString(R.string.degree_centigrade), "50" + getString(R.string.degree_centigrade)};
        List<BarChartBean> data = new ArrayList<>();
        data.add(new BarChartBean("7/1", 1003, 500, 600));
        data.add(new BarChartBean("7/2", 890, 456, 123));
        data.add(new BarChartBean("7/3", 456, 741, 654));
        data.add(new BarChartBean("7/4", 258, 951, 12));
        data.add(new BarChartBean("7/5", 863, 45, 99));
        data.add(new BarChartBean("7/6", 357, 235, 456));
        data.add(new BarChartBean("7/7", 452, 321, 55));
        data.add(new BarChartBean("7/8", 654, 555, 666));
        data.add(new BarChartBean("7/9", 321, 333, 222));
        data.add(new BarChartBean("7/10", 846, 111, 444));
        List<Float> winds = new ArrayList<>();
        winds.add(5f);
        winds.add(6f);
        winds.add(8f);
        winds.add(9f);
        winds.add(4f);
        winds.add(7f);
        winds.add(3f);
        winds.add(1f);
        winds.add(5.5f);
        winds.add(4.8f);
        List<Float> hum = new ArrayList<>();
        hum.add(50f);
        hum.add(60f);
        hum.add(80f);
        hum.add(90f);
        hum.add(40f);
        hum.add(70f);
        hum.add(30f);
        hum.add(10f);
        hum.add(55f);
        hum.add(48f);
        List<Float> tem = new ArrayList<>();
        tem.add(38f);
        tem.add(36f);
        tem.add(27f);
        tem.add(22f);
        tem.add(15f);
        tem.add(-20f);
        tem.add(-30f);
        tem.add(-40f);
        tem.add(10f);
        tem.add(18f);

        combineChart.setLeftYAxisLabels("kwh");
        combineChart.setItems(data, winds, hum, tem, rightYLabels);
        combineChart.setOnItemBarClickListener(new CombineChart.OnItemBarClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(CombineChartActivity.this, "点击了：" + position, Toast.LENGTH_SHORT).show();
            }
        });
        combineChart.startAnimation(2000);
        findViewById(R.id.btn_change).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change:
                if(change){
                    combineChart.setRightYLabels(rightYLabels1);
                    change = !change;
                }else {
                    combineChart.setRightYLabels(rightYLabels);
                    change = !change;
                }
                break;
        }
    }
}

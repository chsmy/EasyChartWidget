package com.chs.easychartwidget.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.BarChartEntity;
import com.chs.easychartwidget.widget.BarAndLineChart;

import java.util.ArrayList;
import java.util.List;

import static com.chs.easychartwidget.widget.BarAndLineChart.TEXT_TYPE_HORIZONTAL;
import static com.chs.easychartwidget.widget.BarAndLineChart.TEXT_TYPE_SLANTING;

/**
 * @author chs
 *
 */
public class BarAndLineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_and_line);
        //新柱状图多种颜色
        BarAndLineChart barChartNew2 = findViewById(R.id.chart_new_2);
        BarAndLineChart barChartNew3 = findViewById(R.id.chart_new_3);
        List<BarChartEntity> datas1 = new ArrayList<>();

        datas1.add(new BarChartEntity("暖通组", new Float[]{1003f, 500f, 600f}));
        datas1.add(new BarChartEntity("暖通组", new Float[]{890f, 456f, 123f}));
        datas1.add(new BarChartEntity("宗秀组", new Float[]{456f, 741f, 654f}));
        datas1.add(new BarChartEntity("粽球组", new Float[]{258f, 951f, 12f}));
        datas1.add(new BarChartEntity("弱点组", new Float[]{863f, 45f, 99f}));
        datas1.add(new BarChartEntity("强电组", new Float[]{357f, 235f, 456f}));
        datas1.add(new BarChartEntity("电器组", new Float[]{452f, 321f, 55f}));
        datas1.add(new BarChartEntity("保洁组", new Float[]{321f, 333f, 222f}));
        datas1.add(new BarChartEntity("暖通组", new Float[]{654f, 555f, 666f}));
        datas1.add(new BarChartEntity("保安组", new Float[]{846f, 111f, 444f}));

        barChartNew2.setOnItemBarClickListener(new BarAndLineChart.OnItemBarClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(BarAndLineActivity.this,"点击了："+position,Toast.LENGTH_SHORT).show();
            }
        });
        List<List<Float>> rightDatas = new ArrayList<>();
        List<Float> tem = new ArrayList<>();
        tem.add(38f);
        tem.add(36f);
        tem.add(27f);
        tem.add(22f);
        tem.add(15f);
        tem.add(60f);
        tem.add(80f);
        tem.add(15f);
        tem.add(10f);
        tem.add(5f);
        List<Float> hm = new ArrayList<>();
        hm.add(15f);
        hm.add(25f);
        hm.add(27f);
        hm.add(35f);
        hm.add(90f);
        hm.add(60f);
        hm.add(55f);
        hm.add(40f);
        hm.add(45f);
        hm.add(33f);
        rightDatas.add(tem);
        rightDatas.add(hm);
        barChartNew2.setData(datas1,
                new int[]{Color.parseColor("#6FC5F4"),Color.parseColor("#78DA9F"),Color.parseColor("#FCAE84")}
                ,"分组","数量",rightDatas,TEXT_TYPE_HORIZONTAL,new int[]{100,0});
        barChartNew2.startAnimation();



        List<List<Float>> rightDatas2 = new ArrayList<>();
        List<BarChartEntity> datas2 = new ArrayList<>();

        datas2.add(new BarChartEntity("暖通组暖通组", new Float[]{1003f, 500f, 600f}));
        datas2.add(new BarChartEntity("暖通组暖通组", new Float[]{890f, 456f, 123f}));
        datas2.add(new BarChartEntity("宗秀组宗秀组", new Float[]{456f, 741f, 654f}));
        datas2.add(new BarChartEntity("粽球组粽球组", new Float[]{258f, 951f, 12f}));
        datas2.add(new BarChartEntity("弱点组弱点组", new Float[]{863f, 45f, 99f}));
        datas2.add(new BarChartEntity("强电组暖通组", new Float[]{357f, 235f, 456f}));
        datas2.add(new BarChartEntity("暖通组暖通组", new Float[]{452f, 321f, 55f}));
        datas2.add(new BarChartEntity("保洁组保洁组", new Float[]{321f, 333f, 222f}));
        datas2.add(new BarChartEntity("暖通组暖通组", new Float[]{654f, 555f, 666f}));
        datas2.add(new BarChartEntity("保安组保安组", new Float[]{846f, 111f, 444f}));
        rightDatas2.add(tem);
        barChartNew3.setData(datas2,
                new int[]{Color.parseColor("#6FC5F4"),Color.parseColor("#78DA9F"),Color.parseColor("#FCAE84")}
                ,"分组","数量",rightDatas2,TEXT_TYPE_SLANTING);
        barChartNew3.startAnimation();
    }
}

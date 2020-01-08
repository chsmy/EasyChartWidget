package com.chs.easychartwidget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chs.easychartwidget.activity.BarAndLineActivity;
import com.chs.easychartwidget.activity.BarChartActivity;
import com.chs.easychartwidget.activity.CombineChartActivity;
import com.chs.easychartwidget.activity.DoubleBarCharActivity;
import com.chs.easychartwidget.activity.HollowPieChartActivity;
import com.chs.easychartwidget.activity.HollowPieChartNewActivity;
import com.chs.easychartwidget.activity.LineChartActivity;
import com.chs.easychartwidget.activity.PieChartActivity;
import com.chs.easychartwidget.activity.ScaleActivity;
import com.chs.easychartwidget.entity.MainBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private List<MainBean> mList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        setRecyclerView();
    }

    private void initData() {
        mList.add(new MainBean("线性图",new Intent(this, LineChartActivity.class)));
        mList.add(new MainBean("柱状图",new Intent(this, BarChartActivity.class)));
        mList.add(new MainBean("双柱状图",new Intent(this, DoubleBarCharActivity.class)));
        mList.add(new MainBean("饼状图",new Intent(this, PieChartActivity.class)));
        mList.add(new MainBean("空心饼",new Intent(this, HollowPieChartActivity.class)));
        mList.add(new MainBean("空心饼1",new Intent(this, HollowPieChartNewActivity.class)));
        mList.add(new MainBean("直线比例",new Intent(this, ScaleActivity.class)));
        mList.add(new MainBean("组合图",new Intent(this, CombineChartActivity.class)));
        mList.add(new MainBean("线和柱",new Intent(this, BarAndLineActivity.class)));
    }

    private void setRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        mRecyclerView.setLayoutManager(layoutManager);
        BaseQuickAdapter adapter = new BaseQuickAdapter<MainBean,BaseViewHolder>(R.layout.item_main,mList) {
            @Override
            protected void convert(BaseViewHolder helper, MainBean item) {
                helper.setText(R.id.tv_name,item.getName());
            }
        };
        mRecyclerView.setAdapter(adapter);
        setEvent(adapter);
    }
    private void setEvent(BaseQuickAdapter adapter) {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                startActivity(mList.get(position).getIntent());
            }
        });
    }
}

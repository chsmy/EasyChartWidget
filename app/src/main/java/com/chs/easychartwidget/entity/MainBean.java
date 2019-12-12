package com.chs.easychartwidget.entity;

import android.content.Intent;

/**
 * @author chs
 * date: 2019-12-12 17:37
 * des:
 */
public class MainBean {
    private String name;
    private Intent mIntent;

    public MainBean(String name, Intent intent) {
        this.name = name;
        mIntent = intent;
    }

    public String getName() {
        return name;
    }

    public Intent getIntent() {
        return mIntent;
    }
}

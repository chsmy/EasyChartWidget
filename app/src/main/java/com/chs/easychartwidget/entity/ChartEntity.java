package com.chs.easychartwidget.entity;

/**
 * 作者：chs on 2016/9/6 15:14
 * 邮箱：657083984@qq.com
 */
public class ChartEntity {
    private String xLabel;
    private Float yValue;

    public ChartEntity(String xLabel, Float yValue) {
        this.xLabel = xLabel;
        this.yValue = yValue;
    }

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public ChartEntity(Float yValue) {
        this.yValue = yValue;
    }

    public Float getyValue() {
        return yValue;
    }

    public void setyValue(Float yValue) {
        this.yValue = yValue;
    }
}

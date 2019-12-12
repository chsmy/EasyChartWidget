package com.chs.easychartwidget.entity;

/**
 * 作者：chs on 2016/7/6 10:19
 * 邮箱：657083984@qq.com
 */
public class BarChartBean {
    private String xLabel;
    private float yNum;
    private float yNum1;
    private float yNum2;
    private int barColor[];

    public BarChartBean() {

    }

    public BarChartBean(String xLabel, float yNum) {
        this.xLabel = xLabel;
        this.yNum = yNum;
    }

    public BarChartBean(String xLabel, float yNum, float yNum1, float yNum2) {
        this.xLabel = xLabel;
        this.yNum = yNum;
        this.yNum1 = yNum1;
        this.yNum2 = yNum2;
    }

    public float getyNum1() {
        return yNum1;
    }

    public float getyNum2() {
        return yNum2;
    }

    public String getxLabel() {
        return xLabel;
    }

    public float getyNum() {
        return yNum;
    }

    public int[] getBarColor() {
        return barColor;
    }

    public void setBarColor(int[] barColor) {
        this.barColor = barColor;
    }
}

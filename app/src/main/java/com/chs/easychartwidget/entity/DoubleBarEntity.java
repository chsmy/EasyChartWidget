package com.chs.easychartwidget.entity;

/**
 * @author chs
 * date: 2019-12-12 16:17
 * des:
 */
public class DoubleBarEntity {
    private String xLabel;
    private float leftNum;
    private float rightNum;

    public DoubleBarEntity(String xLabel, float lertNum, float rightNum) {
        this.xLabel = xLabel;
        this.leftNum = lertNum;
        this.rightNum = rightNum;
    }

    public String getxLabel() {
        return xLabel;
    }

    public float getLeftNum() {
        return leftNum;
    }

    public float getRightNum() {
        return rightNum;
    }
}

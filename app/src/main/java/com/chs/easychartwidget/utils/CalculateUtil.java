package com.chs.easychartwidget.utils;

import android.content.Context;
import android.graphics.Paint;

import java.math.BigDecimal;

/**
 * 作者：chs on 2016/9/8 10:02
 * 邮箱：657083984@qq.com
 * 计算 工具类
 */
public class CalculateUtil {

    /**
     * 获取这个最大数 数总共有几位
     *
     * @param value
     * @return
     */
    public  static int getScale(float value) {
        if (value >= 1 && value < 10) {
            return 0;
        }
        if (value == 0) {
            return 0;
        }
        if (value >= 10) {
            return 1 + getScale(value / 10);
        } else {
            return getScale(value * 10) - 1;
        }
    }

    public static float getRangeTop(float value) {
        //value: [1,10)
        if (value < 1.2) {
            return 1.2f;
        }

        if (value < 1.5) {
            return 1.5f;
        }

        if (value < 2.0) {
            return 2.0f;
        }

        if (value < 3.0) {
            return 3.0f;
        }

        if (value < 4.0) {
            return 4.0f;
        }

        if (value < 5.0) {
            return 5.0f;
        }

        if (value < 6.0) {
            return 6.0f;
        }

        if (value < 8.0) {
            return 8.0f;
        }

        return 10.0f;
    }
    /**
     * 数字的乘法精度计算
     */
    public static float numMathMul(float d1, float d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        float res = b1.multiply(b2).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        return res;
    }
    public static BigDecimal add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }


    public static BigDecimal mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数
        //除不尽的情况
    }
    /**
     * 得到最大宽度值得文本
     *
     * @param maxDivisionValue
     * @return
     */
    public static float getDivisionTextMaxWidth(float maxDivisionValue, Context context) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(DensityUtil.dip2px(context, 10));
        BigDecimal bigDecimal = new BigDecimal(maxDivisionValue);
        float max = textPaint.measureText(String.valueOf(bigDecimal.intValue()));
        for (int i = 2; i <= 10; i++) {
            if (maxDivisionValue * 0.1 >= 1) {
                //当数字非常大的时候会出现精度丢失的情况 所以候使用BigDecimal做运算
                BigDecimal bd = new BigDecimal(maxDivisionValue);
                BigDecimal fen = new BigDecimal(0.1 * i);
                String text = String.valueOf(bd.multiply(fen).longValue());
                float w = textPaint.measureText(text);
                if (w > max) {
                    max = w;
                }
            } else {
                max = textPaint.measureText(String.valueOf(maxDivisionValue * 10));
            }
        }
        return max;
    }

    /**
     * 提供精确的小数位四舍五入处理。
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal ne = new BigDecimal("1");
        return b.divide(ne, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}

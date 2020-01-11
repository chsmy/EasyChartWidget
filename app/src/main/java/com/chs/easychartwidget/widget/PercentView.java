package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author chs
 */
public class PercentView extends View {

    private Path mPathLeft = new Path();
    private Path mPathRight = new Path();
    private Paint mPaint;
    private float mWidth = 0f;
    private float mHeight = 0f;
    /**
     * 看涨的百分比
     */
    private float left = 0.5f;
    private float mid = 0.2f;
    private float right = 0.3f;
    /**
     * 是否绘制中间
     */
    private boolean isDrawMid = true;

    public PercentView(Context context) {
        this(context, null);
    }

    public PercentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mHeight = h;
        this.mWidth = w;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mPathLeft.reset();
        mPathRight.reset();
        //左边
        mPaint.setColor(Color.GREEN);
        setLadderLeftDraw();
        canvas.drawPath(mPathLeft, mPaint);
        mPathLeft.close();

        //中间
        if(isDrawMid){
            RectF midRect = new RectF(mWidth*left, 0f, mWidth * (left+mid), mHeight);
            mPaint.setColor(Color.GRAY);
            canvas.drawRect(midRect,mPaint);
        }

        //右边
        mPaint.setColor(Color.RED);
        setLadderRightDraw();
        mPathRight.close();

        canvas.drawPath(mPathRight, mPaint);
    }

    public void setLadderLeftDraw() {
        //设置将要用来画扇形的矩形的轮廓
        float r = mHeight / 2;
        //先画半圆
        RectF roundRect = new RectF(0f, 0f, mWidth * left, mHeight);
        float[] array = {r, r, 0f, 0f, 0f, 0f, r, r};
        mPathLeft.addRoundRect(roundRect, array, Path.Direction.CCW);
    }

    public void setLadderRightDraw() {
        float r = mHeight / 2;
        RectF roundRect = new RectF(mWidth * (left+mid), 0f, mWidth, mHeight);
        float[] array = {0f, 0f, r, r, r, r, 0f, 0f};
        mPathRight.addRoundRect(roundRect, array, Path.Direction.CCW);
    }
}
package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.chs.easychartwidget.R;


/**
 * @author chs
 * 百分比
 */
public class PercentViewTow extends View {

    private Paint mPaint;
    private float mWidth = 0f;
    private float mHeight = 0f;
    private RectF mRectAll;
    private RectF mRectPercent;
    private float mPercent;
    private boolean isGradient;
    private LinearGradient mGradient;
    private int mRadius;
    public PercentViewTow(Context context) {
        this(context, null);
    }

    public PercentViewTow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentViewTow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mRectAll = new RectF(0,0,0, mHeight);
        mRectPercent = new RectF(0,0,0, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mHeight = h;
        this.mWidth = w;
        mRectAll.bottom = h;
        mRectPercent.bottom = h;
        mRadius = h/2;
        mGradient = new LinearGradient(0, getHeight(), getWidth()*mPercent, getHeight(), ContextCompat.getColor(getContext(), R.color.blue),
                ContextCompat.getColor(getContext(), R.color.green), Shader.TileMode.MIRROR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRectAll.right = mWidth;
        mRectPercent.right = mWidth * mPercent;
        mPaint.setShader(null);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.axis));
        canvas.drawRoundRect(mRectAll, mRadius,mRadius,mPaint);
        if(isGradient){
            mPaint.setShader(mGradient);
        }else {
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.blue));
        }
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.blue));
        canvas.drawRoundRect(mRectPercent, mRadius,mRadius,mPaint);
    }

    /**
     * 给数据赋值
     */
    public void setScales(float percent){
        mPercent = percent;
        invalidate();
    }

    public void setGradient(boolean gradient) {
        isGradient = gradient;
    }
}
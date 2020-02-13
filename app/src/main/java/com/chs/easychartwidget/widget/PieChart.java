package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.chs.easychartwidget.entity.PieDataEntity;
import com.chs.easychartwidget.utils.CalculateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作者：chs on 2016/9/8 16:25
 * 邮箱：657083984@qq.com
 * 饼状图表
 */
public class PieChart extends View {

    public static final int TOUCH_OFFSET = 16;
    private int mTotalWidth, mTotalHeight;
    private float mRadius;
    private Paint mPaint, mLinePaint, mTextPaint;

    private Path mPath;
    /**
     * 扇形的绘制区域
     */
    private RectF mRectF;
    /**
     * 点击之后的扇形的绘制区域
     */
    private RectF mRectFTouch;

    private List<PieDataEntity> mDataList;
    /**
     * 所有的数据加起来的总值
     */
    private float mTotalValue;
    /**
     * 手点击的部分的position
     */
    private int position = -1;
    /**
     * 点击监听
     */
    private OnItemPieClickListener mOnItemPieClickListener;
    private List<Region> mRegions = new ArrayList<>();
    /**
     * 点击某一块之后再次点击回复原状
     */
    private int lastClickedPosition = -1;
    private boolean lastPositionClicked = false;

    public void setOnItemPieClickListener(OnItemPieClickListener onItemPieClickListener) {
        mOnItemPieClickListener = onItemPieClickListener;
    }

    public interface OnItemPieClickListener {
        void onClick(int position);
    }

    public PieChart(Context context) {
        this(context, null);
    }

    public PieChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRectF = new RectF();
        mRectFTouch = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(Color.BLACK);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(24);

        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();

        mRadius = (float) (Math.min(mTotalWidth, mTotalHeight) / 2 * 0.7);

        mRectF.left = -mRadius;
        mRectF.top = -mRadius;
        mRectF.right = mRadius;
        mRectF.bottom = mRadius;

        mRectFTouch.left = -mRadius - TOUCH_OFFSET;
        mRectFTouch.top = -mRadius - TOUCH_OFFSET;
        mRectFTouch.right = mRadius + TOUCH_OFFSET;
        mRectFTouch.bottom = mRadius + TOUCH_OFFSET;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDataList == null)
            return;
        canvas.translate(mTotalWidth / 2, mTotalHeight / 2);
        //绘制饼图的每块区域
        drawPiePath(canvas);
    }

    private float percent = 0f;
    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();

    public void startAnimation(int duration) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(pointInterpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                percent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * 绘制饼图的每块区域 和文本
     *
     * @param canvas
     */
    private void drawPiePath(Canvas canvas) {
        //起始地角度
        float startAngle = 0;
        mRegions.clear();
        for (int i = 0; i < mDataList.size(); i++) {
            float sweepAngle = mDataList.get(i).getValue() / mTotalValue * 360 - 1;//每个扇形的角度
            sweepAngle = sweepAngle * percent;
            mPaint.setColor(mDataList.get(i).getColor());
            mLinePaint.setColor(mDataList.get(i).getColor());
            mTextPaint.setColor(mDataList.get(i).getColor());
            //*******下面的两种方法选其一就可以 一个是通过画路径来实现 一个是直接绘制扇形***********
            mPath.moveTo(0, 0);
            if (position == i) {
                if (lastClickedPosition == position && lastPositionClicked) {
                    mPath.arcTo(mRectFTouch, startAngle, sweepAngle);
                } else {
                    mPath.arcTo(mRectF, startAngle, sweepAngle);
                }
            } else {
                mPath.arcTo(mRectF, startAngle, sweepAngle);
            }
            RectF r = new RectF();
            mPath.computeBounds(r, true);
            Region region = new Region();
            region.setPath(mPath, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
            mRegions.add(region);
            canvas.drawPath(mPath, mPaint);
            mPath.reset();
//            if(position-1==i){
//                canvas.drawArc(mRectFTouch,startAngle,sweepAngle,true,mPaint);
//            }else {
//                canvas.drawArc(mRectF,startAngle,sweepAngle,true,mPaint);
//            }
            Log.i("toRadians", (startAngle + sweepAngle / 2) + "****" + Math.toRadians(startAngle + sweepAngle / 2));
            //确定直线的起始和结束的点的位置
            float pxs = (float) (mRadius * Math.cos(Math.toRadians(startAngle + sweepAngle / 2)));
            float pys = (float) (mRadius * Math.sin(Math.toRadians(startAngle + sweepAngle / 2)));
            float pxt = (float) ((mRadius + 30) * Math.cos(Math.toRadians(startAngle + sweepAngle / 2)));
            float pyt = (float) ((mRadius + 30) * Math.sin(Math.toRadians(startAngle + sweepAngle / 2)));
            startAngle += sweepAngle + 1;
            //绘制线和文本
            canvas.drawLine(pxs, pys, pxt, pyt, mLinePaint);
            float res = mDataList.get(i).getValue() / mTotalValue * 100;
            //提供精确的小数位四舍五入处理。
            double resToRound = CalculateUtil.round(res, 2);
            float v = startAngle % 360;
            if (startAngle % 360.0 >= 90.0 && startAngle % 360.0 <= 270.0) {//2 3 象限
                canvas.drawLine(pxt, pyt, pxt - 30, pyt, mLinePaint);
                canvas.drawText(resToRound + "%", pxt - mTextPaint.measureText(resToRound + "%") - 30, pyt, mTextPaint);
            } else {
                canvas.drawLine(pxt, pyt, pxt + 30, pyt, mLinePaint);
                canvas.drawText(resToRound + "%", pxt + 30, pyt, mTextPaint);
            }
        }

    }

    public void setDataList(List<PieDataEntity> dataList) {
        this.mDataList = dataList;
        mTotalValue = 0;
        for (PieDataEntity pieData : mDataList) {
            mTotalValue += pieData.getValue();
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX() - (mTotalWidth / 2f);
                float y = event.getY() - (mTotalHeight / 2f);
                for (int i = 0; i < mRegions.size(); i++) {
                    Region region = mRegions.get(i);
                    if (region.contains((int) x, (int) y)) {
                        position = i;
                        break;
                    }
                }
                if (lastClickedPosition == position) {
                    lastPositionClicked = !lastPositionClicked;
                } else {
                    lastPositionClicked = true;
                    lastClickedPosition = position;
                }
                invalidate();
                if (mOnItemPieClickListener != null) {
                    mOnItemPieClickListener.onClick(position);
                }
                break;
            default:
        }
        return super.onTouchEvent(event);
    }
}

package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.PieDataEntity;
import com.chs.easychartwidget.utils.CalculateUtil;
import com.chs.easychartwidget.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2016/9/8 16:25
 * 邮箱：657083984@qq.com
 * 饼状图表
 */
public class PieChart extends View {

    public final int TOUCH_OFFSET = DensityUtil.dip2px(getContext(), 5);
    private int mTotalWidth, mTotalHeight;
    private float mRadius;
    private float mVerticalLineSize = DensityUtil.dip2px(getContext(), 20);
    private float mHorizontalLineSize = DensityUtil.dip2px(getContext(), 40);
    private float mLineOffset = DensityUtil.dip2px(getContext(), 8);
    private float mTextOffset = DensityUtil.dip2px(getContext(), 3);
    private Paint mPaint, mLinePaint, mTextPaint,mMiddlePaint;
    private Rect mTextRect = new Rect();
    /**
     * 是否是空心
     */
    private boolean isHollow = false;
    /**
     * 是否显示中间的文字
     */
    private boolean isShowMiddleText = false;

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

    public void setHollow(boolean hollow) {
        isHollow = hollow;
    }

    public void setShowMiddleText(boolean showMiddleText) {
        isShowMiddleText = showMiddleText;
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
        mTextPaint.setTextSize(DensityUtil.dip2px(getContext(),8));

        mMiddlePaint = new Paint();
        mMiddlePaint.setAntiAlias(true);
        mMiddlePaint.setStyle(Paint.Style.FILL);
        mMiddlePaint.setTextSize(24);

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
        canvas.translate(mTotalWidth >> 1, mTotalHeight >> 1);
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
            //每个扇形的角度
            float sweepAngle = mDataList.get(i).getValue() / mTotalValue * 360 - 1;
            sweepAngle = sweepAngle * percent;
            mPaint.setColor(mDataList.get(i).getColor());
            mLinePaint.setColor(mDataList.get(i).getColor());
            mTextPaint.setColor(mDataList.get(i).getColor());
            //*******下面的两种方法选其一就可以 一个是通过画路径来实现 一个是直接绘制扇形***********
            //第一种 绘制path
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
            //第二种绘制扇形
//            if(position-1==i){
//                canvas.drawArc(mRectFTouch,startAngle,sweepAngle,true,mPaint);
//            }else {
//                canvas.drawArc(mRectF,startAngle,sweepAngle,true,mPaint);
//            }
            Log.i("toRadians", (startAngle + sweepAngle / 2) + "****" + Math.toRadians(startAngle + sweepAngle / 2));
            //确定直线的起始和结束的点的位置
            float pxs = ((mRadius + mLineOffset) * mathCos(startAngle + sweepAngle / 2));
            float pys = ((mRadius + mLineOffset) * mathSin(startAngle + sweepAngle / 2));
            float pxe = ((mRadius + mVerticalLineSize) * mathCos(startAngle + sweepAngle / 2));
            float pye = ((mRadius + mVerticalLineSize) * mathSin(startAngle + sweepAngle / 2));
            startAngle += sweepAngle + 1;

            canvas.drawCircle(pxs, pys, DensityUtil.dip2px(getContext(), 2), mLinePaint);
            canvas.drawLine(pxs, pys, pxe, pye, mLinePaint);

            float res = mDataList.get(i).getValue() / mTotalValue * 100;
            String name = mDataList.get(i).getName();
            //提供精确的小数位四舍五入处理。
            double resToRound = CalculateUtil.round(res, 2);
            //2 3 象限
            if (startAngle % 360.0 >= 90.0 && startAngle % 360.0 <= 270.0) {
                canvas.drawLine(pxe, pye, pxe - mHorizontalLineSize, pye, mLinePaint);
                canvas.drawText(resToRound + "%", pxe - mTextPaint.measureText(resToRound + "%"),
                        pye + mTextOffset + getTextHeight(String.valueOf(resToRound)), mTextPaint);
                canvas.drawText(name, pxe - mTextPaint.measureText(name),
                        pye + mTextOffset - getTextHeight(String.valueOf(resToRound)), mTextPaint);
            } else {
                canvas.drawLine(pxe, pye, pxe + mHorizontalLineSize, pye, mLinePaint);
                canvas.drawText(resToRound + "%", pxe + (mHorizontalLineSize / 2 - mTextPaint.measureText(resToRound + "%") / 2),
                        pye + mTextOffset + getTextHeight(String.valueOf(resToRound)), mTextPaint);
                canvas.drawText(name, pxe + (mHorizontalLineSize / 2 - mTextPaint.measureText(name) / 2),
                        pye + mTextOffset - getTextHeight(String.valueOf(resToRound)), mTextPaint);
            }
        }

        if (isHollow) {
            drawHollow(canvas);
        }

    }

    private void drawHollow(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(50);
        canvas.drawCircle(0, 0, mRadius / 5 * 3 + DensityUtil.dip2px(getContext(), 5), mPaint);
        mPaint.setAlpha(255);
        canvas.drawCircle(0, 0, mRadius / 5 * 3, mPaint);
        if(isShowMiddleText){
            mMiddlePaint.setTextSize(DensityUtil.dip2px(getContext(),16));
            mMiddlePaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
            canvas.drawText(String.valueOf((int) mTotalValue), 0 - mMiddlePaint.measureText(String.valueOf((int) mTotalValue))/2, 0, mMiddlePaint);
            mMiddlePaint.setTextSize(DensityUtil.dip2px(getContext(),12));
            mMiddlePaint.setColor(ContextCompat.getColor(getContext(), R.color.gray));
            canvas.drawText("工单数量", 0 - mMiddlePaint.measureText("工单数量")/2,  DensityUtil.dip2px(getContext(),16), mMiddlePaint);
        }
    }

    private float getTextHeight(String text) {
        mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
        return mTextRect.height();
    }

    private float mathCos(double angdeg) {
        return (float) Math.cos(Math.toRadians(angdeg));
    }

    private float mathSin(double angdeg) {
        return (float) Math.sin(Math.toRadians(angdeg));
    }

    public void setDataList(List<PieDataEntity> dataList) {
        this.mDataList = dataList;
        mTotalValue = 0;
        for (PieDataEntity pieData : mDataList) {
            mTotalValue += pieData.getValue();
        }
        invalidate();
    }

    /**
     * 这里使用Region来确定点击的位置
     * 在{@link HollowPieChart } {@link HollowPieNewChart}中使用角度的方式来判断点击的位置
     */
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

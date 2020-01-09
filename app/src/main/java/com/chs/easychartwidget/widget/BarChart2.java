package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.BarChartEntity;
import com.chs.easychartwidget.utils.CalculateUtil;
import com.chs.easychartwidget.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 作者：chs on 2018-04-09 15:39
 * 邮箱：657083984@qq.com
 */
public class BarChart2 extends View {
    private Context mContext;
    /**
     * 视图的宽和高  刻度区域的最大值
     */
    private int mTotalWidth, mTotalHeight, mMaxHeight;
    private int mPaddingRight, mPaddingBottom, mPaddingTop;
    /**
     * 柱形图的颜色集合
     */
    private int[] mBarColors;
    /**
     * 距离底部的多少 用来显示底部的文字
     */
    private int mBottomMargin;
    /**
     * 距离顶部的多少 用来显示顶部的文字
     */
    private int mTopMargin;
    private int mRightMargin;
    private int mLeftMargin;
    /**
     * 画笔 轴 刻度 柱子 点击后的柱子 单位
     */
    private Paint mAxisPaint, mTextPaint, mBarPaint, mBorderPaint, mUnitPaint;
    private List<BarChartEntity> mData;
    /**
     * item中的Y轴最大值
     */
    private float mMaxYValue;
    /**
     * Y轴最大的刻度值
     */
    private float mMaxYDivisionValue;
    /**
     * 柱子的矩形
     */
    private Rect mBarRect, mBarRectClick;
    /**
     * 绘制的区域
     */
    private RectF mDrawArea;
    /**
     * 每一个bar的宽度
     */
    private int mBarWidth;
    /**
     * 每个bar之间的距离
     */
    private int mBarSpace;
    /**
     * 向右边滑动的距离
     */
    private float mLeftMoving;
    /**
     * 左后一次的x坐标
     */
    private float mLastPointX;
    /**
     * 当前移动的距离
     */
    private float mMovingThisTime = 0.0f;
    /**
     * 右边的最大和最小值
     */
    private int mMaxRight, mMinRight;
    /**
     * 下面两个相当于图表的原点
     */
    private float mStartX;
    private int mStartY;
    /**
     * 柱形图左边的x轴坐标 和右边的x轴坐标
     */
    private List<Integer> mBarLeftXPoints = new ArrayList<>();
    private List<Integer> mBarRightXPoints = new ArrayList<>();

    //滑动速度相关
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    /**
     * fling最大速度
     */
    private int mMaxVelocity;
    private String mUnitX;
    private String mUnitY;

    public BarChart2(Context context) {
        super(context);
        init(context);
    }

    public BarChart2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BarChart2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mBarWidth = DensityUtil.dip2px(getContext(), 20);
        mBarSpace = DensityUtil.dip2px(getContext(), 20);
        mTopMargin = DensityUtil.dip2px(getContext(), 20);
        mBottomMargin = DensityUtil.dip2px(getContext(), 30);
        mRightMargin = DensityUtil.dip2px(getContext(), 40);
        mLeftMargin = DensityUtil.dip2px(getContext(), 10);

        mScroller = new Scroller(context);
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

        mAxisPaint = new Paint();
        mAxisPaint.setColor(ContextCompat.getColor(mContext, R.color.axis));
        mAxisPaint.setStrokeWidth(1);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));

        mUnitPaint = new Paint();
        mUnitPaint.setAntiAlias(true);
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        mUnitPaint.setTypeface(typeface);
        mUnitPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));

        mBarPaint = new Paint();
        mBarPaint.setColor(mBarColors != null && mBarColors.length > 0 ? mBarColors[0] : Color.parseColor("#6FC5F4"));

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setColor(Color.rgb(0, 0, 0));
        mBorderPaint.setAlpha(120);

        mBarRect = new Rect(0, 0, 0, 0);
        mBarRectClick = new Rect(0, 0, 0, 0);
        mDrawArea = new RectF(0, 0, 0, 0);
    }

    public void setData(List<BarChartEntity> list, int colors[], String mUnitX, String mUnitY, float max, float min) {
        this.mData = list;
        this.mBarColors = colors;
        this.mUnitX = mUnitX;
        this.mUnitY = mUnitY;
        this.max = max;
        this.min = min;
        if (list != null && list.size() > 0) {
            mMaxYValue = calculateMax(list);
            getRange(mMaxYValue);
        }
    }

    private float fushu = 0;
    private float zhengshu = 0;
    private float max;//上限值
    private float min;//下线值

    /**
     * 计算出Y轴最大值
     * 有整数 有负数的时候 相加
     *
     * @return
     */
    private float calculateMax(List<BarChartEntity> list) {
        List<Float> zhengshus = new ArrayList<>();
        List<Float> fushus = new ArrayList<>();
        float zhengshu = 0;
        float fushu = 0;
        for (BarChartEntity entity : list) {
            if (entity.getSum() >= 0) {
                zhengshus.add(entity.getSum());
            } else {
                fushus.add(entity.getSum());
            }
        }
        if (zhengshus.size() > 0) {
            zhengshu = Collections.max(zhengshus);
        }
        if (fushus.size() > 0) {
            fushu = Collections.min(fushus);
        }
        if (fushu == 0) {
            this.fushu = fushu;
            this.zhengshu = zhengshu > max ? zhengshu : max;
        } else {
            this.fushu = fushu > min ? min : fushu;
            this.zhengshu = zhengshu > max ? zhengshu : max;
        }
        return zhengshu - fushu;
    }

    private float mMaxYDivisionValuez;
    private float mMaxYDivisionValuef;

    /**
     * 得到柱状图的最大和最小的分度值
     */
    private void getRange(float mMaxYValue) {
        int scalez = CalculateUtil.getScale(zhengshu);
        float unScaleValuez = (float) (mMaxYValue / Math.pow(10, scalez));
        mMaxYDivisionValuez = (float) (CalculateUtil.getRangeTop(unScaleValuez) * Math.pow(10, scalez));

        int scalef = CalculateUtil.getScale(Math.abs(fushu));
        float unScaleValuef = (float) (mMaxYValue / Math.pow(10, scalef));
        mMaxYDivisionValuef = (float) (CalculateUtil.getRangeTop(unScaleValuef) * Math.pow(10, scalef));

        mMaxYDivisionValue = mMaxYDivisionValuez + mMaxYDivisionValuef;//获取Y轴的最大的分度值
        mStartX = CalculateUtil.getDivisionTextMaxWidth(mMaxYDivisionValue, mContext) + 20;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        mMaxHeight = h - getPaddingTop() - getPaddingBottom() - mBottomMargin - mTopMargin;
        mPaddingBottom = getPaddingBottom();
        mPaddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();

    }

    //获取滑动范围和指定区域
    private void getArea() {
        mMaxRight = (int) (mStartX + (mBarSpace + mBarWidth) * mData.size());
        mMinRight = mTotalWidth - mLeftMargin - mRightMargin;
        if (fushu == 0) {
            mStartY = mTotalHeight - mBottomMargin - mPaddingBottom;
        } else {//y有负数
            mStartY = (int) (mTotalHeight - mBottomMargin - mPaddingBottom - mMaxYDivisionValuef / mMaxYDivisionValue * mMaxHeight);
        }
        mDrawArea = new RectF(mStartX, mPaddingTop, mTotalWidth - mPaddingRight - mRightMargin, mTotalHeight - mPaddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData != null && !mData.isEmpty()) {
            getArea();
            checkTheLeftMoving();
            //绘制刻度线 和 刻度
            drawScaleLine(canvas);
            //绘制轴
            drawAxis(canvas);
            //绘制单位
            drawUnit(canvas);
            //调用clipRect()方法后，只会显示被裁剪的区域
            canvas.clipRect(mDrawArea.left, mDrawArea.top, mDrawArea.right, mDrawArea.bottom + mDrawArea.height());
            //绘制柱子
            drawBar(canvas);
            //绘制X轴的text
            drawXAxisText(canvas);
        }
    }

    private void drawAxis(Canvas canvas) {
        if (fushu >= 0) {
            canvas.drawLine(mStartX, mStartY, mStartX, mTopMargin, mAxisPaint);
        } else {
            canvas.drawLine(mStartX, mTotalHeight - mBottomMargin - mPaddingBottom, mStartX, mTopMargin, mAxisPaint);
        }
    }

    private void drawUnit(Canvas canvas) {
        String textLength = mMaxYDivisionValue % 5 == 0 ? String.valueOf((int) mMaxYDivisionValue) : String.valueOf(mMaxYDivisionValue);
        canvas.drawText(mUnitY, mStartX - mTextPaint.measureText(textLength), mTopMargin / 2, mUnitPaint);
        canvas.drawText(mUnitX, mTotalWidth - mRightMargin - mPaddingRight + 10, mTotalHeight - mBottomMargin / 2, mUnitPaint);
    }

    /**
     * 检查向左滑动的距离 确保没有画出屏幕
     */
    private void checkTheLeftMoving() {
        if (mLeftMoving > (mMaxRight - mMinRight)) {
            mLeftMoving = mMaxRight - mMinRight;
        }
        if (mLeftMoving < 0) {
            mLeftMoving = 0;
        }
    }

    private void drawXAxisText(Canvas canvas) {
        //这里设置 x 轴的字一条最多显示3个，大于三个就换行
        for (int i = 0; i < mData.size(); i++) {
            String text = mData.get(i).getxLabel();
            if (text.length() <= 3) {
                canvas.drawText(text, mBarLeftXPoints.get(i) - (mTextPaint.measureText(text) - mBarWidth) / 2, mTotalHeight - mBottomMargin * 2 / 3, mTextPaint);
            } else {
                String text1 = text.substring(0, 3);
                String text2 = text.substring(3, text.length());
                canvas.drawText(text1, mBarLeftXPoints.get(i) - (mTextPaint.measureText(text1) - mBarWidth) / 2, mTotalHeight - mBottomMargin * 2 / 3, mTextPaint);
                canvas.drawText(text2, mBarLeftXPoints.get(i) - (mTextPaint.measureText(text2) - mBarWidth) / 2, mTotalHeight - mBottomMargin / 3, mTextPaint);
            }
        }
    }

    private float percent = 1f;
    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();

    public void startAnimation() {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(2000);
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

    private void drawBar(Canvas canvas) {
        mBarLeftXPoints.clear();
        mBarRightXPoints.clear();
        for (int i = 0; i < mData.size(); i++) {
                mBarRect.left = (int) (mStartX + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
                if (mData.get(i).getyValue()[0] < 0) {
                    mBarRect.bottom = mStartY + (int) (Math.abs((mMaxHeight * (mData.get(i).getyValue()[0]) / mMaxYDivisionValue)) * percent);
                } else {
                    mBarRect.bottom = mStartY;
                }
                if (mData.get(i).getyValue()[0] < 0) {
                    mBarRect.top = mStartY;
                } else {
                    mBarRect.top = mStartY - (int) ((mMaxHeight * (mData.get(i).getyValue()[0] / mMaxYDivisionValue)) * percent);
                }
                mBarRect.right = mBarRect.left + mBarWidth;
                canvas.drawRect(mBarRect, mBarPaint);
            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
        }
    }

    private void drawScaleLine(Canvas canvas) {
        canvas.drawText("0", mStartX - mTextPaint.measureText("0") - 5, mStartY + mTextPaint.measureText("0") / 2, mTextPaint);
        canvas.drawLine(mStartX, mStartY, mTotalWidth - mPaddingRight - mRightMargin, mStartY, mAxisPaint);

        canvas.drawText("上限", mStartX - mTextPaint.measureText("上限") - 5, mStartY - max / mMaxYDivisionValue * mMaxHeight + mTextPaint.measureText("0") / 2, mTextPaint);
        canvas.drawLine(mStartX, mStartY - max / mMaxYDivisionValue * mMaxHeight, mTotalWidth - mPaddingRight - mRightMargin, mStartY - max / mMaxYDivisionValue * mMaxHeight, mAxisPaint);

        canvas.drawText("下限", mStartX - mTextPaint.measureText("下限") - 5, mStartY - min / mMaxYDivisionValue * mMaxHeight + mTextPaint.measureText("0") / 2, mTextPaint);
        canvas.drawLine(mStartX, mStartY - min / mMaxYDivisionValue * mMaxHeight, mTotalWidth - mPaddingRight - mRightMargin, mStartY - min / mMaxYDivisionValue * mMaxHeight, mAxisPaint);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mMovingThisTime = (mScroller.getCurrX() - mLastPointX);
            mLeftMoving = mLeftMoving + mMovingThisTime;
            mLastPointX = mScroller.getCurrX();
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastPointX = event.getX();
                mScroller.abortAnimation();//终止动画
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(event);//将用户的移动添加到跟踪器中。
                break;
            case MotionEvent.ACTION_MOVE:
                float movex = event.getX();
                mMovingThisTime = mLastPointX - movex;
                mLeftMoving = mLeftMoving + mMovingThisTime;
                mLastPointX = movex;
                invalidate();
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                int initialVelocity = (int) mVelocityTracker.getXVelocity();
                mVelocityTracker.clear();
                mScroller.fling((int) event.getX(), (int) event.getY(), -initialVelocity / 2,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                invalidate();
                mLastPointX = event.getX();
                break;
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }
}

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
import android.view.GestureDetector;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2018-04-09 15:39
 * 邮箱：657083984@qq.com
 * 新的柱状图
 */
public class BarChartNew extends View {
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
    private int mBottomMargin;
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

    /**
     * 用户点击到了无效位置
     */
    public static final int INVALID_POSITION = -1;
    private OnItemBarClickListener mOnItemBarClickListener;
    private GestureDetector mGestureListener;
    /**
     * 是否绘制点击效果
     */
    private boolean isDrawBorder;
    /**
     * 点击的地方
     */
    private int mClickPosition;

    //滑动速度相关
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    /**
     * fling最大速度
     */
    private int mMaxVelocity;
    private String mUnitX;
    private String mUnitY;

    public void setOnItemBarClickListener(OnItemBarClickListener onRangeBarClickListener) {
        this.mOnItemBarClickListener = onRangeBarClickListener;
    }

    public interface OnItemBarClickListener {
        void onClick(int position);
    }

    public BarChartNew(Context context) {
        super(context);
        init(context);
    }

    public BarChartNew(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BarChartNew(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());

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

    public void setData(List<BarChartEntity> list, int colors[], String mUnitX, String mUnitY) {
        this.mData = list;
        this.mBarColors = colors;
        this.mUnitX = mUnitX;
        this.mUnitY = mUnitY;
        if (list != null && list.size() > 0) {
            mMaxYValue = calculateMax(list);
            getRange(mMaxYValue);
        }
    }

    /**
     * 计算出Y轴最大值
     *
     * @return
     */
    private float calculateMax(List<BarChartEntity> list) {
        float start = list.get(0).getSum();
        for (BarChartEntity entity : list) {
            if (entity.getSum() > start) {
                start = entity.getSum();
            }
        }
        return start;
    }

    /**
     * 得到柱状图的最大和最小的分度值
     */
    private void getRange(float mMaxYValue) {
        int scale = CalculateUtil.getScale(mMaxYValue);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (mMaxYValue / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2
        mMaxYDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
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
        mStartY = mTotalHeight - mBottomMargin - mPaddingBottom;
        mDrawArea = new RectF(mStartX, mPaddingTop, mTotalWidth - mPaddingRight - mRightMargin, mTotalHeight - mPaddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.isEmpty()) return;
        getArea();
        checkTheLeftMoving();
        //绘制刻度线 和 刻度
        drawScaleLine(canvas);
        //绘制单位
        drawUnit(canvas);
        //调用clipRect()方法后，只会显示被裁剪的区域
        canvas.clipRect(mDrawArea.left, mDrawArea.top, mDrawArea.right, mDrawArea.bottom + mDrawArea.height());
        //绘制柱子
        drawBar(canvas);
        //绘制X轴的text
        drawXAxisText(canvas);
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
        mBarRect.bottom = mStartY;
        for (int i = 0; i < mData.size(); i++) {
            if (mBarColors.length == 1) {
                mBarRect.left = (int) (mStartX + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
                mBarRect.top = mStartY - (int) ((mMaxHeight * (mData.get(i).getyValue()[0] / mMaxYDivisionValue)) * percent);
                mBarRect.right = mBarRect.left + mBarWidth;
                canvas.drawRect(mBarRect, mBarPaint);
            } else {
                int eachHeight = 0;//每一块的高度
                mBarRect.left = (int) (mStartX + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
                mBarRect.right = mBarRect.left + mBarWidth;
                for (int j = 0; j < mBarColors.length; j++) {
                    mBarPaint.setColor(mBarColors[j]);
                    mBarRect.bottom = (int) (mStartY - eachHeight * percent);
                    eachHeight += (int) ((mMaxHeight * (mData.get(i).getyValue()[j] / mMaxYDivisionValue)));
                    mBarRect.top = (int) (mBarRect.bottom - ((mMaxHeight * (mData.get(i).getyValue()[j] / mMaxYDivisionValue))) * percent);
                    canvas.drawRect(mBarRect, mBarPaint);
                }
            }
            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
        }
        if (isDrawBorder) {
            drawBorder(mClickPosition);
            canvas.drawRect(mBarRectClick, mBorderPaint);
        }
    }

    private void drawBorder(int position) {
        mBarRectClick.left = (int) (mStartX + mBarWidth * position + mBarSpace * (position + 1) - mLeftMoving);
        mBarRectClick.right = mBarRectClick.left + mBarWidth;
        mBarRectClick.bottom = mStartY;
        mBarRectClick.top = mStartY - (int) (mMaxHeight * (mData.get(position).getSum() / mMaxYDivisionValue));
    }

    /**
     * Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     */
    private void drawScaleLine(Canvas canvas) {
        float eachHeight = (mMaxHeight / 5f);
        float textValue = 0;
        if (mMaxYValue > 1) {
            for (int i = 0; i <= 5; i++) {
                float startY = mStartY - eachHeight * i;
                BigDecimal maxValue = new BigDecimal(mMaxYDivisionValue);
                BigDecimal fen = new BigDecimal(0.2 * i);
                String text = null;
                //因为图表分了5条线，如果能除不进，需要显示小数点不然数据不准确
                if (mMaxYDivisionValue % 5 != 0) {
                    text = String.valueOf(maxValue.multiply(fen).floatValue());
                } else {
                    text = String.valueOf(maxValue.multiply(fen).longValue());
                }
                canvas.drawText(text, mStartX - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0") / 2, mTextPaint);
                canvas.drawLine(mStartX, startY, mTotalWidth - mPaddingRight - mRightMargin, startY, mAxisPaint);
            }
        } else if (mMaxYValue > 0 && mMaxYValue <= 1) {
            for (int i = 0; i <= 5; i++) {
                float startY = mStartY - eachHeight * i;
                textValue = CalculateUtil.numMathMul(mMaxYDivisionValue, (float) (0.2 * i));
                String text = String.valueOf(textValue);
                canvas.drawText(text, mStartX - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0") / 2, mTextPaint);
                canvas.drawLine(mStartX, startY, mTotalWidth - mPaddingRight - mRightMargin, startY, mAxisPaint);
            }
        } else {
            for (int i = 0; i <= 5; i++) {
                float startY = mStartY - eachHeight * i;
                String text = String.valueOf(10 * i);
                canvas.drawText(text, mStartX - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0") / 2, mTextPaint);
                canvas.drawLine(mStartX, startY, mTotalWidth - mPaddingRight - mRightMargin, startY, mAxisPaint);
            }
        }
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
        if (mGestureListener != null) {
            mGestureListener.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 点击
     */
    private class RangeBarOnGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = identifyWhichItemClick(e.getX(), e.getY());
            if (position != INVALID_POSITION && mOnItemBarClickListener != null) {
                mOnItemBarClickListener.onClick(position);
                setClicked(position);
                invalidate();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    /**
     * 设置选中的位置
     *
     * @param position
     */
    public void setClicked(int position) {
        isDrawBorder = true;
        mClickPosition = position;
    }

    /**
     * 根据点击的手势位置识别是第几个柱图被点击
     *
     * @param x
     * @param y
     * @return -1时表示点击的是无效位置
     */
    private int identifyWhichItemClick(float x, float y) {
        float leftx = 0;
        float rightx = 0;
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                leftx = mBarLeftXPoints.get(i);
                rightx = mBarRightXPoints.get(i);
                if (x < leftx) {
                    break;
                }
                if (leftx <= x && x <= rightx) {
                    return i;
                }
            }
        }
        return INVALID_POSITION;
    }
}

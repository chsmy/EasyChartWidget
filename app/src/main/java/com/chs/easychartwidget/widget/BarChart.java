package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.chs.easychartwidget.entity.ChartEntity;
import com.chs.easychartwidget.utils.CalculateUtil;
import com.chs.easychartwidget.utils.DensityUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2016/9/8 09:46
 * 邮箱：657083984@qq.com
 * 柱形图表
 */
public class BarChart extends View {
    private Context mContext;
    /**
     * 背景的颜色
     */
    private static final int BG_COLOR = Color.parseColor("#EEEEEE");
    /**
     * 视图的宽和高
     */
    private int mTotalWidth, mTotalHeight;
    /**
     * x轴 y轴 起始坐标
     */
    private float mXStartIndex, mYStartIndex;
    /**
     * 图表绘制区域的顶部和底部  图表绘制区域的最大高度
     */
    private float mPaintTop, mPaintBottom, mMaxHeight;
    /**
     * 左边和上边的边距
     */
    private int mLeftMargin, mTopMargin;
    /**
     * 画笔 背景，轴 ，线 ，text ,点
     */
    private Paint mBgPaint, mAxisPaint, mTextPaint, mBarPaint, mBorderPaint;
    /**
     * 上下左右的白色部分
     */
    private Rect mLeftWhiteRect, mRightWhiteRect, mTopWhiteRect, mBottomWhiteRect;
    /**
     * 矩形柱子  点击后的矩形
     */
    private Rect mBarRect, mBarRectClick;
    private List<ChartEntity> mData;//数据集合
    /**
     * 右边的最大和最小值
     */
    private int mMaxRight, mMinRight;
    /**
     * item中的最大值
     */
    private float mMaxValueInItems;
    /**
     * 最大分度值
     */
    private float mMaxDivisionValue;
    /**
     * 左后一次的x坐标
     */
    private float mLastPointX;
    /**
     * 向右边滑动的距离
     */
    private float mLeftMoving;
    //左边Y轴的单位
    private String mLeftAxisUnit = "单位";
    /**
     * 当前移动的距离
     */
    private float mMovingThisTime = 0.0f;
    /**
     * 每一个bar的宽度
     */
    private int mBarWidth;
    /**
     * 每个bar之间的距离
     */
    private int mBarSpace;
    /**
     * 柱形图左边的x轴坐标 和右边的x轴坐标
     */
    private List<Integer> mBarLeftXPoints = new ArrayList<>();
    private List<Integer> mBarRightXPoints = new ArrayList<>();


    /* 用户点击到了无效位置 */
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
    /**
     * 速度跟踪器
     */
    private VelocityTracker mVelocityTracker;
    /**
     * 滑动
     */
    private Scroller mScroller;
    /**
     * fling最大速度
     */
    private int mMaxVelocity;

    public void setOnItemBarClickListener(OnItemBarClickListener onRangeBarClickListener) {
        this.mOnItemBarClickListener = onRangeBarClickListener;
    }

    public interface OnItemBarClickListener {
        void onClick(int position);
    }

    public BarChart(Context context) {
        super(context);
        init(context);
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mScroller = new Scroller(context);
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());
        mContext = context;
        mLeftMargin = DensityUtil.dip2px(context, 16);
        mTopMargin = DensityUtil.dip2px(context, 30);

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.WHITE);

        mAxisPaint = new Paint();
        mAxisPaint.setStrokeWidth(DensityUtil.dip2px(context, 1));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));

        mBarPaint = new Paint();
        mBarPaint.setColor(Color.parseColor("#6FC5F4"));

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setColor(Color.rgb(0, 0, 0));
        mBorderPaint.setAlpha(120);

        mBarRect = new Rect(0, 0, 0, 0);
        mBarRectClick = new Rect(0, 0, 0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();
        setNeedHeight();
        mLeftWhiteRect = new Rect(0, 0, 0, mTotalHeight);
        mRightWhiteRect = new Rect(mTotalWidth - mLeftMargin * 2, 0, mTotalWidth, mTotalHeight);
        mTopWhiteRect = new Rect(0, 0, mTotalWidth, mTopMargin / 2);
        mBottomWhiteRect = new Rect(0, (int) mYStartIndex, mTotalWidth, mTotalHeight);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 设置矩形的顶部 底部 右边Y轴的3部分每部分的高度
     */
    private void setNeedHeight() {
        mPaintTop = mTopMargin * 2;
        mPaintBottom = mTotalHeight - mTopMargin / 2;
        mMaxHeight = mPaintBottom - mPaintTop;
        mYStartIndex = mTotalHeight - mTopMargin / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    public void setData(List<ChartEntity> list) {
        this.mData = list;
        //计算最大值
        mMaxValueInItems = list.get(0).getyValue();
        for (ChartEntity entity : list) {
            if (entity.getyValue() > mMaxValueInItems) {
                mMaxValueInItems = entity.getyValue();
            }
        }
        getRange(mMaxValueInItems);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(BG_COLOR);
        if (mData != null) {
            //得到每个bar的宽度
            getItemsWidth();
            checkTheLeftMoving();
            canvas.drawRect(mBottomWhiteRect, mBgPaint);
            canvas.drawRect(mTopWhiteRect, mBgPaint);
            //画中间的白线
            drawWhiteLine(canvas);
            //绘制矩形柱子
            drawBars(canvas);
            //画左边和右边的遮罩层
            mLeftWhiteRect.right = (int) mXStartIndex;
            canvas.drawRect(mLeftWhiteRect, mBgPaint);
            canvas.drawRect(mRightWhiteRect, mBgPaint);
            //画左边的Y轴
            canvas.drawLine(mXStartIndex, mYStartIndex, mXStartIndex, mTopMargin / 2, mAxisPaint);
            //左边Y轴的单位
            canvas.drawText(mLeftAxisUnit, mXStartIndex, mTopMargin / 2 - 14, mTextPaint);
            //画右边的Y轴
            canvas.drawLine(mTotalWidth - mLeftMargin * 2, mYStartIndex, mTotalWidth - mLeftMargin * 2, mTopMargin / 2, mAxisPaint);
            //画左边的Y轴text
            drawLeftYAxis(canvas);
            //画X轴 下面的和上面
            canvas.drawLine(mXStartIndex, mYStartIndex, mTotalWidth - mLeftMargin * 2, mYStartIndex, mAxisPaint);
            canvas.drawLine(mXStartIndex, mTopMargin / 2, mTotalWidth - mLeftMargin * 2, mTopMargin / 2, mAxisPaint);
            //画X轴的text
            drawXAxisText(canvas);
        }
    }

    /**
     * 点击之后绘制点击的地方的边框
     *
     * @param position
     */
    private void drawBorder(int position) {

        mBarRectClick.left = (int) (mXStartIndex + mBarWidth * position + mBarSpace * (position + 1) - mLeftMoving);
        mBarRectClick.right = mBarRectClick.left + mBarWidth;
        mBarRectClick.bottom = mBarRect.bottom;
        mBarRectClick.top = (int) mMaxHeight + mTopMargin * 2 - (int) (mMaxHeight * (mData.get(position).getyValue() / mMaxDivisionValue));
    }
    private float percent = 1f;
    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();
    public void startAnimation(int duration){
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0,1);
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
     * 绘制柱形图
     *
     * @param canvas
     */
    private void drawBars(Canvas canvas) {
        mBarLeftXPoints.clear();
        mBarRightXPoints.clear();
        mBarRect.bottom = mTotalHeight - mTopMargin / 2;
        Log.i("StartIndex","mXStartIndex"+mXStartIndex+"mBarWidth:"+mBarWidth+"mBarSpace"+mBarSpace+"mLeftMoving"+mLeftMoving);
        for (int i = 0; i < mData.size(); i++) {
            mBarRect.left = (int) (mXStartIndex + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
            mBarRect.top = (int) mMaxHeight + mTopMargin * 2 - (int)((mMaxHeight * (mData.get(i).getyValue() / mMaxDivisionValue))*percent);
            mBarRect.right = mBarRect.left + mBarWidth;
            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
//            //在可见的范围内才绘制
//            if (mBarRect.left > mXStartIndex && mBarRect.right < (mTotalWidth - mLeftMargin * 2)) {
            canvas.drawRect(mBarRect, mBarPaint);
//            }
        }
        if (isDrawBorder) {
            drawBorder(mClickPosition);
            canvas.drawRect(mBarRectClick, mBorderPaint);
        }
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

    /**
     * 设定两个点之间的间距 和向右边滑动的时候右边的最大距离
     */
    private void getItemsWidth() {
        int barMinWidth = DensityUtil.dip2px(getContext(), 20);
        int barMinSpace = DensityUtil.dip2px(getContext(), 10);

        mBarWidth = (mTotalWidth - mLeftMargin * 2) / (mData.size() + 3);
        mBarSpace = (mTotalWidth - mLeftMargin * 2 - mBarWidth * mData.size()) / (mData.size() + 1);
        if (mBarWidth < barMinWidth || mBarSpace < barMinSpace) {
            mBarWidth = barMinWidth;
            mBarSpace = barMinSpace;
        }
//        mBarWidth = DensityUtil.dip2px(getContext(), 20);
//        mBarSpace = DensityUtil.dip2px(getContext(), 10);
        mMaxRight = (int) (mXStartIndex + (mBarSpace + mBarWidth) * mData.size()) + mBarSpace * 2;
        mMinRight = mTotalWidth - mBarSpace - mLeftMargin;
    }

    private void drawWhiteLine(Canvas canvas) {
        mAxisPaint.setColor(Color.WHITE);
        float eachHeight = (mMaxHeight / 5f);
        for (int i = 1; i <= 5; i++) {
            float startY = mPaintBottom - eachHeight * i;
            if (startY < mTopMargin / 2) {
                break;
            }
            canvas.drawLine(mXStartIndex, startY, mTotalWidth - mLeftMargin * 2, startY, mAxisPaint);
        }
        mAxisPaint.setColor(Color.BLACK);
    }

    /**
     * 得到柱状图的最大和最小的分度值
     *
     * @param mMaxValueInItems
     */
    private void getRange(float mMaxValueInItems) {
        int scale = CalculateUtil.getScale(mMaxValueInItems);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (mMaxValueInItems / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2

        mMaxDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
        mXStartIndex = CalculateUtil.getDivisionTextMaxWidth(mMaxDivisionValue, mContext) + 20;
    }

    /**
     * 画Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     *
     * @param canvas
     */
    private void drawLeftYAxis(Canvas canvas) {
        float eachHeight = (mMaxHeight / 5f);
        if (mMaxValueInItems > 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = mPaintBottom - eachHeight * i;
                if (startY < mTopMargin / 2) {
                    break;
                }
//                canvas.drawLine(mXStartIndex, startY, mTotalWidth - mLeftMargin*2, startY, mAxisPaint);
                BigDecimal maxValue = new BigDecimal(mMaxDivisionValue);
                BigDecimal fen = new BigDecimal(0.2 * i);
                long textValue = maxValue.multiply(fen).longValue();
                String text = String.valueOf(textValue);
                canvas.drawText(text, mXStartIndex - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0"), mTextPaint);
            }
        } else if (mMaxValueInItems > 0 && mMaxValueInItems <= 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = mPaintBottom - eachHeight * i;
                if (startY < mTopMargin / 2) {
                    break;
                }
//                canvas.drawLine(mXStartIndex, startY, mTotalWidth - mLeftMargin*2, startY, mAxisPaint);
                float textValue = CalculateUtil.numMathMul(mMaxDivisionValue, (float) (0.2 * i));
//                BigDecimal textValues = CalculateUtil.mul(mMaxDivisionValue, (0.2 * i));
                String text = String.valueOf(textValue);
                canvas.drawText(text, mXStartIndex - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0"), mTextPaint);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                float startY = mPaintBottom - eachHeight * i;
                //                canvas.drawLine(mXStartIndex, startY, mTotalWidth - mLeftMargin*2, startY, mAxisPaint);
                String text = String.valueOf(10 * i);
                canvas.drawText(text, mXStartIndex - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0"), mTextPaint);
            }
        }
    }

    /**
     * 绘制X轴上的text
     *
     * @param canvas
     */
    private void drawXAxisText(Canvas canvas) {
        float distance = 0;
        for (int i = 0; i < mData.size(); i++) {
            distance = mXStartIndex + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving;
            String text = mData.get(i).getxLabel();
            //当在可见的范围内才绘制
            if ((mXStartIndex + distance) >= mXStartIndex && (mXStartIndex + distance) < (mTotalWidth - mLeftMargin * 2)) {
                canvas.drawText(text, mBarLeftXPoints.get(i) - (mTextPaint.measureText(text) - mBarWidth) / 2, mPaintBottom + DensityUtil.dip2px(getContext(), 10), mTextPaint);
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
        Log.i("computeScroll","computeScrollstart:"+mLastPointX+"   --getCurrX"+mScroller.getCurrX()+"---mLeftMoving:"+mLeftMoving);
        if (mScroller.computeScrollOffset()) {
            mMovingThisTime = (mScroller.getCurrX() - mLastPointX);
            mLeftMoving = mLeftMoving + mMovingThisTime;
            mLastPointX = mScroller.getCurrX();
            postInvalidate();
            Log.i("computeScroll","computeScroll:"+mLastPointX+"   --getCurrX"+mScroller.getCurrX()+"---mLeftMoving:"+mLeftMoving);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureListener != null) {
            mGestureListener.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastPointX = event.getX();
                mScroller.abortAnimation();//如果在滑动终止动画
                initOrResetVelocityTracker();//初始化速度跟踪器
                Log.i("computeScroll","ACTION_DOWN:"+mLastPointX);
                break;
            case MotionEvent.ACTION_MOVE:
                float movex = event.getX();
                mMovingThisTime = mLastPointX - movex;
                mLeftMoving = mLeftMoving + mMovingThisTime;
                mLastPointX = movex;
                invalidate();
                mVelocityTracker.addMovement(event);//将用户的action添加到跟踪器中。
                Log.i("computeScroll","ACTION_MOVE:"+mLastPointX+"-----"+mLeftMoving);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);//根据已经到达的点计算当前速度。
                int initialVelocity = (int) mVelocityTracker.getXVelocity();//获得最后的速度
                mVelocityTracker.clear();
                //通过mScroller让它飞起来
                mScroller.fling((int) event.getX(), (int) event.getY(), -initialVelocity / 2,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                mLastPointX = event.getX();
                Log.i("computeScroll","ACTION_UP:"+mLastPointX);
                invalidate();
                recycleVelocityTracker();//回收速度跟踪器
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 手势监听器
     *
     * @author A Shuai
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

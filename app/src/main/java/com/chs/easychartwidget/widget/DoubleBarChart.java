package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.DoubleBarEntity;
import com.chs.easychartwidget.utils.CalculateUtil;
import com.chs.easychartwidget.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chs
 * date: 2019-12-12 15:22
 * des:  两个柱子可以对比
 */
public class DoubleBarChart extends View {
    /* 用户点击到了无效位置 */
    public static final int INVALID_POSITION = -1;
    private Context mContext;
    private Paint mBarPaint,mLinePaint,mTextPaint;
    private int mLeftColor = Color.parseColor("#6FC5F4");
    private int mRightColor = Color.parseColor("#78DA9F");
    private int mTotalWidth, mTotalHeight,mUseHeight;
    private float mStartX;
    private int mStartY;
    /**
     * list中的Y轴最大值
     */
    private float mMaxYValue;
    /**
     * Y轴最大的刻度值
     */
    private int maxYDivisionValue;
    private int mTopMargin,mBottomMargin,mRightMargin,mLeftMargin;
    private int mMaxRight, mMinRight;
    private int mBarSpace;
    private int mBarWidth;
    private RectF mDrawArea;
    private Rect mBarLeft,mBarRight;
    private List<Integer> mBarLeftXPoints = new ArrayList<>();
    private List<Integer> mBarRightXPoints = new ArrayList<>();
    /**
     * 向右边滑动的距离
     */
    private float mLeftMoving;
    /**
     * 左后一次的x坐标
     */
    private float mLastPointX;
    private OnItemBarClickListener mOnItemBarClickListener;
    private GestureDetector mGestureListener;
    private VelocityTracker mVelocityTracker;
    /**
     * 当前移动的距离
     */
    private float mMovingThisTime = 0.0f;
    private Scroller mScroller;
    /**
     * fling最大速度
     */
    private int maxVelocity;
    /**
     * 是否绘制点击效果
     */
    private boolean isDrawBorder;
    /**
     * 点击的地方
     */
    private int mClickPosition;

    private List<DoubleBarEntity> mData;

    public void setOnItemBarClickListener(OnItemBarClickListener onRangeBarClickListener) {
        this.mOnItemBarClickListener = onRangeBarClickListener;
    }
    public interface OnItemBarClickListener {
        void onClick(int position);
    }

    public DoubleBarChart(Context context) {
        this(context,null);
    }

    public DoubleBarChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DoubleBarChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBarWidth = DensityUtil.dip2px(getContext(), 20);
        mBarSpace = DensityUtil.dip2px(getContext(), 20);
        mTopMargin = DensityUtil.dip2px(getContext(), 20);
        mBottomMargin = DensityUtil.dip2px(getContext(), 30);
        mRightMargin = DensityUtil.dip2px(getContext(), 40);
        mLeftMargin = DensityUtil.dip2px(getContext(), 10);

        mScroller = new Scroller(context);
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());

        mContext = context;
        mBarPaint = new Paint();
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));
        mTextPaint.setColor(ContextCompat.getColor(context,R.color.axis));
        mDrawArea = new RectF();
        mBarLeft = new Rect(0, 0, 0, 0);
        mBarRight = new Rect(0, 0, 0, 0);
    }

    public void setData(List<DoubleBarEntity> list, int leftColor, int rightColor) {
        mData = list;
        mLeftColor = leftColor;
        mRightColor = rightColor;
        if(list!=null&&!list.isEmpty()){
            mMaxYValue = calculateMax(list);
            getRange(mMaxYValue);
        }
    }

    /**
     *  找出数据中的最大值 动态确定Y轴最大值
     */
    private float calculateMax(List<DoubleBarEntity> list) {
        DoubleBarEntity first = list.get(0);
        float start = Math.max(first.getLeftNum(),first.getRightNum());
        for (DoubleBarEntity entity : list) {
            float next = Math.max(entity.getLeftNum(),entity.getRightNum());
            start = Math.max(next,start);
        }
        return start;
    }

    /**
     *  得到柱状图的最大和最小的分度值
     */
    private void getRange(float maxYValue) {
        //获取这个最大数 数总共有几位
        int scale = CalculateUtil.getScale(maxYValue);
        //最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2
        float unScaleValue = (float) (maxYValue / Math.pow(10, scale));
        //获取Y轴的最大的分度值
        maxYDivisionValue = (int) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));
        //得到最大宽度值的文本
        mStartX = CalculateUtil.getDivisionTextMaxWidth(maxYDivisionValue, mContext) + getPaddingLeft();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        mUseHeight = h - getPaddingTop() - getPaddingBottom()-mTopMargin-mBottomMargin;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData!=null&&!mData.isEmpty()) {
            getArea();
            checkTheLeftMoving();
            drawScaleLine(canvas);
            //调用clipRect()方法后，只会显示被裁剪的区域
            canvas.clipRect(mDrawArea.left, mDrawArea.top, mDrawArea.right, mDrawArea.bottom+mDrawArea.height());
            //绘制柱子
            drawBar(canvas);
        }
    }

    private void drawBar(Canvas canvas) {
        mBarLeft.bottom = mStartY;
        mBarRight.bottom = mStartY;
        for (int i = 0; i < mData.size(); i++) {
            mBarLeft.left = (int) (mStartX + mBarWidth * i*2 + mBarSpace * (i + 1)- mLeftMoving);
            mBarLeft.top = mStartY - (int)((mUseHeight * (mData.get(i).getLeftNum() / maxYDivisionValue)));
            mBarLeft.right = mBarLeft.left + mBarWidth;

            mBarRight.left = mBarLeft.right;
            mBarRight.top = mStartY - (int)((mUseHeight * (mData.get(i).getRightNum() / maxYDivisionValue)));
            mBarRight.right = mBarRight.left + mBarWidth;

            mBarPaint.setColor(mLeftColor);
            canvas.drawRect(mBarLeft, mBarPaint);
            mBarPaint.setColor(mRightColor);
            canvas.drawRect(mBarRight, mBarPaint);
            //绘制X轴的text
            drawXAxisText(canvas,i);
            mBarLeftXPoints.add(mBarLeft.left);
            mBarRightXPoints.add(mBarRight.right);
        }
    }

    private void drawXAxisText(Canvas canvas,int i) {
           //这里设置 x 轴的字一条最多显示3个，大于三个就换行
            String text = mData.get(i).getxLabel();
            if(text.length()<=3){
                canvas.drawText(text, mBarRight.right - mBarWidth - mTextPaint.measureText(text)/2 ,
                        mTotalHeight-mBottomMargin*2/3, mTextPaint);
            }else {
                String text1 = text.substring(0,3);
                String text2 = text.substring(3,text.length());
                canvas.drawText(text1, mBarRight.right - mBarWidth - mTextPaint.measureText(text)/2,
                        mTotalHeight-mBottomMargin*2/3, mTextPaint);
                canvas.drawText(text2, mBarRight.right - mBarWidth - mTextPaint.measureText(text)/2,
                        mTotalHeight-mBottomMargin/3, mTextPaint);
            }
    }
    /**
     * 绘制刻度线 和 刻度
     */
    private void drawScaleLine(Canvas canvas) {
        float eachHeight = (mUseHeight / 5f);
        for (int i = 0; i <= 5; i++) {
            float startY = mStartY-eachHeight * i;
            String text = getTextValue(i);
            canvas.drawText(text,mStartX - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0")/2, mTextPaint);
            canvas.drawLine(mStartX, startY, mTotalWidth-getPaddingRight()-mRightMargin, startY, mTextPaint);
        }
    }

    private String getTextValue(int i) {
        String text = "";
        if (mMaxYValue > 1) {
            text = String.valueOf((int)(0.2*maxYDivisionValue*i));
        } else if (mMaxYValue > 0 && mMaxYValue <= 1) {
            text = String.valueOf(0.2+i);
        }else {
            text = String.valueOf(10*i);
        }
        return text;
    }

    /**
     * 获取绘制区域的范围
     */
    private void getArea() {
        mMaxRight = (int) (mStartX + (mBarSpace + mBarWidth*2) * mData.size());
        mMinRight = mTotalWidth - mRightMargin - mLeftMargin;
        mStartY = mTotalHeight - mBottomMargin - getPaddingBottom();
        mDrawArea.left = mStartX;
        mDrawArea.top = getPaddingTop();
        mDrawArea.right = mTotalWidth - getPaddingRight() - mRightMargin;
        mDrawArea.bottom = mTotalHeight - getPaddingBottom();
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
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastPointX = event.getX();
//                mScroller.abortAnimation();//终止动画
                initOrResetVelocityTracker();
                //将用户的移动添加到跟踪器中。
                mVelocityTracker.addMovement(event);
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
                mVelocityTracker.computeCurrentVelocity(1000, maxVelocity);
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
    /**
     *  点击
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

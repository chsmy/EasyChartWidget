package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.chs.easychartwidget.R;
import com.chs.easychartwidget.entity.BarChartBean;
import com.chs.easychartwidget.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2016/7/6 10:08
 * 邮箱：657083984@qq.com
 * 组合图表
 */
public class CombineChart extends View {
    /** 用户点击到了无效位置 */
    public static final int INVALID_POSITION = -1;
    private int mScreenWidth, mScreenHeight;
    private List<BarChartBean> mBarData;
    private List<Float> mWinds;
    private List<Float> mHumidity;
    private List<Float> mTemperature;
    //柱形图的颜色集合
    private int[] mColors = new int[]{Color.parseColor("#6FC5F4"), Color.parseColor("#78DA9F"), Color.parseColor("#FCAE84")};
    private String[] mRightYLabels;
    /**
     * item中的最大值
     */
    private float mMaxValueInItems;
    /**
     * bar的最高值
     */
    private float mMaxHeight;
    /**
     * 各种画笔 柱形图的 轴的 文本的 线形图的 画点的
     */
    private Paint mBarPaint, mAxisPaint, mTextPaint, mLinePaint, mPointPaint;
    /**
     * 原点的半径
     */
    private static final float RADIUS = 8;
    /**
     * 各种巨型 柱形图的 左边白色部分 右边白色部分
     */
    private Rect mBarRect, mLeftWhiteRect, mRightWhiteRect ,mTopWhiteRect ,mBottomWhiteRect;
    private Rect mBarRect1, mBarRect2;
    /**
     * 左边和上边的边距
     */
    private int mLeftMargin, mTopMargin;
    /**
     * 每一个bar的宽度
     */
    private int mBarWidth;
    /**
     * 每个bar之间的距离
     */
    private int mBarSpace;
    /**
     * x轴 y轴 起始坐标
     */
    private float mXStartIndex, mYStartIndex;
    /**
     * 背景的颜色
     */
    private static final int BG_COLOR = Color.parseColor("#EEEEEE");
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
     * 最大和最小分度值
     */
    private float mMaxDivisionValue, mMinDivisionValue;
    private int mMaxRight, mMinRight;
    /**
     * 线的路径
     */
    Path mLinePathW;
    Path mLinePathH;
    Path mLinePathT;

    //风点的颜色
    private static final int WIND_COLOR = Color.parseColor("#EF6868");
    //湿度线点的颜色
    private static final int HUM_COLOR = Color.parseColor("#549FF4");
    //温度点的颜色
    private static final int TEM_COLOR = Color.parseColor("#FFD400");
    /**
     * 右边的Y轴分成3份  每一分的高度
     */
    private float mLineEachHeight;
    /**
     * 右边的Y轴分成2份  每一分的高度
     */
    private float mLineEachHeightT;
    /**
     * 温度的最大值减最小值
     */
    private float mEachTotalValueT;
    /**
     * 温度的最小刻度值
     */
    private float mTMinValue;
    /**
     * 湿度的最小刻度值
     */
    private float mHMinValue;
    /**
     * 湿度的最大值减最小值
     */
    private float mEachTotalValueH;
    //左边Y轴的单位
    private String mLeftAxisUnit = "";
    private OnItemBarClickListener mOnItemBarClickListener;
    private GestureDetector mGestureListener;

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

    public interface OnItemBarClickListener {
        void onClick(int position);
    }

    /**
     * 保存bar的左边和右边的x轴坐标点
     */
    private List<Integer> mLeftPoints = new ArrayList<>();
    private List<Integer> mRightPoints = new ArrayList<>();

    public void setOnItemBarClickListener(OnItemBarClickListener onRangeBarClickListener) {
        this.mOnItemBarClickListener = onRangeBarClickListener;
    }

    public CombineChart(Context context) {
        super(context);
        init(context);
    }

    public CombineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CombineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mScroller = new Scroller(context);
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());

        mLeftMargin = DensityUtil.dip2px(context, 16);
        mTopMargin = DensityUtil.dip2px(context, 20);

        mBarPaint = new Paint();
        mBarPaint.setColor(mColors[0]);

        mAxisPaint = new Paint();
        mAxisPaint.setStrokeWidth(2);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL);

        mBarRect = new Rect(0, 0, 0, 0);
        mBarRect1 = new Rect(0, 0, 0, 0);
        mBarRect2 = new Rect(0, 0, 0, 0);

        mLinePathW = new Path();
        mLinePathH = new Path();
        mLinePathT = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mScreenWidth = getMeasuredWidth();
        mScreenHeight = getMeasuredHeight();

        //设置矩形的顶部 底部 右边Y轴的3部分每部分的高度
        getStatusHeight();
        mLeftWhiteRect = new Rect(0, 0, 0, mScreenHeight);
        mRightWhiteRect = new Rect(mScreenWidth - mLeftMargin * 2 - 10, 0, mScreenWidth, mScreenHeight);
        mTopWhiteRect = new Rect(0,0,mScreenWidth,mTopMargin/2);
        mBottomWhiteRect = new Rect(0, (int) mYStartIndex,mScreenWidth,mScreenHeight);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = (int) DensityUtil.dip2px(getContext(),50f);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(size,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(size,
                                heightMeasureSpec)));
        //得到每个bar的宽度
        if (mBarData != null) {
            getItemsWidth(mScreenWidth, mBarData.size());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mLeftPoints.clear();
        mRightPoints.clear();
        canvas.drawColor(BG_COLOR);
        if (mWinds == null || mBarData == null || mHumidity == null || mTemperature == null) return;
        //重置3条线
        mLinePathW.reset();
        mLinePathW.incReserve(mWinds.size());
        mLinePathH.reset();
        mLinePathH.incReserve(mWinds.size());
        mLinePathT.reset();
        mLinePathT.incReserve(mWinds.size());
        checkTheLeftMoving();
        mTextPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));
        mBarPaint.setColor(Color.WHITE);
        canvas.drawRect(mBottomWhiteRect, mBarPaint);
        canvas.drawRect(mTopWhiteRect, mBarPaint);
        //画矩形
        drawBars(canvas);
        canvas.save();
        //画线型图
        canvas.drawPath(mLinePathW, mLinePaint);
        canvas.drawPath(mLinePathH, mLinePaint);
        canvas.drawPath(mLinePathT, mLinePaint);
        //画线上的点
        drawCircles(canvas);
//        linePath.rewind();

        //画X轴 下面的和上面的
        canvas.drawLine(mXStartIndex, mYStartIndex, mScreenWidth - mLeftMargin, mYStartIndex, mAxisPaint);
        canvas.drawLine(mXStartIndex, mTopMargin / 2, mScreenWidth - mLeftMargin, mTopMargin / 2, mAxisPaint);
        //画左边和右边的遮罩层
        int c = mBarPaint.getColor();
        mLeftWhiteRect.right = (int) mXStartIndex;

        mBarPaint.setColor(Color.WHITE);
        canvas.drawRect(mLeftWhiteRect, mBarPaint);
        canvas.drawRect(mRightWhiteRect, mBarPaint);
        mBarPaint.setColor(c);

        //画左边的Y轴
        canvas.drawLine(mXStartIndex, mYStartIndex, mXStartIndex, mTopMargin / 2, mAxisPaint);
        //画左边的Y轴text
        drawLeftYAxis(canvas);

        //左边Y轴的单位
        canvas.drawText(mLeftAxisUnit, mXStartIndex - mTextPaint.measureText(mLeftAxisUnit) - 5, mTopMargin/2, mTextPaint);

        //画右边的Y轴
        canvas.drawLine(mScreenWidth - mLeftMargin * 2 - 10, mYStartIndex, mScreenWidth - mLeftMargin * 2 - 10, mTopMargin / 2, mAxisPaint);
        //画右边Y轴text
        drawRightYText(canvas);

    }

    private void drawLeftYAxis(Canvas canvas) {
        int maxYHeight = (int) (mMaxHeight / mMaxValueInItems * mMaxDivisionValue);
        for (int i = 1; i <= 10; i++) {
            float startY = mBarRect.bottom - maxYHeight * 0.1f * i;
            if (startY < mTopMargin / 2) {
                break;
            }
            canvas.drawLine(mXStartIndex, startY, mXStartIndex + 10, startY, mAxisPaint);
            String text = String.valueOf(mMaxDivisionValue * 0.1f * i);
            canvas.drawText(text, mXStartIndex - mTextPaint.measureText(text) - 5, startY + mTextPaint.measureText("0"), mTextPaint);
        }
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

    private void drawBars(Canvas canvas) {
        for (int i = 0; i < mBarData.size(); i++) {
            mBarRect.left = (int) (mXStartIndex + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
            mBarRect.top = (int) mMaxHeight + mTopMargin * 2 - (int) ((mMaxHeight * (mBarData.get(i).getyNum() / mMaxValueInItems))*percent);
            mBarRect.right = mBarRect.left + mBarWidth;
            mLeftPoints.add(mBarRect.left);
            mRightPoints.add(mBarRect.right);
            mBarPaint.setColor(mColors[0]);
            canvas.drawRect(mBarRect, mBarPaint);

            mBarRect1.top = (int) mMaxHeight + mTopMargin * 2 - (int) ((mMaxHeight * (mBarData.get(i).getyNum() / mMaxValueInItems))
                    +(int) (mMaxHeight * (mBarData.get(i).getyNum1() / mMaxValueInItems))*percent);
            mBarRect1.left = (int) (mXStartIndex + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
            mBarRect1.right = mBarRect.left + mBarWidth;
            mBarRect1.bottom = mBarRect.top;
            mBarPaint.setColor(mColors[1]);
            canvas.drawRect(mBarRect1, mBarPaint);

            mBarRect2.top = (int) mMaxHeight + mTopMargin * 2 - (int) ((mMaxHeight * (mBarData.get(i).getyNum() / mMaxValueInItems))
                    + (int) (mMaxHeight * (mBarData.get(i).getyNum1() / mMaxValueInItems))
                    + (int) (mMaxHeight * (mBarData.get(i).getyNum2() / mMaxValueInItems))*percent);
            mBarRect2.left = (int) (mXStartIndex + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving);
            mBarRect2.right = mBarRect.left + mBarWidth;
            mBarRect2.bottom = mBarRect1.top;
            mBarPaint.setColor(mColors[2]);
            canvas.drawRect(mBarRect2, mBarPaint);
            //画x轴的text
            String text = mBarData.get(i).getxLabel();
            canvas.drawText(text, mBarRect.left - (mTextPaint.measureText(text) - mBarWidth) / 2, mBarRect.bottom + DensityUtil.dip2px(getContext(), 10), mTextPaint);

            //确定线形图的路径 和 画圆点
            drawLines(i);
        }
    }

    /**
     * 画右边的Y轴的text
     *
     * @param canvas
     */
    private void drawRightYText(Canvas canvas) {
        if (mRightYLabels.length == 9) {
            float eachHeight = ((mBarRect.bottom - mTopMargin / 2) / 6f);
            for (int j = 0; j < 7; j++) {
                float startY = mBarRect.bottom - eachHeight * j;
//            if (startY < mTopMargin / 2) {
//                break;
//            }
                canvas.drawLine(mScreenWidth - mLeftMargin * 2 - 10, startY, mScreenWidth - mLeftMargin * 2 - 20, startY, mAxisPaint);
                String text = mRightYLabels[j];
                if (j < 2) {
                    mTextPaint.setColor(WIND_COLOR);
                    canvas.drawText(text, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                } else {
                    switch (j) {
                        case 2:
                            canvas.drawText(text, mScreenWidth - mLeftMargin * 2 - 5, startY + mTextPaint.measureText("级"), mTextPaint);
                            String text2 = mRightYLabels[j + 1];
                            mTextPaint.setColor(HUM_COLOR);
                            canvas.drawText(text2, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                            break;
                        case 3:
                            String text3 = mRightYLabels[j + 1];
                            canvas.drawText(text3, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                            break;
                        case 4:
                            String text4 = mRightYLabels[j + 1];
                            canvas.drawText(text4, mScreenWidth - mLeftMargin * 2 - 5, startY + mTextPaint.measureText("级"), mTextPaint);
                            String text41 = mRightYLabels[j + 2];
                            mTextPaint.setColor(TEM_COLOR);
                            canvas.drawText(text41, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                            break;
                        case 5:
                            String text5 = mRightYLabels[j + 2];
                            canvas.drawText(text5, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                            break;
                        case 6:
                            String text6 = mRightYLabels[j + 2];
                            canvas.drawText(text6, mScreenWidth - mLeftMargin * 2 - 5, startY + mTextPaint.measureText("级"), mTextPaint);
                            mTextPaint.setColor(Color.BLACK);
                            break;
                    }
                }
            }
        } else {
            float eachHeight = ((mBarRect.bottom - mTopMargin / 2) / 4f);
            for (int k = 0; k < 5; k++) {
                float startY = mBarRect.bottom - eachHeight * k;
                canvas.drawLine(mScreenWidth - mLeftMargin * 2 - 10, startY, mScreenWidth - mLeftMargin * 2 - 20, startY, mAxisPaint);
                String text = mRightYLabels[k];
                if (k < 2) {
                    mTextPaint.setColor(HUM_COLOR);
                    canvas.drawText(text, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                } else {
                    switch (k) {
                        case 2:
                            canvas.drawText(text, mScreenWidth - mLeftMargin * 2 - 5, startY + mTextPaint.measureText("级"), mTextPaint);
                            String text2 = mRightYLabels[k + 1];
                            mTextPaint.setColor(TEM_COLOR);
                            canvas.drawText(text2, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                            break;
                        case 3:
                            String text3 = mRightYLabels[k + 1];
                            canvas.drawText(text3, mScreenWidth - mLeftMargin * 2 - 5, startY, mTextPaint);
                            break;
                        case 4:
                            String text4 = mRightYLabels[k + 1];
                            canvas.drawText(text4, mScreenWidth - mLeftMargin * 2 - 5, startY + mTextPaint.measureText("级"), mTextPaint);
                            mTextPaint.setColor(Color.BLACK);
                            break;
                    }
                }
            }
        }
    }

    /**
     * 画线上的点
     */
    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mBarData.size(); i++) {
            if (mRightYLabels.length == 9) {
                float lineHeight = mWinds.get(i) * mLineEachHeight / 10f;
                mPointPaint.setColor(WIND_COLOR);
                canvas.drawCircle(mLeftPoints.get(i) + mBarWidth / 2, (mBarRect.bottom - lineHeight)*percent, RADIUS, mPointPaint);
                float lineHeight2 = (mHumidity.get(i)-mHMinValue) * mLineEachHeight / mEachTotalValueH;
                mPointPaint.setColor(HUM_COLOR);
                canvas.drawCircle(mLeftPoints.get(i) + mBarWidth / 2, (mBarRect.bottom - lineHeight2 - mLineEachHeight)*percent, RADIUS, mPointPaint);
                float lineHeight3 = Math.abs(mTemperature.get(i) - mTMinValue) * mLineEachHeight / mEachTotalValueT;
                mPointPaint.setColor(TEM_COLOR);
                canvas.drawCircle(mLeftPoints.get(i) + mBarWidth / 2, (mBarRect.bottom - lineHeight3 - mLineEachHeight * 2)*percent, RADIUS, mPointPaint);
            } else {
                float lineHeight = (mHumidity.get(i)-mHMinValue) * mLineEachHeightT / mEachTotalValueH;
                mPointPaint.setColor(HUM_COLOR);
                canvas.drawCircle(mLeftPoints.get(i) + mBarWidth / 2, (mBarRect.bottom - lineHeight)*percent, RADIUS, mPointPaint);
                float lineHeight1 = Math.abs(mTemperature.get(i) - mTMinValue) * mLineEachHeightT / mEachTotalValueT;
                mPointPaint.setColor(TEM_COLOR);
                canvas.drawCircle(mLeftPoints.get(i) + mBarWidth / 2, (mBarRect.bottom - lineHeight1 - mLineEachHeightT)*percent, RADIUS, mPointPaint);
            }
        }
    }

    /**
     * 画线形图
     *
     * @param i
     */
    private void drawLines(int i) {
        if (mRightYLabels.length == 9) {
            float lineHeight = mWinds.get(i) * mLineEachHeight / 10f;
            if (i == 0) {
                mLinePathW.moveTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight)*percent);
            } else {
                mLinePathW.lineTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight)*percent);
            }

            float lineHeight2 = (mHumidity.get(i)-mHMinValue) * mLineEachHeight / mEachTotalValueH;
            if (i == 0) {
                mLinePathH.moveTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight2 - mLineEachHeight)*percent);
            } else {
                mLinePathH.lineTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight2 - mLineEachHeight)*percent);
            }

            float lineHeight3 = Math.abs(mTemperature.get(i) - mTMinValue) * mLineEachHeight / mEachTotalValueT;
            if (i == 0) {
                mLinePathT.moveTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight3 - mLineEachHeight * 2)*percent);
            } else {
                mLinePathT.lineTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight3 - mLineEachHeight * 2)*percent);
            }
        } else {
            float lineHeight2 = (mHumidity.get(i)-mHMinValue) * mLineEachHeightT / mEachTotalValueH;
            if (i == 0) {
                mLinePathH.moveTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight2)*percent);
            } else {
                mLinePathH.lineTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight2)*percent);
            }

            float lineHeight3 = Math.abs(mTemperature.get(i) - mTMinValue) * mLineEachHeightT / mEachTotalValueT;
            if (i == 0) {
                mLinePathT.moveTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight3 - mLineEachHeightT)*percent);
            } else {
                mLinePathT.lineTo(mBarRect.left + mBarWidth / 2, (mBarRect.bottom - lineHeight3 - mLineEachHeightT)*percent);
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
//            Log.i("computeScroll","computeScroll");
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastPointX = event.getX();
                mScroller.abortAnimation();//如果在滑动终止动画
                initOrResetVelocityTracker();//初始化速度跟踪器
                break;
            case MotionEvent.ACTION_MOVE:
                float movex = event.getX();
                mMovingThisTime = mLastPointX - movex;
                mLeftMoving = mLeftMoving + mMovingThisTime;
                mLastPointX = movex;
                invalidate();
                mVelocityTracker.addMovement(event);//将用户的action添加到跟踪器中。
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
                invalidate();
                mLastPointX = event.getX();
                recycleVelocityTracker();//回收速度跟踪器
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
     * 检查向左滑动的距离 确保没有画出屏幕
     */
    private void checkTheLeftMoving() {
        if (mLeftMoving < 0) {
            mLeftMoving = 0;
        }

        if (mLeftMoving > (mMaxRight - mMinRight)) {
            mLeftMoving = mMaxRight - mMinRight;
        }
    }

    /**
     * 设置矩形的顶部 底部 右边Y轴的3部分每部分的高度
     */
    private void getStatusHeight() {
        mBarRect.top = mTopMargin * 2;
        mBarRect.bottom = mScreenHeight - mTopMargin / 2;
        mMaxHeight = mBarRect.bottom - mBarRect.top;
        mLineEachHeight = (mBarRect.bottom - mTopMargin / 2) / 3;
        mLineEachHeightT = (mBarRect.bottom - mTopMargin / 2) / 2;


        mYStartIndex = mBarRect.bottom;
    }

    public void setRightYLabels(String[] mRightYLabels) {
        this.mRightYLabels = mRightYLabels;
        changeRightYLabels();
        invalidate();
    }

    /**
     * 赋值
     *
     * @param items       柱形图的值
     * @param mWinds       风力线形图的值
     * @param mHumidity    湿度线形图的值
     * @param mTemperature 温度线形图的值
     */
    public void setItems(List<BarChartBean> items, List<Float> mWinds, List<Float> mHumidity, List<Float> mTemperature, String[] mRightYLabels) {
        if (items == null || mWinds == null) {
            throw new RuntimeException("BarChartView.setItems(): the param items cannot be null.");
        }
        if (items.size() == 0) {
            return;
        }
        this.mBarData = items;
        this.mRightYLabels = mRightYLabels;
        this.mWinds = mWinds;
        this.mHumidity = mHumidity;
        this.mTemperature = mTemperature;
        //计算最大值
        mMaxValueInItems = items.get(0).getyNum() + items.get(0).getyNum1() + items.get(0).getyNum2();
        for (BarChartBean barChartBean : items) {
            float totalNum = barChartBean.getyNum() + barChartBean.getyNum1() + barChartBean.getyNum2();
            if (totalNum > mMaxValueInItems) {
                mMaxValueInItems = totalNum;
            }
        }
        changeRightYLabels();
        //获取分度值
        getRange(mMaxValueInItems, 0);

        invalidate();
    }

    /**
     * 计算右边Y轴的刻度标签的值
     */
    private void changeRightYLabels() {
        float HMaxValue = mHumidity.get(0);
        float HMinValue = mHumidity.get(0);
        for (Float hum : mHumidity) {
            if (hum > HMaxValue) {
                HMaxValue = hum;
            }
            if (hum < HMinValue) {
                HMinValue = hum;
            }
        }
        float TMaxValue = mTemperature.get(0);
        float TMinValue = mTemperature.get(0);
        for (Float tem : mTemperature) {
            if (tem > TMaxValue) {
                TMaxValue = tem;
            }
            if (tem < TMinValue) {
                TMinValue = tem;
            }
        }
        int hMaxScale = getScale(HMaxValue);
        float unHMaxScaleValue = (float) (HMaxValue / Math.pow(10, hMaxScale));
        int hMinScale = getScale(HMinValue);
        float unHMinScaleValue = (float) (HMinValue / Math.pow(10, hMinScale));
        int hMax = (int) (getRangeTop(unHMaxScaleValue) * Math.pow(10, hMaxScale));
        int hMin = (int) (getRangeMin(unHMinScaleValue) * Math.pow(10, hMinScale));

        int tMaxScale = getScale(Math.abs(TMaxValue));
        float unTMaxScaleValue = (float) (TMaxValue / Math.pow(10, tMaxScale));
        int tMinScale = getScale(Math.abs(TMinValue));
        float unTMinScaleValue = (float) (TMinValue / Math.pow(10, tMinScale));
        int tMax = (int) (getRangeTop(Math.abs(unTMaxScaleValue)) * Math.pow(10, tMaxScale));
        int tMin = (int) (getRangeMin(Math.abs(unTMinScaleValue)) * Math.pow(10, tMinScale));
        tMax = TMaxValue < 0 ? -tMax : tMax;
        tMin = TMinValue < 0 ? -tMin : tMin;
        if (mRightYLabels.length == 9) {
            mRightYLabels[3] = hMin + "%rh";
            mRightYLabels[4] = hMin + (hMax - hMin) / 2 + "%rh";
            mRightYLabels[5] = hMax + "%rh";
            mRightYLabels[6] = tMin + getResources().getString(R.string.degree_centigrade);
            mRightYLabels[7] = tMin + (tMax - tMin) / 2 + getResources().getString(R.string.degree_centigrade);
            mRightYLabels[8] = tMax + getResources().getString(R.string.degree_centigrade);
        } else {
            mRightYLabels[0] = hMin + "%rh";
            mRightYLabels[1] = hMin + (hMax - hMin) / 2 + "%rh";
            mRightYLabels[2] = hMax + "%rh";
            mRightYLabels[3] = tMin + getResources().getString(R.string.degree_centigrade);
            mRightYLabels[4] = tMin + (tMax - tMin) / 2 + getResources().getString(R.string.degree_centigrade);
            mRightYLabels[5] = tMax + getResources().getString(R.string.degree_centigrade);
        }
        mEachTotalValueH = hMax - hMin;
        mEachTotalValueT = tMax - tMin;
        mHMinValue = hMin;
        mTMinValue = tMin;
    }

    /**
     * 设置左边的Y轴的单位
     *
     * @param labels
     */
    public void setLeftYAxisLabels(String labels) {
        this.mLeftAxisUnit = labels;
    }

    /**
     * 设定每个bar的宽度 和向右边滑动的时候右边的最大距离
     *
     * @param mScreenWidth
     * @param size
     */
    private void getItemsWidth(int mScreenWidth, int size) {
        int barMinWidth = DensityUtil.dip2px(getContext(), 40);
        int barMinSpace = DensityUtil.dip2px(getContext(), 10);

        mBarWidth = (mScreenWidth - mLeftMargin * 2) / (size + 3);
        mBarSpace = (mScreenWidth - mLeftMargin * 2 - mBarWidth * size) / (size + 1);
        if (mBarWidth < barMinWidth || mBarSpace < barMinSpace) {
            mBarWidth = barMinWidth;
            mBarSpace = barMinSpace;
        }
        mMaxRight = (int) (mXStartIndex + (mBarSpace + mBarWidth) * mBarData.size()) + mBarSpace * 2;
        mMinRight = mScreenWidth - mBarSpace - mLeftMargin;
    }

    /**
     * 得到柱状图的最大和最小的分度值
     *
     * @param mMaxValueInItems
     * @param min
     */
    private void getRange(float mMaxValueInItems, float min) {
        int scale = getScale(mMaxValueInItems);
        float unScaleValue = (float) (mMaxValueInItems / Math.pow(10, scale));

        mMaxDivisionValue = (float) (getRangeTop(unScaleValue) * Math.pow(10, scale));

        mXStartIndex = getDivisionTextMaxWidth(mMaxDivisionValue) + 10;
    }

    /**
     * 得到最大宽度值得文本
     *
     * @param mMaxDivisionValue
     * @return
     */
    private float getDivisionTextMaxWidth(float mMaxDivisionValue) {
        Paint mTextPaint = new Paint();
        mTextPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));
        float max = mTextPaint.measureText(String.valueOf(mMaxDivisionValue * 1.0f));
        for (int i = 2; i <= 10; i++) {
            float w = mTextPaint.measureText(String.valueOf(mMaxDivisionValue * 0.1f * i));
            if (w > max) {
                max = w;
            }
        }
        return max;
    }

    private float getRangeTop(float value) {
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
        if (value < 7.0) {
            return 7.0f;
        }

        if (value < 8.0) {
            return 8.0f;
        }

        return 10.0f;
    }

    private float getRangeMin(float value) {
        //value: [1,10)
        if (value < 1.0) {
            return 0f;
        }

        if (value < 1.5) {
            return 1.0f;
        }

        if (value < 2.0) {
            return 1.0f;
        }

        if (value < 3.0) {
            return 2.0f;
        }

        if (value < 4.0) {
            return 3.0f;
        }

        if (value < 5.0) {
            return 4.0f;
        }

        if (value < 6.0) {
            return 5.0f;
        }
        if (value < 7.0) {
            return 6.0f;
        }

        if (value < 8.0) {
            return 7.0f;
        }
        if (value < 9.0) {
            return 8.0f;
        }

        return 9.0f;
    }

    /**
     * 获取这个最大数 数总共有几位
     *
     * @param value
     * @return
     */
    public static int getScale(float value) {
        if (value >= 1 && value < 10) {
            return 0;
        }

        if (value >= 10) {
            return 1 + getScale(value / 10);
        } else {
            return getScale(value * 10) - 1;
        }
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
        for (int i = 0; i < mBarData.size(); i++) {
            leftx = mLeftPoints.get(i);
            rightx = mRightPoints.get(i);
            if (x < leftx) {
                break;
            }
            if (leftx <= x && x <= rightx) {
                return i;
            }
        }
        return INVALID_POSITION;
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
}

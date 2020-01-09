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
import android.util.Log;
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
 * 作者：chs on 2016/9/6 14:17
 * 邮箱：657083984@qq.com
 * 线形图表
 */
public class LineChart extends View {
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
    private Paint mBgPaint,mAxisPaint, mLinePaint, mTextPaint, mPointPaint;
    /**
     * 原点的半径
     */
    private static final float RADIUS = 8;
    /**
     * 上下左右的白色部分
     */
    private Rect mLeftWhiteRect, mRightWhiteRect, mTopWhiteRect, mBottomWhiteRect;
    private List<ChartEntity> mData;
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
     * 线的路径
     */
    Path mLinePath;
    /**
     * 向右边滑动的距离
     */
    private float mLeftMoving;
    //左边Y轴的单位
    private String mLeftAxisUnit = "单位";
    /**
     * 两个点之间的距离
     */
    private int mSpace;
    /**
     * 保存点的x坐标
     */
    private List<Integer> mLinePoints = new ArrayList<>();
    /**
     * 左后一次的x坐标
     */
    private float mLastPointX;
    /**
     * 当前移动的距离
     */
    private float mMovingThisTime = 0.0f;

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

    public LineChart(Context context) {
        super(context);
        init(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mScroller = new Scroller(context);
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
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

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(DensityUtil.dip2px(context, 1));
        mLinePaint.setColor(Color.BLUE);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL);

        mLinePath = new Path();
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mLinePoints.clear();
        canvas.drawColor(BG_COLOR);
        if (mData == null) return;
        //得到每个bar的宽度
        getItemsWidth();
       //重置线
        mLinePath.reset();
        mLinePath.incReserve(mData.size());
        checkTheLeftMoving();
        canvas.drawRect(mBottomWhiteRect, mBgPaint);
        canvas.drawRect(mTopWhiteRect, mBgPaint);
        //画中间的白线
        drawWhiteLine(canvas);
        //画线形图
        drawLines(canvas);
        //画线型图

        //画左边和右边的遮罩层
        mLeftWhiteRect.right = (int) mXStartIndex;
        canvas.drawRect(mLeftWhiteRect, mBgPaint);
        canvas.drawRect(mRightWhiteRect, mBgPaint);
        //画线上的点
        drawCircles(canvas);
        //画左边的Y轴
        canvas.drawLine(mXStartIndex, mYStartIndex, mXStartIndex, mTopMargin / 2, mAxisPaint);
        //左边Y轴的单位
        canvas.drawText(mLeftAxisUnit, mXStartIndex, mTopMargin / 2 - 14, mTextPaint);
        //画右边的Y轴
        canvas.drawLine(mTotalWidth - mLeftMargin * 2, mYStartIndex, mTotalWidth - mLeftMargin * 2, mTopMargin / 2, mAxisPaint);
        //画左边的Y轴text
        drawLeftYAxis(canvas);
        //画X轴 下面的和上面
        canvas.drawLine(mXStartIndex, mYStartIndex, mTotalWidth - mLeftMargin*2, mYStartIndex, mAxisPaint);
        canvas.drawLine(mXStartIndex, mTopMargin / 2, mTotalWidth - mLeftMargin*2, mTopMargin / 2, mAxisPaint);
        //画X轴的text
        drawXAxisText(canvas);
    }

    private void drawXAxisText(Canvas canvas) {
        float distance = 0;
        for(int i = 0;i<mData.size();i++){
            distance = mSpace*i- mLeftMoving;
            String text = mData.get(i).getxLabel();
            //当在可见的范围内才绘制
            if((mXStartIndex+distance)>=mXStartIndex&&(mXStartIndex+distance)<(mTotalWidth-mLeftMargin*2)){
                canvas.drawText(text, mXStartIndex+distance-mTextPaint.measureText(text)/2, mPaintBottom + DensityUtil.dip2px(getContext(), 10), mTextPaint);
            }
        }
    }

    private void drawWhiteLine(Canvas canvas) {
        mAxisPaint.setColor(Color.WHITE);
        float eachHeight = (mMaxHeight/ 5f);
        for (int i = 1; i <= 5; i++) {
            float startY = mPaintBottom - eachHeight * i;
            if (startY < mTopMargin / 2) {
                break;
            }
            canvas.drawLine(mXStartIndex, startY, mTotalWidth - mLeftMargin*2, startY, mAxisPaint);
        }
        mAxisPaint.setColor(Color.BLACK);
    }
    private float percent = 1f;
    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();
    public void startAnimation(int duration){
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0.2f,1);
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
     * 画线形图
     */
    private void drawLines(Canvas canvas) {
        float distance = 0;
        for(int i = 0;i<mData.size();i++){
            distance = mSpace*i- mLeftMoving;
            mLinePoints.add((int) (mXStartIndex+distance));
            float lineHeight = mData.get(i).getyValue() * mMaxHeight / mMaxDivisionValue;
            if (i == 0) {
                mLinePath.moveTo(mXStartIndex + distance, (mPaintBottom - lineHeight)*percent);
            } else {
                mLinePath.lineTo(mXStartIndex + distance, (mPaintBottom - lineHeight)*percent);
            }
        }
        canvas.drawPath(mLinePath, mLinePaint);
    }
    /**
     * 画线上的点
     */
    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mData.size(); i++) {
                mPointPaint.setColor(Color.parseColor("#EF6868"));
            //只有在可见的范围内才绘制
            if(mLinePoints.get(i)>=mXStartIndex&&mLinePoints.get(i)<(mTotalWidth-mLeftMargin*2)){
                canvas.drawCircle(mLinePoints.get(i), (mPaintBottom - mData.get(i).getyValue() * mMaxHeight / mMaxDivisionValue)*percent, RADIUS, mPointPaint);
            }
        }
    }
    /**
     * 画Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
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
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                new Thread(new SmoothScrollThread(mMovingThisTime)).start();
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                int initialVelocity = (int) mVelocityTracker.getXVelocity();
                mVelocityTracker.clear();
                mScroller.fling((int) event.getX(), (int) event.getY(), -initialVelocity / 2,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                invalidate();
                mLastPointX = event.getX();
                recycleVelocityTracker();
                break;
            default:
                return super.onTouchEvent(event);
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
     * 设定两个点之间的间距 和向右边滑动的时候右边的最大距离
     */
    private void getItemsWidth() {
        mSpace = DensityUtil.dip2px(getContext(), 30);
        mMaxRight = (int) (mXStartIndex + mSpace * mData.size());
        mMinRight = mTotalWidth - mLeftMargin*2;
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

    /**
     * 得到柱状图的最大和最小的分度值
     *
     * @param mMaxValueInItems
     */
    private void getRange(float mMaxValueInItems) {
        int scale = CalculateUtil.getScale(mMaxValueInItems);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (mMaxValueInItems / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2

        mMaxDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
        mXStartIndex = CalculateUtil.getDivisionTextMaxWidth(mMaxDivisionValue,mContext) + 20;
    }

    /**
     * 启动和关闭硬件加速   在绘制View的时候支持硬件加速,充分利用GPU的特性,使得绘制更加平滑,但是会多消耗一些内存。
     *
     * @param enabled
     */
    public void setHardwareAccelerationEnabled(boolean enabled) {

        if (android.os.Build.VERSION.SDK_INT >= 11) {

            if (enabled)
                setLayerType(View.LAYER_TYPE_HARDWARE, null);
            else
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } else {
            Log.e("error",
                    "Cannot enable/disable hardware acceleration for devices below API level 11.");
        }
    }
}

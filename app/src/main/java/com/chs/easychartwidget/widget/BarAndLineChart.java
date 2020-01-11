package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
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
import java.util.LinkedList;
import java.util.List;

/**
 * @author chs
 * 邮箱：657083984@qq.com
 * 柱状图加线形图组合
 */
public class BarAndLineChart extends View {

    /**
     * 横轴text为横向
     */
    public static final int TEXT_TYPE_HORIZONTAL = 0X11;
    /**
     * 横轴text为斜体
     */
    public static final int TEXT_TYPE_SLANTING = 0X12;

    private Context mContext;
    /**
     * 视图的宽和高  刻度区域的最大值
     */
    private int mTotalWidth, mTotalHeight, maxHeight;
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
     * 画笔 轴 刻度 柱子 点击后的柱子 单位...
     */
    private Paint mAxisPaint, mTextPaint, mBarPaint, mBorderPaint, mUnitPaint, mLinePaint;
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
    private float mStartY;
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

    //滑动速度相关
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    /**
     * fling最大速度
     */
    private int mMaxVelocity;
    //x轴 y轴的单位
    private String mUnitX;
    private String mUnitY;
    private List<List<Float>> mRightDatas;
    private List<Path> mRightPaths = new LinkedList<>();
    private int []  mRightScale;
    private Path mTextPath = new Path();
    /**
     * 温度的最小最大刻度值
     */
    private float mTMinValue;
    private float mTMaxValue;
    private int mTextType = TEXT_TYPE_HORIZONTAL;
    private float mYOffset;
    private float mXOffset;
    private float mTextAngle = 30;

    public void setOnItemBarClickListener(OnItemBarClickListener onRangeBarClickListener) {
        this.mOnItemBarClickListener = onRangeBarClickListener;
    }

    public interface OnItemBarClickListener {
        void onClick(int position);
    }

    public BarAndLineChart(Context context) {
        super(context);
        init(context);
    }

    public BarAndLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BarAndLineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{15, 5}, 0));

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

    public void setData(List<BarChartEntity> list, int[] colors, String mUnitX, String mUnitY, List<List<Float>> rightDatas, int textType) {
       this.setData(list,colors,mUnitX,mUnitY,rightDatas,textType,new int[]{});
    }
    /**
     *
     * @param list  左边Y轴的数据
     * @param colors 颜色
     * @param mUnitX x轴单位
     * @param mUnitY y轴单位
     * @param rightDatas  右边Y轴的数据
     * @param textType  TEXT_TYPE_HORIZONTAL or TEXT_TYPE_SLANTING
     * @param rightScale  右边Y轴的分度值 长度为2 分别表示最大值和最小值
     */
    public void setData(List<BarChartEntity> list, int[] colors, String mUnitX, String mUnitY, List<List<Float>> rightDatas, int textType,int [] rightScale) {
        this.mData = list;
        this.mBarColors = colors;
        this.mUnitX = mUnitX;
        this.mUnitY = mUnitY;
        this.mRightDatas = rightDatas;
        this.mRightScale = rightScale;
        this.mTextType = textType;
        if(mRightDatas!=null){
            mRightPaths.clear();
            for (List<Float> rightData : mRightDatas) {
                mRightPaths.add(new Path());
            }
        }
        if (list != null && list.size() > 0) {
            mMaxYValue = calculateMax(list);
            getRange(mMaxYValue);
        }
        if(mTextType == TEXT_TYPE_SLANTING){
            //如果是TEXT_TYPE_SLANTING 说明文字太多需要斜着写 这时候需要计算出最长的一个文字 然后往上偏移 防止超出边界
            setYOffset();
        }
        invalidate();
    }

    private void setYOffset() {
        if(mData!=null&&!mData.isEmpty()){
            float yOffset = mTextPaint.measureText(mData.get(0).getxLabel());
            for (int i = 1; i < mData.size(); i++) {
                float curLength = mTextPaint.measureText(mData.get(i).getxLabel());
                if(curLength>mYOffset){
                    yOffset = curLength;
                }
            }
            mYOffset = (float) (yOffset * Math.cos(Math.PI * mTextAngle / 180));
            float xOffset = (float) (yOffset * Math.sin(Math.PI * mTextAngle / 180));
            if(xOffset>mBarSpace){
                mXOffset = xOffset - mBarSpace;
            }
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
        //获取这个最大数 数总共有几位
        int scale = CalculateUtil.getScale(mMaxYValue);
        //最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2
        float unScaleValue = (float) (mMaxYValue / Math.pow(10, scale));
        //获取Y轴的最大的分度值
        mMaxYDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));
        mStartX = CalculateUtil.getDivisionTextMaxWidth(mMaxYDivisionValue, mContext) + 20;
        //获取右边Y轴的最大的分度值
        if(mRightScale.length == 0){
            mTMaxValue = getRightMaxValue(1);
            mTMinValue = getRightMaxValue(2);
        }else {
            mTMaxValue = mRightScale[0];
            mTMinValue = mRightScale[1];
        }
    }

    private float getRightMaxValue(int type) {
        float maxValue = mRightDatas.get(0).get(0);
        float minValue = mRightDatas.get(0).get(0);
        for (Float tem : mRightDatas.get(0)) {
            if (tem > maxValue) {
                maxValue = tem;
            }
            if (tem < minValue) {
                minValue = tem;
            }
        }
        int maxScale = CalculateUtil.getScale(Math.abs(maxValue));
        int minScale = CalculateUtil.getScale(Math.abs(minValue));
        float unMaxScaleValue = (float) (maxValue / Math.pow(10, maxScale));
        float unMinScaleValue = (float) (minValue / Math.pow(10, minScale));
        float max = (float) (CalculateUtil.getRangeTop(Math.abs(unMaxScaleValue)) * Math.pow(10, maxScale));
        float min = (float) (CalculateUtil.getRangeMin(Math.abs(unMinScaleValue)) * Math.pow(10, maxScale));
        max = maxValue > 0 ? max : -max;
        min = minValue > 0 ? min : -min;
        return type == 1 ? max : min;
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
        maxHeight = h - getPaddingTop() - getPaddingBottom() - mBottomMargin - mTopMargin - (int)mYOffset;
        mPaddingBottom = getPaddingBottom();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();

    }

    //获取滑动范围和指定区域
    private void getArea() {
        mMaxRight = (int) (mStartX + (mBarSpace + mBarWidth) * mData.size());
        mMinRight = mTotalWidth - mLeftMargin - mRightMargin;
        mStartY = mTotalHeight - mBottomMargin - mPaddingBottom - mYOffset;
        mDrawArea = new RectF(mStartX, mPaddingTop, mTotalWidth - mPaddingRight - mRightMargin, mTotalHeight - mPaddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData != null && !mData.isEmpty()) {
            pathReset();
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
            for (Path rightPath : mRightPaths) {
                canvas.drawPath(rightPath, mLinePaint);
            }
        }
    }

    private void pathReset() {
        if(mRightDatas!=null&&!mRightPaths.isEmpty()){
            for (int i = 0; i < mRightPaths.size(); i++) {
                Path rightPath = mRightPaths.get(i);
                rightPath.reset();
                rightPath.incReserve(mRightDatas.get(i).size());
            }
        }
    }

    private void drawPath(int position) {
        for (int i = 0; i < mRightPaths.size(); i++) {
            Path path = mRightPaths.get(i);
            List<Float> rightData = mRightDatas.get(i);
            float lineHeight = (Math.abs(rightData.get(position) - mTMinValue) / Math.abs((mTMaxValue - mTMinValue))) * maxHeight;
            if (position == 0) {
                path.moveTo(mBarRect.left + mBarWidth / 2, (mStartY - lineHeight) * percent);
            } else {
                path.lineTo(mBarRect.left + mBarWidth / 2, (mStartY - lineHeight) * percent);
            }
        }
    }

    private void drawUnit(Canvas canvas) {
        String textLength = mMaxYDivisionValue % 5 == 0 ? String.valueOf((int) mMaxYDivisionValue) : String.valueOf(mMaxYDivisionValue);
        canvas.drawText(mUnitY, mStartX - mTextPaint.measureText(textLength), mTopMargin / 2, mUnitPaint);
        canvas.drawText(String.valueOf(mTMinValue), mTotalWidth - mRightMargin - mPaddingRight + 10, mTotalHeight - mBottomMargin * 2 / 3 - mYOffset, mTextPaint);
        canvas.drawText(String.valueOf(mTMaxValue), mTotalWidth - mRightMargin - mPaddingRight + 10, mTopMargin / 2, mTextPaint);
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
        if (mTextType == TEXT_TYPE_HORIZONTAL) {
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
        } else {
            for (int i = 0; i < mData.size(); i++) {
                mTextPath.reset();
                String text = mData.get(i).getxLabel();
                float textLength = mTextPaint.measureText(text);
                float xOffset = (float) (textLength * Math.sin(Math.PI * mTextAngle / 180));
                float yOffset = (float) (textLength * Math.cos(Math.PI * mTextAngle / 180));
                mTextPath.moveTo(mBarLeftXPoints.get(i) + mBarWidth / 2 - xOffset, mTotalHeight - mBottomMargin * 2 / 3-mYOffset + yOffset);
                mTextPath.lineTo(mBarLeftXPoints.get(i) + mBarWidth / 2, mTotalHeight - mBottomMargin * 2 / 3-mYOffset);
                canvas.drawTextOnPath(text, mTextPath, 0, 0, mTextPaint);
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
        mBarRect.bottom = (int) mStartY;
        for (int i = 0; i < mData.size(); i++) {
            if (mBarColors.length == 1) {
                mBarRect.left = (int) (mStartX + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving)+(int)mXOffset;
                mBarRect.top = (int) (mStartY - (int) ((maxHeight * (mData.get(i).getyValue()[0] / mMaxYDivisionValue)) * percent));
                mBarRect.right = mBarRect.left + mBarWidth;
                canvas.drawRect(mBarRect, mBarPaint);
            } else {
                //每一块的高度
                int eachHeight = 0;
                mBarRect.left = (int) (mStartX + mBarWidth * i + mBarSpace * (i + 1) - mLeftMoving)+(int)mXOffset;
                mBarRect.right = mBarRect.left + mBarWidth;
                for (int j = 0; j < mBarColors.length; j++) {
                    mBarPaint.setColor(mBarColors[j]);
                    mBarRect.bottom = (int) (mStartY - eachHeight * percent);
                    eachHeight += (int) ((maxHeight * (mData.get(i).getyValue()[j] / mMaxYDivisionValue)));
                    mBarRect.top = (int) (mBarRect.bottom - ((maxHeight * (mData.get(i).getyValue()[j] / mMaxYDivisionValue))) * percent);
                    canvas.drawRect(mBarRect, mBarPaint);
                }
            }
            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
            //绘制path
            drawPath(i);
        }
        if (isDrawBorder) {
            drawBorder(mClickPosition);
            canvas.drawRect(mBarRectClick, mBorderPaint);
        }
    }

    private void drawBorder(int position) {
        mBarRectClick.left = (int) (mStartX + mBarWidth * position + mBarSpace * (position + 1) - mLeftMoving);
        mBarRectClick.right = mBarRectClick.left + mBarWidth;
        mBarRectClick.bottom = (int) mStartY;
        mBarRectClick.top = (int) (mStartY - (int) (maxHeight * (mData.get(position).getSum() / mMaxYDivisionValue)));
    }

    /**
     * Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     */
    private void drawScaleLine(Canvas canvas) {
        float eachHeight = (maxHeight / 5f);
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

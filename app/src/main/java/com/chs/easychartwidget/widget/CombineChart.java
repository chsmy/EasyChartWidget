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
    /* 用户点击到了无效位置 */
    public static final int INVALID_POSITION = -1;
    private int screenWidth, screenHeight;
    private List<BarChartBean> mBarData;
    private List<Float> winds;//风力的集合
    private List<Float> humidity;//湿度的集合
    private List<Float> temperature;//温度的集合
    //柱形图的颜色集合
    private int colors[] = new int[]{Color.parseColor("#6FC5F4"), Color.parseColor("#78DA9F"), Color.parseColor("#FCAE84")};
    private String[] rightYLabels;
    /**
     * item中的最大值
     */
    private float maxValueInItems;
    /**
     * bar的最高值
     */
    private float maxHeight;
    /**
     * 各种画笔 柱形图的 轴的 文本的 线形图的 画点的
     */
    private Paint barPaint, axisPaint, textPaint, linePaint, pointPaint;
    /**
     * 原点的半径
     */
    private static final float RADIUS = 8;
    /**
     * 各种巨型 柱形图的 左边白色部分 右边白色部分
     */
    private Rect barRect, leftWhiteRect, rightWhiteRect ,topWhiteRect ,bottomWhiteRect;
    private Rect barRect1, barRect2;
    /**
     * 左边和上边的边距
     */
    private int leftMargin, topMargin;
    /**
     * 每一个bar的宽度
     */
    private int barWidth;
    /**
     * 每个bar之间的距离
     */
    private int barSpace;
    /**
     * x轴 y轴 起始坐标
     */
    private float xStartIndex, yStartIndex;
    /**
     * 背景的颜色
     */
    private static final int BG_COLOR = Color.parseColor("#EEEEEE");
    /**
     * 向右边滑动的距离
     */
    private float leftMoving;
    /**
     * 左后一次的x坐标
     */
    private float lastPointX;
    /**
     * 当前移动的距离
     */
    private float movingThisTime = 0.0f;
    /**
     * 最大和最小分度值
     */
    private float maxDivisionValue, minDivisionValue;
    private int maxRight, minRight;
    /**
     * 线的路径
     */
    Path linePathW;//风
    Path linePathH;//湿度
    Path linePathT;//温度

    //风点的颜色
    private static final int WIND_COLOR = Color.parseColor("#EF6868");
    //湿度线点的颜色
    private static final int HUM_COLOR = Color.parseColor("#549FF4");
    //温度点的颜色
    private static final int TEM_COLOR = Color.parseColor("#FFD400");
    /**
     * 右边的Y轴分成3份  每一分的高度
     */
    private float lineEachHeight;
    /**
     * 右边的Y轴分成2份  每一分的高度
     */
    private float lineEachHeightT;
    /**
     * 温度的最大值减最小值
     */
    private float eachTotalValueT;
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
    private float eachTotalValueH;
    //左边Y轴的单位
    private String leftAxisUnit = "";
    private OnItemBarClickListener mOnItemBarClickListener;
    private GestureDetector mGestureListener;

    /**
     * 速度跟踪器
     */
    private VelocityTracker velocityTracker;
    /**
     * 滑动
     */
    private Scroller scroller;
    /**
     * fling最大速度
     */
    private int maxVelocity;

    public interface OnItemBarClickListener {
        void onClick(int position);
    }

    /**
     * 保存bar的左边和右边的x轴坐标点
     */
    private List<Integer> leftPoints = new ArrayList<>();
    private List<Integer> rightPoints = new ArrayList<>();

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
        scroller = new Scroller(context);
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());

        leftMargin = DensityUtil.dip2px(context, 16);
        topMargin = DensityUtil.dip2px(context, 20);

        barPaint = new Paint();
        barPaint.setColor(colors[0]);

        axisPaint = new Paint();
        axisPaint.setStrokeWidth(2);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        barRect = new Rect(0, 0, 0, 0);
        barRect1 = new Rect(0, 0, 0, 0);
        barRect2 = new Rect(0, 0, 0, 0);

        linePathW = new Path();
        linePathH = new Path();
        linePathT = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenWidth = getMeasuredWidth();
        screenHeight = getMeasuredHeight();

        //设置矩形的顶部 底部 右边Y轴的3部分每部分的高度
        getStatusHeight();
        leftWhiteRect = new Rect(0, 0, 0, screenHeight);
        rightWhiteRect = new Rect(screenWidth - leftMargin * 2 - 10, 0, screenWidth, screenHeight);
        topWhiteRect = new Rect(0,0,screenWidth,topMargin/2);
        bottomWhiteRect = new Rect(0, (int) yStartIndex,screenWidth,screenHeight);
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
            getItemsWidth(screenWidth, mBarData.size());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        leftPoints.clear();
        rightPoints.clear();
        canvas.drawColor(BG_COLOR);
        if (winds == null || mBarData == null || humidity == null || temperature == null) return;
        //重置3条线
        linePathW.reset();
        linePathW.incReserve(winds.size());
        linePathH.reset();
        linePathH.incReserve(winds.size());
        linePathT.reset();
        linePathT.incReserve(winds.size());
        checkTheLeftMoving();
        textPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));
        barPaint.setColor(Color.WHITE);
        canvas.drawRect(bottomWhiteRect, barPaint);
        canvas.drawRect(topWhiteRect, barPaint);
        //画矩形
        drawBars(canvas);
        canvas.save();
        //画线型图
        canvas.drawPath(linePathW, linePaint);
        canvas.drawPath(linePathH, linePaint);
        canvas.drawPath(linePathT, linePaint);
        //画线上的点
        drawCircles(canvas);
//        linePath.rewind();

        //画X轴 下面的和上面的
        canvas.drawLine(xStartIndex, yStartIndex, screenWidth - leftMargin, yStartIndex, axisPaint);
        canvas.drawLine(xStartIndex, topMargin / 2, screenWidth - leftMargin, topMargin / 2, axisPaint);
        //画左边和右边的遮罩层
        int c = barPaint.getColor();
        leftWhiteRect.right = (int) xStartIndex;

        barPaint.setColor(Color.WHITE);
        canvas.drawRect(leftWhiteRect, barPaint);
        canvas.drawRect(rightWhiteRect, barPaint);
        barPaint.setColor(c);

        //画左边的Y轴
        canvas.drawLine(xStartIndex, yStartIndex, xStartIndex, topMargin / 2, axisPaint);
        //画左边的Y轴text
        drawLeftYAxis(canvas);

        //左边Y轴的单位
        canvas.drawText(leftAxisUnit, xStartIndex - textPaint.measureText(leftAxisUnit) - 5, topMargin/2, textPaint);

        //画右边的Y轴
        canvas.drawLine(screenWidth - leftMargin * 2 - 10, yStartIndex, screenWidth - leftMargin * 2 - 10, topMargin / 2, axisPaint);
        //画右边Y轴text
        drawRightYText(canvas);

    }

    private void drawLeftYAxis(Canvas canvas) {
        int maxYHeight = (int) (maxHeight / maxValueInItems * maxDivisionValue);
        for (int i = 1; i <= 10; i++) {
            float startY = barRect.bottom - maxYHeight * 0.1f * i;
            if (startY < topMargin / 2) {
                break;
            }
            canvas.drawLine(xStartIndex, startY, xStartIndex + 10, startY, axisPaint);
            String text = String.valueOf(maxDivisionValue * 0.1f * i);
            canvas.drawText(text, xStartIndex - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
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
            barRect.left = (int) (xStartIndex + barWidth * i + barSpace * (i + 1) - leftMoving);
            barRect.top = (int) maxHeight + topMargin * 2 - (int) ((maxHeight * (mBarData.get(i).getyNum() / maxValueInItems))*percent);
            barRect.right = barRect.left + barWidth;
            leftPoints.add(barRect.left);
            rightPoints.add(barRect.right);
            barPaint.setColor(colors[0]);
            canvas.drawRect(barRect, barPaint);

            barRect1.top = (int) maxHeight + topMargin * 2 - (int) ((maxHeight * (mBarData.get(i).getyNum() / maxValueInItems))
                    +(int) (maxHeight * (mBarData.get(i).getyNum1() / maxValueInItems))*percent);
            barRect1.left = (int) (xStartIndex + barWidth * i + barSpace * (i + 1) - leftMoving);
            barRect1.right = barRect.left + barWidth;
            barRect1.bottom = barRect.top;
            barPaint.setColor(colors[1]);
            canvas.drawRect(barRect1, barPaint);

            barRect2.top = (int) maxHeight + topMargin * 2 - (int) ((maxHeight * (mBarData.get(i).getyNum() / maxValueInItems))
                    + (int) (maxHeight * (mBarData.get(i).getyNum1() / maxValueInItems))
                    + (int) (maxHeight * (mBarData.get(i).getyNum2() / maxValueInItems))*percent);
            barRect2.left = (int) (xStartIndex + barWidth * i + barSpace * (i + 1) - leftMoving);
            barRect2.right = barRect.left + barWidth;
            barRect2.bottom = barRect1.top;
            barPaint.setColor(colors[2]);
            canvas.drawRect(barRect2, barPaint);
            //画x轴的text
            String text = mBarData.get(i).getxLabel();
            canvas.drawText(text, barRect.left - (textPaint.measureText(text) - barWidth) / 2, barRect.bottom + DensityUtil.dip2px(getContext(), 10), textPaint);

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
        if (rightYLabels.length == 9) {
            float eachHeight = ((barRect.bottom - topMargin / 2) / 6f);
            for (int j = 0; j < 7; j++) {
                float startY = barRect.bottom - eachHeight * j;
//            if (startY < topMargin / 2) {
//                break;
//            }
                canvas.drawLine(screenWidth - leftMargin * 2 - 10, startY, screenWidth - leftMargin * 2 - 20, startY, axisPaint);
                String text = rightYLabels[j];
                if (j < 2) {
                    textPaint.setColor(WIND_COLOR);
                    canvas.drawText(text, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                } else {
                    switch (j) {
                        case 2:
                            canvas.drawText(text, screenWidth - leftMargin * 2 - 5, startY + textPaint.measureText("级"), textPaint);
                            String text2 = rightYLabels[j + 1];
                            textPaint.setColor(HUM_COLOR);
                            canvas.drawText(text2, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                            break;
                        case 3:
                            String text3 = rightYLabels[j + 1];
                            canvas.drawText(text3, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                            break;
                        case 4:
                            String text4 = rightYLabels[j + 1];
                            canvas.drawText(text4, screenWidth - leftMargin * 2 - 5, startY + textPaint.measureText("级"), textPaint);
                            String text41 = rightYLabels[j + 2];
                            textPaint.setColor(TEM_COLOR);
                            canvas.drawText(text41, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                            break;
                        case 5:
                            String text5 = rightYLabels[j + 2];
                            canvas.drawText(text5, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                            break;
                        case 6:
                            String text6 = rightYLabels[j + 2];
                            canvas.drawText(text6, screenWidth - leftMargin * 2 - 5, startY + textPaint.measureText("级"), textPaint);
                            textPaint.setColor(Color.BLACK);
                            break;
                    }
                }
            }
        } else {
            float eachHeight = ((barRect.bottom - topMargin / 2) / 4f);
            for (int k = 0; k < 5; k++) {
                float startY = barRect.bottom - eachHeight * k;
                canvas.drawLine(screenWidth - leftMargin * 2 - 10, startY, screenWidth - leftMargin * 2 - 20, startY, axisPaint);
                String text = rightYLabels[k];
                if (k < 2) {
                    textPaint.setColor(HUM_COLOR);
                    canvas.drawText(text, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                } else {
                    switch (k) {
                        case 2:
                            canvas.drawText(text, screenWidth - leftMargin * 2 - 5, startY + textPaint.measureText("级"), textPaint);
                            String text2 = rightYLabels[k + 1];
                            textPaint.setColor(TEM_COLOR);
                            canvas.drawText(text2, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                            break;
                        case 3:
                            String text3 = rightYLabels[k + 1];
                            canvas.drawText(text3, screenWidth - leftMargin * 2 - 5, startY, textPaint);
                            break;
                        case 4:
                            String text4 = rightYLabels[k + 1];
                            canvas.drawText(text4, screenWidth - leftMargin * 2 - 5, startY + textPaint.measureText("级"), textPaint);
                            textPaint.setColor(Color.BLACK);
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
            if (rightYLabels.length == 9) {
                float lineHeight = winds.get(i) * lineEachHeight / 10f;
                pointPaint.setColor(WIND_COLOR);
                canvas.drawCircle(leftPoints.get(i) + barWidth / 2, (barRect.bottom - lineHeight)*percent, RADIUS, pointPaint);
                float lineHeight2 = (humidity.get(i)-mHMinValue) * lineEachHeight / eachTotalValueH;
                pointPaint.setColor(HUM_COLOR);
                canvas.drawCircle(leftPoints.get(i) + barWidth / 2, (barRect.bottom - lineHeight2 - lineEachHeight)*percent, RADIUS, pointPaint);
                float lineHeight3 = Math.abs(temperature.get(i) - mTMinValue) * lineEachHeight / eachTotalValueT;
                pointPaint.setColor(TEM_COLOR);
                canvas.drawCircle(leftPoints.get(i) + barWidth / 2, (barRect.bottom - lineHeight3 - lineEachHeight * 2)*percent, RADIUS, pointPaint);
            } else {
                float lineHeight = (humidity.get(i)-mHMinValue) * lineEachHeightT / eachTotalValueH;
                pointPaint.setColor(HUM_COLOR);
                canvas.drawCircle(leftPoints.get(i) + barWidth / 2, (barRect.bottom - lineHeight)*percent, RADIUS, pointPaint);
                float lineHeight1 = Math.abs(temperature.get(i) - mTMinValue) * lineEachHeightT / eachTotalValueT;
                pointPaint.setColor(TEM_COLOR);
                canvas.drawCircle(leftPoints.get(i) + barWidth / 2, (barRect.bottom - lineHeight1 - lineEachHeightT)*percent, RADIUS, pointPaint);
            }
        }
    }

    /**
     * 画线形图
     *
     * @param i
     */
    private void drawLines(int i) {
        if (rightYLabels.length == 9) {
            float lineHeight = winds.get(i) * lineEachHeight / 10f;
            if (i == 0) {
                linePathW.moveTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight)*percent);
            } else {
                linePathW.lineTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight)*percent);
            }

            float lineHeight2 = (humidity.get(i)-mHMinValue) * lineEachHeight / eachTotalValueH;
            if (i == 0) {
                linePathH.moveTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight2 - lineEachHeight)*percent);
            } else {
                linePathH.lineTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight2 - lineEachHeight)*percent);
            }

            float lineHeight3 = Math.abs(temperature.get(i) - mTMinValue) * lineEachHeight / eachTotalValueT;
            if (i == 0) {
                linePathT.moveTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight3 - lineEachHeight * 2)*percent);
            } else {
                linePathT.lineTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight3 - lineEachHeight * 2)*percent);
            }
        } else {
            float lineHeight2 = (humidity.get(i)-mHMinValue) * lineEachHeightT / eachTotalValueH;
            if (i == 0) {
                linePathH.moveTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight2)*percent);
            } else {
                linePathH.lineTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight2)*percent);
            }

            float lineHeight3 = Math.abs(temperature.get(i) - mTMinValue) * lineEachHeightT / eachTotalValueT;
            if (i == 0) {
                linePathT.moveTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight3 - lineEachHeightT)*percent);
            } else {
                linePathT.lineTo(barRect.left + barWidth / 2, (barRect.bottom - lineHeight3 - lineEachHeightT)*percent);
            }
        }
    }
    private void initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }
    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            movingThisTime = (scroller.getCurrX() - lastPointX);
            leftMoving = leftMoving + movingThisTime;
            lastPointX = scroller.getCurrX();
            postInvalidate();
//            Log.i("computeScroll","computeScroll");
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPointX = event.getX();
                scroller.abortAnimation();//如果在滑动终止动画
                initOrResetVelocityTracker();//初始化速度跟踪器
                break;
            case MotionEvent.ACTION_MOVE:
                float movex = event.getX();
                movingThisTime = lastPointX - movex;
                leftMoving = leftMoving + movingThisTime;
                lastPointX = movex;
                invalidate();
                velocityTracker.addMovement(event);//将用户的action添加到跟踪器中。
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);//根据已经到达的点计算当前速度。
                int initialVelocity = (int) velocityTracker.getXVelocity();//获得最后的速度
                velocityTracker.clear();
                //通过scroller让它飞起来
                scroller.fling((int) event.getX(), (int) event.getY(), -initialVelocity / 2,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                invalidate();
                lastPointX = event.getX();
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
        if (leftMoving < 0) {
            leftMoving = 0;
        }

        if (leftMoving > (maxRight - minRight)) {
            leftMoving = maxRight - minRight;
        }
    }

    /**
     * 设置矩形的顶部 底部 右边Y轴的3部分每部分的高度
     */
    private void getStatusHeight() {
        barRect.top = topMargin * 2;
        barRect.bottom = screenHeight - topMargin / 2;
        maxHeight = barRect.bottom - barRect.top;
        lineEachHeight = (barRect.bottom - topMargin / 2) / 3;
        lineEachHeightT = (barRect.bottom - topMargin / 2) / 2;


        yStartIndex = barRect.bottom;
    }

    public void setRightYLabels(String[] rightYLabels) {
        this.rightYLabels = rightYLabels;
        changeRightYLabels();
        invalidate();
    }

    /**
     * 赋值
     *
     * @param items       柱形图的值
     * @param winds       风力线形图的值
     * @param humidity    湿度线形图的值
     * @param temperature 温度线形图的值
     */
    public void setItems(List<BarChartBean> items, List<Float> winds, List<Float> humidity, List<Float> temperature, String[] rightYLabels) {
        if (items == null || winds == null) {
            throw new RuntimeException("BarChartView.setItems(): the param items cannot be null.");
        }
        if (items.size() == 0) {
            return;
        }
        this.mBarData = items;
        this.rightYLabels = rightYLabels;
        this.winds = winds;
        this.humidity = humidity;
        this.temperature = temperature;
        //计算最大值
        maxValueInItems = items.get(0).getyNum() + items.get(0).getyNum1() + items.get(0).getyNum2();
        for (BarChartBean barChartBean : items) {
            float totalNum = barChartBean.getyNum() + barChartBean.getyNum1() + barChartBean.getyNum2();
            if (totalNum > maxValueInItems) {
                maxValueInItems = totalNum;
            }
        }
        changeRightYLabels();
        //获取分度值
        getRange(maxValueInItems, 0);

        invalidate();
    }

    /**
     * 计算右边Y轴的刻度标签的值
     */
    private void changeRightYLabels() {
        float HMaxValue = humidity.get(0);
        float HMinValue = humidity.get(0);
        for (Float hum : humidity) {
            if (hum > HMaxValue) {
                HMaxValue = hum;
            }
            if (hum < HMinValue) {
                HMinValue = hum;
            }
        }
        float TMaxValue = temperature.get(0);
        float TMinValue = temperature.get(0);
        for (Float tem : temperature) {
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
        if (rightYLabels.length == 9) {
            rightYLabels[3] = hMin + "%rh";
            rightYLabels[4] = hMin + (hMax - hMin) / 2 + "%rh";
            rightYLabels[5] = hMax + "%rh";
            rightYLabels[6] = tMin + getResources().getString(R.string.degree_centigrade);
            rightYLabels[7] = tMin + (tMax - tMin) / 2 + getResources().getString(R.string.degree_centigrade);
            rightYLabels[8] = tMax + getResources().getString(R.string.degree_centigrade);
        } else {
            rightYLabels[0] = hMin + "%rh";
            rightYLabels[1] = hMin + (hMax - hMin) / 2 + "%rh";
            rightYLabels[2] = hMax + "%rh";
            rightYLabels[3] = tMin + getResources().getString(R.string.degree_centigrade);
            rightYLabels[4] = tMin + (tMax - tMin) / 2 + getResources().getString(R.string.degree_centigrade);
            rightYLabels[5] = tMax + getResources().getString(R.string.degree_centigrade);
        }
        eachTotalValueH = hMax - hMin;
        eachTotalValueT = tMax - tMin;
        mHMinValue = hMin;
        mTMinValue = tMin;
    }

    /**
     * 设置左边的Y轴的单位
     *
     * @param labels
     */
    public void setLeftYAxisLabels(String labels) {
        this.leftAxisUnit = labels;
    }

    /**
     * 设定每个bar的宽度 和向右边滑动的时候右边的最大距离
     *
     * @param screenWidth
     * @param size
     */
    private void getItemsWidth(int screenWidth, int size) {
        int barMinWidth = DensityUtil.dip2px(getContext(), 40);
        int barMinSpace = DensityUtil.dip2px(getContext(), 10);

        barWidth = (screenWidth - leftMargin * 2) / (size + 3);
        barSpace = (screenWidth - leftMargin * 2 - barWidth * size) / (size + 1);
        if (barWidth < barMinWidth || barSpace < barMinSpace) {
            barWidth = barMinWidth;
            barSpace = barMinSpace;
        }
        maxRight = (int) (xStartIndex + (barSpace + barWidth) * mBarData.size()) + barSpace * 2;
        minRight = screenWidth - barSpace - leftMargin;
    }

    /**
     * 得到柱状图的最大和最小的分度值
     *
     * @param maxValueInItems
     * @param min
     */
    private void getRange(float maxValueInItems, float min) {
        int scale = getScale(maxValueInItems);
        float unScaleValue = (float) (maxValueInItems / Math.pow(10, scale));

        maxDivisionValue = (float) (getRangeTop(unScaleValue) * Math.pow(10, scale));

        xStartIndex = getDivisionTextMaxWidth(maxDivisionValue) + 10;
    }

    /**
     * 得到最大宽度值得文本
     *
     * @param maxDivisionValue
     * @return
     */
    private float getDivisionTextMaxWidth(float maxDivisionValue) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));
        float max = textPaint.measureText(String.valueOf(maxDivisionValue * 1.0f));
        for (int i = 2; i <= 10; i++) {
            float w = textPaint.measureText(String.valueOf(maxDivisionValue * 0.1f * i));
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
            leftx = leftPoints.get(i);
            rightx = rightPoints.get(i);
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

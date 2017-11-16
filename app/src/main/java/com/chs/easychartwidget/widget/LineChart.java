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
    private float xStartIndex, yStartIndex;
    /**
     * 图表绘制区域的顶部和底部  图表绘制区域的最大高度
     */
    private float paintTop, paintBottom, maxHeight;
    /**
     * 左边和上边的边距
     */
    private int leftMargin, topMargin;
    /**
     * 画笔 背景，轴 ，线 ，text ,点
     */
    private Paint bgPaint,axisPaint, linePaint, textPaint, pointPaint;
    /**
     * 原点的半径
     */
    private static final float RADIUS = 8;
    /**
     * 上下左右的白色部分
     */
    private Rect leftWhiteRect, rightWhiteRect, topWhiteRect, bottomWhiteRect;
    private List<ChartEntity> mData;//数据集合
    /**
     * 右边的最大和最小值
     */
    private int maxRight, minRight;
    /**
     * item中的最大值
     */
    private float maxValueInItems;
    /**
     * 最大分度值
     */
    private float maxDivisionValue;
    /**
     * 线的路径
     */
    Path linePath;
    /**
     * 向右边滑动的距离
     */
    private float leftMoving;
    //左边Y轴的单位
    private String leftAxisUnit = "单位";
    /**
     * 两个点之间的距离
     */
    private int space;
    /**
     * 保存点的x坐标
     */
    private List<Integer> linePoints = new ArrayList<>();
    /**
     * 左后一次的x坐标
     */
    private float lastPointX;
    /**
     * 当前移动的距离
     */
    private float movingThisTime = 0.0f;

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
        scroller = new Scroller(context);
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mContext = context;
        leftMargin = DensityUtil.dip2px(context, 16);
        topMargin = DensityUtil.dip2px(context, 30);

        bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);

        axisPaint = new Paint();
        axisPaint.setStrokeWidth(DensityUtil.dip2px(context, 1));

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(DensityUtil.dip2px(context, 1));
        linePaint.setColor(Color.BLUE);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        linePath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();
        setNeedHeight();
        leftWhiteRect = new Rect(0, 0, 0, mTotalHeight);
        rightWhiteRect = new Rect(mTotalWidth - leftMargin * 2, 0, mTotalWidth, mTotalHeight);
        topWhiteRect = new Rect(0, 0, mTotalWidth, topMargin / 2);
        bottomWhiteRect = new Rect(0, (int) yStartIndex, mTotalWidth, mTotalHeight);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 设置矩形的顶部 底部 右边Y轴的3部分每部分的高度
     */
    private void setNeedHeight() {
        paintTop = topMargin * 2;
        paintBottom = mTotalHeight - topMargin / 2;
        maxHeight = paintBottom - paintTop;
        yStartIndex = mTotalHeight - topMargin / 2;
        ;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        linePoints.clear();
        canvas.drawColor(BG_COLOR);
        if (mData == null) return;
        //得到每个bar的宽度
        getItemsWidth();
       //重置线
        linePath.reset();
        linePath.incReserve(mData.size());
        checkTheLeftMoving();
        canvas.drawRect(bottomWhiteRect, bgPaint);
        canvas.drawRect(topWhiteRect, bgPaint);
        //画中间的白线
        drawWhiteLine(canvas);
        //画线形图
        drawLines(canvas);
        //画线型图

        //画左边和右边的遮罩层
        leftWhiteRect.right = (int) xStartIndex;
        canvas.drawRect(leftWhiteRect, bgPaint);
        canvas.drawRect(rightWhiteRect, bgPaint);
        //画线上的点
        drawCircles(canvas);
        //画左边的Y轴
        canvas.drawLine(xStartIndex, yStartIndex, xStartIndex, topMargin / 2, axisPaint);
        //左边Y轴的单位
        canvas.drawText(leftAxisUnit, xStartIndex, topMargin / 2 - 14, textPaint);
        //画右边的Y轴
        canvas.drawLine(mTotalWidth - leftMargin * 2, yStartIndex, mTotalWidth - leftMargin * 2, topMargin / 2, axisPaint);
        //画左边的Y轴text
        drawLeftYAxis(canvas);
        //画X轴 下面的和上面
        canvas.drawLine(xStartIndex, yStartIndex, mTotalWidth - leftMargin*2, yStartIndex, axisPaint);
        canvas.drawLine(xStartIndex, topMargin / 2, mTotalWidth - leftMargin*2, topMargin / 2, axisPaint);
        //画X轴的text
        drawXAxisText(canvas);
    }

    private void drawXAxisText(Canvas canvas) {
        float distance = 0;
        for(int i = 0;i<mData.size();i++){
            distance = space*i- leftMoving;
            String text = mData.get(i).getxLabel();
            //当在可见的范围内才绘制
            if((xStartIndex+distance)>=xStartIndex&&(xStartIndex+distance)<(mTotalWidth-leftMargin*2)){
                canvas.drawText(text, xStartIndex+distance-textPaint.measureText(text)/2, paintBottom + DensityUtil.dip2px(getContext(), 10), textPaint);
            }
        }
    }

    private void drawWhiteLine(Canvas canvas) {
        axisPaint.setColor(Color.WHITE);
        float eachHeight = (maxHeight/ 5f);
        for (int i = 1; i <= 5; i++) {
            float startY = paintBottom - eachHeight * i;
            if (startY < topMargin / 2) {
                break;
            }
            canvas.drawLine(xStartIndex, startY, mTotalWidth - leftMargin*2, startY, axisPaint);
        }
        axisPaint.setColor(Color.BLACK);
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
            distance = space*i- leftMoving;
            linePoints.add((int) (xStartIndex+distance));
            float lineHeight = mData.get(i).getyValue() * maxHeight / maxDivisionValue;
            if (i == 0) {
                linePath.moveTo(xStartIndex + distance, (paintBottom - lineHeight)*percent);
            } else {
                linePath.lineTo(xStartIndex + distance, (paintBottom - lineHeight)*percent);
            }
        }
        canvas.drawPath(linePath, linePaint);
    }
    /**
     * 画线上的点
     */
    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mData.size(); i++) {
                pointPaint.setColor(Color.parseColor("#EF6868"));
            //只有在可见的范围内才绘制
            if(linePoints.get(i)>=xStartIndex&&linePoints.get(i)<(mTotalWidth-leftMargin*2)){
                canvas.drawCircle(linePoints.get(i), (paintBottom - mData.get(i).getyValue() * maxHeight / maxDivisionValue)*percent, RADIUS, pointPaint);
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
        float eachHeight = (maxHeight / 5f);
        if (maxValueInItems > 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = paintBottom - eachHeight * i;
                if (startY < topMargin / 2) {
                    break;
                }
//                canvas.drawLine(xStartIndex, startY, mTotalWidth - leftMargin*2, startY, axisPaint);
                BigDecimal maxValue = new BigDecimal(maxDivisionValue);
                BigDecimal fen = new BigDecimal(0.2 * i);
                long textValue = maxValue.multiply(fen).longValue();
                String text = String.valueOf(textValue);
                canvas.drawText(text, xStartIndex - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
            }
        } else if (maxValueInItems > 0 && maxValueInItems <= 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = paintBottom - eachHeight * i;
                if (startY < topMargin / 2) {
                    break;
                }
//                canvas.drawLine(xStartIndex, startY, mTotalWidth - leftMargin*2, startY, axisPaint);
                float textValue = CalculateUtil.numMathMul(maxDivisionValue, (float) (0.2 * i));
                String text = String.valueOf(textValue);
                canvas.drawText(text, xStartIndex - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                float startY = paintBottom - eachHeight * i;
                //                canvas.drawLine(xStartIndex, startY, mTotalWidth - leftMargin*2, startY, axisPaint);
                String text = String.valueOf(10 * i);
                canvas.drawText(text, xStartIndex - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
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
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPointX = event.getX();
                scroller.abortAnimation();//终止动画
                initOrResetVelocityTracker();
                velocityTracker.addMovement(event);//将用户的移动添加到跟踪器中。
                break;
            case MotionEvent.ACTION_MOVE:
                float movex = event.getX();
                movingThisTime = lastPointX - movex;
                leftMoving = leftMoving + movingThisTime;
                lastPointX = movex;
                invalidate();
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                new Thread(new SmoothScrollThread(movingThisTime)).start();
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity();
                velocityTracker.clear();
                scroller.fling((int) event.getX(), (int) event.getY(), -initialVelocity / 2,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                invalidate();
                lastPointX = event.getX();
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
        if (leftMoving < 0) {
            leftMoving = 0;
        }

        if (leftMoving > (maxRight - minRight)) {
            leftMoving = maxRight - minRight;
        }
    }
    /**
     * 设定两个点之间的间距 和向右边滑动的时候右边的最大距离
     */
    private void getItemsWidth() {
        space = DensityUtil.dip2px(getContext(), 30);
        maxRight = (int) (xStartIndex + space * mData.size());
        minRight = mTotalWidth - leftMargin*2;
    }

    public void setData(List<ChartEntity> list) {
        this.mData = list;
        //计算最大值
        maxValueInItems = list.get(0).getyValue();
        for (ChartEntity entity : list) {
            if (entity.getyValue() > maxValueInItems) {
                maxValueInItems = entity.getyValue();
            }
        }
        getRange(maxValueInItems);
        invalidate();
    }

    /**
     * 得到柱状图的最大和最小的分度值
     *
     * @param maxValueInItems
     */
    private void getRange(float maxValueInItems) {
        int scale = CalculateUtil.getScale(maxValueInItems);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (maxValueInItems / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2

        maxDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
        xStartIndex = CalculateUtil.getDivisionTextMaxWidth(maxDivisionValue,mContext) + 20;
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

package com.chs.easychartwidget.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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
public class LineChartNew extends View {
    private Context mContext;
    /**
     * 背景的颜色
     */
    private static final int BG_COLOR = Color.WHITE;
    /**
     * 视图的宽和高
     */
    private int mTotalWidth, mTotalHeight;
    private int paddingRight, paddingBottom, paddingTop;
    /**
     * x轴 y轴 起始坐标
     */
    private float mStartX, mStartY;
    /**
     * 图表绘制区域的顶部和底部  图表绘制区域的最大高度
     */
    private float paintTop, paintBottom, maxHeight;
    //距离底部的多少 用来显示底部的文字
    private int bottomMargin;
    //距离顶部的多少 用来显示顶部的文字
    private int topMargin;
    /**
     * 左边和上边的边距
     */
    private int leftMargin, rightMargin;
    /**
     * 画笔 背景，轴 ，线 ，text ,点 提示线
     */
    private Paint bgPaint, axisPaint, linePaint, textPaint, pointPaint, hintPaint;
    /**
     * 原点的半径
     */
    private static final float RADIUS = 8;
    private List<ChartEntity> mData;//数据集合
    /**
     * 右边的最大和最小值
     */
    private int maxRight, minRight;
    /**
     * item中的Y轴最大值
     */
    private float maxYValue;
    /**
     * 最大分度值
     */
    private float maxYDivisionValue;
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
     * 绘制的区域
     */
    private RectF mDrawArea, mHintArea;
    private Rect leftWhiteRect, rightWhiteRect;
    /**
     * 保存点的x坐标
     */
    private List<Point> linePoints = new ArrayList<>();
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
    /**
     * 是不是绘制曲线
     */
    private boolean isCurv = false;
    /**
     * 点击的点的位置
     */
    private int selectIndex;
    /**
     * 是否绘制提示文字
     */
    private boolean isDrawHint = false;
    private int hintColor = Color.RED;

    public LineChartNew(Context context) {
        super(context);
        init(context);
    }

    public LineChartNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LineChartNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setWillNotDraw(false);
        mContext = context;

        space = DensityUtil.dip2px(getContext(), 30);
        bottomMargin = DensityUtil.dip2px(getContext(), 30);
        topMargin = DensityUtil.dip2px(context, 30);
        rightMargin = DensityUtil.dip2px(getContext(), 20);
        leftMargin = DensityUtil.dip2px(getContext(), 10);

        scroller = new Scroller(context);
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

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

        float txtSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                12, context.getResources().getDisplayMetrics());
        hintPaint = new Paint();
        hintPaint.setAntiAlias(true);
        hintPaint.setTextSize(txtSize);
        hintPaint.setStyle(Paint.Style.FILL);
        hintPaint.setAlpha(100);
        hintPaint.setStrokeWidth(2);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setColor(hintColor);

        linePath = new Path();
    }

    public void setData(List<ChartEntity> list, boolean isCurv) {
        this.mData = list;
        this.isCurv = isCurv;
        //计算最大值
        if (list.size() > 0) {
            maxYValue = list.get(0).getyValue();
            maxYValue = calculateMax(list);
            getRange(maxYValue);
        }
    }

    /**
     * 计算出Y轴最大值
     *
     * @return
     */
    private float calculateMax(List<ChartEntity> list) {
        float start = list.get(0).getyValue();
        for (ChartEntity entity : list) {
            if (entity.getyValue() > start) {
                start = entity.getyValue();
            }
        }
        return start;
    }

    /**
     * 得到柱状图的最大和最小的分度值
     *
     * @param maxValueInItems
     */
    private void getRange(float maxValueInItems) {
        int scale = CalculateUtil.getScale(maxValueInItems);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (maxValueInItems / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2

        maxYDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
        mStartX = CalculateUtil.getDivisionTextMaxWidth(maxYDivisionValue, mContext) + 20;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();
        maxHeight = h - getPaddingTop() - getPaddingBottom() - bottomMargin - topMargin;
        paddingBottom = getPaddingBottom();
        paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        leftWhiteRect = new Rect(0, 0, 0, mTotalHeight);
        rightWhiteRect = new Rect(mTotalWidth - leftMargin * 2, 0, mTotalWidth, mTotalHeight);
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    //获取滑动范围和指定区域
    private void getArea() {
        if(mData!=null){
            maxRight = (int) (mStartX + space * mData.size());
            minRight = mTotalWidth - leftMargin - rightMargin;
            mStartY = mTotalHeight - bottomMargin - paddingBottom;
            mDrawArea = new RectF(mStartX, paddingTop, mTotalWidth - paddingRight - rightMargin, mTotalHeight - paddingBottom);
            mHintArea = new RectF(mDrawArea.right - mDrawArea.right / 4, mDrawArea.top + topMargin / 2,
                    mDrawArea.right, mDrawArea.top + mDrawArea.height() / 4 + topMargin / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.isEmpty()) return;
        getArea();
        linePoints.clear();
        canvas.drawColor(BG_COLOR);
        //重置线
        linePath.reset();
        linePath.incReserve(mData.size());
        checkTheLeftMoving();
        //画中间的线
        drawWhiteLine(canvas);
        //画左边的Y轴
        canvas.drawLine(mStartX, mStartY, mStartX, topMargin / 2, axisPaint);
        //左边Y轴的单位
        canvas.drawText(leftAxisUnit, mStartX, topMargin / 2 - 14, textPaint);
        //画右边的Y轴
//        canvas.drawLine(mTotalWidth - leftMargin * 2, mStartY, mTotalWidth - leftMargin * 2, topMargin / 2, axisPaint);

        //画X轴 下面的和上面
        canvas.drawLine(mStartX, mStartY, mTotalWidth - leftMargin * 2, mStartY, axisPaint);
//        canvas.drawLine(mStartX, topMargin / 2, mTotalWidth - leftMargin * 2, topMargin / 2, axisPaint);

        //调用clipRect()方法后，只会显示被裁剪的区域
//        canvas.clipRect(mDrawArea.left, mDrawArea.top, mDrawArea.right, mDrawArea.bottom + mDrawArea.height());
        //画线形图
        drawLines(canvas);
        //画左边和右边的遮罩层
        leftWhiteRect.right = (int) mStartX;
        canvas.drawRect(leftWhiteRect, bgPaint);
        canvas.drawRect(rightWhiteRect, bgPaint);

        //画左边的Y轴text
        drawLeftYAxis(canvas);

        //画线上的点
        drawCircles(canvas);
        //画X轴的text
        drawXAxisText(canvas);
        if (isDrawHint) {
            drawHint(canvas);
        }
    }

    private void drawXAxisText(Canvas canvas) {
        //这里设置 x 轴的字一条最多显示3个，大于三个就换行
        for (int i = 0; i < mData.size(); i++) {
            String text = mData.get(i).getxLabel();
            //当在可见的范围内才绘制
            if(linePoints.get(i).x>=mStartX - (textPaint.measureText(text)) / 2&&linePoints.get(i).x<(mTotalWidth-leftMargin*2)){
                if (text.length() <= 3) {
                    canvas.drawText(text, linePoints.get(i).x - (textPaint.measureText(text)) / 2, mTotalHeight - bottomMargin * 2 / 3, textPaint);
                } else {
                    String text1 = text.substring(0, 3);
                    String text2 = text.substring(3, text.length());
                    canvas.drawText(text1, linePoints.get(i).x - (textPaint.measureText(text1)) / 2, mTotalHeight - bottomMargin * 2 / 3, textPaint);
                    canvas.drawText(text2, linePoints.get(i).x - (textPaint.measureText(text2)) / 2, mTotalHeight - bottomMargin / 3, textPaint);
                }
            }
        }
    }

    private void drawWhiteLine(Canvas canvas) {
        axisPaint.setColor(Color.parseColor("#EEEEEE"));
        float eachHeight = (maxHeight / 5f);
        for (int i = 1; i <= 5; i++) {
            float startY = mStartY - eachHeight * i;
            if (startY < topMargin / 2) {
                break;
            }
            canvas.drawLine(mStartX, startY, mTotalWidth - leftMargin * 2, startY, axisPaint);
        }
        axisPaint.setColor(Color.BLACK);
    }

    private float percent = 1f;
    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();

    public void startAnimation(int duration) {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0f, 1);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(pointInterpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                linePaint.setPathEffect(new DashPathEffect(new float[]{pathLength, pathLength}, pathLength - pathLength * percent));
                invalidate();
            }
        });
        mAnimator.start();
    }

    private float pathLength;

    /**
     * 画线形图
     */
    private void drawLines(Canvas canvas) {
        float distance = 0;
//        float lineStart = mStartX + textPaint.measureText(mData.get(0).getxLabel()) / 2 + 20;
        float lineStart = mStartX;
        for (int i = 0; i < mData.size(); i++) {
            distance = space * i - leftMoving;
            float lineHeight = mData.get(i).getyValue() * maxHeight / maxYDivisionValue;
            if (i == 0) {
                linePath.moveTo(lineStart + distance, (mStartY - lineHeight) * percent);
            } else {
                if (!isCurv) {
                    linePath.lineTo(lineStart + distance, (mStartY - lineHeight) * percent);
                } else {
                    float lineHeightPre = mData.get(i - 1).getyValue() * maxHeight / maxYDivisionValue;
                    linePath.cubicTo(lineStart + distance - space / 2, (mStartY - lineHeightPre) * percent,
                            lineStart + distance - space / 2, (mStartY - lineHeight) * percent,
                            lineStart + distance, (mStartY - lineHeight) * percent);
                }

            }
            linePoints.add(new Point((int) (lineStart + distance), (int) (mStartY - lineHeight)));
        }
        PathMeasure measure = new PathMeasure(linePath, false);
        pathLength = measure.getLength();
        canvas.drawPath(linePath, linePaint);
    }

    /**
     * 画线上的点
     */
    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mData.size(); i++) {
            pointPaint.setColor(Color.parseColor("#EF6868"));
//            canvas.drawCircle(linePoints.get(i), (mStartY - mData.get(i).getyValue() * maxHeight / maxYDivisionValue) * percent, RADIUS, pointPaint);
            if(linePoints.get(i).x>=mStartX&&linePoints.get(i).x<(mTotalWidth-leftMargin*2)){
                canvas.drawCircle(linePoints.get(i).x, linePoints.get(i).y * percent, RADIUS, pointPaint);
            }
        }
    }

    /**
     * 画Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     *
     * @param canvas
     */
    private void drawLeftYAxis(Canvas canvas) {
        float eachHeight = (maxHeight / 5f);
        if (maxYValue > 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = mStartY - eachHeight * i;
                if (startY < topMargin / 2) {
                    break;
                }
                BigDecimal maxValue = new BigDecimal(maxYDivisionValue);
                BigDecimal fen = new BigDecimal(0.2 * i);
                String text = null;
                //因为图表分了5条线，如果能除不进，需要显示小数点不然数据不准确
                if (maxYDivisionValue % 5 != 0) {
                    text = String.valueOf(maxValue.multiply(fen).floatValue());
                } else {
                    text = String.valueOf(maxValue.multiply(fen).longValue());
                }
                canvas.drawText(text, mStartX - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
            }
        } else if (maxYValue > 0 && maxYValue <= 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = mStartY - eachHeight * i;
                if (startY < topMargin / 2) {
                    break;
                }
                float textValue = CalculateUtil.numMathMul(maxYDivisionValue, (float) (0.2 * i));
                String text = String.valueOf(textValue);
                canvas.drawText(text, mStartX - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                float startY = mStartY - eachHeight * i;
                String text = String.valueOf(10 * i);
                canvas.drawText(text, mStartX - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
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
                clickAction(event);
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
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
        isDrawHint = false;
        postInvalidate();
        }
    };
    /**
     * 绘制提示文字
     */
    private void drawHint(Canvas canvas) {
        //竖线
        canvas.drawLine(linePoints.get(selectIndex).x, mStartY, linePoints.get(selectIndex).x, topMargin / 2, hintPaint);
        //横线
        canvas.drawLine(mStartX, linePoints.get(selectIndex).y, mTotalWidth - leftMargin * 2, linePoints.get(selectIndex).y, hintPaint);
        hintPaint.setAlpha(60);
        canvas.drawRect(mHintArea, hintPaint);
        hintPaint.setColor(Color.WHITE);
        canvas.drawText("x : " + mData.get(selectIndex).getxLabel(), mHintArea.centerX(), mHintArea.centerY() - 12, hintPaint);
        canvas.drawText("y : " + mData.get(selectIndex).getyValue(), mHintArea.centerX(),
                mHintArea.centerY() + 12 - hintPaint.ascent()-hintPaint.descent(), hintPaint);
        hintPaint.setColor(hintColor);
        postDelayed(mRunnable,800);
    }

    /**
     * 点击X轴坐标或者折线节点
     *
     * @param event
     */
    private void clickAction(MotionEvent event) {
        int range = DensityUtil.dip2px(getContext(), 8);
        float eventX = event.getX();
        float eventY = event.getY();
        for (int i = 0; i < linePoints.size(); i++) {
            //节点
            int x = linePoints.get(i).x;
            int y = linePoints.get(i).y;
            if (eventX >= x - range && eventX <= x + range &&
                    eventY >= y - range && eventY <= y + range) {//每个节点周围4dp都是可点击区域
                selectIndex = i;
                isDrawHint = true;
                removeCallbacks(mRunnable);//移除掉上次点击的runnable
                invalidate();
                return;
            }
        }
    }

    /**
     * 检查向左滑动的距离 确保没有画出屏幕
     */
    private void checkTheLeftMoving() {
        if (leftMoving > (maxRight - minRight)) {
            leftMoving = maxRight - minRight;
        }
        if (leftMoving < 0) {
            leftMoving = 0;
        }
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

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
    private int mTotalWidth, mTotalHeight, maxHeight;
    private int paddingRight, paddingBottom, paddingTop;
    //柱形图的颜色集合
    private int barColors[];
    //距离底部的多少 用来显示底部的文字
    private int bottomMargin;
    //距离顶部的多少 用来显示顶部的文字
    private int topMargin;
    private int rightMargin;
    private int leftMargin;
    /**
     * 画笔 轴 刻度 柱子 点击后的柱子 单位
     */
    private Paint axisPaint, textPaint, barPaint, borderPaint, unitPaint;
    private List<BarChartEntity> mData;//数据集合
    /**
     * item中的Y轴最大值
     */
    private float maxYValue;
    /**
     * Y轴最大的刻度值
     */
    private float maxYDivisionValue;
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
    private int barWidth;
    /**
     * 每个bar之间的距离
     */
    private int barSpace;
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
     * 右边的最大和最小值
     */
    private int maxRight, minRight;
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
    private VelocityTracker velocityTracker;
    private Scroller scroller;
    /**
     * fling最大速度
     */
    private int maxVelocity;
    //x轴 y轴的单位
    private String unitX;
    private String unitY;

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
        barWidth = DensityUtil.dip2px(getContext(), 20);
        barSpace = DensityUtil.dip2px(getContext(), 20);
        topMargin = DensityUtil.dip2px(getContext(), 20);
        bottomMargin = DensityUtil.dip2px(getContext(), 30);
        rightMargin = DensityUtil.dip2px(getContext(), 40);
        leftMargin = DensityUtil.dip2px(getContext(), 10);

        scroller = new Scroller(context);
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

        axisPaint = new Paint();
        axisPaint.setColor(ContextCompat.getColor(mContext, R.color.axis));
        axisPaint.setStrokeWidth(1);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));

        unitPaint = new Paint();
        unitPaint.setAntiAlias(true);
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        unitPaint.setTypeface(typeface);
        unitPaint.setTextSize(DensityUtil.dip2px(getContext(), 10));

        barPaint = new Paint();
        barPaint.setColor(barColors != null && barColors.length > 0 ? barColors[0] : Color.parseColor("#6FC5F4"));

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.rgb(0, 0, 0));
        borderPaint.setAlpha(120);

        mBarRect = new Rect(0, 0, 0, 0);
        mBarRectClick = new Rect(0, 0, 0, 0);
        mDrawArea = new RectF(0, 0, 0, 0);
    }

    public void setData(List<BarChartEntity> list, int colors[], String unitX, String unitY, float max, float min) {
        this.mData = list;
        this.barColors = colors;
        this.unitX = unitX;
        this.unitY = unitY;
        this.max = max;
        this.min = min;
        if (list != null && list.size() > 0) {
            maxYValue = calculateMax(list);
            getRange(maxYValue);
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

    private float maxYDivisionValuez;
    private float maxYDivisionValuef;

    /**
     * 得到柱状图的最大和最小的分度值
     */
    private void getRange(float maxYValue) {
        int scalez = CalculateUtil.getScale(zhengshu);
        float unScaleValuez = (float) (maxYValue / Math.pow(10, scalez));
        maxYDivisionValuez = (float) (CalculateUtil.getRangeTop(unScaleValuez) * Math.pow(10, scalez));

        int scalef = CalculateUtil.getScale(Math.abs(fushu));
        float unScaleValuef = (float) (maxYValue / Math.pow(10, scalef));
        maxYDivisionValuef = (float) (CalculateUtil.getRangeTop(unScaleValuef) * Math.pow(10, scalef));

        maxYDivisionValue = maxYDivisionValuez + maxYDivisionValuef;//获取Y轴的最大的分度值
        mStartX = CalculateUtil.getDivisionTextMaxWidth(maxYDivisionValue, mContext) + 20;
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
        maxHeight = h - getPaddingTop() - getPaddingBottom() - bottomMargin - topMargin;
        paddingBottom = getPaddingBottom();
        paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();

    }

    //获取滑动范围和指定区域
    private void getArea() {
        maxRight = (int) (mStartX + (barSpace + barWidth) * mData.size());
        minRight = mTotalWidth - leftMargin - rightMargin;
        if (fushu == 0) {
            mStartY = mTotalHeight - bottomMargin - paddingBottom;
        } else {//y有负数
            mStartY = (int) (mTotalHeight - bottomMargin - paddingBottom - maxYDivisionValuef / maxYDivisionValue * maxHeight);
        }
        mDrawArea = new RectF(mStartX, paddingTop, mTotalWidth - paddingRight - rightMargin, mTotalHeight - paddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.isEmpty()) return;
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

    private void drawAxis(Canvas canvas) {
        if (fushu >= 0) {
            canvas.drawLine(mStartX, mStartY, mStartX, topMargin, axisPaint);
        } else {
            canvas.drawLine(mStartX, mTotalHeight - bottomMargin - paddingBottom, mStartX, topMargin, axisPaint);
        }
    }

    private void drawUnit(Canvas canvas) {
        String textLength = maxYDivisionValue % 5 == 0 ? String.valueOf((int) maxYDivisionValue) : String.valueOf(maxYDivisionValue);
        canvas.drawText(unitY, mStartX - textPaint.measureText(textLength), topMargin / 2, unitPaint);
        canvas.drawText(unitX, mTotalWidth - rightMargin - paddingRight + 10, mTotalHeight - bottomMargin / 2, unitPaint);
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

    private void drawXAxisText(Canvas canvas) {
        //这里设置 x 轴的字一条最多显示3个，大于三个就换行
        for (int i = 0; i < mData.size(); i++) {
            String text = mData.get(i).getxLabel();
            if (text.length() <= 3) {
                canvas.drawText(text, mBarLeftXPoints.get(i) - (textPaint.measureText(text) - barWidth) / 2, mTotalHeight - bottomMargin * 2 / 3, textPaint);
            } else {
                String text1 = text.substring(0, 3);
                String text2 = text.substring(3, text.length());
                canvas.drawText(text1, mBarLeftXPoints.get(i) - (textPaint.measureText(text1) - barWidth) / 2, mTotalHeight - bottomMargin * 2 / 3, textPaint);
                canvas.drawText(text2, mBarLeftXPoints.get(i) - (textPaint.measureText(text2) - barWidth) / 2, mTotalHeight - bottomMargin / 3, textPaint);
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
                mBarRect.left = (int) (mStartX + barWidth * i + barSpace * (i + 1) - leftMoving);
                if (mData.get(i).getyValue()[0] < 0) {
                    mBarRect.bottom = mStartY + (int) (Math.abs((maxHeight * (mData.get(i).getyValue()[0]) / maxYDivisionValue)) * percent);
                } else {
                    mBarRect.bottom = mStartY;
                }
                if (mData.get(i).getyValue()[0] < 0) {
                    mBarRect.top = mStartY;
                } else {
                    mBarRect.top = mStartY - (int) ((maxHeight * (mData.get(i).getyValue()[0] / maxYDivisionValue)) * percent);
                }
                mBarRect.right = mBarRect.left + barWidth;
                canvas.drawRect(mBarRect, barPaint);
            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
        }
    }

    private void drawScaleLine(Canvas canvas) {
        canvas.drawText("0", mStartX - textPaint.measureText("0") - 5, mStartY + textPaint.measureText("0") / 2, textPaint);
        canvas.drawLine(mStartX, mStartY, mTotalWidth - paddingRight - rightMargin, mStartY, axisPaint);

        canvas.drawText("上限", mStartX - textPaint.measureText("上限") - 5, mStartY - max / maxYDivisionValue * maxHeight + textPaint.measureText("0") / 2, textPaint);
        canvas.drawLine(mStartX, mStartY - max / maxYDivisionValue * maxHeight, mTotalWidth - paddingRight - rightMargin, mStartY - max / maxYDivisionValue * maxHeight, axisPaint);

        canvas.drawText("下限", mStartX - textPaint.measureText("下限") - 5, mStartY - min / maxYDivisionValue * maxHeight + textPaint.measureText("0") / 2, textPaint);
        canvas.drawLine(mStartX, mStartY - min / maxYDivisionValue * maxHeight, mTotalWidth - paddingRight - rightMargin, mStartY - min / maxYDivisionValue * maxHeight, axisPaint);
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
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity();
                velocityTracker.clear();
                scroller.fling((int) event.getX(), (int) event.getY(), -initialVelocity / 2,
                        0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                invalidate();
                lastPointX = event.getX();
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

package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.chs.easychartwidget.entity.ChartEntity;
import com.chs.easychartwidget.utils.DensityUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：chs on 2016/9/6 14:17
 * 邮箱：657083984@qq.com
 */
public class LineChart extends View {
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
    private List<ChartEntity> mData;//风力的集合
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
    private GestureDetector mGestureListener;
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
        mTotalWidth = getMeasuredWidth();
        mTotalHeight = getMeasuredHeight();
        setNeedHeight();
        leftWhiteRect = new Rect(0, 0, 0, mTotalHeight);
        rightWhiteRect = new Rect(mTotalWidth - leftMargin * 2 - 10, 0, mTotalWidth, mTotalHeight);
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
        //画X轴的text 和 线的路径
        drawXAxis(canvas);
        //画线型图
        canvas.drawPath(linePath, linePaint);
        //画线上的点
        drawCircles(canvas);
        //画X轴 下面的和上面
        canvas.drawLine(xStartIndex, yStartIndex, mTotalWidth - leftMargin, yStartIndex, axisPaint);
        canvas.drawLine(xStartIndex, topMargin / 2, mTotalWidth - leftMargin, topMargin / 2, axisPaint);

        //画左边和右边的遮罩层
        leftWhiteRect.right = (int) xStartIndex;
        canvas.drawRect(leftWhiteRect, bgPaint);
        canvas.drawRect(rightWhiteRect, bgPaint);

        //画左边的Y轴
        canvas.drawLine(xStartIndex, yStartIndex, xStartIndex, topMargin / 2, axisPaint);
        //画左边的Y轴text
        drawLeftYAxis(canvas);
        //左边Y轴的单位
        canvas.drawText(leftAxisUnit, xStartIndex, topMargin / 2 - 14, textPaint);
        //画右边的Y轴
        canvas.drawLine(mTotalWidth - leftMargin * 2 - 10, yStartIndex, mTotalWidth - leftMargin * 2 - 10, topMargin / 2, axisPaint);

    }

    private void drawXAxis(Canvas canvas) {
        float distance = 0;
        for(int i = 0;i<mData.size();i++){
            distance = space*i- leftMoving;
            linePoints.add((int) (xStartIndex+distance));
            String text = mData.get(i).getxLabel();
            canvas.drawText(text, xStartIndex+distance, paintBottom + DensityUtil.dip2px(getContext(), 10), textPaint);
            //确定线形图的路径 和 画圆点
            drawLines(i,distance);
        }
    }
    /**
     * 画线形图的路径
     *
     * @param i
     */
    private void drawLines(int i,float distance) {
            float lineHeight = mData.get(i).getyValue() * maxHeight / maxDivisionValue;
            if (i == 0) {
                linePath.moveTo(xStartIndex + distance, paintBottom - lineHeight);
            } else {
                linePath.lineTo(xStartIndex + distance, paintBottom - lineHeight);
            }
    }
    /**
     * 画线上的点
     */
    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < mData.size(); i++) {
                pointPaint.setColor(Color.parseColor("#EF6868"));
            canvas.drawCircle(linePoints.get(i), paintBottom - mData.get(i).getyValue() * maxHeight / maxDivisionValue, RADIUS, pointPaint);
        }
    }
    /**
     * 画Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     * @param canvas
     */
    private void drawLeftYAxis(Canvas canvas) {
        float eachHeight = ((paintBottom - topMargin / 2) / 5f);
        if (maxValueInItems > 1) {
            for (int i = 1; i <= 5; i++) {
                float startY = paintBottom - eachHeight * i;
                if (startY < topMargin / 2) {
                    break;
                }
                canvas.drawLine(xStartIndex, startY, xStartIndex + 10, startY, axisPaint);
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
                canvas.drawLine(xStartIndex, startY, xStartIndex + 10, startY, axisPaint);
                float textValue = numMathMul(maxDivisionValue, (float) (0.2 * i));
                String text = String.valueOf(textValue);
                canvas.drawText(text, xStartIndex - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                float startY = paintBottom - eachHeight * i;
                canvas.drawLine(xStartIndex, startY, xStartIndex + 10, startY, axisPaint);
                String text = String.valueOf(10 * i);
                canvas.drawText(text, xStartIndex - textPaint.measureText(text) - 5, startY + textPaint.measureText("0"), textPaint);
            }
        }
    }
    /**
     * 数字的乘法精度计算
     */
    private float numMathMul(float d1, float d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        float res = b1.multiply(b2).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        return res;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPointX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float movex = event.getRawX();
                movingThisTime = lastPointX - movex;
                leftMoving = leftMoving + movingThisTime;
                lastPointX = movex;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                new Thread(new SmoothScrollThread(movingThisTime)).start();
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
        minRight = mTotalWidth - space - leftMargin;
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
        int scale = getScale(maxValueInItems);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (maxValueInItems / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2

        maxDivisionValue = (float) (getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
        xStartIndex = getDivisionTextMaxWidth(maxDivisionValue) + 20;
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
        BigDecimal bigDecimal = new BigDecimal(maxDivisionValue);
        float max = textPaint.measureText(String.valueOf(bigDecimal.intValue()));
        for (int i = 2; i <= 10; i++) {
            if (maxDivisionValue * 0.1 >= 1) {
                //当数字非常大的时候会出现精度丢失的情况 所以候使用BigDecimal做运算
                BigDecimal bd = new BigDecimal(maxDivisionValue);
                BigDecimal fen = new BigDecimal(0.1 * i);
                String text = String.valueOf(bd.multiply(fen).longValue());
                float w = textPaint.measureText(text);
                if (w > max) {
                    max = w;
                }
            } else {
                max = textPaint.measureText(String.valueOf(maxDivisionValue * 10));
            }
        }
        return max;
    }

    /**
     * 获取这个最大数 数总共有几位
     *
     * @param value
     * @return
     */
    public int getScale(float value) {
        if (value >= 1 && value < 10) {
            return 0;
        }
        if (value == 0) {
            return 0;
        }
        if (value >= 10) {
            return 1 + getScale(value / 10);
        } else {
            return getScale(value * 10) - 1;
        }
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

        if (value < 8.0) {
            return 8.0f;
        }

        return 10.0f;
    }
    /**
     * 左右滑动的时候 当手指抬起的时候  使滑动慢慢停止 不会立刻停止
     */
    private class SmoothScrollThread implements Runnable {
        float lastMoving;
        boolean scrolling = true;

        private SmoothScrollThread(float lastMoving) {
            this.lastMoving = lastMoving;
            scrolling = true;
        }

        @Override
        public void run() {
            while (scrolling) {
                long start = System.currentTimeMillis();
                lastMoving = (int) (0.9f * lastMoving);
                leftMoving += lastMoving;

                checkTheLeftMoving();
                postInvalidate();

                if (Math.abs(lastMoving) < 5) {
                    scrolling = false;
                }

                long end = System.currentTimeMillis();
                if (end - start < 20) {
                    try {
                        Thread.sleep(20 - (end - start));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

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
    private Paint bgPaint, axisPaint, textPaint, barPaint, borderPaint;
    /**
     * 上下左右的白色部分
     */
    private Rect leftWhiteRect, rightWhiteRect, topWhiteRect, bottomWhiteRect;
    /**
     * 矩形柱子  点击后的矩形
     */
    private Rect mBarRect, mBarRectClick;
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
     * 左后一次的x坐标
     */
    private float lastPointX;
    /**
     * 向右边滑动的距离
     */
    private float leftMoving;
    //左边Y轴的单位
    private String leftAxisUnit = "单位";
    /**
     * 当前移动的距离
     */
    private float movingThisTime = 0.0f;
    /**
     * 每一个bar的宽度
     */
    private int barWidth;
    /**
     * 每个bar之间的距离
     */
    private int barSpace;
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
        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());
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

        barPaint = new Paint();
        barPaint.setColor(Color.parseColor("#6FC5F4"));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.rgb(0, 0, 0));
        borderPaint.setAlpha(120);

        mBarRect = new Rect(0, 0, 0, 0);
        mBarRectClick = new Rect(0, 0, 0, 0);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(BG_COLOR);
        if (mData == null) return;
        //得到每个bar的宽度
        getItemsWidth();
        checkTheLeftMoving();
        canvas.drawRect(bottomWhiteRect, bgPaint);
        canvas.drawRect(topWhiteRect, bgPaint);
        //画中间的白线
        drawWhiteLine(canvas);
        //绘制矩形柱子
        drawBars(canvas);
        //画左边和右边的遮罩层
        leftWhiteRect.right = (int) xStartIndex;
        canvas.drawRect(leftWhiteRect, bgPaint);
        canvas.drawRect(rightWhiteRect, bgPaint);
        //画左边的Y轴
        canvas.drawLine(xStartIndex, yStartIndex, xStartIndex, topMargin / 2, axisPaint);
        //左边Y轴的单位
        canvas.drawText(leftAxisUnit, xStartIndex, topMargin / 2 - 14, textPaint);
        //画右边的Y轴
        canvas.drawLine(mTotalWidth - leftMargin * 2, yStartIndex, mTotalWidth - leftMargin * 2, topMargin / 2, axisPaint);
        //画左边的Y轴text
        drawLeftYAxis(canvas);
        //画X轴 下面的和上面
        canvas.drawLine(xStartIndex, yStartIndex, mTotalWidth - leftMargin * 2, yStartIndex, axisPaint);
        canvas.drawLine(xStartIndex, topMargin / 2, mTotalWidth - leftMargin * 2, topMargin / 2, axisPaint);
        //画X轴的text
        drawXAxisText(canvas);
    }

    /**
     * 点击之后绘制点击的地方的边框
     *
     * @param position
     */
    private void drawBorder(int position) {

        mBarRectClick.left = (int) (xStartIndex + barWidth * position + barSpace * (position + 1) - leftMoving);
        mBarRectClick.right = mBarRectClick.left + barWidth;
        mBarRectClick.bottom = mBarRect.bottom;
        mBarRectClick.top = (int) maxHeight + topMargin * 2 - (int) (maxHeight * (mData.get(position).getyValue() / maxValueInItems));
    }

    /**
     * 绘制柱形图
     *
     * @param canvas
     */
    private void drawBars(Canvas canvas) {
        mBarLeftXPoints.clear();
        mBarRightXPoints.clear();
        mBarRect.bottom = mTotalHeight - topMargin / 2;
        for (int i = 0; i < mData.size(); i++) {
            mBarRect.left = (int) (xStartIndex + barWidth * i + barSpace * (i + 1) - leftMoving);
            mBarRect.top = (int) maxHeight + topMargin * 2 - (int) (maxHeight * (mData.get(i).getyValue() / maxValueInItems));
            mBarRect.right = mBarRect.left + barWidth;
            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
//            //在可见的范围内才绘制
//            if (mBarRect.left > xStartIndex && mBarRect.right < (mTotalWidth - leftMargin * 2)) {
            canvas.drawRect(mBarRect, barPaint);
//            }
        }
        if (isDrawBorder) {
            drawBorder(mClickPosition);
            canvas.drawRect(mBarRectClick, borderPaint);
        }
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
        int barMinWidth = DensityUtil.dip2px(getContext(), 40);
        int barMinSpace = DensityUtil.dip2px(getContext(), 10);

        barWidth = (mTotalWidth - leftMargin * 2) / (mData.size() + 3);
        barSpace = (mTotalWidth - leftMargin * 2 - barWidth * mData.size()) / (mData.size() + 1);
        if (barWidth < barMinWidth || barSpace < barMinSpace) {
            barWidth = barMinWidth;
            barSpace = barMinSpace;
        }
        maxRight = (int) (xStartIndex + (barSpace + barWidth) * mData.size()) + barSpace * 2;
        minRight = mTotalWidth - barSpace - leftMargin;
    }

    private void drawWhiteLine(Canvas canvas) {
        axisPaint.setColor(Color.WHITE);
        float eachHeight = ((paintBottom - topMargin / 2) / 5f);
        for (int i = 1; i <= 5; i++) {
            float startY = paintBottom - eachHeight * i;
            if (startY < topMargin / 2) {
                break;
            }
            canvas.drawLine(xStartIndex, startY, mTotalWidth - leftMargin * 2, startY, axisPaint);
        }
        axisPaint.setColor(Color.BLACK);
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
        xStartIndex = CalculateUtil.getDivisionTextMaxWidth(maxDivisionValue, mContext) + 20;
    }

    /**
     * 画Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     *
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

    /**
     * 绘制X轴上的text
     *
     * @param canvas
     */
    private void drawXAxisText(Canvas canvas) {
        float distance = 0;
        for (int i = 0; i < mData.size(); i++) {
            distance = xStartIndex + barWidth * i + barSpace * (i + 1) - leftMoving;
            String text = mData.get(i).getxLabel();
            //当在可见的范围内才绘制
            if ((xStartIndex + distance) >= xStartIndex && (xStartIndex + distance) < (mTotalWidth - leftMargin * 2)) {
                canvas.drawText(text, mBarLeftXPoints.get(i) - (textPaint.measureText(text) - barWidth) / 2, paintBottom + DensityUtil.dip2px(getContext(), 10), textPaint);
            }
        }
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
        if (mGestureListener != null) {
            mGestureListener.onTouchEvent(event);
        }
        return true;
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

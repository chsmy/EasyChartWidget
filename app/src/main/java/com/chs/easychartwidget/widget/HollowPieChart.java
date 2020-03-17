package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.chs.easychartwidget.entity.PieDataEntity;
import com.chs.easychartwidget.utils.CalculateUtil;
import com.chs.easychartwidget.utils.DensityUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 作者：chs on 2016/9/8 16:25
 * 邮箱：657083984@qq.com
 * 空心的饼状图表
 */
public class HollowPieChart extends View {
    public static final int TOUCH_OFFSET = 16;
    private int mTotalWidth, mTotalHeight;
    private float mOutRadius;

    private Paint mPaint, mLinePaint, mTextPaint;

    /**
     * 扇形的绘制区域
     */
    private RectF mOutRectF;
    /**
     * 点击之后的扇形的绘制区域
     */
    private RectF mRectFTouch;

    private List<PieDataEntity> mDataList;
    /**
     * 所有的数据加起来的总值
     */
    private float mTotalValue;
    /**
     * 扇形角度集合
     */
    private float[] angles;
    /**
     * 手点击的部分的position
     */
    private int position = -1;
    /**
     * 点击监听
     */
    private OnItemPieClickListener mOnItemPieClickListener;
    /**
     * 点击某一块之后再次点击回复原状
     */
    private int lastClickedPosition = -1;
    private boolean lastPositionClicked = false;

    public void setOnItemPieClickListener(OnItemPieClickListener onItemPieClickListener) {
        mOnItemPieClickListener = onItemPieClickListener;
    }

    public interface OnItemPieClickListener {
        void onClick(int position);
    }

    public HollowPieChart(Context context) {
        this(context, null);
    }

    public HollowPieChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HollowPieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mOutRectF = new RectF();
        mRectFTouch = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 40));
        mPaint.setStyle(Paint.Style.STROKE);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(Color.BLACK);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(24);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();

        mOutRadius = (float) (Math.min(mTotalWidth, mTotalHeight) / 2 * 0.5);

        mOutRectF.left = -mOutRadius;
        mOutRectF.top = -mOutRadius;
        mOutRectF.right = mOutRadius;
        mOutRectF.bottom = mOutRadius;

        mRectFTouch.left = -mOutRadius - TOUCH_OFFSET;
        mRectFTouch.top = -mOutRadius - TOUCH_OFFSET;
        mRectFTouch.right = mOutRadius + TOUCH_OFFSET;
        mRectFTouch.bottom = mOutRadius + TOUCH_OFFSET;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDataList == null)
            return;
        canvas.translate(mTotalWidth / 2, mTotalHeight / 2);
        //绘制饼图的每块区域
        drawPiePath(canvas);
    }

    /**
     * 绘制饼图的每块区域 和文本
     *
     * @param canvas
     */
    private void drawPiePath(Canvas canvas) {
        //起始地角度
        float startAngle = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            float sweepAngle = mDataList.get(i).getValue() / mTotalValue * 360 - 1;//每个扇形的角度
//            mPath.moveTo(0,0);
            mPaint.setColor(mDataList.get(i).getColor());
            if (position == i) {
                if (lastClickedPosition == position && lastPositionClicked) {
                    canvas.drawArc(mRectFTouch, startAngle, sweepAngle, false, mPaint);
                } else {
                    canvas.drawArc(mOutRectF, startAngle, sweepAngle, false, mPaint);
                }
            } else {
                canvas.drawArc(mOutRectF, startAngle, sweepAngle, false, mPaint);
            }
//            canvas.drawPath(mPath,mPaint);
            Log.i("toRadians", (startAngle + sweepAngle / 2) + "****" + Math.toRadians(startAngle + sweepAngle / 2));
            //确定直线的起始和结束的点的位置
            float pxs = (float) ((mOutRadius + DensityUtil.dip2px(getContext(), 20)) * Math.cos(Math.toRadians(startAngle + sweepAngle / 2)));
            float pys = (float) ((mOutRadius + DensityUtil.dip2px(getContext(), 20)) * Math.sin(Math.toRadians(startAngle + sweepAngle / 2)));
            float pxt = (float) (((mOutRadius + DensityUtil.dip2px(getContext(), 20)) + 30) * Math.cos(Math.toRadians(startAngle + sweepAngle / 2)));
            float pyt = (float) (((mOutRadius + DensityUtil.dip2px(getContext(), 20)) + 30) * Math.sin(Math.toRadians(startAngle + sweepAngle / 2)));
            angles[i] = sweepAngle;
            startAngle += sweepAngle + 1;
            //绘制线和文本
            canvas.drawLine(pxs, pys, pxt, pyt, mLinePaint);
            float res = mDataList.get(i).getValue() / mTotalValue * 100;
            //提供精确的小数位四舍五入处理。
            double resToRound = CalculateUtil.round(res, 2);
            float v = startAngle % 360;
            if (startAngle % 360.0 >= 90.0 && startAngle % 360.0 <= 270.0) {
                canvas.drawText(resToRound + "%", pxt - mTextPaint.measureText(resToRound + "%"), pyt, mTextPaint);
            } else {
                canvas.drawText(resToRound + "%", pxt, pyt, mTextPaint);
            }
        }
    }

    public void setDataList(List<PieDataEntity> dataList) {
        this.mDataList = dataList;
        mTotalValue = 0;
        for (PieDataEntity pieData : mDataList) {
            mTotalValue += pieData.getValue();
        }
        angles = new float[mDataList.size()];
        invalidate();
    }
    /**
     * 这里使用角度的方式来确定点击的位置
     * 在{@link PieChart } 中使Region的方式来判断点击的位置
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX() - (mTotalWidth / 2);
                float y = event.getY() - (mTotalHeight / 2);
                //计算出角度
                //Math.atan2  返回从原点（0,0） 到 （x,y）的线与x轴正方向的弧度值
                //注意Math.atan2的参数是y,x
                float touchAngle = (float) Math.toDegrees(Math.atan2(y,x));
                //坐标1,2象限返回-180~0  3,4象限返回0~180
                if(x<0&&y<0 || x>0&&y<0){//1,2象限
                    touchAngle = touchAngle + 360 ;
                }
                float touchRadius = (float) Math.sqrt(y * y + x * x);
                if (touchRadius < mOutRadius +TOUCH_OFFSET*2 && touchRadius > mOutRadius * 0.5-TOUCH_OFFSET*2) {
                    if (angles != null)
                        position = getClickPosition(touchAngle);
                    if (lastClickedPosition == position) {
                        lastPositionClicked = !lastPositionClicked;
                    } else {
                        lastPositionClicked = true;
                        lastClickedPosition = position;
                    }
                    invalidate();
                    if (mOnItemPieClickListener != null) {
                        mOnItemPieClickListener.onClick(position);
                    }
                }
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    private int getClickPosition(float touchAngle) {
        int position = 0;
        int totalAngle = 0;
        for (int i = 0; i < angles.length; i++) {
            totalAngle += angles[i];
            if (touchAngle <= totalAngle) {
                position = i;
                break;
            }
        }
        return position;
    }

}

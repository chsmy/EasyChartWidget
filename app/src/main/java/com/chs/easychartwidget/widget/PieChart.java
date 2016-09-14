package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.chs.easychartwidget.entity.PieDataEntity;
import com.chs.easychartwidget.utils.CalculateUtil;

import java.util.List;

/**
 * 作者：chs on 2016/9/8 16:25
 * 邮箱：657083984@qq.com
 */
public class PieChart extends View {
    /**
     * 视图的宽和高
     */
    private int mTotalWidth, mTotalHeight;
    /**
     * 绘制区域的半径
     */
    private float mRadius;

    private Paint mPaint,mLinePaint,mTextPaint;

    private Path mPath;

    private RectF mRectF;

    private List<PieDataEntity> mDataList;
    /**
     * 所有的数据加起来的总值
     */
    private float mTotalValue;
    public PieChart(Context context) {
        super(context);
        init(context);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRectF = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(Color.BLACK);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(24);

        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();

        mRadius = (float) (Math.min(mTotalWidth,mTotalHeight)/2*0.7);

        mRectF.left = -mRadius;
        mRectF.top = -mRadius;
        mRectF.right = mRadius;
        mRectF.bottom = mRadius;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mDataList==null)
            return;
        canvas.translate(mTotalWidth/2,mTotalHeight/2);
        //绘制饼图的每块区域
        drawPiePath(canvas);

    }

    /**
     * 绘制饼图的每块区域 和文本
     * @param canvas
     */
    private void drawPiePath(Canvas canvas) {
        float startAngle = 0;
        for(int i = 0;i<mDataList.size();i++){
            float sweepAngle = mDataList.get(i).getValue()/mTotalValue*360-1;
            mPath.moveTo(0,0);
            mPath.arcTo(mRectF,startAngle,sweepAngle);
            mPaint.setColor(mDataList.get(i).getColor());
            canvas.drawPath(mPath,mPaint);
            mPath.reset();
            float pxs = (float) (mRadius*Math.cos(Math.toRadians(startAngle+sweepAngle/2)));
            float pys = (float) (mRadius*Math.sin(Math.toRadians(startAngle+sweepAngle/2)));
            float pxt = (float) ((mRadius+30)*Math.cos(Math.toRadians(startAngle+sweepAngle/2)));
            float pyt = (float) ((mRadius+30)*Math.sin(Math.toRadians(startAngle+sweepAngle/2)));
            startAngle += sweepAngle+1;
            //绘制线和文本
            canvas.drawLine(pxs,pys,pxt,pyt,mLinePaint);
            float res = mDataList.get(i).getValue() / mTotalValue * 100;
            double resToRound = CalculateUtil.round(res,2);
            float v = startAngle % 360;
            if (startAngle % 360.0 >= 90.0 && startAngle % 360.0 <= 270.0) {
                canvas.drawText(resToRound+"%",pxt-mTextPaint.measureText(resToRound+"%"),pyt,mTextPaint);
            }else {
                canvas.drawText(resToRound+"%",pxt,pyt,mTextPaint);
            }
        }
    }

    public void setDataList(List<PieDataEntity> dataList){
        this.mDataList = dataList;
        mTotalValue = 0;
        for(PieDataEntity pieData :mDataList){
            mTotalValue +=pieData.getValue();
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x = event.getX()-(mTotalWidth/2);
                float y = event.getY()-(mTotalHeight/2);
                float touchAngle = 0;
                if (x<0&&y<0){
                    touchAngle += 180;
                }else if (y<0&&x>0){
                    touchAngle += 360;
                }else if (y>0&&x<0){
                    touchAngle += 180;
                }
                touchAngle +=Math.toDegrees(Math.atan(y/x));
//                touchAngle = touchAngle-mPieAxisData.getStartAngle();
                if (touchAngle<0){
                    touchAngle = touchAngle+360;
                }
                float touchRadius = (float) Math.sqrt(y*y+x*x);
                break;
        }
        return super.onTouchEvent(event);
    }
}

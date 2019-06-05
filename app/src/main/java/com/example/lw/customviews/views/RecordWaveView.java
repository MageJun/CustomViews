package com.example.lw.customviews.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecordWaveView extends View {
    public RecordWaveView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(COLOR);
    }
    private static final int COLOR = 0xFF1E90FF;

    public RecordWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(COLOR);
    }

    public RecordWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(COLOR);
    }

    private int mHeight = 100;
    private int mWidth = 100;
    private float mStrokeWidth = 6;
    private float mSpace = 18;
    private float mCenterLineY;
    private Paint mPaint ;



    private List<Point> mPointList;
    class Point{
        float l,t,r,b;
    }

    private void updatePoint(float value) {
        int mid = mPointList.size()/2;
        for (int i = 0;i<mPointList.size();i++){
            Point point = mPointList.get(i);
            float tmp = Math.abs(i-mid);
            float tmp_d ;
            if(tmp==mid){
                tmp_d = 2f/5f*value;
            }else if(i == mid){
                tmp_d = 4f/5f*value;
            }else{
                tmp_d = 3f/5f*value;
            }

            point.t = mCenterLineY-tmp_d-mStrokeWidth/2;
            point.b = mCenterLineY+tmp_d+mStrokeWidth/2;

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = (int) (mSpace*6);
        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenterLineY = mHeight/2;
        mPointList = new ArrayList<>();
        float tt = 1f/6f;
        for (int i = 1;i<6;i++){
            Point point = new Point();
            point.l = i*mSpace;
            point.t = mCenterLineY-mStrokeWidth/2;
            point.r = point.l+mStrokeWidth;
            point.b = mCenterLineY+mStrokeWidth/2;
            mPointList.add(point);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Point point:mPointList
             ) {
            drawLinkPoint(canvas,point);
        }
    }

    private void drawLinkPoint(Canvas canvas,Point point) {
        canvas.drawRect(point.l,point.t,point.r,point.b,mPaint);
    }


    /**
     * 更新振幅幅度大小
     * @param range
     */
    public void updateRange(float range){
        if(range<=0){
            range = 0;
        }
        if(range>mHeight/2-mSpace){
            range = mHeight/2-mSpace;
        }
        invalidateRange(range);
    }

    private void invalidateRange(float range) {
        Log.i("RecordView","range = "+range);
        startAninator(range);
    }
    private void startAninator(float range){
        ValueAnimator animator = ValueAnimator.ofFloat(0,range);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                updatePoint(value);
                invalidate();
            }
        });
        animator.start();
    }

}

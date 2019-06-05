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

public class RecordView extends View {
    public RecordView(Context context) {
        super(context);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(Color.GRAY);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int mHeight = 100;
    private int mWidth = 100;
    private float mStrokeWidth = 10;
    private float mSpace = 15;
    private float mCenterLineX,mCenterLineY;
    private Point mStartPoint,mEndPoint,mControlPoint;
    private Paint mPaint ;


    private List<Point> mPointList;
    class Point{
        float x,y;
        float t;

        @Override
        public String toString() {
            return "x = "+x+",y = "+y+",t="+t;
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            startAninator();
            return false;
        }
    });

    private void startAninator(){
            ValueAnimator animator = ValueAnimator.ofFloat(mControlPoint.y,mControlPoint.y-20);
            animator.setRepeatCount(1);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setDuration(3000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mControlPoint.y = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.start();
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
        mStartPoint = new Point();
        mEndPoint = new Point();
        mControlPoint = new Point();
        mStartPoint.x = 0;
        mStartPoint.y = mCenterLineY-mStrokeWidth;
        mEndPoint.x = mStartPoint.x+mWidth;
        mEndPoint.y = mStartPoint.y;
        mControlPoint.x = mStartPoint.x+(mEndPoint.x-mStartPoint.x)/2;
        mControlPoint.y = mStartPoint.y;
        mPointList = new ArrayList<>();
        float tt = 1f/6f;
        for (int i = 1;i<6;i++){
            if(i==3){
                continue;
            }
            Point point = new Point();
            point.x = i*mSpace
                    +mStartPoint.x;
            point.y = mStartPoint.y;

            point.t = i*tt;
            mPointList.add(point);
        }
        mHandler.sendEmptyMessageDelayed(0,3000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.YELLOW);
        drawLinkPoint(canvas,mControlPoint);
        for (Point point:mPointList
             ) {
            updatePoint(point,point.t);
            drawLinkPoint(canvas,point);
        }
    }

    private void drawLinkPoint(Canvas canvas,Point point) {
        canvas.drawLine(point.x,mHeight/2,point.x,point.y,mPaint);
    }
    private void updatePoint(Point p,float t){
        //求出起点和控制点连线上得Q1点坐标
        float s_length_c = calcPointLenght(mStartPoint,mControlPoint);
        float s_length_q1 = s_length_c*t;
        double s_tan_c = Math.atan2(Math.abs(mControlPoint.y-mStartPoint.y),Math.abs(mControlPoint.x-mStartPoint.x));
        float q1_x = (float) (mStartPoint.x+s_length_q1*Math.sin(s_tan_c));
        float q1_y = (float) (mStartPoint.y-s_length_q1*Math.cos(s_tan_c));
        //求出终点和控制点连线上得Q2点坐标
        float e_length_c = calcPointLenght(mEndPoint,mControlPoint);
        float c_length_q2 = e_length_c*t;
        double e_tan_c = Math.atan2(Math.abs(mControlPoint.x-mEndPoint.x),Math.abs(mControlPoint.y-mEndPoint.y));
        float q2_x = (float) (mControlPoint.x+c_length_q2*Math.sin(e_tan_c));
        float q2_y = (float) (mControlPoint.y+c_length_q2*Math.cos(e_tan_c));

        //计算出Point此时在贝塞尔曲线上得y坐标
        float q1_length_q2 = (float) Math.sqrt((q1_x-q2_x)*(q1_x-q2_x)+(q1_y-q2_y)*(q1_y-q2_y));
        float q1_length_b = q1_length_q2*t;
        double q1_tan_q2 = Math.atan2(Math.abs(q1_y-q2_y),Math.abs(q1_x-q2_x));
        float dy = (float) (Math.sin(q1_tan_q2)*q1_length_b);
        if(p.x>q1_x){
            p.y = q1_y-dy;
        }else{
            p.y = q1_y+dy;
        }

        Log.i("RecordView","p = "+p.toString());
    }

    private float calcPointLenght(Point p1,Point p2){
        float x = Math.abs(p1.x-p2.x);
        float y = Math.abs(p1.y-p2.y);
        return (float) Math.sqrt(x*x+y*y);
    }


}

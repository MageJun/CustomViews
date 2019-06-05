package com.example.lw.customviews.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

public class RecordGroupView extends LinearLayout {
    private static final int BG_COLOR = 0xFFF5F5F5;
    private InnerWaveGroupView mWaveGroupView;
    public RecordGroupView(Context context) {
        super(context);
    }

    public RecordGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.setBackgroundColor(BG_COLOR);
        this.setOrientation(VERTICAL);
        this.setPadding(0,100,0,100);
        mWaveGroupView= new InnerWaveGroupView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;

        this.addView(mWaveGroupView,params);
        mWaveGroupView.setVisibility(View.INVISIBLE);

        CircleRecordView circleView = new CircleRecordView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.CENTER_HORIZONTAL;
        params2.topMargin = 25;
        circleView.setListener(new RecordStateListener() {
            @Override
            public void recordStateChange(RecordState state) {
                if(state!=RecordState.STOPPED){
                    mWaveGroupView.setVisibility(View.VISIBLE);
                }else{
                    mWaveGroupView.setVisibility(View.INVISIBLE);
                }
                mWaveGroupView.updateRecordState(state);
            }
        });
        this.addView(circleView,params2);
    }

    public RecordGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mHandler.sendEmptyMessageDelayed(0,1000);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        private Random random = new Random();
        @Override
        public boolean handleMessage(Message msg) {
            float range = random.nextFloat()*50;
            updateRecordingWave(range);
            mHandler.sendEmptyMessageDelayed(0,500);
            return false;
        }
    });

    public void updateRecordingWave(float range){
        mWaveGroupView.updateRange(range);
    }


    /**
     * 录音音频振幅和文字提示页面
     */
    class InnerWaveGroupView extends RelativeLayout{



        public InnerWaveGroupView(Context context) {
            super(context);
            initView(context);
        }

        public InnerWaveGroupView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context);
            setBackgroundColor(Color.YELLOW);
        }

        public InnerWaveGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        private RecordWaveView mLeftWave,mRightWave;
        private TextView mText;
        private int mPadding = 10;
        private void initView(Context context) {
            this.setPadding(mPadding,mPadding,mPadding,mPadding);
            mLeftWave = new RecordWaveView(context);
            mRightWave = new RecordWaveView(context);
            mText = new TextView(context);
            mText.setText("按下录音");
            mText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.leftMargin = 80;
            params.rightMargin = 80;
            mText.setId(View.generateViewId());
            mText.setGravity(Gravity.CENTER);
            this.addView(mText,params);

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.LEFT_OF,mText.getId());
            params2.addRule(RelativeLayout.CENTER_VERTICAL);
            this.addView(mLeftWave,params2);

            RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params3.addRule(RelativeLayout.RIGHT_OF,mText.getId());
            params3.addRule(RelativeLayout.CENTER_VERTICAL);
            this.addView(mRightWave,params3);


        }

        public void updateRange(float range){
            mLeftWave.updateRange(range);
            mRightWave.updateRange(range);
        }

        public void updateRecordState(RecordState state){
            switch (state){
                case PRESTOP:
                    mText.setTextColor(Color.RED);
                    break;
                case RECORDING:
                    mText.setTextColor(Color.BLACK);
                    break;
                case STOPPED:
                    mText.setTextColor(Color.BLACK);
                    break;
            }
        }
    }


    /**
     * 圆形录音按钮View
     */

    class CircleRecordView extends View{

        private float mRadius = 150;
        private float mPadding = 10;
        private float mStrokeWidth = 5;
        private int mWidth,mHeight;
        private Paint mCirclePaint;
        private float mCenterX,mCenterY;
        private RecordState mState = RecordState.STOPPED;
        private RecordStateListener mListener;
        public CircleRecordView(Context context) {
            super(context);
            init(context);
        }

        public CircleRecordView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        private void init(Context context) {
            mCirclePaint = new Paint();
            mCirclePaint.setAntiAlias(true);
            mCirclePaint.setStyle(Paint.Style.FILL);
            mCirclePaint.setColor(Color.WHITE);
        }

        public CircleRecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void setListener(RecordStateListener listener){
            this.mListener = listener;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(mState==RecordState.STOPPED){
                        changeState(RecordState.RECORDING);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();
                    float length = (float) Math.sqrt((x-mCenterX)*(x-mCenterX)+(y-mCenterY)*(y-mCenterY));
                    if(length>=mRadius){
                        changeState(RecordState.PRESTOP);
                    }else if(RecordState.RECORDING!=mState){
                        changeState(RecordState.RECORDING);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    changeState(RecordState.STOPPED);
                    break;
            }
            return true;
        }

        private void changeState(RecordState state) {
            mState = state;
            if(mListener!=null){
                mListener.recordStateChange(mState);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            mWidth = (int) ((mRadius+mPadding+mStrokeWidth)*2);
            mHeight = mWidth;
            this.setMeasuredDimension(mWidth,mHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mCenterX = mWidth/2;
            mCenterY = mHeight/2;
            canvas.drawCircle(mCenterX,mCenterY,mRadius,mCirclePaint);
        }
    }

    enum RecordState{
        RECORDING,
        PRESTOP,
        STOPPED;
    }
  interface RecordStateListener{
        void recordStateChange(RecordState state);
  }

}

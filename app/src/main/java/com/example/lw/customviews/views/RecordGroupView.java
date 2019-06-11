package com.example.lw.customviews.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
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

import com.example.lw.customviews.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RecordGroupView extends LinearLayout {
    private static final int BG_COLOR = 0xFFF5F5F5;
    private static final int PRESTOP_COLOR = 0xFFFF4081;
    private static final int RECORDING_COLOR = 0xFF49B1E9;
    private InnerWaveGroupView mWaveGroupView;
    public RecordGroupView(Context context) {
        super(context);
    }

    private static final int MSG_UPDATE_RANGE = 0x1;

    enum RecordState{
        RECORDING,
        PRESTOP,
        STOPPED;
    }
    interface RecordStateListener{
        void recordStateChange(RecordState state);
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
                    mHandler.sendEmptyMessage(MSG_UPDATE_RANGE);
                }else{
                    mWaveGroupView.setVisibility(View.INVISIBLE);
                    mHandler.removeMessages(MSG_UPDATE_RANGE);
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
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        private Random random = new Random();
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what==MSG_UPDATE_RANGE){
                float range = random.nextFloat()*50;
                updateRecordingWave(range);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RANGE,1000);
            }
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
        private static final int MSG_UPDATE_TIMECOUNT = 0x2;
        private RecordWaveView mLeftWave,mRightWave;
        private TextView mText;
        private int mPadding = 10;
        private Timer mTimer;
        private InnerTimerTask mTask;
        private int timeDelay = 0;
        private RecordState mState = RecordState.STOPPED;
        private boolean isStartRecording = false;

        private void initView(Context context) {
            this.setPadding(mPadding,mPadding,mPadding,mPadding);
            mLeftWave = new RecordWaveView(context);
            mRightWave = new RecordWaveView(context);
            mText = new TextView(context);
            mText.setTextSize(14);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.leftMargin = 80;
            params.rightMargin = 80;
            mText.setPadding(50,50,50,50);
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
            mState = state;
            switch (state){
                case PRESTOP:
                    mText.setTextColor(PRESTOP_COLOR);
                    break;
                case RECORDING:
                  if(!isStartRecording){
                      isStartRecording = true;
//                      mHandler.sendEmptyMessageDelayed(0,1000);
                         if(mTimer==null&&mTask==null){
                            mTimer = new Timer();
                             mTask = new InnerTimerTask();
                            mTimer.schedule(mTask,1000,1000);
                    }
                  }
                    mText.setTextColor(Color.BLACK);
                    break;
                case STOPPED:
                    isStartRecording = false;
                    resetTimer();
                    timeDelay = 0;
                    mText.setTextColor(Color.BLACK);
                    break;
            }
            mText.setText(createTextStr());
        }

        private void resetTimer() {
            if(mTask!=null){
                mTask.cancel();
                mTask = null;
            }
            if(mTimer!=null){
                mTimer.cancel();
                mTimer = null;
            }
        }

        private String createTextStr(){
            StringBuffer sb = new StringBuffer();
            switch (mState){
                case PRESTOP:
                    sb.append("松开取消");
                    break;
                case RECORDING:
                    sb.append("上滑取消");
                    break;
                case STOPPED:
                    sb.append("松开取消");
                    break;
            }
            sb.append(timeDelay+"\"");
            return sb.toString();
        }

        private Handler mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what==MSG_UPDATE_TIMECOUNT){
                    if(isStartRecording){
                        String str = createTextStr();
                        mText.setText(str);
                        if(timeDelay==60){
                            resetTimer();
                        }
                    }
                }
                return false;
            }
        });
        class InnerTimerTask extends TimerTask {

            @Override
            public void run() {
                timeDelay++;
                mHandler.sendEmptyMessage(MSG_UPDATE_TIMECOUNT);
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
        private Bitmap mRecrodIcon,mDeleIcon;

        private Path mTmpProgressPath;
        private PathMeasure mPathMeasure;
        private Paint mProgressPaint;
        private float mPathRadius = mRadius+mStrokeWidth;
        private static final long MAX_RECORD_TIME = 60*1000l;//最大录音时长，1分钟
        private float mCurrentProgress;
        private boolean isStartRecording = false;

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
            mCirclePaint.setStrokeWidth(mStrokeWidth);

            mProgressPaint = new Paint();
            mProgressPaint.setAntiAlias(true);
            mProgressPaint.setStyle(Paint.Style.STROKE);
            mProgressPaint.setColor(RECORDING_COLOR);
            mProgressPaint.setStrokeWidth(mStrokeWidth);

            mTmpProgressPath = new Path();
            mPathMeasure = new PathMeasure();

            mRecrodIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_chat_voice_speak);
            mDeleIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_voice_delete);

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
            if(mState==state){
                return;
            }
            mState = state;
            if(mListener!=null){
                mListener.recordStateChange(mState);
            }

            switch (state){
                case RECORDING:
                    mProgressPaint.setColor(RECORDING_COLOR);
                    if(!isStartRecording){
                        isStartRecording = true;
                        startProgressAnimatior();
                    }
                    break;
                case STOPPED:
                    mCurrentProgress = 0;
                    mProgressPaint.setColor(RECORDING_COLOR);
                    isStartRecording = false;
                    break;
                case PRESTOP:
                    mProgressPaint.setColor(PRESTOP_COLOR);
                    break;
            }
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            mWidth = (int) ((mPathRadius+mPadding+mStrokeWidth)*2);
            mHeight = mWidth;
            this.setMeasuredDimension(mWidth,mHeight);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            mCenterX = mWidth/2;
            mCenterY = mHeight/2;
            float round_w = mWidth/4;
            float round_h = mHeight/4;
            mRecrodIcon = scaleIcons(mRecrodIcon,round_w,round_h);
            mDeleIcon = scaleIcons(mDeleIcon,round_w,round_h);

            //初始化进度圆的Path
            mTmpProgressPath.reset();
            mTmpProgressPath.moveTo(mCenterX,mCenterY-mPathRadius);
            mTmpProgressPath.addCircle(mCenterX,mCenterY,mPathRadius,Path.Direction.CW);
            //将Path绑定到PathMeasure上，用来截取片段
            mPathMeasure.setPath(mTmpProgressPath,true);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(mCenterX,mCenterY,mRadius,mCirclePaint);
            drawIcon(canvas);
            if(mCurrentProgress!=0){
                canvas.save();
                Path dstPath = new Path();
                dstPath.lineTo(0,0);
                mPathMeasure.getSegment(0,mCurrentProgress,dstPath,true);
                //画布逆时针旋转90度，保证进度起点位于正上方
                canvas.rotate(-90,mCenterX,mCenterY);
                canvas.drawPath(dstPath,mProgressPaint);
                canvas.restore();
            }
        }

        private void drawIcon(Canvas canvas) {
            if(mState==RecordState.RECORDING){
                canvas.drawBitmap(mRecrodIcon,mCenterX-mRecrodIcon.getWidth()/2,mCenterY-mRecrodIcon.getHeight()/2,null);
            }else if(mState == RecordState.PRESTOP){
                canvas.drawBitmap(mDeleIcon,mCenterX-mRecrodIcon.getWidth()/2,mCenterY-mRecrodIcon.getHeight()/2,null);
            }
        }
        private Bitmap scaleIcons(Bitmap bitmap,float round_w, float round_h) {
            if(bitmap.getHeight()>=bitmap.getWidth()){
                round_w = round_h*bitmap.getWidth()/bitmap.getHeight();
            }else{
                round_h = round_w*bitmap.getHeight()/bitmap.getWidth();
            }
            Bitmap newBitmap = Bitmap.createBitmap((int) round_w,(int) round_h,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.save();
            canvas.scale(round_w/bitmap.getWidth(),round_h/bitmap.getHeight());
            canvas.drawBitmap(bitmap,0,0,null);
            canvas.restore();
            return newBitmap;
        }

        private void startProgressAnimatior(){
            final ValueAnimator animator = ValueAnimator.ofFloat(1f,mPathMeasure.getLength());
            animator.setDuration(MAX_RECORD_TIME);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(!isStartRecording){
                        mCurrentProgress = 0;
                        animator.cancel();
                        return;
                    }
                    mCurrentProgress = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.start();
        }

    }

}

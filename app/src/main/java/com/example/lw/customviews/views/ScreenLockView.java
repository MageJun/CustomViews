package com.example.lw.customviews.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScreenLockView extends View {

    enum State{

        STATE_NORMAL,
        STATE_SELECTING,
        STATE_RIGHT,
        STATE_WRONG;
    }

    class Node{
        float x,y;//圆点坐标
        float radius;
        private boolean isSelected = false;
        private int tag = -1;
        private State mState = State.STATE_NORMAL;


        public Node(int tag,float radius){
            this.tag = tag;
            this.radius =radius;
        }

        public int getTag() {
            return tag;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public void setState(State state) {
            this.mState = state;
        }

        public State getState() {
            return mState;
        }
    }

    private boolean isSelecting = false;
    private boolean isChecking = false;

    private int mColor_Normal = Color.GRAY;
    private int mColor_Select = Color.YELLOW;
    private int mColor_Right = Color.GREEN;
    private int mColor_Wrong = Color.RED;

    private int mWidth,mHeight;
    private List<Node> mNodeList;
    private List<Node> mSelectNodeList;

    private Paint mNodePaint,mPathPaint;
    private Canvas mNodeCanvas;
    private Bitmap mNodeBitmap;
    private Path mPath,mTmpPath;
    private float mPathStartX,mPathStartY;
    private float mRadius=18;
    private float mRoundPadding = 20;
    private float mRadiusRate = 2f;


    public ScreenLockView(Context context) {
        super(context);
    }

    public ScreenLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mNodeList = new ArrayList<>();
        mSelectNodeList = new ArrayList<>();
        mNodePaint = new Paint();
        mNodePaint.setAntiAlias(true);
        mNodePaint.setStyle(Paint.Style.FILL);
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(8);
    }

    public ScreenLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScreenLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Handler mHanlder = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            reset();
            invalidate();
            isChecking = false;
            return false;
        }
    });

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if(modeHeight == MeasureSpec.AT_MOST||modeHeight == MeasureSpec.UNSPECIFIED){
            mHeight = mWidth;
            setMeasuredDimension(mWidth,mHeight);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mWidth<=0||mHeight<=0){
            return;
        }
        mNodeBitmap = Bitmap.createBitmap(mWidth,mHeight,Bitmap.Config.ARGB_8888);
        mNodeCanvas = new Canvas(mNodeBitmap);
        mPath = new Path();
        mTmpPath = new Path();
        float radius = /*mWidth/10;*/mRadius;
        float heightSpace = (mHeight-mRoundPadding*2-mRadius*2*3)/2;
        float widthSpace  = (mWidth-mRoundPadding*2-mRadius*2*3)/2;
        mNodeList.clear();
        mSelectNodeList.clear();
        int count = 1;
        for (int j = 1;j<=3;j++){
            for(int i = 1;i<=3;i++){
                Node  node = new Node(count,radius);
                node.x = mRoundPadding+radius+(i-1)*(widthSpace+2*radius);
                node.y = mRoundPadding+radius+(j-1)*(heightSpace+2*radius);
                mNodeList.add(node);
                count++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mNodeBitmap,0,0,null);
        mNodeCanvas.drawColor(0,PorterDuff.Mode.CLEAR);
        for (int i = 0;i<mNodeList.size();i++){
            drawNode(mNodeList.get(i));
        }

        canvas.drawPath(mPath,mPathPaint);
    }

    private void drawNode(Node node) {
        if(node!=null){
            mNodePaint.setColor(getColor(node.getState()));
            mNodeCanvas.drawCircle(node.x,node.y,node.radius,mNodePaint);
        }
    }

    private int getColor(State state) {
        switch (state){
            case STATE_NORMAL:
                return mColor_Normal;
            case STATE_SELECTING:
                return  mColor_Select;
            case STATE_RIGHT:
                return mColor_Right;
            case STATE_WRONG:
                return  mColor_Wrong;
                default:
                    return mColor_Normal;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isChecking){
            return true;
        }
        float x = event.getX();
        float y = event.getY();
        Node node = getFreeNode(x,y);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                reset();
                if(node!=null){
                    isSelecting = true;
                    node.setState(State.STATE_SELECTING);
                    mPathStartX = node.x;
                    mPathStartY = node.y;
                    mTmpPath.moveTo(mPathStartX,mPathStartY);
                    addSelectNode(node);

                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isSelecting){
                    mPath.reset();
                    mPath.addPath(mTmpPath);
                    mPath.moveTo(mPathStartX,mPathStartY);
                    mPath.lineTo(x,y);
                    if(node!=null){
                        Node midNode = getMidNode(node.x,node.y);
                        if(midNode!=null){
                            midNode.setState(State.STATE_SELECTING);
                            mPathStartY = midNode.y;
                            mPathStartX = midNode.x;
                            mTmpPath.lineTo(mPathStartX,mPathStartY);
                            addSelectNode(midNode);
                        }

                       node.setState(State.STATE_SELECTING);
                       mPathStartY = node.y;
                       mPathStartX = node.x;
                       mTmpPath.lineTo(mPathStartX,mPathStartY);
                        addSelectNode(node);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                isSelecting = false;
                if(mSelectNodeList.size()>0){
                    isChecking = true;
                    mPath.reset();
                    mPath.addPath(mTmpPath);
                    boolean result = checkPwd();
                    setResultState(result);
                    mHanlder.sendEmptyMessageDelayed(0,1000);
                }
                break;
        }
        invalidate();
        return true;
    }

    private void addSelectNode(Node node) {
        mSelectNodeList.add(node);
        startNodeAninator(node);
    }

    private Node getMidNode(float x, float y) {
        float targetx = mPathStartX+(x-mPathStartX)/2;
        float targety = mPathStartY+ (y-mPathStartY)/2;
        return getFreeNode(targetx,targety);
    }


    private void setResultState(boolean result) {
        setSelectNodeState(result?State.STATE_RIGHT:State.STATE_WRONG);
        mPathPaint.setColor(result?mColor_Right:mColor_Wrong);

    }

    private void setSelectNodeState(State state) {
        for (int i = 0;i<mSelectNodeList.size();i++){
            mSelectNodeList.get(i).setState(state);
        }
    }

    private void initAllNodeState(){
        for (Node node:mNodeList
             ) {
            node.setState(State.STATE_NORMAL);
        }
    }

    private Node getNode(float x,float y){
        for (int i = 0;i<mNodeList.size();i++){
            Node node = mNodeList.get(i);
           if(Math.abs(x-node.x)<node.radius*mRadiusRate&&Math.abs(y-node.y)<node.radius*mRadiusRate){
               return node;
           }
        }
        return null;
    }

    private Node getFreeNode(float x,float y){
        Node node = getNode(x,y);
        if(mSelectNodeList.size()==0||!mSelectNodeList.contains(node)){
            return node;
        }
        return null;
    }

    private void reset(){
        mPath.reset();
        mTmpPath.reset();
        isSelecting = false;
        initAllNodeState();
        mSelectNodeList.clear();
        mPathStartX = 0;
        mPathStartY = 0;
        mPathPaint.setColor(mColor_Select);

    }
    private String testPwd = "123456";
    private boolean checkPwd(){
        if(mSelectNodeList.size()>0){
            StringBuilder sb = new StringBuilder();
            for (int i = 0;i<mSelectNodeList.size();i++){
                sb.append(""+mSelectNodeList.get(i).getTag());
            }
            Toast.makeText(getContext(),"pwd = "+sb.toString(),Toast.LENGTH_SHORT).show();
            return testPwd.equals(sb.toString());
        }
        return false;
    }

    private boolean pointIsInPath(Path path,float x,float y){
        RectF bounds =new  RectF();
        path.computeBounds(bounds,true);
        Region region = new Region();
        region.setPath(path,new Region((int)bounds.left,(int)bounds.top,(int)bounds.right,(int)bounds.bottom));
        if(region.contains((int)x,(int)y)){
            return true;
        }
        return false;
    }

    private void startNodeAninator(final Node node){
        if(node!=null){
            float src = node.radius;
            float des = src*mRadiusRate;
            ValueAnimator animator = ValueAnimator.ofFloat(src,des);
            animator.setRepeatCount(1);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    node.radius = value;
                    invalidate();
                }
            });
            animator.start();
        }
    }

}

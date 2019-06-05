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

    /**
     * 解锁状态
     */
    enum State{
        /**
         * 普通情况
         */
        STATE_NORMAL,
        /**
         * 连线解锁中
         */
        STATE_SELECTING,
        /**
         * 解锁成功
         */
        STATE_RIGHT,
        /**
         * 解锁失败
         */
        STATE_WRONG;
    }

    /**
     * 九宫格节点类
     */
    class Node{
        float x,y;//圆点坐标
        float radius;//半径
        private boolean isSelected = false;//当前节点是否被选中
        private int tag = -1;//节点值，用来做为密码
        private State mState = State.STATE_NORMAL;//状态，在重绘时用来确定节点的颜色


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


    private boolean isSelecting = false;//是否在连线解锁中
    private boolean isChecking = false;//是否正在检查密码，正在检查密码的时，用户无法重新连线

    //表示不同状态的颜色
    private int mColor_Normal = Color.GRAY;
    private int mColor_Select = Color.YELLOW;
    private int mColor_Right = Color.GREEN;
    private int mColor_Wrong = Color.RED;

    private int mWidth,mHeight;//宽高
    private List<Node> mNodeList;//所有节点集合
    private List<Node> mSelectNodeList;//已选节点的集合

    private Paint mNodePaint,mPathPaint;
    private Canvas mNodeCanvas;
    private Bitmap mNodeBitmap;
    private Path mPath,mTmpPath;//mTmpPath主要负责绘制节点与节点之间的Path，mPath负责绘制完整Path，包含手指移动时的Path
    private float mPathStartX,mPathStartY;
    private float mRadius=18;//节点半径
    private float mRoundPadding = 20;
    private float mRadiusRate = 2f;//比例，用来指定节点有效范围及选中动画最大值


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

    //模拟密码检测结束，恢复状态
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
        float heightSpace = (mHeight-mRoundPadding*2-mRadius*2*3)/2;//垂直方向节点间距
        float widthSpace  = (mWidth-mRoundPadding*2-mRadius*2*3)/2;//水平方向节点间距
        mNodeList.clear();
        mSelectNodeList.clear();
        int count = 1;//节点的tag值
        //创建所有节点对象
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

    /**
     * 返回指定状态对应的颜色
     * @param state
     * @return
     */
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
                if(node!=null){//node不为空，标识手指是在节点范围内
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

    /**
     * 添加选中节点，并调用动画
     * @param node
     */
    private void addSelectNode(Node node) {
        mSelectNodeList.add(node);
        startNodeAninator(node);
    }

    /**
     * 判断当前手指所在位置，同上一个节点之间是否存在另一个节点，如果有
     * @param x
     * @param y
     * @return
     */
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

    /**
     * 恢复所有节点初始状态
     */
    private void initAllNodeState(){
        for (Node node:mNodeList
             ) {
            node.setState(State.STATE_NORMAL);
        }
    }

    /**
     * 循环遍历节点集合，找到节点圆心和当前坐标点的距离小于指定距离（radius*mRadiusRate）的节点
     * @param x
     * @param y
     * @return
     */
    private Node getNode(float x,float y){
        for (int i = 0;i<mNodeList.size();i++){
            Node node = mNodeList.get(i);
           if(Math.abs(x-node.x)<node.radius*mRadiusRate&&Math.abs(y-node.y)<node.radius*mRadiusRate){
               return node;
           }
        }
        return null;
    }

    /**
     * 找出坐标所在范围内，并且不再选中节点集合中的节点
     * @param x
     * @param y
     * @return
     */
    private Node getFreeNode(float x,float y){
        Node node = getNode(x,y);
        if(mSelectNodeList.size()==0||!mSelectNodeList.contains(node)){
            return node;
        }
        return null;
    }

    /**
     * 初始化
     */
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
    //检查密码
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

    /**
     * 节点选中时的放大动画
     * @param node
     */
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

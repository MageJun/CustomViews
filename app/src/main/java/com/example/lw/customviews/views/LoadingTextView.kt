package com.example.lw.customviews.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.example.lw.customviews.R


/**
 *
 */
class LoadingTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Orientation{
        LEFT,TOP,RIGHT,BOTTOM
    }

    val tag = LoadingTextView::class.java.simpleName
    var mNormalColor:Int=0
    var mLoadingColor:Int = 0
    var mBgColor:Int=0
    var mText:String=""
    var mTextWidth =0f
    var mTextHeight = 0f
    var mStartX=0f
    var mStartY=0f
    var mOrientation=Orientation.LEFT.ordinal

    var mWaveLen=0f
    var mAmplitude = 0f


    var mProgress:Float = 0f
    private val mPaint:Paint by lazy {
        val paint = Paint()
        paint.isAntiAlias=true
        paint.textSize= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22f, context.resources.displayMetrics)
        paint.color = mNormalColor
        paint
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingTextView)
        mNormalColor = typedArray.getColor(R.styleable.LoadingTextView_colorNormal,0)
        mLoadingColor = typedArray.getColor(R.styleable.LoadingTextView_colorLoading,0)
        mText = typedArray.getString(R.styleable.LoadingTextView_text)
        mOrientation = typedArray.getInteger(R.styleable.LoadingTextView_orientation,Orientation.LEFT.ordinal)
        typedArray.recycle()
        mTextWidth = getTextWidth(mPaint,mText)
        mTextHeight = getTextHeight(mPaint,mText)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = onMeasureInner(0,widthMeasureSpec)
        var h = onMeasureInner(1,heightMeasureSpec)
        Log.e(tag,"mTextWidth = $mTextWidth,mTextHeight=$mTextHeight,width=$w,height=$h")
        setMeasuredDimension(w,h)
    }

    private fun onMeasureInner(type:Int,measureSpec:Int):Int{
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        var newSize:Int=size

            when(mode){
                MeasureSpec.EXACTLY->{//明确值
                    newSize = size
                }
                MeasureSpec.AT_MOST,MeasureSpec.UNSPECIFIED->{
                    //at_most==wrap_content
                    if(type==0){//宽度
                      newSize = paddingLeft+paddingRight+getTextWidth(mPaint,mText).toInt()
                    }else if(type==1){//高度
                      newSize = paddingTop+paddingBottom+getTextHeight(mPaint,mText).toInt()
                    }
                }

            }

        return newSize
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mStartX = getMidStartX()
        mStartY = getMidStartY(mPaint)
        mAmplitude = 10f
        mWaveLen = getTextWidth(mPaint,mText)/2
        Log.e(tag,"onLayout mStartX = $mStartX,mStartY=$mStartY")
    }

    override fun onDraw(canvas: Canvas?) {
//        canvas!!.drawColor(Color.parseColor("#ff3344"))
        //x y对应的是左下角的基准坐标，并非左上脚坐标

        canvas?.let { canvas->
            onDrawInner(canvas)
        }

    }

    private fun onDrawInner(canvas: Canvas) {
        when(mOrientation){
            Orientation.LEFT.ordinal->{
                //从左向右的展开，根据progress确定分割点x坐标
                val splitX = paddingLeft+mTextWidth*mProgress
                //画正常部分
                canvas.save()
                val path=Path()

                canvas.clipRect(splitX.toInt(),0,width,height)
                mPaint.color = mNormalColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()

                //画进度部分
                canvas.save()
                canvas.clipRect(0,0,splitX.toInt(),height)
                mPaint.color = mLoadingColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()
            }
            Orientation.TOP.ordinal->{
                //从上向下的展开，根据progress确定分割点x坐标
                val splitX = paddingTop+mTextHeight*mProgress
                Log.e(tag,"TOP splitX = $splitX,paddingTop=$paddingTop,paddingLeft=$paddingLeft")
                //画正常部分
                canvas.save()
                canvas.clipRect(0,splitX.toInt(),width,height)
                mPaint.color = mNormalColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()

                //画进度部分
                canvas.save()
                canvas.clipRect(0,0,width,splitX.toInt())
                mPaint.color = mLoadingColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()
            }
            Orientation.RIGHT.ordinal->{

                //从右向左的展开，根据progress确定分割点x坐标
                val splitX = paddingRight+mTextWidth*mProgress
                //画正常部分
                canvas.save()
                canvas.clipRect(0,0,width-splitX.toInt(),height)
                mPaint.color = mNormalColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()

                //画进度部分
                canvas.save()
                canvas.clipRect(width-splitX.toInt(),0,width,height)
                mPaint.color = mLoadingColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()
            }
            Orientation.BOTTOM.ordinal->{
                //从下向上的展开，根据progress确定分割点x坐标
                val splitY = paddingBottom+mTextHeight*mProgress
                Log.e(tag,"bottom splitX = $splitY,paddingTop=$paddingTop,paddingLeft=$paddingLeft")
                //画正常部分
                canvas.save()
                canvas.clipPath(getPathNormal(0f,height-splitY))
//                canvas.clipRect(0,0,width,height-splitY.toInt())
                mPaint.color = mNormalColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()

                //画进度部分
                canvas.save()
                canvas.clipPath(getPathLoading(0f,height-splitY))
//                canvas.clipRect(0,height-splitY.toInt(),width,height)
                mPaint.color = mLoadingColor
                canvas.drawText(mText,mStartX,mStartY,mPaint)
                canvas.restore()

            }
        }
    }

    /**
     * 获取文字的宽度
     */
    private fun getTextWidth(paint:Paint,text:String):Float{
        return paint.measureText(text)
    }

    /**
     * 获取文字的高度
     */
    private fun getTextHeight(paint:Paint,text:String):Float{
        val fm = paint.fontMetrics
        //文字基准线的下部距离-文字基准线的上部距离 = 文字高度
        //可以参考app/text.png图片说明，整个文字区域从上到下分别对应 top、 ascent 、baseLine、descent、bottom
        //baseLine之下为正，之上为负
        //descent和ascent之间是文字的高度，top和bottom之间是整个文本区域的高度
        return fm.bottom - fm.top
    }

    /**
     * 获取文本居中时的startY值
     * 文本绘制时，startX和startY
     * 所以将文本居中绘制，需相当于将descent的位置，绘制在控件垂直一半位置向下移动整个文本区域的一半
     */
    private fun getMidStartY(paint:Paint):Float{
        val fontMetrics = paint.fontMetrics
        //step1，descent位置转移到控件一半的位置
        val step1 = height/2f
        Log.e(tag,"step1 = $step1,height=$height")
        //step2，向下移动整个文本区域的一半
        val step2 = step1+Math.abs(fontMetrics.bottom-fontMetrics.top)/2
        Log.e(tag,"step2 = $step2,bottom=${fontMetrics.bottom},top=${fontMetrics.top}")
        //step3，根据descent获取baseline的值
        Log.e(tag,"step3 = ${step2-fontMetrics.descent},descent = ${fontMetrics.descent}")
        return step2-fontMetrics.descent
    }

    /**
     * 获取文本居中是startX值
     */
    private fun getMidStartX():Float{
        return paddingLeft.toFloat()
    }

    /**
     * progress在0到1f之间
     */
    fun updateProgress(progress:Float){
        Log.e(tag,"updateProgress =$progress")
        mProgress = progress
        invalidate()
    }
    fun getPathNormal(startX:Float,startY:Float):Path{
        val path = Path()
        path.moveTo(-mWaveLen+startX,startY)
        for (x in 0..(1+width/mWaveLen.toInt())){
            //rQudaTo相对上次终点的位置
            path.rQuadTo(mWaveLen/4,-mAmplitude,mWaveLen/2,0f)

            path.rQuadTo(mWaveLen/4,mAmplitude,mWaveLen/2,0f)
        }

        path.lineTo(width.toFloat(),0f)
        path.lineTo(0f,0f)
        path.close()
        return path
    }

    fun getPathLoading(startX:Float,startY:Float):Path{
        val path = Path()
        path.moveTo(-mWaveLen+startX,startY)
        for (x in 0..(1+width/mWaveLen.toInt())){
            //rQudaTo相对上次终点的位置
            path.rQuadTo(mWaveLen/4,-mAmplitude,mWaveLen/2,0f)

            path.rQuadTo(mWaveLen/4,mAmplitude,mWaveLen/2,0f)
        }

        path.lineTo(width.toFloat(),height.toFloat())
        path.lineTo(0f,height.toFloat())
        path.close()
        return path
    }

}
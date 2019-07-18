package com.example.lw.customviews.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.example.lw.customviews.R



class LoadingTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val tag = LoadingTextView::class.java.simpleName
    var mNormalColor:Int=0
    var mLoadingColor:Int = 0
    var mText:String=""
    var mTextWidth =0f
    var mTextHeight = 0f
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
        typedArray.recycle()
        mTextWidth = getTextWidth(mPaint,mText)
        mTextHeight = getTextHeight(mPaint,mText)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.e(tag,"$mTextWidth,$mTextHeight")
        setMeasuredDimension(mTextWidth.toInt(),mTextHeight.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        //x y对应的是左下角的基准坐标，并非左上脚坐标
//          canvas!!.drawText(mText,0f,mTextHeight,mPaint)
        canvas!!.save()
        canvas.clipRect(0,0,(mTextWidth/2).toInt(),mTextHeight.toInt())
        mPaint.color = mNormalColor
        canvas.drawText(mText,0f,mTextHeight,mPaint)
        canvas!!.restore()

        canvas!!.save()
        canvas.clipRect((mTextWidth/2).toInt(),0,mTextWidth.toInt(),mTextHeight.toInt())
        mPaint.color = mLoadingColor
        canvas.drawText(mText,0f,mTextHeight,mPaint)
        canvas!!.restore()
    }

    /**
     * 获取文字的宽度
     */
    fun getTextWidth(paint:Paint,text:String):Float{
        return paint.measureText(text)
    }

    /**
     * 获取文字的高度
     */
    fun getTextHeight(paint:Paint,text:String):Float{
        val fm = paint.fontMetrics
        //文字基准线的下部距离-文字基准线的上部距离 = 文字高度
        return fm.descent - fm.ascent
    }

}
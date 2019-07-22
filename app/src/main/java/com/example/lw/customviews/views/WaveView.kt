package com.example.lw.customviews.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.lw.customviews.R

class WaveView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mWidth = 0
    var mHeight = 0
    var mWaveLen = 0f
    var mAmplitude = 10f//振幅
    var mStartX = 0f//波浪线起点x坐标
    var mStartY=0f//波浪线起点y坐标
    val mWavePath :Path by lazy {
        val path = Path()

        path
    }

    private val mPaint: Paint by lazy {
        var p = Paint()
        p.color = Color.parseColor("#686868")
        p.isAntiAlias=true
        p.strokeWidth= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2f,context.resources.displayMetrics)
        p
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = measuredWidth
        mHeight = measuredHeight
        mWaveLen = (mWidth/2).toFloat()
        mStartY = (mHeight/2).toFloat()

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            mPaint.color = context.resources.getColor(R.color.colorAccent)
            it.save()
            it.clipPath(getPathNormal())
            it.drawCircle(mWidth/2f,mHeight/2f,mWidth/4f,mPaint)
//            it.drawPath(getPathNormal(),mPaint)
            it.restore()
            it.save()
            it.clipPath(getPathLoading())
            mPaint.color = context.resources.getColor(R.color.colorPrimary)
            it.drawCircle(mWidth/2f,mHeight/2f,mWidth/4f,mPaint)
//            it.drawPath(getPathLoading(),mPaint)
            it.restore()
            if(mStartX<mWaveLen)
            mStartX+=mWaveLen/100f
            else
                mStartX= 0f

            postInvalidateDelayed(10)
        }

    }

    fun getPathNormal():Path{
        val path = Path()
        path.moveTo(-mWaveLen+mStartX,mStartY)
        for (x in 0..2){
            //rQudaTo相对上次终点的位置
            path.rQuadTo(mWaveLen/4,-mAmplitude,mWaveLen/2,0f)

            path.rQuadTo(mWaveLen/4,mAmplitude,mWaveLen/2,0f)
        }

        path.lineTo(mWidth.toFloat(),0f)
        path.lineTo(0f,0f)
        path.close()
        return path
    }

    fun getPathLoading():Path{
        val path = Path()
        path.moveTo(-mWaveLen+mStartX,mStartY)
        for (x in 0..2){
            //rQudaTo相对上次终点的位置
            path.rQuadTo(mWaveLen/4,-mAmplitude,mWaveLen/2,0f)

            path.rQuadTo(mWaveLen/4,mAmplitude,mWaveLen/2,0f)
        }

        path.lineTo(mWidth.toFloat(),mHeight.toFloat())
        path.lineTo(0f,mHeight.toFloat())
        path.close()
        return path
    }

}
package com.example.lw.customviews.views

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.lw.customviews.R
import com.example.lw.customviews.R.attr.stateChecked1

//
/**
 * @JvmOverloads 的作用是在有默认参数值的方法中使用
 * 例如：@JvmOverloads fun f(a: String, b: Int=0, c:String="abc"){}
 *      相当于Java中声明了三个方法：
 *      void f(String a)
 *      void f(String a, int b)
 *      void f(String a, int b, String c)
 *
 */
class MulitStateTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    enum class State{
        STATE1,STATE2,STATE3
    }

    private var mState = -1

    companion object {
        private var stateChecked1= intArrayOf(R.attr.stateChecked1)
        private var stateChecked2 = intArrayOf(R.attr.stateChecked2)
        private var stateChecked3 = intArrayOf(R.attr.stateChecked3)
        init {
            Log.e("MulitStateTag","companion init")

        }
    }

   init {
        Log.e("MulitStateTag","init")
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.MulitStateTextView)
        mState = typeArray.getInteger(R.styleable.MulitStateTextView_state,State.STATE1.ordinal)
        typeArray.recycle()
        refreshDrawableState()
   }

    fun setState(state:State){
        Log.e("MulitStateTag","setState")
        mState = state.ordinal
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        Log.e("MulitStateTag","$mState,${drawableState==null},${stateChecked1==null}")
        when(mState){
            State.STATE1.ordinal->{
                 mergeDrawableStates(drawableState,stateChecked1)
            }
            State.STATE2.ordinal->{
                 mergeDrawableStates(drawableState,stateChecked2)

            }
            State.STATE3.ordinal->{
                 mergeDrawableStates(drawableState,stateChecked3)

            }

        }
        return drawableState
    }
}
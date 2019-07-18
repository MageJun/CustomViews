package com.example.lw.customviews

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.lw.customviews.views.MulitStateTextView

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

   var count = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titleView.setOnClickListener(View.OnClickListener {
            var m = count%3
            when(m){
                0 ->{
                    titleView.setState(MulitStateTextView.State.STATE1)
                }
                1->{
                    titleView.setState(MulitStateTextView.State.STATE2)
                }
                2->{
                    titleView.setState(MulitStateTextView.State.STATE3)
                }
            }
            count++
        })
    }
}

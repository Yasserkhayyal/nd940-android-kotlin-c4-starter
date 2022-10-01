package com.udacity.project4.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityBaseBinding

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var activityBaseBinding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBaseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base)
        setSupportActionBar(activityBaseBinding.toolbar)
    }

    protected fun setLayoutContainerContent(@LayoutRes layoutRes: Int): ViewDataBinding {
        return DataBindingUtil.inflate(
            layoutInflater,
            layoutRes,
            activityBaseBinding.layoutContainer,
            true
        )
    }


}
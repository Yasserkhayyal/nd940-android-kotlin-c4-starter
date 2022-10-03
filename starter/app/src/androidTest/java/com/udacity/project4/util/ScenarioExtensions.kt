package com.udacity.project4.util

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity

fun <T : Fragment> FragmentScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onFragment {
        description =
            (it.requireActivity() as BaseActivity).findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}
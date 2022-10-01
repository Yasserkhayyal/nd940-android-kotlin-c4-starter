package com.udacity.project4.locationreminders.reminderslist

import androidx.lifecycle.LifecycleOwner
import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    callBack: (selectedReminder: ReminderDataItem) -> Unit
) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder

    override fun getLifecycleOwner(): LifecycleOwner = viewLifecycleOwner
}
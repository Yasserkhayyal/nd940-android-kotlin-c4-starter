package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : BaseActivity() {

    val viewModel by inject<SaveReminderViewModel>()

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reminderDataItemParcelableExtra =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_ReminderDataItem, ReminderDataItem::class.java)
            } else {
                intent.getParcelableExtra(EXTRA_ReminderDataItem) as? ReminderDataItem
            }

        binding =
            (setLayoutContainerContent(R.layout.activity_reminder_description) as ActivityReminderDescriptionBinding).apply {
                lifecycleOwner = this@ReminderDescriptionActivity
                reminderDataItem = reminderDataItemParcelableExtra
            }
        reminderDataItemParcelableExtra?.let {
            viewModel.deleteReminder(reminderDataItem = it)
        }
    }
}

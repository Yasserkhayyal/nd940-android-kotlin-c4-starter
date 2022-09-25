package com.udacity.project4.authentication

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var signInFlowActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityAuthenticationBinding

    private val activityResultCallback = ActivityResultCallback<ActivityResult?> {
        val response = IdpResponse.fromResultIntent(it.data)
        if (it.resultCode == Activity.RESULT_OK) {
            // User successfully signed in
            handleSuccessfulSignIn()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise
            // response.getError().getErrorCode() is checked and handled.
            handleFailedSignIn(response)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        setSupportActionBar(binding.toolbar)

        signInFlowActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            activityResultCallback
        )

        binding.loginBtn.setOnClickListener {
            onLoginButtonClicked()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        signInFlowActivityResultLauncher.unregister()
    }

    private fun launchSignInFlow() {
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.EmailBuilder().build()
                )
            )
            .setTheme(R.style.AppTheme).build()
        signInFlowActivityResultLauncher.launch(signInIntent)
    }

    private fun handleSuccessfulSignIn() {
        //navigate to ReminderActivity
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    private fun handleFailedSignIn(response: IdpResponse?) {
        if (response == null) return //user clicked back and canceled the sign in flow
        AlertDialog.Builder(this@AuthenticationActivity)
            .setTitle(R.string.error_popup_title).apply {
                response.error?.message?.let {
                    setMessage(it)
                } ?: setMessage(R.string.sign_in_general_error)
            }.setNegativeButton(R.string.ok_btn) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }.setPositiveButton(R.string.retry) { dialog: DialogInterface, _: Int ->
                launchSignInFlow()
                dialog.dismiss()
            }.create().show()
    }

    private fun onLoginButtonClicked() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            //Login is required, start the signIn flow
            launchSignInFlow()
        } else {
            //user is already signed in, route him/her to RemindersActivity
            handleSuccessfulSignIn()
        }
    }
}

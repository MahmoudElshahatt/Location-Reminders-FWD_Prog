package com.udacity.project4.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.Constants

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    //I made a SharedPreferences to save the login state.
    val sharedPref by lazy {
        getSharedPreferences(getString(R.string.login_preferences), Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        val loginButton = findViewById<Button>(R.id.login_button)

        if (sharedPref.getBoolean(getString(R.string.login_state), false)) {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            with(sharedPref.edit()) {
                putBoolean(getString(R.string.login_state), false)
                apply()
            }
            Login()
        }

    }

    private fun Login() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(),
            Constants.SIGN_IN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.SIGN_IN_REQUEST_CODE) {

            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(
                    "AuthenticationActivity",
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                LoginIsDone()
            } else {

                Log.i(
                    "AuthenticationActivity",
                    "Sign in unsuccessful ${response?.error?.errorCode}"
                )
            }
        }
    }

    private fun LoginIsDone() {
        with(sharedPref.edit()) {
            putBoolean(getString(R.string.login_state), true)
            apply()
        }
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

}
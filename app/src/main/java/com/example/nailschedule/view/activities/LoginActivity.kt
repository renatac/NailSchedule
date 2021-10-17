package com.example.nailschedule.view.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics

class LoginActivity : AppCompatActivity() {

    companion object {
        const val LOGIN_TYPE_IDENTIFIER = "login_type_identifier"
        const val GOOGLE_SIGN_IN_VALUE = 1
        const val EXTRA_USER_DATA = "extra_user_data"
        const val EXTRA_DISPLAY_NAME = "extra_display_name"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
    }

    private lateinit var analytics: FirebaseAnalytics
    private lateinit var binding: ActivityMainBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerForActivityResult()

        binding.btnGoogleSignIn.setOnClickListener {
            googleSignIn()
        }

        analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("initialize_firebase_analytics", null)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun registerForActivityResult() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                            // The Task returned from this call is always completed, no need to attach
                            // a listener.
                            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            handleSignInResult(task)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        print(account)
    }

    private fun googleSignIn() {
        val intent : Intent = mGoogleSignInClient.signInIntent.apply {
            putExtra(LOGIN_TYPE_IDENTIFIER, GOOGLE_SIGN_IN_VALUE)
        }
        startForResult.launch(intent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            println(account)
            val bundle = Bundle().apply {
                putString(EXTRA_DISPLAY_NAME, account.displayName)
                putString(EXTRA_PHOTO_URL, account.photoUrl?.toString())
            }
            val intent =  Intent(
                this,
                BottomNavigationActivity::class.java).apply {
                    putExtra(EXTRA_USER_DATA, bundle)
                }
            startActivity(intent)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Test", "signInResult:failed code=" + e.statusCode)
        }
    }
}

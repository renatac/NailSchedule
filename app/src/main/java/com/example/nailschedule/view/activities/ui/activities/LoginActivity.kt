package com.example.nailschedule.view.activities.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.databinding.ActivityLoginBinding
import com.example.nailschedule.view.activities.utils.SharedPreferenceHelper
import com.example.nailschedule.view.activities.utils.isLoggedInFacebook
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import org.json.JSONException


class LoginActivity : AppCompatActivity() {

    companion object {
        const val LOGIN_TYPE_IDENTIFIER = "login_type_identifier"
        const val GOOGLE_SIGN_IN_VALUE = 1

        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: GoogleSignInClient? = null

        fun googleSignInClientGetInstance(activity: Activity): GoogleSignInClient =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildGoogleSignInClient(activity).also { INSTANCE = it }
            }

        private fun buildGoogleSignInClient(activity: Activity): GoogleSignInClient {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            // Build a GoogleSignInClient with the options specified by gso.
            return GoogleSignIn.getClient(activity, gso)
        }
    }

    private lateinit var analytics: FirebaseAnalytics
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSharedPreferences()

        if(GoogleSignIn.getLastSignedInAccount(this) != null || isLoggedInFacebook()) {
            redirectToBottomNavigation()
        }
        else {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = googleSignInClientGetInstance(this)

            //If loggin through Google the return is in registerForActivityResult
            registerForActivityResult()

            registerGoogleSignInClickListener()
            registerFacebookSignInCallback()

            setFirebaseAnalytics()
        }
    }

    private fun registerGoogleSignInClickListener() {
        binding.btnGoogleSignIn.setOnClickListener {
            googleSignIn()
        }
    }

    private fun registerFacebookSignInCallback() {
        // CallbackManager to handle like facebook login calls
        callbackManager = CallbackManager.Factory.create()

        binding.btnFacebookSignIn.setReadPermissions("email")

        // Callback registration
        binding.btnFacebookSignIn.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                loadUserProfile(loginResult?.accessToken)
                Toast.makeText(applicationContext,"SUCESSO AO LOGAR PELO FACE!",
                    Toast.LENGTH_LONG).show()
            }

            override fun onCancel() {
                println("onCancel")
            }

            override fun onError(exception: FacebookException) {
                println("exception = $exception")
            }
        })
    }

    private fun setFirebaseAnalytics() {
        analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("initialize_firebase_analytics", null)
    }

    private fun initSharedPreferences() {
        SharedPreferenceHelper.init(applicationContext)

        /*
        sharedPref = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        */
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
            SharedPreferenceHelper.write(SharedPreferenceHelper.EXTRA_DISPLAY_NAME, account.displayName)
            SharedPreferenceHelper.write(SharedPreferenceHelper.EXTRA_PHOTO_URL, account.photoUrl?.toString())
            redirectToBottomNavigation()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Test", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun redirectToBottomNavigation() {
        val intent =  Intent(
            this,
            BottomNavigationActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadUserProfile(newAccessToken: AccessToken?) {
        val request = GraphRequest.newMeRequest(
            newAccessToken
        ) { `object`, _ ->
            try {
                //edit.putBoolean(IS_SIGNED, true)
                val name = `object`.getString("first_name") +
                        `object`.getString("last_name")
                val imageUrl = "https://graph.facebook.com/" +
                        `object`.getString("id").toString() + "/picture?type=normal"
                SharedPreferenceHelper.write(SharedPreferenceHelper.EXTRA_DISPLAY_NAME, name)
                SharedPreferenceHelper.write(SharedPreferenceHelper.EXTRA_PHOTO_URL, imageUrl)
                redirectToBottomNavigation()

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        if(newAccessToken != null) {
            val parameters = Bundle()
            parameters.putString("fields", "first_name, last_name, email, id")
            request.parameters = parameters
            request.executeAsync()
        }
    }
}

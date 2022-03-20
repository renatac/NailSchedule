package com.example.nailschedule.view.activities.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityLoginBinding
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.isLoggedInFacebook
import com.example.nailschedule.view.activities.view.owner.OwnerActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.io.IOException


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

        var credential: AuthCredential? = null
    }

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

            //If login through Google the return is in registerForActivityResult
            registerForActivityResult()
            registerGoogleSignInClickListener()
            registerFacebookSignInCallback()
            setListeners()
        }
    }

    private fun setListeners() = binding.apply {
        btnClient.setOnClickListener {
            setClientOrOwnerGroupVisibility(View.GONE)
            setBtnsLoginGroupVisibility(View.VISIBLE)
        }
        btnOwner.setOnClickListener {
            redirectOwnerFlow()
        }
        tvClientOrOwnerAgain.setOnClickListener {
            setBtnsLoginGroupVisibility(View.GONE)
            setClientOrOwnerGroupVisibility(View.VISIBLE)
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

        binding.btnFacebookSignIn.setReadPermissions("email", "public_profile")

        // Callback registration
        binding.btnFacebookSignIn.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                SharedPreferencesHelper.write(
                    SharedPreferencesHelper.FACEBOOK_ACCESS_TOKEN,
                    loginResult?.accessToken.toString()
                )
                loadUserProfile(loginResult?.accessToken)
            }

            override fun onCancel() {
                println("onCancel")
            }

            override fun onError(exception: FacebookException) {
                println("exception = $exception")
            }
        })
    }

    private fun initSharedPreferences() {
        SharedPreferencesHelper.init(applicationContext)
    }

    private fun registerForActivityResult() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(result.data)

                    SharedPreferencesHelper.write(
                        SharedPreferencesHelper.GOOGLE_TOKEN_ID,
                        task.result.idToken
                    )

                    val googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)
                    if (googleSignInResult.isSuccess) {
                        val account: GoogleSignInAccount? = googleSignInResult.signInAccount
                        saveHeaderNavDatasInSharedPreferences(
                            account?.displayName.toString(),
                            account?.photoUrl.toString(),
                            account?.email.toString())
                        val runnable = Runnable {
                            try {
                                val scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE
                                val accessToken = GoogleAuthUtil.getToken(
                                    applicationContext,
                                    account?.account,
                                    scope,
                                    Bundle()
                                )
                                SharedPreferencesHelper.write(
                                    SharedPreferencesHelper.GOOGLE_ACCESS_TOKEN,
                                    accessToken
                                )

                                val accountGoogle = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
                                accountGoogle?.let { conta ->
                                    credential = GoogleAuthProvider.getCredential(conta.idToken, accessToken)
                                }

                                //Login With Google
                                FirebaseAuth.getInstance()
                                    .signInWithCredential(credential!!)
                                    .addOnSuccessListener {
                                        handleSignInResult(task)
                                    }.addOnFailureListener {
                                        println(it)
                                    }

                            } catch (e: IOException) {
                                e.printStackTrace();
                            } catch (e: GoogleAuthException) {
                                e.printStackTrace()
                            }
                        }
                        AsyncTask.execute(runnable)
                    }
                }
            }
    }

    private fun saveHeaderNavDatasInSharedPreferences(name: String, photoUrl: String, email: String) {
        SharedPreferencesHelper.write(SharedPreferencesHelper.EXTRA_DISPLAY_NAME, name)
        SharedPreferencesHelper.write(SharedPreferencesHelper.EXTRA_PHOTO_URL, photoUrl)
        SharedPreferencesHelper.write(SharedPreferencesHelper.EXTRA_EMAIL, email)
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
          redirectToBottomNavigation()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Test", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun redirectOwnerFlow() {
        val intent =  Intent(
            this,
            OwnerActivity::class.java)
        startActivity(intent)
    }

    private fun redirectToBottomNavigation() {
        val intent =  Intent(
            this,
            BottomNavigationActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadUserProfile(newAccessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(newAccessToken?.token ?: "" )
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = FirebaseAuth.getInstance().currentUser
                    saveHeaderNavDatasInSharedPreferences(
                        user?.displayName.toString(),
                        user?.photoUrl.toString(),
                        user?.email.toString())
                    redirectToBottomNavigation()
                }
            }.addOnFailureListener {
                // If sign in fails, display a message to the user.
                Toast.makeText(baseContext, getString(R.string.login_failed),
                    Toast.LENGTH_SHORT).show()
            }
    }


    private fun setBtnsLoginGroupVisibility(typeVisibility: Int) {
        binding.btnsLoginGroup.visibility = typeVisibility
    }

    private fun setClientOrOwnerGroupVisibility(typeVisibility: Int) {
        binding.clientOrOwnerGroup.visibility= typeVisibility
    }
}
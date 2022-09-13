package com.ssossotable.food

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.ssossotable.food.databinding.ActivitySignupBinding
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

class SignupActivity : AppCompatActivity() {

    private var mBinding: ActivitySignupBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    private lateinit var oneTapClient: SignInClient
    //private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest

    //private val REQ_ONE_TAP_SIGNIN = 2
    private val REQ_ONE_TAP_SIGNUP = 3  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    private val TAG="MAIN TAG"

    private lateinit var mSocket: Socket


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        //구글
        try {
            mSocket = IO.socket("http://172.20.10.5:3000")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        //mSocket.on("signin", signinEvent)
        mSocket.on("signup", signupEvent)
        mSocket.connect()


        oneTapClient = Identity.getSignInClient(this)
        /*signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()*/

        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    //.setServerClientId(getString(R.string.default_web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        /*binding.google.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP_SIGNIN,
                            null, 0, 0, 0, null)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage)
                }
        }*/

        binding.google.setOnClickListener {
            oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP_SIGNUP,
                            null, 0, 0, 0)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            /* REQ_ONE_TAP_SIGNIN -> {
                 try {
                     val credential = oneTapClient.getSignInCredentialFromIntent(data)
                     val idToken = credential.googleIdToken
                     //val username = credential.id
                     //val password = credential.password
                     when {
                         idToken != null -> {
                             // Got an ID token from Google. Use it to authenticate
                             // with your backend.
                             mSocket.emit("signup", idToken)
                             Log.d("idtoken", idToken)
                             Log.d(TAG, "Got ID token.")
                         }
                         else -> {
                             // Shouldn't happen.
                             Log.d(TAG, "No ID token or password!")
                         }
                     }
                 } catch (e: ApiException) {
                     when (e.statusCode) {
                         CommonStatusCodes.CANCELED -> {
                             Log.d(TAG, "One-tap dialog was closed.")
                             // Don't re-prompt the user.
                             showOneTapUI = false
                         }
                         CommonStatusCodes.NETWORK_ERROR -> {
                             Log.d(TAG, "One-tap encountered a network error.")
                             // Try again or just ignore.
                         }
                         else -> {
                             Log.d(TAG, "Couldn't get credential from result." +
                                     " (${e.localizedMessage})")
                         }
                     }
                 }
             }*/


            REQ_ONE_TAP_SIGNUP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            mSocket.emit("signup", idToken)
                            Log.d(TAG, idToken)
                            Log.d(TAG, "Got ID token.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(
                                TAG, "Couldn't get credential from result." +
                                        " (${e.localizedMessage})"
                            )
                        }
                    }
                }
            }
        }
    }

    /*private val signinEvent =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val data: JSONObject = args[0] as JSONObject
                if(data.getBoolean("success")) {
                    startActivity(Intent(applicationContext,MainActivity::class.java))
                }
                else {
                    Toast.makeText(applicationContext,"not registered user", Toast.LENGTH_SHORT).show()
                }
                Log.d("AAA","AAAAA")
            })
        }*/

    private val signupEvent =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val data: JSONObject = args[0] as JSONObject
                if(data.getBoolean("success")) {
                    startActivity(Intent(applicationContext,SigninActivity::class.java))
                }
                else {
                    Toast.makeText(applicationContext,"registered info", Toast.LENGTH_SHORT).show()
                }
            })
        }
}
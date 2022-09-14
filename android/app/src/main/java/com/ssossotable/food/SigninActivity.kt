package com.ssossotable.food

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.ssossotable.food.databinding.ActivitySigninBinding
import io.socket.emitter.Emitter
import org.json.JSONObject

class SigninActivity : AppCompatActivity() {
    /**디바이스 고유값**/
    private var setCookieJSON=JSONObject()
    private var userInfoJSON=JSONObject()
    private lateinit var deviceID:String
    /**초기 로그인 판별 상태코드**/
    private val INIT_SIGNIN:Int=231
    private val SIGNIN:Int=232

    private var mBinding: ActivitySigninBinding? = null
    private val binding get() = mBinding!!

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    //private lateinit var signUpRequest: BeginSignInRequest

    private val REQ_ONE_TAP_SIGNIN = 2
    //private val REQ_ONE_TAP_SIGNUP = 3  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    private val TAG="MAIN TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        deviceID= Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        /**소켓 연결**/
        AppHelper.socket.on("check init",checkInit)
        AppHelper.socket.connect()
        /**
         * 로그인 방법(일반 로그인, 소셜 로그인) 에 따른 다른 값(ID, 토큰)을 서버로 전송한다
         * **/

        // 일반 로그인
        binding.signin.setOnClickListener {
            userInfoJSON.put("userID",binding.id.text)
            userInfoJSON.put("userPassword",binding.password.text)
            AppHelper.socket.emit("id password signin")
        }

        // 회원가입
        binding.signup.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
        }


        // 구글로그인
        AppHelper.socket.on("signin", signinEvent)
        //mSocket.on("signup", signupEvent)
        AppHelper.socket.connect()


        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
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
            .build()

        binding.google.setOnClickListener {
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
        }
    }

    /**이벤트 처리**/
    private val checkInit =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val data: JSONObject = args[0] as JSONObject
                when(data.getInt("state")) {
                    // 초기로그인인 경우 사용자 정보 초기화(UserInit) 액티비티로 이동
                    INIT_SIGNIN->{
                        setCookieJSON.put("deviceID",deviceID)
                        AppHelper.socket.emit("check cookie",setCookieJSON)
                        startActivity(Intent(this,UserInitImageActivity::class.java))
                    }
                    // 초기 로그인이 아닌 경우 Main 액티비티로 이동
                    SIGNIN -> {
                        setCookieJSON.put("deviceID",deviceID)
                        AppHelper.socket.emit("check cookie",setCookieJSON)
                        startActivity(Intent(this,MainActivity::class.java))
                    }
                    // 에러 상황
                    else->{
                        finish();
                        startActivity(intent);
                    }
                }
            })
        }
    override fun onDestroy() {
        super.onDestroy()
        AppHelper.socket.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP_SIGNIN -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    //val username = credential.id
                    //val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            AppHelper.socket.emit("signup", idToken)
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
            }


            /*REQ_ONE_TAP_SIGNUP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            AppHelper.socket.emit("signup", idToken)
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
            }*/
        }
    }

    private val signinEvent =
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
        }

    /*private val signupEvent =
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
        }*/
}
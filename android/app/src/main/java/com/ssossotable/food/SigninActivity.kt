package com.ssossotable.food

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
                        startActivity(Intent(this,UserInitActivity::class.java))
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
}
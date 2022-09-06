package com.ssossotable.food

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.socket.emitter.Emitter
import org.json.JSONObject


class Splash : AppCompatActivity() {
    /**디바이스 고유값**/
    private var checkCookieJSON=JSONObject()
    private lateinit var deviceID:String

    /**상태 코드**/
    private val COOKIE:Int=231 // 쿠키 존재
    private val NO_COOKIE:Int=232 // 쿠키 미존재

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        deviceID= Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        /**소켓 연결**/
        AppHelper.socket.on("check cookie",checkCookie)
        AppHelper.socket.connect()

        /**
         * 자동 로그인을 위한 값 저장
         * 장치 고유값을 통해 판별
         * DB에 장치 고유값에 대한 쿠키값이 만료되었는가를 판별하기 위한 이벤트 전송
         * **/
        checkCookieJSON.put("deviceID",deviceID)
        AppHelper.socket.emit("check cookie",checkCookieJSON)
    }
    /**소켓 이벤트**/
    private val checkCookie =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val data: JSONObject = args[0] as JSONObject
                /**상태 값에 따른 코드**/
                when(data.getInt("state")) {
                    // 쿠키 존재 시 Main화면으로 이동
                    COOKIE->{
                        startActivity(Intent(this,MainActivity::class.java))
                    }
                    // 쿠키 미 존재 시 재로그인을 위해 Signin화면으로 이동
                    NO_COOKIE->{
                        startActivity(Intent(this,SigninActivity::class.java))
                    }
                    // 에러 시 액티비티 새로고침
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
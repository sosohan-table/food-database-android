package com.ssossotable.food

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ssossotable.food.databinding.ActivityUserinitNicknameBinding
import io.socket.emitter.Emitter
import org.json.JSONObject

class UserInitNicknameActivity : AppCompatActivity() {
    private var mBinding: ActivityUserinitNicknameBinding? = null
    private val binding get() = mBinding!!
    private var user:JSONObject=JSONObject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUserinitNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppHelper.socket.on("init user nickname",initUserNickname)
        AppHelper.socket.connect()

        binding.confirm.setOnClickListener {
            user.put("userId",User.userId)
            user.put("nickname",binding.nickname.text)
            AppHelper.socket.emit("init user nickname",user)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppHelper.socket.disconnect()
    }
    private val initUserNickname =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val data: JSONObject = args[0] as JSONObject
                if(!data.getBoolean("duplicated"))
                    startActivity(Intent(applicationContext,MainActivity::class.java))
                else
                    Toast.makeText(applicationContext,"duplicated nickname",Toast.LENGTH_SHORT).show()
            })
        }
}
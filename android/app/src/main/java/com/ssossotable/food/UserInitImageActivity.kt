package com.ssossotable.food

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

import com.ssossotable.food.databinding.ActivityUserinitImageBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class UserInitImageActivity : AppCompatActivity() {
    private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var imageURI: Uri? = null
    private var mBinding: ActivityUserinitImageBinding? = null
    private val binding get() = mBinding!!

    private var userInfo:JSONObject = JSONObject();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUserinitImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppHelper.socket.connect()

        binding.confirm.setOnClickListener {
            startActivity(Intent(applicationContext,UserInitNicknameActivity::class.java))
        }

        binding.profileImage.setOnClickListener {
            if (verifyPermissions() == true) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                launcher.launch(intent)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        AppHelper.socket.disconnect()
    }

    private var launcher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val uri = intent!!.data
            imageURI = intent.data
            // imageview.setImageURI(uri);
            Glide.with(this)
                .load(imageURI)
                .into(binding.profileImage)
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .override(120, 120)
                .into(object : SimpleTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        var resource = resource
                        resource = Bitmap.createScaledBitmap(resource, 120, 120, true)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        val byteArray = byteArrayOutputStream.toByteArray()
                        try {
                            // userInfo.put("userid", UserInfo.userid);
                            userInfo.put("userId", User.userId)
                            userInfo.put("userImage", byteArray)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        AppHelper.socket.emit("init user image", userInfo)
                    }
                })
        }
    }

    private fun verifyPermissions(): Boolean? {
        // This will return the current Status
        val permissionExternalMemory =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted then ask for permission real time.
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, 1)
            return false
        }
        return true
    }
}
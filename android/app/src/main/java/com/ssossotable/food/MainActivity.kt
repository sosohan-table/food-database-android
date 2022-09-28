package com.ssossotable.food

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssossotable.food.databinding.ActivityMainBinding
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.text.FieldPosition

class MainActivity : AppCompatActivity() {

    private var mBinding:ActivityMainBinding?=null
    private val binding get() = mBinding!!

    // RecyclerView 가 불러올 목록
    private val foodData:MutableList<FoodRatingContent> = mutableListOf()
    private var adapter: FoodRatingAdapter? = null
    private var userInfoJSON=JSONObject()
    private var foodLength:Int=-1
    private var ratingLength:Int=-1
    private var foodDataJSONArray=JSONArray()
    private var ratingDataJSONArray=JSONArray()
    private var changedJSONArray=JSONArray()
    private var emitJSON=JSONObject()

    private var ratingInfoMap=HashMap<Int,RatingInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        mBinding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInfoJSON.put("userId",User.id)

        AppHelper.socket.on("rating list", ratingList)
        AppHelper.socket.connect()
        AppHelper.socket.emit("rating list",userInfoJSON)

        binding.add.setOnClickListener {
        }
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    true
                }
                R.id.rate -> {
                    true
                }
                R.id.info -> {
                    true
                }
                else -> false
            }
        }
        //initialize() // data 값 초기화
        refreshRecyclerView() // recyclerView 데이터 바인딩
        AppHelper.socket.emit("changed data","AAAAA")
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        AppHelper.socket.disconnect()
    }

    companion object{
        private var instance:MainActivity? = MainActivity()
        fun getInstance(): MainActivity? {
            return instance
        }
    }
    public fun editContent(foodRatingContent: FoodRatingContent?, position: Int?) {
//        foodData[position!!].foodRating= foodRatingContent?.foodRating!!
//        adapter?.notifyDataSetChanged()
        var changedJSONObject:JSONObject=JSONObject()
        changedJSONObject.put("foodName",foodRatingContent?.foodName!!)
        changedJSONObject.put("foodId",foodRatingContent?.foodId!!)
        changedJSONObject.put("foodRating",foodRatingContent?.foodRating!!)
        changedJSONObject.put("foodImage",foodRatingContent?.foodImage!!)
        changedJSONObject.put("userId",User.id)
        AppHelper.socket.emit("changed data",changedJSONObject)
    }
    private fun refreshRecyclerView(){
        adapter = FoodRatingAdapter()
        adapter!!.listData = foodData
        binding.foodRating.adapter = adapter
        binding.foodRating.layoutManager = LinearLayoutManager(this)
    }
    private val ratingList =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val totalData= args[0] as JSONObject
                foodDataJSONArray = totalData.getJSONArray("foodData") as JSONArray
                ratingDataJSONArray = totalData.getJSONArray("ratingData") as JSONArray
                foodLength=totalData.getInt("foodLength") as Int
                ratingLength=totalData.getInt("ratingLength") as Int

                with(ratingInfoMap) {
                    for(i in 0 until foodLength) {
                        var jsonObject=foodDataJSONArray[i] as JSONObject
                        put(jsonObject.getInt("id"), RatingInfo(
                            jsonObject.getString("name"),
                            jsonObject.getString("image").toString(),
                            0
                        ))
                    }
                }
                with(ratingInfoMap) {
                    for(i in 0 until ratingLength) {
                        var jsonObject=ratingDataJSONArray[i] as JSONObject
                        if(ratingInfoMap[jsonObject.getInt("id")]==null) {
                            put(jsonObject.getInt("id"), RatingInfo(
                                jsonObject.getString("name"),
                                jsonObject.getString("image").toString(),
                                0
                            ))
                        }
                        else {
                            put(jsonObject.getInt("id"),RatingInfo(
                                jsonObject.getString("name"),
                                jsonObject.getString("image").toString(),
                                jsonObject.getInt("rating")
                            ))
                        }
                    }
                }
                with(foodData) {
                    ratingInfoMap.forEach { (k, e) ->
                        add(
                            FoodRatingContent(
                                e.foodName,
                                k,
                                e.foodImage,
                                e.foodRating,
                                false
                            )
                        )
                    }
                }
                refreshRecyclerView()
            })
        }
}
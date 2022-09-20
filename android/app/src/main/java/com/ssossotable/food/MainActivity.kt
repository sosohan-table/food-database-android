package com.ssossotable.food

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssossotable.food.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var mBinding:ActivityMainBinding?=null
    private val binding get() = mBinding!!

    // RecyclerView 가 불러올 목록
    private val data:MutableList<FoodRatingContent> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        mBinding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        initialize() // data 값 초기화
        refreshRecyclerView() // recyclerView 데이터 바인딩
    }
    companion object{
        private var instance:MainActivity? = null
        fun getInstance(): MainActivity? {
            return instance
        }
    }
    private fun initialize(){
        with(data){
            add(FoodRatingContent("Food1",-1,"",-1))
            add(FoodRatingContent("Food2",-1,"",-1))
            add(FoodRatingContent("Food3",-1,"",-1))
        }
    }
    private fun refreshRecyclerView(){
        val adapter = FoodRatingAdapter()
        adapter.listData = data
        binding.foodRating.adapter = adapter
        binding.foodRating.layoutManager = LinearLayoutManager(this)
    }
}
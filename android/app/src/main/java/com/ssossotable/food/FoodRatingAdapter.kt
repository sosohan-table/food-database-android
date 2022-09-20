package com.ssossotable.food

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssossotable.food.databinding.RatingListItemBinding

class FoodRatingAdapter : RecyclerView.Adapter<Holder>()
{
    var listData = mutableListOf<FoodRatingContent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RatingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val member = listData[position]
        holder.setData(member,position)
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}
class Holder(private val binding: RatingListItemBinding) : RecyclerView.ViewHolder(binding.root){
    private val mainActivity = MainActivity.getInstance()
    var mFoodRatingContent: FoodRatingContent? = null
    var mPosition: Int? = null

    fun clear() {
        binding.rateOne.setImageResource(R.drawable.rating_before)
        binding.rateTwo.setImageResource(R.drawable.rating_before)
        binding.rateThree.setImageResource(R.drawable.rating_before)
        binding.rateFour.setImageResource(R.drawable.rating_before)
        binding.rateFive.setImageResource(R.drawable.rating_before)
    }

    init {
        binding.rateOne.setOnClickListener{
            clear()
            binding.rateOne.setImageResource(R.drawable.rating_after)
        }
        binding.rateTwo.setOnClickListener{
            clear()
            binding.rateOne.setImageResource(R.drawable.rating_after)
            binding.rateTwo.setImageResource(R.drawable.rating_after)
        }
        binding.rateThree.setOnClickListener{
            clear()
            binding.rateOne.setImageResource(R.drawable.rating_after)
            binding.rateTwo.setImageResource(R.drawable.rating_after)
            binding.rateThree.setImageResource(R.drawable.rating_after)
        }
        binding.rateFour.setOnClickListener{
            clear()
            binding.rateOne.setImageResource(R.drawable.rating_after)
            binding.rateTwo.setImageResource(R.drawable.rating_after)
            binding.rateThree.setImageResource(R.drawable.rating_after)
            binding.rateFour.setImageResource(R.drawable.rating_after)
        }
        binding.rateFive.setOnClickListener{
            clear()
            binding.rateOne.setImageResource(R.drawable.rating_after)
            binding.rateTwo.setImageResource(R.drawable.rating_after)
            binding.rateThree.setImageResource(R.drawable.rating_after)
            binding.rateFour.setImageResource(R.drawable.rating_after)
            binding.rateFive.setImageResource(R.drawable.rating_after)
        }
    }

    fun setData(data: FoodRatingContent, position: Int){
        binding.ratingContentTitle.text=data.foodName
        this.mFoodRatingContent = data
        this.mPosition = position
    }

}
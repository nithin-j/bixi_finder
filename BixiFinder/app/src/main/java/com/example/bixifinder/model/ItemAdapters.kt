package com.example.bixifinder.model

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.example.bixifinder.R

class ItemAdapters(var context: Context, var arrayList: ArrayList<ItemList>): BaseAdapter() {

    override fun getItem(position: Int): Any {
        return arrayList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return arrayList.size
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.ratings_items, null)

        var txtReviewTitle: TextView = view.findViewById(R.id.txt_review_title)
        var rbUserRatings: RatingBar = view.findViewById(R.id.rb_user_ratings)
        var txtUserReview: TextView = view.findViewById(R.id.txt_user_review)
        var txtReviewDate: TextView = view.findViewById(R.id.txt_review_date)
        var txtReviewerName: TextView = view.findViewById(R.id.txt_reviewer_name)

        var items: ItemList = arrayList[position]

        txtReviewTitle.text = items.title
        rbUserRatings.rating = items.rating?.toFloat()!!
        txtUserReview.text = items.review
        txtReviewDate.text = items.reviewDate
        txtReviewerName.text = items.name


        return view!!

    }
}
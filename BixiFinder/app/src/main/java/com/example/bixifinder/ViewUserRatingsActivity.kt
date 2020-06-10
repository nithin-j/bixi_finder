package com.example.bixifinder

import android.annotation.SuppressLint
import android.app.LauncherActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import com.example.bixifinder.dbContext.UserRatings
import com.example.bixifinder.model.ItemAdapters
import com.example.bixifinder.model.ItemList
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_view_user_ratings.*
import kotlinx.android.synthetic.main.ratings_dialog.*
import java.text.DecimalFormat

class ViewUserRatingsActivity : AppCompatActivity() {

    private var listView: ListView ? =null
    private var itemAdapters: ItemAdapters ? =null
    private var arrayList: ArrayList<ItemList> ? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_user_ratings)

        initialize();
    }

    private fun initialize() {
        setSupportActionBar(rating_toolbar)

        supportActionBar?.title = "Ratings & Reviews"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        listView = findViewById(R.id.lv_ratings)
        arrayList = ArrayList()

        //set data to object


        var listItem: ArrayList<ItemList> = ArrayList()
        var userRatings: ArrayList<UserRatings?> = ArrayList()


        val ratingReference = FirebaseDatabase.getInstance().getReference("UserRatings")
        val ratingListener = object :ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            @SuppressLint("SetTextI18n")
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                listItem.clear()
                val ratings = p0.getValue(UserRatings::class.java)
                userRatings?.add(ratings)

                var totalRating: Double = 0.0
                var oneRating: Int = 0
                var twoRating: Int = 0
                var threeRating: Int = 0
                var fourRating: Int = 0
                var fiveRating: Int = 0

                for ( rating in userRatings){
                    listItem.add(
                        ItemList(
                            rating?.name,
                            rating?.rating,
                            rating?.title,
                            rating?.review,
                            rating?.reviewDate
                        )
                    )

                    when (rating?.rating) {
                        1 -> oneRating += 1
                        2 -> twoRating += 1
                        3 -> threeRating += 1
                        4 -> fourRating += 1
                        5 -> fiveRating += 1
                    }


                    totalRating += rating?.rating?.toDouble()!!


                }

                if (userRatings.size != 0){


                    var oneRatingPercent = (oneRating / userRatings.size) * 100
                    pb_oneRating.progress = oneRatingPercent

                    var twoRatingPercent = (twoRating/userRatings.size) * 100
                    pb_TwoRating.progress = twoRatingPercent

                    var threeRatingPercent = (threeRating/userRatings.size) * 100
                    pb_ThreeRating.progress = threeRatingPercent

                    var fourRatingPercent = (fourRating/userRatings.size) * 100
                    pb_FourRating.progress = fourRatingPercent

                    var fiveRatingPercent = (fiveRating/userRatings.size) * 100
                    pb_FiveRating.progress = fiveRatingPercent

                }

                val format = DecimalFormat("0.0")
                val overallRating = ( ( totalRating / ( 5 * userRatings.size ) ) * 5 )
                lbl_overall_ratings.text = format.format(overallRating)
                lbl_num_ratings.text = "${userRatings.size} Ratings"




                arrayList = listItem
                itemAdapters = ItemAdapters(applicationContext, listItem)
                listView?.adapter = itemAdapters

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }

        }

        ratingReference.addChildEventListener(ratingListener)

    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

        return super.onOptionsItemSelected(item)
    }
}

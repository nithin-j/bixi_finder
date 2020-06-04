package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_bixi_details.*
import kotlinx.android.synthetic.main.activity_purchase_pass.*
import kotlinx.android.synthetic.main.activity_purchase_pass.main_toolbar

class BixiDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bixi_details)

        initialise()

    }

    private fun initialise() {


        val station_name = intent.getStringExtra("station_name")
        val bike_count = intent.getIntExtra("num_bikes",0)
        val ebike_count = intent.getIntExtra("num_ebikes",0)
        val dock_count = intent.getIntExtra("num_docks",0)

        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "BIXI Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        lbl_station_name.text = station_name
        lbl_docks.text = dock_count.toString()
        lbl_bikes.text = bike_count.toString()
        lbl_ebikes.text = ebike_count.toString()


        button_confirm_ride.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null){
                val accessToken = "${(1..3).random()}${(1..3).random()}${(1..3).random()}${(1..3).random()}${(1..3).random()}${(1..3).random()}"

                Toast.makeText(this,accessToken,Toast.LENGTH_SHORT).show()
            }

            else
                Snackbar.make(it, "Please login first", Snackbar.LENGTH_SHORT)
                    .show();


        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

        return super.onOptionsItemSelected(item)
    }
}

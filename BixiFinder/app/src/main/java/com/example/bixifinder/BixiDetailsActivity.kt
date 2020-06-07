package com.example.bixifinder

import android.accounts.Account
import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.bixifinder.dbContext.AccountDetails
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_bixi_details.*
import kotlinx.android.synthetic.main.activity_purchase_pass.*
import kotlinx.android.synthetic.main.activity_purchase_pass.main_toolbar
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class BixiDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bixi_details)


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){


            val userReference = FirebaseDatabase.getInstance().getReference(user?.uid.toString()).child("AccountDetails")
            val userListener = object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val userDetails = p0.getValue(AccountDetails::class.java)
                    if (userDetails?.membershipStatus?.toLowerCase() == "invalid"){
                        val snackbar = Snackbar.make(
                            findViewById(android.R.id.content),
                            "Please purchase an access pass to enjoy     your ride",
                            Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction("Get it NOW"){

                            val intent = Intent(this@BixiDetailsActivity,PurchasePassActivity::class.java)
                            intent.putExtra("layout","details")
                            startActivity(intent)
                            finish()
                            snackbar.dismiss()
                        }
                        snackbar.show()

                        button_confirm_ride.isEnabled = false
                    }
                }

            }

            userReference.addListenerForSingleValueEvent(userListener
            )
        }
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

        val tokenDialog = Dialog(this)




        button_confirm_ride.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null){

                val userReference = FirebaseDatabase.getInstance().getReference(user?.uid.toString()).child("AccountDetails")
                val userListener = object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(p0: DataSnapshot) {
                        val userDetails = p0.getValue(AccountDetails::class.java)
                        if (userDetails?.membershipType == "One Way"){

                            val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
                            val today = LocalDate.now().toString()
                            val calender = Calendar.getInstance()
                            calender.time = Date.valueOf(today)
                            calender.add(Calendar.DATE,-1)
                            val validUpTo: String = dateFormatter.format(calender.time).toString()

                            userReference.child("membershipStatus").setValue("Invalid")
                            userReference.child("validUpTo").setValue(validUpTo)
                        }
                    }

                }
                userReference.addListenerForSingleValueEvent(userListener)

                val accessToken = "${(1..3).random()}${(1..3).random()}${(1..3).random()}${(1..3).random()}${(1..3).random()}${(1..3).random()}"

                tokenDialog.setContentView(R.layout.custom_access_token_popup)

                val close = tokenDialog.findViewById<TextView>(R.id.txt_close)
                val token = tokenDialog.findViewById<TextView>(R.id.txt_token)
                token.text = accessToken
                close.setOnClickListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                    tokenDialog.dismiss()
                }

                tokenDialog.show()

            }

            else {
                val snackbar = Snackbar.make(it, "Please login first", Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("Login"){

                    val intent = Intent(this@BixiDetailsActivity,LoginActivity::class.java)
                    intent.putExtra("layout","details")
                    startActivity(intent)
                    finish()
                    snackbar.dismiss()
                }
                snackbar.show()
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

        return super.onOptionsItemSelected(item)
    }
}
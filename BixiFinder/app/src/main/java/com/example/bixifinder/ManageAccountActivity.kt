package com.example.bixifinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.example.bixifinder.dbContext.AccountDetails
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_account_details.main_toolbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_manage_account.*
import java.text.SimpleDateFormat
import java.time.LocalDate

class ManageAccountActivity : AppCompatActivity() {

    internal var userDetails : AccountDetails? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)

        initialize()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        setSupportActionBar(update_toolbar)

        supportActionBar?.title = "Update Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        val user = FirebaseAuth.getInstance().currentUser
        val userReference = FirebaseDatabase.getInstance().getReference(user?.uid.toString()).child("AccountDetails")
        getUserDetails(userReference)
        button_update_details.setOnClickListener {
            updateUserDetails(userReference)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUserDetails(userReference: DatabaseReference) {
        userDetails?.name = edit_user_full_name.text.toString()
        userDetails?.address = edit_user_address.text.toString()
        userDetails?.pinCode = edit_user_zipcode.text.toString()
        userDetails?.dob = edit_user_dob.text.toString()
        if (radio_btn_female.isChecked){
            userDetails?.gender = radio_btn_female.text.toString()
        }
        else
            userDetails?.gender = radio_btn_male.text.toString()



        userReference.setValue(userDetails)

        Snackbar.make(findViewById(android.R.id.content), "Details updated successfully", Snackbar.LENGTH_SHORT)
            .show();
    }

    private fun getUserDetails(userReference: DatabaseReference) {


        val userListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(p0: DataSnapshot) {
                userDetails = p0.getValue(AccountDetails::class.java)
                edit_user_full_name.setText(userDetails?.name)
                edit_user_address.setText(userDetails?.address)
                edit_user_dob.setText(userDetails?.dob)
                edit_user_zipcode.setText(userDetails?.pinCode)

                when (userDetails?.gender?.toLowerCase()){
                    "male" ->
                        radio_btn_male.isChecked = true
                    "female" ->
                        radio_btn_female.isChecked = true
                }
            }
        }
        userReference.addListenerForSingleValueEvent(userListener)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent);
        finish()

        return super.onOptionsItemSelected(item)
    }
}

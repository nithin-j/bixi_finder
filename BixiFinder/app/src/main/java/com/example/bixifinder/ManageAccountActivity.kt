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

        initializeSpinner()
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
        userDetails?.gender = sp_gender.selectedItem.toString()


        userReference.setValue(userDetails)
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
                        sp_gender.setSelection(0,true)
                    "female" ->
                        sp_gender.setSelection(1,true)
                    "other" ->
                        sp_gender.setSelection(3,true)
                }
            }
        }
        userReference.addListenerForSingleValueEvent(userListener)
    }


    private fun initializeSpinner(){
        val gender = arrayOf("Male","Female","Other")
        val type = arrayOf("\$2.99: One Way Pass",
            "\$5.25: One DayPass",
            "\$97: One Year Pass",
            "\$36: One Month Pass")

        val genderSpinner = ArrayAdapter<String>(
            this,R.layout.custom_spinner_item,gender
        )
        genderSpinner.setDropDownViewResource(R.layout.custom_spinner_item)
        sp_gender.adapter = genderSpinner

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent);

        return super.onOptionsItemSelected(item)
    }
}

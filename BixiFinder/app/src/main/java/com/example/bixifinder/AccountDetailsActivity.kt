package com.example.bixifinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.bixifinder.dbContext.AccountDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_login.main_toolbar
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)


        initialize()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "Account Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        rdo_btn_male.isChecked = true

        button_register.setOnClickListener {
            registerNewUser()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerNewUser() {

        val user = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userReference = FirebaseDatabase.getInstance().getReference(user).child("AccountDetails")

        //validating data->
        if (et_user_full_name.text.toString().isEmpty()){
            et_user_full_name.error = "Please enter a valid name"
            et_user_full_name.requestFocus()
            return
        }
        if (et_user_address.text.toString().isEmpty()){
            et_user_address.error = "Please enter a valid address"
            et_user_address.requestFocus()
            return
        }
        if (et_user_zipcode.text.toString().isEmpty()){
            et_user_zipcode.error = "Please enter a valid pin code"
            et_user_zipcode.requestFocus()
            return
        }
        if (et_user_dob.text.toString().isEmpty()){
            et_user_dob.error = "Please enter your date of birth"
            et_user_dob.requestFocus()
            return
        }

        val database = FirebaseDatabase.getInstance()

        val id = database.getReference("AccountDetails").push().key.toString()
        val name = et_user_full_name.text.toString()
        val address = et_user_address.text.toString()
        val zip = et_user_zipcode.text.toString()
        val dob = et_user_dob.text.toString()
        var gender: String? = null

        if (rdo_btn_male.isChecked)
            gender = rdo_btn_male.text.toString()
        else
            gender = rdo_btn_female.text.toString()

        val users = AccountDetails(id, name, address, zip, dob, gender.toString(), "N/A", "Invalid", "N/A", "N/A")
        userReference.setValue(users)

        val intent = Intent(this, PurchasePassActivity::class.java)
        intent.putExtra("layout", "account")
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent);
        finish()

        return super.onOptionsItemSelected(item)
    }
}

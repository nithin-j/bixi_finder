package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.example.bixifinder.dbContext.AccountDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_login.main_toolbar
import java.sql.Date
import java.time.LocalDate

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        initializeSpinner()
        initialize()
    }

    private fun initializeSpinner() {
        val gender = arrayOf("Male","Female", "Other")
        val type = arrayOf("\$2.99: One Way Pass",
                           "\$5.25: One DayPass",
                           "\$97: One Year Pass",
                           "\$36: One Month Pass")
        val genderSpinner = ArrayAdapter<String>(
            this,R.layout.custom_spinner_item,gender
        )
        genderSpinner.setDropDownViewResource(R.layout.custom_spinner_item)
        spinner_gender.adapter = genderSpinner

        val typeSpinner = ArrayAdapter<String>(
            this,R.layout.custom_spinner_item,type
        )
        typeSpinner.setDropDownViewResource(R.layout.custom_spinner_item)
        spinner_member_type.adapter = typeSpinner
    }

    private fun initialize() {
        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "Account Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        button_register.setOnClickListener {
            registerNewUser()
        }
    }

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
        val gender = spinner_gender.selectedItem.toString()
        val type = spinner_member_type.selectedItem.toString()
        val date = LocalDate.now().toString()

        val users = AccountDetails(id, name, address, zip, dob, gender, type, "Valid", date)
        userReference.child(name).setValue(users)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent);
        return super.onOptionsItemSelected(item)
    }
}

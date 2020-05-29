package com.example.bixifinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.example.bixifinder.dbContext.AccessPass
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.activity_purchase_pass.*
import kotlinx.android.synthetic.main.activity_purchase_pass.main_toolbar
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.zip.Inflater

class PurchasePassActivity : AppCompatActivity() {

    var arrayList = ArrayList<String>()
    var arrayAdapter: ArrayAdapter<String>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_pass)

        initialise()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialise() {
        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "Purchase Access Pass"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        arrayAdapter = ArrayAdapter<String>(applicationContext, android.R.layout.simple_spinner_dropdown_item,arrayList)
        loadAccessPass()
        sp_member_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var passDetails : AccessPass? = null
                val passReference = FirebaseDatabase.getInstance().getReference("AccessPass").child(position.toString())

                val passListener = object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(p0: DataSnapshot) {
                        passDetails = p0.getValue(AccessPass::class.java)
                        lbl_name.text = passDetails?.name
                        lbl_price.text  = "$${passDetails?.price}"
                        lbl_description.text = passDetails?.description
                    }
                }
                passReference.addListenerForSingleValueEvent(passListener)
            }

        }

        button_purchase.setOnClickListener {
            purchasePass()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun purchasePass() {

        val user = FirebaseAuth.getInstance().currentUser
        val userReference = FirebaseDatabase.getInstance().getReference(user?.uid.toString()).child("AccountDetails")

        if (user != null){
            val type = lbl_name.text
            val status = "Valid"
            val membershipDate = LocalDate.now().toString()

            val calender = Calendar.getInstance()
            calender.time = Date.valueOf(membershipDate)

            when (sp_member_type.selectedItemId.toString()){
                "0" ->
                    calender.add(Calendar.DATE,0).toString()
                "1" ->
                    calender.add(Calendar.DATE, 1).toString()
                "2" ->
                    calender.add(Calendar.DATE, 30).toString()
                "3" ->
                    calender.add(Calendar.DATE, 365).toString()
            }

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
            val validUpTo: String = dateFormatter.format(calender.time).toString()

            var userListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    userReference.child("membershipStatus").setValue(status)
                    userReference.child("membershipType").setValue(type)
                    userReference.child("membershipDate").setValue(membershipDate)
                    userReference.child("validUpTo").setValue(validUpTo)
                }
            }
            userReference.addListenerForSingleValueEvent(userListener)

            Snackbar.make(findViewById(android.R.id.content), "BIXI Access Pass updated successfully", Snackbar.LENGTH_SHORT).show();
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("layout","pass")
            startActivity(intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val layout = intent.getStringExtra("layout")
        if (layout.equals("main")){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        return super.onOptionsItemSelected(item)
    }

    private fun loadAccessPass() {
        //val userReference = FirebaseDatabase.getInstance().getReference(uid).child("AccountDetails")
        val passReference = FirebaseDatabase.getInstance().getReference("AccessPass")

        arrayAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_member_type.adapter =arrayAdapter
        
        val passListener = object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {


            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val accessPass = p0.getValue(AccessPass::class.java)
                arrayList.add(accessPass.toString())
                arrayAdapter?.notifyDataSetChanged()


            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        }
        passReference.addChildEventListener(passListener)
    }
}

package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_login.*

class AccountDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        initialize()
    }

    private fun initialize() {
        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "Account Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent);
        return super.onOptionsItemSelected(item)
    }
}

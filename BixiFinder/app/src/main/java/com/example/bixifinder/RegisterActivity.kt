package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.main_toolbar
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initialize()
    }

    private fun initialize() {
        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        button_Next.setOnClickListener{
            val intent = Intent(this, AccountDetailsActivity::class.java)
            startActivity(intent);
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, StartupActivity::class.java)
        startActivity(intent);

        return super.onOptionsItemSelected(item)
    }
}

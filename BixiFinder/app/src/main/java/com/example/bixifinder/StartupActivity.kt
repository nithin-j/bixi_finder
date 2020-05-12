package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_startup.*

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)


        initialize()
    }

    private fun initialize() {

        button_load_maps.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent);
            finish()
        }
        button_load_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent);
            finish()
        }
        button_load_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent);
            finish()
        }
    }
}

package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_learn_more.*
import kotlinx.android.synthetic.main.activity_login.*

class LearnMoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_more)

        setSupportActionBar(toolbar)

        supportActionBar?.title = "About"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        webView.loadUrl("https://bixi.com/en/who-we-are")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        return super.onOptionsItemSelected(item)
    }
}

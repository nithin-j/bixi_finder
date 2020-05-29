package com.example.bixifinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.main_toolbar
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity()  {

    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        button_Next.setOnClickListener{
            signUp(tv_Email.text.toString(),tv_Password.text.toString())
        }
    }

    private fun signUp(email: String, password: String) {
        if (tv_Email.text.toString().isEmpty()){
            tv_Email.error = "Please enter a valid email"
            tv_Email.requestFocus()
            return
        }
        if (tv_Password.text.toString().isEmpty()){
            tv_Password.error = "Password is missing"
            tv_Password.requestFocus()
            return
        }
        if (tv_Confirm_Password.text.toString().isEmpty()){
            tv_Confirm_Password.error = "Confirm Password is missing"
            tv_Confirm_Password.requestFocus()
            return
        }
        if (tv_Confirm_Password.text.toString() != tv_Password.text.toString()){
            tv_Password.error = "Passwords does not match"
            tv_Password.text = null
            tv_Confirm_Password.text = null
            tv_Password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, AccountDetailsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }

            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val intent = Intent(this, StartupActivity::class.java)
        startActivity(intent);
        finish()

        return super.onOptionsItemSelected(item)
    }
}

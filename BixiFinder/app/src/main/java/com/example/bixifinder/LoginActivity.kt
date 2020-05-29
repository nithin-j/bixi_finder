package com.example.bixifinder

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initialize();

        button_login.setOnClickListener {
            userLogin(et_login_email.text.toString(), et_login_password.text.toString()) }
    }

    private fun userLogin(email: String, password: String) {
        if (et_login_email.text.toString().isEmpty()){
            et_login_email.error = "Please enter a valid email"
            et_login_email.requestFocus()
            return
        }
        if (et_login_password.text.toString().isEmpty()){
            et_login_password.error = "Password is missing"
            et_login_password.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    updateUI(null)
                }
            }

    }

    private fun updateUI(user: FirebaseUser?) {
        if (user !=null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            Toast.makeText(baseContext, "Unable to login. try again",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun initialize() {
        setSupportActionBar(main_toolbar)

        supportActionBar?.title = "Login"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val layout = intent.getStringExtra("layout")

        if (layout.toLowerCase().equals("main")){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            val intent = Intent(this, StartupActivity::class.java)
            startActivity(intent);
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}

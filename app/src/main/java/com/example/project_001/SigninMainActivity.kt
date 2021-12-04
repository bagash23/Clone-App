package com.example.project_001

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SigninMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin_main)

        findViewById<Button>(R.id.signup_link_btn).setOnClickListener {
            startActivity(Intent(this, SignUpMainActivity::class.java))
        }

        findViewById<Button>(R.id.login_btn).setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser() {
        val email = findViewById<TextView>(R.id.email_login).text.toString()
        val password = findViewById<TextView>(R.id.password_login).text.toString()

        when{
            TextUtils.isEmpty((email)) -> Toast.makeText(this, "email is required", Toast.LENGTH_LONG)
            TextUtils.isEmpty((password)) -> Toast.makeText(this, "password is required", Toast.LENGTH_LONG)

            else -> {
                val progressDialog = ProgressDialog(this@SigninMainActivity)
                progressDialog.setTitle("Login")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        progressDialog.dismiss()
                        val intent = Intent(this@SigninMainActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        val message = task.exception!!.toString()
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG)
                        FirebaseAuth.getInstance().signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this@SigninMainActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
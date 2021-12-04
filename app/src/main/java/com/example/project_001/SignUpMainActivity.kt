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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

class SignUpMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_main)

        findViewById<Button>(R.id.sigin_link_btn).setOnClickListener {
            startActivity(Intent(this, SigninMainActivity::class.java))
        }

        findViewById<Button>(R.id.signup_btn).setOnClickListener {
            CraateAccount()
        }

    }

    private fun CraateAccount() {
        val fullname = findViewById<TextView>(R.id.name_register).text.toString()
        val username = findViewById<TextView>(R.id.username_register).text.toString()
        val email = findViewById<TextView>(R.id.email_register).text.toString()
        val password = findViewById<TextView>(R.id.password_register).text.toString()

        when{
            TextUtils.isEmpty(fullname) -> Toast.makeText(this, "full name is required", Toast.LENGTH_LONG)
            TextUtils.isEmpty(username) -> Toast.makeText(this, "username is required", Toast.LENGTH_LONG)
            TextUtils.isEmpty(email) -> Toast.makeText(this, "email is required", Toast.LENGTH_LONG)
            TextUtils.isEmpty(password) -> Toast.makeText(this, "password is required", Toast.LENGTH_LONG)

            else -> {
                val progressDialog = ProgressDialog(this@SignUpMainActivity)
                progressDialog.setTitle("SingUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            saveUserInfo(fullname, username, email, progressDialog)
                        }
                        else {
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG)
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullname: String, username: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val usersMap =  HashMap<String, Any>()
        usersMap["uid"] = currentUserID
        usersMap["fullname"] = fullname.toLowerCase()
        usersMap["username"] = username.toLowerCase()
        usersMap["email"] = email
        usersMap["bio"] = "Android Developer || Jakarta"
        usersMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-clone-app-24d2d.appspot.com/o/Default%20Images%2Fbagas.jpg?alt=media&token=2566763d-803f-4311-87f7-5f7619d1c2f0"

        usersRef.child(currentUserID).setValue(usersMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been oreated successfully", Toast.LENGTH_LONG)


                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)


                    val intent = Intent(this@SignUpMainActivity, MainActivity::class.java)
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
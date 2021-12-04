package com.example.project_001

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.project_001.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

class AccountSettingsMainActivity2 : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicker: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings_main2)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicker = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        findViewById<Button>(R.id.logout_btn).setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsMainActivity2, SigninMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.change_image_text_btn).setOnClickListener {

            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsMainActivity2)
        }

        findViewById<ImageView>(R.id.save_info_profile_btn).setOnClickListener {
            if (checker == "clicked"){
                uploadImageAndUpdateInfo()
            }else{
                updateUserInfoOnly()
            }
        }

        userInfo()

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<ImageView>(R.id.profile_image_view_profile_frag).setImageURI(imageUri)

        }
    }

    private fun updateUserInfoOnly() {

        when {
            TextUtils.isEmpty(findViewById<TextView>(R.id.full_name_profile_frag).text.toString()) -> {
                Toast.makeText(this, "Please write full name first", Toast.LENGTH_LONG)
            }
            findViewById<TextView>(R.id.username_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write user name first", Toast.LENGTH_LONG)
            }
            findViewById<TextView>(R.id.bio_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write bio first", Toast.LENGTH_LONG)
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

                val usersMap =  HashMap<String, Any>()
                usersMap["fullname"] = findViewById<TextView>(R.id.full_name_profile_frag).text.toString().toLowerCase()
                usersMap["username"] = findViewById<TextView>(R.id.username_profile_frag).text.toString().toLowerCase()
                usersMap["bio"] = findViewById<TextView>(R.id.bio_profile_frag).text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(usersMap)
                Toast.makeText(this, "Account Information has been successfully", Toast.LENGTH_LONG)

                val intent = Intent(this@AccountSettingsMainActivity2, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private  fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(findViewById<ImageView>(R.id.profile_image_view_profile_frag))
                    findViewById<TextView>(R.id.username_profile_frag).setText(user!!.getUsername())
                    findViewById<TextView>(R.id.full_name_profile_frag).setText(user!!.getFullname())
                    findViewById<TextView>(R.id.bio_profile_frag).setText(user!!.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun uploadImageAndUpdateInfo() {

        when{
            imageUri == null -> Toast.makeText(this, "Please select image firs", Toast.LENGTH_LONG)
            TextUtils.isEmpty(findViewById<TextView>(R.id.full_name_profile_frag).text.toString()) -> {
                Toast.makeText(this, "Please write full name first", Toast.LENGTH_LONG)
            }
            findViewById<TextView>(R.id.username_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write user name first", Toast.LENGTH_LONG)
            }
            findViewById<TextView>(R.id.bio_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write bio first", Toast.LENGTH_LONG)
            }

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileref = storageProfilePicker!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri>{task ->
                    if (task.isSuccessful){
                        val downloadUri = task.result
                        myUrl = downloadUri.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val usersMap =  HashMap<String, Any>()
                        usersMap["fullname"] = findViewById<TextView>(R.id.full_name_profile_frag).text.toString().toLowerCase()
                        usersMap["username"] = findViewById<TextView>(R.id.username_profile_frag).text.toString().toLowerCase()
                        usersMap["bio"] = findViewById<TextView>(R.id.bio_profile_frag).text.toString().toLowerCase()
                        usersMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(usersMap)

                        Toast.makeText(this, "Account Information has been successfully", Toast.LENGTH_LONG)

                        val intent = Intent(this@AccountSettingsMainActivity2, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                    }
                } )
            }
        }
    }

}
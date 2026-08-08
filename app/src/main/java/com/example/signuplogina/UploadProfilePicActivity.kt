package com.example.signuplogina

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.signuplogina.databinding.ActivityUploadProfilePicBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class UploadProfilePicActivity : AppCompatActivity() {
    private lateinit var uriImage: Uri
    private val PICK_IMAGE_REQUEST: Int = 1
    private lateinit var binding: ActivityUploadProfilePicBinding
    private lateinit var imageViewUploadPic: ImageView
    private lateinit var authProfile: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUploadProfilePicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.title = "Upload Profile Picture"

        val buttonUploadPicChoose: Button = binding.uploadPicChooseButton
        val buttonUploadPic: Button = binding.uploadPicButton
        val progressBar = binding.progressBar
        imageViewUploadPic = binding.imageViewProfilePic
        authProfile = FirebaseAuth.getInstance()

        //  firebaseUser=authProfile.currentUser!!
        // Get the current authenticated user
        // Get the current authenticated user
        authProfile.currentUser?.let {
            firebaseUser = it
        } ?: run {
            // Handle the case where the current user is null
            Log.e("FirebaseAuth", "User is not authenticated")
            // You can also show a message to the user or redirect to a login screen
            return
        }

// Initialize the storage reference for storing/displaying profile pictures
// If there is a location "DisplayPics" in Firebase Storage, this line will get a reference to it.
// If it doesn't exist, it will create the location and get a reference to it.
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics")

// Check if the user has a profile picture (photoUrl is not null)
        firebaseUser.photoUrl?.let { uri ->
            // If the user has a profile picture, load it into the ImageView using Picasso
            Picasso.with(this@UploadProfilePicActivity).load(uri).into(imageViewUploadPic)
        } ?: run {
            // Handle the case where there is no profile picture
            Log.e("FirebaseAuth", "User has no profile picture set")
        }

// Set up the button to choose a new profile picture from the file chooser
        buttonUploadPicChoose.setOnClickListener {
            openFileChooser()
        }
        buttonUploadPic.setOnClickListener {
            UploadPic()
        }


    }
    private fun UploadPic() {
        if (uriImage != null) {
            // Save the image with the UID of the currently logged-in user
            val fileReference: StorageReference = storageReference.child(
                authProfile.currentUser!!.uid + "." + getFileExtension(uriImage) // Get file extension
            )

            fileReference.putFile(uriImage).addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded file
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    // Handle the download URL here (e.g., update user profile with this URL)
                    val downloadUrl = uri
                    firebaseUser=authProfile.currentUser!!

                    val profileUpdates:UserProfileChangeRequest= UserProfileChangeRequest.Builder().setPhotoUri(downloadUrl).build()
                    firebaseUser.updateProfile(profileUpdates)

                    Log.d("UploadPic", "File uploaded successfully. Download URL: $downloadUrl")
                    Toast.makeText(this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    // Handle any errors
                    Log.e("UploadPic", "Failed to get download URL")
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Handle any errors during the upload
                Log.e("UploadPic", "Upload failed: ${it.message}")
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to get the file extension of the selected image
    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }





    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            uriImage = data.data!!
            imageViewUploadPic.setImageURI(uriImage)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflate menu items
        menuInflater.inflate(R.menu.common_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.menu_refresh -> {
                // refresh activity
                startActivity(intent)
                finish()
            }
            R.id.menu_update_profile -> {
                val intent =Intent(this@UploadProfilePicActivity,UpdateProfileActivity::class.java)
                startActivity(intent)
                finish()// we don't want multiple duplicate activities running, we just want the UserProfile activity Running in the background
            }
            R.id.menu_update_email -> {
                val intent =Intent(this@UploadProfilePicActivity, UpdateEmailActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_settings -> {
                Toast.makeText(this, "menu settings", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_change_pass -> {
                val intent =Intent(this@UploadProfilePicActivity,ChangePasswordActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_delete_profile -> {
                val intent =Intent(this@UploadProfilePicActivity,DeleteProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.menu_logout -> {
                authProfile.signOut()
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@UploadProfilePicActivity, SignUpLogin::class.java)
                // clear the stack to prevent the user from coming back to the userProfileActivity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()// close UserProfileActivity
            }

            else -> {
                Toast.makeText(this, "Something Went Wrong!!!", Toast.LENGTH_SHORT).show()

            }
        }
        return super.onOptionsItemSelected(item)


    }

}
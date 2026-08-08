package com.example.signuplogina

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddItem : AppCompatActivity() {

    // Firebase references
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseItems: DatabaseReference
    private lateinit var storageReference: StorageReference // Reference for Firebase Storage

    // UI components
    private lateinit var productNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var availabilityEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var uploadButton: Button
    private lateinit var productImageView: ImageView // Declare ImageView for product image
    private lateinit var backButton: ImageView // Declare back button ImageView

    private var selectedImageUri: Uri? = null // Variable to hold the selected image URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseItems = FirebaseDatabase.getInstance().getReference("Items") // Reference for Items
        storageReference = FirebaseStorage.getInstance().reference // Initialize storage reference

        // Initialize UI components
        productNameEditText = findViewById(R.id.editTextProductName)
        descriptionEditText = findViewById(R.id.editTextDescription)
        availabilityEditText = findViewById(R.id.editTextAvailability)
        categorySpinner = findViewById(R.id.spinnerCategory)
        uploadButton = findViewById(R.id.btnUploadItem)
        productImageView = findViewById(R.id.ivProductImage) // Initialize ImageView
        backButton = findViewById(R.id.ivMenu) // Initialize back button

        // Set up category spinner (you can populate this with real categories)
        val categories = arrayOf("Electronics", "Furniture", "Clothing", "Books", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // Set up click listener for the image view to choose an image
        productImageView.setOnClickListener {
            openGallery()
        }

        // Set up upload button
        uploadButton.setOnClickListener {
            uploadItemToFirebase()
        }
        // Set up click listener for the back button
        backButton.setOnClickListener {
            finish() // Close the current activity and return to the previous one
        }
    }

    private fun openGallery() {
        // Intent to open the gallery
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // Limit to images
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // Handle the result of the gallery selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data // Get the image URI
            productImageView.setImageURI(selectedImageUri) // Set the selected image to ImageView
        }
    }
    private fun uploadItemToFirebase() {
        // Get userId of the currently logged-in user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Validate inputs
            val productName = productNameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val availability = availabilityEditText.text.toString().toIntOrNull() ?: 0
            val category = categorySpinner.selectedItem.toString()

            if (TextUtils.isEmpty(productName)) {
                productNameEditText.error = "Please enter the product name"
                return
            }

            if (TextUtils.isEmpty(description)) {
                descriptionEditText.error = "Please enter a description"
                return
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return
            }

            // Create a reference to Firebase Storage
            val storageReference = FirebaseStorage.getInstance().getReference("Items/${userId}/${System.currentTimeMillis()}.jpg")

            // Upload the image to Firebase Storage
            storageReference.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL of the uploaded image
                    storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Create item data object
                        val item = Item(
                            productName = productName,
                            description = description,
                            availability = availability,
                            category = category,
                            imageUrl = downloadUrl.toString() // Add image URL to item
                        )

                        // Push the item under the Items node with userId as a parent
                        val userItemsRef = databaseItems.child(userId).push() // Unique item ID generated
                        userItemsRef.setValue(item)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Item uploaded successfully", Toast.LENGTH_SHORT).show()
                                    // Clear fields after successful upload
                                    clearInputFields()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to upload item: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputFields() {
        productNameEditText.text.clear()
        descriptionEditText.text.clear()
        availabilityEditText.text.clear()
        categorySpinner.setSelection(0)
        productImageView.setImageURI(null) // Clear the image view
        selectedImageUri = null // Reset the selected image URI
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1001 // Request code for gallery
    }
}

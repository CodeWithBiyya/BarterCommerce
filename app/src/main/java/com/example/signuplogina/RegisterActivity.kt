package com.example.signuplogina

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.signuplogina.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.regex.Matcher
import java.util.regex.Pattern



class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var radioBtnRegisterGenderSelected: RadioButton
    private lateinit var picker:DatePickerDialog

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize all the necessary views early
        val fullNameField = binding.editTextRegisterFullName
        val emailField = binding.editTextRegisterEmail
        val dobField = binding.editTextRegisterDob
        val mobileField = binding.editTextRegisterMobile
        val passwordField = binding.editTextRegisterPassword
        val confirmPassField = binding.editTextRegisterConfirmPass
        val genderGroup = binding.radioGroupRegisterGender
        val progressBar = binding.progressBar

        Toast.makeText(this, "You Can Register Now", Toast.LENGTH_SHORT).show()

        genderGroup.clearCheck()
        // Setting up DatePicker on EditText
        dobField.setOnClickListener {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            // Date Picker Dialog
             picker = DatePickerDialog(
                this@RegisterActivity,
                 { _, selectedYear, selectedMonth, selectedDay ->
                     dobField.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                 },
                year,
                month,
                day
            )
            picker.show()
        }


        binding.btnRegister.setOnClickListener {
            val selectedGenderId = genderGroup.checkedRadioButtonId
            if (selectedGenderId != -1) {
                radioBtnRegisterGenderSelected = findViewById(selectedGenderId)
            }

            val fullName = fullNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val dob = dobField.text.toString().trim()
            val mobile = mobileField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPass = confirmPassField.text.toString().trim()
            val gender: String?

            // validate Mobile number using Matcher and pattern (Regular Expression)
            val mobileRegex="^03[0-9]{2}[0-9]{7}$"
            val mobileMatcher: Matcher
            val mobilePattern:Pattern=Pattern.compile(mobileRegex)
            mobileMatcher=mobilePattern.matcher(mobile)




            when {
                fullName.isEmpty() -> {
                    fullNameField.error = "Full Name is Required"
                    fullNameField.requestFocus()
                }

                email.isEmpty() -> {
                    emailField.error = "Email is Required"
                    emailField.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailField.error = "Valid Email is Required"
                    emailField.requestFocus()
                }

                dob.isEmpty() -> {
                    dobField.error = "DOB is Required"
                    dobField.requestFocus()
                }

                selectedGenderId == -1 -> {
                    Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
                    genderGroup.requestFocus()
                }

                mobile.isEmpty() -> {
                    mobileField.error = "Mobile is Required"
                    mobileField.requestFocus()
                }

                mobile.length != 11 -> {
                    mobileField.error = "Number must be 11 digits"
                    mobileField.requestFocus()
                }

                !mobileMatcher.find()->{
                    mobileField.error = "Not a valid Number"
                    mobileField.requestFocus()
                }
                password.isEmpty() -> {
                    passwordField.error = "Password is Required"
                    passwordField.requestFocus()
                }

                password.length < 8 -> {
                    passwordField.error = "Password must be at least 8 characters"
                    passwordField.requestFocus()
                }

                confirmPass.isEmpty() -> {
                    confirmPassField.error = "Confirm Password is Required"
                    confirmPassField.requestFocus()
                }

                password != confirmPass -> {
                    confirmPassField.error = "Passwords do not match"
                    confirmPassField.requestFocus()
                    passwordField.text.clear()
                    confirmPassField.text.clear()
                }

                else -> {
                    gender = radioBtnRegisterGenderSelected.text.toString()
                    progressBar.visibility = View.VISIBLE

                    registerUser(fullName, email, mobile, dob, gender, password)
                }
            }
        }
    }

    private fun registerUser(
        fullName: String,
        email: String,
        mobile: String,
        dob: String,
        gender: String,
        password: String
    ) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                // Store user data in Firebase Realtime Database
                val userId = user?.uid ?: return@addOnCompleteListener
                val userDatabaseRef =
                    FirebaseDatabase.getInstance().getReference("Users").child(userId)
                val userData = User(fullName, dob, gender, mobile)

                userDatabaseRef.setValue(userData).addOnCompleteListener { dbTask ->
                    if (dbTask.isSuccessful) {
                        binding.progressBar.visibility=View.GONE

                        user.sendEmailVerification()

                        Toast.makeText(
                            this,
                            "Registration successful: ${user.email}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Start UserProfileActivity and clear previous activities
//                        val intent = Intent(this, UserProfileActivity::class.java)
//                        intent.flags =
//                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                        startActivity(intent)
//                        finish() // Close RegisterActivity
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to save user data: ${dbTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progressBar.visibility=View.GONE

                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Registration failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                try {
                    throw task.exception ?: Exception("Unknown error")
                } catch (e: FirebaseAuthWeakPasswordException) {
                    // Handle the weak password exception
                    Toast.makeText(this, "Weak password: ${e.message}", Toast.LENGTH_SHORT).show()
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    // Handle invalid credentials exception
                    Toast.makeText(this, "Invalid credentials: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: FirebaseAuthUserCollisionException) {
                    // Handle user collision exception
                    Toast.makeText(this, "User already exists: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    // Handle any other exceptions
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.progressBar.visibility=View.GONE

            }
        }
    }

}


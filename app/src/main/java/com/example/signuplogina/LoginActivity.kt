package com.example.signuplogina

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.signuplogina.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authProfile: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authProfile = FirebaseAuth.getInstance()

        // Initialize fields inside onCreate after binding is set up
        val emailField = binding.editTextLoginEmail
        val passwordField = binding.editTextLoginPassword
        val progressBar = binding.progressBar
        val buttonForgotPassword: Button =binding.buttonForgotPassword
        //Reset Password
        buttonForgotPassword.setOnClickListener {
            Toast.makeText(this,"You can reset your password ", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@LoginActivity,ForgotPassActivity::class.java))
            // we don't need to finish() this activity because the user can navigate to this screen while on forgetPassActivity
            //after changing the password by forget pass email and they can get back to the login activity to login after change password

        }

        // Show/Hide password using eye icon
        val imageViewShowHidePass: ImageView = binding.imageViewShowHidePwd
        imageViewShowHidePass.setImageResource(R.drawable.ic_hide_pwd)

        imageViewShowHidePass.setOnClickListener {
            if (passwordField.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // If the password is hidden, show it
                passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imageViewShowHidePass.setImageResource(R.drawable.ic_show_pwd)
            } else {
                // If the password is visible, hide it
                passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
                imageViewShowHidePass.setImageResource(R.drawable.ic_hide_pwd)
            }
            // Move the cursor to the end of the text
            passwordField.setSelection(passwordField.text.length)
        }

        // Login user
        binding.buttonLogin.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            when {
                email.isEmpty() -> {
                    emailField.error = "Email is Required"
                    emailField.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailField.error = "Please Give Valid Email"
                    emailField.requestFocus()
                }

                password.isEmpty() -> {
                    passwordField.error = "Password is Required"
                    passwordField.requestFocus()
                }

                else -> {
                    progressBar.visibility = View.VISIBLE
                    loginUser(email, password, emailField, passwordField, progressBar)
                }
            }
        }
    }

    private fun loginUser(
        email: String,
        password: String,
        emailField: EditText,
        passwordField: EditText,
        progressBar: ProgressBar
    ) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                // Proceed to the next screen or functionality
                // get instance of the current user
                val firebaseUser: FirebaseUser? = authProfile.currentUser
                if (firebaseUser!!.isEmailVerified) {
                    Toast.makeText(this, "You are Logged in Now", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,UserProfileActivity::class.java))
                    finish()// close login activity
                } else {
                    firebaseUser.sendEmailVerification();
                    authProfile.signOut()
                    showAlertDialog()
                }
            } else {
                try {
                    throw task.exception ?: Exception("Unknown error")
                } catch (e: FirebaseAuthInvalidUserException) {
                    // Invalid email (not registered)
                    emailField.error =
                        "User doesn't exist or is no longer valid, Please Register Again"
                    emailField.requestFocus()
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    // Invalid credentials (likely wrong password)
                    emailField.error = "Invalid Credentials .Please Recheck and type Again"
                    emailField.requestFocus()
                } catch (e: Exception) {
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAlertDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Email Not Verified")
        builder.setMessage(" Please verify your Email Now . You Can't login without Email verification")

        // Open Email Apps if user clickes continue button
        builder.setPositiveButton("Continue", DialogInterface.OnClickListener { dialog, which ->
            // Handle the "Continue" button click here
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)// To email app in the new window and not in our App
            startActivity(intent)
        }).setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                // Handle the "Cancel" button click here
                dialog.dismiss()
            })

        val alertDialog = builder.create()// created alert dialog box, using builder .create
        alertDialog.show()// we have to show the alert
    }
    override fun onStart() {
        super.onStart()
        if ( authProfile.currentUser!=null){
            Toast.makeText(this,"Already Logged In", Toast.LENGTH_SHORT).show()
        // start the profile activity
//            startActivity(Intent(this,UserProfileActivity::class.java))
            startActivity(Intent(this@LoginActivity,HomeScreen::class.java))
            finish()// close login activity
        }
        else{
            Toast.makeText(this,"You can login now", Toast.LENGTH_SHORT).show()

        }

    }


}

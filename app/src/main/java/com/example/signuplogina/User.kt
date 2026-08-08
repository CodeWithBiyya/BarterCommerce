package com.example.signuplogina
// helper class to read write user details
// we will use this same helper class in userprofileactivity to get the snapshots of the data
data class User(
    val fullName: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val mobile: String? = null
)

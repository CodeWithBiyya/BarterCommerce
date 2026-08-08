package com.example.signuplogina
data class Item(
    val productName: String = "",
    val description: String = "",
    val availability: Int = 0,
    val category: String = "",
    val imageUrl: String = "" // Add this field for the image URL
)

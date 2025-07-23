package com.donationtracker.app.model


data class Donation(
    val id:String ="",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhoto: String = "",
    val amount: Double = 0.0,
    val method: String = "",             // e.g. "PayPal", "Direct"
    val message: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),  // when created
    val updatedAt: Long = System.currentTimeMillis()   // when updated
)
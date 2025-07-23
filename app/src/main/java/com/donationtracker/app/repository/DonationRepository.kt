package com.donationtracker.app.repository

import android.util.Log
import com.donationtracker.app.model.Donation
import com.donationtracker.app.model.User
import com.google.firebase.firestore.FirebaseFirestore

object DonationRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addDonation(donation: Donation, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("donations").document()
        val donationWithId = donation.copy(id = docRef.id)

        docRef.set(donationWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getDonations(onSuccess: (List<Donation>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("donations")
            .get()
            .addOnSuccessListener { result ->
                val donations = result.map { it.toObject(Donation::class.java) }
                onSuccess(donations)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun fetchUser(userId: String, onResult: (User?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    Log.d("TAG", "fetchUser: $user")
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun deleteDonation(donationId: String, onSuccess: () -> Unit={}, onFailure: (Exception) -> Unit={}) {
        db.collection("donations").document(donationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getDonationById(donationId: String, onSuccess: (Donation?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("donations").document(donationId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val donation = document.toObject(Donation::class.java)
                    onSuccess(donation)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateDonation(donation: Donation, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("donations").document(donation.id)
            .set(donation)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun updateUserProfileImage(userId: String, imageUrl: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId)
            .update("photo", imageUrl)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
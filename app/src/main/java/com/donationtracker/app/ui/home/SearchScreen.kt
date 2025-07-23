package com.donationtracker.app.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.donationtracker.app.model.Donation
import com.donationtracker.app.repository.DonationRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SearchScreen(innerPadding: PaddingValues, onlyMine: Boolean, navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var donations by remember { mutableStateOf<List<Donation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val user = FirebaseAuth.getInstance().currentUser

    // Fetch all donations on load
    LaunchedEffect(Unit) {
        DonationRepository.getDonations(
            onSuccess = {
                donations = it
                isLoading = false
            },
            onFailure = {
                Log.e("Firestore", "Failed to load donations", it)
                isLoading = false
            }
        )
    }

    // Filter by ownership first, then by search
    val filteredDonations = donations
        .filter { if (onlyMine && user != null) it.userId == user.uid else true }
        .filter { donation ->
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) return@filter true
            val amountMatch = donation.amount.toString().contains(query, ignoreCase = true)
            val messageMatch = donation.message.lowercase().contains(query)
            val nameMatch = donation.userName.lowercase().contains(query)
            amountMatch || messageMatch || nameMatch
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search Donations...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredDonations.isEmpty()) {
            Text("No donations found.", color = Color.Gray)
        } else {
            LazyColumn {
                items(filteredDonations) { donation ->
                    DonationCard(
                        donation = donation,
                        onShowMore = {
                            navController.navigate("donationDetail/${donation.id}")
                        },
                    )
                }
            }
        }
    }
}

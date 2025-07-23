package com.donationtracker.app.ui.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.donationtracker.app.R
import com.donationtracker.app.model.Donation
import com.donationtracker.app.model.User
import com.donationtracker.app.repository.DonationRepository
import com.donationtracker.app.repository.DonationRepository.deleteDonation
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController

@Composable
fun ReportScreen(innerPadding: PaddingValues, onlyMine: Boolean, navController: NavController? = null) {
    val user = FirebaseAuth.getInstance().currentUser
    var donations by remember { mutableStateOf<List<Donation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch donations using repository
    LaunchedEffect(user) {
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

    val filteredDonations = if (onlyMine && user != null) {
        donations.filter { it.userId == user.uid }
    } else {
        donations
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Donations List",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredDonations.isEmpty()) {
            Text("No donations found.", color = Color.Gray)
        } else {
            filteredDonations.forEach { donation ->
                DonationCard(
                    donation = donation,
                    onShowMore = {
                        navController?.navigate("donationDetail/${donation.id}")
                    },
                    onDelete = { onDeleteComplete -> deleteDonation(
                        donationId = donation.id,
                        onSuccess = {
                            DonationRepository.getDonations(
                                onSuccess = { donations = it },
                                onFailure = {
                                    Log.e(
                                        "Firestore",
                                        "Failed to reload donations",
                                        it
                                    )
                                }
                            )
                            onDeleteComplete()
                        }
                    ) }
                )
            }
        }
    }
}


@Composable
fun DonationCard(
    donation: Donation,
    onShowMore: () -> Unit = {},
    onDelete: (onDeleteComplete: () -> Unit) -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isMyDonation = currentUser?.uid == donation.userId
    
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2196F3)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Row: Image, Method, Amount, Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (donation.userPhoto.isNotEmpty()) {
                            val painter = rememberAsyncImagePainter(
                                model = donation.userPhoto,
                                error = painterResource(R.drawable.app_logo)
                            )
                            Image(
                                painter = painter,
                                contentDescription = "User Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.app_logo),
                                contentDescription = "Default Profile Photo",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = donation.userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "€${donation.amount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isMyDonation) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Donated and Modified dates
            Text(
                text = "Donated ${formatDate(donation.createdAt)}",
                fontSize = 12.sp,
                color = Color.White
            )
            Text(
                text = "Modified ${formatDate(donation.updatedAt)}",
                fontSize = 12.sp,
                color = Color.White
            )

            // Expanded content - only show for user's own donations
            if (isMyDonation && expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = donation.message,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show More and Delete buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onShowMore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Show More")
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Confirm Delete?", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
            text = { Text("Are you sure you want to delete this donation?") },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        onDelete {
                            isDeleting = false
                            showDeleteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    shape = RoundedCornerShape(50),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Yes")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    shape = RoundedCornerShape(50),
                    enabled = !isDeleting
                ) {
                    Text("No")
                }
            },
            containerColor = Color(0xFFEFF3FA)
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
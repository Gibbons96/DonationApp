package com.donationtracker.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.donationtracker.app.model.Donation
import com.donationtracker.app.repository.DonationRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationDetailScreen(donationId: String, navController: NavController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    var donation by remember { mutableStateOf<Donation?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    
    // Check if this donation belongs to the current user
    val isMyDonation = donation?.userId == currentUser?.uid

    // Fetch donation details
    LaunchedEffect(donationId) {
        DonationRepository.getDonationById(
            donationId,
            onSuccess = {
                donation = it
                message = it?.message ?: ""
                isLoading = false
            },
            onFailure = {
                Toast.makeText(context, "Failed to load donation", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    donation?.let { d ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Donation Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00449D),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Please Update your Message Below",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = d.method,
                    onValueChange = {},
                    label = { Text("Payment Type") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = "€${d.amount}",
                    onValueChange = {},
                    label = { Text("Payment Amount") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = formatDate(d.createdAt),
                    onValueChange = {},
                    label = { Text("Date Donated") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { if (isEditing && isMyDonation) message = it },
                    label = { Text("Message") },
                    trailingIcon = {
                        if (isMyDonation) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Message")
                            }
                        }
                    },
                    readOnly = !isEditing || !isMyDonation,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (message.isBlank()) return@Button
                        if (isSaving) return@Button
                        isSaving = true
                        val updatedDonation = d.copy(message = message, updatedAt = System.currentTimeMillis())
                        DonationRepository.updateDonation(
                            updatedDonation,
                            onSuccess = {
                                isSaving = false
                                Toast.makeText(context, "Donation updated", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onFailure = {
                                isSaving = false
                                Toast.makeText(context, "Failed to update donation", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = isMyDonation && isEditing && message.isNotBlank() && message != d.message && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}
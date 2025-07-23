package com.donationtracker.app.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.donationtracker.app.model.Donation
import com.donationtracker.app.repository.DonationRepository
import com.donationtracker.app.R
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DonateScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: "unknown"

    var selectedOption by remember { mutableStateOf("PayPal") }
    var amount by remember { mutableStateOf("10") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                "Welcome ${user?.displayName ?: "User"},",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Text(
                "Please Give Generously",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
            Spacer(Modifier.height(10.dp))

            // Payment options
            PaymentOptionRow(
                option = "PayPal",
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                amount = amount,
                onAmountChange = { amount = it },
                isEnabled = selectedOption == "PayPal" && !isLoading
            )
            PaymentOptionRow(
                option = "Direct",
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                amount = amount,
                onAmountChange = { amount = it },
                isEnabled = selectedOption == "Direct" && !isLoading
            )
        }

        // Bottom section: Message and Donate button
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Enter Message", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    if (isLoading) return@Button
                    isLoading = true
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            // First fetch user data to get the profile image
                            DonationRepository.fetchUser(userId) { userData ->
                                val userPhotoUrl = userData?.photo ?: ""
                                val donation = Donation(
                                    userId = userId,
                                    userName = user?.displayName ?: "",
                                    userEmail = user?.email ?: "",
                                    userPhoto = userPhotoUrl,
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    method = selectedOption,
                                    message = message,
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                )
                                DonationRepository.addDonation(donation,
                                    onSuccess = {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Donation added",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("home") {
                                            popUpTo("addDonation") { inclusive = true }
                                        }
                                    },
                                    onFailure = {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Failed: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("+ Donate", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentOptionRow(
    option: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) }
            )
            Text(option, fontSize = 18.sp)
        }

        // Amount increment/decrement controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val newAmount = (amount.toIntOrNull() ?: 0) - 1
                    if (newAmount >= 0) onAmountChange(newAmount.toString())
                },
                enabled = isEnabled
            ) {
                Icon(painter = painterResource(R.drawable.remove), contentDescription = "Decrease")
            }

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) onAmountChange(it)
                },
                modifier = Modifier.width(80.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                enabled = isEnabled,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            IconButton(
                onClick = {
                    val newAmount = (amount.toIntOrNull() ?: 0) + 1
                    onAmountChange(newAmount.toString())
                },
                enabled = isEnabled
            ) {
                Icon(painter = painterResource(R.drawable.add), contentDescription = "Increase")
            }
        }
    }
}
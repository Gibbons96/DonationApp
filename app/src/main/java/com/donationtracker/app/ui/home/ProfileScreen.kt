package com.donationtracker.app.ui.home

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.donationtracker.app.R
import com.donationtracker.app.util.ProfileImageDataStore
import com.donationtracker.app.repository.DonationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.content.Context
import androidx.compose.ui.layout.ContentScale
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(innerPadding: PaddingValues, navController: NavController? = null) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Loading state for profile image update
    var isUpdatingProfileImage by remember { mutableStateOf(false) }

    // Observe the profile image URI from DataStore (user-specific)
    val imageUri by ProfileImageDataStore.getProfileImageUri(context, user?.uid ?: "").collectAsState(initial = null)

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { originalUri ->
            isUpdatingProfileImage = true
            scope.launch {
                try {
                    // Copy image to internal storage
                    val localUri = copyImageToInternalStorage(context, originalUri)
                    localUri?.let { localUriString ->
                        val localUriString = localUriString.toString()
                        // Save to local DataStore
                        ProfileImageDataStore.setProfileImageUri(context, user?.uid ?: "", localUriString)
                        
                        // Save to Firebase database
                        user?.uid?.let { userId ->
                            DonationRepository.updateUserProfileImage(
                                userId = userId,
                                imageUrl = localUriString,
                                onSuccess = {
                                    Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                                    isUpdatingProfileImage = false
                                },
                                onFailure = { exception ->
                                    Toast.makeText(context, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    isUpdatingProfileImage = false
                                }
                            )
                        } ?: run {
                            Toast.makeText(context, "Failed to update profile image: User not found", Toast.LENGTH_SHORT).show()
                            isUpdatingProfileImage = false
                        }
                    } ?: run {
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                        isUpdatingProfileImage = false
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error updating profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                    isUpdatingProfileImage = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Account Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFECECEC)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                val painter = rememberAsyncImagePainter(
                    model = imageUri,
                    error = painterResource(R.drawable.app_logo)
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(120.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // User Name
        Text(
            text = user?.displayName ?: "No Name",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        // User Email
        Text(
            text = user?.email ?: "No Email",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Change Profile Photo Button
        Button(
            onClick = {
                if (!isUpdatingProfileImage) {
                    launcher.launch("image/*")
                }
            },
            enabled = !isUpdatingProfileImage,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (isUpdatingProfileImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = androidx.compose.ui.graphics.Color.White
                )
            } else {
                Text("Change Profile Photo")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Logout Button
        OutlinedButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                navController?.navigate("auth") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Logout")
        }
    }
}

private fun copyImageToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream: OutputStream = file.outputStream()

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file) // Return the URI of the saved file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
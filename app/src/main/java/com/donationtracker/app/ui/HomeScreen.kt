package com.donationtracker.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.donationtracker.app.ui.home.MapScreen
import com.donationtracker.app.ui.home.ProfileScreen
import com.donationtracker.app.ui.home.ReportScreen
import com.donationtracker.app.ui.home.SearchScreen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val bottomTab = remember { mutableStateOf(0) }
    val switch = remember { mutableStateOf(false) }
    val user = FirebaseAuth.getInstance().currentUser

    // Permission
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (bottomTab.value) {
                                0 -> "Map"
                                1 -> "Report"
                                2 -> "Profile"
                                3 -> "Search"
                                else -> "Map"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        user?.let {
                            Text(
                                "${it.displayName ?: "No Name"} (${it.email})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.LightGray
                            )
                        }
                    }
                },
//                navigationIcon = {
//                    IconButton(onClick = { /* open drawer or back */ }) {
//                        Icon(Icons.Default.Menu, contentDescription = "Menu")
//                    }
//                },
                actions = {
                    Switch(
                        checked = switch.value,
                        onCheckedChange = { switch.value = !switch.value }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00449D),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addDonation")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Donation")
            }
        },
        bottomBar = { BottomNavigationBar(bottomTab) }
    ) { innerPadding ->
        when (bottomTab.value) {
            0 -> MapScreen(innerPadding, !switch.value)
            1 -> ReportScreen(innerPadding, !switch.value, navController)
            2 -> ProfileScreen(innerPadding,navController)
            3 -> SearchScreen(innerPadding, !switch.value,navController)
        }

    }
}

@Composable
fun BottomNavigationBar(bottomTab: MutableState<Int>) {
    NavigationBar {
        NavigationBarItem(
            selected = bottomTab.value == 0,
            onClick = { bottomTab.value = 0 },
            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
            label = { Text("Map") }
        )
        NavigationBarItem(
            selected = bottomTab.value == 1,
            onClick = { bottomTab.value = 1 },
            icon = { Icon(Icons.Default.List, contentDescription = "Report") },
            label = { Text("Report") }
        )
        NavigationBarItem(
            selected = bottomTab.value == 2,
            onClick = { bottomTab.value = 2 },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
        NavigationBarItem(
            selected = bottomTab.value == 3,
            onClick = { bottomTab.value = 3 },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
    }
}

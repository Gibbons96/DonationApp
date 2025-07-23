package com.donationtracker.app.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.app.ActivityCompat
import com.donationtracker.app.ui.AuthScreen
import com.donationtracker.app.ui.DonateScreen
import com.donationtracker.app.ui.HomeScreen
import com.donationtracker.app.ui.SplashScreen
import com.donationtracker.app.ui.home.DonationDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request location permission on app startup
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        
        setContent {
            DonationTrackerApp()
        }
    }
}

@Composable
fun DonationTrackerApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("auth") { AuthScreen (navController) }
        composable("home") { HomeScreen(navController) }
        composable("addDonation") { DonateScreen(navController) }
        composable("donationDetail/{donationId}") { backStackEntry ->
            val donationId = backStackEntry.arguments?.getString("donationId") ?: ""
            DonationDetailScreen(donationId = donationId, navController = navController)
        }
    }
}
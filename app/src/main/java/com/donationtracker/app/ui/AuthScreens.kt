package com.donationtracker.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun AuthScreen(navController: NavHostController) {
    var isLogin by remember { mutableStateOf(true) }
    Surface(color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLogin) {
                LoginScreen(onSwitch = { isLogin = false },navController)
            } else {
                RegisterScreen(onSwitch = { isLogin = true },navController)
            }
        }
    }
}
@Composable
fun NavigationBar(selectedTab: Int, onTabSelected: () -> Unit) {
    NavigationBar(containerColor = Color(0xFF2196F3)) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { if (selectedTab != 0) onTabSelected() },
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Filled.Lock,
                    contentDescription = null
                )
            },
            label = { Text("Login") },
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { if (selectedTab != 1) onTabSelected() },
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Filled.Person,
                    contentDescription = null
                )
            },
            label = { Text("Register") },
            alwaysShowLabel = true
        )
    }
}
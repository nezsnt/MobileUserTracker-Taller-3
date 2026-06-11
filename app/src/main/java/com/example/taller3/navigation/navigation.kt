package com.example.taller3.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller3.screens.Login
import com.example.taller3.screens.Register
import com.example.taller3.screens.Home
import com.example.taller3.screens.MapTracker
import com.example.taller3.screens.Userlist
import com.google.firebase.auth.FirebaseUser


enum class AppScreens{
    authentication,
    register,
    userList,
    tracking,
    home

}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation(currentUser: FirebaseUser?, trackUserId: String?){
    val navController = rememberNavController()
    val startDestination = when {
        currentUser == null -> AppScreens.authentication.name
        trackUserId != null -> "${AppScreens.tracking.name}/$trackUserId"
        else -> AppScreens.home.name
    }
    NavHost(navController = navController, startDestination = startDestination){
        composable(route = AppScreens.authentication.name){
            Login(navController)
        }
        composable(route = AppScreens.register.name){
            Register(navController)
        }
        composable(route = AppScreens.home.name){
            Home(navController)
        }
        composable(route = AppScreens.userList.name){
            Userlist(navController)
        }
        composable(route = "${AppScreens.tracking.name}/{id}") {
            backStackEntry ->
            val userId = backStackEntry.arguments?.getString("id") ?: ""
            MapTracker(navController, userId)
        }
    }
}
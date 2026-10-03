package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.local.AppDatabase
import com.example.data.repository.DieselFlowRepository
import com.example.ui.MainScreen
import com.example.ui.theme.DieselFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = DieselFlowRepository(
            dailyLogDao = database.dailyLogDao(),
            fleetAssetDao = database.fleetAssetDao(),
            userDao = database.userDao()
        )

        setContent {
            DieselFlowTheme {
                MainScreen(repository = repository)
            }
        }
    }
}

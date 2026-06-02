package com.example.cashflow

import android.os.Bundle
import com.example.cashflow.ui.main.MainScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cashflow.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavGraph()
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TradingRepository
import com.example.ui.TradingViewModel
import com.example.ui.TradingViewModelFactory
import com.example.ui.screens.AnalystScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TradingRepository(database.tradingDao())
        val factory = TradingViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[TradingViewModel::class.java]

        setContent {
            MyApplicationTheme(darkTheme = true) { // Force beautiful high-tech dark theme
                var currentTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF111622),
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                label = { Text("Terminal") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "HFT Terminal"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00E5FF),
                                    selectedTextColor = Color(0xFF00E5FF),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFF1B2333)
                                ),
                                modifier = Modifier.testTag("nav_item_terminal")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                label = { Text("Portefeuille") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Portefeuille Ledger"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00E5FF),
                                    selectedTextColor = Color(0xFF00E5FF),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFF1B2333)
                                ),
                                modifier = Modifier.testTag("nav_item_portfolio")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                label = { Text("Analyste IA") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Analyste IA"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00E5FF),
                                    selectedTextColor = Color(0xFF00E5FF),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFF1B2333)
                                ),
                                modifier = Modifier.testTag("nav_item_analyst")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> TerminalScreen(viewModel = viewModel)
                            1 -> LedgerScreen(viewModel = viewModel)
                            2 -> AnalystScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

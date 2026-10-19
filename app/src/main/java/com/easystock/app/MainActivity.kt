package com.easystock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.easystock.app.data.local.EasyStockDatabase
import com.easystock.app.data.repository.EasyStockRepository
import com.easystock.app.theme.EasyStockTheme
import com.easystock.app.ui.MainApp
import com.easystock.app.ui.viewmodel.EasyStockViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EasyStockViewModel by viewModels {
        val database = EasyStockDatabase.getDatabase(applicationContext)
        val repository = EasyStockRepository(database)
        EasyStockViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}

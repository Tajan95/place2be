package de.place2be

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.place2be.app.Place2BeApp
import de.place2be.ui.theme.Place2beTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Place2beTheme {
                Place2BeApp()
            }
        }
    }
}

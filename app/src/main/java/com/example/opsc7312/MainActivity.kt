package com.example.opsc7312

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.opsc7312.ui.theme.OPSC7312Theme

class MainActivity : ComponentActivity() {

    private lateinit var btnMainLogin: Button
    private lateinit var btnMainSignUp: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnMainLogin = findViewById(R.id.btnMainLogin)
        btnMainSignUp = findViewById(R.id.btnMainSignUp)

        btnMainLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnMainSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }


    }
}


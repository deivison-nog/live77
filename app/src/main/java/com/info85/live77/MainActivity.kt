package com.info85.live77

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.info85.live77.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

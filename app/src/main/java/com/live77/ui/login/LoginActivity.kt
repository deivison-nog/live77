package com.live77.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.live77.R
import com.live77.databinding.ActivityLoginBinding
import com.live77.ui.channels.ChannelListActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()
            clearErrors()
            viewModel.login(login, password)
        }

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                LoginResult.Success -> {
                    startActivity(Intent(this, ChannelListActivity::class.java))
                    finish()
                }
                LoginResult.InvalidFormat -> {
                    binding.tilLogin.error = getString(R.string.error_invalid_format)
                    binding.tilPassword.error = getString(R.string.error_invalid_format)
                }
                LoginResult.InvalidCredentials -> {
                    binding.tvError.text = getString(R.string.error_invalid_credentials)
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun clearErrors() {
        binding.tilLogin.error = null
        binding.tilPassword.error = null
        binding.tvError.visibility = View.GONE
    }
}

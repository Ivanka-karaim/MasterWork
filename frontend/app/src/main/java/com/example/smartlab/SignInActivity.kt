package com.example.smartlab

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.smartlab.databinding.ActivitySignInBinding
import android.content.Intent
import com.example.smartlab.addResources.ErrorHandler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.api.UserApi
import com.example.smartlab.service.UserService



class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this@SignInActivity, RegistrationActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
        binding.btnLogin.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val errors = UserService.signIn(
                    userApi,
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString(),
                    this@SignInActivity
                )
                if (errors.isEmpty()) {
                    val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else if (errors == "unknownError") {
                    ErrorHandler.generalError(this@SignInActivity)
                    binding.etEmail.text.clear()
                    binding.etPassword.text.clear()
                } else {
                    binding.error.text = errors
                }
            }
        }
    }
    fun togglePasswordVisibility(view: View) {
        val editText = findViewById<EditText>(R.id.etPassword)
        val currentInputType = editText.inputType
        println(currentInputType)
        if (currentInputType ==144) {
            binding.hidden.setImageResource(R.drawable.ic_eye_closed)
            editText.inputType = 129
        } else {
            binding.hidden.setImageResource(R.drawable.ic_eye_open)
            editText.inputType = 144
        }
        editText.setSelection(editText.text.length)
    }


}


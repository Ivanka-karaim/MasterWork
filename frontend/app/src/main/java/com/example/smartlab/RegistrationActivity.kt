package com.example.smartlab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.api.UserApi
import com.example.smartlab.databinding.ActivityRegistrationBinding
import com.example.smartlab.databinding.ActivitySignInBinding
import com.example.smartlab.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistrationActivity: AppCompatActivity()  {
    lateinit var binding: ActivityRegistrationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, SignInActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
        binding.btnRegister.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val errors = UserService.signUp(
                    userApi,
                    binding.etFullName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString(),
                    binding.etConfirmPassword.text.toString(),
                    this@RegistrationActivity
                )
                if (errors.isEmpty()) {
                    val intent = Intent(this@RegistrationActivity, SignInActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else if (errors == "unknownError") {
                    ErrorHandler.generalError(this@RegistrationActivity)
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
    fun togglePasswordVisibility2(view: View) {
        val editText = findViewById<EditText>(R.id.etConfirmPassword)
        val currentInputType = editText.inputType
        println(currentInputType)
        if (currentInputType ==144) {
            binding.hidden2.setImageResource(R.drawable.ic_eye_closed)
            editText.inputType = 129
        } else {
            binding.hidden2.setImageResource(R.drawable.ic_eye_open)
            editText.inputType = 144
        }
        editText.setSelection(editText.text.length)
    }
}
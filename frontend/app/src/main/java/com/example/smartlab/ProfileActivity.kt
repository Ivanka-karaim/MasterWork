package com.example.smartlab

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.UserApi
import com.example.smartlab.service.GlobalNotificationManager
import com.example.smartlab.databinding.ActivityHomeBinding
import com.example.smartlab.databinding.ActivityProfileBinding
import com.example.smartlab.databinding.ActivityRegistrationBinding
import com.example.smartlab.model.EditProfileModel
import com.example.smartlab.model.SignUpModel
import com.example.smartlab.service.UserService
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.editButton.setOnClickListener {
            showEditBottomSheet()
        }

        binding.logoutButton.setOnClickListener {
            logout()

        }


    }

    private fun showEditBottomSheet() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.bottom_sheet_edit_profile)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialog.show()

        val view = layoutInflater.inflate(R.layout.bottom_sheet_edit_profile, null)
        dialog.setContentView(view)

        view.findViewById<EditText>(R.id.editFullName).setText(binding.userName.text)
        view.findViewById<EditText>(R.id.editEmail).setText(binding.userEmail.text)

        view.findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            val newName = view.findViewById<EditText>(R.id.editFullName).text.toString()
            val newEmail = view.findViewById<EditText>(R.id.editEmail).text.toString()
            val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
            val accessToken = SharedPreferencesFactory(this).getSharedPreferences("TOKEN")!!

            lifecycleScope.launch {
                val errors = UserService.editProfile(
                    userApi, newName, newEmail,
                    "Bearer $accessToken"
                )
                if (errors.isEmpty()) {
                    loadProfile()
                    dialog.dismiss()
                } else if (errors == "unknownError") {
                    ErrorHandler.generalError(this@ProfileActivity)
                } else {
                    view.findViewById<TextView>(R.id.error).text = errors
                }
            }
        }
        view.findViewById<TextView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun logout() {
        val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
        val token = SharedPreferencesFactory(this@ProfileActivity).getSharedPreferences("TOKEN")!!
        lifecycleScope.launch {
            val response = userApi.logout("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                ErrorHandler.unauthorizedUser(this@ProfileActivity)
            } else if (response.code() == 401) {
                ErrorHandler.unauthorizedUser(this@ProfileActivity)
            } else {
                Toast.makeText(
                    this@ProfileActivity,
                    "Помилка завантаження профілю",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


    private fun loadProfile() {
        val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
        val token = SharedPreferencesFactory(this@ProfileActivity).getSharedPreferences("TOKEN")!!
        lifecycleScope.launch {
            try {
                val response = userApi.profile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data
                    binding.userName.text = user.fullName
                    binding.userEmail.text = user.email
                    binding.userRole.text = user.role
                } else if (response.code() == 401) {
                    ErrorHandler.unauthorizedUser(this@ProfileActivity)
                } else Toast.makeText(
                    this@ProfileActivity,
                    "Помилка завантаження профілю",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}
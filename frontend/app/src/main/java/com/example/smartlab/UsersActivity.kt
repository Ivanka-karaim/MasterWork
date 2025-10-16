package com.example.smartlab

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartlab.adapter.UserAdapter
import com.example.smartlab.addResources.ErrorHandler
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.UserApi
import com.example.smartlab.databinding.ActivityUsersBinding
import com.example.smartlab.model.SignUpModel
import com.example.smartlab.model.UserProfile
import com.example.smartlab.service.UserService
import kotlinx.coroutines.launch


class UsersActivity: BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityUsersBinding
    private lateinit var userApi: UserApi
    private var allUsers: List<UserProfile> = emptyList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNav()


        binding.usersRecyclerView.layoutManager = LinearLayoutManager(this)


        userApi = RetrofitClient.getInstance().create(UserApi::class.java)
        loadData()

        navigation()
    }
    private fun loadData(){
        lifecycleScope.launch {
            val token = SharedPreferencesFactory(this@UsersActivity)
                .getSharedPreferences("TOKEN") ?: return@launch
            val response =
                userApi.getAllUsers(token ="Bearer $token")
            if (response.isSuccessful) {
                allUsers = response.body()?.data ?: emptyList()
                updateUsersList()
            } else if (response.code() == 401) {
                ErrorHandler.unauthorizedUser(this@UsersActivity)
            }
        }
    }

    private fun updateUsersList() {
//        val filtered = if (binding.isStrict.isChecked) {
//            allRules.filter { it.strict }
//        } else {
//            allRules.filter { !it.strict }
//        }

        adapter = UserAdapter(
            users = allUsers.toMutableList(),
            onEditClick = { user -> editUser(user) },
            onDeleteClick = { user -> deleteUser(user) }
        )
        binding.usersRecyclerView.adapter = adapter
    }

    private fun showEditBottomSheet(user: UserProfile) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_profile)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialog.show()

        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(view)

        view.findViewById<EditText>(R.id.editFullName1).setText(user.fullName)
        view.findViewById<EditText>(R.id.editEmail1).setText(user.email)

        val spinner = view.findViewById<Spinner>(R.id.typeRole1)
        val roles = listOf("ADMIN", "TEACHER", "STUDENT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Встановлення ролі користувача
        val userRole = user.role.uppercase()
        val selectedIndex = roles.indexOfFirst { it.equals(userRole, ignoreCase = true) }
        if (selectedIndex != -1) spinner.setSelection(selectedIndex)

        view.findViewById<Button>(R.id.saveProfileButton1).setOnClickListener {
            val newName = view.findViewById<EditText>(R.id.editFullName1).text.toString()
            val newEmail = view.findViewById<EditText>(R.id.editEmail1).text.toString()
            val password = if (view.findViewById<EditText>(R.id.editPassword1).text.toString().isEmpty())
                null
            else
                view.findViewById<EditText>(R.id.editPassword1).text.toString()
            val role = view.findViewById<Spinner>(R.id.typeRole1).selectedItem.toString()
            val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
            val accessToken = SharedPreferencesFactory(this).getSharedPreferences("TOKEN")!!



            lifecycleScope.launch {
                val response = userApi.updateUser("Bearer $accessToken", user.id, SignUpModel(newName, newEmail,password, null, role))
                if (response.isSuccessful) {
                    loadData()
                    dialog.dismiss()
                } else if (response.code() == 401) {
                    ErrorHandler.generalError(this@UsersActivity)
                } else {
                    view.findViewById<TextView>(R.id.error1).text = response.body()?.message.toString()
                }
            }
        }
        view.findViewById<TextView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }



    private fun editUser(user: UserProfile) {
        showEditBottomSheet(user)
    }

    private fun deleteUser(user: UserProfile) {
        val token = SharedPreferencesFactory(this@UsersActivity)
            .getSharedPreferences("TOKEN")
        lifecycleScope.launch {
            val response = userApi.deleteUser("Bearer $token", user.id)
            if(response.isSuccessful){
                adapter.removeUser(user)
            } else if(response.code() == 401){
                ErrorHandler.unauthorizedUser(this@UsersActivity)
            } else {
                ErrorHandler.generalError(this@UsersActivity)
            }
        }

    }


    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
        loadData()
    }

    private fun navigation() {
        binding.topToolbar.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.bottomNav.home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.bottomNav.notification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        binding.bottomNav.edit.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }
    }
}
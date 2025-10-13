package com.example.smartlab.addResources

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.smartlab.SignInActivity
import com.example.smartlab.service.GlobalNotificationManager

object ErrorHandler {
    fun unauthorizedUser(activity: Activity){
        GlobalNotificationManager.logout()
        val context = activity
        SharedPreferencesFactory(context).clearSharedPreferences("TOKEN")

        val intent = Intent(context, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        activity.finish() // Закриваємо поточну активність
    }

    fun generalError(context: Context){
        Toast.makeText(
            context,
            "Уппс! Пішло щось не так",
            Toast.LENGTH_SHORT
        ).show()

    }
}
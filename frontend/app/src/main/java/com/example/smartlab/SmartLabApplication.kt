package com.example.smartlab

import android.app.Application
import android.content.Context
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.service.GlobalNotificationManager

class SmartLabApplication : Application() {
    
    companion object {
        lateinit var instance: SmartLabApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Ініціалізуємо глобальний менеджер сповіщень
        GlobalNotificationManager.initialize(this)
        
        // Автоматично підключаємо WebSocket якщо користувач залогінений
        val userId = SharedPreferencesFactory(this).getSharedPreferences("USER_ID")
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN")
        
        if (!userId.isNullOrEmpty() && !token.isNullOrEmpty()) {
            GlobalNotificationManager.connect(userId)
        }
    }
    
    fun getContext(): Context = this
}


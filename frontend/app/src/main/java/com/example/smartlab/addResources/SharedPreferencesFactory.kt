package com.example.smartlab.addResources

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesFactory(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("MY_APP", Context.MODE_PRIVATE)

    fun saveSharedPreferences(title: String, value: String){
        sharedPreferences.edit { putString(title, value) }
    }

    fun getSharedPreferences(title: String): String? {
        return sharedPreferences.getString(title, null)
    }

    fun clearSharedPreferences(title: String){
        sharedPreferences.edit { remove(title) }
    }
}
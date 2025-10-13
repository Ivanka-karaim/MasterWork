package com.example.smartlab

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.UserApi
import kotlinx.coroutines.launch

class MainActivity: AppCompatActivity()  {
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val token = SharedPreferencesFactory(this).getSharedPreferences("TOKEN")?: ""
        progressBar = findViewById(R.id.progressBar)
        val duration = 3000L
        val interval = 30L
        val maxProgress = progressBar.max
        val steps = duration / interval
        val stepIncrement = maxProgress / steps.toFloat()

        var progress = 0f
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                progress += stepIncrement
                if (progress >= maxProgress) {
                    progressBar.progress = maxProgress.toInt()
                    if (token.isNotEmpty()) {
                        lifecycleScope.launch {
                            val userApi = RetrofitClient.getInstance().create(UserApi::class.java)
                            val response = userApi.profile("Bearer $token")
                            if (response.isSuccessful && response.body() != null) {
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }else{
                        val intent = Intent(this@MainActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()

                    }
                } else {
                    progressBar.progress = progress.toInt()
                    handler.postDelayed(this, interval)
                }
            }
        }

        handler.post(runnable)
    }


}
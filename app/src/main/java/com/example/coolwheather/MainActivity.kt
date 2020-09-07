package com.example.coolwheather

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.example.coolwheather.WeatherActivity.Companion.SP_WEATHER
import com.example.coolwheather.bean.Weather

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getString(SP_WEATHER, null) != null) {
            val intent = Intent(this, WeatherActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
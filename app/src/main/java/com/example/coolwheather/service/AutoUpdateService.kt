package com.example.coolwheather.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import com.example.coolwheather.WeatherActivity.Companion.SP_BINGPIC
import com.example.coolwheather.WeatherActivity.Companion.SP_WEATHER
import com.example.coolwheather.util.HttpUtil
import com.example.coolwheather.util.Utility
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * @Description:
 * @author: haishan
 * @Date: 2020/9/7
 */
class AutoUpdateService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWeather()
        updateBiyingPic()
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val anHour = 8 * 60 * 60 * 1000
        val triggerAtTime = SystemClock.elapsedRealtime() + anHour
        val i = Intent(this, AutoUpdateService::class.java)
        val pi = PendingIntent.getService(this, 0, i, 0)
        manager.cancel(pi)
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateBiyingPic() {
        val requestBiyingPic = "http://guolin.tech/api/bing_pic"
        HttpUtil.sendOkHttpRequest(requestBiyingPic, object : Callback{
            override fun onResponse(call: Call, response: Response) {
                val biyingPic = response.body?.string()
                val editor = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                editor.putString(SP_BINGPIC, biyingPic)
                editor.apply()
            }

            override fun onFailure(call: Call, e: IOException) {
            }
        })

    }

    private fun updateWeather() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val weatherString = prefs.getString(SP_WEATHER, null)
        if (!weatherString.isNullOrEmpty()) {
            val weather = Utility.handleWeatherResponse(weatherString)
            val weatherId = weather?.basic?.weatherId
            val weatherUrl = "http://guolin.tech./api/weather?cityid=${weatherId}&key=d3d303b7ab314add897857be7f3300e7"
            HttpUtil.sendOkHttpRequest(weatherUrl, object : Callback{
                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body?.string()
                    val weatherResp = Utility.handleWeatherResponse(responseText)
                    if (weatherResp != null && "ok" == weatherResp.status) {
                        val editor = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                        editor.putString(SP_WEATHER, responseText)
                        editor.apply()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                }
            })
        }

    }
}
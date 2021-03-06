package com.example.coolwheather

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.coolwheather.bean.Weather
import com.example.coolwheather.service.AutoUpdateService
import com.example.coolwheather.util.HttpUtil
import com.example.coolwheather.util.Utility
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.aqi.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.now.*
import kotlinx.android.synthetic.main.suggestion.*
import kotlinx.android.synthetic.main.title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * @Description: 天气信息
 * @author: haishan
 * @Date: 2020/9/7
 */
class WeatherActivity : AppCompatActivity() {
    //天气id
    private var mWeatherId: String? = null

    companion object {

        const val WEATHER_ID = "weather_id"
        const val SP_WEATHER = "weather"
        const val SP_BINGPIC = "biying_pic"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }

        setContentView(R.layout.activity_weather)
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary)
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val weatherString = prefs.getString(SP_WEATHER, null)
        if (!weatherString.isNullOrEmpty()) {
            val weather = Utility.handleWeatherResponse(weatherString)
            mWeatherId = weather?.basic?.weatherId
            showWeatherInfo(weather)
        } else {
            mWeatherId = intent.getStringExtra(WEATHER_ID)
            weather_layout.visibility = View.INVISIBLE
            requestWeather(mWeatherId)
        }

        val biyingPic = prefs.getString(SP_BINGPIC, null)
        if (!biyingPic.isNullOrEmpty()) {
            Glide.with(this).load(biyingPic).into(bing_pic_img)
        } else {
            loadBingPic()
        }

        initListener()
    }

    private fun initListener() {
        swipe_refresh.setOnRefreshListener {
            requestWeather(mWeatherId)
        }

        nav_button.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }
    }

    /**
     * 获取背景图
     */
    private fun loadBingPic() {
        val requestBiyingPic = "http://guolin.tech/api/bing_pic"
        HttpUtil.sendOkHttpRequest(requestBiyingPic, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val biyingPic = response.body?.string()
                val editor =
                    PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                editor.putString(SP_BINGPIC, biyingPic)
                editor.apply()
                runOnUiThread {
                    Glide.with(applicationContext).load(biyingPic).into(bing_pic_img)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    bing_pic_img.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                }
            }
        })
    }

    /**
     * 请求天气数据
     */
    fun requestWeather(weatherId: String?) {
        val weatherUrl =
            "http://guolin.tech./api/weather?cityid=${weatherId}&key=d3d303b7ab314add897857be7f3300e7"
        HttpUtil.sendOkHttpRequest(weatherUrl, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val reponseText = response.body?.string()
                val weather = Utility.handleWeatherResponse(reponseText)
                runOnUiThread {
                    if (weather != null && "ok" == weather.status) {
                        val editor =
                            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                        editor.putString(SP_WEATHER, reponseText)
                        editor.apply()
                        showWeatherInfo(weather)
                    } else {
                        Toast.makeText(applicationContext, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    }
                    swipe_refresh.isRefreshing = false
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    swipe_refresh.isRefreshing = false
                }
            }
        })
    }

    /**
     * 显示天气信息
     */
    @SuppressLint("SetTextI18n")
    private fun showWeatherInfo(weather: Weather?) {
        weather?.apply {
            title_city.text = basic?.cityName
            title_update_time.text = basic?.update?.updateTime?.split(" ")?.get(1) ?: ""
            degree_text.text = "${now?.temperature}°C"
            weather_info_text.text = now?.more?.info
            forecast_layout.removeAllViews()
            daily_forecast?.forEach {
                val view =
                    LayoutInflater.from(applicationContext).inflate(R.layout.forecast_item, forecast_layout, false)
                val dateText = view.findViewById<TextView>(R.id.date_text)
                val infoText = view.findViewById<TextView>(R.id.info_text)
                val maxText = view.findViewById<TextView>(R.id.max_text)
                val minText = view.findViewById<TextView>(R.id.min_text)
                it.apply {
                    dateText.text = date
                    infoText.text = more?.info
                    maxText.text = temperature?.max
                    minText.text = temperature?.min
                }
                forecast_layout.addView(view)
            }
            aqi_text.text = aqi?.city?.aqi
            pm25_text.text = aqi?.city?.pm25

            comfort_text.text = "舒适度：${suggestion?.comfort?.info}"
            car_wash_text.text = "洗车指数：${suggestion?.carWash?.info}"
            sport_text.text = "运动建议：${suggestion?.sport?.info}"
            weather_layout.visibility = View.VISIBLE
            val intent = Intent(applicationContext, AutoUpdateService::class.java)
            startService(intent)
        }
    }
}
package com.example.coolwheather.util

import com.example.coolwheather.db.City
import com.example.coolwheather.db.County
import com.example.coolwheather.db.Province
import org.json.JSONArray
import org.json.JSONException

/**
 * @Description: 解析处理数据工具类
 * @author: haishan
 * @Date: 2020/9/5
 */
class Utility {
    companion object {
        /**
         * 解析和处理服务器返回的省级数据
         */
        fun handleProvinceResponse(response: String?): Boolean {
            if (!response.isNullOrEmpty()) {
                try {
                    val allProvices = JSONArray(response)
                    for (i in 0 until allProvices.length()) {
                        val provinceObject = allProvices.getJSONObject(i)
                        val province = Province()
                        province.provinceName = provinceObject.getString("name")
                        province.provinceCode = provinceObject.getInt("id")
                        province.save()
                    }
                    return true
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return false
        }

        /**
         * 解析和处理服务器返回的市级数据
         */
        fun handleCityResponse(response: String?, provinceId: Int?): Boolean {
            if (!response.isNullOrEmpty()) {
                try {
                    val allCities = JSONArray(response)
                    for (i in 0 until allCities.length()) {
                        val cityObject = allCities.getJSONObject(i)
                        val city = City()
                        city.cityName = cityObject.getString("name")
                        city.cityCode = cityObject.getInt("id")
                        city.provinceId = provinceId
                        city.save()
                    }
                    return true
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return false
        }

        /**
         * 解析和处理服务器返回的县数据
         */
        fun handleCountyResponse(response: String?, cityId: Int?): Boolean {
            if (!response.isNullOrEmpty()) {
                try {
                    val allCounties = JSONArray(response)
                    for (i in 0 until (allCounties.length() - 1)) {
                        val countyObject = allCounties.getJSONObject(i)
                        val county = County()
                        county.countyName = countyObject.getString("name")
                        county.weatherId = countyObject.getString("weather_id")
                        county.cityId = cityId
                        county.save()
                    }
                    return true
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return false
        }
    }
}
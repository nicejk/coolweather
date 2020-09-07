package com.example.coolwheather.bean

import com.google.gson.annotations.SerializedName

/**
 * @Description:
 * @author: haishan
 * @Date: 2020/9/5
 */
data class Weather(
        var status: String,
        var bisic: Basic? = null,
        var aqi: AQI? = null,
        var now: Now? = null,
        var suggestion: Suggestion? = null,
        var forecastList: MutableList<Forecast>? = null
)

data class Basic(
        @SerializedName("city")
        var cityName: String? = null,
        @SerializedName("id")
        var weatherId: String? = null,
        var update: Update? = null
)

data class Update(
        @SerializedName("loc")
        var updateTime: String? = null
)

data class AQI(
        var city: AQICity? = null
)

data class AQICity(var api: String, var pm25: String)

data class Now(
        @SerializedName("tmp")
        var temperature: String? = null,
        @SerializedName("cond")
        var more: NowMore? = null
)

data class NowMore(@SerializedName("txt") var info: String? = null)

data class Suggestion(
        @SerializedName("comf")
        var comfort: Comfort? = null,
        @SerializedName("cw")
        var carWash: CarWash? = null,
        var sport: Sport? = null
)

data class Comfort(@SerializedName("txt") var info: String? = null)

data class CarWash(@SerializedName("txt") var info: String? = null)

data class Sport(@SerializedName("txt") var info: String? = null)

data class Forecast(
        var data: String? = null,
        @SerializedName("tmp")
        var temperature: Temperature? = null,
        @SerializedName("cond")
        var more: More? = null
)

data class Temperature(var max: String? = null, var min: String? = null)

data class More(@SerializedName("txt_d") var info: String? = null)
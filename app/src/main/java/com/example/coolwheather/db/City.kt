package com.example.coolwheather.db

import org.litepal.crud.DataSupport

/**
 * @Description: 市
 * @author: haishan
 * @Date: 2020/9/5
 */
data class City(var id: Int?, var cityName: String?, var cityCode: Int?) : DataSupport()
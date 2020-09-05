package com.example.coolwheather.db

import org.litepal.crud.DataSupport

/**
 * @Description: 城市
 * @author: haishan
 * @Date: 2020/9/5
 */
data class County(
        var id: Int = 0,
        var countyName: String = "",
        var weatherId: String = "",
        var cityId: Int? = 0
) : DataSupport()
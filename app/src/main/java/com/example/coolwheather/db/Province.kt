package com.example.coolwheather.db

import org.litepal.crud.DataSupport

/**
 * @Description:省份
 * @author: haishan
 * @Date: 2020/9/5
 */
data class Province(var id: Int?, var provinceName: String?, var provinceCode: Int?) : DataSupport()
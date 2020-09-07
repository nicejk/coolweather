package com.example.coolwheather

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.coolwheather.db.City
import com.example.coolwheather.db.County
import com.example.coolwheather.db.Province
import com.example.coolwheather.util.HttpUtil
import com.example.coolwheather.util.Utility
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.fragment_choose_area.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.litepal.crud.DataSupport
import java.io.IOException

/**
 * @Description:选择区域
 * @author: haishan
 * @Date: 2020/9/5
 */
class ChooseAreaFragment : Fragment() {
    private lateinit var mListView: ListView
    private var mAdapter: ArrayAdapter<String>? = null
    private var mDataList: MutableList<String>? = ArrayList()

    //省列表
    private var mProvinceList: MutableList<Province>? = null

    //市列表
    private var mCityList: MutableList<City>? = null

    //县列表
    private var mCountyList: MutableList<County>? = null

    //选中的省份
    private var mSelectedProvince: Province? = null

    //选中的城市
    private var mSelectedCity: City? = null

    //当前选中的级别
    private var mCurrentLevel: Int? = 0

    companion object {
        const val LEVEL_PROVINCE = 0
        const val LEVEL_CITY = 1
        const val LEVEL_COUNTY = 2

        const val TYPE_PROVINCE = "province"
        const val TYPE_CITY = "city"
        const val TYPE_COUNTY = "county"

        const val WEATHER_ID = "weather_id"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_choose_area, container, false)
        mListView = view.findViewById(R.id.lv_list)
        mAdapter = mDataList?.let {
            context?.let { it1 ->
                ArrayAdapter(it1, android.R.layout.simple_list_item_1, it)
            }
        }
        mListView.adapter = mAdapter
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mListView.setOnItemClickListener { _, _, position, _ ->
            if (mCurrentLevel == LEVEL_PROVINCE) {
                mSelectedProvince = mProvinceList?.get(position)
                queryCities()
            } else if (mCurrentLevel == LEVEL_CITY) {
                mSelectedCity = mCityList?.get(position)
                queryCounties()
            } else if (mCurrentLevel == LEVEL_COUNTY) {
                val weatherId = mCountyList?.get(position)?.weatherId
                if (activity is MainActivity) {
                    val intent = Intent(activity, WeatherActivity::class.java)
                    intent.putExtra(WEATHER_ID, weatherId)
                    startActivity(intent)
                    activity?.finish()
                } else if (activity is WeatherActivity) {
                    val activity = activity as WeatherActivity
                    activity.apply {
                        drawer_layout.closeDrawers()
                        swipe_refresh.isRefreshing = true
                        requestWeather(weatherId)
                    }
                }
            }
        }

        mIvBack.setOnClickListener {
            if (mCurrentLevel == LEVEL_COUNTY) {
                queryCities()
            } else if (mCurrentLevel == LEVEL_CITY) {
                queryProvinces()
            }
        }

        queryProvinces()
    }

    /**
     * 查询省
     */
    private fun queryProvinces() {
        mTvTitle.text = "中国"
        mIvBack.visibility = View.GONE
        mProvinceList = DataSupport.findAll(Province::class.java)
        if (!mProvinceList.isNullOrEmpty()) {
            mDataList?.clear()
            mProvinceList?.forEach {
                mDataList?.add(it.provinceName)
            }
            mAdapter?.notifyDataSetChanged()
            mListView.setSelection(0)
            mCurrentLevel = LEVEL_PROVINCE
        } else {
            val address = "http://guolin.tech/api/china"
            queryFromServer(address, TYPE_PROVINCE)
        }
    }

    /**
     * 查询市
     */
    private fun queryCities() {
        mTvTitle.text = mSelectedProvince?.provinceName
        mIvBack.visibility = View.VISIBLE
        mCityList = DataSupport.where("provinceid = ?", mSelectedProvince?.id.toString())
                .find(City::class.java)
        if (!mCityList.isNullOrEmpty()) {
            mDataList?.clear()
            mCityList?.forEach {
                mDataList?.add(it.cityName)
            }
            mAdapter?.notifyDataSetChanged()
            mListView.setSelection(0)
            mCurrentLevel = LEVEL_CITY
        } else {
            val provinceCode = mSelectedProvince?.provinceCode
            val address = "http://guolin.tech/api/china/${provinceCode}"
            queryFromServer(address, TYPE_CITY)
        }
    }

    /**
     * 查询县
     */
    private fun queryCounties() {
        mTvTitle.text = mSelectedCity?.cityName
        mIvBack.visibility = View.VISIBLE
        mCountyList =
            DataSupport.where("cityid = ?", mSelectedCity?.id.toString()).find(County::class.java)
        if (!mCountyList.isNullOrEmpty()) {
            mDataList?.clear()
            mCountyList?.forEach {
                mDataList?.add(it.countyName)
            }
            mAdapter?.notifyDataSetChanged()
            mListView.setSelection(0)
            mCurrentLevel = LEVEL_COUNTY
        } else {
            val provinceCode = mSelectedProvince?.provinceCode
            val cityCode = mSelectedCity?.cityCode
            val address = "http://guolin.tech/api/china/${provinceCode}/${cityCode}"
            queryFromServer(address, TYPE_COUNTY)
        }
    }

    /**
     * 从服务器获取数据
     */
    private fun queryFromServer(address: String, type: String) {
        showProgress()
        HttpUtil.sendOkHttpRequest(address, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseText: String? = response.body?.string()
                var result = false
                when (type) {
                    TYPE_PROVINCE -> {
                        result = Utility.handleProvinceResponse(responseText)
                    }
                    TYPE_CITY -> {
                        result = Utility.handleCityResponse(responseText, mSelectedProvince?.id)
                    }
                    TYPE_COUNTY -> {
                        result = Utility.handleCountyResponse(responseText, mSelectedCity?.id)
                    }
                }

                if (result) {
                    activity?.runOnUiThread {
                        hideProgress()
                        when (type) {
                            TYPE_PROVINCE -> {
                                queryProvinces()
                            }
                            TYPE_CITY -> {
                                queryCities()
                            }
                            TYPE_COUNTY -> {
                                queryCounties()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    hideProgress()
                    Toast.makeText(context, "数据加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showProgress() {
        mProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mProgressBar.visibility = View.GONE
    }
}
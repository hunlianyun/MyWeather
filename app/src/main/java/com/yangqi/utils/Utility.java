package com.yangqi.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yangqi.db.City;
import com.yangqi.db.County;
import com.yangqi.db.Province;
import com.yangqi.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析省市县及天气查询 json 数据
 */

public class Utility {

    /**
     * 处理返回的省的 json 数据
     *
     * @param response json 数据
     * @return 成功处理返回 true，负责返回 false
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinces = new JSONArray(response);
                for (int i = 0; i < provinces.length(); i++) {
                    JSONObject jsonObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理返回的市的 json 数据
     *
     * @param response   json 数据
     * @param provinceId 市所属省
     * @return 成功处理返回 true，负责返回 false
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cities = new JSONArray(response);
                for (int i = 0; i < cities.length(); i++) {
                    JSONObject jsonObject = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理返回的省的 json 数据
     *
     * @param response json 数据
     * @param cityId   县所属市
     * @return 成功处理返回 true，负责返回 false
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray couties = new JSONArray(response);
                for (int i = 0; i < couties.length(); i++) {
                    JSONObject jsonObject = couties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 利用 Gson 将数据解析成 Weather 实体类
     * json 数据格式大致如下:
     * <pre>
     *     {
     *         "HeWeather":
     *         [
     *             {
     *                 "status":"ok",
     *                 "basic":{},
     *                 "aqi":{},
     *                 "now":{},
     *                 "suggestion":{},
     *                 "daily_forecast":[]
     *             }
     *         ]
     *     }
     * </pre>
     * @param response json 数据
     * @return Weather 实体类
     */
    public static Weather handleWeatherResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
                String weatherContent = jsonArray.getJSONObject(0).toString();
                return new Gson().fromJson(weatherContent, Weather.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

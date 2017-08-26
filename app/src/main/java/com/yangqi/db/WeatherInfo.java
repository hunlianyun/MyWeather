package com.yangqi.db;

import org.litepal.crud.DataSupport;

/**
 * 天气信息数据库
 */

public class WeatherInfo extends DataSupport{
    private String weatherId;
    private String content;
    private String cityName;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public WeatherInfo(){

    }

    public WeatherInfo(String weatherId, String content, String cityName) {
        this.weatherId = weatherId;
        this.content = content;
        this.cityName = cityName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

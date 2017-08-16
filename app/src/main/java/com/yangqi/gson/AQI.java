package com.yangqi.gson;

/**
 * 空气质量，返回 json 数据如下：
 * <pre>
 *     "aqi":{
 *         "city":{
 *             "aqi":"20",
 *             "pm25":"11"
 *         }
 *     }
 * </pre>
 */

public class AQI {

    public AQICity city;

    public class AQICity {

        public String aqi;

        public String pm25;

    }

}

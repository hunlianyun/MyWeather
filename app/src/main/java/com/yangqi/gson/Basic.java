package com.yangqi.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 基本信息，包括城市，天气 id，以及更新时间，返回的 json 字符串如下：
 * <pre>
 *     "basic":{
 *         "city":"江夏",
 *         "cnty":"中国",
 *         "id":"CN101200105",
 *         "lat":"30.34904480",
 *         "lon":"114.31395721",
 *         "update":{"loc":"2017-08-12 09:52","utc":"2017-08-12 01:52"}
 *     }
 * </pre>
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }
}

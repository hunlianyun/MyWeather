package com.yangqi.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 未来天气，json 数据大致如下：
 * <pre>
 *     "daily_forecast":[
 *         {
 *             "date":"2017-08-12",
 *             "cond":{"txt_d":"暴雨"},
 *             "tmp":{"max":"27","min":"25"}
 *         },
 *         {
 *
 *         },
 *         ...
 *     ]
 * </pre>
 */

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}

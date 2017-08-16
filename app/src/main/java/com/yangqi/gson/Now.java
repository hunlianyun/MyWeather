package com.yangqi.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 当前天气状况，json 数据大致如下：
 * <pre>
 *     "now":{
 *         "tmp":"26",
 *         "cond":{
 *             "txt":"小雨"
 *         }
 *     }
 * </pre>
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;
    }

}

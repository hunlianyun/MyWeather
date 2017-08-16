package com.yangqi.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 建议，json 数据大致如下：
 * <pre>
 *     "suggestion":{
 *         "comf":{"txt":"白天有雨，从而使空气湿度加大，会使人们感觉有点儿闷热，但早晚的天气很凉爽、舒适。"},
 *         "cw":{"txt":"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。"},
 *         "sport":{"txt":"有较强降水，建议您选择在室内进行健身休闲运动。"}
 *     }
 * </pre>
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort {
        public String txt;
    }
    public class CarWash {
        public String txt;
    }
    public class Sport {
        public String txt;
    }
}

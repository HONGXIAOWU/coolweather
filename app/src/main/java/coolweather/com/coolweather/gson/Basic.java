package coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DELL on 2018/4/5.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Updata updata;

    public class Updata{
        @SerializedName("loc")
        public  String updataTime;
    }
}

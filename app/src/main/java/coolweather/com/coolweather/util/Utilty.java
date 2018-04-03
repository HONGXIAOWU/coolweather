package coolweather.com.coolweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import coolweather.com.coolweather.db.City;
import coolweather.com.coolweather.db.County;
import coolweather.com.coolweather.db.Province;

/**
 * Created by DELL on 2018/4/3.
 */
//解析省，市，区数据工具类

public class Utilty {


    //解析服务器返回的省级数据
    public  static  boolean handleProvinceResponse(String response){

        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provincesObject = allProvinces.getJSONObject(i);
                    Province province  = new Province();
                    province.setProvinceName(provincesObject.getString("name"));
                    province.setProvinceCode(provincesObject.getInt("id"));
                    province.save();//存储到数据库中
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析服务器返回的市级数据
    public static boolean handlerCityResponse(String response ,int provinceId){

        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0;i<allCities.length();i++){
                    JSONObject cityResponse = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityResponse.getInt("id"));
                    city.setCityName(cityResponse.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    //解析服务器返回来得区级数据
    public  static  boolean handleCountry(String response,int cityId){

        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCountry = new JSONArray(response);
                for (int i = 0;i<allCountry.length();i++){
                    JSONObject countryResponse = allCountry.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countryResponse.getString("name"));
                    county.setWeatherId(countryResponse.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();//保存在数据库中
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}

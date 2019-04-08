package wlj.com.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import wlj.com.coolweather.db.City;
import wlj.com.coolweather.db.County;
import wlj.com.coolweather.db.Province;


public class JsonUtil {
    /**
     * 解析省级数据并组装成实体类型存进数据库
     *
     * @param response
     * @return
     */
    public static boolean parseProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //数据解析成数组
                JSONArray provinceList = new JSONArray(response);
                for (int i = 0; i < provinceList.length(); i++) {
                    JSONObject provinceObj = provinceList.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObj.getString("name"));
                    province.setProvinceCode(provinceObj.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean parseCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cityList = new JSONArray(response);
                for (int i = 0; i < cityList.length(); i++) {
                    JSONObject cityObj = cityList.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObj.getString("name"));
                    city.setCityCode(cityObj.getInt("id"));
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

    public static boolean parseCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray countyList = new JSONArray(response);
                for (int i = 0; i < countyList.length(); i++) {
                    JSONObject countyObj = countyList.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObj.getString("name"));
                    county.setWeatherId(countyObj.getString("weather_id"));
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
}

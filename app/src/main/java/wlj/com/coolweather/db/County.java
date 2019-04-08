package wlj.com.coolweather.db;

import org.litepal.crud.DataSupport;
/**
 * 县/区的实体类
 */
public class County extends DataSupport {
    private int id;
    private String countyName;
    //当前区/县对应的天气id
    private int weatherId;
    //当前区/县所属的城市id
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}

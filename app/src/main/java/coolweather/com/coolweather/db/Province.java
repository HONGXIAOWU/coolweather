package coolweather.com.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by DELL on 2018/4/2.F:\CoolWeather\app\src\main\java\coolweather\com\coolweather\db\Province.java
 * F:\CoolWeather\app\src\main\java\coolweather\com\coolweather\db\Province.java
 * coolweather.com.coolweather.db.Province
 *coolweather.com.coolweather.db.Province
 */

public class Province extends DataSupport {

    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + id +
                ", provinceName='" + provinceName + '\'' +
                ", provinceCode=" + provinceCode +
                '}';
    }
}

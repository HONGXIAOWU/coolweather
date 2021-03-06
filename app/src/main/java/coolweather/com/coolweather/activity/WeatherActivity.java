package coolweather.com.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import coolweather.com.coolweather.R;
import coolweather.com.coolweather.gson.Forecast;
import coolweather.com.coolweather.gson.Weather;
import coolweather.com.coolweather.service.UpdateWeatherService;
import coolweather.com.coolweather.util.HttpUtil;
import coolweather.com.coolweather.util.Utilty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG="WeatherActivity";
    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;
    private ScrollView weahterLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView  degreeText;
    private TextView weahterInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private LinearLayout forecastLayout;
    private ImageView bingPicImage;
    private Button mNavButton;
    public DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        setContentView(R.layout.activity_weather);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        weahterLayout = (ScrollView) findViewById(R.id.weather_layout);
        forecastLayout =(LinearLayout)findViewById(R.id.forecast_layout);
        titleCity =(TextView) findViewById(R.id.title_city);
        titleUpdateTime =(TextView) findViewById(R.id.title_update_time);
        degreeText =(TextView) findViewById(R.id.degree_text);
        weahterInfoText =(TextView) findViewById(R.id.weahter_info_text);
        aqiText =(TextView) findViewById(R.id.aqi_text);
        pm25Text =(TextView) findViewById(R.id.pm25_text);
        comfortText =(TextView) findViewById(R.id.comfort_text);
        carWashText =(TextView) findViewById(R.id.car_wash_text);
        sportText =(TextView) findViewById(R.id.sport_text);
        bingPicImage = (ImageView)findViewById(R.id.bing_pic_img);
        mNavButton = (Button)findViewById(R.id.nav_button);
        drawerLayout =(DrawerLayout)findViewById(R.id.drawer_layout);
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = sharedPreferences.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImage);
        }else{
            //
            loadBingPicImage();
        }
        String weatherString   = sharedPreferences.getString("weather",null);
        if(weatherString !=null){
            //看是否有缓存，如果有缓存则直接从缓存中解析数据
            Weather weather = Utilty.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherId = getIntent().getStringExtra("weather_id");
            weahterLayout.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(this, UpdateWeatherService.class);
            startService(intent);
            requestWeather(mWeatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
    }

    private void loadBingPicImage(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImage);
                    }
                });
            }
        });
    }

    public void requestWeather(final String weahterId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weahterId+"&key=b11848517e8344d8a5a7d0a02c75ce3e";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.i(TAG,responseText+"================");
                final Weather weather = Utilty.handleWeatherResponse(responseText);
                Log.e(TAG,"返回的数据："+weather);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather !=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_LONG).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        });

        loadBingPicImage();
    }

    //展示从服务器获取到的数据
    public   void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        Log.i(TAG,"updateTime===="+updateTime);
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weahterInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if(weather.aqi !=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度"+weather.suggestion.comfort.info;
        String carWash = "洗车指数"+weather.suggestion.carWash.info;
        String sport = "运动建议"+weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weahterLayout.setVisibility(View.VISIBLE);
    }
}

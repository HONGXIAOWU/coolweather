package coolweather.com.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import coolweather.com.coolweather.gson.Weather;
import coolweather.com.coolweather.util.HttpUtil;
import coolweather.com.coolweather.util.Utilty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateWeatherService extends Service {

    public UpdateWeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        updateWetaher();
        updateBingPicImage();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour = 8*60*60*1000;//八个小时
        long griggerAtTime = SystemClock.elapsedRealtime()+hour;
        Intent intent2 = new Intent(this,UpdateWeatherService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent2,0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,griggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    public  void updateWetaher(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather",null);
        if(weatherString != null){
            //如果有缓存可以直接解析数据
            Weather weather = Utilty.handleWeatherResponse(weatherString);
            String WetaherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid="+WetaherId+"&key=b11848517e8344d8a5a7d0a02c75ce3e";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                 String weatherContent =  response.body().string();
                    Weather weather1 = Utilty.handleWeatherResponse(weatherContent);
                    if(weather1 !=null&&"ok".equals(weather1.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateWeatherService.this).edit();
                        editor.putString("weather",weatherContent);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPicImage(){
        String bingPicImage = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingPicImage, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    String bingPicImage = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateWeatherService.this).edit();
                editor.putString("bing_pic",bingPicImage);
                editor.apply();
            }
        });
    }
}

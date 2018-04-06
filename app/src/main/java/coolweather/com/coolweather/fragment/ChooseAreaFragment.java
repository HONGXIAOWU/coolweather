package coolweather.com.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coolweather.com.coolweather.R;
import coolweather.com.coolweather.activity.MainActivity;
import coolweather.com.coolweather.activity.WeatherActivity;
import coolweather.com.coolweather.db.City;
import coolweather.com.coolweather.db.County;
import coolweather.com.coolweather.db.Province;
import coolweather.com.coolweather.util.HttpUtil;
import coolweather.com.coolweather.util.Utilty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by DELL on 2018/4/3.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private TextView titleView;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private ProgressDialog progressDialog;

    //省列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    //选中的省份
    private Province selectProvince;
    //选中的市
    private City selectCity;

    private int currentLevel;//当前的选中的级别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleView = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //设置点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectProvince = provinceList.get(position);
                    //接着查询市
                    queryCity();
                }else  if(currentLevel==LEVEL_CITY){
                    selectCity = cityList.get(position);
                    //接着查询区
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTRY){
                    String weahtId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent  = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id",weahtId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weahtId);

                    }

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel==LEVEL_COUNTRY){
                    queryCity();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

       queryProvinces();
    }


    private void queryProvinces(){
        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList =DataSupport.findAll(Province.class);
       // 首先去数据库查询
        if(provinceList.size()>0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            //如果数据库没有就去服务器查询
            String address = "http://guolin.tech/api/china";
            ///去服务器查询数据
            queryService(address ,"province");
        }
    }


    //选中的市
    private void queryCity(){
        titleView.setText(selectProvince.getProvinceName());//选中的省的名字
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?",String.valueOf(selectProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            //去服务器查询
           int provinceCode = selectProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryService(address,"city");
        }
    }


    private void queryCounties(){
        titleView.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList  = DataSupport.where("cityid=?",String.valueOf(selectCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode= selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryService(address,"county");
        }

    }

    private void queryService(String address , final String type){
        //显示进度
       // showProgressDialog();
        //请求服务器数据
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              //返回主线程的逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                boolean result =false;
                if("province".equals(type)){
                    result =Utilty.handleProvinceResponse(body);
                }else if("city".equals(type)){
                    result = Utilty.handlerCityResponse(body,selectProvince.getId());
                }else if("county".equals(type)){
                    result = Utilty.handleCountry(body,selectCity.getId());
                }

                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                  queryCity();
                            }else if("county".equals(type)){
                                queryCounties();

                            }
                        }
                    });
                }

            }
        });

    }


    //显示对话框
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog   = new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }

    }


}

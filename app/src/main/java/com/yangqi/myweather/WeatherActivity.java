package com.yangqi.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.yangqi.db.WeatherInfo;
import com.yangqi.gson.Forecast;
import com.yangqi.gson.Weather;
import com.yangqi.utils.HttpUtil;
import com.yangqi.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView sv_weather;
    private TextView tv_city;
    private TextView tv_update_time;
    private TextView tv_degree;
    private TextView tv_info;
    private LinearLayout ll_forecast;
    private TextView tv_aqi;
    private TextView tv_pm25;
    private TextView tv_comfort;
    private TextView tv_wash_car;
    private TextView tv_sport;
    private SharedPreferences sp;
    private ImageView iv_bingImg;
    private Button bt_drawer;
    private SwipeRefreshLayout srl_refresh;
    private DrawerLayout dl_drawer;

    private String mWeatherId;
    private NavigationView nv_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        initUI();
        initData();
        initListener();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        initBackground();
        initWeather();
        initDrawerCityMenu();
    }

    /**
     * 为个控件添加监听事件
     */
    private void initListener() {
        bt_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl_drawer.openDrawer(GravityCompat.START);
            }
        });

        srl_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather();
            }
        });

        nv_menu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // TODO 菜单点击事件
                switch (item.getItemId()) {
                    case R.id.item_setting:
                        break;
                    case R.id.item_feedback:
                        break;
                    case R.id.item_clear_cache:
                        break;
                    case R.id.item_about:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 动态添加左侧滑动菜单城市子菜单
     */
    private void initDrawerCityMenu() {
        // 通过数据库获得天气 WeatherInfo 对象 List，遍历生成菜单项
        SubMenu subMenu = nv_menu.getMenu().getItem(0).getSubMenu();
        subMenu.clear();
        List<WeatherInfo> all = DataSupport.findAll(WeatherInfo.class);
        for (WeatherInfo weatherInfo : all) {
            MenuItem item = subMenu.add(weatherInfo.getCityName()).setIcon(R.drawable.city);
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra("weather_id", weatherInfo.getWeatherId());
            item.setIntent(intent);
        }
        MenuItem cityManage = subMenu.add("添加/删除城市").setIcon(R.drawable.manage_city);
        Intent intent = new Intent(this, CityManagerActivity.class);
        cityManage.setIntent(intent);
    }

    /**
     * 初始化天气信息
     */
    private void initWeather() {
        mWeatherId = getIntent().getStringExtra("weather_id");
        sp.edit().putString("weather_id", mWeatherId).apply();
        //天气信息缓存有直接拿过来，没有则去网络加载
        WeatherInfo weatherInfo = DataSupport.where("weatherId = ?", mWeatherId).findFirst(WeatherInfo.class);
        if (weatherInfo == null || weatherInfo.getContent() == null) {
            sv_weather.setVisibility(View.INVISIBLE);
            requestWeather();
        } else {
            Weather weather = Utility.handleWeatherResponse(weatherInfo.getContent());
            showWeather(weather);
        }
    }

    /**
     * 加载背景图
     */
    private void initBackground() {
        //加载 bing 搜索每日一图作为背景
        RequestOptions options = new RequestOptions();
        options.signature(new MediaStoreSignature("", sp.getLong("oldTime", 0), 0));
        Glide.with(this).load("http://api.dujin.org/bing/1920.php")
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_bingImg);

        // 缓存时间超过 24 小时，重新加载
        long timeDiff = System.currentTimeMillis() - sp.getLong("oldTime", 0);
        if (timeDiff > 24 * 3600 * 1000) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            long time = 0L;
            try {
                // 下面这个操作将当前时间毫秒数转化为当天零点时的毫秒数
                time = df.parse(df.format(new Date())).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 将当天零点时的毫秒数存起来，后面每次对比实现24小时刷新策略
            sp.edit().putLong("oldTime", time).apply();
            options.signature(new MediaStoreSignature("", sp.getLong("oldTime", 0), 0));
            Glide.with(this).load("http://api.dujin.org/bing/1920.php")
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_bingImg);
        }
    }

    /**
     * 联网查询天气数据
     */
    public void requestWeather() {

        String url = "https://free-api.heweather.com/v5/weather?city=" + mWeatherId +
                "&key=6516ce048ddc48bc991ca3f1712fe74d";

        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                "获取数据失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                        srl_refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherResponse = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(weatherResponse);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            showWeather(weather);
                            WeatherInfo weatherInfo = new WeatherInfo(weather.basic.weatherId, weatherResponse, weather.basic.cityName);
                            if (DataSupport.where("weatherId = ?", mWeatherId).findFirst(WeatherInfo.class) != null) {
                                weatherInfo.updateAll("weatherId = ?", mWeatherId);
                            } else {
                                weatherInfo.save();
                            }
                            // 请求完成后，需要更新城市管理条目
                            initDrawerCityMenu();
                        } else {
                            Toast.makeText(WeatherActivity.this,
                                    "服务器维护中，请稍后再试！", Toast.LENGTH_SHORT).show();
                        }
                        srl_refresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        dl_drawer.closeDrawers();

        sp.edit().putString("weather_id", intent.getStringExtra("weather_id")).apply();

        initDrawerCityMenu();
        initWeather();
    }

    @Override
    public void onBackPressed() {
        if (dl_drawer.isDrawerOpen(nv_menu)) {
            dl_drawer.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dl_drawer.closeDrawers();
    }

    /**
     * 将天气数据展示到各个控件上
     *
     * @param weather 天气实体类
     */
    private void showWeather(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String info = weather.now.more.info;

        tv_city.setText(cityName);
        tv_update_time.setText(updateTime);
        tv_degree.setText(degree);
        tv_info.setText(info);

        ll_forecast.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = View.inflate(this, R.layout.item_forecast, null);

            TextView tv_forecast_date = (TextView) view.findViewById(R.id.tv_forecast_date);
            TextView tv_forecast_info = (TextView) view.findViewById(R.id.tv_forecast_info);
            TextView tv_forecast_max = (TextView) view.findViewById(R.id.tv_forecast_max);
            TextView tv_forecast_min = (TextView) view.findViewById(R.id.tv_forecast_min);

            tv_forecast_date.setText(forecast.date);
            tv_forecast_info.setText(forecast.more.info);
            tv_forecast_max.setText(forecast.temperature.max);
            tv_forecast_min.setText(forecast.temperature.min);

            ll_forecast.addView(view);
        }

        if (weather.aqi != null) {
            tv_aqi.setText(weather.aqi.city.aqi);
            tv_pm25.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.txt;
        String carWash = "洗车指数：" + weather.suggestion.carWash.txt;
        String sport = "运动指数：" + weather.suggestion.sport.txt;
        tv_comfort.setText(comfort);
        tv_wash_car.setText(carWash);
        tv_sport.setText(sport);

        sv_weather.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化各个控件及成员变量
     */
    private void initUI() {

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        bt_drawer = (Button) findViewById(R.id.bt_drawer);
        dl_drawer = (DrawerLayout) findViewById(R.id.dl_drawer);

        sv_weather = (ScrollView) findViewById(R.id.sv_weather);

        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_update_time = (TextView) findViewById(R.id.tv_update_time);

        tv_degree = (TextView) findViewById(R.id.tv_degree);
        tv_info = (TextView) findViewById(R.id.tv_info);

        ll_forecast = (LinearLayout) findViewById(R.id.ll_forecast);

        tv_aqi = (TextView) findViewById(R.id.tv_aqi);
        tv_pm25 = (TextView) findViewById(R.id.tv_pm25);
        tv_comfort = (TextView) findViewById(R.id.tv_comfort);

        tv_wash_car = (TextView) findViewById(R.id.tv_wash_car);
        tv_sport = (TextView) findViewById(R.id.tv_sport);

        iv_bingImg = (ImageView) findViewById(R.id.iv_bingImg);

        srl_refresh = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        srl_refresh.setColorSchemeResources(R.color.colorPrimary);

        nv_menu = (NavigationView) findViewById(R.id.nv_menu);

    }
}

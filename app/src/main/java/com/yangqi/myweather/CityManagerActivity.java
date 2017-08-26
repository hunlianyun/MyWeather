package com.yangqi.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yangqi.db.WeatherInfo;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CityManagerActivity extends AppCompatActivity {

    /**
     * 要删除的所有城市在 all 集合中的索引位置存放在此集合，方便删除
     */
    private Set<Integer> removeCitiesIndex = new HashSet<>();
    private List<WeatherInfo> all;
    private boolean isDeleteMode = false;

    /**
     * 要展示在 listview 上的数据
     */
    private List<String> cities = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView lv_city;

    private TextView tv_delete;
    private TextView tv_add;
    private TextView tv_cancel;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);

        initUI();
        initData();
    }

    /**
     * 加载数据，设置监听事件
     */
    private void initData() {

        initCitiesList();

        // listview 加载数据
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cities);
        lv_city.setAdapter(adapter);

        // listview 的条目点击事件，分是否是删除模式两种情况
        lv_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isDeleteMode) {
                    if (removeCitiesIndex.contains(position)) {
                        view.setBackgroundColor(Color.GRAY);
                        removeCitiesIndex.remove(position);
                    } else {
                        view.setBackgroundColor(Color.rgb(0xFF, 0x9D, 0x9D));
                        removeCitiesIndex.add(position);
                    }
                } else {
                    Intent intent = new Intent(CityManagerActivity.this, WeatherActivity.class);
                    intent.putExtra("weather_id", all.get(position).getWeatherId());
                    startActivity(intent);
                }
            }
        });

        // listview 长点击事件，用来出发删除模式
        lv_city.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isDeleteMode) {
                    enableDeleteMode();
                    view.setBackgroundColor(Color.rgb(0xFF, 0x9D, 0x9D));
                    removeCitiesIndex.add(position);
                }
                return true;
            }
        });

        // 添加城市
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CityManagerActivity.this, MainActivity.class);
                intent.putExtra("isAddCity", true);
                startActivityForResult(intent, 0);
            }
        });

        // 删除城市
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (Integer index : removeCitiesIndex) {
                    all.get(index).delete();
                }
                initCitiesList();
                unableDeleteMode();
                adapter.notifyDataSetChanged();

                // 没有城市了，跳转到 MainActivity
                if (cities.isEmpty()) {
                    Intent intent = new Intent(CityManagerActivity.this, MainActivity.class);
                    intent.putExtra("isNoCity", true);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 取消删除
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unableDeleteMode();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String weather_id = data.getStringExtra("weather_id");
            if (DataSupport.where("weatherId = ?", weather_id).findFirst(WeatherInfo.class) == null) {
                String city_name = data.getStringExtra("city_name");
                WeatherInfo weatherInfo = new WeatherInfo(weather_id, null, city_name);
                weatherInfo.save();
                initCitiesList();
                adapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(this, "您取消了添加", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // 按下返回键时，如果是删除状态，就取消删除状态，
        // 如若不是删除状态，则打开 WeatherActivity
        if (isDeleteMode) {
            unableDeleteMode();
        } else {
            String weather_id = sp.getString("weather_id", "");
            Intent intent = new Intent(this, WeatherActivity.class);
            if (DataSupport.where("weatherId = ?", weather_id).findFirst(WeatherInfo.class) == null) {
                // 如果本来默认城市被删除了，则换成当前第一条城市
                intent.putExtra("weather_id", all.get(0).getWeatherId());
            } else {
                intent.putExtra("weather_id", weather_id);
            }
            startActivity(intent);
            finish();
        }
    }

    /**
     * 取消删除模式
     */
    private void unableDeleteMode() {
        isDeleteMode = false;
        for (int i = 0; i < cities.size(); i++) {
            lv_city.getChildAt(i).setBackgroundColor(Color.WHITE);
        }
        removeCitiesIndex.clear();
        tv_cancel.setVisibility(View.INVISIBLE);
        tv_add.setVisibility(View.VISIBLE);
        tv_delete.setVisibility(View.GONE);
    }

    /**
     * 进入删除模式
     */
    private void enableDeleteMode() {
        isDeleteMode = true;
        for (int i = 0; i < cities.size(); i++) {
            lv_city.getChildAt(i).setBackgroundColor(Color.GRAY);
        }
        tv_add.setVisibility(View.GONE);
        tv_delete.setVisibility(View.VISIBLE);
        tv_cancel.setVisibility(View.VISIBLE);
    }

    /**
     * 加载数据库已存城市信息
     */
    private void initCitiesList() {
        all = DataSupport.findAll(WeatherInfo.class);
        cities.clear();
        for (WeatherInfo weatherInfo : all) {
            cities.add(weatherInfo.getCityName());
        }
    }

    private void initUI() {
        lv_city = (ListView) findViewById(R.id.lv_city);
        tv_delete = (TextView) findViewById(R.id.tv_delete);
        tv_add = (TextView) findViewById(R.id.tv_add);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
    }
}

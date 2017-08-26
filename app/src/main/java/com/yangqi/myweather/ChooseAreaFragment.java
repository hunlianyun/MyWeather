package com.yangqi.myweather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
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

import com.yangqi.db.City;
import com.yangqi.db.County;
import com.yangqi.db.Province;
import com.yangqi.utils.HttpUtil;
import com.yangqi.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 选择地区类
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private int currentLevel;

    private Button bt_back;
    private ListView lv_info;
    private TextView tv_title;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;

    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //ui 初始化
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        bt_back = (Button) view.findViewById(R.id.bt_back);
        lv_info = (ListView) view.findViewById(R.id.lv_info);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        lv_info.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv_info.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    selectedCounty = countyList.get(position);
                    String weatherId = selectedCounty.getWeatherId();
                    if (getActivity().getIntent().getBooleanExtra("isAddCity", false)) {
                        Intent data = new Intent();
                        data.putExtra("weather_id", weatherId);
                        data.putExtra("city_name", selectedCounty.getCountyName());
                        getActivity().setResult(0, data);
                        getActivity().finish();
                    } else {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }
        });

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                }
            }
        });

        queryProvinces();
    }

    /**
     * 查询各省份，让用户选择，除了第一次启动联网加载到本地数据库，以后默认从数据库查询
     */
    private void queryProvinces() {

        provinceList = DataSupport.findAll(Province.class);
        if (!provinceList.isEmpty()) {
            tv_title.setText("中国");
            bt_back.setVisibility(View.INVISIBLE);
            dataList.clear();
            adapter.notifyDataSetChanged();

            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv_info.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }


    /**
     * 查询各市，让用户选择，除了第一次启动联网加载到本地数据库，以后默认从数据库查询
     */
    private void queryCities() {

        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);

        if (!cityList.isEmpty()) {
            tv_title.setText(selectedProvince.getProvinceName());
            bt_back.setVisibility(View.VISIBLE);
            dataList.clear();
            adapter.notifyDataSetChanged();

            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv_info.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询各县，让用户选择，除了第一次启动联网加载到本地数据库，以后默认从数据库查询
     */
    private void queryCounties() {

        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId()))
                .find(County.class);

        if (!countyList.isEmpty()) {
            tv_title.setText(selectedCity.getCityName());
            bt_back.setVisibility(View.VISIBLE);
            dataList.clear();
            adapter.notifyDataSetChanged();

            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            lv_info.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 从网络接口获取地区列表
     *
     * @param address 请求地址
     * @param type    查询类型，可以为 province, city, county
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                closeProgressDialog();
                Looper.prepare();
                Toast.makeText(getContext(), "加载失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result;
                switch (type) {
                    case "province":
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                        break;
                    case "county":
                        result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                        break;
                    default:
                        result = false;
                        break;
                }

                closeProgressDialog();
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryFor(type);
                        }
                    });
                } else {
                    Looper.prepare();
                    Toast.makeText(getContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }

    /**
     * 根据不同类型调用不同方法
     *
     * @param type 类型
     */
    private void queryFor(String type) {
        switch (type) {
            case "province":
                queryProvinces();
                break;
            case "city":
                queryCities();
                break;
            case "county":
                queryCounties();
                break;
            default:
                break;
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载数据...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

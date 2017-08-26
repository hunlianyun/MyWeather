package com.yangqi.myweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weather_id = sp.getString("weather_id", "");
        boolean isAddCity = getIntent().getBooleanExtra("isAddCity", false);
        boolean isNoCity = getIntent().getBooleanExtra("isNoCity", false);
        if (weather_id.equals("") || isAddCity || isNoCity) {
            setContentView(R.layout.activity_main);
        } else {
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra("weather_id", weather_id);
            startActivity(intent);
            finish();
        }
    }
}

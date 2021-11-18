package com.weatherApp.weatherapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{
    protected LocationManager locationManager;
    String API = "8118ed6ee68db2debfaaa5a44c832918"; //ลองไป Register เอง
    String CITY = "";
    String COUNTRY = "";
    String changeCity = "";
    String cityNameFromLatLng = "";
    String value;
    double lat = 0, lng = 0;
    boolean checkLatLng = true;

    TextView addressTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt, sunsetTxt, windTxt, humidityTxt;
    ImageView weatherCon;
    Button searchBtn;
    EditText searchBox;

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        addressTxt = findViewById(R.id.address);
        statusTxt = findViewById(R.id.status);
        tempTxt = findViewById(R.id.temp);
        temp_minTxt = findViewById(R.id.mintemp);
        temp_maxTxt = findViewById(R.id.maxtemp);
        sunriseTxt = findViewById(R.id.sunrise);
        sunsetTxt = findViewById(R.id.sunset);
        windTxt = findViewById(R.id.wind);
        humidityTxt = findViewById(R.id.humidity);
        weatherCon = findViewById(R.id.weatherCon);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        searchBtn = findViewById(R.id.button);
        new SearchBox().search(searchBtn);
        new SearchBox().searchWithEnterKey();
    }

    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
        if(checkLatLng) {
            try {
                List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
                System.out.println("latitude: "+lat);
                System.out.println("longitude: "+lng);
                cityNameFromLatLng = addresses.get(0).getLocality();
                System.out.println(addresses.get(0));
                if(cityNameFromLatLng == null){
                    cityNameFromLatLng = addresses.get(0).getCountryName();
                }
                CITY = cityNameFromLatLng;
                COUNTRY = addresses.get(0).getCountryName();
                new weatherTask().execute();
                checkLatLng = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    class SearchBox {
        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
        protected void search(Button btn){
            btn.setOnClickListener(view -> {
                if(changeCity.equals("")){
                    searchBox = findViewById(R.id.searchBox);
                    value = searchBox.getText().toString();
                    if(value.length() == 0){
                        checkLatLng = true;
                    }else {
                        changeCity = value;
                        CITY = changeCity;
                        COUNTRY = "";
                        new weatherTask().execute();
                        changeCity = "";
                    }//end if
                    searchBox.setText(null);
                }//end if
            });
        }
        protected void searchWithEnterKey(){
            searchBox = findViewById(R.id.searchBox);
            searchBox.setOnEditorActionListener((v, actionId, event) -> {
                value = searchBox.getText().toString();
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(value.length() == 0){
                        checkLatLng = true;
                    }else {
                        changeCity = value;
                        CITY = changeCity;
                        COUNTRY = "";
                        new weatherTask().execute();
                        changeCity = "";
                    }//end if
                    searchBox.setText(null);
                    return true;
                }
                return false;
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    class weatherTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... args) {
            String targetUrl1 = "https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + API;
            String targetUrl2 = "https://api.openweathermap.org/data/2.5/weather?q=" + COUNTRY + "&units=metric&appid=" + API;
            String response = HttpRequest.excuteGet(targetUrl1, targetUrl2);
            System.out.println(response);
            return response;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            try{
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                String temp = main.getString("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String humidity = main.getString("humidity");
                long sunrise = sys.getLong("sunrise");
                long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");
                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                /*Set ค่าต่างๆ ตาม TextView*/
                addressTxt.setText(address);
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windTxt.setText("Wind: "+windSpeed);
                humidityTxt.setText("Humidity: "+humidity);

                /*เปลี่ยนรูป icon weather condition*/
                switch (weatherDescription){
                    case "clear sky":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.sun));
                        break;
                    case "few clouds":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.few_clouds));
                        break;
                    case "scattered clouds":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.clouds));
                        break;
                    case "broken clouds":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.broken_clouds));
                        break;
                    case "shower rain":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.shower_rains));
                        break;
                    case "rain":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.rains));
                        break;
                    case "thunderstorm":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.thunderstrom));
                        break;
                    case "snow":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                        break;
                    case "mist":
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.mist));
                        break;
                    default:
                        weatherCon.setImageDrawable(getResources().getDrawable(R.drawable.clouds2));
                        break;
                }

            }catch(JSONException e) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.CustomDialog);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alert_style, findViewById(R.id.linear_layout), false);
                alertDialog.setView(view);
                alertDialog.setCancelable(false);
                ((TextView) view.findViewById(R.id.textTitle)).setText(R.string.err);
                value = getText(R.string.err_text) + "\"" +value+ "\"";
                System.out.println(value);
                ((TextView) view.findViewById(R.id.textMessage)).setText(value);
                ((Button) view.findViewById(R.id.button)).setText(R.string.btn);
                final AlertDialog alert = alertDialog.create();
                view.findViewById(R.id.button).setOnClickListener(view1 -> alert.dismiss());
                alert.show();
            }
        }
    }
}
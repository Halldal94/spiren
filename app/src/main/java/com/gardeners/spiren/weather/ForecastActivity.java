package com.gardeners.spiren.weather;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gardeners.spiren.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ForecastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.setTitle("Værvarsel");

        Intent intent = getIntent();
        String[] h = intent.getStringArrayExtra("hours");
        ArrayList<Hour> hours = new ArrayList<>();

        for (String s : h) {
            String[] strings = s.split(",");
            Date from = formatDate(strings[0]);
            Date to = formatDate(strings[1]);
            double temperature = Double.valueOf(strings[2]);
            double humidity = Double.valueOf(strings[3]);
            double precipitation = Double.valueOf(strings[4]);
            String symbol = strings[5];
            hours.add(new Hour(from, to, temperature, humidity, precipitation, symbol));
        }

        TableLayout tableForecast = findViewById(R.id.tableForecast);
        tableForecast.setStretchAllColumns(true);
        tableForecast.bringToFront();

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        float ratio = (float) (110 / (size.x * 0.1));

        for(int i = 0; i < hours.size(); i++){
            TableRow row =  new TableRow(this);
            row.setPadding(10, 40, 10, 40);

            int color = i % 2 == 0 ? Color.WHITE : Color.rgb(240, 250, 255);

            // Time
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.VERTICAL);
            TextView tvDay = new TextView(this);
            String day = dayOfWeek(hours.get(i).getTo().getDay());
            tvDay.setText(day);
            tvDay.setTextSize(15);
            tvDay.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            int hour = hours.get(i).getTo().getHours();
            String time = hour > 9 ? hour + ":00" : "0" + hour + ":00";
            TextView tvTime = getTv(time, color);
            tvTime.setPadding(0,0,0,0);
            l.addView(tvDay);
            l.addView(tvTime);
            l.setPadding(0, 20, 0, 30);
            row.addView(l);

            // Symbol
            ImageView view = new ImageView(this);
            view.setImageResource(getSymbol(hours.get(i).getSymbol()));
            view.setScaleY(ratio);
            view.setScaleX(ratio);
            view.setBackgroundColor(color);
            row.addView(view);

            // Temperature
            String temperature = String.valueOf(hours.get(i).getTemperature() + "\u2103");
            TextView tvTemperature = getTv(temperature, color);
            row.addView(tvTemperature);

            // Precipitation
            String precipitation = String.valueOf(hours.get(i).getPrecipitation()) + " mm";
            TextView tvPrecipitation = getTv(precipitation, color);
            row.addView(tvPrecipitation);

            TableRow trBlank = new TableRow(this);
            //row.setBackgroundColor(Color.LTGRAY);
            row.setBackgroundColor(color);
            row.setPadding(0,5,0,0);

            tableForecast.addView(row);
            tableForecast.addView(trBlank);

        }
    }

    public TextView getTv(String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextSize(25);
        tv.setBackgroundColor(color);
        tv.setPadding(10, 50, 10, 40);
        return tv;
    }


    public int getSymbol(String symbol) {
        switch (symbol) {
            case "Sun": return R.drawable.weather_sun;
            case "SleetSun": return R.drawable.weather_sleet_sun;
            case "LightSleetSun": return R.drawable.weather_light_sleet_sun;

            case "Cloud": return R.drawable.weather_cloud;
            case "PartlyCloud":
            case "LightCloud": return R.drawable.weather_light_cloud;

            case "Rain": return R.drawable.weather_rain;
            case "Drizzle":
            case "LightRain": return R.drawable.weather_light_rain;

            case "DrizzleSun":
            case "LightRainSun": return R.drawable.weather_light_rain_sun;

            case "Snow": return R.drawable.weather_snow;
            case "LightSnow": return R.drawable.weather_light_snow;
            case "HeavySnow": return R.drawable.weather_heavy_snow;

            case "LightSleet":
            case "Sleet": return R.drawable.weather_sleet;
            case "HeavySleet": return R.drawable.weather_heavy_sleet;

            default: return 0;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }

        return true;
    }

    private Date formatDate(String date) {
        String year = date.substring(30);
        String month = monthToNumber(date.substring(4,7));
        String day = date.substring(8,10);
        String time = date.substring(11,19);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        try {
            return fmt.parse(year + "-" + month + "-" + day + "T" + time + "Z");
        } catch (ParseException e) {
            Log.e("ForecastActivity", "DateParseException");
            return null;
        }
    }

    private String monthToNumber(String month) {
        switch (month) {
            case "Jan": return "01";
            case "Feb": return "02";
            case "Mar": return "03";
            case "Apr": return "04";
            case "May": return "05";
            case "Jun": return "06";
            case "Jul": return "07";
            case "Aug": return "08";
            case "Sep": return "09";
            case "Oct": return "10";
            case "Nov": return "11";
            case "Dec": return "12";
            default: return "00";
        }
    }

    private String dayOfWeek(int day) {
        switch (day) {
            case 0: return "Søndag";
            case 1: return "Mandag";
            case 2: return "Tirsdag";
            case 3: return "Onsdag";
            case 4: return "Torsdag";
            case 5: return "Fredag";
            case 6: return "Lørdag";
            default: return "";
        }
    }

}

package com.gardeners.spiren.weather;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

        int headerTextSize = 25;

        TableRow trHeader = new TableRow(this);
        trHeader.setPadding(0,30,0,50);
        trHeader.setBackgroundColor(Color.rgb(227, 242, 255));

        TextView tvTime = new TextView(this);
        tvTime.setText(R.string.forecast_time);
        tvTime.setTextSize(headerTextSize);
        tvTime.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTime.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        TextView tvTemp = new TextView(this);
        tvTemp.setText(R.string.forecast_temperature);
        tvTemp.setTextSize(headerTextSize);
        tvTemp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTemp.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        TextView tvPrecipitation = new TextView(this);
        tvPrecipitation.setText(R.string.forecast_precipitation);
        tvPrecipitation.setTextSize(headerTextSize);
        tvPrecipitation.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvPrecipitation.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        trHeader.addView(tvTime);
        trHeader.addView(tvTemp);
        trHeader.addView(tvPrecipitation);
        tableForecast.addView(trHeader);


        int rowTextSize = 25;
        for(int i = 0; i < hours.size(); i++){
            TableRow tr =  new TableRow(this);
            tr.setPadding(10, 40, 10, 40);

            int color = i % 2 == 0 ? Color.WHITE : Color.rgb(245, 245, 245);

            /*TextView symbol = new TextView(this);
            symbol.setText(String.valueOf(getSymbol(hours.get(i).getSymbol())));
            symbol.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            symbol.setBackgroundColor(color);
            symbol.setPadding(10, 40, 10, 40);
            */

            // Time
            TextView c1 = new TextView(this);
            int time = hours.get(i).getTo().getHours();
            String t = time > 9 ? time + ":00" : "0" + time + ":00";
            c1.setText(t);
            c1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            c1.setTextSize(rowTextSize);
            c1.setBackgroundColor(color);
            c1.setPadding(10, 40, 10, 40);

            // Temperature
            TextView c2 = new TextView(this);
            String temperature = String.valueOf(hours.get(i).getTemperature() + "\u2103");
            c2.setText(temperature);
            c2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            c2.setTextSize(rowTextSize);
            c2.setBackgroundColor(color);
            c2.setPadding(10, 40, 10, 40);

            // Precipitation
            TextView c3 = new TextView(this);
            String precipitation = String.valueOf(hours.get(i).getPrecipitation()) + " mm";
            c3.setText(precipitation);
            c3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            c3.setTextSize(rowTextSize);
            c3.setBackgroundColor(color);
            c3.setPadding(10, 40, 10, 40);

            //tr.addView(symbol);
            tr.addView(c1);
            tr.addView(c2);
            tr.addView(c3);
            tableForecast.addView(tr);

            TableRow trBlank = new TableRow(this);
            tr.setBackgroundColor(Color.LTGRAY);
            tr.setPadding(0,5,0,0);
            tableForecast.addView(trBlank);
        }
    }


    public int getSymbol(String symbol) {
        switch (symbol) {
            case "Cloud": return 1;
            case "LightRain": return 2;
            case "Drizzle": return 3;
            case "Rain": return 4;
            default: return 5;
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

}

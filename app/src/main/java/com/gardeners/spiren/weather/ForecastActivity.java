package com.gardeners.spiren.weather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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
            hours.add(new Hour(from, to, temperature, humidity, precipitation));
        }

        Log.i("i", "i");


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

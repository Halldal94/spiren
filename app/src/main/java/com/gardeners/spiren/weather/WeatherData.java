package com.gardeners.spiren.weather;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.gardeners.spiren.MainActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherData {

    private Context context;
    private TextView tv;

    private List<TimeSlot> timeSlots;
    private List<Hour> hours;

    private static final String TAG = "XMLDataDownload";
    private static final double LAT = 63.4325;
    private static final double LON = 10.4071;
    private static final String SERVER_URL = "https://api.met.no/";
    private static final String QUERY_FILE = "weatherapi/locationforecast/1.9/";
    private static final String QUERY_OPTIONS = "?lat=" + LAT + "&lon=" + LON;
    private static final String QUERY_URL = SERVER_URL + QUERY_FILE + QUERY_OPTIONS;

    private int tries = 0; // Keep track of attempted downloads

    public WeatherData(Context context, TextView tv) {
        this.context = context;
        this.tv = tv;
    }

    public void download() {
        Log.i(TAG, "Querying server");
        AsyncDownloader downloader = new AsyncDownloader();
        downloader.execute();
    }

    public List<Hour> getHours() {
        return this.hours;
    }

    public void tryAgain() {
        tries++;
        Log.e(TAG, "Trying again");
        if (tries <= 5) {
            AsyncDownloader downloader = new AsyncDownloader();
            downloader.execute();
        }
    }

    public void processFinish() {

        // XmlPullParser in tryDownloadingXmlData() sometimes returns FileNotFoundException on
        // valid URL. If exception occurs, a maximum of 5 new attempts will be executed.
        if (tries < 5 && timeSlots.size() == 0) {
            tryAgain();
        } else if (tries == 5 && timeSlots.size() == 0) {
            Toast.makeText(context, "An error occured, please try again.",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Only collect hourly data, not average over several hours.
            // Run through each timeslot and collect temperature and humidity for each hour.
            // For each timeslot valid from t to t (e.g. 10:00 - 10:00, get precipitation
            // from timeslot indexed 6 ahead, which includes precipitation from t to t+1
            // (e.g. 10:00 - 11:00)

            for (TimeSlot slot : timeSlots) {
                if ((slot.getTo().getTime() - slot.getFrom().getTime()) == 0) {

                    int index = timeSlots.indexOf(slot) + 6;

                    if (index < 250) {
                        TimeSlot t = timeSlots.get(index);
                        double precipitation = 0;
                        String symbol;

                        precipitation = t.getPrecipitation();
                        symbol = t.getSymbol();

                        Hour h = new Hour(slot.getFrom(), slot.getTo(), slot.getTemperature(),
                                slot.getHumidity(), precipitation, symbol);

                        hours.add(h);
                    }

                }
            }

            Calendar rightNow = Calendar.getInstance();
            int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
            int firstHour = hours.get(0).getTo().getHours();

            if (currentHour > firstHour) {
                hours.remove(0);
            }

            String temperature = String.valueOf(hours.get(0).getTemperature()) + "\u2103";
            tv.setText(temperature);

            tries = 0;

            MainActivity.setWeatherStatus(true);
        }

    }

    private class AsyncDownloader extends AsyncTask<Object, String, Integer> {

        @Override
        protected void onPostExecute(Integer result) {
            processFinish();
            super.onPostExecute(result);
        }

        @Override
        protected Integer doInBackground(Object... objects) {
            timeSlots = new ArrayList<>();
            hours = new ArrayList<>();
            XmlPullParser receivedData = tryDownloadingXmlData();
            return tryParsingXmlData(receivedData);
        }

        private XmlPullParser tryDownloadingXmlData() {
            try {
                URL xmlUrl = new URL(QUERY_URL);
                XmlPullParser receivedData = XmlPullParserFactory.newInstance().newPullParser();
                receivedData.setInput(xmlUrl.openStream(), null);
                return receivedData;
            } catch (XmlPullParserException e) {
                Log.e(TAG, "XmlPullParserException", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException");//, e);
            }
            return null;
        }

        private int tryParsingXmlData(XmlPullParser receivedData) {
            if (receivedData != null) {
                try {
                    return processReceivedData(receivedData);
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "Pull parser failure", e);
                } catch (IOException e) {
                    Log.e(TAG, "IO Exception parsing XML", e);
                }
            }
            return 0;
        }

        private int processReceivedData(XmlPullParser xmlData) throws XmlPullParserException, IOException {
            int recordsFound = 0;

            // Forecast validity
            String from = "";
            String to = "";

            // Weather related attributes
            String temperature = "";
            String humidity = "";
            String precipitation = "";
            String symbol = "";

            int eventType = -1;

            while (eventType != XmlResourceParser.END_DOCUMENT) {

                String tagName = xmlData.getName();

                switch (eventType) {

                    case XmlResourceParser.START_TAG: // Start of a record, so pull values encoded as attributes.

                        if (tagName.equals("time")) {
                            from = xmlData.getAttributeValue(null, "from");
                            to = xmlData.getAttributeValue(null, "to");
                        }

                        if (tagName.equals("temperature")) {
                            temperature = xmlData.getAttributeValue(null, "value");
                        }

                        if (tagName.equals("humidity")) {
                            humidity = xmlData.getAttributeValue(null, "value");
                        }

                        if (tagName.equals("precipitation")) {
                            precipitation = xmlData.getAttributeValue(null, "value");
                        }

                        if (tagName.equals("symbol")) {
                            symbol = xmlData.getAttributeValue(null, "id");
                        }

                        break;

                    case XmlPullParser.END_TAG:

                        String type = "0";

                        if (tagName.equals("time")) {
                            if (!temperature.equals("")) {
                                recordsFound++;
                                type = "1";
                                publishProgress(type, from, to, temperature, humidity);
                                from = to = temperature = humidity = "";
                            }
                            if (!precipitation.equals("")) {
                                recordsFound++;
                                type = "2";
                                publishProgress(type, from, to, precipitation, symbol);
                                from = to = precipitation = "";
                            }
                        }
                        break;
                }
                eventType = xmlData.next();
            }

            // Handle no data available: Publish an empty event.
            if (recordsFound == 0) {
                publishProgress();
            }

            Log.i(TAG, "Finished processing " + recordsFound + " records.");
            return recordsFound;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            int type;// = 0;
            double temperature;// = 0.0;
            double humidity;//= 0.0;
            double precipitation;// = 0.0;
            String symbol;// = "";

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date from, to;

            // Data fetched
            if (values.length > 0) {
                type = Integer.valueOf(values[0]);
                from = processDate(fmt, values[1]);
                to = processDate(fmt, values[2]);
            } else { // No data fetched
                Log.i(TAG, "No data downloaded");
                return;
            }

            TimeSlot slot = new TimeSlot(type, from, to);

            if (type == 1) { // type, from, to, temperature, humidity
                temperature = Double.valueOf(values[3]);
                humidity = Double.valueOf(values[4]);
                slot.setTemperature(temperature);
                slot.setHumidity(humidity);
            } else if (type == 2) {  // type, from, to, precipitation, symbol
                precipitation = Double.valueOf(values[3]);
                symbol = values[4];
                slot.setPrecipitation(precipitation);
                slot.setSymbol(symbol);
            }

            timeSlots.add(slot);

            super.onProgressUpdate(values);
        }

        private Date processDate(SimpleDateFormat format, String dateStr) {
            try{
                return format.parse(dateStr);
            } catch (ParseException e) {
                Log.e(TAG, "Date parse exception", e);
            }
            return null;
        }


    }

}

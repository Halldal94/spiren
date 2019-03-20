package com.gardeners.spiren.weather;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            // from next timeslot, which includes precipitation from t-1 to t (e.g. 09:00 - 10:00)
            for (TimeSlot slot : timeSlots) {
                if ((slot.getTo().getTime() - slot.getFrom().getTime()) == 0) {
                    double percipitation = timeSlots.get(timeSlots.indexOf(slot) + 1).getPrecipitation();
                    Hour h = new Hour(slot.getFrom(), slot.getTo(), slot.getTemperature(),
                            slot.getHumidity(), percipitation);
                    hours.add(h);
                }
            }

            String temperature = String.valueOf(hours.get(0).getTemperature()) + "\u2103";
            tv.setText(temperature);

            tries = 0;
        }

    }

    // Inner class for doing background download
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

                        break;

                    case XmlPullParser.END_TAG:

                        if (tagName.equals("time")) {
                            if (!temperature.equals("")) {
                                recordsFound++;
                                publishProgress(from, to, temperature, humidity);
                                from = to = temperature = humidity = "";
                            }
                            if (!precipitation.equals("")) {
                                recordsFound++;
                                publishProgress(from, to, precipitation);
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

            double precipitation = 0.0;
            double temperature = 0.0;
            double humidity = 0.0;
            int type = 0;

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date from, to;

            // Data fetched
            if (values.length > 0) {
                from = processDate(fmt, values[0]);
                to = processDate(fmt, values[1]);

                if (values.length == 3) { // Length = 3 [from, to, precipitation]
                    precipitation = Double.valueOf(values[2]);

                    if (to != null && from != null) {
                        // Type 2 and 3 both have 3 attributes, but only type 3 is a 6 hour interval
                        long diff = (to.getTime() - from.getTime()) / 3600000; // Diff in hours
                        type = diff == 6 ? 3 : 2;
                    }

                } else if (values.length == 4) { // Length = 4 [from, to, temperature, himidity]
                    temperature = Double.valueOf(values[2]);
                    humidity = Double.valueOf(values[3]);
                    type = 1;
                }
            } else { // No data fetched
                Log.i(TAG, "No data downloaded");
                return;
            }

            TimeSlot slot = new TimeSlot(type, from, to);

            if (type == 1) {
                slot.setTemperature(temperature);
                slot.setHumidity(humidity);
            } else {
                slot.setPrecipitation(precipitation);
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

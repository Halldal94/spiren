package com.gardeners.spiren.weather;

import java.io.Serializable;
import java.util.Date;

public class Hour implements Serializable {

    private Date from;
    private Date to;
    private double temperature;
    private double humidity;
    private double precipitation;

    public Hour(Date from, Date to, double temperature, double humidity, double precipitation) {
        this.from = from;
        this.to = to;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public String toString() {
        return this.from + "," + this.to + "," + this.temperature + "," +
                this.humidity + "," + this.precipitation;
    }

}

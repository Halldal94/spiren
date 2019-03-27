package com.gardeners.spiren.weather;

import java.util.Date;

public class Hour { //implements Serializable {

    private Date from;
    private Date to;
    private double temperature;
    private double humidity;
    private double precipitation;
    private String symbol;

    public Hour(Date from, Date to, double temperature, double humidity, double precipitation, String symbol) {
        this.from = from;
        this.to = to;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
        this.symbol = symbol;
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
                this.humidity + "," + this.precipitation + "," + this.symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}

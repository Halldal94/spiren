package com.gardeners.spiren.weather;
import java.util.Date;

public class TimeSlot {

    // Primary values
    private int type;
    private Date from;
    private Date to;
    private Double temperature;     // Object type instead of
    private Double humidity;        // primitive type in order
    private Double precipitation;   // to store null values
    private String symbol;


// Additional values that can be used
    /*private Double windDirection;
    private Double windSpeed;
    private Double windGust;
    private Double areaMaxWindSpeed;
    private Double pressure;
    private Double cloudiness;
    private Double fog;
    private Double lowClouds;
    private Double mediumClouds;
    private Double highClouds;
    private Double dewpointTemperature;
    private Double minTemperature;
    private Double maxTemperature;*/

    public TimeSlot(int type, Date from, Date to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}

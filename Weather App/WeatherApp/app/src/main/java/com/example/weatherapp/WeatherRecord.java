package com.example.weatherapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class WeatherRecord {

    private Date date;
    private double humidity;
    private double temperature;
    private double windspeed;

    public WeatherRecord(Date date, double temperature, double humidity,  double windspeed) {
        this.date = date;
        this.humidity = humidity;
        this.temperature = temperature;
        this.windspeed = windspeed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(double windspeed) {
        this.windspeed = windspeed;
    }

    @Override
    public String toString() {
        return "WeatherRecord{" +
                "date=" + date +
                ", humidity=" + humidity +
                ", temperature=" + temperature +
                ", windspeed=" + windspeed +
                '}';
    }
}


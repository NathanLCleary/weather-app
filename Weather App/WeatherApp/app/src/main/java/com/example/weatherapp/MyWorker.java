package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

//https://developer.android.com/jetpack/androidx/releases/work
public class MyWorker extends Worker {

    public static final String WEATHER_BROADCAST = "com.example.workerexample.weatherbroadcast";
    public static final String weather_KEY = "weather.key";

    private Context context;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // get the weather
        String weather = getweather();

        // create custom Broadcast for the weather
        Intent broadcastIntent = new Intent(WEATHER_BROADCAST);
        broadcastIntent.putExtra(weather_KEY,weather);
        // send the Broadcast
        context.sendBroadcast(broadcastIntent);


        // flag successful work
        return Result.success();
    }

    private String getweather() {
        // https://geek-weathers.sameerkumar.website/api
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=53.42&longitude=-7.94&hourly=temperature_2m,relativehumidity_2m,windspeed_10m");

            HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            while(scanner.hasNext())
                sb.append(scanner.nextLine());

        } catch (IOException e) {
            e.printStackTrace();
            sb.append(e.getMessage());
        }

        return sb.toString();
    }

}

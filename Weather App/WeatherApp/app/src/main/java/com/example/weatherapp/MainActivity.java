package com.example.weatherapp;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView Elevation;
    private TextView Longitude;
    private TextView Latitude;
    private TextView Timezone;
    private TextView TempUnit;
    private TextView HumUnit;
    private TextView WindUnit;
    private TextView Current;
    private TextView extractedTv;
    public WeatherRecord weatherRecords[];
    private LineChart lineChart;
    private ArrayList<Entry> entries = new ArrayList<Entry>();
    private XAxis xAxis;
    private RadioGroup radioGroup;
    List<Date> datesArray = new ArrayList<>();
    String TAG = "Test";
    ArrayList<WeatherRecord> listOfRecords = new ArrayList<WeatherRecord>();
    LineData data = new LineData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get all ids from layout
        radioGroup = findViewById(R.id.radioGroup);
        lineChart = findViewById(R.id.chart1);
        Elevation = findViewById(R.id.Elevation);
        Longitude = findViewById(R.id.Longitude);
        Latitude = findViewById(R.id.Latitude);
        Timezone = findViewById(R.id.Timezone);
        TempUnit = findViewById(R.id.TempUnit);
        HumUnit = findViewById(R.id.HumUnit);
        WindUnit = findViewById(R.id.WindUnit);
        Current = findViewById(R.id.Current);
        extractedTv = findViewById(R.id.ExtractedText);

        // set up svg images
        ImageView temp = (ImageView) findViewById(R.id.TempImg);
        temp.setColorFilter(Color.parseColor("#bdbdbd"));



        // graph settings
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("No chart data available, wait for refresh...");
        lineChart.invalidate();
        xAxis = lineChart.getXAxis();


        // create Work request for every 15min to run the worker
        // define Constrains for the job
        Constraints constraints = new Constraints
                .Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        // define Job Request
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                .Builder(MyWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        // execute the job
        WorkManager
                .getInstance(this)
                .enqueue(workRequest);

        // set up radio group and add a listener
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // clear previous data on graph to prepare for new information
                data.clearValues();
                entries.clear();
                lineChart.clear();

                // set up the value formatter to label the xAxis as String from the Array
                xAxis.setValueFormatter(new LineChartData(datesArray));

                if(listOfRecords != null){
                    // check what radio button is clicked
                    if (checkedId == R.id.radioButtonHum){
                        Log.e(TAG, "onCheckedChanged: Here 2");
                        for(WeatherRecord r: listOfRecords){
                            entries.add(new Entry((float) listOfRecords.indexOf(r), (float) r.getHumidity()));
                        }
                    }else if(checkedId == R.id.radioButtonWind){
                        Log.e(TAG, "onCheckedChanged: Here 3");
                        for(WeatherRecord r: listOfRecords){
                            entries.add(new Entry((float) listOfRecords.indexOf(r), (float)r.getWindspeed()));
                        }
                    }else{
                        Log.e(TAG, "onCheckedChanged: Here 1");
                        for(WeatherRecord r: listOfRecords){
                            entries.add(new Entry((float) listOfRecords.indexOf(r), (float) r.getTemperature()));
                        }
                    }

                    // apply the data to the graph
                    LineDataSet dataSet = createSet();
                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);

                    data.notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                    lineChart.invalidate();

                    // change graph settings
                    lineChart.setVisibleXRangeMaximum(12);
                    lineChart.moveViewTo(
                            0,
                            50f,
                            YAxis.AxisDependency.LEFT);
                }

            }
        });
    }

    // subscribe to the broadcast
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Test", "Got information from API");

            radioGroup.check(R.id.radioButtonTemp);
            String joke = intent.getStringExtra(MyWorker.weather_KEY);

            // clear previous data on graph to prepare for new information
            data.clearValues();
            entries.clear();
            lineChart.clear();

            // access the JSON object and read all contents
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(joke);

                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String timezone = jsonObject.getString("timezone");

                // hourly units is behind the 'hourly_units' JSON object
                JSONObject unitObject = jsonObject.getJSONObject("hourly_units");
                String tempUnit = unitObject.getString("temperature_2m");
                String humUnit = unitObject.getString("relativehumidity_2m");
                String windUnit = unitObject.getString("windspeed_10m");
                double elevation = jsonObject.getDouble("elevation");

                /*
                int utc_offset_seconds = jsonObject.getInt("utc_offset_seconds");
                double generationtime_ms = jsonObject.getDouble("generationtime_ms");
                */

                // add information to the screen
                Latitude.setText("Latitude: " + latitude);
                Longitude.setText("Longitude: " + longitude);
                Elevation.setText("Elevation: " + elevation);
                Timezone.setText("Timezone: " + timezone);
                extractedTv.setText("Nathan Cleary");
                TempUnit.setText(" " + tempUnit);
                HumUnit.setText(" " + humUnit);
                WindUnit.setText(" " + windUnit);

                // All of the data is behind the 'hourly' JSON Object
                JSONObject object = jsonObject.getJSONObject("hourly");

                // Get the Array of data from the object
                JSONArray date = object.getJSONArray("time");
                JSONArray temperature = object.getJSONArray("temperature_2m");
                JSONArray humidity = object.getJSONArray("relativehumidity_2m");
                JSONArray windspeed = object.getJSONArray("windspeed_10m");

                // get the all dates in the array and convert the date from a string to a java Date Object
                for(int i = 0; i < date.length(); i++){
                    String stringDate = date.getString(i);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T']HH:mm")
                            .withZone(ZoneOffset.UTC);
                    OffsetDateTime d = OffsetDateTime.parse(stringDate, formatter);
                    Date dateDate = Date.from(d.toInstant());

                    // add date to array to be used by the formatter and place it on the X-Axis
                    datesArray.add(dateDate);

                    // put all information gotten from the JSON and create
                    WeatherRecord r = new WeatherRecord(
                            dateDate,
                            temperature.getDouble(i),
                            humidity.getInt(i),
                            windspeed.getDouble(i)
                    );

                    // add the WeatherRecord to the array
                    listOfRecords.add(r);

                }

                // set current weather
                Current.setText("It is currently " + listOfRecords.get(0).getTemperature() + " " + tempUnit +
                        ". The humidity is " + listOfRecords.get(0).getHumidity() +  humUnit
                        + " and the current wind speed is " + listOfRecords.get(0).getWindspeed() + " " + windUnit);

                xAxis.setValueFormatter(new LineChartData(datesArray));

                // add all temperature units and hourly units as points on the graph
                for(WeatherRecord r: listOfRecords){
                    entries.add(new Entry((float) listOfRecords.indexOf(r), (float) r.getTemperature()));
                }

                // notify that the data has changed
                LineDataSet dataSet = createSet();
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();

                // change graph settings
                lineChart.setVisibleXRangeMaximum(12);
                lineChart.moveViewTo(
                        0,
                        50f,
                        YAxis.AxisDependency.LEFT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(entries, "DataSet 1");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(240, 99, 99));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);

        return set;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MyWorker.WEATHER_BROADCAST);
        registerReceiver(broadcastReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}



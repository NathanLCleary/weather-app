package com.example.weatherapp;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Date;
import java.util.List;

public class LineChartData extends ValueFormatter {

    static List<Date> datesList;
    public LineChartData(List<Date> arrayOfDates) {
        this.datesList = arrayOfDates;
    }

    @Override
    public String getFormattedValue(float value) {
        if ((int) value < 0 && (int) value > LineChartData.datesList.size() - 1) {
            return "";
        }
        Date d = datesList.get((int)(value));

        int day = d.getDay();
        int month = d.getMonth();
        int year = d.getYear();
        int hour = d.getHours();
        int min= d.getMinutes();

        return day + "/" + month + " " + hour + ":" +min + "0";

    }
    public static void getXAxis(){


    }
}

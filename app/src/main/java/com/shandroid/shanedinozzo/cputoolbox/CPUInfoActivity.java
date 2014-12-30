package com.shandroid.shanedinozzo.cputoolbox;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class CPUInfoActivity extends ActionBarActivity {

    TextView CPUInfoTV, deviceManufacturerModelTV;
    String realManufacturer, valueManufacturer, model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpuinfo);

        CPUInfoTV = (TextView) findViewById(R.id.CPUInfoTV);
        CPUInfoTV.setText(_getCPUInfo());

        deviceManufacturerModelTV = (TextView) findViewById(R.id.deviceManufacturerModelTV);
        realManufacturer = _manufacturer();
        model = _model();
        deviceManufacturerModelTV.setText(_valueManufacturer(realManufacturer) + " " + model);
    }


    public String _getCPUInfo() {
        StringBuilder stringBuffer = new StringBuilder();
        //noinspection deprecation
        stringBuffer.append("ABI: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(
                        new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = bufferedReader.readLine()) != null) {
                    stringBuffer.append(aLine).append("\n");
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            CPUInfoTV.setText("YOUR DEVICE IS NOT SUPPORTED AT THIS TIME!");
        }
        return stringBuffer.toString();
    }

    public String _manufacturer() {
        return Build.MANUFACTURER;
    }

    public String _model() {
        return Build.MODEL;
    }

    public String _valueManufacturer(String RealModel) {
        switch (RealModel) {
            case "samsung":
                valueManufacturer = "Samsung";
                break;
        }

        return valueManufacturer;
    }
}

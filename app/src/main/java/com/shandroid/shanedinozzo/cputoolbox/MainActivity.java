package com.shandroid.shanedinozzo.cputoolbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class MainActivity extends ActionBarActivity {

    static Context context;
    String[] availableFrequenciesArray;
    String selectedGovernor, selectedMaximumFrequency, selectedMinimumFrequency;
    int selectedMaxFreq, selectedMinFreq;
    Button applySelectedCPUFrequencyButton;
    Spinner maxFreqSpinner, minFreqSpinner, governorSpinner;
    ArrayList<String> availableFrequencies, availableFrequenciesForSpinner,
            availableGovernorsForSpinner;
    int frequency;
    String selectedRealMaximumFrequency, selectedRealMinimumFrequency, governor, currentCPUFrequency;
    TextView currentGovernorTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _context();
        _declareBaseMaximumFrequencyVariable();
        _declareBaseMinimumFrequencyVariable();

        currentGovernorTV = (TextView) findViewById(R.id.currentGovernor);
        governorSpinner = (Spinner) findViewById(R.id.governorSpinner);
        applySelectedCPUFrequencyButton = (Button) findViewById(
                R.id.applySelectedCPUFrequenciesButton);

        _currentCPUFrequencyTextViewUpdate();
        _currentCPUMaxFrequencyTextViewUpdate();
        _currentCPUMinFrequencyTextViewUpdate();

        try {
            _readAvailableFrequencies();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            _readAvailableFrequenciesForSpinner();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            _readAvailableGovernorsForSpinner();
        } catch (Exception e) {
            e.printStackTrace();
        }

        _readCurrentGovernor();
    }

    private String _declareBaseMaximumFrequencyVariable() {
        return selectedMaximumFrequency = "";
    }

    private String _declareBaseMinimumFrequencyVariable() {
        return selectedMinimumFrequency = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cpu_info:
                Intent intent = new Intent(this, CPUInfoActivity.class);
                this.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public Context _context() {
        context = getApplicationContext();

        return context;
    }

    public String[] _readAvailableFrequencies() throws Exception {
        File scalingAvailableFrequenciesFile = new File(
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        availableFrequencies = new ArrayList<>();
        Scanner scanner = new Scanner(scalingAvailableFrequenciesFile);
        while (scanner.hasNext()) {
            availableFrequencies.add(scanner.next());
        }

        availableFrequenciesArray = new String[availableFrequencies.size()];
        availableFrequenciesArray = availableFrequencies.toArray(availableFrequenciesArray);

        return availableFrequenciesArray;
    }

    public ArrayList<String> _readAvailableFrequenciesForSpinner() throws Exception {
        File scalingAvailableFrequenciesFile = new File(
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        availableFrequenciesForSpinner = new ArrayList<>();
        Scanner scanner = new Scanner(scalingAvailableFrequenciesFile);
        while (scanner.hasNext()) {
            frequency = scanner.nextInt();
            switch (String.valueOf(frequency).length()) {
                case 6:
                    int frequencyInMHz = frequency / 1000;
                    String frequencyInMHzString = String.valueOf(frequencyInMHz);
                    availableFrequenciesForSpinner.add(frequencyInMHzString + " MHz");
                    break;
                case 7:
                    String frequencyInGHzString = String.valueOf(frequency);
                    double frequencyInGHzDouble = Double.valueOf(frequencyInGHzString);
                    double frequencyToGHz = frequencyInGHzDouble / 1000000;
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    String frequencyToGHzString = String.valueOf(df.format(frequencyToGHz));
                    availableFrequenciesForSpinner.add(frequencyToGHzString + " GHz");
            }
        }
        _selectMaximumFrequencySpinner(availableFrequenciesForSpinner);
        _selectMinimumFrequencySpinner(availableFrequenciesForSpinner);

        return availableFrequenciesForSpinner;
    }

    public void _readAvailableGovernorsForSpinner() throws Exception {
        File scalingAvailableGovernorsFile = new File(
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
        availableGovernorsForSpinner = new ArrayList<>();
        Scanner scanner4 = new Scanner(scalingAvailableGovernorsFile);
        while (scanner4.hasNext()) {
            governor = scanner4.next();
            availableGovernorsForSpinner.add(governor);
        }
        _selectGovernorSpinner(availableGovernorsForSpinner);
    }

    private String _selectGovernorSpinner(final List<String> availableGovernorsForSpinner) {
        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(this, R.layout.row_layout,
                availableGovernorsForSpinner);
        governorSpinner.setAdapter(adapter4);
        governorSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                                               long id) {
                        selectedGovernor = availableGovernorsForSpinner.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        return selectedGovernor;
    }

    public int _selectMaximumFrequencySpinner(final List<String> availableFrequenciesForSpinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.row_layout, availableFrequenciesForSpinner);
        maxFreqSpinner.setAdapter(adapter);
        maxFreqSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {
                        selectedRealMaximumFrequency = availableFrequenciesArray[arg2];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        onVisibleBehindCanceled();
                    }
                });

        return selectedMaxFreq;
    }

    public int _selectMinimumFrequencySpinner(final List<String> availableFrequenciesForSpinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.row_layout, availableFrequenciesForSpinner);
        minFreqSpinner.setAdapter(adapter);
        minFreqSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {
                        selectedRealMinimumFrequency = availableFrequenciesArray[arg2];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        onVisibleBehindCanceled();
                    }
                });

        return selectedMinFreq;
    }

    public void _applySelectedFrequencies(View view) {
        _checkForSelectedFrequencies();
    }

    public String _readCurrentCPUFrequency() {
        ProcessBuilder readOutCurrentCPUFrequency;
        String currentCPUFrequency = "";

        try {
            String[] currentCPUFrequencyFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"};
            readOutCurrentCPUFrequency = new ProcessBuilder(currentCPUFrequencyFile);
            Process readProcess = readOutCurrentCPUFrequency.start();
            InputStream readInCurrentCPUFrequency = readProcess.getInputStream();
            byte[] read = new byte[1024];
            while (readInCurrentCPUFrequency.read(read) != -1) {
                currentCPUFrequency = currentCPUFrequency + new String(read);
            }
            readInCurrentCPUFrequency.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return currentCPUFrequency;
    }

    public String _readCurrentMaxCPUFrequency() {
        ProcessBuilder readOutCurrentMaxCPUFrequency;
        String currentMaxCPUFrequency = "";

        try {
            String[] currentMaxCPUFrequencyFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"};
            readOutCurrentMaxCPUFrequency = new ProcessBuilder(currentMaxCPUFrequencyFile);
            Process readProcess2 = readOutCurrentMaxCPUFrequency.start();
            InputStream readInCurrentMaxCPUFrequency = readProcess2.getInputStream();
            byte[] read2 = new byte[1024];
            while (readInCurrentMaxCPUFrequency.read(read2) != -1) {
                currentMaxCPUFrequency = currentMaxCPUFrequency + new String(read2);
            }
            readInCurrentMaxCPUFrequency.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return currentMaxCPUFrequency;
    }

    public String _readCurrentMinCPUFrequency() {
        ProcessBuilder readOutCurrentMinCPUFrequency;
        String currentMinCPUFrequency = "";

        try {
            String[] currentMinCPUFrequencyFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"};
            readOutCurrentMinCPUFrequency = new ProcessBuilder(currentMinCPUFrequencyFile);
            Process process3 = readOutCurrentMinCPUFrequency.start();
            InputStream readInCurrentMinCPUFrequency = process3.getInputStream();
            byte[] read3 = new byte[1024];
            while (readInCurrentMinCPUFrequency.read(read3) != -1) {
                currentMinCPUFrequency = currentMinCPUFrequency + new String(read3);
            }
            readInCurrentMinCPUFrequency.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return currentMinCPUFrequency;
    }

    public void _currentCPUFrequencyTextViewUpdate() {
        final TextView currentCPUFrequencyTV = (TextView) findViewById(R.id.currentCPUFreq);
        Thread currentCPUFrequencyUpdate = new Thread() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        Thread.sleep(600);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String currentCPUFrequencyString = _readCurrentCPUFrequency();
                                String finalCurrentCPUFreq = currentCPUFrequencyString.trim();
                                currentCPUFrequencyTV.setText(finalCurrentCPUFreq + " KHz");
                            } //run()
                        }); //MainActivity.this
                    } //WHILE
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } //try-catch
            } //run()
        }; //thread
        currentCPUFrequencyUpdate.start();
    } //cpuFreqTextViewUpdate

    public void _currentCPUMaxFrequencyTextViewUpdate() {
        final TextView currentCPUMaxFrequencyTV = (TextView) findViewById(R.id.currentMaxCPUFreq);
        Thread currentCPUMaxFrequencyUpdate = new Thread() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        Thread.sleep(600);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String currentCPUMaxFrequencyString = _readCurrentMaxCPUFrequency();
                                String finalCurrentMaxCPUFreq = currentCPUMaxFrequencyString.trim();
                                currentCPUMaxFrequencyTV.setText(finalCurrentMaxCPUFreq + " KHz");
                            } //run()
                        }); //MainActivity.this
                    } //WHILE
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } //try-catch
            } //run()
        }; //thread
        currentCPUMaxFrequencyUpdate.start();
    }

    public void _currentCPUMinFrequencyTextViewUpdate() {
        final TextView currentCPUMinFrequencyTV = (TextView) findViewById(R.id.currentMinCPUFreq);
        Thread currentCPUMinFrequencyUpdate = new Thread() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        Thread.sleep(600);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String currentCPUMinFrequencyString = _readCurrentMinCPUFrequency();
                                String finalCurrentMinCPUFreq = currentCPUMinFrequencyString.trim();
                                currentCPUMinFrequencyTV.setText(finalCurrentMinCPUFreq + " KHz");
                            } //run()
                        }); //MainActivity.this
                    } //WHILE
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } //try-catch
            } //run()
        }; //thread
        currentCPUMinFrequencyUpdate.start();
    }

    public void _applySelectedGovernor(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hey you!");
        builder.setMessage("Are you sure? Do you want to set your selected governor?");
        builder.setPositiveButton("Yep, let it go!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    CommandCapture setGovernor = new CommandCapture(0,
                            "echo \"" + selectedGovernor +
                                    "\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
                    RootTools.getShell(true).add(setGovernor);
                } catch (RootDeniedException | IOException rde) {
                    rde.printStackTrace();
                    Toast.makeText(context, rde.getMessage(), Toast.LENGTH_LONG).show();
                } catch (TimeoutException te) {
                    te.printStackTrace();
                    Toast.makeText(context, te.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(context, "Set " + selectedGovernor + " governor",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Hell no! Step back!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

        _readCurrentGovernor();
    }

    public void _setCurrentGovernorTextLabel(String currentGovernor) {
        currentGovernorTV.setText(currentGovernor);
    }

    public String _readCurrentGovernor() {
        ProcessBuilder readOutCurrentGovernor;
        String currentGovernor = "";

        try {
            String[] currentGovernorFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"};
            readOutCurrentGovernor = new ProcessBuilder(currentGovernorFile);
            Process readProcess4 = readOutCurrentGovernor.start();
            InputStream readInCurrentGovernor = readProcess4.getInputStream();
            byte[] read4 = new byte[1024];
            while (readInCurrentGovernor.read(read4) != -1) {
                currentGovernor = currentGovernor + new String(read4);
            }
            readInCurrentGovernor.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        _setCurrentGovernorTextLabel(currentGovernor);
        return currentGovernor;
    }

    @SuppressWarnings("UnusedParameters")
    public String _selectMaximumCPUFrequencyDialog(View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select maximum CPU frequency:");
        final ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<>(context, R.layout.row_layout,
                availableFrequenciesForSpinner);
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedMaximumFrequency = availableFrequenciesArray[which];
                        Button selectMaximumCPUFrequencyButton = (Button) findViewById(
                                R.id.selectMaximumFrequencyButton);
                        String selectedMaximumValueWhich = availableFrequenciesForSpinner.get(which);
                        selectMaximumCPUFrequencyButton.setText(selectedMaximumValueWhich);
                    }
                });
        builderSingle.show();

        return selectedMaximumFrequency;
    }

    @SuppressWarnings("UnusedParameters")
    public String _selectMinimumCPUFrequencyDialog(View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select minimum CPU frequency:");
        final ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<>(context, R.layout.row_layout,
                availableFrequenciesForSpinner);
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedMinimumFrequency = availableFrequenciesArray[which];
                        Button selectMinimumCPUFrequencyButton = (Button) findViewById(
                                R.id.selectMinimumFrequencyButton);
                        String selectedMinimumValieWhich = availableFrequenciesForSpinner.get(
                                which);
                        selectMinimumCPUFrequencyButton.setText(selectedMinimumValieWhich);
                    }
                });
        builderSingle.show();

        return selectedMinimumFrequency;
    }

    public void _checkForSelectedFrequencies() {
        /*if (selectedMaximumFrequency.equals("")) {
            applySelectedCPUFrequencyButton.setText("Please select maximum first!");
        } if (selectedMinimumFrequency.equals("")) {
            applySelectedCPUFrequencyButton.setText("Please select minimum first!");
        } else {
            Toast.makeText(context, "Set " + selectedMaximumFrequency +
                    " as maximum frequency\nSet " + selectedMinimumFrequency +
                    " as minimum frequency", Toast.LENGTH_LONG).show();
        }*/
        if (selectedMaximumFrequency.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hell no!");
            builder.setMessage("Select maximum frequency first!");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else {
            _checkMinimumVariable();
        }
    }

    private void _checkMinimumVariable() {
        if (selectedMinimumFrequency.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hell no!");
            builder.setMessage("Select minimum frequency first!");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Hey you!");
            builder.setMessage("Are you sure? Do you want to set your selected CPU frequencies?");
            builder.setPositiveButton("Yep, let it go!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        CommandCapture setMaximumCPUFrequency = new CommandCapture(0,
                                "echo \"" + selectedMaximumFrequency +
                                        "\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
                        RootTools.getShell(true).add(setMaximumCPUFrequency);
                    } catch (RootDeniedException | IOException rde) {
                        rde.printStackTrace();
                        Toast.makeText(context, rde.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (TimeoutException te) {
                        te.printStackTrace();
                        Toast.makeText(context, te.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        CommandCapture setMinimumCPUFrequency = new CommandCapture(0,
                                "echo \"" + selectedMinimumFrequency +
                                        "\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
                        RootTools.getShell(true).add(setMinimumCPUFrequency);
                    } catch (RootDeniedException | IOException rde) {
                        rde.printStackTrace();
                        Toast.makeText(context, rde.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (TimeoutException te) {
                        te.printStackTrace();
                        Toast.makeText(context, te.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(context, "Set " + selectedMaximumFrequency +
                            " as maximum frequency\nSet " + selectedMinimumFrequency +
                            " as minimum frequency", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Hell no! Step back!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
}

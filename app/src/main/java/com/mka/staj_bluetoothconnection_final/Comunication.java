package com.mka.staj_bluetoothconnection_final;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Comunication extends AppCompatActivity {

    LineChart mpLineChart, mp_2_LineChart; // declaring line chart
    ArrayList<Entry> dataVals = new ArrayList<Entry>();

    ArrayList<Entry> dataVals2 = new ArrayList<Entry>();
    ArrayList<Entry> dataVals3 = new ArrayList<Entry>();
    ArrayList<Entry> dataValsPast = new ArrayList<Entry>();
    int pastCounter=0;
    int temparatureDataCounter = 0;
    int humunityDataCounter = 0;
    int valueCounter = 0;
    float receivedFloatData;
    float receivedFloatTemperatureData, receivedFloatHumunityData;
    float AlertingValue, AlertingHumunityValue, AlertingTemperatureValue;

    SharedPreferences sharedPref;


    String globalData;
    boolean selectedGraph = false;
    String currentDateTimeString;

    DataSource dataSource;

    private final String CHANNEL_ID = "bambam";
    private final int NOTIFICATION_ID = 001;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    BluetoothDevice remoteDevice;
    BluetoothServerSocket mService;
    Button ledOn, LedOf, getdataButton, temperatureButton, TemperatureGraphButton, historyButton, HumunityGraphButton;

    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;


    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView dateTextview;
    Context context = this;
    private Toolbar toolbar;

    static final int[] receivedDateArray = new int[6];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunication);
        Intent newintent = getIntent();
        address = newintent.getStringExtra(MainActivity.Extra_ADRESS);


        ledOn = findViewById(R.id.openLedID);
        getdataButton = findViewById(R.id.getDataButtonID);
        TemperatureGraphButton = findViewById(R.id.graphButtonID);


        HumunityGraphButton = findViewById(R.id.humunityGraphID);
        // <toolbar ending>
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // </ toolbar ending>1
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        AlertingTemperatureValue = sharedPref.getFloat("AlertingTemperatureValue", (float) 30.00);
        Toast.makeText(getApplicationContext(), "Current Temperature Alert Value " + AlertingTemperatureValue, Toast.LENGTH_SHORT).show();

        AlertingHumunityValue = sharedPref.getFloat("AlertingHumunityValue", (float) 30.00);
        Toast.makeText(getApplicationContext(), "Current Humudity Alert Value " + AlertingHumunityValue, Toast.LENGTH_SHORT).show();


        mpLineChart = (LineChart) findViewById(R.id.line_chart);
        //mp_2_LineChart = (LineChart) findViewById(R.id.line_chart);

        dateTextview = findViewById(R.id.dateTextID);
        // <data base>
        dataSource = new DataSource(this);
        dataSource.open();
        // dataSource.clear();

        // </data base>
        TemperatureGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // DEGİŞİM BURDA BASLADI
                selectedGraph = true;//

            }
        });
        HumunityGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedGraph = false;//

            }
        });

        getdataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdataButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                if (btSocket != null) {
                    try {
                        mmInputStream = btSocket.getInputStream();
                        beginListenForData();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        ledOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mpLineChart.setVisibility(View.INVISIBLE);
                if (btSocket != null) {
                    try {
                        btSocket.getOutputStream().write("2".toString().getBytes());
                        dateTextview.setText("LED IS ON");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        new BTbaglan().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notification) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View mView = getLayoutInflater().inflate(R.layout.notification, null);


            NumberPicker myNumberPicker = (NumberPicker) mView.findViewById(R.id.numberPickerID);
            myNumberPicker.setMinValue(1);
            myNumberPicker.setMaxValue(100);

            NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    //Toast.makeText(getApplicationContext(), i1+" ", Toast.LENGTH_SHORT).show();
                    //dateTextview.setText(""+i1);

                    // Toast.makeText(getApplicationContext(),"Selected Radio Button: "+radioButton.getText(),Toast.LENGTH_LONG).show();

                    AlertingValue = (float) i1;

                }
            };
            myNumberPicker.setOnValueChangedListener(onValueChangeListener);
            TextView okeyTextView = mView.findViewById(R.id.okeyTextID);
            okeyTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RadioGroup radioGroup = mView.findViewById(R.id.radioGroupID);
                    int radioID = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = mView.findViewById(radioID);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (radioButton.getText().equals("Temperature")) {
                        AlertingTemperatureValue = AlertingValue;

                        editor.putFloat("AlertingTemperatureValue", AlertingTemperatureValue);
                        //editor.putInt("intValue",value); //int değer kayıt eklemek için kullanıyoruz.
                        //editor.putString("stringValue",stringValue); //string değer kayıt etmek için kullanıyoruz.
                        editor.commit(); //Kayıt.
                        Toast.makeText(getApplicationContext(), "Temperature Notification Value is Updated( " + AlertingTemperatureValue + " )", Toast.LENGTH_LONG).show();

                    }

                    if (radioButton.getText().equals("Humudity")) {
                        AlertingHumunityValue = AlertingValue;
                        editor.putFloat("AlertingHumunityValue", AlertingHumunityValue);
                        editor.commit(); //Kayıt.

                        Toast.makeText(getApplicationContext(), "Humudity Notification Value is Updated( " + AlertingHumunityValue + " )", Toast.LENGTH_LONG).show();
                    }


                }
            });
            TextView cancelTextView = mView.findViewById(R.id.cancelTextID);

            builder.setView(mView);
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        if (id == R.id.action_delete) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Deleting Data Base");
            alert.setMessage("Are you sure?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "Datas Deleted", Toast.LENGTH_SHORT).show();

                }

            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                }
            });
            alert.create().show();

        }


        if (id == R.id.action_past) {
            Calendar nowTıme = Calendar.getInstance();
            int year = nowTıme.get(Calendar.YEAR);
            int month = nowTıme.get(Calendar.MONTH);
            int day = nowTıme.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    System.out.println(i + " " + i1 + " " + i2);
                    receivedDateArray[3]=i; receivedDateArray[4]=i1; receivedDateArray[5]=i2;
                    Toast.makeText(getApplicationContext(), i + " " + i1 + " " + i2, Toast.LENGTH_SHORT).show();
                    showPastGraph(receivedDateArray,selectedGraph);

                }
            },year, month ,day  );

            datePickerDialog.setTitle("Finish date of chart to display");
            datePickerDialog.show();
            DatePickerDialog datePickerDialog2 = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    System.out.println(i + " " + i1 + " " + i2);
                    receivedDateArray[0]=i; receivedDateArray[1]=i1; receivedDateArray[2]=i2;
                    Toast.makeText(getApplicationContext(), i + " " + i1 + " " + i2, Toast.LENGTH_SHORT).show();


                }
            }, year, month, day);

            datePickerDialog2.setTitle("Start date of chart to display");
            datePickerDialog2.show();



        }


        return true;
    }
    private void showPastGraph(int[] dateArray,boolean selectedGraph)
    {

        if(selectedGraph)
        {
            System.out.println(selectedGraph);
            ArrayList<Sensor> receivedSensorsArray = dataSource.Overlistele("T"); // database
            for(int i=0 ; i< receivedSensorsArray.size() ; ++i)     // database
            {
                int[] array = new int[3];
                //System.out.println(receivedSensorsArray.get(i).getSensorName()+" "+receivedSensorsArray.get(i).getSensorValue()+" "+receivedSensorsArray.get(i).getCurrentDateTimeString());
                String temp =receivedSensorsArray.get(i).getCurrentDateTimeString();
                String[] parts = temp.split(" ");
                switch(parts[0]) {
                    case "Jan" :
                        array[1]=1;
                        break; // optional

                    case "Feb" :
                        array[1]=2;
                        break; // optional
                    case "Mar" :
                        array[1]=3;
                        break; // optional
                    case "Apr" :
                        array[1]=4;
                        break; // optional
                    case "May" :
                        array[1]=5;
                        break; // optional
                    case "Jun" :
                        array[1]=6;
                        break; // optional
                    case "Jul" :
                        array[1]=7;
                        break; // optional
                    case "Aug" :
                        array[1]=8;
                        break; // optional
                    case "Sep" :
                        array[1]=9;
                        break; // optional
                    case "Oct" :
                        array[1]=10;
                        break; // optional
                    case "Nov" :
                        array[1]=11;
                        break; // optional
                    case "Dec" :
                        array[1]=12;
                        break; // optional

                    // You can have any number of case statements.
                    default : // Optional
                        // Statements
                }
                parts[1] = parts[1].substring(0, parts[1].length() - 1);
                array[2]=Integer.parseInt(parts[1]);
                array[0] = Integer.parseInt(parts[2]);
                String check = Integer.toString(array[0])+Integer.toString(array[1])+Integer.toString(array[2]);
                String check2 = Integer.toString(dateArray[0])+Integer.toString(dateArray[1]+1)+Integer.toString(dateArray[2]);
                String check3 = Integer.toString(dateArray[3])+Integer.toString(dateArray[4]+1)+Integer.toString(dateArray[5]);
                System.out.println(check+" "+check2+" "+check3);
                if(Integer.parseInt(check)>= Integer.parseInt(check2) && Integer.parseInt(check)<= Integer.parseInt(check3) )
                {
                    dataValsPast.add(new Entry(pastCounter, receivedSensorsArray.get(i).getSensorValue()));
                   System.out.println(pastCounter+" ,"+dataValsPast.get(pastCounter));
                    pastCounter++;

                }



            }

            pastCounter=0;
            mpLineChart.setVisibility(View.VISIBLE);

            // < editing line chart >
            mpLineChart.setDrawBorders(true);
            mpLineChart.setBorderColor(Color.BLUE);
            mpLineChart.setBorderWidth(2);
            mpLineChart.setNoDataTextColor(Color.BLACK);
            // </ editing line chart>

            LineDataSet lineDataSet1 = new LineDataSet(dataValsPast, "Temperature");
            // <editing lines>
            System.out.println(lineDataSet1.getEntryCount());
            lineDataSet1.setLineWidth(2);
            lineDataSet1.setColor(Color.BLACK);
            lineDataSet1.setDrawCircles(true);
            lineDataSet1.setDrawCircleHole(true);
            lineDataSet1.setCircleColor(Color.BLACK);
            lineDataSet1.setCircleHoleColor(Color.BLUE);
            lineDataSet1.setCircleRadius(3);
            lineDataSet1.setCircleHoleRadius(2);
            lineDataSet1.setValueTextColor(Color.GRAY);
            lineDataSet1.setValueTextSize(10);
            lineDataSet1.setDrawValues(true);
            // </ editing lines >
            ArrayList<ILineDataSet> dataSets2 = new ArrayList<>();
            dataSets2.add(lineDataSet1);
            LineData data2 = new LineData(dataSets2);
            mpLineChart.setData(data2);
            mpLineChart.invalidate();



        }
        else{
            System.out.println(selectedGraph);
            ArrayList<Sensor> receivedSensorsArray = dataSource.Overlistele("H"); // database
            for(int i=0 ; i< receivedSensorsArray.size() ; ++i)     // database
            {
                int[] array = new int[3];
                //System.out.println(receivedSensorsArray.get(i).getSensorName()+" "+receivedSensorsArray.get(i).getSensorValue()+" "+receivedSensorsArray.get(i).getCurrentDateTimeString());
                String temp =receivedSensorsArray.get(i).getCurrentDateTimeString();
                String[] parts = temp.split(" ");
                switch(parts[0]) {
                    case "Jan" :
                        array[1]=1;
                        break; // optional

                    case "Feb" :
                        array[1]=2;
                        break; // optional
                    case "Mar" :
                        array[1]=3;
                        break; // optional
                    case "Apr" :
                        array[1]=4;
                        break; // optional
                    case "May" :
                        array[1]=5;
                        break; // optional
                    case "Jun" :
                        array[1]=6;
                        break; // optional
                    case "Jul" :
                        array[1]=7;
                        break; // optional
                    case "Aug" :
                        array[1]=8;
                        break; // optional
                    case "Sep" :
                        array[1]=9;
                        break; // optional
                    case "Oct" :
                        array[1]=10;
                        break; // optional
                    case "Nov" :
                        array[1]=11;
                        break; // optional
                    case "Dec" :
                        array[1]=12;
                        break; // optional

                    // You can have any number of case statements.
                    default : // Optional
                        // Statements
                }
                parts[1] = parts[1].substring(0, parts[1].length() - 1);
                array[2]=Integer.parseInt(parts[1]);
                array[0] = Integer.parseInt(parts[2]);
                String check = Integer.toString(array[0])+Integer.toString(array[1])+Integer.toString(array[2]);
                String check2 = Integer.toString(dateArray[0])+Integer.toString(dateArray[1]+1)+Integer.toString(dateArray[2]);
                String check3 = Integer.toString(dateArray[3])+Integer.toString(dateArray[4]+1)+Integer.toString(dateArray[5]);
                System.out.println(check+" "+check2+" "+check3);
                if(Integer.parseInt(check)>= Integer.parseInt(check2) && Integer.parseInt(check)<= Integer.parseInt(check3) )
                {
                    dataValsPast.add(new Entry(pastCounter, receivedSensorsArray.get(i).getSensorValue()));
                    System.out.println(receivedSensorsArray.get(i).getSensorName()+" "+receivedSensorsArray.get(i).currentDateTimeString);
                    pastCounter++;
                }

            }
            pastCounter=0;
            mpLineChart.setVisibility(View.VISIBLE);

            // < editing line chart >
            mpLineChart.setDrawBorders(true);
            mpLineChart.setBorderColor(Color.BLUE);
            mpLineChart.setBorderWidth(2);
            mpLineChart.setNoDataTextColor(Color.BLACK);
            // </ editing line chart>

            LineDataSet lineDataSet1 = new LineDataSet(dataValsPast, "Huminity");
            // <editing lines>

            lineDataSet1.setLineWidth(2);
            lineDataSet1.setColor(Color.BLACK);
            lineDataSet1.setDrawCircles(true);
            lineDataSet1.setDrawCircleHole(true);
            lineDataSet1.setCircleColor(Color.BLACK);
            lineDataSet1.setCircleHoleColor(Color.BLUE);
            lineDataSet1.setCircleRadius(3);
            lineDataSet1.setCircleHoleRadius(2);
            lineDataSet1.setValueTextColor(Color.GRAY);
            lineDataSet1.setValueTextSize(10);
            lineDataSet1.setDrawValues(false);
            // </ editing lines >
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet1);
            LineData data = new LineData(dataSets);
            mpLineChart.setData(data);
            mpLineChart.invalidate();



        }
        dataValsPast.clear();


    }

    private void showGraph(boolean selectedGraph) {
        // <dating>
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        dateTextview.setText("Last Update: " + currentDateTimeString);
        // </ dating>

        if (selectedGraph) {
            mpLineChart.setVisibility(View.VISIBLE);

            // < editing line chart>
            mpLineChart.setDrawBorders(true);
            mpLineChart.setBorderColor(Color.BLUE);
            mpLineChart.setBorderWidth(2);
            mpLineChart.setNoDataTextColor(Color.BLACK);
            // </ editing line chart>

            LineDataSet lineDataSet1 = new LineDataSet(dataVals2, "Temperature");
            // <editing lines>
            lineDataSet1.setLineWidth(2);
            lineDataSet1.setColor(Color.BLACK);
            lineDataSet1.setDrawCircles(true);
            lineDataSet1.setDrawCircleHole(true);
            lineDataSet1.setCircleColor(Color.BLACK);
            lineDataSet1.setCircleHoleColor(Color.BLUE);
            lineDataSet1.setCircleRadius(3);
            lineDataSet1.setCircleHoleRadius(2);
            lineDataSet1.setValueTextColor(Color.GRAY);
            lineDataSet1.setValueTextSize(10);
            lineDataSet1.setDrawValues(false);
            // </ editing lines >
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet1);
            LineData data = new LineData(dataSets);
            mpLineChart.setData(data);
            mpLineChart.invalidate();
        } else {
            mpLineChart.setVisibility(View.VISIBLE);

            // < editing line chart >
            mpLineChart.setDrawBorders(true);
            mpLineChart.setBorderColor(Color.BLUE);
            mpLineChart.setBorderWidth(2);
            mpLineChart.setNoDataTextColor(Color.BLACK);
            // </ editing line chart>

            LineDataSet lineDataSet1 = new LineDataSet(dataVals3, "Huminity");
            // <editing lines>

            lineDataSet1.setLineWidth(2);
            lineDataSet1.setColor(Color.BLACK);
            lineDataSet1.setDrawCircles(true);
            lineDataSet1.setDrawCircleHole(true);
            lineDataSet1.setCircleColor(Color.BLACK);
            lineDataSet1.setCircleHoleColor(Color.BLUE);
            lineDataSet1.setCircleRadius(3);
            lineDataSet1.setCircleHoleRadius(2);
            lineDataSet1.setValueTextColor(Color.GRAY);
            lineDataSet1.setValueTextSize(10);
            lineDataSet1.setDrawValues(false);
            // </ editing lines >
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet1);
            LineData data = new LineData(dataSets);
            mpLineChart.setData(data);
            mpLineChart.invalidate();
        }

    }

    private void showNotification(float receivedFloatAlertData, String key) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_priority_high_black_24dp);
        builder.setContentTitle("Caution!");
        if (key == "K") {
            builder.setContentText("High temperature value: " + receivedFloatAlertData + " C");
        }
        if (key == "H") {
            builder.setContentText("High humunity value: " + "% " + receivedFloatAlertData);
        }
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void dataValues1() {


        System.out.println(globalData);
        JSONObject mainObject = null;

        try {
            mainObject = new JSONObject(globalData);
            {
                String temperatureValue = mainObject.getString("temperature");
                receivedFloatTemperatureData = Float.parseFloat(temperatureValue);
                if (receivedFloatTemperatureData > AlertingTemperatureValue) {
                    showNotification(receivedFloatTemperatureData, "K");
                }
                if (temparatureDataCounter == 0) {
                    dataVals2.add(new Entry(temparatureDataCounter, receivedFloatTemperatureData));
                    temparatureDataCounter++;
                    currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    /*
                    currentDateTimeString = String.valueOf(System.currentTimeMillis());
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(currentDateTimeString));
                    cal.get(Calendar.HOUR_OF_DAY);
                    */
                    Sensor receivedSensor = new Sensor("temperature", receivedFloatTemperatureData, currentDateTimeString); //database
                    dataSource.createSensor(receivedSensor);          // database

                } else if (receivedFloatTemperatureData == dataVals2.get(temparatureDataCounter - 1).getY()) {
                } else {
                    dataVals2.add(new Entry(temparatureDataCounter, receivedFloatTemperatureData));
                    temparatureDataCounter++;
                    currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    Sensor receivedSensor = new Sensor("temperature", receivedFloatTemperatureData, currentDateTimeString); //database
                    dataSource.createSensor(receivedSensor);          // database
                }


            }
            {
                String humunityValue = mainObject.getString("humunity");
                receivedFloatHumunityData = Float.parseFloat(humunityValue);
                if (receivedFloatHumunityData > AlertingHumunityValue) {
                    showNotification(receivedFloatHumunityData, "H");
                }
                if (humunityDataCounter == 0) {
                    dataVals3.add(new Entry(humunityDataCounter, receivedFloatHumunityData));
                    humunityDataCounter++;
                    currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    Sensor receivedSensor = new Sensor("humunity", receivedFloatHumunityData, currentDateTimeString); //database
                    dataSource.createSensor(receivedSensor);          // database
                }
                if (receivedFloatHumunityData == dataVals3.get(humunityDataCounter - 1).getY()) {
                } else {
                    dataVals3.add(new Entry(humunityDataCounter, receivedFloatHumunityData));
                    humunityDataCounter++;
                    currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    Sensor receivedSensor = new Sensor("humunity", receivedFloatHumunityData, currentDateTimeString); //database
                    dataSource.createSensor(receivedSensor);          // database
                }

            }
            /*
            ArrayList<Sensor> receivedSensorsArray = dataSource.listele(); // database

            for(int i=0 ; i< receivedSensorsArray.size() ; ++i)     // database
            {
                System.out.println(receivedSensorsArray.get(i).getSensorName()+" "+receivedSensorsArray.get(i).getSensorValue()+" "+receivedSensorsArray.get(i).getCurrentDateTimeString());
            }
            */
            showGraph(selectedGraph);


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    void beginListenForData() // Getting data from remote bluetooth device
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {

                                            globalData = data;
                                            dataValues1(); // for adding datas to Arraylist;

                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();

            } catch (IOException e) {
                //msg("Error");

            }

        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Disconnect();
    }


    private class BTbaglan extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Comunication.this, "Connecting...", "Please Wait");
        }

        // https://gelecegiyazanlar.turkcell.com.tr/konu/android/egitim/android-301/asynctask
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice cihaz = myBluetooth.getRemoteDevice(address);
                    btSocket = cihaz.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();

                }

            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {

                Toast.makeText(getApplicationContext(), "Connection Error, Try Again.", Toast.LENGTH_SHORT).show();
                finish();
            } else {

                Toast.makeText(getApplicationContext(), "Connection Successful", Toast.LENGTH_SHORT).show();

                isBtConnected = true;
            }
            progress.dismiss();
        }

    }
}
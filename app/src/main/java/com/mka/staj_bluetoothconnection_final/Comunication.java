package com.mka.staj_bluetoothconnection_final;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Comunication extends AppCompatActivity {

    LineChart mpLineChart, mp_2_LineChart; // declaring line chart
    ArrayList<Entry> dataVals = new ArrayList<Entry>();

    ArrayList<Entry> dataVals2 =new ArrayList<Entry>();
    ArrayList<Entry> dataVals3 =new ArrayList<Entry>();
    int cdata=0;
    int cdata2=0;
    int valueCounter = 0;
    float receivedFloatData;
    String globalData;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    BluetoothDevice remoteDevice;
    BluetoothServerSocket mService;
    Button ledOn, LedOf, getdataButton, temperatureButton, TemperatureGraphButton, humunityButton, HumunityGraphButton;
    TextView textView;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;


    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView dateTextview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunication);
        Intent newintent = getIntent();
        address = newintent.getStringExtra(MainActivity.Extra_ADRESS);
        textView = findViewById(R.id.receivedDataTextID);
        temperatureButton = findViewById(R.id.temperatureID);
        ledOn = findViewById(R.id.openLedID);
        getdataButton = findViewById(R.id.getDataButtonID);
        TemperatureGraphButton = findViewById(R.id.graphButtonID);
        humunityButton = findViewById(R.id.humunityID);
        HumunityGraphButton = findViewById(R.id.humunityGraphID);

        mpLineChart = (LineChart) findViewById(R.id.line_chart);
        //mp_2_LineChart = (LineChart) findViewById(R.id.line_chart);

        dateTextview = findViewById(R.id.dateTextID);
        // <dating>
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dateTextview.setText(date);
        // </ dating>


        TemperatureGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // dataVals2.addAll(dataVals);
                cdata=dataVals2.size();
                for(int i=0 ; i< dataVals.size() ; i++)
                {

                    dataVals2.add(new Entry(cdata, dataVals.get(i).getY()));
                    cdata++;
                }

                dataVals.clear(); // clearing ArrayList of other sensor values
                valueCounter = 0;// declaring 0 for new begin

                for(int i=0 ; i< dataVals2.size() ; ++i){
                    System.out.println(i+" ,"+dataVals2.get(i));
                }




                mpLineChart.setVisibility(View.VISIBLE);
                textView.setText("Graph of datas until now");
                //mpLineChart.setBackgroundColor(Color.BLACK);          //< editing line chart >
                // mpLineChart.setNoDataText("No Data");
                //mpLineChart.setDrawGridBackground(true);
                mpLineChart.setDrawBorders(true);
                mpLineChart.setBorderColor(Color.BLUE);
                mpLineChart.setBorderWidth(2);
                // </ editing line chart>

                LineDataSet lineDataSet1 = new LineDataSet(dataVals2, "Temperature");
                // editing lines
                lineDataSet1.setLineWidth(2);
                lineDataSet1.setColor(Color.BLACK);
                lineDataSet1.setDrawCircles(true);
                lineDataSet1.setDrawCircleHole(true);
                lineDataSet1.setCircleColor(Color.BLUE);
                lineDataSet1.setCircleHoleColor(Color.BLACK);
                lineDataSet1.setCircleRadius(5);
                lineDataSet1.setCircleHoleRadius(3);
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
        });
        HumunityGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cdata=dataVals3.size();
                for(int i=0 ; i< dataVals.size() ; i++)
                {

                    dataVals3.add(new Entry(cdata, dataVals.get(i).getY()));
                    cdata++;
                }

                dataVals.clear(); // clearing ArrayList of other sensor values
                valueCounter = 0;// declaring 0 for new begin

                for(int i=0 ; i< dataVals3.size() ; ++i){
                    System.out.println(i+" ,"+dataVals3.get(i));
                }
                mpLineChart.setVisibility(View.VISIBLE);
                textView.setText("Graph of datas until now");
                //mpLineChart.setBackgroundColor(Color.BLACK);          //< editing line chart >
                // mpLineChart.setNoDataText("No Data");
                //mpLineChart.setDrawGridBackground(true);
                mpLineChart.setDrawBorders(true);
                mpLineChart.setBorderColor(Color.BLUE);
                mpLineChart.setBorderWidth(2);
                // </ editing line chart>

                LineDataSet lineDataSet1 = new LineDataSet(dataVals3, "Humunity");
                // editing lines
                lineDataSet1.setLineWidth(2);
                lineDataSet1.setColor(Color.BLACK);
                lineDataSet1.setDrawCircles(true);
                lineDataSet1.setDrawCircleHole(true);
                lineDataSet1.setCircleColor(Color.BLUE);
                lineDataSet1.setCircleHoleColor(Color.BLACK);
                lineDataSet1.setCircleRadius(5);
                lineDataSet1.setCircleHoleRadius(3);
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
        humunityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dataVals.clear(); // clearing ArrayList of other sensor values
                valueCounter = 0; // declaring 0 for new begin

                if (btSocket != null) {
                    try {
                        if (isBtConnected)
                            btSocket.getOutputStream().write("3".toString().getBytes());
                        else
                            Toast.makeText(getApplicationContext(), "Connection is Broken", Toast.LENGTH_LONG).show();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
        temperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dataVals.clear(); // clearing ArrayList of other sensor values
                valueCounter = 0; // declaring 0 for new begin
                if (btSocket != null) {
                    try {
                        if (isBtConnected)
                            btSocket.getOutputStream().write("1".toString().getBytes());
                        else
                            Toast.makeText(getApplicationContext(), "Connection is Broken", Toast.LENGTH_LONG).show();


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
                        textView.setText("LED IS ON");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        new BTbaglan().execute();

    }

    private void dataValues1() {
        receivedFloatData = Float.parseFloat(globalData);
        dataVals.add(new Entry(valueCounter, receivedFloatData));
        valueCounter++;


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
                                            textView.setText("Current Data:" + data);
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
                // msg("Baglantı Hatası, Lütfen Tekrar Deneyin");
                Toast.makeText(getApplicationContext(), "Connection Error, Try Again.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                //   msg("Baglantı Basarılı");
                Toast.makeText(getApplicationContext(), "Connection Successful", Toast.LENGTH_SHORT).show();

                isBtConnected = true;
            }
            progress.dismiss();
        }

    }
}

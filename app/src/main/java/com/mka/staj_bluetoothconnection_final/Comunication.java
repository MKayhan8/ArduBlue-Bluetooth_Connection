package com.mka.staj_bluetoothconnection_final;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class Comunication extends AppCompatActivity {

    String address=null;
    private ProgressDialog  progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket =null;
    BluetoothDevice remoteDevice;
    BluetoothServerSocket mService;
    Button ledOn,LedOf,getdataButton;
    TextView textView;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;


    private boolean isBtConnected = false;
    static final UUID myUUID =UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunication);
        Intent newintent=getIntent();
        address = newintent.getStringExtra(MainActivity.Extra_ADRESS);
        textView = findViewById(R.id.receivedDataTextID);
        ledOn = findViewById(R.id.openLedID);
        LedOf = findViewById(R.id.closeLedID);
        getdataButton = findViewById(R.id.getDataButtonID);
        getdataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdataButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                if(btSocket != null)
                {
                    try {
                        mmInputStream=btSocket.getInputStream();
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
               // ledOn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                if(btSocket != null)
                {
                    try {
                        btSocket.getOutputStream().write("1".toString().getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        LedOf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btSocket != null)
                {
                    try {
                        btSocket.getOutputStream().write("2".toString().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        new BTbaglan().execute();

    }

    void beginListenForData() // Gettin data from remote bluetooth device
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            textView.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
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
            progress = ProgressDialog.show(Comunication.this, "Baglanıyor...", "Lütfen Bekleyin");
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
                Toast.makeText(getApplicationContext(), "Bağlantı Hatası Tekrar Deneyin", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                //   msg("Baglantı Basarılı");
                Toast.makeText(getApplicationContext(), "Bağlantı Başarılı", Toast.LENGTH_SHORT).show();

                isBtConnected = true;
            }
            progress.dismiss();
        }

    }
}

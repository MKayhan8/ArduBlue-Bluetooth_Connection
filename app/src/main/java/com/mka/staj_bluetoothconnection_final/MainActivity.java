package com.mka.staj_bluetoothconnection_final;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button buttonON,buttonOFF;
    BluetoothAdapter myBluetoothAdapter;

    Intent btenablingIntent;
    int requestCodeForeEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonON = findViewById(R.id.openButtonID);
        buttonOFF = findViewById(R.id.closeButtonID);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Uygulamanın yüklü olduğu cihazda Bluetooth mı yok mu ?..

        btenablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForeEnable=1;

        bluetoothONMethod();
        bluetoothOFMethod();
    }

    private void bluetoothOFMethod() {
        buttonOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myBluetoothAdapter.isEnabled())
                {
                    myBluetoothAdapter.disable();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == requestCodeForeEnable)
        {
            if(resultCode == RESULT_OK)
            {
                Toast.makeText(getApplicationContext(),"Bluetooth is Enable",Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(),"Bluetooth Enabling is Cancelled",Toast.LENGTH_LONG).show();
            }

        }

    }

    private void bluetoothONMethod() {
        buttonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myBluetoothAdapter == null)
                {
                    Toast.makeText(getApplicationContext(),"Bluetooth does not support on this Device",Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(!myBluetoothAdapter.isEnabled())
                    {
                        startActivityForResult(btenablingIntent,requestCodeForeEnable);
                    }

                }
            }
        });

    }
}

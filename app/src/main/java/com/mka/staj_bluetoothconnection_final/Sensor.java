package com.mka.staj_bluetoothconnection_final;

public class Sensor {
    String sensorName;
   float  sensorValue;
    String currentDateTimeString;



    public Sensor(String sensorName , float sensorValue, String currentDateTimeString ) {
        this.sensorName = sensorName;
        this.sensorValue = sensorValue;
        this.currentDateTimeString = currentDateTimeString;
    }

    public String getCurrentDateTimeString() {
        return currentDateTimeString;
    }

    public void setCurrentDateTimeString(String currentDateTimeString) {
        this.currentDateTimeString = currentDateTimeString;
    }
    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public float getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(float sensorValue) {
        this.sensorValue = sensorValue;
    }
}

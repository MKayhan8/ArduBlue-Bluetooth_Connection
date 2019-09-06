package com.mka.staj_bluetoothconnection_final;

public class Sensor {
    String sensorName;
    float  sensorValue;

    public Sensor(String sensorName , float sensorValue) {
        this.sensorName = sensorName;
        this.sensorValue = sensorValue;
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

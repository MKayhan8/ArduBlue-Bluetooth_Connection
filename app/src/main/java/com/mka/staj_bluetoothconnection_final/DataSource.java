package com.mka.staj_bluetoothconnection_final;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DataSource {
    SQLiteDatabase db;
    SQLite_Level  mydb;

    public DataSource(Context context)
    {
        mydb = new SQLite_Level(context);
    }
    public void open()
    {
        db = mydb.getWritableDatabase(); // read and write
    }
    public void close()
    {
        mydb.close();
    }
    public void clear ()
    {
        db.execSQL("delete  from Sensor" );
    }



    public void createSensor(Sensor sensor)
    {
       //Sensor s =new  Sensor("Temperature", 35.70f);
        ContentValues val = new ContentValues();
        val.put("sensorName",sensor.getSensorName());
        val.put("sensorValue",sensor.getSensorValue());
        val.put("currentDateTimeString",sensor.getCurrentDateTimeString());
        db.insert("Sensor",null,val);
    }

    public ArrayList<Sensor> listele()
    {
        String columns[] = {"sensorName","sensorValue","currentDateTimeString"};
        Cursor c = db.query("Sensor",columns,null,null,null,null,null);
        c.moveToFirst();
        ArrayList<Sensor> arrayList = new ArrayList<Sensor>();
        while(!c.isAfterLast())
        {
            String sensorName = c.getString(0);
            float sensorValue = c.getFloat(1);
            String sensorDate = c.getString(2);
            Sensor s = new Sensor(sensorName,sensorValue,sensorDate);
            arrayList.add(s);
            c.moveToNext();
        }
        return  arrayList;
    }
}

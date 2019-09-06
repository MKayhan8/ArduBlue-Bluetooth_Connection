package com.mka.staj_bluetoothconnection_final;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite_Level extends SQLiteOpenHelper {
    private static Context myContext;

    public SQLite_Level(Context context)
    {
        super(context,"Sensor",null,1);
    }



    public void onCreate(SQLiteDatabase db)
    {

        String sql =" create table Sensor ( sensorName String , sensorValue float , currentDateTimeString String )";
        db.execSQL(sql);

    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion)
    {
        db.execSQL("drop table if exists Sensor");

    }




}

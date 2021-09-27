package com.example.metroapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "metro")
public class station {
    @PrimaryKey(autoGenerate = true)
    public int stationId;

    public String stationName,stationLatitude,stationLongitude;

}

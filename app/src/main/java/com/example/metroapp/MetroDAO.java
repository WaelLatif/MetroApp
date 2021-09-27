package com.example.metroapp;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MetroDAO {
    @Query("SELECT stationName from metro")
    List<String> selectStations();

    @Query("SELECT stationName FROM metro WHERE stationId BETWEEN 3 AND 37")
    List<String> line1();

    @Query("SELECT stationName FROM metro WHERE stationId BETWEEN 39 AND 58")
    List<String> line2();

    @Query("SELECT stationName FROM metro WHERE stationId BETWEEN 60 AND 82")
    List<String> line3();

    @Query("SELECT stationLatitude FROM metro WHERE stationName =:station")
    float stationLatitude(String station);

    @Query("SELECT stationLongitude FROM metro WHERE stationName =:station")
    float stationLongitude(String station);
}

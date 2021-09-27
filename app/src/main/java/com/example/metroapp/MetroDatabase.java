package com.example.metroapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {station.class}, version = 1)
public abstract class MetroDatabase extends RoomDatabase {

    public abstract MetroDAO metroDAO();

    private static MetroDatabase ourInstance;

    public static MetroDatabase getInstance(Context context) {

        if (ourInstance == null) {

            ourInstance = Room.databaseBuilder(context,

                    MetroDatabase.class, "metro.db")
                    .createFromAsset("databases/metro.db")
                    .allowMainThreadQueries()
//                    .fallbackToDestructiveMigration()
                    .build();
        }

        return ourInstance;
    }
}

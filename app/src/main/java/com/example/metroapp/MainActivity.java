package com.example.metroapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Spinner spinnerJourneyStart, spinnerJourneyEnd;
    Button buttonStart, buttonClear, startMapBtn, endMapBtn;
    TextView textViewResult, TimeTv;
    String desStart = "", desEnd = "", direction = "", direction2 = "", destination = "", riddingStation = "", dropOffStation = "",
            JourneyTime, station;
    int StTime = 0, hours = 0, minutes = 0, ticketPrice = 0, start = 0, end = 0;
    LocationManager manager;
    ArrayAdapter adapter;
    Intent in;
    double latitude, longitude;
    Location l1, l2;

    List<String> stations;
    List<String> line_1 = new ArrayList<>();
    List<String> line_2 = new ArrayList<>();
    List<String> line_3 = new ArrayList<>();

    List<String> journey = new ArrayList<>();//for the total journey
    List<String> part1 = new ArrayList<>();// for the 1st part of the journey
    List<String> part2 = new ArrayList<>();// for the 2nd part of the journey

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinnerJourneyStart = findViewById(R.id.spinnerJourneyStart);
        spinnerJourneyEnd = findViewById(R.id.spinnerJourneyEnd);
        buttonStart = findViewById(R.id.buttonStart);
        buttonClear = findViewById(R.id.buttonClear);
        startMapBtn = findViewById(R.id.startMapBtn);
        endMapBtn = findViewById(R.id.endMapBtn);

        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        TimeTv = findViewById(R.id.TimeTv);

        //Load Data from database
        line_1 = MetroDatabase.getInstance(this).metroDAO().line1();
        line_2 = MetroDatabase.getInstance(this).metroDAO().line2();
        line_3 = MetroDatabase.getInstance(this).metroDAO().line3();

        stations = MetroDatabase.getInstance(this).metroDAO().selectStations();
        stations.add(0, "Select your Riding Station :");

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, stations);
        spinnerJourneyStart.setAdapter(adapter);

        stations = MetroDatabase.getInstance(this).metroDAO().selectStations();
        stations.add(0, "Select your Drop off Station :");
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, stations);
        spinnerJourneyEnd.setAdapter(adapter);

        //get the data from the shared Preferences
        pref = getPreferences(MODE_PRIVATE);
        spinnerJourneyStart.setSelection(pref.getInt("Journey Start", 0));
        spinnerJourneyEnd.setSelection(pref.getInt("Journey End", 0));
        textViewResult.setText(pref.getString("last text", ""));
        buttonStart.setEnabled(pref.getBoolean("calculateBtnStatus", true));
        l1 = new Location("");
        l2 = new Location("");
    }

    @Override
    public void onBackPressed() {
        // put the data to the shared Preferences
        if (!textViewResult.getText().equals("")) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("last text", textViewResult.getText().toString());
            editor.putInt("Journey Start", spinnerJourneyStart.getSelectedItemPosition());
            editor.putInt("Journey End", spinnerJourneyEnd.getSelectedItemPosition());
            editor.putBoolean("calculateBtnStatus", buttonStart.isEnabled());
            editor.apply();
        }
        super.onBackPressed();
    }

    public void NearestStation(View view) {
        in = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Nearest+metro+station+cairo"));
        startActivity(in);
    }

    public void clear(View view) {
        spinnerJourneyStart.setSelection(0);
        spinnerJourneyEnd.setSelection(0);
        part1.clear();
        part2.clear();
        journey.clear();
        StTime = 0;
        JourneyTime = "";
        TimeTv.setText("");
        ticketPrice = 0;
        direction = "";
        direction2 = "";
        textViewResult.setText("");
        buttonStart.setEnabled(true);
    }

    public void calculate(View view) {
        riddingStation = spinnerJourneyStart.getSelectedItem().toString();
        dropOffStation = spinnerJourneyEnd.getSelectedItem().toString();
        // to Select valid Riding Station
        if (spinnerJourneyStart.getSelectedItemPosition() < 3) {
            Toast.makeText(this, "Select valid Riding Station!!", Toast.LENGTH_LONG).show();
            spinnerJourneyStart.setFocusable(true);
            return;
        }// to select valid drop off Station
        if (spinnerJourneyEnd.getSelectedItemPosition() < 3) {
            Toast.makeText(this, "Select valid drop off Station!!", Toast.LENGTH_LONG).show();
            spinnerJourneyStart.setFocusable(true);
            return;
        }
        //to select a station not a line tag
        if (!line_1.contains(dropOffStation) &&
                !line_2.contains(dropOffStation) &&
                !line_3.contains(dropOffStation)) {
            Toast.makeText(this, "Select valid Station", Toast.LENGTH_LONG).show();
            spinnerJourneyEnd.setFocusable(true);
            return;
        }
        // not to print same data twice
        if (riddingStation.equals(desStart) &&
                dropOffStation.equals(desEnd)) {
            return;
        }
        desStart = riddingStation;
        desEnd = dropOffStation;

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // if two stations in the same line
        if (line_1.contains(desStart) && line_1.contains(desEnd)) {
            part1.addAll(line_1);
            if (part1.indexOf(desEnd) < part1.indexOf(desStart)) {
                Collections.reverse(part1);
            }
            start = part1.indexOf(desStart);
            end = part1.indexOf(desEnd) + 1;
            direction = part1.get(part1.size() - 1);
            direction2 = part1.get(part1.size() - 1);
            part1 = part1.subList(start, end);

        }
        if (line_2.contains(desStart) && line_2.contains(desEnd)) {
            part1.addAll(line_2);
            if (part1.indexOf(desEnd) < part1.indexOf(desStart)) {
                Collections.reverse(part1);
            }

            start = part1.indexOf(desStart);
            end = part1.indexOf(desEnd) + 1;
            direction = part1.get(part1.size() - 1);
            direction2 = part1.get(part1.size() - 1);
            part1 = part1.subList(start, end);
        }
        if (line_3.contains(desStart) && line_3.contains(desEnd)) {
            part1.addAll(line_3);
            if (part1.indexOf(desEnd) < part1.indexOf(desStart)) {
                Collections.reverse(part1);
            }
            start = part1.indexOf(desStart);
            end = part1.indexOf(desEnd) + 1;
            direction = part1.get(part1.size() - 1);
            direction2 = part1.get(part1.size() - 1);
            part1 = part1.subList(start, end);
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // if the Riding Station and the  drop off Station in two different lines
        // separate the journey for two parts
        // part one of the  journey

        if (line_1.contains(desStart) && !line_1.contains(desEnd)) {
            part1.addAll(line_1);
            if (part1.indexOf(desStart) > part1.indexOf("alshohadaa (changing station)") + 1) {
                Collections.reverse(part1);
            }
            start = part1.indexOf(desStart);
            // if (line_3.contains(desEnd)) {
            //     end = part1.indexOf("nasser") + 1;
            // } else {
            end = part1.indexOf("alshohadaa (changing station)") + 1;
            // }
            direction = part1.get(part1.size() - 1);
            part1 = part1.subList(start, end);

        }
        if (line_2.contains(desStart) && !line_2.contains(desEnd)) {
            part1.addAll(line_2);
            if (part1.indexOf(desStart) > part1.indexOf("alshohadaa (changing station)") + 1) {
                Collections.reverse(part1);
            }
            if (line_3.contains(desEnd)) {
                end = part1.indexOf("attaba (changing station)") + 1;
            } else {
                end = part1.indexOf("alshohadaa (changing station)") + 1;
            }
            start = part1.indexOf(desStart);
            direction = part1.get(part1.size() - 1);
            part1 = part1.subList(start, end);
        }
        if (line_3.contains(desStart) && !line_3.contains(desEnd)) {
            part1.addAll(line_3);
            if (part1.indexOf(desStart) > part1.indexOf("attaba (changing station)")) {
                Collections.reverse(part1);
            }
            // if (line_1.contains(desEnd)) {
            //     end = part1.indexOf("nasser") + 1;
            // } else {
            end = part1.indexOf("attaba (changing station)") + 1;

            start = part1.indexOf(desStart);
            direction = part1.get(part1.size() - 1);
            part1 = part1.subList(start, end);
            if (line_1.contains(desEnd)) {
                part1.add("alshohadaa (changing station)");
            }
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // part two of the  journey
        if (line_1.contains(desEnd) && !line_1.contains(desStart)) {
            part2.addAll(line_1);
            if (part2.indexOf(desEnd) < part2.indexOf("alshohadaa (changing station)") + 1) {
                Collections.reverse(part2);
            }
            // if (line_3.contains(desStart)) {
            //     start = part2.indexOf("nasser") + 1;
            // } else {
            start = part2.indexOf("alshohadaa (changing station)") + 1;
            //  }
            end = part2.indexOf(desEnd) + 1;
            direction2 = part2.get(part2.size() - 1);
            part2 = part2.subList(start, end);

        }
        if (line_2.contains(desEnd) && !line_2.contains(desStart)) {
            part2.addAll(line_2);
            if (part2.indexOf(desEnd) < part2.indexOf("alshohadaa (changing station)") + 1) {
                Collections.reverse(part2);
            }
            if (line_3.contains(desStart)) {
                start = part2.indexOf("attaba (changing station)") + 1;
            } else {
                start = part2.indexOf("alshohadaa (changing station)") + 1;
            }
            end = part2.indexOf(desEnd) + 1;
            direction2 = part2.get(part2.size() - 1);
            part2 = part2.subList(start, end);
        }
        if (line_3.contains(desEnd) && !line_3.contains(desStart)) {
            part2.addAll(line_3);
            if (part2.indexOf(desEnd) < part2.indexOf("attaba (changing station)") + 1) {
                Collections.reverse(part2);
            }
            // if (line_1.contains(desStart)) {
            //     start = part2.indexOf("nasser") + 1;
            //  } else {
            start = part2.indexOf("attaba (changing station)") + 1;
            //   }
            end = part2.indexOf(desEnd) + 1;
            direction2 = part2.get(part2.size() - 1);
            part2 = part2.subList(start, end);
        }

        if (line_3.contains(desEnd) && line_1.contains(desStart)) {
            part1.add("attaba (changing station)");

        }
        // add the two parts of the trip to the journey
        journey.addAll(part1);
        journey.addAll(part2);

        if (line_1.contains(desStart) && line_3.contains(desEnd)) {
            destination = (direction +
                    " direction and change from al shohdaa Station to line two then change again from al attaba station to " + direction2);
        } else if (line_3.contains(desStart) && line_1.contains(desEnd)) {
            destination = (direction +
                    " direction and change from al attaba Station to line two then change again from al shohdaa station to " + direction2);

        } else {

            if (direction.equalsIgnoreCase(direction2)) {
                destination = direction2 + "";
            } else {
                destination = (direction + " direction and change from alShohdaa to " + direction2);
            }
        }

        //to calculate the journey time
        StTime = 2 * journey.size();
        if (StTime > 60) {
            hours = StTime / 60;
            minutes = StTime % 60;
            JourneyTime = ("the journey estimated time will be : " + hours + " and " + minutes + " minutes.");
        } else {
            JourneyTime = ("the journey estimated time will be :" + StTime + " minutes.");
        }

        //to calculate the ticket price
        if (journey.size() <= 9) {
            ticketPrice = 5;
        } else if (journey.size() <= 16) {
            ticketPrice = 7;
        } else {
            ticketPrice = 10;

        }
        // to print out data
        textViewResult.append("** you are taking " + destination + " direction.\n");
        textViewResult.append("** " + journey + "\n");
        textViewResult.append("** your ticket will cost " + ticketPrice + " EGP.\n");
        textViewResult.append("** No of stations is " + journey.size() + " stations.\n");
        textViewResult.append("** " + JourneyTime);

        // to clear the data for the next search
        buttonStart.setEnabled(false);
    }

    public void getStationLocation(View view) {
        if (view.equals(startMapBtn)) {
            station = spinnerJourneyStart.getSelectedItem().toString();
        } else if (view.equals(endMapBtn)) {
            station = spinnerJourneyEnd.getSelectedItem().toString();
        }
        if (station.equals("Select Station")) {
            Toast.makeText(this, "Select valid Station!!", Toast.LENGTH_LONG).show();
            return;
        }
        latitude = MetroDatabase.getInstance(this).metroDAO().stationLatitude(station);
        longitude = MetroDatabase.getInstance(this).metroDAO().stationLongitude(station);
        in = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + latitude + "," + longitude));
        startActivity(in);
    }

    public void timeToArrive(View view) {
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perm, 1);
        } else
            manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                try {
                    manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            else
                Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            String line = addressList.get(0).getAddressLine(0);

        } catch (IOException e) {
            Toast.makeText(this, "internet error", Toast.LENGTH_SHORT).show();
        }
        l1.setLatitude(latitude);
        l1.setLongitude(longitude);

        station = spinnerJourneyEnd.getSelectedItem().toString();
        l2.setLatitude(MetroDatabase.getInstance(this).metroDAO().stationLatitude(station));
        l2.setLongitude(MetroDatabase.getInstance(this).metroDAO().stationLongitude(station));
        float c = (l1.distanceTo(l2) / 1000);
        int m = (int) (c * 2);
        TimeTv.setText(m + " Minutes to Arrive");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
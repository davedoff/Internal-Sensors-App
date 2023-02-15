package com.davidhalldoff.internalsensors.model;

import java.util.ArrayList;

public class SensorsModel {
    private ArrayList<Sensors> accelList;
    private ArrayList<Sensors> gyroList;
    private ArrayList<Sensors> accelGyroList;

    public SensorsModel() {
        accelList = new ArrayList<>();
        gyroList = new ArrayList<>();
        accelGyroList = new ArrayList<>();
    }

    public void addAccel(Sensors accelerometer) {
        accelList.add(accelerometer);
    }

    public void addGyro(Gyroscope gyro) {
        gyroList.add(gyro.getSensor());
    }

    public void addAccelGyro(Sensors accelGyro) {
        accelGyroList.add(accelGyro);
    }

    public ArrayList<Sensors> getAccelList() {
        return accelList;
    }

    public ArrayList<Sensors> getGyroList() {
        return gyroList;
    }

    public ArrayList<Sensors> getAccelGyroList() {
        return accelGyroList;
    }

    public int getAccelListSize() {
        return accelList.size();
    }

    public int getGyroListSize() {
        return gyroList.size();
    }

    public int getAccelGyroListSize() {
        return accelGyroList.size();
    }

    public void reset() {
        accelList.clear();
        gyroList.clear();
        accelGyroList.clear();
    }
}

package com.davidhalldoff.internalsensors.model;

public class Gyroscope {
    private Sensors sensor;
    private static final float F = 0.1f;
    private double x, y, z;

    public Gyroscope(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        sensor = new Sensors();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Sensors getSensor() {
        return sensor;
    }

    public void setSensor(Sensors sensor) {
        this.sensor = sensor;
    }
}

package com.davidhalldoff.internalsensors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.davidhalldoff.internalsensors.io.FileIO;
import com.davidhalldoff.internalsensors.model.Accelerometer;
import com.davidhalldoff.internalsensors.model.Gyroscope;
import com.davidhalldoff.internalsensors.model.Sensors;
import com.davidhalldoff.internalsensors.model.SensorsModel;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Class representing the controller used when recording angular data from the internal Accelerometer and Gyroscope sensors.
 */
public class MainActivity extends AppCompatActivity {

    private AppCompatTextView tvAccelDegrees, tvGyroDegrees, tvAccelGyroDegrees;

    private AppCompatButton buttonRecord;
    private Boolean isRecording = false;
    private double accelX, accelY, accelZ;
    private double gyroX, gyroY, gyroZ;
    private double accelAngle, gyroAngle, accelGyroAngle;
    private GraphView graph;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private long firstAccelTimestamp;
    private long firstGyroTimestamp;
    private static final float F = 0.1f;
    private static final float NS2S = 1.0f / 1000000000.0f;

    private SensorsModel sensorsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRecord = findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(this::onRecord);

        tvAccelDegrees = findViewById(R.id.accelerometerDegrees);
        tvGyroDegrees = findViewById(R.id.gyroscopeDegrees);
        tvAccelGyroDegrees = findViewById(R.id.accelGyroDegrees);
        graph = (GraphView) findViewById(R.id.graph);

        // init sensor objects
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorsModel = new SensorsModel();

        resetValues();
    }

    /**
     * onRecord listener
     *
     * when button is pressed:
     * if isRecording = false, the method will activate the sensor listeners and reset all values
     * if isRecording = true, the method will deactivate the sensor listeners and save the recorded values to Json files
     */
    @SuppressLint("SetTextI18n")
    private void onRecord(View view) {
        if (isRecording) {
            stopRecording();
        } else {
            isRecording = true;
            buttonRecord.setText("Off");

            resetValues();

            mSensorManager.registerListener(accelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(gyroscopeListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Accelerometer listener
     */
    private final SensorEventListener accelerometerListener = new SensorEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (firstAccelTimestamp == 0) {
                firstAccelTimestamp = event.timestamp;
            }

            // EWMA filter
            accelX = F * accelX + (1-F) * event.values[0];
            accelY = F * accelY + (1-F) * event.values[1];
            accelZ = F * accelZ + (1-F) * event.values[2];

            Accelerometer accel = new Accelerometer(accelX, accelY, accelZ);

            long timestamp = event.timestamp - firstAccelTimestamp;

            // If 10 seconds has passed, stop recording and save values to json files
            if (tenSecPassed(timestamp)) {
                if (isRecording) {
                    stopRecording();
                }
            }

            //accelAngle = Math.atan2(accelZ, accelY) + Math.PI;
            accelAngle = Math.atan2(accelZ, accelY);

            // Save timestamp and angle to object, add object to the list of objects
            accel.getSensor().setTimestamp((long) (timestamp * NS2S));
            accel.getSensor().setAngle(Math.toDegrees(accelAngle));
            sensorsModel.addAccel(accel.getSensor());

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            tvAccelDegrees.setText(df.format(Math.toDegrees(accelAngle)) + "\u00B0");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /**
     * Gyroscope listener
     */
    private final SensorEventListener gyroscopeListener = new SensorEventListener() {
        final float dT = 0.19999999f;

        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (firstGyroTimestamp == 0) {
                firstGyroTimestamp = event.timestamp;
            }

            // EWMA filter
            gyroX = F * gyroX + (1-F) * event.values[0];
            gyroY = F * gyroY + (1-F) * event.values[1];
            gyroZ = F * gyroZ + (1-F) * event.values[2];

            Sensors accelGyro = new Sensors();
            Gyroscope gyro = new Gyroscope(gyroX, gyroY, gyroZ);

            long timestamp = event.timestamp - firstGyroTimestamp;
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);

            gyroAngle = gyroAngle - dT * gyroX;

            // Complementary filter (sensor fusion)
            accelGyroAngle = F * (accelAngle) + (1-F) * gyroAngle;
            tvGyroDegrees.setText(df.format(Math.toDegrees(gyroAngle)) + "\u00B0");
            tvAccelGyroDegrees.setText(df.format(Math.toDegrees(accelGyroAngle)) + "\u00B0");

            gyro.getSensor().setAngle(Math.toDegrees(gyroAngle));
            gyro.getSensor().setTimestamp((long) (timestamp * NS2S));
            sensorsModel.addGyro(gyro);

            accelGyro.setAngle(Math.toDegrees(accelGyroAngle));
            accelGyro.setTimestamp((long) (timestamp * NS2S));
            sensorsModel.addAccelGyro(accelGyro);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(gyroscopeListener);
    }

    private void displayGraph() {
        LineGraphSeries<DataPoint> seriesA = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesG = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesAG = new LineGraphSeries<>();
        double yA, yG, yAG;
        long xA, xG, xAG;
        for (int i=0; i<sensorsModel.getAccelListSize(); i++) {
            yA = sensorsModel.getAccelList().get(i).getAngle();
            xA = sensorsModel.getAccelList().get(i).getTimestamp();
            seriesA.appendData(new DataPoint(xA, yA), true, sensorsModel.getAccelListSize());
        }

        for (int i=0; i< sensorsModel.getGyroListSize(); i++) {
            yG = sensorsModel.getGyroList().get(i).getAngle();
            xG = sensorsModel.getGyroList().get(i).getTimestamp();
            yAG = sensorsModel.getAccelGyroList().get(i).getAngle();
            xAG = sensorsModel.getAccelGyroList().get(i).getTimestamp();
            seriesG.appendData(new DataPoint(xG, yG), true, sensorsModel.getGyroListSize());
            seriesAG.appendData(new DataPoint(xAG, yAG), true, sensorsModel.getAccelGyroListSize());
        }

        graph.removeAllSeries();
        seriesA.setTitle("Accel");
        seriesG.setTitle("Gyro");
        seriesAG.setTitle("Accel + Gyro");

        seriesA.setColor(Color.parseColor("#1982c4"));
        seriesG.setColor(Color.parseColor("#8ac926"));
        seriesAG.setColor(Color.parseColor("#ff595e"));

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.addSeries(seriesA);
        graph.addSeries(seriesG);
        graph.addSeries(seriesAG);
    }

    private boolean tenSecPassed(long timestamp) {
        return timestamp * NS2S > 10;
    }

    @SuppressLint("SetTextI18n")
    private void stopRecording() {
        isRecording = false;
        buttonRecord.setText("On");

        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(gyroscopeListener);

        displayGraph();

        FileIO.saveValuesToJson(MainActivity.this, sensorsModel.getAccelList(), "accel");
        FileIO.saveValuesToJson(MainActivity.this, sensorsModel.getAccelGyroList(), "accelgyro");
    }

    private void resetValues() {
        firstAccelTimestamp = 0;
        firstGyroTimestamp = 0;
        accelAngle = 0.0;
        gyroAngle = 0.0;
        accelGyroAngle = 0.0;
        accelX = 0;
        accelY = 0;
        accelZ = 0;
        gyroX = 0;
        gyroY = 0;
        gyroZ = 0;

        sensorsModel.reset();
    }
}
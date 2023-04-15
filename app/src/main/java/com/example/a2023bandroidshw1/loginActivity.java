package com.example.a2023bandroidshw1;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;


public class loginActivity extends AppCompatActivity implements SensorEventListener {

    private AppCompatEditText passwordEditText;
    private boolean isWifiConnected = false;
    private boolean isFlashOn;
    private SensorManager sensorManager_acc;
    private Sensor accelerometer;

    private boolean isStanding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);

        passwordEditText = findViewById(R.id.passwordEditText);

        // Get battery level percentage
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, intentFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        int batteryPercentage = (int) (batteryPct * 100);

        // Set password to battery level percentage
        String password = String.valueOf(batteryPercentage);

        // wifi part
        String ssid = "not connected";

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
                ssid = wifiInfo.getSSID();
                if (ssid.equals("AfekaOpen"))
                    isWifiConnected = true;
                Log.d("pttt", "Connected to wifi: " + ssid);
            } else {
                Log.d("pttt", "Not connected to any wifi network");
            }
        }


        // flash part

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Check if the device has a flash unit
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            try {
                // Get the ID of the back-facing camera
                String cameraId = cameraManager.getCameraIdList()[0];

                cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                    @Override
                    public void onTorchModeChanged(String cameraId, boolean enabled) {
                        super.onTorchModeChanged(cameraId, enabled);

                        // Check the current state of the torch mode
                        if (enabled) {
                            isFlashOn=true;
                            // Torch mode is ON
                        } else {

                            isFlashOn=false;
                            // Torch mode is OFF
                        }
                    }
                }, new Handler());

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }



        //sensor for accelerometer

        sensorManager_acc = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager_acc.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // to pass to other activity
        String finalSsid = ssid;

        findViewById(R.id.loginButton).setOnClickListener(view -> {
            String enteredPassword = passwordEditText.getText().toString();

            // && isWifiConnected &&  && isFlashOn

            Log.d("pttt", ""+isFlashOn);
            if (enteredPassword.equals(password) && isStanding & isFlashOn) {
                Toast.makeText(this, "Login successful!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(loginActivity.this, ProfileActivity.class);
                intent.putExtra("password", enteredPassword);
                intent.putExtra("wifi", finalSsid);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid password or NotConnected to wifi Or flashOFF or YouMovings", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager_acc.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager_acc.unregisterListener(this);
    }

    ///he acceleration of gravity will be close to 9.81 m/s^2.
    // Therefore, the code checks if the acceleration is within a certain range around 9.81 m/s^2 (i.e., 9.81 * 0.9 to 9.81 * 1.1).
    // If the acceleration is within this range it sets the boolean variable isLaid to true
    @Override
    public void onSensorChanged(SensorEvent event) {
         boolean isLaid = false;
          boolean isMoving = false;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            if (acceleration < 9.81 * 1.1 && acceleration > 9.81 * 0.9) {
                // Phone is lying flat on a surface
                isLaid = true;
            } else {
                // Phone is not lying flat on a surface
                isLaid = false;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            float angularVelocity = (float) Math.sqrt(x * x + y * y + z * z);

            if (angularVelocity < 0.1) {
                // Phone is stationary
                isMoving = false;
            } else {
                // Phone is moving
                isMoving = true;
            }
        }

        // Check if phone is placed on a surface
        if (isLaid && !isMoving) {
            isStanding = true;
        } else {
            isStanding = false;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

}

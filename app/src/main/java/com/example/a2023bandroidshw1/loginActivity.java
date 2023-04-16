package com.example.a2023bandroidshw1;

import static android.Manifest.permission_group.LOCATION;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.Manifest;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;


public class loginActivity extends AppCompatActivity implements SensorEventListener {

    private AppCompatEditText passwordEditText;
    private static boolean isWifiConnected = false;
    private boolean isFlashOn;
    private SensorManager sensorManager_acc;
    private Sensor accelerometer;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private boolean isStanding;


    private static final int LOCATION = 1;


    protected void onStart() {
        super.onStart();
        //Assume you want to read the SSID when the activity is started
        tryToReadSSID();
        if(tryToReadSSID().equals("\"Building_E\""))
            isWifiConnected=true;
        Log.d("pttt1", tryToReadSSID());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);

        passwordEditText = findViewById(R.id.passwordEditText);

       // tryToReadSSID();

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


        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            tryToReadSSID();

        } else {
            // Location permissions have not been granted, request them
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
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

        findViewById(R.id.loginButton).setOnClickListener(view -> {
            String enteredPassword = passwordEditText.getText().toString();


            if (enteredPassword.equals(password) && isStanding && isFlashOn  && isWifiConnected) {
                Toast.makeText(this, "Login successful!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(loginActivity.this, ProfileActivity.class);
                intent.putExtra("password", enteredPassword);
                intent.putExtra("wifi",tryToReadSSID());
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





    private String tryToReadSSID() {
        //If requested permission isn't Granted yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Integer.parseInt(String.valueOf(LOCATION)));
        }else{//Permission already granted
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo.getSupplicantState() == SupplicantState.COMPLETED){
                String ssid = wifiInfo.getSSID();//Here you can access your SSID
                return ssid;
            }
        }
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the permission request was for location permissions
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the location permissions have been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permissions have been granted, proceed with app logic
                // ...
            } else {
                // Location permissions have been denied, handle this case as necessary
                // ...
            }
        }
    }

}

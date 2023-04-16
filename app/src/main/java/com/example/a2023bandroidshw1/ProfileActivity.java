package com.example.a2023bandroidshw1;

import android.os.BatteryManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    private AppCompatTextView passwordTextView;
    private AppCompatTextView batteryTextView;
    private AppCompatTextView wifiTextView;
    private ShapeableImageView flashImgView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        // Get references to the password and battery TextViews
        passwordTextView = findViewById(R.id.Profile_TXT_password);
        wifiTextView = findViewById(R.id.Profile_TXT_wifi);
        batteryTextView = findViewById(R.id.Profile_TXT_battery);
        flashImgView=findViewById(R.id.Profile_IMG_flash);

        Animation blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);
        flashImgView.startAnimation(blinkAnimation);



        // Get the battery percentage using the BatteryManager API
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryPercentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        // Get the password passed from LoginActivity
        String password = getIntent().getStringExtra("password");
        String wifi_name = getIntent().getStringExtra("wifi");

        // Set the password TextView to display the password
        passwordTextView.setText("Password: " + password);
        wifiTextView.setText("You Are Connected To  :"+wifi_name);

        // Set the battery TextView to display the battery percentage
        batteryTextView.setText("Battery Percentage: " + batteryPercentage + "%");
    }
}

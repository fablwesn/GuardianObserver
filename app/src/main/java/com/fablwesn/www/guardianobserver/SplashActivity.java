package com.fablwesn.www.guardianobserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Splash screen starting the main activity as soon as it has finished loading.
 *
 * from https://android.jlelse.eu/right-way-to-create-splash-screen-on-android-e7f1709ba154
 * */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start main activity.
        Intent intent = new Intent(SplashActivity.this, NewsActivity.class);
        startActivity(intent);
        // Exit the splash activity.
        finish();
    }
}

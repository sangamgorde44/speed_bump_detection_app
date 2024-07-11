package com.example.speedbumpdetectionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
public class welcome_screen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        Intent goLogin = new Intent(welcome_screen.this, LoginPage.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(goLogin);
                finish();
            }
        },1000);
    }
}

//---------------------------------------------- end -----------------------------------------------
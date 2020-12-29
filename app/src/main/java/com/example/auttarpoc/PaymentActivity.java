package com.example.auttarpoc;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.os.Bundle;

public class PaymentActivity extends AppCompatActivity {

    public MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
    }
}
package com.braintreepayments.api.test;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class UnitTestActivity extends AppCompatActivity {

    public static int view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        setContentView(view);
    }
}

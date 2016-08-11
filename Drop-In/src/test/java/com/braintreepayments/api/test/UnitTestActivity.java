package com.braintreepayments.api.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class UnitTestActivity extends AppCompatActivity {

    public static int view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat);

        setContentView(view);
    }
}

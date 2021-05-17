package com.braintreepayments.api;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SelectPaymentMethodFragmentTest {

    @Test
    public void onCreate_vaultEditButtonIsInvisible() {
        ActivityScenario activityScenario = ActivityScenario.launch(DropInActivity.class);
        activityScenario.onActivity(new ActivityScenario.ActivityAction() {
            @Override
            public void perform(Activity activity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                appCompatActivity.getSupportFragmentManager();
            }
        });
    }
}
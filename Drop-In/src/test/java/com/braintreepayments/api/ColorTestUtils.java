package com.braintreepayments.api;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import org.robolectric.RuntimeEnvironment;

import androidx.appcompat.app.AppCompatActivity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ColorTestUtils {

    public static AppCompatActivity setupActivity(int backgroundColor) {
        ColorDrawable colorDrawable = mock(ColorDrawable.class);
        when(colorDrawable.getColor()).thenReturn(getColor(backgroundColor));
        View rootView = mock(View.class);
        when(rootView.getBackground()).thenReturn(colorDrawable);
        View decorView = mock(View.class);
        when(decorView.getRootView()).thenReturn(rootView);
        Window window = mock(Window.class);
        when(window.getDecorView()).thenReturn(decorView);
        AppCompatActivity activity = mock(AppCompatActivity.class);
        when(activity.getResources()).thenReturn(RuntimeEnvironment.application.getResources());
        when(activity.getWindow()).thenReturn(window);

        return activity;
    }

    public static int getColor(int color) {
        return RuntimeEnvironment.application.getResources().getColor(color);
    }
}

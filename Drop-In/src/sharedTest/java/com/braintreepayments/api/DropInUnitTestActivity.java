package com.braintreepayments.api;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.braintreepayments.api.dropin.R;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DropInUnitTestActivity extends DropInActivity {

    public Context context;
    public DropInClient dropInClient;
    public String deviceData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_drop_in_activity_theme);

        super.onCreate(savedInstanceState);
    }

    public void setDropInRequest(DropInRequest dropInRequest) {
        this.dropInRequest = dropInRequest;
    }

    @Override
    public Resources getResources() {
        Resources resources = spy(super.getResources());
        when(resources.getInteger(android.R.integer.config_longAnimTime)).thenReturn(0);
        when(resources.getInteger(android.R.integer.config_mediumAnimTime)).thenReturn(0);
        when(resources.getInteger(android.R.integer.config_shortAnimTime)).thenReturn(0);
        return resources;
    }

    @Override
    public Context getApplicationContext() {
        if (context != null) {
            return context;
        }

        return super.getApplicationContext();
    }
}

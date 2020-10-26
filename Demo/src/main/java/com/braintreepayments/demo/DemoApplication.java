package com.braintreepayments.demo;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.braintreepayments.demo.internal.ApiClient;
import com.braintreepayments.demo.internal.ApiClientRequestInterceptor;

import retrofit.RestAdapter;

public class DemoApplication extends Application {

    private static ApiClient sApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Settings.getVersion(this) != BuildConfig.VERSION_CODE) {
            Settings.setVersion(this);
        }
        DeveloperTools.setup(this);
    }

    static ApiClient getApiClient(Context context) {
        if (sApiClient == null) {
            sApiClient = new RestAdapter.Builder()
                    .setEndpoint(Settings.getEnvironmentUrl(context))
                    .setRequestInterceptor(new ApiClientRequestInterceptor())
                    .build()
                    .create(ApiClient.class);
        }

        return sApiClient;
    }

    static void resetApiClient() {
        sApiClient = null;
    }
}

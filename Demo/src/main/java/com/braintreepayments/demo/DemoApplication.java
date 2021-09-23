package com.braintreepayments.demo;

import android.app.Application;
import android.content.Context;

import com.braintreepayments.demo.internal.ApiClient;
import com.braintreepayments.demo.internal.ApiClientRequestInterceptor;

import retrofit.RestAdapter;

public class DemoApplication extends Application {

    private static ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Settings.getVersion(this) != BuildConfig.VERSION_CODE) {
            Settings.setVersion(this);
        }
    }

    static ApiClient getApiClient(Context context) {
        if (apiClient == null) {
            apiClient = new RestAdapter.Builder()
                    .setEndpoint(Settings.getEnvironmentUrl(context))
                    .setRequestInterceptor(new ApiClientRequestInterceptor())
                    .build()
                    .create(ApiClient.class);
        }

        return apiClient;
    }

    static void resetApiClient() {
        apiClient = null;
    }
}

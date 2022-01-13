package com.braintreepayments.demo;

import android.content.Context;
import android.text.TextUtils;

import com.braintreepayments.api.BraintreeAuthCallback;
import com.braintreepayments.api.BraintreeAuthProvider;
import com.braintreepayments.demo.models.ClientToken;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DemoAuthorizationProvider implements BraintreeAuthProvider {

    private final Context appContext;

    public DemoAuthorizationProvider(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override
    public void getAuthorization(BraintreeAuthCallback callback) {
        if (Settings.useTokenizationKey(appContext)) {
            String authString = Settings.getEnvironmentTokenizationKey(appContext);
            callback.onAuthResult(authString, null);
        } else {
            DemoApplication.getApiClient(appContext).getClientToken(Settings.getCustomerId(appContext),
                    Settings.getMerchantAccountId(appContext), new Callback<ClientToken>() {
                        @Override
                        public void success(ClientToken clientToken, Response response) {
                            if (TextUtils.isEmpty(clientToken.getClientToken())) {
                                callback.onAuthResult(null, new Exception("Client token was empty"));
                            } else {
                                String authString = clientToken.getClientToken();
                                callback.onAuthResult(authString, null);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            String message = "Unable to get a client token. Response Code: " +
                                    error.getResponse().getStatus() + " Response body: " +
                                    error.getResponse().getBody();
                            callback.onAuthResult(null, new Exception(message));
                        }
                    });

        }
    }
}

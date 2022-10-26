package com.braintreepayments.demo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.braintreepayments.api.ClientTokenCallback;
import com.braintreepayments.api.ClientTokenProvider;
import com.braintreepayments.demo.internal.ApiClient;
import com.braintreepayments.demo.models.ClientToken;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DemoClientTokenProvider implements ClientTokenProvider {

    private Context applicationContext;

    public DemoClientTokenProvider(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @Override
    public void getClientToken(@NonNull ClientTokenCallback callback) {
        String customerId = Settings.getCustomerId(applicationContext);
        String merchantId = Settings.getMerchantAccountId(applicationContext);
        ClientTokenRequest request = new ClientTokenRequest(customerId, merchantId);

        ApiClient apiClient = DemoApplication.getApiClient(applicationContext);
        apiClient.getClientToken(request, new Callback<ClientToken>() {
            @Override
            public void success(ClientToken clientToken, Response response) {
                callback.onSuccess(clientToken.getValue());
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getResponse() != null) {
                    String errorMessage = "Unable to get a client token. Response Code: " +
                            error.getResponse().getStatus() + " Response body: " +
                            error.getResponse().getBody();
                    callback.onFailure(new Exception(errorMessage));
                } else {
                    String errorMessage = "Unable to get a client token.";
                    callback.onFailure(new Exception(errorMessage));
                }
            }
        });
    }
}

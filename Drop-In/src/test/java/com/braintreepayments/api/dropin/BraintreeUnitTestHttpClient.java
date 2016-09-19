package com.braintreepayments.api.dropin;

import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.BraintreeHttpClient;
import com.braintreepayments.api.models.CardBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import static com.braintreepayments.api.test.StringUtils.getStreamFromString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BraintreeUnitTestHttpClient extends BraintreeHttpClient {

    public static final String GET_PAYMENT_METHODS = "/v1/payment_methods.*";
    public static final String TOKENIZE_CREDIT_CARD = "/v1/payment_methods/" + new CardBuilder().getApiPath();
    public static final String UNIONPAY_CAPABILITIES_PATH = "/v1/payment_methods/credit_cards/capabilities.*";
    public static final String UNIONPAY_ENROLLMENT_PATH = "/v1/union_pay_enrollments";

    private Map<String, String> mResponseMap;
    private Map<String, SimpleEntry<Integer, String>> mErrorResponseMap;

    public BraintreeUnitTestHttpClient() {
        super(null);
        mResponseMap = new HashMap<>();
        mErrorResponseMap = new HashMap<>();
    }

    public BraintreeUnitTestHttpClient configuration(String configuration) {
        mResponseMap.put("configuration.*", configuration);
        return this;
    }

    public BraintreeUnitTestHttpClient successResponse(String path, String response) {
        mResponseMap.put(path, response);
        return this;
    }

    public BraintreeUnitTestHttpClient errorResponse(String path, int responseCode, String response) {
        mErrorResponseMap.put(path, new SimpleEntry<>(responseCode, response));
        return this;
    }

    @Override
    public void get(String path, HttpResponseCallback callback) {
        handleRequest(path, callback);
    }

    @Override
    public void post(String path, String data, HttpResponseCallback callback) {
        handleRequest(path, callback);
    }

    private void handleRequest(String path, HttpResponseCallback callback) {
        for (String requestPath : mResponseMap.keySet()) {
            if (path.matches(".*" + requestPath)) {
                callback.success(mResponseMap.get(requestPath));
                return;
            }
        }
        for (String requestPath : mErrorResponseMap.keySet()) {
            if (path.matches(".*" + requestPath)) {
                try {
                    super.parseResponse(getHttpUrlConnection(mErrorResponseMap.get(requestPath)));
                } catch (Exception e) {
                    callback.failure(e);
                    return;
                }
            }
        }
    }

    private HttpURLConnection getHttpUrlConnection(SimpleEntry<Integer, String> response) throws IOException {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(response.getKey());
        when(connection.getErrorStream()).thenReturn(getStreamFromString(response.getValue()));
        return connection;
    }
}

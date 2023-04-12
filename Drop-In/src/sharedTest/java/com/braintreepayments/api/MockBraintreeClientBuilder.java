package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.stubbing.Answer;

public class MockBraintreeClientBuilder {

    private String sendGETSuccess;
    private Exception sendGETError;

    private String sendGraphQLPOSTSuccess;
    private Exception sendGraphQLPOSTError;

    private Configuration configuration;
    private Exception configurationError;

    private Authorization authorization;
    private Exception authorizationError;

    private String sessionId;
    private String integration;

    public MockBraintreeClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public MockBraintreeClientBuilder configurationError(Exception configurationError) {
        this.configurationError = configurationError;
        return this;
    }

    public MockBraintreeClientBuilder authorizationSuccess(Authorization authorization) {
        this.authorization = authorization;
        return this;
    }

    public MockBraintreeClientBuilder authorizationError(Exception authorizationError) {
        this.authorizationError = authorizationError;
        return this;
    }

    public MockBraintreeClientBuilder sendGETSuccessfulResponse(String response) {
        sendGETSuccess = response;
        return this;
    }

    public MockBraintreeClientBuilder sendGETErrorResponse(Exception error) {
        sendGETError = error;
        return this;
    }

    public MockBraintreeClientBuilder sendGraphQLPOSTSuccessfulResponse(String response) {
        sendGraphQLPOSTSuccess = response;
        return this;
    }

    public MockBraintreeClientBuilder sendGraphQLPOSTErrorResponse(Exception error) {
        sendGraphQLPOSTError = error;
        return this;
    }

    public MockBraintreeClientBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public MockBraintreeClientBuilder integration(String integration) {
        this.integration = integration;
        return this;
    }

    public BraintreeClient build() {
        BraintreeClient braintreeClient = mock(BraintreeClient.class);
        when(braintreeClient.getSessionId()).thenReturn(sessionId);
        when(braintreeClient.getIntegrationType()).thenReturn(integration);

        doAnswer((Answer<Void>) invocation -> {
            ConfigurationCallback callback = (ConfigurationCallback) invocation.getArguments()[0];
            if (configuration != null) {
                callback.onResult(configuration, null);
            } else if (configurationError != null) {
                callback.onResult(null, configurationError);
            }
            return null;
        }).when(braintreeClient).getConfiguration(any(ConfigurationCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            AuthorizationCallback callback = (AuthorizationCallback) invocation.getArguments()[0];
            if (authorization != null) {
                callback.onAuthorizationResult(authorization, null);
            } else if (authorizationError != null) {
                callback.onAuthorizationResult(null, authorizationError);
            }
            return null;
        }).when(braintreeClient).getAuthorization(any(AuthorizationCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            HttpResponseCallback callback = (HttpResponseCallback) invocation.getArguments()[1];
            if (sendGETSuccess != null) {
                callback.onResult(sendGETSuccess, null);
            } else if (sendGETError != null) {
                callback.onResult(null, sendGETError);
            }
            return null;
        }).when(braintreeClient).sendGET(anyString(), any(HttpResponseCallback.class));

        doAnswer((Answer<Void>) invocation -> {
            HttpResponseCallback callback = (HttpResponseCallback) invocation.getArguments()[1];
            if (sendGraphQLPOSTSuccess != null) {
                callback.onResult(sendGraphQLPOSTSuccess, null);
            } else if (sendGraphQLPOSTError != null) {
                callback.onResult(null, sendGraphQLPOSTError);
            }
            return null;
        }).when(braintreeClient).sendGraphQLPOST(anyString(), any(HttpResponseCallback.class));

        return braintreeClient;
    }
}

package com.braintreepayments.api;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class PaymentMethodClientUnitTest {

    private Context context;
    private CardNonce cardNonce;

    @Captor
    ArgumentCaptor<List<PaymentMethodNonce>> paymentMethodNoncesCaptor;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);

        cardNonce = mock(CardNonce.class);
        context = ApplicationProvider.getApplicationContext();

        when(cardNonce.getString()).thenReturn("im-a-card-nonce");
    }

    @Test
    public void getPaymentMethodNonces_whenDefaultFirstIsFalse_sendsPaymentMethodsGETRequest() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("sample-session-id")
                .build();

        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);
        sut.getPaymentMethodNonces(false, mock(GetPaymentMethodNoncesCallback.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendGET(captor.capture(), any(HttpResponseCallback.class));

        String url = captor.getValue();
        assertEquals("/v1/payment_methods?default_first=false&session_id=sample-session-id", url);
    }

    @Test
    public void getPaymentMethodNonces_whenDefaultFirstIsTrue_sendsPaymentMethodsGETRequest() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("sample-session-id")
                .build();

        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);
        sut.getPaymentMethodNonces(true, mock(GetPaymentMethodNoncesCallback.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendGET(captor.capture(), any(HttpResponseCallback.class));

        String url = captor.getValue();
        assertEquals("/v1/payment_methods?default_first=true&session_id=sample-session-id", url);
    }

    @Test
    public void getPaymentMethodNonces_parsesCards() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("sample-session-id")
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_RESPONSE)
                .build();

        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals("123456-12345-12345-a-adfa", paymentMethodNonces.get(0).getString());
        assertTrue(paymentMethodNonces.get(0).isDefault());
    }

    @Test
    public void getPaymentMethodNonces_parsesPayPal() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("sample-session-id")
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_RESPONSE)
                .build();

        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", paymentMethodNonces.get(1).getString());
        assertFalse(paymentMethodNonces.get(1).isDefault());
    }

    @Test
    public void parsePaymentMethods_parsesVenmoAccountNonce() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("sample-session-id")
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_RESPONSE)
                .build();

        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals("fake-venmo-nonce", paymentMethodNonces.get(2).getString());
    }

    @Test
    public void parsePaymentMethods_doesNotParseGooglePayCardNonces() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("sample-session-id")
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_GOOGLE_PAY_RESPONSE)
                .build();

        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(false, callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(0, paymentMethodNonces.size());
    }

    @Test
    public void getPaymentMethodNonces_returnsAnEmptyListIfEmpty() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_EMPTY_RESPONSE)
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        assertEquals(0, paymentMethodNoncesCaptor.getValue().size());
    }

    @Test
    public void getPaymentMethodNonces_throwsAnError() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETErrorResponse(new UnexpectedException("Error"))
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(callback).onResult((List<PaymentMethodNonce>) isNull(), captor.capture());
        assertTrue(captor.getValue() instanceof UnexpectedException);
    }

    @Test
    public void getPaymentMethodNonces_sendsAnAnalyticsEventForParsingErrors() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETSuccessfulResponse("{}")
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        verify(braintreeClient).sendAnalyticsEvent("get-payment-methods.failed");
    }

    @Test
    public void getPaymentMethodNonces_sendsAnAnalyticsEventForErrors() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETErrorResponse(new UnexpectedException("Error"))
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        verify(braintreeClient).sendAnalyticsEvent("get-payment-methods.failed");
    }

    @Test
    public void getPaymentMethodNonces_fetchesPaymentMethods() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_RESPONSE)
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(3, paymentMethodNonces.size());
        assertTrue(paymentMethodNonces.get(0) instanceof CardNonce);
        assertTrue(paymentMethodNonces.get(1) instanceof PayPalAccountNonce);
        assertTrue(paymentMethodNonces.get(2) instanceof VenmoAccountNonce);
    }

    @Test
    public void getPaymentMethodNonces_doesNotParseGooglePayMethods() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_GOOGLE_PAY_RESPONSE)
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        verify(callback).onResult(paymentMethodNoncesCaptor.capture(), (Exception) isNull());

        List<PaymentMethodNonce> paymentMethodNonces = paymentMethodNoncesCaptor.getValue();
        assertEquals(0, paymentMethodNonces.size());
    }

    @Test
    public void getPaymentMethodNonces_sendsAnAnalyticsEventForSuccess() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sendGETSuccessfulResponse(Fixtures.PAYMENT_METHODS_GET_PAYMENT_METHODS_RESPONSE)
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(callback);

        verify(braintreeClient).sendAnalyticsEvent("get-payment-methods.succeeded");
    }

    @Test
    public void getPaymentMethodNonces_includesDefaultFirstParamAndSessionIdInRequestPath() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .sessionId("session-id")
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        GetPaymentMethodNoncesCallback callback = mock(GetPaymentMethodNoncesCallback.class);
        sut.getPaymentMethodNonces(true, callback);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendGET(captor.capture(), any(HttpResponseCallback.class));

        String requestUri = captor.getValue();
        assertTrue(requestUri.contains("default_first=true"));
        assertTrue(requestUri.contains("session_id=session-id"));
    }

    @Test
    public void deletePaymentMethodNonce_withTokenizationKey_throwsAnError() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.TOKENIZATION_KEY))
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(context, cardNonce, callback);

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(callback).onResult((PaymentMethodNonce) isNull(), captor.capture());

        assertTrue(captor.getValue() instanceof BraintreeException);
        assertEquals("A client token with a customer id must be used to delete a payment method nonce.",
                captor.getValue().getMessage());

        verify(braintreeClient, never()).sendGraphQLPOST(anyString(), any(HttpResponseCallback.class));
    }

    @Test
    public void deletePaymentMethodNonce_throwsAnError() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .sendGraphQLPOSTErrorResponse(new UnexpectedException("Error"))
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(context, cardNonce, callback);

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(callback).onResult((PaymentMethodNonce) isNull(), captor.capture());

        PaymentMethodDeleteException paymentMethodDeleteException = (PaymentMethodDeleteException)captor.getValue();
        PaymentMethodNonce paymentMethodNonce = paymentMethodDeleteException.getPaymentMethodNonce();
        assertEquals(cardNonce, paymentMethodNonce);
    }

    @Test
    public void deletePaymentMethodNonce_sendAnAnalyticsEventForFailure() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .sendGraphQLPOSTErrorResponse(new UnexpectedException("Error"))
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(context, cardNonce, callback);

        verify(braintreeClient).sendAnalyticsEvent("delete-payment-methods.failed");
    }

    @Test
    public void deletePaymentMethodNonce_sendAnAnalyticsEventForSuccess() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .sendGraphQLPOSTSuccessfulResponse("Success")
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(context, cardNonce, callback);

        verify(braintreeClient).sendAnalyticsEvent("delete-payment-methods.succeeded");
    }

    @Test
    public void deletePaymentMethodNonce_sendNoncePostCallbackForSuccess() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN))
                .sendGraphQLPOSTSuccessfulResponse("Success")
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(context, cardNonce, callback);

        verify(callback).onResult(cardNonce, null);
    }

    @Test
    public void deletePaymentMethodNonce_postToGraphQL()
            throws Exception {
        Authorization authorization = Authorization
                .fromString(Fixtures.BASE64_CLIENT_TOKEN);

        BraintreeClient braintreeClient = new MockBraintreeClientBuilder()
                .authorizationSuccess(authorization)
                .sendGraphQLPOSTSuccessfulResponse("Success")
                .sessionId("test-session-id")
                .integration("test-integration")
                .build();
        PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        DeletePaymentMethodNonceCallback callback = mock(DeletePaymentMethodNonceCallback.class);
        sut.deletePaymentMethod(context, cardNonce, callback);

        verify(braintreeClient).getIntegrationType();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(braintreeClient).sendGraphQLPOST(captor.capture(), any(HttpResponseCallback.class));

        JSONObject graphQlRequest = new JSONObject(captor.getValue());

        String expectedGraphQLQuery = GraphQLQueryHelper.getQuery(
                ApplicationProvider.getApplicationContext(), R.raw.delete_payment_method_mutation);
        assertEquals(expectedGraphQLQuery, graphQlRequest.getString(GraphQLConstants.Keys.QUERY));

        JSONObject metadata = graphQlRequest.getJSONObject("clientSdkMetadata");

        assertEquals(cardNonce.getString(), graphQlRequest.getJSONObject("variables")
                .getJSONObject("input").getString("singleUseTokenId"));

        assertEquals("DeletePaymentMethodFromSingleUseToken", graphQlRequest
                .getString(GraphQLConstants.Keys.OPERATION_NAME));

        assertEquals("test-integration", metadata.getString("integration"));
        assertEquals("test-session-id", metadata.getString("sessionId"));
        assertEquals("client", metadata.getString("source"));
    }
}

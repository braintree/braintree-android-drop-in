package com.braintreepayments.api;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED;
import static androidx.appcompat.app.AppCompatActivity.RESULT_FIRST_USER;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static com.braintreepayments.api.UnitTestFixturesHelper.base64EncodedClientTokenFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class DropInActivityUnitTest {

    private ActivityController activityController;
    private DropInActivity activity;
    private ShadowActivity shadowActivity;

    @Before
    public void beforeEach() {
    }

    private void setupDropInActivity(String authorization, DropInClient dropInClient, DropInRequest dropInRequest, String sessionId) {
        Intent intent = new Intent()
                .putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putExtra(DropInClient.EXTRA_AUTHORIZATION, authorization)
                .putExtra(DropInClient.EXTRA_SESSION_ID, sessionId);

        activityController = Robolectric.buildActivity(DropInActivity.class, intent);
        activity = (DropInActivity) activityController.get();
        activity.dropInClient = dropInClient;
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        activityController.setup();

        activity.onBackPressed();
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_CANCELED, shadowActivity.getResultCode());
    }

    @Test
    public void pressingBackSendsAnalyticsEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.onBackPressed();
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        verify(activity.dropInClient).sendAnalyticsEvent("sdk.exit.canceled");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenShouldNotRequestThreeDSecureVerification_returnsANonce() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .collectDeviceDataSuccess("sample-data")
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);

        activity.onVaultedPaymentMethodSelected(event);
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        verify(dropInClient, never()).performThreeDSecureVerification(same(activity), same(cardNonce), any(DropInResultCallback.class));
        assertTrue(activity.isFinishing());
        assertEquals(RESULT_OK, shadowActivity.getResultCode());
        DropInResult result = shadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(cardNonce.getString(), Objects.requireNonNull(result.getPaymentMethodNonce()).getString());
        assertEquals(cardNonce.getLastTwo(), ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void onVaultedPaymentSelected_requestsThreeDSecureVerificationForCardWhenEnabled() throws Exception {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(true)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        activity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient).performThreeDSecureVerification(same(activity), same(cardNonce), any(DropInResultCallback.class));
    }

    @Test
    public void onVaultedPaymentMethodSelected_sendsAnAnalyticsEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .collectDeviceDataSuccess("device data")
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        activityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);

        // the activity will finish once the view model bottom sheet state has moved to hidden
        activity.onVaultedPaymentMethodSelected(event);
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        verify(activity.dropInClient).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void onPaymentMethodNonceCreated_storesPaymentMethodType() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .collectDeviceDataSuccess("sample-data")
                .build();

        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        assertNull(BraintreeSharedPreferences.getSharedPreferences(activity)
                .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);

        activity.onVaultedPaymentMethodSelected(event);
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        assertEquals(DropInPaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(activity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void onVaultedPaymentMethodSelected_returnsDeviceData() throws JSONException {
        DropInRequest dropInRequest = new DropInRequest();

        String authorization = Fixtures.TOKENIZATION_KEY;

        DropInClient dropInClient = new MockDropInClientBuilder()
                .collectDeviceDataSuccess("device-data")
                .shouldPerformThreeDSecureVerification(false)
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);

        activity.onVaultedPaymentMethodSelected(event);
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_OK, shadowActivity.getResultCode());
        DropInResult result = shadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals("device-data", result.getDeviceData());
    }

    @Test
    public void selectingAVaultedPaymentMethod_returnsANonce() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        List<PaymentMethodNonce> nonces = new ArrayList<>();
        PaymentMethodNonce paymentMethodNonce = PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON));
        nonces.add(paymentMethodNonce);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .getVaultedPaymentMethodsSuccess(nonces)
                .collectDeviceDataSuccess("sample-data")
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(paymentMethodNonce);

        activity.onVaultedPaymentMethodSelected(event);
        activity.dropInViewModel.setBottomSheetState(BottomSheetState.HIDDEN);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_OK, shadowActivity.getResultCode());
        assertEquals(paymentMethodNonce.getString(),
                Objects.requireNonNull(((DropInResult) shadowActivity.getResultIntent()
                        .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT))
                        .getPaymentMethodNonce())
                        .getString());
        verify(dropInClient).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenCard_sendsAnalyticEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        activity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient).sendAnalyticsEvent("vaulted-card.select");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenPayPal_doesNotSendAnalyticEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        PayPalAccountNonce payPalAccountNonce =
            PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(payPalAccountNonce);
        activity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient, never()).sendAnalyticsEvent("vaulted-card.select");
    }

    @Test
    public void configurationExceptionExitsActivityWithError() {
        assertExceptionIsReturned("configuration-exception",
                new ConfigurationException("Configuration exception"));
    }

    @Test
    public void authenticationExceptionExitsActivityWithError() {
        assertExceptionIsReturned("developer-error",
                new AuthenticationException("Access denied"));
    }

    @Test
    public void authorizationExceptionExitsActivityWithError() {
        assertExceptionIsReturned("developer-error",
                new AuthorizationException("Access denied"));
    }

    @Test
    public void upgradeRequiredExceptionExitsActivityWithError() {
        assertExceptionIsReturned("developer-error",
                new UpgradeRequiredException("Exception"));
    }

    @Test
    public void serverExceptionExitsActivityWithError() {
        assertExceptionIsReturned("server-error",
                new ServerException("Exception"));
    }

    @Test
    public void unexpectedExceptionExitsActivityWithError() {
        assertExceptionIsReturned("server-error",
                new UnexpectedException("Exception"));
    }

    @Test
    public void downForMaintenanceExceptionExitsActivityWithError() {
        assertExceptionIsReturned("server-unavailable",
                new ServiceUnavailableException("Exception"));
    }

    @Test
    public void anyExceptionExitsActivityWithError() {
        assertExceptionIsReturned("sdk-error", new Exception("Error!"));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypePayPal_tokenizesPayPal() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAYPAL);
        activity.onSupportedPaymentMethodSelected(event);

        verify(dropInClient).tokenizePayPalRequest(same(activity), any(PayPalFlowStartedCallback.class));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypeVenmo_tokenizesVenmo() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAY_WITH_VENMO);
        activity.onSupportedPaymentMethodSelected(event);

        verify(dropInClient).tokenizeVenmoAccount(same(activity), any(VenmoTokenizeAccountCallback.class));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypeGooglePay_requestsGooglePay() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.GOOGLE_PAYMENT);
        activity.onSupportedPaymentMethodSelected(event);

        verify(dropInClient).requestGooglePayPayment(same(activity), any(GooglePayRequestPaymentCallback.class));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypeUnknown_showsAddCardFragment() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN);
        activity.onSupportedPaymentMethodSelected(event);
        activity.getSupportFragmentManager().executePendingTransactions();

        assertNotNull(activity.getSupportFragmentManager().findFragmentByTag("ADD_CARD"));
    }

    @Test
    public void onCardDetailsSubmit_onError_whenErrorWithResponse_setsCardTokenizationErrorInViewModel() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        ErrorWithResponse error = ErrorWithResponse.fromJson(Fixtures.CREDIT_CARD_ERROR_RESPONSE);
        DropInClient dropInClient = new MockDropInClientBuilder()
                .cardTokenizeError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        Card card = new Card();
        DropInEvent event = DropInEvent.createCardDetailsSubmitEvent(card);
        activity.onCardDetailsSubmit(event);

        assertEquals(error, activity.dropInViewModel.getCardTokenizationError().getValue());
    }

    @Test
    public void onCardDetailsSubmit_onError_whenErrorNotErrorWithResponse_finishesWithError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .cardTokenizeError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        Card card = new Card();
        DropInEvent event = DropInEvent.createCardDetailsSubmitEvent(card);
        activity.onCardDetailsSubmit(event);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals(error.getMessage(), actualException.getMessage());
    }

    @Test
    public void startPaymentFlow_onPayPalError_finishesWithError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .payPalError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.startPaymentFlow(DropInPaymentMethodType.PAYPAL);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals(error.getMessage(), actualException.getMessage());
    }

    @Test
    public void startPaymentFlow_onGooglePayError_finishesWithError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .googlePayError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.startPaymentFlow(DropInPaymentMethodType.GOOGLE_PAYMENT);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals(error.getMessage(), actualException.getMessage());
    }

    @Test
    public void startPaymentFlow_onVenmoError_finishesWithError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .venmoError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.startPaymentFlow(DropInPaymentMethodType.PAY_WITH_VENMO);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals(error.getMessage(), actualException.getMessage());
    }

    @Test
    public void startPaymentFlow_onGetSupportedCardTypesError_finishesWithError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .getSupportedCardTypesError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.startPaymentFlow(DropInPaymentMethodType.UNKNOWN);

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals(error.getMessage(), actualException.getMessage());
    }

    @Test
    public void removePaymentMethodNonce_onError_finishesWithError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .deletePaymentMethodError(error)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.removePaymentMethodNonce(mock(PaymentMethodNonce.class));

        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(error.getClass(), actualException.getClass());
        assertEquals(error.getMessage(), actualException.getMessage());
    }

    @Test
    public void finishDropInWithPendingResult_finishesWithPaymentMethodNonceAndDeviceDataInDropInResult()
            throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = mock(DropInClient.class);
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        DropInResult dropInResult = new DropInResult();
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        dropInResult.paymentMethodNonce(cardNonce);
        dropInResult.deviceData("device_data");

        activity.pendingDropInResult = dropInResult;
        activity.finishDropInWithPendingResult(false);

        ShadowActivity shadowActivity = shadowOf(activity);
        assertTrue(activity.isFinishing());
        assertEquals(RESULT_OK, shadowActivity.getResultCode());
        DropInResult result = shadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertNotNull(result);
        assertEquals(cardNonce.getString(), result.getPaymentMethodNonce().getString());
        assertEquals("device_data", result.getDeviceData());
    }

    @Test
    public void finishDropInWithError_finishesWithException() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = mock(DropInClient.class);
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        Exception exception = new Exception("Error message");
        activity.finishDropInWithError(exception);

        ShadowActivity shadowActivity = shadowOf(activity);
        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception error = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertNotNull(error);
        assertEquals("Error message", error.getMessage());
    }

    private void assertExceptionIsReturned(String analyticsEvent, Exception exception) {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        activityController.setup();

        activity.onError(exception);

        verify(activity.dropInClient).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(activity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception actualException = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }

}

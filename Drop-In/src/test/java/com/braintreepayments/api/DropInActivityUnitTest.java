package com.braintreepayments.api;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.Mock;
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
import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;
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

    private ActivityController mActivityController;
    private DropInUnitTestActivity mActivity;
    private ShadowActivity mShadowActivity;

    @Before
    public void setup() {
    }

    private void setupDropInActivity(String authorization, DropInClient dropInClient, DropInRequest dropInRequest, String sessionId) {
        Intent intent = new Intent()
                .putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putExtra(DropInClient.EXTRA_AUTHORIZATION, authorization)
                .putExtra(DropInClient.EXTRA_SESSION_ID, sessionId);

        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class, intent);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mActivity.dropInClient = dropInClient;
        mShadowActivity = shadowOf(mActivity);
    }

    @Test
    public void onCreate_whenAuthorizationIsInvalid_finishesWithError() {
        String authorization = "not a tokenization key";
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = mock(DropInClient.class);
        when(dropInClient.getAuthorization()).thenReturn(mock(InvalidAuthorization.class));

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception exception = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertTrue(exception instanceof InvalidArgumentException);
        assertEquals("Tokenization Key or Client Token was invalid.", exception.getMessage());
    }

    @Test
    public void onResume_deliversBrowserSwitchResult() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = mock(DropInClient.class);

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        verify(dropInClient).deliverBrowserSwitchResult(same(mActivity), any(DropInResultCallback.class));
    }

    @Test
    public void onResume_whenBrowserSwitchResultExists_finishesActivity() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInResult result = mock(DropInResult.class);
        PaymentMethodNonce nonce = mock(PaymentMethodNonce.class);
        when(result.getPaymentMethodNonce()).thenReturn(nonce);
        when(result.getDeviceData()).thenReturn("device data");

        DropInClient dropInClient = new MockDropInClientBuilder()
                .handleThreeDSecureActivityResultSuccess(result)
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        assertFalse(mActivity.isFinishing());
    }

    @Test
    public void onResume_whenBrowserSwitchReturnsUserCanceledException_setsUserCanceledErrorInViewModel() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new UserCanceledException("User canceled 3DS.");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .deliverBrowseSwitchResultError(error)
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onActivityResult(100, 1, mock(Intent.class));

        assertFalse(mActivity.isFinishing());
        assertEquals(error, mActivity.dropInViewModel.getUserCanceledError().getValue());
    }

    @Test
    public void onResume_whenBrowserSwitchError_forwardsError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("A 3DS error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .deliverBrowseSwitchResultError(error)
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onActivityResult(100, 1, mock(Intent.class));

        assertTrue(mActivity.isFinishing());
        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.sdk-error");
    }

    @Test
    public void onActivityResult_whenDropInResultExists_finishesActivity() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInResult result = mock(DropInResult.class);
        PaymentMethodNonce nonce = mock(PaymentMethodNonce.class);
        when(result.getPaymentMethodNonce()).thenReturn(nonce);
        when(result.getDeviceData()).thenReturn("device data");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .handleActivityResultSuccess(result)
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, mock(Intent.class));
        assertTrue(mActivity.isFinishing());
    }

    @Test
    public void onActivityResult_whenResultIsUserCanceledException_setsUserCanceledErrorInViewModel() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new UserCanceledException("User canceled 3DS.");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .handleActivityResultError(error)
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, mock(Intent.class));

        assertFalse(mActivity.isFinishing());
        assertEquals(error, mActivity.dropInViewModel.getUserCanceledError().getValue());
    }

    @Test
    public void onActivityResult_whenError_handlesError() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        Exception error = new Exception("A 3DS error");
        DropInClient dropInClient = new MockDropInClientBuilder()
                .handleActivityResultError(error)
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, mock(Intent.class));

        assertTrue(mActivity.isFinishing());
        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.sdk-error");
    }

    @Test
    public void onActivityResult_forwardsResultToDropInClient() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();

        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        Intent intent = mock(Intent.class);
        mActivity.onActivityResult(BraintreeRequestCodes.THREE_D_SECURE, 1, intent);

        verify(mActivity.dropInClient).handleActivityResult(same(mActivity), eq(BraintreeRequestCodes.THREE_D_SECURE), eq(1), same(intent), any(DropInResultCallback.class));
    }

    @Test
    public void setsIntegrationTypeToDropinForDropinActivity() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        // TODO: revisit integration type metadata and consider passing integration (core PR)
        // type through BraintreeClient constructor instead of relying on reflection
//        assertEquals("dropin3", mActivity.getDropInClient().getIntegrationType());
    }

    @Test
    public void sendsAnalyticsEventWhenShown() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        verify(mActivity.dropInClient).sendAnalyticsEvent("appeared");
    }

    @Test
    public void onVaultedPaymentMethodSelected_reloadsPaymentMethodsIfThreeDSecureVerificationFails() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN)))
                .threeDSecureError(new Exception("three d secure failure"))
                .shouldPerformThreeDSecureVerification(true)
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        String authorization = Fixtures.TOKENIZATION_KEY;
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivity.mClientTokenPresent = true;
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));

        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient).getVaultedPaymentMethods(same(mActivity), eq(true), any(GetPaymentMethodNoncesCallback.class));
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void pressingBackSendsAnalyticsEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onBackPressed();

        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.canceled");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenShouldNotRequestThreeDSecureVerification_returnsANonce() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient, never()).performThreeDSecureVerification(same(mActivity), same(cardNonce), any(DropInResultCallback.class));
        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        DropInResult result = mShadowActivity.getResultIntent()
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
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient).performThreeDSecureVerification(same(mActivity), same(cardNonce), any(DropInResultCallback.class));
    }

    @Test
    public void onVaultedPaymentMethodSelected_sendsAnAnalyticsEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .collectDeviceDataSuccess("device data")
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void onPaymentMethodNonceCreated_storesPaymentMethodType() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .build();

        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        assertNull(BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        assertEquals(DropInPaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(mActivity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void onVaultedPaymentMethodSelected_returnsDeviceData() throws JSONException {
        DropInRequest dropInRequest = new DropInRequest()
                .collectDeviceData(true);

        String authorization = Fixtures.TOKENIZATION_KEY;

        DropInClient dropInClient = new MockDropInClientBuilder()
                .collectDeviceDataSuccess("device-data")
                .shouldPerformThreeDSecureVerification(false)
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        DropInResult result = mShadowActivity.getResultIntent()
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
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(paymentMethodNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertEquals(paymentMethodNonce.getString(),
                Objects.requireNonNull(((DropInResult) mShadowActivity.getResultIntent()
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
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(cardNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

        verify(dropInClient).sendAnalyticsEvent("vaulted-card.select");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenPayPal_doesNotSendAnalyticEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        PayPalAccountNonce payPalAccountNonce =
            PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON));
        DropInEvent event = DropInEvent.createVaultedPaymentMethodSelectedEvent(payPalAccountNonce);
        mActivity.onVaultedPaymentMethodSelected(event);

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
        mActivityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAYPAL);
        mActivity.onSupportedPaymentMethodSelected(event);

        verify(dropInClient).tokenizePayPalRequest(same(mActivity), any(PayPalFlowStartedCallback.class));
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
        mActivityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAY_WITH_VENMO);
        mActivity.onSupportedPaymentMethodSelected(event);

        verify(dropInClient).tokenizeVenmoAccount(same(mActivity), any(VenmoTokenizeAccountCallback.class));
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
        mActivityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.GOOGLE_PAYMENT);
        mActivity.onSupportedPaymentMethodSelected(event);

        verify(dropInClient).requestGooglePayPayment(same(mActivity), any(GooglePayRequestPaymentCallback.class));
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
        mActivityController.setup();

        DropInEvent event =
                DropInEvent.createSupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN);
        mActivity.onSupportedPaymentMethodSelected(event);

        assertNotNull(mActivity.getSupportFragmentManager().findFragmentByTag("ADD_CARD"));
    }

    @Test
    public void onPaymentMethodNonceDeleted_sendsAnalyticCall() {
        // TODO: test this after determining analytics testing strategy
//        verify(dropInClient).sendAnalyticsEvent("manager.delete.succeeded");
    }

    private void assertExceptionIsReturned(String analyticsEvent, Exception exception) {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest();

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onError(exception);

        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }
}

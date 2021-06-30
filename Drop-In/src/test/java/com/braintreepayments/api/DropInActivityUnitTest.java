package com.braintreepayments.api;

import android.content.Intent;
import android.os.Parcelable;
import android.widget.ViewSwitcher;

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
import static com.braintreepayments.api.DropInActivity.ADD_CARD_REQUEST_CODE;
import static com.braintreepayments.api.DropInActivity.DELETE_PAYMENT_METHOD_NONCE_CODE;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.UnitTestFixturesHelper.base64EncodedClientTokenFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import com.braintreepayments.api.dropin.R;

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
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

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
    public void setsIntegrationTypeToDropinForDropinActivity() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        // TODO: revisit integration type metadata and consider passing integration (core PR)
        // type through BraintreeClient constructor instead of relying on reflection
//        assertEquals("dropin3", mActivity.getDropInClient().getIntegrationType());
    }

    @Test
    public void sendsAnalyticsEventWhenShown() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        verify(mActivity.dropInClient).sendAnalyticsEvent("appeared");
    }

    @Test
    public void onVaultedPaymentMethodSelected_reloadsPaymentMethodsIfThreeDSecureVerificationFails() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .threeDSecureError(new Exception("three d secure failure"))
                .shouldPerformThreeDSecureVerification(true)
                .build();

        ThreeDSecureRequest threeDSecureRequest = new ThreeDSecureRequest();
        threeDSecureRequest.setAmount("1.00");

        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN))
                .threeDSecureRequest(threeDSecureRequest)
                .requestThreeDSecureVerification(true);

        String authorization = Fixtures.TOKENIZATION_KEY;
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivity.mClientTokenPresent = true;
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        mActivity.onVaultedPaymentMethodSelected(cardNonce);

        verify(dropInClient).getVaultedPaymentMethods(same(mActivity), eq(true), any(GetPaymentMethodNoncesCallback.class));
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void pressingBackSendsAnalyticsEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onBackPressed();

        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.canceled");
    }

    @Test
    public void touchingOutsideSheetTriggersBackPress() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onBackgroundClicked(null);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void touchingOutsideSheetSendsAnalyticsEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onBackgroundClicked(null);

        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.canceled");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenShouldNotRequestThreeDSecureVerification_returnsANonce() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        mActivity.onVaultedPaymentMethodSelected(cardNonce);

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
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(true)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        mActivity.onVaultedPaymentMethodSelected(cardNonce);
        verify(dropInClient).performThreeDSecureVerification(same(mActivity), same(cardNonce), any(DropInResultCallback.class));
    }

    @Test
    public void onVaultedPaymentMethodSelected_sendsAnAnalyticsEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .collectDeviceDataSuccess("device data")
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        mActivityController.setup();

        mActivity.onVaultedPaymentMethodSelected(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        verify(mActivity.dropInClient).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void onPaymentMethodNonceCreated_storesPaymentMethodType() throws JSONException {
        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .build();

        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        assertNull(BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));

        mActivity.onVaultedPaymentMethodSelected(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));

        assertEquals(DropInPaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(mActivity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void onVaultedPaymentMethodSelected_returnsDeviceData() throws JSONException {
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
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

        mActivity.onVaultedPaymentMethodSelected(cardNonce);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals("device-data", result.getDeviceData());
    }

    @Test
    public void selectingAVaultedPaymentMethod_returnsANonce() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        List<PaymentMethodNonce> nonces = new ArrayList<>();
        PaymentMethodNonce paymentMethodNonce = PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON));
        nonces.add(paymentMethodNonce);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .getVaultedPaymentMethodsSuccess(nonces)
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onVaultedPaymentMethodSelected(paymentMethodNonce);

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
    public void onVaultedPaymentMethodSelected_whenCard_sendsAnalyticEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onVaultedPaymentMethodSelected(mock(CardNonce.class));

        verify(dropInClient).sendAnalyticsEvent("vaulted-card.select");
    }

    @Test
    public void onVaultedPaymentMethodSelected_whenPayPal_doesNotSendAnalyticEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onVaultedPaymentMethodSelected(mock(PayPalAccountNonce.class));

        verify(dropInClient, never()).sendAnalyticsEvent("vaulted-card.select");
    }

    @Test
    public void onActivityResult_addCardCancelRefreshesVaultedPaymentMethods() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        mActivity.mClientTokenPresent = true;
        mActivityController.setup();

        mActivity.onActivityResult(1, RESULT_CANCELED, null);
        verify(dropInClient).getVaultedPaymentMethods(same(mActivity), eq(true), any(GetPaymentMethodNoncesCallback.class));
    }

    @Test
    public void onActivityResult_nonCardCancelDoesNotRefreshVaultedPaymentMethods() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.onActivityResult(2, RESULT_CANCELED, null);

        verify(dropInClient, never()).getVaultedPaymentMethods(any(FragmentActivity.class), anyBoolean(), any(GetPaymentMethodNoncesCallback.class));
    }

    @Test
    public void onActivityResult_returnsNonceFromAddCardActivity() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
        mActivity.onActivityResult(1, RESULT_OK, data);

        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        DropInResult response = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(result.getPaymentMethodType(), response.getPaymentMethodType());
        assertEquals(result.getPaymentMethodNonce(), response.getPaymentMethodNonce());
    }

    @Test
    public void onActivityResult_withPayPal_doesNotSendCardAnalyticEvent() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        PayPalAccountNonce paypalNonce = mock(PayPalAccountNonce.class);
        ArrayList<Parcelable> paymentMethodNonces = new ArrayList<>();
        paymentMethodNonces.add(paypalNonce);

        mActivity.onActivityResult(2, RESULT_OK, new Intent()
                .putExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES", paymentMethodNonces));

        verify(dropInClient, never()).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    public void onActivityResult_nonceFromAddCardActivity_doesNotSendVaultAnalyticEvent() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);

        mActivity.onActivityResult(1, RESULT_OK, data);

        verify(dropInClient, never()).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    public void onActivityResult_returnsDeviceData() throws JSONException {
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true);

        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInClient dropInClient = new MockDropInClientBuilder()
                .shouldPerformThreeDSecureVerification(false)
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        mActivity.mDropInRequest = dropInRequest;

        mActivityController.setup();

        DropInResult result = new DropInResult()
                .deviceData("device-data")
                .paymentMethodNonce(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));

        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);

        mActivity.onActivityResult(1, RESULT_OK, data);

        DropInResult response = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals("device-data", response.getDeviceData());
    }

    @Test
    public void onActivityResult_returnsErrorFromAddCardActivity() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        Intent data = new Intent()
                .putExtra(DropInActivity.EXTRA_ERROR, new Exception("Error"));
        mActivity.onActivityResult(1, RESULT_FIRST_USER, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        assertEquals("Error",
                ((Exception) mShadowActivity.getResultIntent()
                        .getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage());
    }

    @Test
    public void onActivityResult_storesPaymentMethodType() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
        assertNull(BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));

        mActivity.onActivityResult(1, RESULT_OK, data);

        assertEquals(DropInPaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(mActivity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void onActivityResult_whenVaultManagerResultOk_refreshesVaultedPaymentMethods() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");

        mActivity.mClientTokenPresent = true;
        mActivityController.setup();

        mActivity.onActivityResult(2, RESULT_OK, null);

        verify(dropInClient).getVaultedPaymentMethods(same(mActivity), eq(true), any(GetPaymentMethodNoncesCallback.class));
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
    public void onVaultEditButtonClick_launchesVaultManagerActivity() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .getVaultedPaymentMethodsSuccess(new ArrayList<PaymentMethodNonce>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        mActivity.showVaultManager();

        ShadowActivity.IntentForResult intent = mShadowActivity.getNextStartedActivityForResult();

        assertEquals(2, intent.requestCode);
        assertEquals(mActivity.mDropInRequest, intent.intent.getParcelableExtra(EXTRA_CHECKOUT_REQUEST));

        // TODO: revisit nonce caching
//        assertEquals(mActivity.mBraintreeFragment.getCachedPaymentMethodNonces(),
//                intent.intent.getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypePayPal_tokenizesPayPal() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        SupportedPaymentMethodSelectedEvent event =
            new SupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAYPAL);
        mActivity.onSupportedPaymentMethodSelectedEvent(event);

        verify(dropInClient).tokenizePayPalRequest(same(mActivity), any(PayPalFlowStartedCallback.class));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypeVenmo_tokenizesVenmo() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        SupportedPaymentMethodSelectedEvent event =
                new SupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.PAY_WITH_VENMO);
        mActivity.onSupportedPaymentMethodSelectedEvent(event);

        verify(dropInClient).tokenizeVenmoAccount(same(mActivity), any(VenmoTokenizeAccountCallback.class));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypeGooglePay_requestsGooglePay() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        SupportedPaymentMethodSelectedEvent event =
                new SupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.GOOGLE_PAYMENT);
        mActivity.onSupportedPaymentMethodSelectedEvent(event);

        verify(dropInClient).requestGooglePayPayment(same(mActivity), any(GooglePayRequestPaymentCallback.class));
    }

    @Test
    public void onSupportedPaymentMethodSelected_withTypeUnknown_showsAddCardFragment() throws JSONException {
//        String authorization = Fixtures.TOKENIZATION_KEY;
//        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
//
//        DropInClient dropInClient = new MockDropInClientBuilder()
//                .authorization(Authorization.fromString(authorization))
//                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
//                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
//                .build();
//        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
//        mActivityController.setup();
//
//        SupportedPaymentMethodSelectedEvent event =
//                new SupportedPaymentMethodSelectedEvent(DropInPaymentMethodType.UNKNOWN);
//        mActivity.onSupportedPaymentMethodSelectedEvent(event);
//
//        ShadowActivity.IntentForResult intent = mShadowActivity.peekNextStartedActivityForResult();
//        assertEquals(AddCardActivity.class.getName(), intent.intent.getComponent().getClassName());
    }

    @Test
    public void onActivityResult_whenAddCardReturnsSuccessfully_showsLoader() throws JSONException {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .getConfigurationSuccess(Configuration.fromJson(Fixtures.CONFIGURATION_WITH_GOOGLE_PAY_AND_CARD_AND_PAYPAL))
                .getSupportedPaymentMethodsSuccess(new ArrayList<DropInPaymentMethodType>())
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);

        mActivity.onActivityResult(ADD_CARD_REQUEST_CODE, RESULT_OK, data);

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onActivityResult_whenAddCardCanceled_removesLoadingIndicator() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onActivityResult(1, RESULT_CANCELED, null);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onActivityResult_whenPaymentMethodNonceDeleted_removesLoader() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);
        setupDropInActivity(authorization, mock(DropInClient.class), dropInRequest, "sessionId");
        mActivityController.setup();

        PayPalAccountNonce paypalNonce = mock(PayPalAccountNonce.class);

        ArrayList<Parcelable> paymentMethodNonces = new ArrayList<>();
        paymentMethodNonces.add(paypalNonce);

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onActivityResult(DELETE_PAYMENT_METHOD_NONCE_CODE, RESULT_OK, new Intent()
                .putExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES", paymentMethodNonces));

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onActivityResult_whenEditingFinished_removesLoadingIndicator() {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

        DropInClient dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(authorization))
                .build();
        setupDropInActivity(authorization, dropInClient, dropInRequest, "sessionId");
        mActivityController.setup();

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onActivityResult(2, RESULT_OK, null);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    private void assertExceptionIsReturned(String analyticsEvent, Exception exception) {
        String authorization = Fixtures.TOKENIZATION_KEY;
        DropInRequest dropInRequest = new DropInRequest().tokenizationKey(authorization);

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

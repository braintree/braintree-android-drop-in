package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.internal.BraintreeSharedPreferences;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import static com.braintreepayments.api.test.ReflectionHelper.getField;
import static com.braintreepayments.api.test.ReflectionHelper.setField;
import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class DropInActivityUnitTest {

    private ActivityController mActivityController;
    private DropInUnitTestActivity mActivity;
    private ShadowActivity mShadowActivity;

    @Before
    public void setup() {
        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mShadowActivity = shadowOf(mActivity);
    }

    @Test
    public void returnsExceptionWhenBraintreeFragmentSetupFails() {
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey("not a tokenization key"));

        mActivityController.setup();

        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        assertEquals("Tokenization Key or client token was invalid.", mShadowActivity.getResultIntent()
                .getStringExtra(DropInActivity.EXTRA_ERROR));
    }

    @Test
    public void returnsExceptionWhenAuthorizationIsEmpty() {
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey(null));

        mActivityController.setup();

        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        assertEquals("A client token or client key must be specified in the DropInRequest", mShadowActivity.getResultIntent()
                .getStringExtra(DropInActivity.EXTRA_ERROR));
    }

    @Test
    public void setsIntegrationTypeToDropinForDropinActivity() throws NoSuchFieldException,
            IllegalAccessException {
        setup(new BraintreeUnitTestHttpClient());

        assertEquals("dropin2", getField(mActivity.braintreeFragment, "mIntegrationType"));
    }

    @Test
    public void sendsAnalyticsEventWhenShown() {
        setup(mock(BraintreeFragment.class));

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("appeared");
    }

    @Test
    public void doesNotSendAnalyticsEventTwiceWhenRecreated() {
        BraintreeFragment fragment = mock(BraintreeFragment.class);
        setup(fragment);

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mActivity.braintreeFragment = fragment;
        mActivityController.setup(bundle);
        mActivity.braintreeFragment.onAttach(mActivity);
        mActivity.braintreeFragment.onResume();

        verify(mActivity.braintreeFragment, times(1)).sendAnalyticsEvent("appeared");
    }

    @Test
    public void showsLoadingIndicatorWhenWaitingForConfiguration() {
        setup(new BraintreeUnitTestHttpClient());

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onCancel_hidesLoadingView() {
        setup(new BraintreeUnitTestHttpClient());
        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onCancel(0);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void handlesConfigurationChanges() {
        String configuration = new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes("Visa"))
                .paypalEnabled(true)
                .build();
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY);
        mActivity.setDropInRequest(dropInRequest);
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(configuration);
        setup(httpClient);
        assertEquals(2, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mActivity.setDropInRequest(dropInRequest);
        mActivity.httpClient = httpClient;
        mActivityController.setup(bundle);
        mActivity.braintreeFragment.onAttach(mActivity);
        mActivity.braintreeFragment.onResume();

        assertEquals(2, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());
    }

    @Test
    public void handlesConfigurationChangesWithVaultedPaymentMethods() {
        String configuration = new TestConfigurationBuilder()
                .creditCards(new TestConfigurationBuilder.TestCardConfigurationBuilder()
                        .supportedCardTypes("Visa"))
                .paypalEnabled(true)
                .build();
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(stringFromFixture("client_token.json"));
        mActivity.setDropInRequest(dropInRequest);
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(configuration)
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, stringFromFixture("responses/get_payment_methods_two_cards_response.json"));
        setup(httpClient);
        assertEquals(2, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());
        assertEquals(2, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mActivity.setDropInRequest(dropInRequest);
        mActivity.httpClient = httpClient;
        mActivityController.setup(bundle);
        mActivity.braintreeFragment.onAttach(mActivity);
        mActivity.braintreeFragment.onResume();

        assertEquals(2, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());
        assertEquals(2, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());
    }

    @Test
    public void onSaveInstanceState_savesDeviceData() throws NoSuchFieldException,
            IllegalAccessException {
        mActivityController.setup();
        setField(DropInActivity.class, mActivity, "mDeviceData", "device-data-string");

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mActivityController.setup(bundle);

        assertEquals("device-data-string", getField(DropInActivity.class, mActivity, "mDeviceData"));
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() {
        setup(mock(BraintreeFragment.class));

        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void pressingBackSendsAnalyticsEvent() {
        setup(mock(BraintreeFragment.class));

        mActivity.onBackPressed();

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit.canceled");
    }

    @Test
    public void touchingOutsideSheetTriggersBackPress() {
        setup(mock(BraintreeFragment.class));

        mActivity.onBackgroundClicked(null);

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, mShadowActivity.getResultCode());
    }

    @Test
    public void touchingOutsideSheetSendsAnalyticsEvent() {
        setup(mock(BraintreeFragment.class));

        mActivity.onBackgroundClicked(null);

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit.canceled");
    }

    @Test
    public void onPaymentMethodSelected_showsLoadingView() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder().build()));
        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onPaymentMethodSelected(PaymentMethodType.UNKNOWN);

        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onPaymentMethodNonceCreated_returnsANonce() throws JSONException {
        mActivityController.setup();
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(cardNonce.getNonce(), result.getPaymentMethodNonce().getNonce());
        assertEquals(cardNonce.getLastTwo(), ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void onPaymentMethodNonceCreated_sendsAnAnalyticsEvent() throws JSONException {
        setup(mock(BraintreeFragment.class));

        mActivity.onPaymentMethodNonceCreated(CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json")));

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit.success");
    }

    @Test
    public void onPaymentMethodNonceCreated_storesPaymentMethodType() throws JSONException {
        mActivityController.setup();
        assertNull(BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));

        mActivity.onPaymentMethodNonceCreated(CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json")));

        assertEquals(PaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(mActivity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void onPaymentMethodNonceCreated_returnsDeviceData() throws JSONException {
        mActivity.mDropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true);
        mActivity.httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build());
        mActivityController.setup();
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertNotNull(result.getDeviceData());
    }

    @Test
    public void selectingAVaultedPaymentMethod_returnsANonce() throws JSONException {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_response.json"));
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        RecyclerView recyclerView = ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods));
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
        recyclerView.findViewHolderForAdapterPosition(0).itemView.callOnClick();

        PaymentMethodNonce paymentMethodNonce = PaymentMethodNonce.parsePaymentMethodNonces(
                stringFromFixture("responses/get_payment_methods_response.json")).get(0);
        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        assertEquals(paymentMethodNonce.getNonce(),
                ((DropInResult) mShadowActivity.getResultIntent()
                        .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT))
                        .getPaymentMethodNonce()
                        .getNonce());
    }

    @Test
    public void onPaymentMethodNoncesUpdated_showsVaultedPaymentMethods() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                       stringFromFixture("responses/get_payment_methods_response.json"));
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods)).isShown();
        assertEquals(3, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());
        assertThat((TextView) mActivity.findViewById(R.id.bt_supported_payment_methods_header)).hasText(R.string.bt_other);
    }

    @Test
    public void onPaymentMethodNoncesUpdated_showsNothingIfNoVaultedPaymentMethods() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_empty_response.json"));
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods)).isNotShown();
        assertThat((TextView) mActivity.findViewById(R.id.bt_supported_payment_methods_header)).hasText(R.string.bt_select_payment_method);
    }

    @Test
    public void onActivityResult_cancelHidesLoadingView() {
        setup(new BraintreeUnitTestHttpClient());
        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onActivityResult(1, Activity.RESULT_CANCELED, null);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onActivityResult_returnsNonceFromAddCardActivity() throws JSONException {
        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJson(
                        stringFromFixture("responses/visa_credit_card_response.json")));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
        mActivityController.setup();

        mActivity.onActivityResult(1, Activity.RESULT_OK, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        DropInResult response = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(result.getPaymentMethodType(), response.getPaymentMethodType());
        assertEquals(result.getPaymentMethodNonce(), response.getPaymentMethodNonce());
    }

    @Test
    public void onActivityResult_returnsDeviceData() throws JSONException, NoSuchFieldException,
            IllegalAccessException {
        mActivity.mDropInRequest = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .collectDeviceData(true);
        mActivity.httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build());
        mActivityController.setup();
        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJson(
                        stringFromFixture("responses/visa_credit_card_response.json")));

        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);

        mActivity.onActivityResult(1, Activity.RESULT_OK, data);

        DropInResult response = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertNotNull(response.getDeviceData());
    }

    @Test
    public void onActivityResult_returnsErrorFromAddCardActivity() {
        Intent data = new Intent()
                .putExtra(DropInActivity.EXTRA_ERROR, "Error");
        mActivityController.setup();
        mActivity.onActivityResult(1, Activity.RESULT_FIRST_USER, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        assertEquals("Error", mShadowActivity.getResultIntent().getStringExtra(DropInActivity.EXTRA_ERROR));
    }

    @Test
    public void onActivityResult_storesPaymentMethodType() throws JSONException {
        mActivityController.setup();
        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJson(
                        stringFromFixture("responses/visa_credit_card_response.json")));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
        assertNull(BraintreeSharedPreferences.getSharedPreferences(mActivity)
                .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));

        mActivity.onActivityResult(1, Activity.RESULT_OK, data);

        assertEquals(PaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(mActivity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void configurationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("configuration-exception",
                new ConfigurationException("Configuration exception"));
    }

    @Test
    public void authenticationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error",
                new AuthenticationException("Access denied"));
    }

    @Test
    public void authorizationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error",
                new AuthorizationException("Access denied"));
    }

    @Test
    public void upgradeRequiredExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error",
                new UpgradeRequiredException("Exception"));
    }

    @Test
    public void serverExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error",
                new ServerException("Exception"));
    }

    @Test
    public void unexpectedExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error",
                new UnexpectedException("Exception"));
    }

    @Test
    public void downForMaintenanceExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-unavailable",
                new DownForMaintenanceException("Exception"));
    }

    @Test
    public void anyExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("sdk-error", new Exception("Error!"));
    }

    private void setup(BraintreeFragment fragment) {
        mActivity.braintreeFragment = fragment;
        mActivityController.setup();
    }

    private void setup(BraintreeUnitTestHttpClient httpClient) {
        mActivity.httpClient = httpClient;
        mActivityController.setup();
    }

    private void assertExceptionIsReturned(String analyticsEvent, Exception exception) {
        mActivity.onError(exception);

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }

    @Test
    public void onConfigurationFetched_whenAndroidPayDisabledClientSide_doesNotShowAndroidPay() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .androidPay(new TestConfigurationBuilder.TestAndroidPayConfigurationBuilder())
                        .build());
        mActivity.setDropInRequest(new DropInRequest()
                .disableAndroidPay()
                .tokenizationKey(TOKENIZATION_KEY));
        setup(httpClient);

        assertEquals(0, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());
    }
}

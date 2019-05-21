package com.braintreepayments.api.dropin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.BraintreeSharedPreferences;
import com.braintreepayments.api.models.BraintreeRequestCodes;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED;
import static androidx.appcompat.app.AppCompatActivity.RESULT_FIRST_USER;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static com.braintreepayments.api.dropin.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;
import static com.braintreepayments.api.dropin.DropInRequest.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.test.PackageManagerUtils.mockPackageManagerSupportsThreeDSecure;
import static com.braintreepayments.api.test.ReflectionHelper.getField;
import static com.braintreepayments.api.test.ReflectionHelper.setField;
import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
        mActivityController = Robolectric.buildActivity(DropInUnitTestActivity.class);
        mActivity = (DropInUnitTestActivity) mActivityController.get();
        mShadowActivity = shadowOf(mActivity);
    }

    @Test
    public void returnsExceptionWhenBraintreeFragmentSetupFails() {
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey("not a tokenization key"));

        mActivityController.setup();

        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception exception = (Exception) mShadowActivity.getResultIntent()
                        .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertTrue(exception instanceof InvalidArgumentException);
        assertEquals("Tokenization Key or client token was invalid.", exception.getMessage());
    }

    @Test
    public void returnsExceptionWhenAuthorizationIsEmpty() {
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey(null));

        mActivityController.setup();

        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception exception = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertTrue(exception instanceof InvalidArgumentException);
        assertEquals("A client token or tokenization key must be specified in the DropInRequest",
                exception.getMessage());
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
    public void onConfigurationFetched_whenGooglePaymentDisabledClientSide_doesNotShowGooglePayment() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .googlePayment(new TestConfigurationBuilder.TestGooglePaymentConfigurationBuilder()
                        .enabled(true))
                        .build());
        mActivity.setDropInRequest(new DropInRequest()
                .disableGooglePayment()
                .tokenizationKey(TOKENIZATION_KEY));
        setup(httpClient);

        assertEquals(0, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());
    }

    @Test
    public void onConfigurationFetched_whenGooglePayDisabledClientSide_doesNotShowGooglePay() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .googlePayment(new TestConfigurationBuilder.TestGooglePaymentConfigurationBuilder()
                                .enabled(true))
                        .build());
        mActivity.setDropInRequest(new DropInRequest()
                .disableGooglePayment()
                .tokenizationKey(TOKENIZATION_KEY));
        setup(httpClient);

        assertEquals(0, ((ListView) mActivity.findViewById(R.id.bt_supported_payment_methods)).getAdapter().getCount());
    }

    @Test
    public void onCancel_hidesLoadingView() {
        setup(new BraintreeUnitTestHttpClient());
        assertEquals(0, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());

        mActivity.onCancel(0);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onCancel_reloadsPaymentMethodsIfThreeDSecureWasRequestedPreviously()
            throws Exception {
        PackageManager packageManager = mockPackageManagerSupportsThreeDSecure();
        Context context = spy(RuntimeEnvironment.application);
        when(context.getPackageManager()).thenReturn(packageManager);
        mActivity.context = context;
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json"))
                .amount("1.00")
                .requestThreeDSecureVerification(true));
        mActivity.httpClient = spy(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .threeDSecureEnabled(true)
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json")));
        mActivityController.setup();
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);
        mActivity.onCancel(BraintreeRequestCodes.THREE_D_SECURE);

        verify(mActivity.httpClient, times(2)).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));
    }

    @Test
    public void onError_reloadsPaymentMethodsIfThreeDSecureWasRequestedPreviously()
            throws Exception {
        PackageManager packageManager = mockPackageManagerSupportsThreeDSecure();
        Context context = spy(RuntimeEnvironment.application);
        when(context.getPackageManager()).thenReturn(packageManager);
        mActivity.context = context;
        mActivity.setDropInRequest(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json"))
                .amount("1.00")
                .requestThreeDSecureVerification(true));
        mActivity.httpClient = spy(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .threeDSecureEnabled(true)
                        .build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json")));
        mActivityController.setup();
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);
        mActivity.onError(new Exception());

        verify(mActivity.httpClient, times(2)).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));
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
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
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
        assertEquals(RESULT_CANCELED, mShadowActivity.getResultCode());
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
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        DropInResult result = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(cardNonce.getNonce(), result.getPaymentMethodNonce().getNonce());
        assertEquals(cardNonce.getLastTwo(), ((CardNonce) result.getPaymentMethodNonce()).getLastTwo());
    }

    @Test
    public void onPaymentMethodNonceCreated_requestsThreeDSecureVerificationWhenEnabled()
            throws Exception {
        PackageManager packageManager = mockPackageManagerSupportsThreeDSecure();
        Context context = spy(RuntimeEnvironment.application);
        when(context.getPackageManager()).thenReturn(packageManager);
        mActivity.context = context;
        mActivity.setDropInRequest(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .requestThreeDSecureVerification(true));
        mActivity.httpClient = spy(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder()
                        .threeDSecureEnabled(true)
                        .build()));
        mActivityController.setup();
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);

        verify(mActivity.httpClient).post(matches(BraintreeUnitTestHttpClient.THREE_D_SECURE_LOOKUP),
                anyString(), any(HttpResponseCallback.class));
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
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
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

        RecyclerView recyclerView = mActivity.findViewById(R.id.bt_vaulted_payment_methods);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
        recyclerView.findViewHolderForAdapterPosition(0).itemView.callOnClick();

        PaymentMethodNonce paymentMethodNonce = PaymentMethodNonce.parsePaymentMethodNonces(
                stringFromFixture("responses/get_payment_methods_response.json")).get(0);
        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        assertEquals(paymentMethodNonce.getNonce(),
                ((DropInResult) mShadowActivity.getResultIntent()
                        .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT))
                        .getPaymentMethodNonce()
                        .getNonce());
    }

    @Test
    public void clickingVaultedPaymentMethod_whenCard_sendsAnalyticEvent() {
        PaymentMethodNonce nonce = mock(CardNonce.class);
        ArrayList<PaymentMethodNonce> nonces = new ArrayList<>();
        nonces.add(nonce);

        BraintreeFragment fragment = mock(BraintreeFragment.class);

        setup(fragment);

        mActivity.onPaymentMethodNoncesUpdated(nonces);
        RecyclerView recyclerView = mActivity.findViewById(R.id.bt_vaulted_payment_methods);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
        recyclerView.findViewHolderForAdapterPosition(0).itemView.callOnClick();

        verify(fragment).sendAnalyticsEvent("vaulted-card.select");
    }

    @Test
    public void clickingVaultedPaymentMethod_whenPayPal_doesNotSendAnalyticEvent() {
        PaymentMethodNonce nonce = mock(PayPalAccountNonce.class);
        ArrayList<PaymentMethodNonce> nonces = new ArrayList<>();
        nonces.add(nonce);

        BraintreeFragment fragment = mock(BraintreeFragment.class);

        setup(fragment);

        mActivity.onPaymentMethodNoncesUpdated(nonces);
        RecyclerView recyclerView = mActivity.findViewById(R.id.bt_vaulted_payment_methods);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);
        recyclerView.findViewHolderForAdapterPosition(0).itemView.callOnClick();

        verify(fragment, never()).sendAnalyticsEvent("vaulted-card.select");
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
        assertEquals(2, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());
        assertThat((TextView) mActivity.findViewById(R.id.bt_supported_payment_methods_header)).hasText(R.string.bt_other);
    }

    @Test
    public void onPaymentMethodNoncesUpdated_doesNotIncludeVaultedGooglePaymentCardNonces() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_google_pay_response.json"));
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods)).isShown();
        assertEquals(2, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());
        assertThat((TextView) mActivity.findViewById(R.id.bt_supported_payment_methods_header)).hasText(R.string.bt_other);
    }

    @Test
    public void onPaymentMethodNoncesUpdated_withACard_sendsAnalyticEvent() throws JSONException {
        setup(mock(BraintreeFragment.class));

        List<PaymentMethodNonce> nonceList = new ArrayList<>();

        nonceList.add(CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json")));

        mActivity.onPaymentMethodNoncesUpdated(nonceList);

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    public void onPaymentMethodNoncesUpdated_withNoCards_doesNotSendAnalyticEvent() throws JSONException {
        setup(mock(BraintreeFragment.class));

        List<PaymentMethodNonce> nonceList = new ArrayList<>();

        mActivity.onPaymentMethodNoncesUpdated(nonceList);

        verify(mActivity.braintreeFragment, never()).sendAnalyticsEvent("vaulted-card.appear");
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

        mActivity.onActivityResult(1, RESULT_CANCELED, null);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    public void onActivityResult_addCardCancelRefreshesVaultedPaymentMethods() {
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));
        BraintreeUnitTestHttpClient httpClient = spy(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build()))
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json"));
        setup(httpClient);
        verify(httpClient).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));

        mActivity.onActivityResult(1, RESULT_CANCELED, null);

        verify(httpClient, times(2)).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));
    }

    @Test
    public void onActivityResult_nonCardCancelDoesNotRefreshVaultedPaymentMethods() {
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));
        BraintreeUnitTestHttpClient httpClient = spy(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build()))
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json"));
        setup(httpClient);
        verify(httpClient).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));

        mActivity.onActivityResult(2, RESULT_CANCELED, null);

        verify(httpClient, times(1)).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));
    }

    @Test
    public void onActivityResult_returnsNonceFromAddCardActivity() throws JSONException {
        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJson(
                        stringFromFixture("responses/visa_credit_card_response.json")));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);
        mActivityController.setup();

        mActivity.onActivityResult(1, RESULT_OK, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(RESULT_OK, mShadowActivity.getResultCode());
        DropInResult response = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertEquals(result.getPaymentMethodType(), response.getPaymentMethodType());
        assertEquals(result.getPaymentMethodNonce(), response.getPaymentMethodNonce());
    }

    @Test
    public void onActivityResult_withPayPal_doesNotSendCardAnalyticEvent() {
        setup(mock(BraintreeFragment.class));

        PayPalAccountNonce paypalNonce = mock(PayPalAccountNonce.class);
        when(paypalNonce.getDescription()).thenReturn("paypal-nonce");

        ArrayList<Parcelable> paymentMethodNonces = new ArrayList<Parcelable>();
        paymentMethodNonces.add(paypalNonce);

        mActivity.onActivityResult(2, RESULT_OK, new Intent()
                .putExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES", paymentMethodNonces));

        verify(mActivity.braintreeFragment, never()).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    public void onActivityResult_nonceFromAddCardActivity_doesNotSendVaultAnalyticEvent() throws JSONException {
        setup(mock(BraintreeFragment.class));

        DropInResult result = new DropInResult()
                .paymentMethodNonce(CardNonce.fromJson(
                        stringFromFixture("responses/visa_credit_card_response.json")));
        Intent data = new Intent()
                .putExtra(DropInResult.EXTRA_DROP_IN_RESULT, result);

        mActivity.onActivityResult(1, RESULT_OK, data);

        verify(mActivity.braintreeFragment, never()).sendAnalyticsEvent("vaulted-card.appear");
    }

    @Test
    public void onActivityResult_returnsDeviceData() throws JSONException {
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

        mActivity.onActivityResult(1, RESULT_OK, data);

        DropInResult response = mShadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertNotNull(response.getDeviceData());
    }

    @Test
    public void onActivityResult_returnsErrorFromAddCardActivity() {
        Intent data = new Intent()
                .putExtra(DropInActivity.EXTRA_ERROR, new Exception("Error"));
        mActivityController.setup();
        mActivity.onActivityResult(1, RESULT_FIRST_USER, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        assertEquals("Error",
                ((Exception) mShadowActivity.getResultIntent()
                        .getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage());
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

        mActivity.onActivityResult(1, RESULT_OK, data);

        assertEquals(PaymentMethodType.VISA.getCanonicalName(),
                BraintreeSharedPreferences.getSharedPreferences(mActivity)
                        .getString(DropInResult.LAST_USED_PAYMENT_METHOD_TYPE, null));
    }

    @Test
    public void onActivityResult_whenVaultManagerResultOk_refreshesVaultedPaymentMethods() {
        mActivity.setDropInRequest(new DropInRequest().clientToken(stringFromFixture("client_token.json")));

        BraintreeUnitTestHttpClient httpClient = spy(new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build()))
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_two_cards_response.json"));
        setup(httpClient);
        verify(httpClient).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));

        mActivity.onActivityResult(2, RESULT_OK, null);

        verify(httpClient, times(2)).get(matches(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS),
                any(HttpResponseCallback.class));
    }

    @Test
    public void onActivityResult_whenVaultManagerResultOk_setsVaultedPaymentMethodsFromVaultManager() {
        mActivityController.setup();

        PayPalAccountNonce paypalNonce = mock(PayPalAccountNonce.class);
        when(paypalNonce.getDescription()).thenReturn("paypal-nonce");

        ArrayList<Parcelable> paymentMethodNonces = new ArrayList<Parcelable>();
        paymentMethodNonces.add(paypalNonce);

        assertNull(((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter());

        mActivity.onActivityResult(2, RESULT_OK, new Intent()
                .putExtra("com.braintreepayments.api.EXTRA_PAYMENT_METHOD_NONCES", paymentMethodNonces));

        assertEquals(1, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods))
                .getAdapter().getItemCount());
    }

    @Test
    public void onActivityResult_whenVaultManagerResultOk_removesLoadingIndicator() {
        mActivityController.setup();

        mActivity.onActivityResult(2, RESULT_FIRST_USER, null);

        assertEquals(1, ((ViewSwitcher) mActivity.findViewById(R.id.bt_loading_view_switcher)).getDisplayedChild());
    }

    @Test
    /*
     * TODO this a temporary test for the work around to fix the Google Payment flow.
     *
     * BraintreeFragment starts the GooglePaymentActivity for result to tokenize
     * Google Payment. GooglePaymentActivity#onActivityResult is called from Google Payment
     * and a result is set, then the activity finishes.
     *
     * What should happen is BraintreeFragment#onActivityResult should be called.
     *
     * There seems to be a bug that BraintreeFragment#onActivityResult is bypassed and
     * DropInActivity#onActivityResult is called, with a random requestCode (79129).
     * DropInActivity doesn't understand this requestCode so it noops.
     *
     * The temporary fix is to forward the data back to BraintreeFragment when we detect
     * the 79129 requestCode.
     */
    public void onActivityResult_withRequestCode79129_callsBraintreeFragmentOnActivityResult() {
        Intent data = new Intent();

        setup(mock(BraintreeFragment.class));

        mActivity.onActivityResult(79129, RESULT_OK, data);

        verify(mActivity.braintreeFragment).onActivityResult(
                eq(BraintreeRequestCodes.GOOGLE_PAYMENT), eq(RESULT_OK), eq(data));
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

    @Test
    public void onCreate_vaultEditButtonIsInvisible() {
        mActivity.setDropInRequest(new DropInRequest()
                .vaultManager(true));

        mActivityController.setup();

        assertEquals(View.INVISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    public void vaultEditButton_whenVaultManagerEnabled_isVisible() {
        setup(mock(BraintreeFragment.class));

        List<PaymentMethodNonce> nonceList = new ArrayList<>();
        nonceList.add(mock(CardNonce.class));

        mActivity.setDropInRequest(new DropInRequest()
                .vaultManager(true));

        mActivity.onPaymentMethodNoncesUpdated(nonceList);

        assertEquals(View.VISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    public void vaultEditButton_whenVaultManagerUnspecified_isInvisible() {
        setup(mock(BraintreeFragment.class));

        List<PaymentMethodNonce> nonceList = new ArrayList<>();
        nonceList.add(mock(CardNonce.class));

        mActivity.setDropInRequest(new DropInRequest());

        mActivity.onPaymentMethodNoncesUpdated(nonceList);

        assertEquals(View.INVISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    public void vaultEditButton_whenVaultManagerDisabled_isInvisible() {
        setup(mock(BraintreeFragment.class));

        List<PaymentMethodNonce> nonceList = new ArrayList<>();
        nonceList.add(mock(CardNonce.class));

        mActivity.setDropInRequest(new DropInRequest()
                .vaultManager(false));

        mActivity.onPaymentMethodNoncesUpdated(nonceList);

        assertEquals(View.INVISIBLE, mActivity.findViewById(R.id.bt_vault_edit_button).getVisibility());
    }

    @Test
    public void onVaultEditButtonClick_sendsAnalyticEvent() {
        setup(mock(BraintreeFragment.class));

        mActivity.onVaultEditButtonClick(null);

        verify(mActivity.mBraintreeFragment).sendAnalyticsEvent("manager.appeared");
    }

    @Test
    public void onVaultEditButtonClick_launchesVaultManagerActivity() {
        setup(mock(BraintreeFragment.class));

        mActivity.onVaultEditButtonClick(null);

        ShadowActivity.IntentForResult intent = mShadowActivity.getNextStartedActivityForResult();

        assertEquals(2, intent.requestCode);
        assertEquals(mActivity.mDropInRequest, intent.intent.getParcelableExtra(EXTRA_CHECKOUT_REQUEST));
        assertEquals(mActivity.mBraintreeFragment.getCachedPaymentMethodNonces(),
                intent.intent.getParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES));
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
        assertEquals(RESULT_FIRST_USER, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }
}

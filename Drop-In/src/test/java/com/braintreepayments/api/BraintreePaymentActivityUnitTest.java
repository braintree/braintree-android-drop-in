package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.exceptions.AuthenticationException;
import com.braintreepayments.api.exceptions.AuthorizationException;
import com.braintreepayments.api.exceptions.ConfigurationException;
import com.braintreepayments.api.exceptions.DownForMaintenanceException;
import com.braintreepayments.api.exceptions.ServerException;
import com.braintreepayments.api.exceptions.UnexpectedException;
import com.braintreepayments.api.exceptions.UpgradeRequiredException;
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

import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class BraintreePaymentActivityUnitTest {

    private ActivityController mActivityController;
    private BraintreePaymentUnitTestActivity mActivity;
    private ShadowActivity mShadowActivity;

    @Before
    public void setup() {
        mActivityController = Robolectric.buildActivity(BraintreePaymentUnitTestActivity.class);
        mActivity = (BraintreePaymentUnitTestActivity) mActivityController.get();
        mShadowActivity = shadowOf(mActivity);
    }

    @Test
    public void returnsExceptionWhenBraintreeFragmentSetupFails() {
        mActivity.setPaymentRequest(new PaymentRequest()
                .tokenizationKey("not a tokenization key"));

        mActivityController.setup();

        assertEquals(BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR, mShadowActivity.getResultCode());
        assertEquals("Tokenization Key or client token was invalid.", mShadowActivity.getResultIntent()
                .getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE));
    }

    @Test
    public void returnsExceptionWhenAuthorizationIsEmpty() {
        mActivity.setPaymentRequest(new PaymentRequest()
                .tokenizationKey(null));

        mActivityController.setup();

        assertEquals(BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR, mShadowActivity.getResultCode());
        assertEquals("A client token or client key must be specified in the PaymentRequest", mShadowActivity.getResultIntent()
                .getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE));
    }

    @Test
    public void setsIntegrationTypeToDropinForDropinActivity() {
        setup(new BraintreeUnitTestHttpClient());

        assertEquals("dropin", mActivity.braintreeFragment.mIntegrationType);
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
        PaymentRequest paymentRequest = new PaymentRequest()
                .clientToken(stringFromFixture("client_token.json"));
        mActivity.setPaymentRequest(paymentRequest);
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(configuration)
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS, stringFromFixture("responses/get_payment_methods_two_cards_response.json"));
        setup(httpClient);
        assertEquals(2, ((ListView) mActivity.findViewById(R.id.bt_available_payment_methods)).getAdapter().getCount());
        assertEquals(2, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();

        mActivityController = Robolectric.buildActivity(BraintreePaymentUnitTestActivity.class);
        mActivity = (BraintreePaymentUnitTestActivity) mActivityController.get();
        mActivity.setPaymentRequest(paymentRequest);
        mActivity.httpClient = httpClient;
        mActivityController.setup(bundle);
        mActivity.braintreeFragment.onAttach(mActivity);
        mActivity.braintreeFragment.onResume();

        assertEquals(2, ((ListView) mActivity.findViewById(R.id.bt_available_payment_methods)).getAdapter().getCount());
        assertEquals(2, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());
    }

    @Test
    public void pressingBackExitsActivityWithResultCanceled() {
        setup(mock(BraintreeFragment.class));

        mActivity.onBackPressed();

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, mShadowActivity.getResultCode());
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
        CardNonce cardNonce = CardNonce.fromJson(stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        CardNonce returnedNonce = mShadowActivity.getResultIntent().getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
        assertEquals(cardNonce.getNonce(), returnedNonce.getNonce());
        assertEquals(cardNonce.getLastTwo(), returnedNonce.getLastTwo());
    }

    @Test
    public void selectingAVaultedPaymentMethod_returnsANonce() throws JSONException {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_response.json"));
        mActivity.setPaymentRequest(new PaymentRequest().clientToken(stringFromFixture("client_token.json")));
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
                ((PaymentMethodNonce) mShadowActivity.getResultIntent()
                        .getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE)).getNonce());
    }

    @Test
    public void onPaymentMethodNoncesUpdated_showsVaultedPaymentMethods() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                       stringFromFixture("responses/get_payment_methods_response.json"));
        mActivity.setPaymentRequest(new PaymentRequest().clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods)).isShown();
        assertEquals(3, ((RecyclerView) mActivity.findViewById(R.id.bt_vaulted_payment_methods)).getAdapter().getItemCount());
        assertThat((TextView) mActivity.findViewById(R.id.bt_available_payment_methods_header)).hasText(R.string.bt_other);
    }

    @Test
    public void onPaymentMethodNoncesUpdated_showsNothingIfNoVaultedPaymentMethods() {
        BraintreeUnitTestHttpClient httpClient = new BraintreeUnitTestHttpClient()
                .configuration(new TestConfigurationBuilder().build())
                .successResponse(BraintreeUnitTestHttpClient.GET_PAYMENT_METHODS,
                        stringFromFixture("responses/get_payment_methods_empty_response.json"));
        mActivity.setPaymentRequest(new PaymentRequest().clientToken(stringFromFixture("client_token.json")));
        setup(httpClient);

        assertThat(mActivity.findViewById(R.id.bt_vaulted_payment_methods)).isNotShown();
        assertThat((TextView) mActivity.findViewById(R.id.bt_available_payment_methods_header)).hasText(R.string.bt_select_payment_method);
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
        CardNonce cardNonce = CardNonce.fromJson(stringFromFixture("responses/visa_credit_card_response.json"));
        Intent data = new Intent()
                .putExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE, cardNonce);
        mActivityController.setup();
        mActivity.onActivityResult(1, Activity.RESULT_OK, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        assertEquals(cardNonce.getNonce(),
                ((PaymentMethodNonce) mShadowActivity.getResultIntent()
                        .getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE)).getNonce());
    }

    @Test
    public void onActivityResult_returnsErrorFromAddCardActivity() {
        Intent data = new Intent()
                .putExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE, "Error");
        mActivityController.setup();
        mActivity.onActivityResult(1, BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR, data);

        assertTrue(mShadowActivity.isFinishing());
        assertEquals(BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR, mShadowActivity.getResultCode());
        assertEquals("Error", mShadowActivity.getResultIntent().getStringExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE));
    }

    @Test
    public void configurationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("configuration-exception", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR,
                new ConfigurationException("Configuration exception"));
    }

    @Test
    public void authenticationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR,
                new AuthenticationException("Access denied"));
    }

    @Test
    public void authorizationExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR,
                new AuthorizationException("Access denied"));
    }

    @Test
    public void upgradeRequiredExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("developer-error", BraintreePaymentActivity.BRAINTREE_RESULT_DEVELOPER_ERROR,
                new UpgradeRequiredException("Exception"));
    }

    @Test
    public void serverExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR,
                new ServerException("Exception"));
    }

    @Test
    public void unexpectedExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-error", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR,
                new UnexpectedException("Exception"));
    }

    @Test
    public void downForMaintenanceExceptionExitsActivityWithError() {
        setup(mock(BraintreeFragment.class));

        assertExceptionIsReturned("server-unavailable", BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE,
                new DownForMaintenanceException("Exception"));
    }

    private void setup(BraintreeFragment fragment) {
        mActivity.braintreeFragment = fragment;
        mActivityController.setup();
    }

    private void setup(BraintreeUnitTestHttpClient httpClient) {
        mActivity.httpClient = httpClient;
        mActivityController.setup();
    }

    private void assertExceptionIsReturned(String analyticsEvent, int responseCode, Exception exception) {
        mActivity.onError(exception);

        verify(mActivity.braintreeFragment).sendAnalyticsEvent("sdk.exit." + analyticsEvent);
        assertTrue(mActivity.isFinishing());
        assertEquals(responseCode, mShadowActivity.getResultCode());
        Exception actualException = (Exception) mShadowActivity.getResultIntent()
                .getSerializableExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE);
        assertEquals(exception.getClass(), actualException.getClass());
        assertEquals(exception.getMessage(), actualException.getMessage());
    }
}

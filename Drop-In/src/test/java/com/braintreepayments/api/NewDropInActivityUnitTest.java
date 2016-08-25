package com.braintreepayments.api;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import android.widget.TextView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.testutils.TestConfigurationBuilder;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import static com.braintreepayments.testutils.FixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class NewDropInActivityUnitTest {

    private ActivityController mActivityController;
    private NewDropInUnitTestActivity mActivity;
    private ShadowActivity mShadowActivity;

    @Before
    public void setup() {
        mActivityController = Robolectric.buildActivity(NewDropInUnitTestActivity.class);
        mActivity = (NewDropInUnitTestActivity) mActivityController.get();
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
    public void getsConfigurationIfConfigurationHasAlreadyBeenFetched() {
        setup(new BraintreeUnitTestHttpClient().configuration(new TestConfigurationBuilder().build()));
        assertEquals(1, ((ListView) mActivity.findViewById(R.id.bt_available_payment_methods)).getAdapter().getCount());

        Bundle bundle = new Bundle();
        mActivityController.saveInstanceState(bundle)
                .pause()
                .stop()
                .destroy();
        mActivityController = Robolectric.buildActivity(NewDropInUnitTestActivity.class)
                .setup(bundle);
        mActivity = (NewDropInUnitTestActivity) mActivityController.get();

        assertEquals(1, ((ListView) mActivity.findViewById(R.id.bt_available_payment_methods)).getAdapter().getCount());
    }

    @Test
    public void handlesConfigurationChanges() {
        String configuration = new TestConfigurationBuilder()
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
        mActivityController = Robolectric.buildActivity(NewDropInUnitTestActivity.class)
                .setup(bundle);
        mActivity = (NewDropInUnitTestActivity) mActivityController.get();

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
    public void onPaymentMethodNonceCreated_returnsANonce() throws JSONException {
        mActivityController.setup();
        CardNonce cardNonce = CardNonce.fromJson(stringFromFixture("responses/visa_credit_card_response.json"));

        mActivity.onPaymentMethodNonceCreated(cardNonce);

        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, mShadowActivity.getResultCode());
        CardNonce returnedNonce = mShadowActivity.getResultIntent().getParcelableExtra(NewDropInActivity.EXTRA_PAYMENT_METHOD_NONCE);
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
                        .getParcelableExtra(NewDropInActivity.EXTRA_PAYMENT_METHOD_NONCE)).getNonce());
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

    private void setup(BraintreeFragment fragment) {
        mActivity.braintreeFragment = fragment;
        mActivityController.setup();
    }

    private void setup(BraintreeUnitTestHttpClient httpClient) {
        mActivity.httpClient = httpClient;
        mActivityController.setup();
    }
}

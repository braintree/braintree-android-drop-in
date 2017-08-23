package com.braintreepayments.api.dropin;

import android.app.Activity;
import android.content.Intent;

import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class BaseActivityUnitTest {

    private ActivityController mActivityController;
    private BaseActivity mActivity;

    @Test
    public void onCreate_setsDropInRequest() {
        Intent intent = new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application);
        mActivityController = Robolectric.buildActivity(BaseActivity.class, intent);
        mActivity = (BaseActivity) mActivityController.get();

        assertNull(mActivity.mDropInRequest);

        mActivityController.create();

        assertNotNull(mActivity.mDropInRequest);
        assertEquals(TOKENIZATION_KEY, mActivity.mDropInRequest.getAuthorization());
    }

    @Test
    public void getBraintreeFragment_returnsABraintreeFragment() throws InvalidArgumentException {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        assertNotNull(mActivity.getBraintreeFragment());
    }

    @Test(expected = InvalidArgumentException.class)
    public void getBraintreeFragment_throwsAnExceptionForEmptyAuthorization() throws InvalidArgumentException {
        setup(new DropInRequest().getIntent(RuntimeEnvironment.application));

        mActivity.getBraintreeFragment();
    }

    @Test
    public void getBraintreeFragment_setsClientTokenPresentWhenAClientTokenIsPresent() throws InvalidArgumentException {
        setup(new DropInRequest()
                .clientToken(stringFromFixture("client_token.json"))
                .getIntent(RuntimeEnvironment.application));

        mActivity.getBraintreeFragment();

        assertTrue(mActivity.mClientTokenPresent);
    }

    @Test
    public void getBraintreeFragment_setsClientTokenPresentWhenAClientTokenIsNotPresent() throws InvalidArgumentException {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        mActivity.getBraintreeFragment();

        assertFalse(mActivity.mClientTokenPresent);
    }

    @Test
    public void shouldRequestThreeDSecureVerification_returnsTrueWhenConditionsAreMet() {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .requestThreeDSecureVerification(true)
                .getIntent(RuntimeEnvironment.application));
        mActivity.mConfiguration = new TestConfigurationBuilder()
                .threeDSecureEnabled(true)
                .buildConfiguration();

        assertTrue(mActivity.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void shouldRequestThreeDSecureVerification_returnsFalseWhenNotEnabledInDropInRequest() {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .requestThreeDSecureVerification(false)
                .getIntent(RuntimeEnvironment.application));
        mActivity.mConfiguration = new TestConfigurationBuilder()
                .threeDSecureEnabled(true)
                .buildConfiguration();

        assertFalse(mActivity.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void shouldRequestThreeDSecureVerification_returnsFalseWhenNoAmountSpecified() {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .requestThreeDSecureVerification(true)
                .getIntent(RuntimeEnvironment.application));
        mActivity.mConfiguration = new TestConfigurationBuilder()
                .threeDSecureEnabled(true)
                .buildConfiguration();

        assertFalse(mActivity.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void shouldRequestThreeDSecureVerification_returnsFalseWhenThreeDSecureIsNotEnabled() {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .amount("1.00")
                .requestThreeDSecureVerification(true)
                .getIntent(RuntimeEnvironment.application));
        mActivity.mConfiguration = new TestConfigurationBuilder()
                .threeDSecureEnabled(false)
                .buildConfiguration();

        assertFalse(mActivity.shouldRequestThreeDSecureVerification());
    }

    @Test
    public void finish_finishesWithPaymentMethodNonceAndDeviceDataInDropInResult()
            throws JSONException {
        CardNonce cardNonce = CardNonce.fromJson(
                stringFromFixture("responses/visa_credit_card_response.json"));
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        mActivity.finish(cardNonce, "device_data");

        ShadowActivity shadowActivity = shadowOf(mActivity);
        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_OK, shadowActivity.getResultCode());
        DropInResult result = shadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertNotNull(result);
        assertEquals(cardNonce.getNonce(), result.getPaymentMethodNonce().getNonce());
        assertEquals("device_data", result.getDeviceData());
    }

    @Test
    public void finish_finishesWithException() {
        Exception exception = new Exception("Error message");
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        mActivity.finish(exception);

        ShadowActivity shadowActivity = shadowOf(mActivity);
        assertTrue(mActivity.isFinishing());
        assertEquals(Activity.RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception error = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInActivity.EXTRA_ERROR);
        assertNotNull(error);
        assertEquals("Error message", error.getMessage());
    }

    private void setup(Intent intent) {
        mActivityController = Robolectric.buildActivity(BaseActivity.class, intent)
                .create();
        mActivity = (BaseActivity) mActivityController.get();
    }
}

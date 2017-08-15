package com.braintreepayments.api.dropin;

import android.content.Intent;

import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.test.TestConfigurationBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import static com.braintreepayments.api.test.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.test.UnitTestFixturesHelper.stringFromFixture;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

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

    public void setup(Intent intent) {
        mActivityController = Robolectric.buildActivity(BaseActivity.class, intent)
                .create();
        mActivity = (BaseActivity) mActivityController.get();
    }
}

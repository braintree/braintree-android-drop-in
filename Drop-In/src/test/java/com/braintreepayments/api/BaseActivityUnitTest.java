package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import static androidx.appcompat.app.AppCompatActivity.RESULT_FIRST_USER;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;
import static com.braintreepayments.api.UnitTestFixturesHelper.base64EncodedClientTokenFromFixture;
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
    public void onCreate_setsConfigurationIfSaved() throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = new TestConfigurationBuilder().buildConfiguration();

        Bundle savedState = new Bundle();
        savedState.putString(BaseActivity.EXTRA_CONFIGURATION_DATA, configuration.toJson());
        mActivityController = Robolectric.buildActivity(BaseActivity.class);

        mActivityController.create(savedState);
        mActivity = (BaseActivity) mActivityController.get();

        Configuration restoredConfiguration = (Configuration) ReflectionHelper.getField(mActivity, "mConfiguration");
        assertNotNull(restoredConfiguration);
        assertNotNull(restoredConfiguration.getMerchantId());
        assertEquals(configuration.getMerchantId(), restoredConfiguration.getMerchantId());
    }

    @Test
    public void getDropInClient_returnsADropInClient() {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        assertNotNull(mActivity.getDropInClient());
    }

    @Test(expected = InvalidArgumentException.class)
    public void getDropInClient_throwsAnExceptionForEmptyAuthorization() {
        setup(new DropInRequest().getIntent(RuntimeEnvironment.application));

        mActivity.getDropInClient();
    }

    @Test
    public void getDropInClient_setsClientTokenPresentWhenAClientTokenIsPresent() {
        setup(new DropInRequest()
                .clientToken(base64EncodedClientTokenFromFixture(Fixtures.CLIENT_TOKEN))
                .getIntent(RuntimeEnvironment.application));

        mActivity.getDropInClient();

        assertTrue(mActivity.mClientTokenPresent);
    }

    @Test
    public void getDropInClient_setsClientTokenPresentWhenAClientTokenIsNotPresent() {
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        mActivity.getDropInClient();

        assertFalse(mActivity.mClientTokenPresent);
    }

    @Test
    public void finish_finishesWithPaymentMethodNonceAndDeviceDataInDropInResult()
            throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        setup(new DropInRequest()
                .tokenizationKey(TOKENIZATION_KEY)
                .getIntent(RuntimeEnvironment.application));

        mActivity.finish(cardNonce, "device_data");

        ShadowActivity shadowActivity = shadowOf(mActivity);
        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_OK, shadowActivity.getResultCode());
        DropInResult result = shadowActivity.getResultIntent()
                .getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
        assertNotNull(result);
        assertEquals(cardNonce.getString(), result.getPaymentMethodNonce().getString());
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
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
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

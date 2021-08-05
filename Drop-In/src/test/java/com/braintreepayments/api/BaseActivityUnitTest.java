package com.braintreepayments.api;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import static androidx.appcompat.app.AppCompatActivity.RESULT_FIRST_USER;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
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
        Intent intent = new Intent();
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, new DropInRequest());
        mActivityController = Robolectric.buildActivity(BaseActivity.class, intent);
        mActivity = (BaseActivity) mActivityController.get();

        assertNull(mActivity.mDropInRequest);

        mActivityController.create();

        assertNotNull(mActivity.mDropInRequest);
    }

    @Test
    public void getDropInClient_returnsADropInClient() {
        Intent intent = new Intent();
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, new DropInRequest());
        setup(intent);

        assertNotNull(mActivity.getDropInClient());
    }

    @Test
    public void getDropInClient_setsClientTokenPresentWhenAClientTokenIsNotPresent() {
        Intent intent = new Intent();
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, new DropInRequest());
        setup(intent);

        mActivity.getDropInClient();

        assertFalse(mActivity.mClientTokenPresent);
    }

    @Test
    public void finish_finishesWithPaymentMethodNonceAndDeviceDataInDropInResult()
            throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        Intent intent = new Intent();
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, new DropInRequest());
        setup(intent);

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
        Intent intent = new Intent();
        intent.putExtra(DropInClient.EXTRA_CHECKOUT_REQUEST, new DropInRequest());
        setup(intent);

        mActivity.finish(exception);

        ShadowActivity shadowActivity = shadowOf(mActivity);
        assertTrue(mActivity.isFinishing());
        assertEquals(RESULT_FIRST_USER, shadowActivity.getResultCode());
        Exception error = (Exception) shadowActivity.getResultIntent()
                .getSerializableExtra(DropInResult.EXTRA_ERROR);
        assertNotNull(error);
        assertEquals("Error message", error.getMessage());
    }

    private void setup(Intent intent) {
        mActivityController = Robolectric.buildActivity(BaseActivity.class, intent)
                .create();
        mActivity = (BaseActivity) mActivityController.get();
    }
}

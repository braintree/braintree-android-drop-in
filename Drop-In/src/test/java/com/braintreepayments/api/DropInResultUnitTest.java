package com.braintreepayments.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DropInResultUnitTest {

    @Test
    public void paymentMethodNonce_setsPaymentMethodTypeAndNonce() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInResult result = new DropInResult();
        result.setPaymentMethodNonce(cardNonce);

        assertEquals(DropInPaymentMethod.VISA, result.getPaymentMethodType());
        assertEquals(cardNonce, result.getPaymentMethodNonce());
    }

    @Test
    public void paymentMethodNonce_setsPaymentDescription() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInResult result = new DropInResult();
        result.setPaymentMethodNonce(cardNonce);

        assertEquals("1111", result.getPaymentDescription());
    }

    @Test
    public void paymentMethodNonce_isNullable() {
        DropInResult result = new DropInResult();
        result.setPaymentMethodNonce(null);

        assertNull(result.getPaymentMethodType());
        assertNull(result.getPaymentMethodNonce());
    }

    @Test
    public void deviceData_setsDeviceData() {
        DropInResult result = new DropInResult();
        result.setDeviceData("device_data");

        assertEquals("device_data", result.getDeviceData());
    }

    @Test
    public void deviceData_isNullable() {
        DropInResult result = new DropInResult();
        result.setDeviceData(null);

        assertNull(result.getDeviceData());
    }

    @Test
    public void isParcelable() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInResult result = new DropInResult();
        result.setPaymentMethodNonce(cardNonce);
        result.setDeviceData("device_data");
        Parcel parcel = Parcel.obtain();
        result.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        DropInResult parceled = DropInResult.CREATOR.createFromParcel(parcel);

        assertEquals(DropInPaymentMethod.VISA, parceled.getPaymentMethodType());
        assertNotNull(parceled.getPaymentMethodNonce());
        assertEquals(cardNonce.getString(), parceled.getPaymentMethodNonce().getString());
        assertEquals("device_data", parceled.getDeviceData());
        assertEquals("1111", parceled.getPaymentDescription());
    }
}

package com.braintreepayments.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import android.os.Parcel;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DropInResult2UnitTest {

    @Test
    public void isParcelable() throws JSONException {
        CardNonce cardNonce = CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE));
        DropInResult dropInResult = new DropInResult();
        dropInResult.setPaymentMethodNonce(cardNonce);
        dropInResult.setDeviceData("device_data");
        Exception error = new Exception("error");
        DropInResult2 result = new DropInResult2();
        result.setDropInResult(dropInResult);
        result.setError(error);

        Parcel parcel = Parcel.obtain();
        result.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        DropInResult2 parceled = DropInResult2.CREATOR.createFromParcel(parcel);

        assertNotNull(parceled.getDropInResult());
        assertNotNull(parceled.getError());
        assertEquals("error", parceled.getError().getMessage());
        assertEquals(DropInPaymentMethod.VISA, parceled.getDropInResult().getPaymentMethodType());
        assertNotNull(parceled.getDropInResult().getPaymentMethodNonce());
        assertEquals(cardNonce.getString(), parceled.getDropInResult().getPaymentMethodNonce().getString());
        assertEquals("device_data", parceled.getDropInResult().getDeviceData());
        assertEquals("1111", parceled.getDropInResult().getPaymentDescription());
    }
}
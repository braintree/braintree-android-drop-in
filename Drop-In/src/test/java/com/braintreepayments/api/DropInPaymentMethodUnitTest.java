package com.braintreepayments.api;

import com.braintreepayments.api.dropin.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class DropInPaymentMethodUnitTest {

    @Test
    public void getDrawable_returnsCorrectDrawables() {
        assertEquals(R.drawable.bt_ic_visa, DropInPaymentMethod.VISA.getDrawable());
        assertEquals(R.drawable.bt_ic_mastercard, DropInPaymentMethod.MASTERCARD.getDrawable());
        assertEquals(R.drawable.bt_ic_discover, DropInPaymentMethod.DISCOVER.getDrawable());
        assertEquals(R.drawable.bt_ic_amex, DropInPaymentMethod.AMEX.getDrawable());
        assertEquals(R.drawable.bt_ic_jcb, DropInPaymentMethod.JCB.getDrawable());
        assertEquals(R.drawable.bt_ic_diners_club, DropInPaymentMethod.DINERS_CLUB.getDrawable());
        assertEquals(R.drawable.bt_ic_maestro, DropInPaymentMethod.MAESTRO.getDrawable());
        assertEquals(R.drawable.bt_ic_unionpay, DropInPaymentMethod.UNIONPAY.getDrawable());
        assertEquals(R.drawable.bt_ic_paypal, DropInPaymentMethod.PAYPAL.getDrawable());
        assertEquals(R.drawable.bt_ic_unknown, DropInPaymentMethod.UNKNOWN.getDrawable());
        assertEquals(R.drawable.bt_ic_venmo, DropInPaymentMethod.VENMO.getDrawable());
        assertEquals(R.drawable.bt_ic_hiper, DropInPaymentMethod.HIPER.getDrawable());
        assertEquals(R.drawable.bt_ic_hipercard, DropInPaymentMethod.HIPERCARD.getDrawable());
    }

    @Test
    public void getVaultedDrawable_returnsCorrectDrawables() {
        assertEquals(R.drawable.bt_ic_vaulted_visa, DropInPaymentMethod.VISA.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_mastercard, DropInPaymentMethod.MASTERCARD.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_discover, DropInPaymentMethod.DISCOVER.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_amex, DropInPaymentMethod.AMEX.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_jcb, DropInPaymentMethod.JCB.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_diners_club, DropInPaymentMethod.DINERS_CLUB.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_maestro, DropInPaymentMethod.MAESTRO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unionpay, DropInPaymentMethod.UNIONPAY.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_paypal, DropInPaymentMethod.PAYPAL.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unknown, DropInPaymentMethod.UNKNOWN.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_venmo, DropInPaymentMethod.VENMO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_hiper, DropInPaymentMethod.HIPER.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_hipercard, DropInPaymentMethod.HIPERCARD.getVaultedDrawable());
    }

    @Test
    public void getLocalizedName_returnsCorrectString() {
        assertEquals(R.string.bt_descriptor_visa, DropInPaymentMethod.VISA.getLocalizedName());
        assertEquals(R.string.bt_descriptor_mastercard, DropInPaymentMethod.MASTERCARD.getLocalizedName());
        assertEquals(R.string.bt_descriptor_discover, DropInPaymentMethod.DISCOVER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_amex, DropInPaymentMethod.AMEX.getLocalizedName());
        assertEquals(R.string.bt_descriptor_jcb, DropInPaymentMethod.JCB.getLocalizedName());
        assertEquals(R.string.bt_descriptor_diners, DropInPaymentMethod.DINERS_CLUB.getLocalizedName());
        assertEquals(R.string.bt_descriptor_maestro, DropInPaymentMethod.MAESTRO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unionpay, DropInPaymentMethod.UNIONPAY.getLocalizedName());
        assertEquals(R.string.bt_descriptor_paypal, DropInPaymentMethod.PAYPAL.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unknown, DropInPaymentMethod.UNKNOWN.getLocalizedName());
        assertEquals(R.string.bt_descriptor_pay_with_venmo, DropInPaymentMethod.VENMO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hiper, DropInPaymentMethod.HIPER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hipercard, DropInPaymentMethod.HIPERCARD.getLocalizedName());
    }

    @Test
    public void containsOnlyKnownPaymentMethodTypes() {
        assertEquals(14, DropInPaymentMethod.values().length);
    }
}

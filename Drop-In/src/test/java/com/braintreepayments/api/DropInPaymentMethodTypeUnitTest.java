package com.braintreepayments.api;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.LinkedHashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class DropInPaymentMethodTypeUnitTest {

    @Test
    public void forType_returnsCorrectPaymentMethodType() throws JSONException {
        assertEquals(DropInPaymentMethodType.VISA, DropInPaymentMethodType.forType("Visa"));
        assertEquals(DropInPaymentMethodType.MASTERCARD, DropInPaymentMethodType.forType("MasterCard"));
        assertEquals(DropInPaymentMethodType.DISCOVER, DropInPaymentMethodType.forType("Discover"));
        assertEquals(DropInPaymentMethodType.AMEX, DropInPaymentMethodType.forType("American Express"));
        assertEquals(DropInPaymentMethodType.JCB, DropInPaymentMethodType.forType("JCB"));
        assertEquals(DropInPaymentMethodType.DINERS, DropInPaymentMethodType.forType("Diners"));
        assertEquals(DropInPaymentMethodType.MAESTRO, DropInPaymentMethodType.forType("Maestro"));
        assertEquals(DropInPaymentMethodType.UNIONPAY, DropInPaymentMethodType.forType("UnionPay"));
        assertEquals(DropInPaymentMethodType.PAYPAL, DropInPaymentMethodType.forType("PayPal"));
        assertEquals(DropInPaymentMethodType.UNKNOWN, DropInPaymentMethodType.forType("unknown"));
        assertEquals(DropInPaymentMethodType.PAY_WITH_VENMO, DropInPaymentMethodType.forType("Venmo"));
        assertEquals(DropInPaymentMethodType.HIPER, DropInPaymentMethodType.forType("Hiper"));
        assertEquals(DropInPaymentMethodType.HIPERCARD, DropInPaymentMethodType.forType("Hipercard"));

        assertEquals(DropInPaymentMethodType.VISA, DropInPaymentMethodType.forType(
                CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))));
        assertEquals(DropInPaymentMethodType.PAYPAL, DropInPaymentMethodType.forType(
                PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON))));
        assertEquals(DropInPaymentMethodType.UNKNOWN, DropInPaymentMethodType.forType(
                new PaymentMethodNonce("unknown-nonce", false)));
        assertEquals(DropInPaymentMethodType.PAY_WITH_VENMO, DropInPaymentMethodType.forType(
                new VenmoAccountNonce("venmo-nonce", "@venmo_user", false)));
    }

    @Test
    public void forType_returnsUnknownForRandomString() {
        assertEquals(DropInPaymentMethodType.UNKNOWN, DropInPaymentMethodType.forType("payment method"));
    }

    @Test
    public void getCardTypes_returnsCorrectCardTypeArray() throws JSONException {
        Set<String> supportedCardTypes = new LinkedHashSet<>();
        supportedCardTypes.add(DropInPaymentMethodType.VISA.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.MASTERCARD.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.DISCOVER.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.AMEX.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.JCB.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.DINERS.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.MAESTRO.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.UNIONPAY.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.PAYPAL.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.UNKNOWN.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.PAY_WITH_VENMO.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.HIPER.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethodType.HIPERCARD.getCanonicalName());

        CardType[] cardTypes = DropInPaymentMethodType.getCardsTypes(supportedCardTypes);

        assertEquals(10, cardTypes.length);
        assertEquals(CardType.VISA, cardTypes[0]);
        assertEquals(CardType.MASTERCARD, cardTypes[1]);
        assertEquals(CardType.DISCOVER, cardTypes[2]);
        assertEquals(CardType.AMEX, cardTypes[3]);
        assertEquals(CardType.JCB, cardTypes[4]);
        assertEquals(CardType.DINERS_CLUB, cardTypes[5]);
        assertEquals(CardType.MAESTRO, cardTypes[6]);
        assertEquals(CardType.UNIONPAY, cardTypes[7]);
        assertEquals(CardType.HIPER, cardTypes[8]);
        assertEquals(CardType.HIPERCARD, cardTypes[9]);
    }

    @Test
    public void getDrawable_returnsCorrectDrawables() {
        assertEquals(R.drawable.bt_ic_visa, DropInPaymentMethodType.VISA.getDrawable());
        assertEquals(R.drawable.bt_ic_mastercard, DropInPaymentMethodType.MASTERCARD.getDrawable());
        assertEquals(R.drawable.bt_ic_discover, DropInPaymentMethodType.DISCOVER.getDrawable());
        assertEquals(R.drawable.bt_ic_amex, DropInPaymentMethodType.AMEX.getDrawable());
        assertEquals(R.drawable.bt_ic_jcb, DropInPaymentMethodType.JCB.getDrawable());
        assertEquals(R.drawable.bt_ic_diners_club, DropInPaymentMethodType.DINERS.getDrawable());
        assertEquals(R.drawable.bt_ic_maestro, DropInPaymentMethodType.MAESTRO.getDrawable());
        assertEquals(R.drawable.bt_ic_unionpay, DropInPaymentMethodType.UNIONPAY.getDrawable());
        assertEquals(R.drawable.bt_ic_paypal, DropInPaymentMethodType.PAYPAL.getDrawable());
        assertEquals(R.drawable.bt_ic_unknown, DropInPaymentMethodType.UNKNOWN.getDrawable());
        assertEquals(R.drawable.bt_ic_venmo, DropInPaymentMethodType.PAY_WITH_VENMO.getDrawable());
        assertEquals(R.drawable.bt_ic_hiper, DropInPaymentMethodType.HIPER.getDrawable());
        assertEquals(R.drawable.bt_ic_hipercard, DropInPaymentMethodType.HIPERCARD.getDrawable());
    }

    @Test
    public void getVaultedDrawable_returnsCorrectDrawables() {
        assertEquals(R.drawable.bt_ic_vaulted_visa, DropInPaymentMethodType.VISA.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_mastercard, DropInPaymentMethodType.MASTERCARD.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_discover, DropInPaymentMethodType.DISCOVER.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_amex, DropInPaymentMethodType.AMEX.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_jcb, DropInPaymentMethodType.JCB.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_diners_club, DropInPaymentMethodType.DINERS.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_maestro, DropInPaymentMethodType.MAESTRO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unionpay, DropInPaymentMethodType.UNIONPAY.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_paypal, DropInPaymentMethodType.PAYPAL.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unknown, DropInPaymentMethodType.UNKNOWN.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_venmo, DropInPaymentMethodType.PAY_WITH_VENMO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_hiper, DropInPaymentMethodType.HIPER.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_hipercard, DropInPaymentMethodType.HIPERCARD.getVaultedDrawable());
    }

    @Test
    public void getLocalizedName_returnsCorrectString() {
        assertEquals(R.string.bt_descriptor_visa, DropInPaymentMethodType.VISA.getLocalizedName());
        assertEquals(R.string.bt_descriptor_mastercard, DropInPaymentMethodType.MASTERCARD.getLocalizedName());
        assertEquals(R.string.bt_descriptor_discover, DropInPaymentMethodType.DISCOVER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_amex, DropInPaymentMethodType.AMEX.getLocalizedName());
        assertEquals(R.string.bt_descriptor_jcb, DropInPaymentMethodType.JCB.getLocalizedName());
        assertEquals(R.string.bt_descriptor_diners, DropInPaymentMethodType.DINERS.getLocalizedName());
        assertEquals(R.string.bt_descriptor_maestro, DropInPaymentMethodType.MAESTRO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unionpay, DropInPaymentMethodType.UNIONPAY.getLocalizedName());
        assertEquals(R.string.bt_descriptor_paypal, DropInPaymentMethodType.PAYPAL.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unknown, DropInPaymentMethodType.UNKNOWN.getLocalizedName());
        assertEquals(R.string.bt_descriptor_pay_with_venmo, DropInPaymentMethodType.PAY_WITH_VENMO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hiper, DropInPaymentMethodType.HIPER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hipercard, DropInPaymentMethodType.HIPERCARD.getLocalizedName());
    }

    @Test
    public void getCanonicalName_returnsCorrectString() {
        assertEquals("Visa", DropInPaymentMethodType.VISA.getCanonicalName());
        assertEquals("MasterCard", DropInPaymentMethodType.MASTERCARD.getCanonicalName());
        assertEquals("Discover", DropInPaymentMethodType.DISCOVER.getCanonicalName());
        assertEquals("American Express", DropInPaymentMethodType.AMEX.getCanonicalName());
        assertEquals("JCB", DropInPaymentMethodType.JCB.getCanonicalName());
        assertEquals("Diners", DropInPaymentMethodType.DINERS.getCanonicalName());
        assertEquals("Maestro", DropInPaymentMethodType.MAESTRO.getCanonicalName());
        assertEquals("UnionPay", DropInPaymentMethodType.UNIONPAY.getCanonicalName());
        assertEquals("PayPal", DropInPaymentMethodType.PAYPAL.getCanonicalName());
        assertEquals("Unknown", DropInPaymentMethodType.UNKNOWN.getCanonicalName());
        assertEquals("Venmo", DropInPaymentMethodType.PAY_WITH_VENMO.getCanonicalName());
        assertEquals("Hiper", DropInPaymentMethodType.HIPER.getCanonicalName());
        assertEquals("Hipercard", DropInPaymentMethodType.HIPERCARD.getCanonicalName());
    }

    @Test
    public void containsOnlyKnownPaymentMethodTypes() {
        assertEquals(14, DropInPaymentMethodType.values().length);
    }
}

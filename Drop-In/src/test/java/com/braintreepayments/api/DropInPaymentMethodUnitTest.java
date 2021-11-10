package com.braintreepayments.api;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class DropInPaymentMethodUnitTest {

    @Test
    public void forType_returnsCorrectPaymentMethodType() throws JSONException {
        assertEquals(DropInPaymentMethod.VISA, DropInPaymentMethod.forType("Visa"));
        assertEquals(DropInPaymentMethod.MASTERCARD, DropInPaymentMethod.forType("MasterCard"));
        assertEquals(DropInPaymentMethod.DISCOVER, DropInPaymentMethod.forType("Discover"));
        assertEquals(DropInPaymentMethod.AMEX, DropInPaymentMethod.forType("American Express"));
        assertEquals(DropInPaymentMethod.JCB, DropInPaymentMethod.forType("JCB"));
        assertEquals(DropInPaymentMethod.DINERS, DropInPaymentMethod.forType("Diners"));
        assertEquals(DropInPaymentMethod.MAESTRO, DropInPaymentMethod.forType("Maestro"));
        assertEquals(DropInPaymentMethod.UNIONPAY, DropInPaymentMethod.forType("UnionPay"));
        assertEquals(DropInPaymentMethod.PAYPAL, DropInPaymentMethod.forType("PayPal"));
        assertEquals(DropInPaymentMethod.UNKNOWN, DropInPaymentMethod.forType("unknown"));
        assertEquals(DropInPaymentMethod.PAY_WITH_VENMO, DropInPaymentMethod.forType("Venmo"));
        assertEquals(DropInPaymentMethod.HIPER, DropInPaymentMethod.forType("Hiper"));
        assertEquals(DropInPaymentMethod.HIPERCARD, DropInPaymentMethod.forType("Hipercard"));

        assertEquals(DropInPaymentMethod.VISA, DropInPaymentMethod.forType(
                CardNonce.fromJSON(new JSONObject(Fixtures.VISA_CREDIT_CARD_RESPONSE))));
        assertEquals(DropInPaymentMethod.PAYPAL, DropInPaymentMethod.forType(
                PayPalAccountNonce.fromJSON(new JSONObject(Fixtures.PAYPAL_ACCOUNT_JSON))));
        assertEquals(DropInPaymentMethod.UNKNOWN, DropInPaymentMethod.forType(
                new PaymentMethodNonce("unknown-nonce", false)));
        assertEquals(DropInPaymentMethod.PAY_WITH_VENMO, DropInPaymentMethod.forType(
                new VenmoAccountNonce("venmo-nonce", "@venmo_user", false)));
    }

    @Test
    public void forType_returnsUnknownForRandomString() {
        assertEquals(DropInPaymentMethod.UNKNOWN, DropInPaymentMethod.forType("payment method"));
    }

    @Test
    public void getCardTypes_returnsCorrectCardTypeArray() {
        List<String> supportedCardTypes = new LinkedList<>();
        supportedCardTypes.add(DropInPaymentMethod.VISA.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.MASTERCARD.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.DISCOVER.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.AMEX.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.JCB.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.DINERS.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.MAESTRO.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.UNIONPAY.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.PAYPAL.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.UNKNOWN.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.PAY_WITH_VENMO.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.HIPER.getCanonicalName());
        supportedCardTypes.add(DropInPaymentMethod.HIPERCARD.getCanonicalName());

        CardType[] cardTypes = DropInPaymentMethod.getCardsTypes(supportedCardTypes);

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
        assertEquals(R.drawable.bt_ic_visa, DropInPaymentMethod.VISA.getDrawable());
        assertEquals(R.drawable.bt_ic_mastercard, DropInPaymentMethod.MASTERCARD.getDrawable());
        assertEquals(R.drawable.bt_ic_discover, DropInPaymentMethod.DISCOVER.getDrawable());
        assertEquals(R.drawable.bt_ic_amex, DropInPaymentMethod.AMEX.getDrawable());
        assertEquals(R.drawable.bt_ic_jcb, DropInPaymentMethod.JCB.getDrawable());
        assertEquals(R.drawable.bt_ic_diners_club, DropInPaymentMethod.DINERS.getDrawable());
        assertEquals(R.drawable.bt_ic_maestro, DropInPaymentMethod.MAESTRO.getDrawable());
        assertEquals(R.drawable.bt_ic_unionpay, DropInPaymentMethod.UNIONPAY.getDrawable());
        assertEquals(R.drawable.bt_ic_paypal, DropInPaymentMethod.PAYPAL.getDrawable());
        assertEquals(R.drawable.bt_ic_unknown, DropInPaymentMethod.UNKNOWN.getDrawable());
        assertEquals(R.drawable.bt_ic_venmo, DropInPaymentMethod.PAY_WITH_VENMO.getDrawable());
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
        assertEquals(R.drawable.bt_ic_vaulted_diners_club, DropInPaymentMethod.DINERS.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_maestro, DropInPaymentMethod.MAESTRO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unionpay, DropInPaymentMethod.UNIONPAY.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_paypal, DropInPaymentMethod.PAYPAL.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unknown, DropInPaymentMethod.UNKNOWN.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_venmo, DropInPaymentMethod.PAY_WITH_VENMO.getVaultedDrawable());
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
        assertEquals(R.string.bt_descriptor_diners, DropInPaymentMethod.DINERS.getLocalizedName());
        assertEquals(R.string.bt_descriptor_maestro, DropInPaymentMethod.MAESTRO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unionpay, DropInPaymentMethod.UNIONPAY.getLocalizedName());
        assertEquals(R.string.bt_descriptor_paypal, DropInPaymentMethod.PAYPAL.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unknown, DropInPaymentMethod.UNKNOWN.getLocalizedName());
        assertEquals(R.string.bt_descriptor_pay_with_venmo, DropInPaymentMethod.PAY_WITH_VENMO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hiper, DropInPaymentMethod.HIPER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hipercard, DropInPaymentMethod.HIPERCARD.getLocalizedName());
    }

    @Test
    public void getCanonicalName_returnsCorrectString() {
        assertEquals("Visa", DropInPaymentMethod.VISA.getCanonicalName());
        assertEquals("MasterCard", DropInPaymentMethod.MASTERCARD.getCanonicalName());
        assertEquals("Discover", DropInPaymentMethod.DISCOVER.getCanonicalName());
        assertEquals("American Express", DropInPaymentMethod.AMEX.getCanonicalName());
        assertEquals("JCB", DropInPaymentMethod.JCB.getCanonicalName());
        assertEquals("Diners", DropInPaymentMethod.DINERS.getCanonicalName());
        assertEquals("Maestro", DropInPaymentMethod.MAESTRO.getCanonicalName());
        assertEquals("UnionPay", DropInPaymentMethod.UNIONPAY.getCanonicalName());
        assertEquals("PayPal", DropInPaymentMethod.PAYPAL.getCanonicalName());
        assertEquals("Unknown", DropInPaymentMethod.UNKNOWN.getCanonicalName());
        assertEquals("Venmo", DropInPaymentMethod.PAY_WITH_VENMO.getCanonicalName());
        assertEquals("Hiper", DropInPaymentMethod.HIPER.getCanonicalName());
        assertEquals("Hipercard", DropInPaymentMethod.HIPERCARD.getCanonicalName());
    }

    @Test
    public void containsOnlyKnownPaymentMethodTypes() {
        assertEquals(14, DropInPaymentMethod.values().length);
    }
}

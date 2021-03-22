package com.braintreepayments.api;

import com.braintreepayments.api.PaymentMethodType;
import com.braintreepayments.api.test.Fixtures;
import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.VenmoAccountNonce;
import com.braintreepayments.cardform.utils.CardType;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.LinkedHashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PaymentMethodTypeUnitTest {

    @Test
    public void forType_returnsCorrectPaymentMethodType() throws JSONException {
        assertEquals(PaymentMethodType.VISA, PaymentMethodType.forType("Visa"));
        assertEquals(PaymentMethodType.MASTERCARD, PaymentMethodType.forType("MasterCard"));
        assertEquals(PaymentMethodType.DISCOVER, PaymentMethodType.forType("Discover"));
        assertEquals(PaymentMethodType.AMEX, PaymentMethodType.forType("American Express"));
        assertEquals(PaymentMethodType.JCB, PaymentMethodType.forType("JCB"));
        assertEquals(PaymentMethodType.DINERS, PaymentMethodType.forType("Diners"));
        assertEquals(PaymentMethodType.MAESTRO, PaymentMethodType.forType("Maestro"));
        assertEquals(PaymentMethodType.UNIONPAY, PaymentMethodType.forType("UnionPay"));
        assertEquals(PaymentMethodType.PAYPAL, PaymentMethodType.forType("PayPal"));
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.forType("unknown"));
        assertEquals(PaymentMethodType.PAY_WITH_VENMO, PaymentMethodType.forType("Venmo"));
        assertEquals(PaymentMethodType.HIPER, PaymentMethodType.forType("Hiper"));
        assertEquals(PaymentMethodType.HIPERCARD, PaymentMethodType.forType("Hipercard"));

        assertEquals(PaymentMethodType.VISA, PaymentMethodType.forType(
                CardNonce.fromJson(Fixtures.VISA_CREDIT_CARD_RESPONSE)));
        assertEquals(PaymentMethodType.PAYPAL, PaymentMethodType.forType(new PayPalAccountNonce()));
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.forType(new PaymentMethodNonce() {
            @Override
            public String getTypeLabel() {
                return null;
            }
        }));
        assertEquals(PaymentMethodType.PAY_WITH_VENMO, PaymentMethodType.forType(new VenmoAccountNonce()));
    }

    @Test
    public void forType_returnsUnknownForRandomString() {
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.forType("payment method"));
    }

    @Test
    public void getCardTypes_returnsCorrectCardTypeArray() throws JSONException {
        Set<String> supportedCardTypes = new LinkedHashSet<>();
        supportedCardTypes.add(PaymentMethodType.VISA.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.MASTERCARD.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.DISCOVER.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.AMEX.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.JCB.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.DINERS.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.MAESTRO.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.UNIONPAY.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.PAYPAL.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.UNKNOWN.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.PAY_WITH_VENMO.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.HIPER.getCanonicalName());
        supportedCardTypes.add(PaymentMethodType.HIPERCARD.getCanonicalName());

        CardType[] cardTypes = PaymentMethodType.getCardsTypes(supportedCardTypes);

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
        assertEquals(R.drawable.bt_ic_visa, PaymentMethodType.VISA.getDrawable());
        assertEquals(R.drawable.bt_ic_mastercard, PaymentMethodType.MASTERCARD.getDrawable());
        assertEquals(R.drawable.bt_ic_discover, PaymentMethodType.DISCOVER.getDrawable());
        assertEquals(R.drawable.bt_ic_amex, PaymentMethodType.AMEX.getDrawable());
        assertEquals(R.drawable.bt_ic_jcb, PaymentMethodType.JCB.getDrawable());
        assertEquals(R.drawable.bt_ic_diners_club, PaymentMethodType.DINERS.getDrawable());
        assertEquals(R.drawable.bt_ic_maestro, PaymentMethodType.MAESTRO.getDrawable());
        assertEquals(R.drawable.bt_ic_unionpay, PaymentMethodType.UNIONPAY.getDrawable());
        assertEquals(R.drawable.bt_ic_paypal, PaymentMethodType.PAYPAL.getDrawable());
        assertEquals(R.drawable.bt_ic_unknown, PaymentMethodType.UNKNOWN.getDrawable());
        assertEquals(R.drawable.bt_ic_venmo, PaymentMethodType.PAY_WITH_VENMO.getDrawable());
        assertEquals(R.drawable.bt_ic_hiper, PaymentMethodType.HIPER.getDrawable());
        assertEquals(R.drawable.bt_ic_hipercard, PaymentMethodType.HIPERCARD.getDrawable());
    }

    @Test
    public void getVaultedDrawable_returnsCorrectDrawables() {
        assertEquals(R.drawable.bt_ic_vaulted_visa, PaymentMethodType.VISA.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_mastercard, PaymentMethodType.MASTERCARD.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_discover, PaymentMethodType.DISCOVER.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_amex, PaymentMethodType.AMEX.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_jcb, PaymentMethodType.JCB.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_diners_club, PaymentMethodType.DINERS.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_maestro, PaymentMethodType.MAESTRO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unionpay, PaymentMethodType.UNIONPAY.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_paypal, PaymentMethodType.PAYPAL.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_unknown, PaymentMethodType.UNKNOWN.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_venmo, PaymentMethodType.PAY_WITH_VENMO.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_hiper, PaymentMethodType.HIPER.getVaultedDrawable());
        assertEquals(R.drawable.bt_ic_vaulted_hipercard, PaymentMethodType.HIPERCARD.getVaultedDrawable());
    }

    @Test
    public void getLocalizedName_returnsCorrectString() {
        assertEquals(R.string.bt_descriptor_visa, PaymentMethodType.VISA.getLocalizedName());
        assertEquals(R.string.bt_descriptor_mastercard, PaymentMethodType.MASTERCARD.getLocalizedName());
        assertEquals(R.string.bt_descriptor_discover, PaymentMethodType.DISCOVER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_amex, PaymentMethodType.AMEX.getLocalizedName());
        assertEquals(R.string.bt_descriptor_jcb, PaymentMethodType.JCB.getLocalizedName());
        assertEquals(R.string.bt_descriptor_diners, PaymentMethodType.DINERS.getLocalizedName());
        assertEquals(R.string.bt_descriptor_maestro, PaymentMethodType.MAESTRO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unionpay, PaymentMethodType.UNIONPAY.getLocalizedName());
        assertEquals(R.string.bt_descriptor_paypal, PaymentMethodType.PAYPAL.getLocalizedName());
        assertEquals(R.string.bt_descriptor_unknown, PaymentMethodType.UNKNOWN.getLocalizedName());
        assertEquals(R.string.bt_descriptor_pay_with_venmo, PaymentMethodType.PAY_WITH_VENMO.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hiper, PaymentMethodType.HIPER.getLocalizedName());
        assertEquals(R.string.bt_descriptor_hipercard, PaymentMethodType.HIPERCARD.getLocalizedName());
    }

    @Test
    public void getCanonicalName_returnsCorrectString() {
        assertEquals("Visa", PaymentMethodType.VISA.getCanonicalName());
        assertEquals("MasterCard", PaymentMethodType.MASTERCARD.getCanonicalName());
        assertEquals("Discover", PaymentMethodType.DISCOVER.getCanonicalName());
        assertEquals("American Express", PaymentMethodType.AMEX.getCanonicalName());
        assertEquals("JCB", PaymentMethodType.JCB.getCanonicalName());
        assertEquals("Diners", PaymentMethodType.DINERS.getCanonicalName());
        assertEquals("Maestro", PaymentMethodType.MAESTRO.getCanonicalName());
        assertEquals("UnionPay", PaymentMethodType.UNIONPAY.getCanonicalName());
        assertEquals("PayPal", PaymentMethodType.PAYPAL.getCanonicalName());
        assertEquals("Unknown", PaymentMethodType.UNKNOWN.getCanonicalName());
        assertEquals("Venmo", PaymentMethodType.PAY_WITH_VENMO.getCanonicalName());
        assertEquals("Hiper", PaymentMethodType.HIPER.getCanonicalName());
        assertEquals("Hipercard", PaymentMethodType.HIPERCARD.getCanonicalName());
    }

    @Test
    public void containsOnlyKnownPaymentMethodTypes() {
        assertEquals(14, PaymentMethodType.values().length);
    }
}

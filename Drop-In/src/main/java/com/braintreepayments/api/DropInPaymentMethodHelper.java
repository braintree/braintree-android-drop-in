package com.braintreepayments.api;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

class DropInPaymentMethodHelper {

    @StringRes
    int getLocalizedName(DropInPaymentMethodType paymentMethodType) {
        switch (paymentMethodType) {
            case AMEX:
                return R.string.bt_descriptor_amex;
            case GOOGLE_PAY:
                return R.string.bt_descriptor_google_pay;
            case DINERS:
                return R.string.bt_descriptor_diners;
            case DISCOVER:
                return R.string.bt_descriptor_discover;
            case JCB:
                return R.string.bt_descriptor_jcb;
            case MAESTRO:
                return R.string.bt_descriptor_maestro;
            case MASTERCARD:
                return R.string.bt_descriptor_mastercard;
            case PAYPAL:
                return R.string.bt_descriptor_paypal;
            case VISA:
                return R.string.bt_descriptor_visa;
            case PAY_WITH_VENMO:
                return R.string.bt_descriptor_pay_with_venmo;
            case UNIONPAY:
                return R.string.bt_descriptor_unionpay;
            case HIPER:
                return R.string.bt_descriptor_hiper;
            case HIPERCARD:
                return R.string.bt_descriptor_hipercard;
            case UNKNOWN:
            default:
                return R.string.bt_descriptor_unknown;
        }
    }

    CardType parseCardType(String cardType) {
        switch (cardType) {
            case PaymentMethodCanonicalName.AMEX:
                return CardType.AMEX;
            case PaymentMethodCanonicalName.DINERS_CLUB:
                return CardType.DINERS_CLUB;
            case PaymentMethodCanonicalName.DISCOVER:
                return CardType.DISCOVER;
            case PaymentMethodCanonicalName.JCB:
                return CardType.JCB;
            case PaymentMethodCanonicalName.MAESTRO:
                return CardType.MAESTRO;
            case PaymentMethodCanonicalName.MASTERCARD:
                return CardType.MASTERCARD;
            case PaymentMethodCanonicalName.VISA:
                return CardType.VISA;
            case PaymentMethodCanonicalName.UNION_PAY:
                return CardType.UNIONPAY;
            case PaymentMethodCanonicalName.HIPER:
                return CardType.HIPER;
            case PaymentMethodCanonicalName.HIPERCARD:
                return CardType.HIPERCARD;
            default:
                return null;
        }
    }
}

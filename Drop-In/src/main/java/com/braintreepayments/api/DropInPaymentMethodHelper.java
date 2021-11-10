package com.braintreepayments.api;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

class DropInPaymentMethodHelper {

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

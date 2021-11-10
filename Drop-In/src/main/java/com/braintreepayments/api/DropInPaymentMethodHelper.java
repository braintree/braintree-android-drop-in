package com.braintreepayments.api;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;

class DropInPaymentMethodHelper {

    @DrawableRes
    int getDrawable(DropInPaymentMethodType paymentMethodType) {
        // `getFrontResource` is pulling icons from android-card-form, `R.drawable` icons are drop-in internal
        switch (paymentMethodType) {
            case AMEX:
                return CardType.AMEX.getFrontResource();
            case GOOGLE_PAY:
                return R.drawable.bt_ic_google_pay;
            case DINERS:
                return CardType.DINERS_CLUB.getFrontResource();
            case DISCOVER:
                return CardType.DISCOVER.getFrontResource();
            case JCB:
                return CardType.JCB.getFrontResource();
            case MAESTRO:
                return CardType.MAESTRO.getFrontResource();
            case MASTERCARD:
                return CardType.MASTERCARD.getFrontResource();
            case PAYPAL:
                return R.drawable.bt_ic_paypal;
            case VISA:
                return CardType.VISA.getFrontResource();
            case PAY_WITH_VENMO:
                return R.drawable.bt_ic_venmo;
            case UNIONPAY:
                return CardType.UNIONPAY.getFrontResource();
            case HIPER:
                return CardType.HIPER.getFrontResource();
            case HIPERCARD:
                return CardType.HIPERCARD.getFrontResource();
            case UNKNOWN:
            default:
                return CardType.UNKNOWN.getFrontResource();
        }
    }

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

    @DrawableRes
    int getVaultedDrawable(DropInPaymentMethodType paymentMethodType) {
        switch (paymentMethodType) {
            case AMEX:
                return R.drawable.bt_ic_vaulted_amex;
            case GOOGLE_PAY:
                return 0;
            case DINERS:
                return R.drawable.bt_ic_vaulted_diners_club;
            case DISCOVER:
                return R.drawable.bt_ic_vaulted_discover;
            case JCB:
                return R.drawable.bt_ic_vaulted_jcb;
            case MAESTRO:
                return R.drawable.bt_ic_vaulted_maestro;
            case MASTERCARD:
                return R.drawable.bt_ic_vaulted_mastercard;
            case PAYPAL:
                return R.drawable.bt_ic_vaulted_paypal;
            case VISA:
                return R.drawable.bt_ic_vaulted_visa;
            case PAY_WITH_VENMO:
                return R.drawable.bt_ic_vaulted_venmo;
            case UNIONPAY:
                return R.drawable.bt_ic_vaulted_unionpay;
            case HIPER:
                return R.drawable.bt_ic_vaulted_hiper;
            case HIPERCARD:
                return R.drawable.bt_ic_vaulted_hipercard;
            case UNKNOWN:
            default:
                return R.drawable.bt_ic_vaulted_unknown;
        }
    }

    public CardType getCardType(DropInPaymentMethodType paymentMethod) {
        switch (paymentMethod) {
            case AMEX:
                return CardType.AMEX;
            case DINERS:
                return CardType.DINERS_CLUB;
            case DISCOVER:
                return CardType.DISCOVER;
            case JCB:
                return CardType.JCB;
            case MAESTRO:
                return CardType.MAESTRO;
            case MASTERCARD:
                return CardType.MASTERCARD;
            case VISA:
                return CardType.VISA;
            case UNIONPAY:
                return CardType.UNIONPAY;
            case HIPER:
                return CardType.HIPER;
            case HIPERCARD:
                return CardType.HIPERCARD;
            case GOOGLE_PAY:
            case PAYPAL:
            case PAY_WITH_VENMO:
            case UNKNOWN:
            default:
                return null;
        }
    }
}

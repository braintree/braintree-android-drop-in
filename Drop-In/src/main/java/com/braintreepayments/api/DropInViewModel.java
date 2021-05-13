package com.braintreepayments.api;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DropInViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<PaymentMethodNonce>> vaultedPaymentMethodNonces = new MutableLiveData<List<PaymentMethodNonce>>(new ArrayList<PaymentMethodNonce>());
    private final MutableLiveData<List<PaymentMethodType>> availablePaymentMethods = new MutableLiveData<List<PaymentMethodType>>(new ArrayList<PaymentMethodType>());

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    void setIsLoading(boolean value) {
        isLoading.postValue(value);
    }

    void setVaultedPaymentMethodNonces(List<PaymentMethodNonce> value) {
       vaultedPaymentMethodNonces.postValue(value);
    }

    public LiveData<List<PaymentMethodNonce>> getVaultedPaymentMethodNonces() {
        return vaultedPaymentMethodNonces;
    }

    void updateAvailablePaymentMethods(Context context, Configuration configuration, DropInRequest dropInRequest, boolean googlePayEnabled, boolean unionpaySupported) {
        List<PaymentMethodType> availablePaymentMethods = new ArrayList<>();
        if (dropInRequest.isPayPalEnabled() && configuration.isPayPalEnabled()) {
            availablePaymentMethods.add(PaymentMethodType.PAYPAL);
        }

        if (dropInRequest.isVenmoEnabled() && configuration.getPayWithVenmo().isEnabled(context)) {
            availablePaymentMethods.add(PaymentMethodType.PAY_WITH_VENMO);
        }

        if (dropInRequest.isCardEnabled()) {
            Set<String> supportedCardTypes =
                    new HashSet<>(configuration.getCardConfiguration().getSupportedCardTypes());
            if (!unionpaySupported) {
                supportedCardTypes.remove(PaymentMethodType.UNIONPAY.getCanonicalName());
            }
            if (supportedCardTypes.size() > 0) {
                availablePaymentMethods.add(PaymentMethodType.UNKNOWN);
            }
        }

        if (googlePayEnabled) {
            if (dropInRequest.isGooglePaymentEnabled()) {
                availablePaymentMethods.add(PaymentMethodType.GOOGLE_PAYMENT);
            }
        }

        this.availablePaymentMethods.postValue(availablePaymentMethods);
    }

    public LiveData<List<PaymentMethodType>> getAvailablePaymentMethods() {
        return availablePaymentMethods;
    }
}

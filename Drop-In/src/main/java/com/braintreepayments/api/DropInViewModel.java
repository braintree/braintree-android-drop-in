package com.braintreepayments.api;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class DropInViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final MutableLiveData<List<DropInPaymentMethodType>> supportedPaymentMethods = new MutableLiveData<>();
    private final MutableLiveData<List<PaymentMethodNonce>> vaultedPaymentMethods = new MutableLiveData<>();

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }

    void setVaultedPaymentMethods(List<PaymentMethodNonce> value) {
        vaultedPaymentMethods.setValue(value);
    }

    public LiveData<List<PaymentMethodNonce>> getVaultedPaymentMethods() {
        return vaultedPaymentMethods;
    }

    public void setSupportedPaymentMethods(List<DropInPaymentMethodType> supportedPaymentMethods) {
        this.supportedPaymentMethods.setValue(supportedPaymentMethods);
    }

    public LiveData<List<DropInPaymentMethodType>> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }
}

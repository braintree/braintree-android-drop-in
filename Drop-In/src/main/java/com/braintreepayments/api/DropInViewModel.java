package com.braintreepayments.api;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class DropInViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<DropInPaymentMethodType>> availablePaymentMethods =
        new MutableLiveData<List<DropInPaymentMethodType>>(new ArrayList<DropInPaymentMethodType>());

    private final MutableLiveData<List<PaymentMethodNonce>> vaultedPaymentMethodNonces =
        new MutableLiveData<List<PaymentMethodNonce>>(new ArrayList<PaymentMethodNonce>());

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }

    void setVaultedPaymentMethodNonces(List<PaymentMethodNonce> value) {
        vaultedPaymentMethodNonces.postValue(value);
    }

    public LiveData<List<PaymentMethodNonce>> getVaultedPaymentMethodNonces() {
        return vaultedPaymentMethodNonces;
    }

    public void setAvailablePaymentMethods(List<DropInPaymentMethodType> availablePaymentMethods) {
        this.availablePaymentMethods.setValue(availablePaymentMethods);
    }

    public LiveData<List<DropInPaymentMethodType>> getAvailablePaymentMethods() {
        return availablePaymentMethods;
    }
}

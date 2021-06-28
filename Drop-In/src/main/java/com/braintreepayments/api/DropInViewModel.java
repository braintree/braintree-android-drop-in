package com.braintreepayments.api;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.braintreepayments.cardform.utils.CardType;

import java.util.List;

public class DropInViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final MutableLiveData<List<DropInPaymentMethodType>> supportedPaymentMethods = new MutableLiveData<>();
    private final MutableLiveData<List<PaymentMethodNonce>> vaultedPaymentMethods = new MutableLiveData<>();
    private final MutableLiveData<List<CardType>> supportedCardTypes = new MutableLiveData<>();

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

    public void setSupportedPaymentMethods(List<DropInPaymentMethodType> value) {
        this.supportedPaymentMethods.setValue(value);
    }

    public LiveData<List<DropInPaymentMethodType>> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    public void setSupportedCardTypes(List<CardType> value) {
        supportedCardTypes.postValue(value);
    }

    public LiveData<List<CardType>> getSupportedCardTypes() {
        return supportedCardTypes;
    }
}

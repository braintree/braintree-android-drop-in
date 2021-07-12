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
    private final MutableLiveData<Exception> cardTokenizationError = new MutableLiveData<>();

    LiveData<Boolean> isLoading() {
        return isLoading;
    }

    void setIsLoading(boolean value) {
        isLoading.setValue(value);
    }

    void setVaultedPaymentMethods(List<PaymentMethodNonce> value) {
        vaultedPaymentMethods.setValue(value);
    }

    LiveData<List<PaymentMethodNonce>> getVaultedPaymentMethods() {
        return vaultedPaymentMethods;
    }

    void setSupportedPaymentMethods(List<DropInPaymentMethodType> value) {
        this.supportedPaymentMethods.setValue(value);
    }

    LiveData<List<DropInPaymentMethodType>> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    void setSupportedCardTypes(List<CardType> value) {
        supportedCardTypes.postValue(value);
    }

    LiveData<List<CardType>> getSupportedCardTypes() {
        return supportedCardTypes;
    }

    LiveData<Exception> getCardTokenizationError() {
        return cardTokenizationError;
    }

    void setCardTokenizationError(Exception value) {
        cardTokenizationError.setValue(value);
    }

}

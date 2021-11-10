package com.braintreepayments.api;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.braintreepayments.cardform.utils.CardType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DropInViewModel extends ViewModel {

    private final MutableLiveData<BottomSheetState> bottomSheetState = new MutableLiveData<>(BottomSheetState.HIDDEN);
    private final MutableLiveData<DropInState> dropInState = new MutableLiveData<>(DropInState.IDLE);

    private final MutableLiveData<List<DropInPaymentMethod>> supportedPaymentMethods = new MutableLiveData<>();
    private final MutableLiveData<List<PaymentMethodNonce>> vaultedPaymentMethods = new MutableLiveData<>();
    private final MutableLiveData<List<CardType>> supportedCardTypes = new MutableLiveData<>();
    private final MutableLiveData<Exception> cardTokenizationError = new MutableLiveData<>();
    private final MutableLiveData<Exception> userCanceledError = new MutableLiveData<>();

    LiveData<BottomSheetState> getBottomSheetState() {
        return bottomSheetState;
    }

    void setBottomSheetState(BottomSheetState value) {
        bottomSheetState.setValue(value);
    }

    void setVaultedPaymentMethods(List<PaymentMethodNonce> value) {
        vaultedPaymentMethods.setValue(value);
    }

    LiveData<List<PaymentMethodNonce>> getVaultedPaymentMethods() {
        return vaultedPaymentMethods;
    }

    void setSupportedPaymentMethods(List<DropInPaymentMethod> value) {
        this.supportedPaymentMethods.setValue(value);
    }

    LiveData<List<DropInPaymentMethod>> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    void setSupportedCardTypes(List<CardType> value) {
        supportedCardTypes.setValue(value);
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

    LiveData<Exception> getUserCanceledError() {
        return userCanceledError;
    }

    void setUserCanceledError(Exception value) {
        userCanceledError.setValue(value);
    }

    void removeVaultedPaymentMethodNonce(PaymentMethodNonce paymentMethodNonceToDelete) {
        List<PaymentMethodNonce> currentPaymentMethods = vaultedPaymentMethods.getValue();
        if (currentPaymentMethods != null) {
            List<PaymentMethodNonce> updatedPaymentMethods = new ArrayList<>(currentPaymentMethods);

            // find nonce by string and remove it
            Iterator<PaymentMethodNonce> iterator = updatedPaymentMethods.iterator();
            while (iterator.hasNext()) {
                PaymentMethodNonce nonce = iterator.next();
                if (nonce.getString().equals(paymentMethodNonceToDelete.getString())) {
                    iterator.remove();
                    break;
                }
            }
            vaultedPaymentMethods.setValue(updatedPaymentMethods);
        }
    }

    LiveData<DropInState> getDropInState() {
        return dropInState;
    }

    void setDropInState(DropInState state) {
        dropInState.setValue(state);
    }
}

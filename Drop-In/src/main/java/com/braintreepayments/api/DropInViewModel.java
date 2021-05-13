package com.braintreepayments.api;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNoncesUpdatedListener;
import com.braintreepayments.api.models.ClientToken;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DropInViewModel extends ViewModel implements PaymentMethodNoncesUpdatedListener {

    private final DropInRequest dropInRequest;
    private final BraintreeFragment braintreeFragment;
    private final boolean isClientTokenPresent;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<PaymentMethodNonce>> vaultedPaymentMethodNonces = new MutableLiveData<List<PaymentMethodNonce>>(new ArrayList<PaymentMethodNonce>());
    private final MutableLiveData<List<PaymentMethodType>> availablePaymentMethods = new MutableLiveData<List<PaymentMethodType>>(new ArrayList<PaymentMethodType>());

    DropInViewModel(BraintreeFragment braintreeFragment, DropInRequest dropInRequest) {
        this.dropInRequest = dropInRequest;
        this.braintreeFragment = braintreeFragment;
        this.isClientTokenPresent = braintreeFragment.getAuthorization() instanceof ClientToken;

        // TODO: remove after migrating to android v4 and BraintreeClient
        this.braintreeFragment.addListener(this);
    }

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

    void fetchPaymentMethodNonces(final boolean refetch) {
        if (isClientTokenPresent) {
            if (braintreeFragment.hasFetchedPaymentMethodNonces() && !refetch) {
                onPaymentMethodNoncesUpdated(braintreeFragment.getCachedPaymentMethodNonces());
            } else {
                PaymentMethod.getPaymentMethodNonces(braintreeFragment, true);
            }
        }
    }

    @Override
    public void onPaymentMethodNoncesUpdated(List<PaymentMethodNonce> paymentMethodNonces) {
        final List<PaymentMethodNonce> noncesRef = paymentMethodNonces;
        if (paymentMethodNonces.size() > 0) {
            if (dropInRequest.isGooglePaymentEnabled()) {
                GooglePayment.isReadyToPay(braintreeFragment, new BraintreeResponseListener<Boolean>() {
                    @Override
                    public void onResponse(Boolean isReadyToPay) {
                        updatedVaultedPaymentMethods(noncesRef, isReadyToPay);
                    }
                });
            } else {
                updatedVaultedPaymentMethods(noncesRef, false);
            }
        }
    }

    private void updatedVaultedPaymentMethods(final List<PaymentMethodNonce> paymentMethodNonces, final boolean googlePayEnabled) {
        braintreeFragment.waitForConfiguration(new ConfigurationListener() {
            @Override
            public void onConfigurationFetched(Configuration configuration) {
                AvailablePaymentMethodNonceList availablePaymentMethodNonceList = new AvailablePaymentMethodNonceList(
                        braintreeFragment.getApplicationContext(), configuration, paymentMethodNonces, dropInRequest, googlePayEnabled);
                vaultedPaymentMethodNonces.postValue(availablePaymentMethodNonceList.getItems());
            }
        });
    }
}

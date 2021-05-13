package com.braintreepayments.api;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.exceptions.InvalidArgumentException;

/**
 * Ref: https://medium.com/koderlabs/viewmodel-with-viewmodelprovider-factory-the-creator-of-viewmodel-8fabfec1aa4f
 */
class DropInViewModelFactory implements ViewModelProvider.Factory {

    private final DropInRequest dropInRequest;
    private BraintreeFragment braintreeFragment;

    // TODO: Remove FragmentActivity parameter in favor of application context parameter
    // after migrating to BraintreeClient
    DropInViewModelFactory(FragmentActivity activity, DropInRequest dropInRequest) {
        try {
            braintreeFragment = BraintreeFragment.newInstance(activity, dropInRequest.getAuthorization());
        } catch (InvalidArgumentException e) {
            // TODO: remove after migrating to BraintreeClient
            e.printStackTrace();
        }
        this.dropInRequest = dropInRequest;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DropInViewModel(braintreeFragment, dropInRequest);
    }
}

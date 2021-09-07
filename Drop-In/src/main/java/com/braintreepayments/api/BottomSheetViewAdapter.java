package com.braintreepayments.api;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

class BottomSheetViewAdapter extends FragmentStateAdapter {

    private final DropInRequest dropInRequest;
    private final BottomSheetViewModel childFragmentList;

    BottomSheetViewAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, BottomSheetViewModel childFragmentList, DropInRequest dropInRequest) {
        super(fragmentManager, lifecycle);
        this.dropInRequest = dropInRequest;
        this.childFragmentList = childFragmentList;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        BottomSheetViewType childFragment = childFragmentList.getItem(position);

        Bundle args = new Bundle();
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest);

        switch (childFragment) {
            case VAULT_MANAGER:
                VaultManagerFragment vaultManagerFragment = new VaultManagerFragment();
                vaultManagerFragment.setArguments(args);
                return vaultManagerFragment;
            default:
            case SUPPORTED_PAYMENT_METHODS:
                SupportedPaymentMethodsFragment fragment = new SupportedPaymentMethodsFragment();
                fragment.setArguments(args);
                return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return childFragmentList.size();
    }

    @Override
    public long getItemId(int position) {
        return childFragmentList.getItemId(position);
    }

    @Override
    public boolean containsItem(long itemId) {
        return childFragmentList.containsItem(itemId);
    }
}

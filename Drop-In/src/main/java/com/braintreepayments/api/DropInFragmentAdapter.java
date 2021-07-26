package com.braintreepayments.api;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class DropInFragmentAdapter extends FragmentStateAdapter {

    private final DropInRequest dropInRequest;
    private final List<FragmentType> fragments;

    public DropInFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<FragmentType> fragments, DropInRequest dropInRequest) {
        super(fragmentManager, lifecycle);
        this.fragments = fragments;
        this.dropInRequest = dropInRequest;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FragmentType fragmentType = fragments.get(position);

        Bundle args = new Bundle();
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest);

        switch (fragmentType) {
            case PROGRESS_INDICATOR:
                return null;
//                return new ProgressFragment();
            case VAULT_MANAGER:
                VaultManagerFragment vaultManagerFragment = new VaultManagerFragment();
                vaultManagerFragment.setArguments(args);
                return vaultManagerFragment;
            default:
            case SELECT_PAYMENT_METHOD:
                SupportedPaymentMethodsFragment fragment = new SupportedPaymentMethodsFragment();
                fragment.setArguments(args);
                return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    @Override
    public long getItemId(int position) {
        FragmentType fragmentType = fragments.get(position);
        switch (fragmentType) {
            case VAULT_MANAGER:
                return 1;
            case PROGRESS_INDICATOR:
                return 2;
            default:
            case SELECT_PAYMENT_METHOD:
                return 0;
        }
    }

    @Override
    public boolean containsItem(long itemId) {
        for (FragmentType fragment: fragments) {
            switch ((int) itemId) {
                case 1:
                    if (fragment == FragmentType.VAULT_MANAGER) {
                        return true;
                    }
                case 2:
                    if (fragment == FragmentType.PROGRESS_INDICATOR) {
                        return true;
                    }
                case 0:
                    if (fragment == FragmentType.SELECT_PAYMENT_METHOD) {
                        return true;
                    }
            }
        }
        return false;
    }
}

package com.braintreepayments.api;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class DropInFragmentAdapter extends FragmentStateAdapter {

    private final List<FragmentType> fragments;

    public DropInFragmentAdapter(@NonNull FragmentActivity fragmentActivity, List<FragmentType> fragments) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FragmentType fragmentType = fragments.get(position);
        switch (fragmentType) {
            case PROGRESS_INDICATOR:
                return null;
//                return new ProgressFragment();
            case VAULT_MANAGER:
                return new VaultManagerFragment();
            default:
            case SELECT_PAYMENT_METHOD:
                return new SelectPaymentMethodFragment();
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

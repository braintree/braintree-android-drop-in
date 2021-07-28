package com.braintreepayments.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SelectPaymentMethodChildFragmentList {

    private final List<SelectPaymentMethodChildFragment> fragments;

    SelectPaymentMethodChildFragmentList(SelectPaymentMethodChildFragment ...args) {
        fragments = new ArrayList<>();
        fragments.addAll(Arrays.asList(args));
    }

    SelectPaymentMethodChildFragment getItem(int position) {
        return fragments.get(position);
    }

    void add(SelectPaymentMethodChildFragment childFragment) {
        fragments.add(childFragment);
    }

    void remove(int position) {
        fragments.remove(position);
    }

    long getItemId(int position) {
        return getItem(position).getId();
    }

    boolean containsItem(long itemId) {
        for (SelectPaymentMethodChildFragment fragment : fragments) {
            if (fragment.hasId(itemId)) {
                return true;
            }
        }
        return false;
    }

    int size() {
        return fragments.size();
    }
}

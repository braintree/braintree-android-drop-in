package com.braintreepayments.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BottomSheetViewModel {

    private final List<BottomSheetViewType> fragments;

    BottomSheetViewModel(BottomSheetViewType...args) {
        fragments = new ArrayList<>();
        fragments.addAll(Arrays.asList(args));
    }

    BottomSheetViewType getItem(int position) {
        return fragments.get(position);
    }

    void add(BottomSheetViewType childFragment) {
        fragments.add(childFragment);
    }

    void remove(int position) {
        fragments.remove(position);
    }

    long getItemId(int position) {
        return getItem(position).getId();
    }

    boolean containsItem(long itemId) {
        for (BottomSheetViewType fragment : fragments) {
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

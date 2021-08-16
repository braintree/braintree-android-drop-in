package com.braintreepayments.api;

import androidx.fragment.app.Fragment;

abstract class DropInFragment extends Fragment {

    protected void sendDropInEvent(DropInEvent event) {
        if (isAdded()) {
            getParentFragmentManager().setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle());
        }
    }

    protected void sendAnalyticsEvent(String eventName) {
        sendDropInEvent(DropInEvent.createSendAnalyticsEvent(eventName));
    }
}

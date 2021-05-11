package com.braintreepayments.api;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

public class DropInClient {

    public static final String EXTRA_AUTHORIZATION = "com.braintreepayments.api.EXTRA_AUTHORIZATION";
    public static final String EXTRA_DROP_IN_REQUEST = "com.braintreepayments.api.EXTRA_DROP_IN_REQUEST";

    private final String authorization;

    public DropInClient(String authorization) {
        this.authorization = authorization;
    }

    public void launchDropIn(FragmentActivity activity, DropInRequest request, int requestCode) {
        Intent intent = new Intent(activity, DropInActivity.class)
                .putExtra(EXTRA_AUTHORIZATION, authorization)
                .putExtra(EXTRA_DROP_IN_REQUEST, request);
        activity.startActivityForResult(intent, requestCode);
    }
}

package com.braintreepayments.api;

import java.util.List;

interface GetSupportedCardTypesCallback {

    void onResult(List<String> supportedCardTypes, Exception error);
}

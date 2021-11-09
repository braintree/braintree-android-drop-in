package com.braintreepayments.api;

import java.util.List;

interface GetSupportedCardTypesCallback {

    void onResult(List<DropInPaymentMethodType> supportedCardTypes, Exception error);
}

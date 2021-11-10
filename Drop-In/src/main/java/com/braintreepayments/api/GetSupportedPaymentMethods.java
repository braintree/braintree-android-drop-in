package com.braintreepayments.api;

import java.util.List;

interface GetSupportedPaymentMethods {

    void onResult(List<DropInPaymentMethodType> supportedCardTypes, Exception error);
}

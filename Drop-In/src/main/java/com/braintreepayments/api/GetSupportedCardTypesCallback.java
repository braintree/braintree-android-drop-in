package com.braintreepayments.api;

import java.util.List;

public interface GetSupportedCardTypesCallback {

    void onResult(List<String> supportedCardTypes, Exception error);
}

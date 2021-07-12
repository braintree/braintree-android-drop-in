package com.braintreepayments.api;

import com.braintreepayments.cardform.utils.CardType;

import java.util.List;

public interface GetSupportedCardTypesCallback {

    void onResult(List<String> supportedCardTypes, Exception error);
}

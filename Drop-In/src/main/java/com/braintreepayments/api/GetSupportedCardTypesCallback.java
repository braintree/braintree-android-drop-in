package com.braintreepayments.api;

import com.braintreepayments.cardform.utils.CardType;

import java.util.List;

interface GetSupportedCardTypesCallback {

    void onResult(List<CardType> supportedCardTypes, Exception error);
}

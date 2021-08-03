package com.braintreepayments.api;

enum SelectPaymentMethodChildFragment {
    SUPPORTED_PAYMENT_METHODS(0),
    VAULT_MANAGER(1);

    private final long id;

    SelectPaymentMethodChildFragment(long id) {
        this.id = id;
    }

    long getId() {
        return id;
    }

    boolean hasId(long id) {
        return this.id == id;
    }
}

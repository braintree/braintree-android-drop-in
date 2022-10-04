package com.braintreepayments.api;

class BraintreeErrorInspector {

    private static final int ERROR_CODE_DUPLICATE_PAYMENT = 81724;

    boolean isDuplicatePaymentError(ErrorWithResponse error) {
        BraintreeError creditCardError = error.errorFor("creditCard");
        if (creditCardError != null) {
            BraintreeError numberError = creditCardError.errorFor("number");
            if (numberError != null) {
                return (numberError.getCode() == ERROR_CODE_DUPLICATE_PAYMENT);
            }
        }
        return false;
    }
}

package com.braintreepayments.api;

import java.util.Calendar;

public class ExpirationDate {

    public static final String VALID_EXPIRATION = "12" + validExpirationYear();

    public static String validExpirationYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1).substring(2);
    }
}

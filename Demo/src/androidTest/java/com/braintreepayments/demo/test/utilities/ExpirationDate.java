package com.braintreepayments.demo.test.utilities;

import java.util.Calendar;

public class ExpirationDate {

    public static final String VALID_EXPIRATION_YEAR =
        String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1);
}

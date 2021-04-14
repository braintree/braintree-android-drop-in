package com.braintreepayments.api;

import java.util.Base64;

public class UnitTestFixturesHelper {

    public static String base64EncodedClientTokenFromFixture(String fixture) {
        return Base64.getEncoder().encodeToString(fixture.getBytes());
    }
}

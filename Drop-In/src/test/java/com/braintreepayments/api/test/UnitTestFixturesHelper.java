package com.braintreepayments.api.test;

import java.io.IOException;
import java.io.InputStream;

import static com.braintreepayments.api.test.StringUtils.getStringFromStream;

public class UnitTestFixturesHelper {

    private static final String FIXTURES_PATH = "fixtures/";

    public static String stringFromFixture(String filename) {

        try {
            InputStream inputStream = null;
            try {
                inputStream = UnitTestFixturesHelper.class.getClassLoader()
                        .getResourceAsStream(FIXTURES_PATH + filename);
                return getStringFromStream(inputStream);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

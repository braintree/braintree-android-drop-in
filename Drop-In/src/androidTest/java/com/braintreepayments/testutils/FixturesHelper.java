package com.braintreepayments.testutils;

import java.io.IOException;
import java.io.InputStream;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.braintreepayments.api.test.StringUtils.getStringFromStream;

public class FixturesHelper {

    private static final String FIXTURES_PATH = "fixtures/";

    public static String stringFromFixture(String filename) {
        try {
            InputStream inputStream = null;
            try {
                inputStream = getTargetContext().getResources().getAssets().open(FIXTURES_PATH + filename);
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

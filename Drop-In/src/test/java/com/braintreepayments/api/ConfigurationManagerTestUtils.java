package com.braintreepayments.api;

public class ConfigurationManagerTestUtils {

    public static void setFetchingConfiguration(boolean fetchingConfiguration) {
        ConfigurationManager.sFetchingConfiguration = fetchingConfiguration;
    }
}

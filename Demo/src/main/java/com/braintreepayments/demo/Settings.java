package com.braintreepayments.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.braintreepayments.api.ThreeDSecureRequest;
import com.braintreepayments.cardform.view.CardForm;

public class Settings {

    protected static final String ENVIRONMENT = "environment";

    private static final String VERSION = "version";

    private static final String MERCHANT_SERVER_URL = "https://sdk-sample-merchant-server.herokuapp.com";

    private static final String SANDBOX_BASE_SERVER_URL = MERCHANT_SERVER_URL + "/braintree/sandbox";
    private static final String PRODUCTION_BASE_SERVER_URL = MERCHANT_SERVER_URL + "/braintree/production";
    private static final String MOCKED_PAY_PAL_SANDBOX_SERVER_URL = MERCHANT_SERVER_URL + "/braintree/mock_pay_pal";

    private static final String SANDBOX_TOKENIZATION_KEY = "sandbox_tmxhyf7d_dcpspy2brwdjr3qn";
    private static final String PRODUCTION_TOKENIZATION_KEY = "production_t2wns2y2_dfy45jdj3dxkmz5m";
    private static final String MOCKED_PAY_PAL_SANDBOX_TOKENIZATION_KEY = "sandbox_q7v35n9n_555d2htrfsnnmfb3";

    private static SharedPreferences sSharedPreferences;

    public static SharedPreferences getPreferences(Context context) {
        if (sSharedPreferences == null) {
            sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }

        return sSharedPreferences;
    }

    public static int getVersion(Context context) {
        return getPreferences(context).getInt(VERSION, 0);
    }

    public static void setVersion(Context context) {
        getPreferences(context).edit().putInt(VERSION, BuildConfig.VERSION_CODE).apply();
    }

    public static int getEnvironment(Context context) {
        return getPreferences(context).getInt(ENVIRONMENT, 0);
    }

    public static void setEnvironment(Context context, int environment) {
        getPreferences(context)
                .edit()
                .putInt(ENVIRONMENT, environment)
                .apply();
    }

    public static String getSandboxUrl() {
        return SANDBOX_BASE_SERVER_URL;
    }

    public static String getEnvironmentUrl(Context context) {
        int environment = getEnvironment(context);
        switch (environment) {
            case 0:
                return SANDBOX_BASE_SERVER_URL;
            case 1:
                return PRODUCTION_BASE_SERVER_URL;
            case 2:
                return MOCKED_PAY_PAL_SANDBOX_SERVER_URL;
            default:
                return "";
        }
    }

    public static String getCustomerId(Context context) {
        return getPreferences(context).getString("customer", null);
    }

    public static String getMerchantAccountId(Context context) {
        return getPreferences(context).getString("merchant_account", null);
    }

    public static String getThreeDSecureMerchantAccountId(Context context) {
        if (isThreeDSecureEnabled(context) && getEnvironment(context) == 1) {
            return "test_AIB";
        } else {
            return null;
        }
    }

    public static String getUnionPayMerchantAccountId(Context context) {
        if (getEnvironment(context) == 0) {
            return "fake_switch_usd";
        } else {
            return null;
        }
    }

    public static boolean useTokenizationKey(Context context) {
        return getPreferences(context).getBoolean("tokenization_key", false);
    }

    public static String getEnvironmentTokenizationKey(Context context) {
        int environment = getEnvironment(context);
        switch (environment) {
            case 0:
                return SANDBOX_TOKENIZATION_KEY;
            case 1:
                return PRODUCTION_TOKENIZATION_KEY;
            case 2:
                return MOCKED_PAY_PAL_SANDBOX_TOKENIZATION_KEY;
            default:
                return "";
        }
    }

    public static boolean isThreeDSecureEnabled(Context context) {
        return getPreferences(context).getBoolean("enable_three_d_secure", false);
    }

    public static boolean isThreeDSecureRequired(Context context) {
        return getPreferences(context).getBoolean("require_three_d_secure", true);
    }

    public static String getThreeDSecureVersion(Context context) {
        return getPreferences(context).getString("three_d_secure_version", ThreeDSecureRequest.VERSION_2);
    }

    public static boolean isVaultManagerEnabled(Context context) {
        return getPreferences(context).getBoolean("enable_vault_manager", false);
    }

    public static int getCardholderNameStatus(Context context) {
        String status = getPreferences(context).getString("cardholder_name_status", "Disabled");

        switch (status) {
            case "Optional":
                return CardForm.FIELD_OPTIONAL;
            case "Required":
                return CardForm.FIELD_REQUIRED;
            case "Disabled":
            default:
                return CardForm.FIELD_DISABLED;
        }
    }

    static boolean isSaveCardCheckBoxVisible(Context context) {
        return getPreferences(context).getBoolean("save_card_checkbox_visible", false);
    }

    static boolean defaultVaultSetting(Context context) {
        return getPreferences(context).getBoolean("save_card_checkbox_default_value", true);
    }
}

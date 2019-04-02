package com.braintreepayments.demo.test.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Spinner;

import com.braintreepayments.cardform.view.CardForm;
import com.lukekorth.deviceautomator.DeviceAutomator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.CallSuper;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.AutomatorAssertion.text;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withClass;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withResourceId;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextContaining;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;

public class TestHelper {

    public static final String PAYPAL_WALLET_PACKAGE_NAME = "com.paypal.android.p2pmobile";

    @CallSuper
    public void setup() {
        clearPreference("BraintreeApi");
        clearPreference("PayPalOTC");

        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .clear()
                .putBoolean("paypal_use_hardcoded_configuration", true)
                .commit();

        onDevice().onHomeScreen().launchApp("com.braintreepayments.demo");
        onDevice().acceptRuntimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ensureEnvironmentIs("Sandbox");
    }

    public DeviceAutomator getNonceDetails() {
        return onDevice(withResourceId("com.braintreepayments.demo:id/nonce_details"));
    }

    public static void installPayPalWallet() {
        if (!isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME)) {
            Log.d("request_command", "install paypal wallet");

            final CountDownLatch lock = new CountDownLatch(1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        if(isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME)) {
                            lock.countDown();
                            break;
                        }
                    }
                }
            });
            try {
                lock.await(90, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}

            assertTrue(isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME));
        }
    }

    public static void uninstallPayPalWallet() {
        if (isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME)) {
            Log.d("request_command", "uninstall paypal wallet");

            final CountDownLatch lock = new CountDownLatch(1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        if(!isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME)) {
                            lock.countDown();
                            break;
                        }
                    }
                }
            });
            try {
                lock.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}

            assertFalse(isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME));
        }
    }

    public void setCustomerId(String customerId) {
        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putString("customer", customerId)
                .commit();

        onDevice(withText("Reset")).perform(click());
        SystemClock.sleep(2000);
    }

    public void setMerchantAccountId(String merchantAccountId) {
        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putString("merchant_account", merchantAccountId)
                .commit();

        onDevice(withText("Reset")).perform(click());
        SystemClock.sleep(2000);
    }

    public void useTokenizationKey() {
        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putBoolean("tokenization_key", true)
                .commit();

        onDevice(withText("Reset")).perform(click());
        SystemClock.sleep(2000);
    }

    public void enableThreeDSecure() {
        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putBoolean("enable_three_d_secure", true)
                .commit();
    }

    public void enableVaultManager() {
        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putBoolean("enable_vault_manager", true)
                .commit();
    }

    public void setCardholderNameStatus(int cardholderNameStatus) {
        String status;

        switch(cardholderNameStatus) {
            case CardForm.FIELD_REQUIRED:
                status = "Required";
                break;
            case CardForm.FIELD_OPTIONAL:
                status = "Optional";
                break;
            default:
            case CardForm.FIELD_DISABLED:
                status = "Disabled";
                break;
        }

        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putString("cardholder_name_status", status)
                .commit();

        onDevice(withText("Reset")).perform(click());
        SystemClock.sleep(2000);
    }

    public void setSaveCardCheckBoxVisibilityAndDefault(boolean isVisible, boolean defaultValue) {
        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putBoolean("save_card_checkbox_visible", isVisible)
                .commit();

        PreferenceManager.getDefaultSharedPreferences(getTargetContext())
                .edit()
                .putBoolean("default_vault_setting", defaultValue)
                .commit();
    }

    private void clearPreference(String preference) {
        getTargetContext().getSharedPreferences(preference, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    private static void ensureEnvironmentIs(String environment) {
        try {
            onDevice(withText(environment)).check(text(equalTo(environment)));
        } catch (RuntimeException e) {
            onDevice(withClass(Spinner.class)).perform(click());
            onDevice(withText(environment)).perform(click());
            onDevice(withText(environment)).check(text(equalTo(environment)));
        }
    }

    private static boolean isAppInstalled(String packageName) {
        PackageManager pm = getTargetContext().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    protected void performCardDetailsEntry() {
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withText("CVV")).perform(setText("123"));
        onDevice(withText("Postal Code")).perform(setText("12345"));
    }

    protected void tokenizeCard(String cardNumber) {
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(cardNumber));
        performCardDetailsEntry();
        onDevice(withTextContaining("Add Card")).perform(click());
    }
}

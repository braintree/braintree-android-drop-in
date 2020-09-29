package com.braintreepayments.demo.test.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Spinner;

import androidx.annotation.CallSuper;
import androidx.test.core.app.ApplicationProvider;

import com.braintreepayments.DeviceAutomator;
import com.braintreepayments.cardform.view.CardForm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.braintreepayments.AutomatorAction.click;
import static com.braintreepayments.AutomatorAction.setText;
import static com.braintreepayments.AutomatorAssertion.text;
import static com.braintreepayments.DeviceAutomator.onDevice;
import static com.braintreepayments.UiObjectMatcher.withClass;
import static com.braintreepayments.UiObjectMatcher.withResourceId;
import static com.braintreepayments.UiObjectMatcher.withText;
import static com.braintreepayments.UiObjectMatcher.withTextContaining;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assume.assumeFalse;

public class TestHelper {

    public static final String PAYPAL_WALLET_PACKAGE_NAME = "com.paypal.android.p2pmobile";

    @CallSuper
    public void setup() {
        clearPreference("BraintreeApi");
        clearPreference("PayPalOTC");

        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
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

            assumeFalse("The PayPal app needs to be uninstalled before running this test",
                    isAppInstalled(PAYPAL_WALLET_PACKAGE_NAME));
        }
    }

    public void setCustomerId(String customerId) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putString("customer", customerId)
                .commit();

        SystemClock.sleep(2000);
        onDevice(withText("Reset")).perform(click());
    }

    /**
     * Sets the customer ID to a value that should be randomized.
     * There should not be any saved payment methods for this customer.
     */
    public void setUniqueCustomerId() {
        setCustomerId(""+System.currentTimeMillis());
    }

    public void setMerchantAccountId(String merchantAccountId) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putString("merchant_account", merchantAccountId)
                .commit();

        SystemClock.sleep(2000);
        onDevice(withText("Reset")).perform(click());
    }

    public void useTokenizationKey() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putBoolean("tokenization_key", true)
                .commit();

        // additional sleep here makes tests more consistent for some reason
        SystemClock.sleep(2000);
        onDevice(withText("Reset")).perform(click());
        SystemClock.sleep(2000);
    }

    public void enableThreeDSecure() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putBoolean("enable_three_d_secure", true)
                .commit();
    }

    public void enableVaultManager() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
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

        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putString("cardholder_name_status", status)
                .commit();

        SystemClock.sleep(2000);
        onDevice(withText("Reset")).perform(click());
    }

    public void setSaveCardCheckBox(boolean visible, boolean defaultValue) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putBoolean("save_card_checkbox_visible", visible)
                .putBoolean("save_card_checkbox_default_value", defaultValue)
                .commit();

        SystemClock.sleep(2000);
        onDevice(withText("Reset")).perform(click());
    }

    private void clearPreference(String preference) {
        ApplicationProvider.getApplicationContext().getSharedPreferences(preference, Context.MODE_PRIVATE)
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
        PackageManager pm = ApplicationProvider.getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    protected void performCardDetailsEntry() {
        onDevice(withText("Expiration Date")).perform(setText("12" + ExpirationDate.VALID_EXPIRATION_YEAR));
        onDevice().pressBack();
        onDevice(withText("CVV")).perform(setText("123"));
        onDevice(withText("Postal Code")).perform(setText("12345"));
    }

    protected void tokenizeCard(String cardNumber) {
        onDevice(withText("Credit or Debit Card")).waitForExists().perform(click());
        onDevice(withText("Card Number")).waitForExists().perform(setText(cardNumber));
        performCardDetailsEntry();
        onDevice(withTextContaining("Add Card")).perform(click());
    }
}

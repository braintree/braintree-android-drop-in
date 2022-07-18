package com.braintreepayments.demo.test.utilities;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Spinner;

import androidx.annotation.CallSuper;
import androidx.test.core.app.ApplicationProvider;

import com.braintreepayments.DeviceAutomator;
import com.braintreepayments.cardform.view.CardForm;

import static com.braintreepayments.AutomatorAction.click;
import static com.braintreepayments.AutomatorAction.setText;
import static com.braintreepayments.AutomatorAssertion.text;
import static com.braintreepayments.DeviceAutomator.onDevice;
import static com.braintreepayments.UiObjectMatcher.withClass;
import static com.braintreepayments.UiObjectMatcher.withResourceId;
import static com.braintreepayments.UiObjectMatcher.withText;
import static com.braintreepayments.UiObjectMatcher.withTextContaining;
import static org.hamcrest.CoreMatchers.equalTo;

public class TestHelper {

    @CallSuper
    public void setup() {
        clearPreference("BraintreeApi");
        clearPreference("PayPalOTC");

        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .clear()
                .putBoolean("paypal_use_hardcoded_configuration", true)
                .commit();
    }

    public void launchApp() {
        launchApp("Sandbox");
    }

    public void launchApp(String targetEnvironment) {
        onDevice().onHomeScreen().launchApp("com.braintreepayments.demo");
        ensureEnvironmentIs(targetEnvironment);
    }

    public DeviceAutomator getNonceDetails() {
        return onDevice(withResourceId("com.braintreepayments.demo:id/nonce_details"));
    }

    public void setCustomerId(String customerId) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putString("customer", customerId)
                .commit();
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
    }

    public void useTokenizationKey() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putBoolean("tokenization_key", true)
                .commit();
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
    }

    public void setSaveCardCheckBox(boolean visible, boolean defaultValue) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putBoolean("save_card_checkbox_visible", visible)
                .putBoolean("save_card_checkbox_default_value", defaultValue)
                .commit();
    }

    private void clearPreference(String preference) {
        ApplicationProvider.getApplicationContext().getSharedPreferences(preference, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    private static void ensureEnvironmentIs(String environment) {
        onDevice(withClass(Spinner.class)).perform(click());
        onDevice(withText(environment)).perform(click());
        onDevice(withText(environment)).check(text(equalTo(environment)));
    }

    protected void performCardDetailsEntry() {
        onDevice(withText("Expiration Date")).perform(setText("12" + ExpirationDate.VALID_EXPIRATION_YEAR));
        onDevice(withText("CVV")).perform(setText("123"));
        onDevice(withText("Postal Code")).perform(setText("12345"));
    }

    protected void tokenizeCard(String cardNumber) {
        onDevice(withText("Credit or Debit Card")).waitForExists().perform(click());
        onDevice(withText("Card Number")).waitForExists().perform(setText(cardNumber));
        onDevice(withText("Next")).perform(click());
        performCardDetailsEntry();
        onDevice(withTextContaining("Add Card")).perform(click());
    }
}

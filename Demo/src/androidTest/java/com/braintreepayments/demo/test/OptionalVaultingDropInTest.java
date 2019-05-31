package com.braintreepayments.demo.test;

import com.braintreepayments.demo.test.utilities.TestHelper;

import org.junit.Before;
import org.junit.Test;

import static com.braintreepayments.demo.test.utilities.CardNumber.VISA;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextContaining;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class OptionalVaultingDropInTest extends TestHelper {

    @Before
    public void setup() {
        super.setup();
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_whenVisibleAndChecked_vaults() {
        setSaveCardCheckBox(true, true);
        setUniqueCustomerId();

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        tokenizeCard(VISA);

        onDevice(withText("Visa")).waitForExists().perform(click());
        boolean hasSavedPaymentMethods = onDevice(withText("Recent")).waitForExists().exists();

        assertTrue(hasSavedPaymentMethods);
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_whenVisibleAndCustomerChecks_vaults() {
        setSaveCardCheckBox(true, false);
        setUniqueCustomerId();

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));
        performCardDetailsEntry();
        onDevice(withText("Save card")).perform(click());
        onDevice(withTextContaining("Add Card")).perform(click());

        onDevice(withText("Visa")).waitForExists().perform(click());
        boolean hasSavedPaymentMethods = onDevice(withText("Recent")).waitForExists().exists();

        assertTrue(hasSavedPaymentMethods);
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_whenVisibleAndCustomerUnchecks_doesNotVault() {
        setSaveCardCheckBox(true, true);
        setUniqueCustomerId();

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));
        performCardDetailsEntry();
        onDevice(withText("Save card")).perform(click());
        onDevice(withTextContaining("Add Card")).perform(click());

        onDevice(withText("Visa")).waitForExists().perform(click());
        boolean hasSavedPaymentMethods = onDevice(withText("Recent")).waitForExists().exists();

        assertFalse(hasSavedPaymentMethods);
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_whenGoneAndChecked_vaults() {
        setSaveCardCheckBox(false, true);
        setUniqueCustomerId();

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        tokenizeCard(VISA);

        onDevice(withText("Visa")).waitForExists().perform(click());
        boolean hasSavedPaymentMethods = onDevice(withText("Recent")).waitForExists().exists();

        assertTrue(hasSavedPaymentMethods);
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_whenGoneAndUnchecked_doesNotVault() {
        setSaveCardCheckBox(false, false);
        setUniqueCustomerId();

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        tokenizeCard(VISA);

        onDevice(withText("Visa")).waitForExists().perform(click());
        boolean hasSavedPaymentMethods = onDevice(withText("Recent")).waitForExists().exists();

        assertFalse(hasSavedPaymentMethods);
    }
}

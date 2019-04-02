package com.braintreepayments.demo.test;

import com.braintreepayments.demo.test.utilities.TestHelper;

import org.junit.Before;
import org.junit.Test;

import static com.braintreepayments.demo.test.utilities.CardNumber.VISA;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static junit.framework.Assert.fail;

public class OptionalVaultingDropInTest extends TestHelper {

    @Before
    public void setup() {
        super.setup();
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_isVisible() {
        setSaveCardCheckBoxVisibilityAndDefault(true, true);

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));
        performCardDetailsEntry();

        onDevice(withText("Card Details")).waitForExists();

        if (onDevice(withText("Save card")).exists()) {
            // TODO: Assert a test pass
        } else {
            fail("A save card CheckBox was supposed to appear when it's visibility was set to true");
        }
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_isHidden() {
        setSaveCardCheckBoxVisibilityAndDefault(false, true);

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));
        performCardDetailsEntry();

        onDevice(withText("Card Details")).waitForExists();

        if (onDevice(withText("Save card")).exists()) {
            fail("A save card CheckBox was not supposed to appear when it's visibility was set to false");
        } else {
            // TODO: Assert a test pass
        }
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_vaults_whenChecked() {

        // TODO:

        // Get access to BTCardForm

        // Access cardForm.getSaveCardCheckBoxValue() method

        // Confirm box is checked

        // Test the shouldVault() method in AddCardActivity.java
    }

    @Test(timeout = 60000)
    public void saveCardCheckBox_doesNotVault_whenNotChecked() {
        //
    }

}

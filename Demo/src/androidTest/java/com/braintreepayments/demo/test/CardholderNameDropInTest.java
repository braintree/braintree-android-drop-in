package com.braintreepayments.demo.test;

import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.demo.test.utilities.TestHelper;

import org.junit.Before;
import org.junit.Test;

import static com.braintreepayments.demo.test.utilities.CardNumber.VISA;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.AutomatorAssertion.text;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextContaining;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextStartingWith;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;

public class CardholderNameDropInTest extends TestHelper {

    @Before
    public void setup() {
        super.setup();
    }

    @Test(timeout = 60000)
    public void cardholderName_whenDisabled_isHidden() {
        setCardholderNameStatus(CardForm.FIELD_DISABLED);
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));

        onDevice(withText("Card Details")).waitForExists();
        try {
            onDevice().check(text(containsString("Cardholder Name")));
            fail("Cardholder Name was not supposed to exist when CardForm.FIELD_DISABLED is set" +
                    "for Cardholder Name status");
        } catch (RuntimeException ignored) {}
    }

    @Test(timeout = 60000)
    public void cardholderName_whenRequired_mustBeFilled() {
        setCardholderNameStatus(CardForm.FIELD_REQUIRED);
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));

        onDevice(withText("Cardholder Name")).waitForExists();
        performCardDetailsEntry();
        onDevice(withText("Cardholder name is required")).waitForExists();
        onDevice(withText("Cardholder Name")).perform(setText("Brian Tree"));
        onDevice(withText("Add Card")).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void cardholderName_whenOptional_isShownButCanRemainEmpty() {
        setCardholderNameStatus(CardForm.FIELD_OPTIONAL);
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(VISA));

        onDevice(withText("Cardholder Name")).waitForExists();
        performCardDetailsEntry();
        onDevice(withTextContaining("Add Card")).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }
}

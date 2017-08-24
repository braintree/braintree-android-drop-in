package com.braintreepayments.demo.test;

import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Button;

import com.braintreepayments.demo.Settings;
import com.braintreepayments.demo.test.utilities.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.braintreepayments.demo.test.utilities.CardNumber.THREE_D_SECURE_VERIFICATON;
import static com.braintreepayments.demo.test.utilities.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.demo.test.utilities.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static com.braintreepayments.demo.test.utilities.CardNumber.VISA;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.AutomatorAssertion.text;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withContentDescription;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextContaining;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextStartingWith;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

@RunWith(AndroidJUnit4.class)
public class DropInTest extends TestHelper {

    @Before
    public void setup() {
        super.setup();
    }

    @Test(timeout = 60000)
    public void tokenizesACard() {
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        tokenizeCard(VISA);

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void tokenizesACardWithATokenizationKey() {
        useTokenizationKey();
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        tokenizeCard(VISA);

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void performsThreeDSecureVerification() {
        enableThreeDSecure();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        tokenizeCard(THREE_D_SECURE_VERIFICATON);

        onDevice(withText("Authentication")).waitForExists();
        onDevice().pressTab();
        onDevice().typeText("1234");
        onDevice().pressTab().pressTab().pressEnter();

        getNonceDetails().check(text(containsString("Card Last Two: 02")));
        getNonceDetails().check(text(containsString("3DS isLiabilityShifted: true")));
        getNonceDetails().check(text(containsString("3DS isLiabilityShiftPossible: true")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPay() {
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(UNIONPAY_CREDIT));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVN")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withContentDescription("Country Code")).perform(setText("01"));
        onDevice(withContentDescription("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());
        onDevice(withContentDescription("SMS Code")).perform(setText("12345"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 32")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPayWhenEnrollmentIsNotRequired() {
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(UNIONPAY_SMS_NOT_REQUIRED));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVN")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withContentDescription("Country Code")).perform(setText("01"));
        onDevice(withContentDescription("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 85")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPayWhenFirstSMSCodeIsInvalid() {
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(UNIONPAY_CREDIT));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVN")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withContentDescription("Country Code")).perform(setText("01"));
        onDevice(withContentDescription("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());
        onDevice(withContentDescription("SMS Code")).perform(setText("999999"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());
        onDevice(withText("SMS code is invalid")).check(text(is("SMS code is invalid")));
        onDevice(withContentDescription("SMS Code")).perform(setText("12345"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 32")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @SdkSuppress(minSdkVersion = 21)
    @Test(timeout = 60000)
    public void tokenizesPayPal() {
        uninstallPayPalWallet();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("PayPal")).perform(click());
        onDevice(withContentDescription("Proceed with Sandbox Purchase")).perform(click());

        getNonceDetails().check(text(containsString("Email: bt_buyer_us@paypal.com")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @SdkSuppress(minSdkVersion = 21)
    @Test(timeout = 60000)
    public void tokenizesPayPalWithATokenizationKey() {
        uninstallPayPalWallet();
        useTokenizationKey();
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        onDevice(withText("PayPal")).perform(click());
        onDevice(withContentDescription("Proceed with Sandbox Purchase")).perform(click());

        getNonceDetails().check(text(containsString("Email: bt_buyer_us@paypal.com")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @RequiresDevice
    @Test(timeout = 60000)
    public void tokenizesAndroidPay() {
        onDevice(withText("Add Payment Method")).waitForEnabled().perform(click());

        onDevice(withText("Android Pay")).perform(click());
        onDevice(withText("CONTINUE")).perform(click());

        getNonceDetails().check(text(containsString("Underlying Card Last Two")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void exitsAfterCancelingAddingAPaymentMethod() {
        uninstallPayPalWallet();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("PayPal")).perform(click());
        onDevice(withContentDescription("Proceed with Sandbox Purchase")).waitForExists();
        onDevice().pressBack();
        onDevice(withContentDescription("Pay with PayPal")).waitForExists();

        onDevice().pressBack();

        onDevice(withText("Add Payment Method")).check(text(equalToIgnoringCase("Add Payment Method")));
    }

    private void tokenizeCard(String cardNumber) {
        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withContentDescription("Card Number")).perform(setText(cardNumber));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withContentDescription("CVV")).perform(setText("123"));
        onDevice(withContentDescription("Postal Code")).perform(setText("12345"));
        onDevice(withTextContaining("Add Card")).perform(click());
    }
}

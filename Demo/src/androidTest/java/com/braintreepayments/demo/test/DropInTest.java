package com.braintreepayments.demo.test;

import android.os.Build;
import android.widget.Button;

import com.braintreepayments.demo.Settings;
import com.braintreepayments.demo.test.utilities.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.RequiresDevice;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.braintreepayments.demo.test.utilities.CardNumber.THREE_D_SECURE_VERIFICATON;
import static com.braintreepayments.demo.test.utilities.CardNumber.UNIONPAY_CREDIT;
import static com.braintreepayments.demo.test.utilities.CardNumber.UNIONPAY_SMS_NOT_REQUIRED;
import static com.braintreepayments.demo.test.utilities.CardNumber.VISA;
import static com.braintreepayments.demo.test.utilities.UiTestActions.clickWebViewText;
import static com.lukekorth.deviceautomator.AutomatorAction.clearTextField;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.AutomatorAction.setText;
import static com.lukekorth.deviceautomator.AutomatorAssertion.text;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withContentDescription;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withResourceId;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextContaining;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withTextStartingWith;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class DropInTest extends TestHelper {

    @Before
    public void setup() {
        super.setup();
    }

    @Test(timeout = 60000)
    public void tokenizesACard() {
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        tokenizeCard(VISA);

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void tokenizesACardWithATokenizationKey() {
        useTokenizationKey();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        tokenizeCard(VISA);

        getNonceDetails().check(text(containsString("Card Last Two: 11")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void tokenizesACard_whenClientTokenWithCustomerId_vaults() {
        setUniqueCustomerId();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        tokenizeCard(VISA);

        onDevice(withText("Visa")).waitForExists().perform(click());
        assertTrue(onDevice(withText("Recent")).waitForExists().exists());
    }


    @Test(timeout = 60000)
    public void performsThreeDSecureVerification() {
        enableThreeDSecure();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        tokenizeCard(THREE_D_SECURE_VERIFICATON);

        onDevice(withText("Added Protection")).waitForExists();
        onDevice().typeText("1234").pressEnter();

        onDevice(withText("Return To App")).waitForExists(1000);
        if (onDevice(withText("Return To App")).exists()) {
            clickWebViewText("Return To App");
        }

        getNonceDetails().check(text(containsString("Card Last Two: 02")));
        getNonceDetails().check(text(containsString("3DS isLiabilityShifted: true")));
        getNonceDetails().check(text(containsString("3DS isLiabilityShiftPossible: true")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPay() {
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(UNIONPAY_CREDIT));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withText("CVN")).perform(setText("123"));
        onDevice(withText("Postal Code")).perform(setText("12345"));
        onDevice(withText("Country Code")).perform(setText("01"));
        onDevice(withText("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());
        onDevice(withText("SMS Code")).perform(setText("12345"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 32")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPayWhenEnrollmentIsNotRequired() {
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(UNIONPAY_SMS_NOT_REQUIRED));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withText("CVN")).perform(setText("123"));
        onDevice(withText("Postal Code")).perform(setText("12345"));
        onDevice(withText("Country Code")).perform(setText("01"));
        onDevice(withText("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 85")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test
    public void tokenizesUnionPayWhenFirstSMSCodeIsInvalid() {
        setMerchantAccountId(Settings.getUnionPayMerchantAccountId(getTargetContext()));
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("Credit or Debit Card")).perform(click());
        onDevice(withText("Card Number")).perform(setText(UNIONPAY_CREDIT));
        onDevice(withText("12")).perform(click());
        onDevice(withText("2019")).perform(click());
        onDevice().pressBack();
        onDevice(withText("CVN")).perform(setText("123"));
        onDevice(withText("Postal Code")).perform(setText("12345"));
        onDevice(withText("Country Code")).perform(setText("01"));
        onDevice(withText("Mobile Number")).perform(setText("5555555555"));
        onDevice(withTextContaining("Add Card")).perform(click());
        onDevice(withText("SMS Code")).perform(setText("999999"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());
        onDevice(withText("SMS code is invalid")).check(text(is("SMS code is invalid")));
        onDevice(withContentDescription("SMS Code")).perform(clearTextField(), setText("12345"));
        onDevice(withTextContaining("Confirm", Button.class)).perform(click());

        getNonceDetails().check(text(containsString("Card Last Two: 32")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void tokenizesPayPal() {
        uninstallPayPalWallet();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("PayPal")).perform(click());
        clickWebViewText("Proceed with Sandbox Purchase", 3000);

        getNonceDetails().check(text(containsString("Email: bt_buyer_us@paypal.com")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void tokenizesPayPalWithATokenizationKey() {
        uninstallPayPalWallet();
        useTokenizationKey();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("PayPal")).perform(click());
        clickWebViewText("Proceed with Sandbox Purchase", 3000);

        getNonceDetails().check(text(containsString("Email: bt_buyer_us@paypal.com")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @RequiresDevice
    @Test(timeout = 60000)
    public void tokenizesGooglePay() {
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("Google Pay")).perform(click());
        onDevice(withText("Continue")).waitForExists().perform(click());

        getNonceDetails().check(text(containsString("Underlying Card Last Two")));

        onDevice(withText("Purchase")).perform(click());
        onDevice(withTextStartingWith("created")).check(text(endsWith("authorized")));
    }

    @Test(timeout = 60000)
    public void exitsAfterCancelingAddingAPaymentMethod() {
        uninstallPayPalWallet();
        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        onDevice(withText("PayPal")).perform(click());
        onDevice(withText("Proceed with Sandbox Purchase")).waitForExists();
        onDevice().pressBack();
        onDevice(withText("Pay with PayPal")).waitForExists();

        onDevice().pressBack();

        onDevice(withText("Add Payment Method")).check(text(equalToIgnoringCase("Add Payment Method")));
    }

    @Test(timeout = 60000)
    public void deletesPaymentMethod() {
        assumeTrue("braintree-api.com SSL connection does not support API < 21 (Lollipop)",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        setCustomerId("delete-payment-method-uitest");
        enableVaultManager();

        onDevice(withText("Add Payment Method")).waitForExists().waitForEnabled().perform(click());

        assumeFalse("Vaulted payment methods exist for this customer. Expecting no vaulted payments precondition for this test.",
                onDevice(withResourceId("com.braintreepayments.demo:id/bt_payment_method_title")).exists());

        tokenizeCard(VISA);

        onDevice(withText("Visa")).waitForExists().perform(click());
        onDevice(withText("Edit")).waitForExists().perform(click());
        onDevice(withResourceId("com.braintreepayments.demo:id/bt_payment_method_delete_icon"))
                .waitForExists().perform(click());

        onDevice(withText("Delete")).waitForExists().perform(click());
        onDevice(withText("Done")).waitForExists().perform(click());

        onDevice().pressBack();
        onDevice(withText("Select Payment Method")).waitForExists();

        assertFalse(onDevice(withResourceId("com.braintreepayments.demo:id/bt_payment_method_title")).exists());
    }

}

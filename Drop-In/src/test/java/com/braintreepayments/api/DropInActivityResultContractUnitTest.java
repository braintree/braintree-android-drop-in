package com.braintreepayments.api;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;
import static com.braintreepayments.api.DropInResult.EXTRA_DROP_IN_RESULT;
import static com.braintreepayments.api.DropInResult.EXTRA_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DropInActivityResultContractUnitTest {

    private Context context;

    @Before
    public void beforeEach() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void createIntent_returnsIntentWithExtras() {
        DropInActivityResultContract sut = new DropInActivityResultContract();

        String sessionId = "sample-session-id";
        DropInRequest dropInRequest = new DropInRequest();
        Authorization authorization = Authorization.fromString(Fixtures.BASE64_CLIENT_TOKEN);

        DropInIntentData input = new DropInIntentData(dropInRequest, authorization, sessionId);

        Intent intent = sut.createIntent(context, input);

        String expectedClass = "com.braintreepayments.api.DropInActivity";
        assertEquals(expectedClass, intent.getComponent().getClassName());

        assertEquals("sample-session-id", intent.getStringExtra(EXTRA_SESSION_ID));
        assertEquals(Fixtures.BASE64_CLIENT_TOKEN, intent.getStringExtra(EXTRA_AUTHORIZATION));

        Bundle requestBundle = intent.getBundleExtra(EXTRA_CHECKOUT_REQUEST_BUNDLE);
        assertNotNull(requestBundle.getParcelable(EXTRA_CHECKOUT_REQUEST));
    }

    @Test
    public void parseResult_whenResultIsOK_returnsDropInResultWithNoError() {
        DropInActivityResultContract sut = new DropInActivityResultContract();

        Intent successIntent = new Intent()
                .putExtra(EXTRA_DROP_IN_RESULT, new DropInResult());

        DropInResult dropInResult = sut.parseResult(RESULT_OK, successIntent);
        assertNotNull(dropInResult);
    }

    @Test
    public void parseResult_whenResultIsCANCELED_returnsDropInResultWithUserCanceledError() {
        DropInActivityResultContract sut = new DropInActivityResultContract();

        DropInResult dropInResult = sut.parseResult(RESULT_CANCELED, null);

        assertNotNull(dropInResult);
        UserCanceledException error = (UserCanceledException) dropInResult.getError();
        assertEquals("User canceled DropIn.", error.getMessage());
    }

    @Test
    public void parseResult_whenResultIsRESULT_FIRST_USER_returnsDropInResultWithErrorForwardedFromIntentExtras() {
        DropInActivityResultContract sut = new DropInActivityResultContract();

        Exception error = new Exception("sample error message");
        Intent errorIntent = new Intent()
                .putExtra(EXTRA_ERROR, error);

        DropInResult dropInResult = sut.parseResult(RESULT_FIRST_USER, errorIntent);

        assertNotNull(dropInResult);
        assertEquals("sample error message", dropInResult.getError().getMessage());
    }

    @Test
    public void parseResult_whenIntentIsNull_returnsNull() {
        DropInActivityResultContract sut = new DropInActivityResultContract();

        DropInResult dropInResult = sut.parseResult(RESULT_OK, null);
        assertNull(dropInResult);
    }
}
package com.braintreepayments.api;

import static com.braintreepayments.api.DropInClient.EXTRA_AUTHORIZATION;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST;
import static com.braintreepayments.api.DropInClient.EXTRA_CHECKOUT_REQUEST_BUNDLE;
import static com.braintreepayments.api.DropInClient.EXTRA_SESSION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.ComponentName;
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
}
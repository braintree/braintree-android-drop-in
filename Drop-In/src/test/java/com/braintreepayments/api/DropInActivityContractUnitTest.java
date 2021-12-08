package com.braintreepayments.api;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DropInActivityContractUnitTest {

    private DropInClient dropInClient;
    private Context context;
    private DropInRequest dropInRequest;
    private Intent intent;
    private DropInActivityResult result;

    @Before
    public void beforeEach() {
        context = mock(Context.class);
        dropInRequest = new DropInRequest();
        dropInClient = new MockDropInClientBuilder()
                .authorization(Authorization.fromString(Fixtures.TOKENIZATION_KEY))
                .dropInRequest(dropInRequest)
                .sessionId("session-id")
                .build();
        result = new DropInActivityResult();
        intent = new Intent().putExtra("com.braintreepayments.api.dropin.EXTRA_DROP_IN_ACTIVITY_RESULT", result);
    }

    @Test
    public void createIntent_returnsIntentWithExtras() {
        DropInActivityContract sut = new DropInActivityContract();
        Intent intent = sut.createIntent(context, dropInClient);

        assertEquals(Fixtures.TOKENIZATION_KEY, intent.getStringExtra("com.braintreepayments.api.EXTRA_AUTHORIZATION"));
        assertEquals("session-id", intent.getStringExtra("com.braintreepayments.api.EXTRA_SESSION_ID"));
        Bundle bundle = intent.getParcelableExtra("com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST_BUNDLE");
        assertEquals(dropInRequest, bundle.getParcelable("com.braintreepayments.api.EXTRA_CHECKOUT_REQUEST"));
    }

    @Test
    public void parseResult_whenResultOK_returnsDropInActivityResultFromIntent() {
        DropInActivityContract sut = new DropInActivityContract();

        DropInActivityResult dropInResult = sut.parseResult(RESULT_OK, intent);
        assertSame(result, dropInResult);
    }

    @Test
    public void parseResult_whenResultFirstUser_returnsDropInActivityResultFromIntent() {
        DropInActivityContract sut = new DropInActivityContract();

        DropInActivityResult dropInResult = sut.parseResult(RESULT_FIRST_USER, intent);
        assertSame(result, dropInResult);
    }

    @Test
    public void parseResult_whenResultCanceled_returnsDropInActivityResultWithUserCanceledException() {
        DropInActivityContract sut = new DropInActivityContract();

        DropInActivityResult dropInResult = sut.parseResult(RESULT_CANCELED, intent);
        assertNotNull(dropInResult.getError());
        assertTrue(dropInResult.getError() instanceof UserCanceledException);
        assertNull(dropInResult.getDropInResult());
    }
}
package com.braintreepayments.api;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DropInClientTest {

    private FragmentActivity activity;

    @Before
    public void beforeEach() {
        activity = mock(FragmentActivity.class);
    }

    @Test
    public void launchDropIn_launchesActivityForResult() {
        DropInClient sut = new DropInClient();
        DropInRequest dropInRequest = new DropInRequest();

        sut.launchDropIn(activity, dropInRequest, 123);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(captor.capture(), eq(123));

        Intent intent = captor.getValue();
        assertSame(DropInActivity.class.getName(), intent.getComponent().getClassName());

        DropInRequest parcelableRequest =
            intent.getParcelableExtra("com.braintreepayments.api.EXTRA_DROP_IN_REQUEST");
        assertNotNull(parcelableRequest);
    }
}
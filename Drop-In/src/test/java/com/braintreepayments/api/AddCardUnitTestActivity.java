package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.braintreepayments.api.dropin.R;
import static com.braintreepayments.api.TestTokenizationKey.TOKENIZATION_KEY;

public class AddCardUnitTestActivity extends AddCardActivity {

    public Context context;
    public DropInClient dropInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.bt_add_card_activity_theme);

        if (mDropInRequest == null) {
            mDropInRequest = new DropInRequest().tokenizationKey(TOKENIZATION_KEY);
        }

        Intent intent = new Intent()
                .putExtra(DropInRequest.EXTRA_CHECKOUT_REQUEST, mDropInRequest);
        setIntent(intent);

        super.onCreate(savedInstanceState);

    }

    @Override
    DropInClient getDropInClient() {
        if (dropInClient == null) {
            dropInClient = super.getDropInClient();
        }
        return dropInClient;
    }

    public void setDropInRequest(DropInRequest dropInRequest) {
        mDropInRequest = dropInRequest;
    }

    @Override
    public Context getApplicationContext() {
        if (context != null) {
            return context;
        }

        return super.getApplicationContext();
    }
}

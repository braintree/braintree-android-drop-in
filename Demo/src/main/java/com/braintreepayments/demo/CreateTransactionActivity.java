package com.braintreepayments.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.braintreepayments.api.CardNonce;
import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.demo.models.Transaction;

import androidx.appcompat.app.AppCompatActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_PAYMENT_METHOD_NONCE = "nonce";

    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_transaction_activity);
        loadingSpinner = findViewById(R.id.loading_spinner);
        setTitle(R.string.processing_transaction);

        sendNonceToServer((PaymentMethodNonce) getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD_NONCE));
    }

    private void sendNonceToServer(PaymentMethodNonce nonce) {
        Callback<Transaction> callback = new Callback<Transaction>() {
            @Override
            public void success(Transaction transaction, Response response) {
                if (transaction.getMessage() != null &&
                        transaction.getMessage().startsWith("created")) {
                    setStatus(R.string.transaction_complete);
                    setMessage(transaction.getMessage());
                } else {
                    setStatus(R.string.transaction_failed);
                    if (TextUtils.isEmpty(transaction.getMessage())) {
                        setMessage("Server response was empty or malformed");
                    } else {
                        setMessage(transaction.getMessage());
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setStatus(R.string.transaction_failed);
                setMessage("Unable to create a transaction. Response Code: " +
                        error.getResponse().getStatus() + " Response body: " +
                        error.getResponse().getBody());
            }
        };

        String nonceString = nonce.getString();

        TransactionRequest transactionRequest;
        if (Settings.isThreeDSecureEnabled(this)) {
            String threeDSecureMerchantId = Settings.getThreeDSecureMerchantAccountId(this);
            transactionRequest = new TransactionRequest(nonceString, threeDSecureMerchantId);

            if (Settings.isThreeDSecureRequired(this)) {
                transactionRequest.setThreeDSecureRequired(true);
            }
        } else if (isUnionPayCardNonce(nonce)) {
            String unionPayMerchantAccountId = Settings.getUnionPayMerchantAccountId(this);
            transactionRequest = new TransactionRequest(nonceString, unionPayMerchantAccountId);
        } else {
            String merchantAccountId = Settings.getMerchantAccountId(this);
            transactionRequest = new TransactionRequest(nonceString, merchantAccountId);
        }

        DemoApplication.getApiClient(this).createTransaction(transactionRequest, callback);
    }

    private void setStatus(int message) {
        loadingSpinner.setVisibility(View.GONE);
        setTitle(message);
        TextView status = findViewById(R.id.transaction_status);
        status.setText(message);
        status.setVisibility(View.VISIBLE);
    }

    private void setMessage(String message) {
        loadingSpinner.setVisibility(View.GONE);
        TextView textView = findViewById(R.id.transaction_message);
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
    }

    private boolean isUnionPayCardNonce(PaymentMethodNonce nonce) {
        return (nonce instanceof CardNonce) && ((CardNonce) nonce).getCardType().equals("UnionPay");
    }
}

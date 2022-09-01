package com.braintreepayments.api;

import static com.braintreepayments.api.Assertions.assertIsANonce;
import static com.braintreepayments.api.CardNumber.VISA;
import static com.braintreepayments.api.ExpirationDate.validExpirationYear;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4ClassRunner.class)
public class PaymentMethodClientTest {

    private static String TOKENIZATION_KEY = "sandbox_tmxhyf7d_dcpspy2brwdjr3qn";

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test(timeout = 10000)
    public void getPaymentMethodNonces_andDeletePaymentMethod_returnsCardNonce() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String clientToken = new TestClientTokenBuilder().withCustomerId().build();

        final BraintreeClient braintreeClient = new BraintreeClient(context, clientToken);
        CardClient cardClient = new CardClient(braintreeClient);
        final PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        Card card = new Card();
        card.setNumber(VISA);
        card.setExpirationMonth("12");
        card.setExpirationYear(validExpirationYear());
        card.setShouldValidate(true);

        cardClient.tokenize(card, (cardNonce, tokenizeError) -> {
            if (tokenizeError != null) {
                fail(tokenizeError.getMessage());
            }

            sut.getPaymentMethodNonces((paymentMethodNonces, getPaymentMethodNoncesError) -> {
                assertNull(getPaymentMethodNoncesError);
                assertNotNull(paymentMethodNonces);
                assertEquals(1, paymentMethodNonces.size());

                PaymentMethodNonce paymentMethodNonce = paymentMethodNonces.get(0);

                assertIsANonce(paymentMethodNonce.getString());

                sut.deletePaymentMethod(context, paymentMethodNonce, (deletedNonce, deletePaymentMethodError) -> {
                    assertNull(deletePaymentMethodError);
                    assertTrue(deletedNonce instanceof CardNonce);
                    latch.countDown();
                });
            });
        });

        latch.await();
    }

    @Test(timeout = 10000)
    public void getPaymentMethodNonces_failsWithATokenizationKey() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final BraintreeClient braintreeClient = new BraintreeClient(context, TOKENIZATION_KEY);
        CardClient cardClient = new CardClient(braintreeClient);
        final PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        Card card = new Card();
        card.setNumber(VISA);
        card.setExpirationMonth("04");
        card.setExpirationYear(validExpirationYear());

        cardClient.tokenize(card, (cardNonce, tokenizeError) -> {
            if (tokenizeError != null) {
                fail(tokenizeError.getMessage());
            }

            sut.getPaymentMethodNonces((paymentMethodNonces, getPaymentMethodNoncesError) -> {
                assertNull(paymentMethodNonces);

                assertTrue(getPaymentMethodNoncesError instanceof AuthorizationException);
                assertEquals("Tokenization key authorization not allowed for this endpoint. Please use an authentication method with upgraded permissions",
                        getPaymentMethodNoncesError.getMessage());
                latch.countDown();
            });
        });

        latch.await();
    }
}

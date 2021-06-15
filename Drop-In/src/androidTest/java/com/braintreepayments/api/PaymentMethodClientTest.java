package com.braintreepayments.api;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.braintreepayments.api.Assertions.assertIsANonce;
import static com.braintreepayments.api.CardNumber.VISA;
import static com.braintreepayments.api.ExpirationDate.validExpirationYear;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4ClassRunner.class)
public class PaymentMethodClientTest {

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

        cardClient.tokenize(card, new CardTokenizeCallback() {
            @Override
            public void onResult(@Nullable CardNonce cardNonce, @Nullable Exception error) {
                if (error != null) {
                    fail(error.getMessage());
                }

                sut.getPaymentMethodNonces(new GetPaymentMethodNoncesCallback() {
                    @Override
                    public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonces, @Nullable Exception error) {
                        assertNull(error);
                        assertNotNull(paymentMethodNonces);
                        assertEquals(1, paymentMethodNonces.size());

                        PaymentMethodNonce paymentMethodNonce = paymentMethodNonces.get(0);

                        assertIsANonce(paymentMethodNonce.getString());

                        sut.deletePaymentMethod(context, paymentMethodNonce, new DeletePaymentMethodNonceCallback() {
                            @Override
                            public void onResult(@Nullable PaymentMethodNonce deletedNonce, @Nullable Exception error) {
                                assertNull(error);
                                assertTrue(deletedNonce instanceof CardNonce);
                                latch.countDown();
                            }
                        });
                    }
                });
            }
        });

        latch.await();
    }

    @Test(timeout = 10000)
    public void getPaymentMethodNonces_failsWithATokenizationKey() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final BraintreeClient braintreeClient = new BraintreeClient(context, Fixtures.TOKENIZATION_KEY);
        CardClient cardClient = new CardClient(braintreeClient);
        final PaymentMethodClient sut = new PaymentMethodClient(braintreeClient);

        Card card = new Card();
        card.setNumber(VISA);
        card.setExpirationMonth("04");
        card.setExpirationYear(validExpirationYear());

        cardClient.tokenize(card, new CardTokenizeCallback() {
            @Override
            public void onResult(@Nullable CardNonce cardNonce, @Nullable Exception error) {
                if (error != null) {
                    fail(error.getMessage());
                }

                sut.getPaymentMethodNonces(new GetPaymentMethodNoncesCallback() {
                    @Override
                    public void onResult(@Nullable List<PaymentMethodNonce> paymentMethodNonces, @Nullable Exception error) {
                        assertNull(paymentMethodNonces);

                        assertTrue(error instanceof AuthorizationException);
                        assertEquals("Tokenization key authorization not allowed for this endpoint. Please use an authentication method with upgraded permissions",
                                error.getMessage());
                        latch.countDown();
                    }
                });
            }
        });

        latch.await();
    }
}

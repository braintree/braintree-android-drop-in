package com.braintreepayments.api;

import org.junit.Test;

import static com.braintreepayments.api.SelectPaymentMethodChildFragment.SUPPORTED_PAYMENT_METHODS;
import static com.braintreepayments.api.SelectPaymentMethodChildFragment.VAULT_MANAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SelectPaymentMethodChildFragmentListTest {

    @Test
    public void getItem() {
        SelectPaymentMethodChildFragmentList sut = new SelectPaymentMethodChildFragmentList(
                SUPPORTED_PAYMENT_METHODS);
        assertEquals(SUPPORTED_PAYMENT_METHODS, sut.getItem(0));
    }

    @Test
    public void add() {
        SelectPaymentMethodChildFragmentList sut = new SelectPaymentMethodChildFragmentList();
        sut.add(SUPPORTED_PAYMENT_METHODS);
        assertEquals(SUPPORTED_PAYMENT_METHODS, sut.getItem(0));
    }

    @Test
    public void size() {
        SelectPaymentMethodChildFragmentList sut =
            new SelectPaymentMethodChildFragmentList(SUPPORTED_PAYMENT_METHODS);
        assertEquals(1, sut.size());
    }

    @Test
    public void remove() {
        SelectPaymentMethodChildFragmentList sut =
            new SelectPaymentMethodChildFragmentList(SUPPORTED_PAYMENT_METHODS);
        sut.remove(0);
        assertEquals(0, sut.size());
    }

    @Test
    public void getItemId() {
        SelectPaymentMethodChildFragmentList sut =
            new SelectPaymentMethodChildFragmentList(VAULT_MANAGER);
        assertEquals(VAULT_MANAGER.getId(), sut.getItemId(0));
    }

    @Test
    public void containsItem_whenItemExists_returnsTrue() {
        SelectPaymentMethodChildFragmentList sut =
                new SelectPaymentMethodChildFragmentList(SUPPORTED_PAYMENT_METHODS);
        assertTrue(sut.containsItem(SUPPORTED_PAYMENT_METHODS.getId()));
    }

    @Test
    public void containsItem_whenItemDoesNotExist_returnsFalse() {
        SelectPaymentMethodChildFragmentList sut =
                new SelectPaymentMethodChildFragmentList(SUPPORTED_PAYMENT_METHODS);
        assertFalse(sut.containsItem(VAULT_MANAGER.getId()));
    }
}
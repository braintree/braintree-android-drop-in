package com.braintreepayments.api;

import org.junit.Test;

import static com.braintreepayments.api.BottomSheetViewType.SUPPORTED_PAYMENT_METHODS;
import static com.braintreepayments.api.BottomSheetViewType.VAULT_MANAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BottomSheetViewModelUnitTest {

    @Test
    public void getItem() {
        BottomSheetViewModel sut = new BottomSheetViewModel(
                SUPPORTED_PAYMENT_METHODS);
        assertEquals(SUPPORTED_PAYMENT_METHODS, sut.getItem(0));
    }

    @Test
    public void add() {
        BottomSheetViewModel sut = new BottomSheetViewModel();
        sut.add(SUPPORTED_PAYMENT_METHODS);
        assertEquals(SUPPORTED_PAYMENT_METHODS, sut.getItem(0));
    }

    @Test
    public void size() {
        BottomSheetViewModel sut =
            new BottomSheetViewModel(SUPPORTED_PAYMENT_METHODS);
        assertEquals(1, sut.size());
    }

    @Test
    public void remove() {
        BottomSheetViewModel sut =
            new BottomSheetViewModel(SUPPORTED_PAYMENT_METHODS);
        sut.remove(0);
        assertEquals(0, sut.size());
    }

    @Test
    public void getItemId() {
        BottomSheetViewModel sut =
            new BottomSheetViewModel(VAULT_MANAGER);
        assertEquals(VAULT_MANAGER.getId(), sut.getItemId(0));
    }

    @Test
    public void containsItem_whenItemExists_returnsTrue() {
        BottomSheetViewModel sut =
                new BottomSheetViewModel(SUPPORTED_PAYMENT_METHODS);
        assertTrue(sut.containsItem(SUPPORTED_PAYMENT_METHODS.getId()));
    }

    @Test
    public void containsItem_whenItemDoesNotExist_returnsFalse() {
        BottomSheetViewModel sut =
                new BottomSheetViewModel(SUPPORTED_PAYMENT_METHODS);
        assertFalse(sut.containsItem(VAULT_MANAGER.getId()));
    }
}
package com.braintreepayments.api;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;

import com.braintreepayments.api.dropin.R;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class SelectPaymentMethodFragmentTest {

    @Test
    public void onCreate_loaderIsVisible() {
        FragmentScenario<SelectPaymentMethodFragment> scenario =
                FragmentScenario.launch(SelectPaymentMethodFragment.class);
        scenario.moveToState(Lifecycle.State.CREATED);
        onView(withId(R.id.bt_select_payment_method_loader)).check(matches(isDisplayed()));
    }

    @Test
    public void onCreate_vaultEditButtonIsInvisible() {
        FragmentScenario<SelectPaymentMethodFragment> scenario =
            FragmentScenario.launch(SelectPaymentMethodFragment.class);

        scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onFragment(new FragmentScenario.FragmentAction<SelectPaymentMethodFragment>() {
            @Override
            public void perform(@NotNull SelectPaymentMethodFragment selectPaymentMethodFragment) {
                DropInViewModel viewModel =
                    new ViewModelProvider(selectPaymentMethodFragment.getActivity()).get(DropInViewModel.class);

                viewModel.setAvailablePaymentMethods(Collections.<PaymentMethodType>emptyList());

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                onView(withId(R.id.bt_supported_payment_methods_header)).check(doesNotExist());
                onView(withId(R.id.bt_vault_edit_button)).check(matches(not(isDisplayed())));
            }
        });
    }
}
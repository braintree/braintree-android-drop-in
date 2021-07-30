package com.braintreepayments.api;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.viewpager2.widget.ViewPager2;

import com.braintreepayments.api.dropin.R;

import java.util.ArrayList;

import static com.braintreepayments.api.SelectPaymentMethodChildFragment.VAULT_MANAGER;

public class SelectPaymentMethodParentFragment extends Fragment {

    private ViewPager2 viewPager;
    private SelectPaymentMethodChildFragmentAdapter viewPagerAdapter;
    private SelectPaymentMethodChildFragmentList childFragmentList;

    private DropInRequest dropInRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_select_payment_method_parent, container, false);

        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);

        childFragmentList = new SelectPaymentMethodChildFragmentList(
                SelectPaymentMethodChildFragment.SUPPORTED_PAYMENT_METHODS);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }

        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.setFragmentResultListener(DropInEvent.REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                onDropInEvent(DropInEvent.fromBundle(result));
            }
        });

        viewPagerAdapter = new SelectPaymentMethodChildFragmentAdapter(childFragmentManager, getLifecycle(), childFragmentList, dropInRequest);
        viewPager.setAdapter(viewPagerAdapter);

        // disable animation for now
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position < -1 || position > 1) {
                    // page is either offscreen to the left or offscreen to the right
                    page.setAlpha(0.0f);
                } else {
                    // page is visible
                    page.setAlpha(1.0f);
                }
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int position = viewPager.getCurrentItem();
                SelectPaymentMethodChildFragment visibleFragment =
                    childFragmentList.getItem(position);

                if (visibleFragment == VAULT_MANAGER) {
                    onDismissVaultManager();
                } else {
                    sendDropInEvent(new DropInEvent(DropInEventType.CANCEL_DROPIN));
                    remove();
                }
            }
        });

        return view;
    }

    @VisibleForTesting
    void onDropInEvent(DropInEvent event) {
        switch (event.getType()) {
            case SHOW_VAULT_MANAGER:
                onShowVaultManager();
                break;
            case DISMISS_VAULT_MANAGER:
                onDismissVaultManager();
                break;
        }

        // propagate event up to the parent activity
        sendDropInEvent(event);
    }

    private void onShowVaultManager() {
        int targetHeight = dpToPixels(400);
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.height = targetHeight;
        viewPager.setLayoutParams(layoutParams);
        requestLayout();

        childFragmentList.add(VAULT_MANAGER);
        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(1, false);
    }

    private void onDismissVaultManager() {
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        viewPager.setLayoutParams(layoutParams);
        requestLayout();

        childFragmentList.remove(1);
        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0, false);
    }

    private void requestLayout() {
        View rootView = getView();
        if (rootView != null) {
            rootView.requestLayout();
        }
    }

    private void sendDropInEvent(DropInEvent event) {
        getParentFragmentManager().setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle());
    }

    private int dpToPixels(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }
}
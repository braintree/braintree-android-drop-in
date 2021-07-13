package com.braintreepayments.api;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParentFragment extends Fragment {

    private ViewPager2 viewPager;
    private DropInFragmentAdapter viewPagerAdapter;
    private List<FragmentType> fragments;

    private DropInRequest dropInRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent, container, false);

        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);

        fragments = new ArrayList<>();
        fragments.add(FragmentType.SELECT_PAYMENT_METHOD);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
        }

        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.setFragmentResultListener( "BRAINTREE_EVENT", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Parcelable braintreeResult = result.getParcelable("BRAINTREE_RESULT");

                boolean didHandleEvent = false;
                if (braintreeResult instanceof DropInUIEvent) {
                    DropInUIEvent event = (DropInUIEvent) braintreeResult;
                    if (event.getType() == DropInUIEventType.SHOW_VAULT_MANAGER) {
                        didHandleEvent = true;
                        fragments.add(FragmentType.VAULT_MANAGER);
                        viewPagerAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(1, true);

                        int targetHeight = dpToPixels(400);
                        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                        layoutParams.height = targetHeight;
                        viewPager.setLayoutParams(layoutParams);
                    } else if (event.getType() == DropInUIEventType.DISMISS_VAULT_MANAGER) {
                        didHandleEvent = true;

                        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        viewPager.setLayoutParams(layoutParams);

                        fragments.remove(1);
                        viewPagerAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(0, true);
                    }
                }

                if (!didHandleEvent) {
                    // propagate event up to DropIn activity
                    sendBraintreeEvent(braintreeResult);
                }
            }
        });

        viewPagerAdapter = new DropInFragmentAdapter(childFragmentManager, getLifecycle(), fragments, dropInRequest);
        viewPager.setAdapter(viewPagerAdapter);

        return view;
    }

    private void sendBraintreeEvent(Parcelable eventResult) {
        Bundle result = new Bundle();
        result.putParcelable("BRAINTREE_RESULT", eventResult);
        getParentFragmentManager().setFragmentResult("BRAINTREE_EVENT", result);
    }

    private int dpToPixels(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }
}
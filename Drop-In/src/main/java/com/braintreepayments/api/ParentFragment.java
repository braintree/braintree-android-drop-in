package com.braintreepayments.api;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.R;

import java.util.Arrays;
import java.util.List;

public class ParentFragment extends Fragment {

    private ViewPager2 viewPager;
    private DropInFragmentAdapter viewPagerAdapter;
    private List<FragmentType> fragments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent, container, false);

        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);

        fragments = Arrays.asList(FragmentType.SELECT_PAYMENT_METHOD);
        viewPagerAdapter = new DropInFragmentAdapter(requireActivity(), fragments);
        viewPager.setAdapter(viewPagerAdapter);

        return view;
    }
}
package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

public class SelectPaymentMethodFragment extends Fragment {

    private View mBottomSheet;
    private ViewSwitcher mLoadingViewSwitcher;
    private TextView mSupportedPaymentMethodsHeader;

    @VisibleForTesting
    protected ListView mSupportedPaymentMethodListView;

    private View mVaultedPaymentMethodsContainer;
    private RecyclerView mVaultedPaymentMethodsView;
    private Button mVaultManagerButton;

    public SelectPaymentMethodFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_payment_method, container, false);

        mBottomSheet = view.findViewById(R.id.bt_dropin_bottom_sheet);
        mLoadingViewSwitcher = view.findViewById(R.id.bt_loading_view_switcher);
        mSupportedPaymentMethodsHeader = view.findViewById(R.id.bt_supported_payment_methods_header);
        mSupportedPaymentMethodListView = view.findViewById(R.id.bt_supported_payment_methods);
        mVaultedPaymentMethodsContainer = view.findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = view.findViewById(R.id.bt_vaulted_payment_methods);
        mVaultManagerButton = view.findViewById(R.id.bt_vault_edit_button);
        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

        return view;
    }

    private void showSupportedPaymentMethods(boolean googlePaymentEnabled) {
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(this, this);
        adapter.setup(mConfiguration, mDropInRequest, googlePaymentEnabled, mClientTokenPresent);
        mSupportedPaymentMethodListView.setAdapter(adapter);
        mLoadingViewSwitcher.setDisplayedChild(1);
        fetchPaymentMethodNonces(false);
    }
}
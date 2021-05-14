package com.braintreepayments.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.ArrayList;
import java.util.List;

import static com.braintreepayments.api.DropInActivity.DELETE_PAYMENT_METHOD_NONCE_CODE;
import static com.braintreepayments.api.DropInActivity.EXTRA_PAYMENT_METHOD_NONCES;
import static com.braintreepayments.api.DropInRequest.EXTRA_CHECKOUT_REQUEST;

public class SelectPaymentMethodFragment extends Fragment implements SupportedPaymentMethodsAdapter.PaymentMethodSelectedListener {

    private String mDeviceData;
    private ViewSwitcher mLoadingViewSwitcher;
    private TextView mSupportedPaymentMethodsHeader;

    @VisibleForTesting
    protected ListView mSupportedPaymentMethodListView;

    private View mVaultedPaymentMethodsContainer;
    private RecyclerView mVaultedPaymentMethodsView;
    private Button mVaultManagerButton;

    private DropInRequest dropInRequest;

    private BraintreeFragment braintreeFragment;
    private DropInViewModel dropInViewModel;

    public SelectPaymentMethodFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            try {
                dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
                braintreeFragment = BraintreeFragment.newInstance(this, dropInRequest.getAuthorization());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_select_payment_method, container, false);

        mLoadingViewSwitcher = view.findViewById(R.id.bt_loading_view_switcher);
        mSupportedPaymentMethodsHeader = view.findViewById(R.id.bt_supported_payment_methods_header);
        mSupportedPaymentMethodListView = view.findViewById(R.id.bt_supported_payment_methods);
        mVaultedPaymentMethodsContainer = view.findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = view.findViewById(R.id.bt_vaulted_payment_methods);
        mVaultManagerButton = view.findViewById(R.id.bt_vault_edit_button);

        mVaultManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVaultEditButtonClick(view);
            }
        });

        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

        if (dropInRequest.shouldCollectDeviceData() && TextUtils.isEmpty(mDeviceData)) {
            DataCollector.collectDeviceData(braintreeFragment, new BraintreeResponseListener<String>() {
                @Override
                public void onResponse(String deviceData) {
                    mDeviceData = deviceData;
                }
            });
        }

        braintreeFragment.sendAnalyticsEvent("appeared");

        DropInViewModelFactory viewModelFactory =
                new DropInViewModelFactory(requireActivity(), dropInRequest);
        dropInViewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(DropInViewModel.class);

        dropInViewModel.getAvailablePaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<PaymentMethodType>>() {
            @Override
            public void onChanged(List<PaymentMethodType> paymentMethodTypes) {
                showSupportedPaymentMethods();
            }
        });

        dropInViewModel.getVaultedPaymentMethodNonces().observe(getViewLifecycleOwner(), new Observer<List<PaymentMethodNonce>>() {
            @Override
            public void onChanged(List<PaymentMethodNonce> paymentMethodNonces) {
                showVaultedPaymentMethods(paymentMethodNonces);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: show spinner while fetching nonces
        dropInViewModel.fetchPaymentMethodNonces(true);
        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    private void showSupportedPaymentMethods() {
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(getActivity(), this, dropInViewModel.getAvailablePaymentMethods());
        mSupportedPaymentMethodListView.setAdapter(adapter);
        mLoadingViewSwitcher.setDisplayedChild(1);
        dropInViewModel.fetchPaymentMethodNonces(false);
    }

    @Override
    public void onPaymentMethodSelected(PaymentMethodType type) {
        mLoadingViewSwitcher.setDisplayedChild(0);
        DropInActivity activity = ((DropInActivity) getActivity());
        activity.onPaymentMethodSelected(type);
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> paymentMethodNonces) {
        if (paymentMethodNonces.size() > 0) {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_other);
            mVaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);

            VaultedPaymentMethodsAdapter vaultedPaymentMethodsAdapter = new VaultedPaymentMethodsAdapter(new PaymentMethodNonceCreatedListener() {
                @Override
                public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                    if (paymentMethodNonce instanceof CardNonce) {
                        braintreeFragment.sendAnalyticsEvent("vaulted-card.select");
                    }
                    dropInViewModel.setSelectedPaymentMethodNonce(paymentMethodNonce);
                }
            }, paymentMethodNonces);

            mVaultedPaymentMethodsView.setAdapter(vaultedPaymentMethodsAdapter);

            if (dropInRequest.isVaultManagerEnabled()) {
                mVaultManagerButton.setVisibility(View.VISIBLE);
            }

            boolean hasCardNonce = false;
            for (PaymentMethodNonce nonce : paymentMethodNonces) {
                if (nonce instanceof CardNonce) {
                    hasCardNonce = true;
                    break;
                }
            }

            if (hasCardNonce) {
                braintreeFragment.sendAnalyticsEvent("vaulted-card.appear");
            }
        } else {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
        }
    }

    public void onVaultEditButtonClick(View view) {
        ArrayList<Parcelable> parcelableArrayList = new ArrayList<Parcelable>(braintreeFragment.getCachedPaymentMethodNonces());

        Intent intent = new Intent(getActivity(), VaultManagerActivity.class)
                .putExtra(EXTRA_CHECKOUT_REQUEST, dropInRequest)
                .putParcelableArrayListExtra(EXTRA_PAYMENT_METHOD_NONCES, parcelableArrayList);
        startActivityForResult(intent, DELETE_PAYMENT_METHOD_NONCE_CODE);

        braintreeFragment.sendAnalyticsEvent("manager.appeared");
    }
}
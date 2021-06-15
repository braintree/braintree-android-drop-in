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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.List;

public class SelectPaymentMethodFragment extends Fragment implements SupportedPaymentMethodSelectedListener, VaultedPaymentMethodSelectedListener {

    private ViewSwitcher mLoadingViewSwitcher;
    private TextView mSupportedPaymentMethodsHeader;

    @VisibleForTesting
    protected ListView mSupportedPaymentMethodListView;

    private View mVaultedPaymentMethodsContainer;
    private RecyclerView mVaultedPaymentMethodsView;
    private Button mVaultManagerButton;

    private DropInRequest dropInRequest;
    private DropInViewModel dropInViewModel;

    public SelectPaymentMethodFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
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

        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getAvailablePaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<PaymentMethodType>>() {
            @Override
            public void onChanged(List<PaymentMethodType> paymentMethodTypes) {
                showSupportedPaymentMethods(paymentMethodTypes);
            }
        });

        dropInViewModel.getVaultedPaymentMethodNonces().observe(getViewLifecycleOwner(), new Observer<List<PaymentMethodNonce>>() {
            @Override
            public void onChanged(List<PaymentMethodNonce> paymentMethodNonces) {
                showVaultedPaymentMethods(paymentMethodNonces);
            }
        });

        dropInViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    mLoadingViewSwitcher.setDisplayedChild(0);
                } else {
                    mLoadingViewSwitcher.setDisplayedChild(1);
                }
            }
        });

        mVaultManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVaultManager();
            }
        });

        sendAnalyticsEvent("appeared");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        dropInViewModel.setIsLoading(true);
    }

    private void showVaultManager() {
//        DropInActivity activity = ((DropInActivity) requireActivity());
//        activity.showVaultManager();
    }

    private void sendAnalyticsEvent(String eventFragment) {
//        DropInActivity activity = ((DropInActivity) requireActivity());
//        activity.sendAnalyticsEvent(eventFragment);
    }

    private void updateVaultedPaymentMethodNonces(boolean refetch) {
//        DropInActivity activity = ((DropInActivity) requireActivity());
//        activity.updateVaultedPaymentMethodNonces(refetch);
    }

    private void showSupportedPaymentMethods(List<PaymentMethodType> availablePaymentMethods) {
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(getActivity(), this, availablePaymentMethods);
        mSupportedPaymentMethodListView.setAdapter(adapter);
        dropInViewModel.setIsLoading(false);

        updateVaultedPaymentMethodNonces(false);
    }

    @Override
    public void onPaymentMethodSelected(PaymentMethodType type) {
        dropInViewModel.setIsLoading(true);

        DropInActivity activity = ((DropInActivity) requireActivity());
        activity.onPaymentMethodSelected(type);
    }

    @Override
    public void onVaultedPaymentMethodSelected(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            sendAnalyticsEvent("vaulted-card.select");
        }

        DropInActivity activity = ((DropInActivity) requireActivity());
        activity.onPaymentMethodNonceCreated(paymentMethodNonce);
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> paymentMethodNonces) {
        if (paymentMethodNonces.size() > 0) {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_other);
            mVaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);

            VaultedPaymentMethodsAdapter vaultedPaymentMethodsAdapter =
                new VaultedPaymentMethodsAdapter(this, paymentMethodNonces);

            mVaultedPaymentMethodsView.setAdapter(vaultedPaymentMethodsAdapter);

            if (dropInRequest.isVaultManagerEnabled()) {
                mVaultManagerButton.setVisibility(View.VISIBLE);
            }

            if (containsCardNonce(paymentMethodNonces)) {
                sendAnalyticsEvent("vaulted-card.appear");
            }
        } else {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_select_payment_method);
            mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
        }
    }

    private static boolean containsCardNonce(List<PaymentMethodNonce> paymentMethodNonces) {
        for (PaymentMethodNonce nonce : paymentMethodNonces) {
            if (nonce instanceof CardNonce) {
                return true;
            }
        }
        return false;
    }
}
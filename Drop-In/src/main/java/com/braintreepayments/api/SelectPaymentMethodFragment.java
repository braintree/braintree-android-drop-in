package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcelable;
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

    @VisibleForTesting
    DropInViewModel dropInViewModel;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_select_payment_method, container, false);

        mLoadingViewSwitcher = view.findViewById(R.id.bt_loading_view_switcher);
        mSupportedPaymentMethodsHeader = view.findViewById(R.id.bt_supported_payment_methods_header);
        mSupportedPaymentMethodListView = view.findViewById(R.id.bt_supported_payment_methods);
        mVaultedPaymentMethodsContainer = view.findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = view.findViewById(R.id.bt_vaulted_payment_methods);

        mVaultManagerButton = view.findViewById(R.id.bt_vault_edit_button);

        mVaultedPaymentMethodsView.setLayoutManager(new LinearLayoutManager(requireActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(mVaultedPaymentMethodsView);

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getSupportedPaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<DropInPaymentMethodType>>() {
            @Override
            public void onChanged(List<DropInPaymentMethodType> paymentMethodTypes) {
                showSupportedPaymentMethods(paymentMethodTypes);
            }
        });

        dropInViewModel.getVaultedPaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<PaymentMethodNonce>>() {
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
                sendBraintreeEvent(new DropInEvent(DropInEventType.SHOW_VAULT_MANAGER));
            }
        });

        sendBraintreeEvent(DropInEvent.createSendAnalyticsEvent("appeared"));
        return view;
    }

    private void sendBraintreeEvent(Parcelable eventResult) {
        Bundle result = new Bundle();
        result.putParcelable(DropInEvent.RESULT_KEY, eventResult);
        getParentFragmentManager().setFragmentResult(DropInEvent.REQUEST_KEY, result);
    }

    private void showSupportedPaymentMethods(List<DropInPaymentMethodType> availablePaymentMethods) {
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                availablePaymentMethods, this);
        mSupportedPaymentMethodListView.setAdapter(adapter);
        dropInViewModel.setIsLoading(false);

        sendBraintreeEvent(new DropInEvent(DropInEventType.DID_DISPLAY_SUPPORTED_PAYMENT_METHODS));
    }

    @Override
    public void onPaymentMethodSelected(DropInPaymentMethodType type) {
        dropInViewModel.setIsLoading(true);
        sendBraintreeEvent(
                DropInEvent.createSupportedPaymentMethodSelectedEvent(type));
    }

    @Override
    public void onVaultedPaymentMethodSelected(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            sendBraintreeEvent(DropInEvent.createSendAnalyticsEvent("vaulted-card.select"));
        }

        sendBraintreeEvent(
                DropInEvent.createVaultedPaymentMethodSelectedEvent(paymentMethodNonce));
    }

    private void showVaultedPaymentMethods(List<PaymentMethodNonce> paymentMethodNonces) {
        if (paymentMethodNonces.size() > 0) {
            mSupportedPaymentMethodsHeader.setText(R.string.bt_other);
            mVaultedPaymentMethodsContainer.setVisibility(View.VISIBLE);

            VaultedPaymentMethodsAdapter vaultedPaymentMethodsAdapter =
                new VaultedPaymentMethodsAdapter(paymentMethodNonces, this);

            mVaultedPaymentMethodsView.setAdapter(vaultedPaymentMethodsAdapter);

            if (dropInRequest.isVaultManagerEnabled()) {
                mVaultManagerButton.setVisibility(View.VISIBLE);
            }

            if (containsCardNonce(paymentMethodNonces)) {
                sendBraintreeEvent(DropInEvent.createSendAnalyticsEvent("vaulted-card.appear"));
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
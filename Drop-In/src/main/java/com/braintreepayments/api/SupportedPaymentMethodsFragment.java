package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.dropin.R;

import java.util.List;

public class SupportedPaymentMethodsFragment extends DropInFragment implements SupportedPaymentMethodSelectedListener, VaultedPaymentMethodSelectedListener {

    @VisibleForTesting
    View mLoadingIndicatorWrapper;

    private TextView mSupportedPaymentMethodsHeader;

    @VisibleForTesting
    RecyclerView mSupportedPaymentMethodsView;

    @VisibleForTesting
    RecyclerView mVaultedPaymentMethodsView;

    private View mVaultedPaymentMethodsContainer;
    private Button mVaultManagerButton;

    private DropInRequest dropInRequest;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    public SupportedPaymentMethodsFragment() {}

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
        View view = inflater.inflate(R.layout.bt_fragment_supported_payment_methods, container, false);

        mLoadingIndicatorWrapper = view.findViewById(R.id.bt_select_payment_method_loader_wrapper);
        mSupportedPaymentMethodsHeader = view.findViewById(R.id.bt_supported_payment_methods_header);
        mSupportedPaymentMethodsView = view.findViewById(R.id.bt_supported_payment_methods);
        mVaultedPaymentMethodsContainer = view.findViewById(R.id.bt_vaulted_payment_methods_wrapper);
        mVaultedPaymentMethodsView = view.findViewById(R.id.bt_vaulted_payment_methods);
        mVaultManagerButton = view.findViewById(R.id.bt_vault_edit_button);

        LinearLayoutManager supportedPaymentMethodsLayoutManager =
                new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        mSupportedPaymentMethodsView.setLayoutManager(supportedPaymentMethodsLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                requireActivity(), supportedPaymentMethodsLayoutManager.getOrientation());
        mSupportedPaymentMethodsView.addItemDecoration(dividerItemDecoration);

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

        dropInViewModel.getDropInState().observe(getViewLifecycleOwner(), new Observer<DropInState>() {
            @Override
            public void onChanged(DropInState dropInState) {
                if (dropInState == DropInState.FINISHING) {
                    // hide vault manager (if necessary) and show loader
                    mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
                    showLoader();
                }
            }
        });

        mVaultManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDropInEvent(new DropInEvent(DropInEventType.SHOW_VAULT_MANAGER));
            }
        });

        sendAnalyticsEvent("appeared");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // show supported payment methods immediately if possible
        List<DropInPaymentMethodType> supportedPaymentMethods =
            dropInViewModel.getSupportedPaymentMethods().getValue();
        if (supportedPaymentMethods != null) {
            showSupportedPaymentMethods(supportedPaymentMethods);
        }

        // show vaulted payment methods immediately if possible
        List<PaymentMethodNonce> vaultedPaymentMethods =
            dropInViewModel.getVaultedPaymentMethods().getValue();
        if (vaultedPaymentMethods != null) {
            showVaultedPaymentMethods(vaultedPaymentMethods);
        }
    }

    private void showLoader() {
        mLoadingIndicatorWrapper.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        mLoadingIndicatorWrapper.setVisibility(View.GONE);
    }

    private void showSupportedPaymentMethods(List<DropInPaymentMethodType> availablePaymentMethods) {
        hideLoader();
        SupportedPaymentMethodsAdapter adapter = new SupportedPaymentMethodsAdapter(
                availablePaymentMethods, this);
        mSupportedPaymentMethodsView.setAdapter(adapter);
    }

    @Override
    public void onPaymentMethodSelected(DropInPaymentMethodType type) {
        boolean paymentTypeWillInitiateAsyncRequest = (type == DropInPaymentMethodType.PAYPAL)
            || (type == DropInPaymentMethodType.PAY_WITH_VENMO);

        if (paymentTypeWillInitiateAsyncRequest) {
            // hide vault manager (if necessary) and show loader
            mVaultedPaymentMethodsContainer.setVisibility(View.GONE);
            showLoader();
        }
        sendDropInEvent(
                DropInEvent.createSupportedPaymentMethodSelectedEvent(type));
    }

    @Override
    public void onVaultedPaymentMethodSelected(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce instanceof CardNonce) {
            sendAnalyticsEvent("vaulted-card.select");
        }

        sendDropInEvent(
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
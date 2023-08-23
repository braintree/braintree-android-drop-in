package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.AccessibleSupportedCardTypesView;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

public class AddCardFragment extends DropInFragment implements OnCardFormSubmitListener,
        CardEditText.OnCardTypeChangedListener {

    private static final String EXTRA_DROP_IN_REQUEST = "EXTRA_DROP_IN_REQUEST";
    private static final String EXTRA_CARD_NUMBER = "EXTRA_CARD_NUMBER";

    @VisibleForTesting
    CardForm cardForm;

    @VisibleForTesting
    AccessibleSupportedCardTypesView supportedCardTypesView;

    private AnimatedButtonView animatedButtonView;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    BraintreeErrorInspector braintreeErrorInspector = new BraintreeErrorInspector();

    static AddCardFragment from(DropInRequest dropInRequest, @Nullable String cardNumber) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DROP_IN_REQUEST, dropInRequest);
        if (cardNumber != null) {
            args.putString(EXTRA_CARD_NUMBER, cardNumber);
        }

        AddCardFragment instance = new AddCardFragment();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bt_fragment_add_card, container, false);

        cardForm = view.findViewById(R.id.bt_card_form);
        supportedCardTypesView = view.findViewById(R.id.bt_supported_card_types);

        Bundle args = getArguments();
        DropInRequest dropInRequest = (DropInRequest) args.getParcelable("EXTRA_DROP_IN_REQUEST");

        if (dropInRequest.areCardLogosDisabled()) {
            supportedCardTypesView.setVisibility(View.GONE);
        }

        animatedButtonView = view.findViewById(R.id.bt_animated_button_view);

        TextView textView = view.findViewById(R.id.bt_privacy_policy);
        String noticeOfCollection = getString(R.string.bt_notice_of_collection);
        NoticeOfCollectionHelper.setNoticeOfCollectionText(textView, noticeOfCollection);

        animatedButtonView.setClickListener(v -> onCardFormSubmit());

        cardForm.getCardEditText().displayCardTypeIcon(false);

        cardForm.cardRequired(true).setup(requireActivity());

        cardForm.setOnCardTypeChangedListener(this);
        cardForm.setOnCardFormSubmitListener(this);

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getSupportedCardTypes().observe(getViewLifecycleOwner(), cardTypes ->
                supportedCardTypesView.setSupportedCardTypes(cardTypes.toArray(new CardType[0])));

        dropInViewModel.getCardTokenizationError().observe(getViewLifecycleOwner(), error -> {
            if (error instanceof ErrorWithResponse) {
                setErrors((ErrorWithResponse) error);
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack();
                remove();
            }
        });

        Toolbar toolbar = view.findViewById(R.id.bt_toolbar);
        // Add a label to the toolbar back button for the screen reader and make it accessible via tab navigation
        toolbar.setNavigationContentDescription(R.string.bt_back);
        toolbar.setTouchscreenBlocksFocus(false);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        sendAnalyticsEvent("card.selected");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cardForm.getCardEditText().requestFocus();

        Bundle args = getArguments();
        if (args != null) {
            String cardNumber = args.getString("EXTRA_CARD_NUMBER");
            if (cardNumber != null) {
                cardForm.getCardEditText().setText(cardNumber);
                CardType cardType = cardForm.getCardEditText().getCardType();
                onCardTypeChanged(cardType);

                // reset bundle args to prevent existing input from being overriden
                // when the fragment is re-created (e.g. on configuration change)
                Bundle newArgs = new Bundle();
                newArgs.putString(EXTRA_DROP_IN_REQUEST, args.getString(EXTRA_DROP_IN_REQUEST));
                setArguments(newArgs);
            }
        }
    }

    private boolean isValid() {
        return cardForm.isValid() && isCardTypeValid();
    }

    private boolean isCardTypeValid() {
        if (dropInViewModel.getSupportedCardTypes().getValue() != null) {
            return (dropInViewModel.getSupportedCardTypes().getValue()).contains(cardForm.getCardEditText()
                    .getCardType());
        } else {
            return false;
        }
    }

    private void showCardNotSupportedError() {
        cardForm.getCardEditText().setError(requireContext().getString(R.string.bt_card_not_accepted));
        animatedButtonView.showButton();
    }

    void setErrors(ErrorWithResponse errors) {
        boolean isDuplicatePaymentMethod = braintreeErrorInspector.isDuplicatePaymentError(errors);
        if (isDuplicatePaymentMethod) {
            cardForm.setCardNumberError(getString(R.string.bt_card_already_exists));
        } else {
            BraintreeError formErrors = errors.errorFor("creditCard");
            if (formErrors != null) {
                if (formErrors.errorFor("number") != null) {
                    cardForm.setCardNumberError(requireContext().getString(R.string.bt_card_number_invalid));
                }
            }
        }
        animatedButtonView.showButton();
    }

    @Override
    public void onCardFormSubmit() {
        if (isValid()) {
            animatedButtonView.showLoading();
            String cardNumber = cardForm.getCardNumber();
            sendDropInEvent(DropInEvent.createAddCardSubmitEvent(cardNumber));
        } else {
            if (!cardForm.isValid()) {
                animatedButtonView.showButton();
                cardForm.validate();
            } else if (!isCardTypeValid()) {
                showCardNotSupportedError();
            }
        }
    }

    @Override
    public void onCardTypeChanged(CardType cardType) {
        if (cardType == CardType.EMPTY && dropInViewModel.getSupportedCardTypes().getValue() != null) {
            supportedCardTypesView.setSupportedCardTypes(dropInViewModel.getSupportedCardTypes().getValue().toArray(new CardType[0]));
        } else {
            supportedCardTypesView.setSelected(cardType);
        }
    }
}
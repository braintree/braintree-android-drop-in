package com.braintreepayments.api;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

import java.util.List;

public class AddCardFragment extends DropInFragment implements OnCardFormSubmitListener,
        CardEditText.OnCardTypeChangedListener {

    @VisibleForTesting
    CardForm cardForm;

    private SupportedCardTypesView supportedCardTypesView;
    private AnimatedButtonView animatedButtonView;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    static AddCardFragment from(DropInRequest dropInRequest, @Nullable String cardNumber) {
        Bundle args = new Bundle();
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest);
        if (cardNumber != null) {
            args.putString("EXTRA_CARD_NUMBER", cardNumber);
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
        animatedButtonView = view.findViewById(R.id.bt_animated_button_view);

        animatedButtonView.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCardFormSubmit();
            }
        });

        cardForm.getCardEditText().displayCardTypeIcon(false);

        cardForm.cardRequired(true).setup(requireActivity());

        cardForm.setOnCardTypeChangedListener(this);
        cardForm.setOnCardFormSubmitListener(this);

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getSupportedCardTypes().observe(getViewLifecycleOwner(), new Observer<List<CardType>>() {
            @Override
            public void onChanged(List<CardType> cardTypes) {
                supportedCardTypesView.setSupportedCardTypes(cardTypes.toArray(new CardType[0]));
            }
        });

        dropInViewModel.getCardTokenizationError().observe(getViewLifecycleOwner(), new Observer<Exception>() {
            @Override
            public void onChanged(Exception error) {
                if (error instanceof ErrorWithResponse) {
                    setErrors((ErrorWithResponse) error);
                }
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

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
            }
            // prevent card number from overriding existing input
            setArguments(null);
        }
    }

    private boolean isValid() {
        return cardForm.isValid() && isCardTypeValid();
    }

    private boolean isCardTypeValid() {
        return (dropInViewModel.getSupportedCardTypes().getValue()).contains(cardForm.getCardEditText()
                .getCardType());
    }

    private void showCardNotSupportedError() {
        cardForm.getCardEditText().setError(getContext().getString(R.string.bt_card_not_accepted));
        animatedButtonView.showButton();
    }

    void setErrors(ErrorWithResponse errors) {
        BraintreeError formErrors = errors.errorFor("creditCard");

        if (formErrors != null) {
            if (formErrors.errorFor("number") != null) {
                cardForm.setCardNumberError(getContext().getString(R.string.bt_card_number_invalid));
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
        if (cardType == CardType.EMPTY) {
            supportedCardTypesView.setSupportedCardTypes(dropInViewModel.getSupportedCardTypes().getValue().toArray(new CardType[0]));
        } else {
            supportedCardTypesView.setSelected(cardType);
        }
    }
}
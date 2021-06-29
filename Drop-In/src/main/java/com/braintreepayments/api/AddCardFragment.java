package com.braintreepayments.api;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

import java.util.List;

public class AddCardFragment extends Fragment implements  OnCardFormSubmitListener,
        CardEditText.OnCardTypeChangedListener {

    private CardForm cardForm;
    private SupportedCardTypesView supportedCardTypesView;
    private AnimatedButtonView animatedButtonView;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    public AddCardFragment() {}

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

        Bundle args = getArguments();
        if (args != null) {
            String cardNumber = args.getString("EXTRA_CARD_NUMBER");
            if (cardNumber != null) {
                cardForm.getCardEditText().setText(cardNumber);
            }
        }
        return view;
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

    private void sendBraintreeEvent(Parcelable eventResult) {
        Bundle result = new Bundle();
        result.putParcelable("BRAINTREE_RESULT", eventResult);
        getParentFragmentManager().setFragmentResult("BRAINTREE_EVENT", result);
    }

    @Override
    public void onCardFormSubmit() {
        if (isValid()) {
            animatedButtonView.showLoading();
            sendBraintreeEvent(new AddCardEvent(cardForm.getCardNumber()));
        } else {
            if (!cardForm.isValid()) {
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
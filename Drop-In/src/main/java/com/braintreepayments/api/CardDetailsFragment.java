package com.braintreepayments.api;

import android.os.Bundle;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.OnCardFormFieldFocusedListener;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;

public class CardDetailsFragment extends Fragment implements OnCardFormSubmitListener, OnCardFormFieldFocusedListener {

    @VisibleForTesting
    CardForm cardForm;
    private AnimatedButtonView animatedButtonView;

    private DropInRequest dropInRequest;
    private CardFormConfiguration configuration;
    private String cardNumber;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    public CardDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
            configuration = args.getParcelable("EXTRA_CARD_FORM_CONFIGURATION");
            cardNumber = args.getString("EXTRA_CARD_NUMBER");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bt_fragment_card_details, container, false);
        cardForm = view.findViewById(R.id.bt_card_form);
        animatedButtonView = view.findViewById(R.id.bt_animated_button_view);

        animatedButtonView.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCardFormSubmit();
            }
        });

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getCardTokenizationError().observe(getViewLifecycleOwner(), new Observer<Exception>() {
            @Override
            public void onChanged(Exception error) {
                if (error instanceof ErrorWithResponse) {
                    setErrors((ErrorWithResponse) error);
                }
                animatedButtonView.showButton();
            }
        });

        boolean showCardCheckbox = !Authorization.isTokenizationKey(dropInRequest.getAuthorization())
                && dropInRequest.isSaveCardCheckBoxShown();

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(configuration.isCvvChallengePresent())
                .postalCodeRequired(configuration.isPostalCodeChallengePresent())
                .cardholderName(dropInRequest.getCardholderNameStatus())
                .saveCardCheckBoxVisible(showCardCheckbox)
                .saveCardCheckBoxChecked(dropInRequest.getDefaultVaultSetting())
                .setup(requireActivity());

        cardForm.maskCardNumber(dropInRequest.shouldMaskCardNumber());
        cardForm.maskCvv(dropInRequest.shouldMaskSecurityCode());
        cardForm.setOnFormFieldFocusedListener(this);

        cardForm.getCardEditText().setText(cardNumber);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cardForm.getExpirationDateEditText().requestFocus();
    }

    void setErrors(ErrorWithResponse errors) {
        BraintreeError formErrors = errors.errorFor("unionPayEnrollment");
        if (formErrors == null) {
            formErrors = errors.errorFor("creditCard");
        }

        if (formErrors != null) {
            if (formErrors.errorFor("expirationYear") != null ||
                    formErrors.errorFor("expirationMonth") != null ||
                    formErrors.errorFor("expirationDate") != null) {
                cardForm.setExpirationError(getContext().getString(R.string.bt_expiration_invalid));
            }

            if (formErrors.errorFor("cvv") != null) {
                cardForm.setCvvError(getContext().getString(R.string.bt_cvv_invalid,
                        getContext().getString(
                                cardForm.getCardEditText().getCardType().getSecurityCodeName())));
            }

            if (formErrors.errorFor("billingAddress") != null) {
                cardForm.setPostalCodeError(getContext().getString(R.string.bt_postal_code_invalid));
            }

            if (formErrors.errorFor("mobileCountryCode") != null) {
                cardForm.setCountryCodeError(getContext().getString(R.string.bt_country_code_invalid));
            }

            if (formErrors.errorFor("mobileNumber") != null) {
                cardForm.setMobileNumberError(getContext().getString(R.string.bt_mobile_number_invalid));
            }
        }
    }

    private void sendDropInEvent(DropInEvent event) {
        getParentFragmentManager().setFragmentResult(DropInEvent.REQUEST_KEY, event.toBundle());
    }

    @Override
    public void onCardFormSubmit() {
        if (cardForm.isValid()) {
            animatedButtonView.showLoading();

            boolean shouldVault = Authorization.fromString(dropInRequest.getAuthorization()) instanceof ClientToken && cardForm.isSaveCardCheckBoxChecked();

            final Card card = new Card();
            card.setCardholderName(cardForm.getCardholderName());
            card.setNumber(cardForm.getCardNumber());
            card.setExpirationMonth(cardForm.getExpirationMonth());
            card.setExpirationYear(cardForm.getExpirationYear());
            card.setCvv(cardForm.getCvv());
            card.setPostalCode(cardForm.getPostalCode());
            card.setShouldValidate(shouldVault);

            sendDropInEvent(DropInEvent.createCardDetailsSubmitEvent(card));
            animatedButtonView.showLoading();

        } else {
            animatedButtonView.showButton();
            cardForm.validate();
        }
    }

    @Override
    public void onCardFormFieldFocused(View view) {
        if (view instanceof CardEditText) {
            String cardNumber = cardForm.getCardNumber();
            sendDropInEvent(DropInEvent.createEditCardNumberEvent(cardNumber));
        }
    }
}

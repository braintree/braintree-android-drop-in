package com.braintreepayments.api;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.OnCardFormFieldFocusedListener;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardForm;

public class CardDetailsFragment extends Fragment implements OnCardFormSubmitListener {

    private CardForm cardForm;
    private AnimatedButtonView animatedButtonView;

    private DropInRequest dropInRequest;
    private CardFormConfiguration configuration;
    private String cardNumber;

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

        boolean showCardCheckbox = !Authorization.isTokenizationKey(dropInRequest.getAuthorization())
                && dropInRequest.isSaveCardCheckBoxShown();

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(configuration.isCvvChallengePresent())
                .postalCodeRequired(configuration.isPostalCodeChallengePresent())
                .cardholderName(dropInRequest.getCardholderNameStatus())
                .saveCardCheckBoxVisible(showCardCheckbox)
                .saveCardCheckBoxChecked(dropInRequest.getDefaultVaultSetting())
                .setup((AppCompatActivity) requireActivity());

        cardForm.maskCardNumber(dropInRequest.shouldMaskCardNumber());
        cardForm.maskCvv(dropInRequest.shouldMaskSecurityCode());

        cardForm.getCardEditText().setText(cardNumber);

        return view;
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

        animatedButtonView.showButton();
    }

    private void sendBraintreeEvent(Parcelable eventResult) {
        Bundle result = new Bundle();
        result.putParcelable("BRAINTREE_RESULT", eventResult);
        getParentFragmentManager().setFragmentResult("BRAINTREE_EVENT", result);
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

            sendBraintreeEvent(new CardDetailsEvent(card));
        } else {
            animatedButtonView.showButton();
            cardForm.validate();
        }
    }
}
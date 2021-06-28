package com.braintreepayments.api;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.view.CardForm;

public class CardDetailsFragment extends Fragment {

    private CardForm cardForm;
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
}
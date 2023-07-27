package com.braintreepayments.api;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.OnCardFormFieldFocusedListener;
import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;

public class CardDetailsFragment extends DropInFragment implements OnCardFormSubmitListener, OnCardFormFieldFocusedListener {

    @VisibleForTesting
    CardForm cardForm;

    @VisibleForTesting
    AnimatedButtonView animatedButtonView;

    private DropInRequest dropInRequest;
    private CardFormConfiguration configuration;
    private String cardNumber;
    private Boolean isTokenizationKeyAuth;

    @VisibleForTesting
    DropInViewModel dropInViewModel;

    BraintreeErrorInspector braintreeErrorInspector = new BraintreeErrorInspector();

    static CardDetailsFragment from(DropInRequest dropInRequest, String cardNumber, Configuration configuration, boolean hasTokenizationKeyAuth) {
        CardFormConfiguration cardFormConfiguration =
                new CardFormConfiguration(configuration.isCvvChallengePresent(), configuration.isPostalCodeChallengePresent());

        Bundle args = new Bundle();
        args.putParcelable("EXTRA_DROP_IN_REQUEST", dropInRequest);
        args.putString("EXTRA_CARD_NUMBER", cardNumber);
        args.putParcelable("EXTRA_CARD_FORM_CONFIGURATION", cardFormConfiguration);
        args.putBoolean("EXTRA_AUTH_IS_TOKENIZATION_KEY", hasTokenizationKeyAuth);

        CardDetailsFragment instance = new CardDetailsFragment();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            dropInRequest = args.getParcelable("EXTRA_DROP_IN_REQUEST");
            configuration = args.getParcelable("EXTRA_CARD_FORM_CONFIGURATION");
            cardNumber = args.getString("EXTRA_CARD_NUMBER");
            isTokenizationKeyAuth = args.getBoolean("EXTRA_AUTH_IS_TOKENIZATION_KEY");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bt_fragment_card_details, container, false);
        cardForm = view.findViewById(R.id.bt_card_form);
        animatedButtonView = view.findViewById(R.id.bt_animated_button_view);

        animatedButtonView.setClickListener(v -> onCardFormSubmit());

        TextView textView = view.findViewById(R.id.bt_privacy_policy);
        String noticeOfCollection = getString(R.string.bt_notice_of_collection);
        NoticeOfCollectionHelper.setNoticeOfCollectionText(textView, noticeOfCollection);

        dropInViewModel = new ViewModelProvider(requireActivity()).get(DropInViewModel.class);

        dropInViewModel.getCardTokenizationError().observe(getViewLifecycleOwner(), error -> {
            if (error instanceof ErrorWithResponse) {
                setErrors((ErrorWithResponse) error);
            }
            animatedButtonView.showButton();
        });

        dropInViewModel.getUserCanceledError().observe(getViewLifecycleOwner(), e -> animatedButtonView.showButton());

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        getParentFragmentManager().popBackStack();
                        remove();
                    }
                });

        Toolbar toolbar = view.findViewById(R.id.bt_toolbar);
        toolbar.setNavigationContentDescription(R.string.bt_back);
        toolbar.setTouchscreenBlocksFocus(false);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        boolean showCardCheckbox = !isTokenizationKeyAuth && dropInRequest.getAllowVaultCardOverride();

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(configuration.isCvvChallengePresent())
                .postalCodeRequired(configuration.isPostalCodeChallengePresent())
                .cardholderName(dropInRequest.getCardholderNameStatus())
                .saveCardCheckBoxVisible(showCardCheckbox)
                .saveCardCheckBoxChecked(dropInRequest.getVaultCardDefaultValue())
                .setup(requireActivity());

        cardForm.maskCardNumber(dropInRequest.getMaskCardNumber());
        cardForm.maskCvv(dropInRequest.getMaskSecurityCode());
        cardForm.setOnFormFieldFocusedListener(this);
        cardForm.setOnCardFormSubmitListener(this);

        cardForm.getCardEditText().setText(cardNumber);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dropInRequest.getCardholderNameStatus() == CardForm.FIELD_DISABLED) {
            cardForm.getExpirationDateEditText().requestFocus();
        } else {
            cardForm.getCardholderNameEditText().requestFocus();
        }
    }

    void setErrors(ErrorWithResponse errors) {
        boolean isDuplicatePaymentMethod = braintreeErrorInspector.isDuplicatePaymentError(errors);
        if (isDuplicatePaymentMethod) {
            cardForm.setCardNumberError(getString(R.string.bt_card_already_exists));
        } else {
            BraintreeError formErrors = errors.errorFor("unionPayEnrollment");
            if (formErrors == null) {
                formErrors = errors.errorFor("creditCard");
            }

            if (formErrors != null) {
                if (formErrors.errorFor("expirationYear") != null ||
                        formErrors.errorFor("expirationMonth") != null ||
                        formErrors.errorFor("expirationDate") != null) {
                    cardForm.setExpirationError(requireContext().getString(R.string.bt_expiration_invalid));
                }

                if (formErrors.errorFor("cvv") != null) {
                    cardForm.setCvvError(requireContext().getString(R.string.bt_cvv_invalid,
                            requireContext().getString(
                                    cardForm.getCardEditText().getCardType().getSecurityCodeName())));
                }

                if (formErrors.errorFor("billingAddress") != null) {
                    cardForm.setPostalCodeError(requireContext().getString(R.string.bt_postal_code_invalid));
                }

                if (formErrors.errorFor("mobileCountryCode") != null) {
                    cardForm.setCountryCodeError(requireContext().getString(R.string.bt_country_code_invalid));
                }

                if (formErrors.errorFor("mobileNumber") != null) {
                    cardForm.setMobileNumberError(requireContext().getString(R.string.bt_mobile_number_invalid));
                }
            }
        }
    }

    @Override
    public void onCardFormSubmit() {
        hideSoftKeyboard();
        if (cardForm.isValid()) {
            animatedButtonView.showLoading();

            boolean shouldVault = !isTokenizationKeyAuth && cardForm.isSaveCardCheckBoxChecked();

            final Card card = new Card();
            card.setCardholderName(cardForm.getCardholderName());
            card.setNumber(cardForm.getCardNumber());
            card.setExpirationMonth(cardForm.getExpirationMonth());
            card.setExpirationYear(cardForm.getExpirationYear());
            card.setCvv(cardForm.getCvv());
            card.setPostalCode(cardForm.getPostalCode());
            card.setShouldValidate(shouldVault);

            sendDropInEvent(DropInEvent.createCardDetailsSubmitEvent(card));
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

    private void hideSoftKeyboard() {
        // Ref: https://stackoverflow.com/a/3553811
        FragmentActivity activity = getActivity();
        if (activity != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View focusedView = activity.getCurrentFocus();
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }
}

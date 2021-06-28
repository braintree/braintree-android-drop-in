package com.braintreepayments.api;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braintreepayments.api.dropin.R;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardForm;

public class AddCardFragment extends Fragment {

    private CardForm cardForm;

    public AddCardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);
        cardForm = view.findViewById(R.id.bt_card_form);

        cardForm.getCardEditText().displayCardTypeIcon(false);

        cardForm.cardRequired(true).setup((AppCompatActivity) requireActivity());
//        mCardForm.setOnCardTypeChangedListener(this);
//        mCardForm.setOnCardFormValidListener(this);
//        mCardForm.setOnCardFormSubmitListener(this);
        return view;
    }
}
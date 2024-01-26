package com.braintreepayments.api

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.dropin.R
import com.braintreepayments.cardform.view.CardForm
import com.google.android.gms.wallet.button.ButtonConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.paypal.android.paymentbuttons.PayPalButton
import com.paypal.android.paymentbuttons.PayPalButtonColor
import com.paypal.android.paymentbuttons.PaymentButtonShape
import org.json.JSONArray

class NewDropInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_drop_in)
        showDropInBottomSheet()
    }

    private fun showDropInBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bt_drop_in_bottom_sheet)
        val cardForm = bottomSheetDialog.findViewById<CardForm>(R.id.card_form)
        cardForm!!.cardRequired(true)
            .maskCardNumber(true)
            .maskCvv(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .postalCodeRequired(true)
            .setup(this)

        val payPalButton = bottomSheetDialog.findViewById<PayPalButton>(R.id.paypal_button)
        payPalButton?.shape = PaymentButtonShape.PILL


        val googlePayButton = bottomSheetDialog.findViewById<PayButton>(R.id.google_pay_button)
        googlePayButton?.initialize(
            ButtonOptions.newBuilder()
                .setAllowedPaymentMethods(GooglePayUtil.allowedPaymentMethods.toString())
                .setButtonType(ButtonConstants.ButtonType.PAY)
                .setButtonTheme(ButtonConstants.ButtonTheme.DARK)
                .build()
        )

        cardForm.visibility = View.GONE


        bottomSheetDialog.show()
    }
}
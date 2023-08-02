package com.braintreepayments.api;

import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class NoticeOfCollectionHelper {

    private NoticeOfCollectionHelper() {}

    static void setNoticeOfCollectionText(TextView textView, String noticeOfCollectionText) {
        String privacyPolicyUrlString = "https://www.paypal.com/us/legalhub/home";
        String html = String.format("<a href=\"%s\">%s</a>", privacyPolicyUrlString, noticeOfCollectionText);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(html));
        }

        textView.setMovementMethod(LinkMovementMethod.getInstance());

        Integer linkBlueColor = Color.parseColor("#2489F6");
        textView.setLinkTextColor(linkBlueColor);
    }
}

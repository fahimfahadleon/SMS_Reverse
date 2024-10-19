package com.horoftech.smsreverse.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.horoftech.smsreverse.viewmodel.ActivityMainViewModel;

public class SmsReceiver extends BroadcastReceiver {
    ActivityMainViewModel model;

    public SmsReceiver(ActivityMainViewModel model) {
        this.model = model;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("sms","received");
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();  // Get the SMS message passed in
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String message = smsMessage.getMessageBody();
                        model.onMessageReceived(message);
                    }
                }
            }
        }
    }
}

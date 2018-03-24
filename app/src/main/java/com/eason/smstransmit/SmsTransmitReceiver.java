package com.eason.smstransmit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eason.sun on 2018/3/24.
 */

public class SmsTransmitReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsTransmitReceiver";
    private SmsTransmitCallback callback;

    public SmsTransmitReceiver (SmsTransmitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + this);
        Bundle bundle = intent.getExtras();
        SmsMessage msg = null;
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                callback.onSms(msg);
            }
        }
    }
}

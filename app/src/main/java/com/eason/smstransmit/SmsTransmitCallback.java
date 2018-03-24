package com.eason.smstransmit;

import android.telephony.SmsMessage;

/**
 * Created by eason.sun on 2018/3/24.
 */

interface SmsTransmitCallback {
    void onSms(SmsMessage msg);
}

package com.eason.smstransmit;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eason.sun on 2018/3/24.
 */

public class SmsTransmitService extends Service {
    private static final String TAG = "SmsTransmitService";
    private SmsTransmitReceiver smsTransmitReceiver;

    private String receiverNumber;
    private String senderNumber;
    private String senderContent;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate executed.");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand executed.");
        if (null != smsTransmitReceiver) {
            return super.onStartCommand(intent, flags, startId);
        }

        senderNumber = intent.getStringExtra(SmsTransmitActivity.SENDER_NUMBER);
        senderContent = intent.getStringExtra(SmsTransmitActivity.SENDER_CONTENT);
        receiverNumber = intent.getStringExtra(SmsTransmitActivity.RECEIVER_NUMBER);

        smsTransmitReceiver = new SmsTransmitReceiver(new SmsTransmitCallback( ) {
            @Override
            public void onSms(SmsMessage msg) {
                Date date = new Date(msg.getTimestampMillis());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                String number = msg.getOriginatingAddress();
                String message = msg.getDisplayMessageBody();
                Log.d(TAG, "onSms：from " + number + ", content：" + message + ", time：" + receiveTime);

                if (number.equals(senderNumber) && message.contains(senderContent)) {
                    transmitMessageTo(receiverNumber, message);
                }
            }
        });
        IntentFilter iff = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsTransmitReceiver, iff);

        Log.d(TAG, "started service: " + smsTransmitReceiver + ", from " + senderNumber + " to " + receiverNumber);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy executed.");
        unregisterReceiver(smsTransmitReceiver);
        smsTransmitReceiver = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind executed.");
        return null;
    }

    public void transmitMessageTo(String phoneNumber, String message){
        Log.d(TAG, "transmitMessageTo: " + phoneNumber + ", with: " + message);
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(phoneNumber, null, message, null, null);
    }
}

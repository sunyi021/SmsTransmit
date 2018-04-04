package com.eason.smstransmit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by eason.sun on 2018/3/24.
 */

public class SmsTransmitService extends Service {
    private static final String TAG = "SmsTransmitService";

    private static String NOTIFICATION_CHANNEL_ID = "8551";
    private static String NOTIFICATION_CHANNEL_NAME = "8551";

    private SmsTransmitReceiver smsTransmitReceiver;

    private String receiverNumber;
    private String senderNumber;
    private List<String[]> senderContentList = new ArrayList<String[]>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate executed.");
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand executed.");
        if (null != smsTransmitReceiver) {
            return super.onStartCommand(intent, flags, startId);
        }

        senderNumber = intent.getStringExtra(SmsTransmitActivity.SENDER_NUMBER);
        receiverNumber = intent.getStringExtra(SmsTransmitActivity.RECEIVER_NUMBER);
        String tempSenderContent = intent.getStringExtra(SmsTransmitActivity.SENDER_CONTENT);
        String[] tempCotents = tempSenderContent.split(";");
        for (String content : tempCotents) {
            senderContentList.add(content.split(","));
        }

        smsTransmitReceiver = new SmsTransmitReceiver(new SmsTransmitCallback( ) {
            @Override
            public void onSms(SmsMessage msg) {
                Date date = new Date(msg.getTimestampMillis());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                String number = msg.getOriginatingAddress();
                String message = msg.getDisplayMessageBody();
                Log.d(TAG, "onSms：from " + number + ", content：" + message + ", time：" + receiveTime);

                if (matchCondition(msg)) {
                    transmitMessageTo(receiverNumber, message);
                }
            }
        });
        IntentFilter iff = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsTransmitReceiver, iff);

        Log.d(TAG, "started service: " + smsTransmitReceiver + ", from " + senderNumber + " to " + receiverNumber);

        Intent newIntent = new Intent(this, SmsTransmitActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, newIntent, 0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);

        Notification notification = builder.build();
        createNotificationChannel();
        startForeground(8551, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy executed.");
        unregisterReceiver(smsTransmitReceiver);
        stopForeground(true);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    private boolean matchCondition(SmsMessage msg){
        String message = msg.getDisplayMessageBody();
        for (String[] contents : senderContentList) {
            boolean result = true;
            for (String content : contents) {
                if (!message.contains(content)) {
                    result = false;
                    break;
                }
            }

            if (result) {
                return true;
            }
        }

        return false;
    }
}

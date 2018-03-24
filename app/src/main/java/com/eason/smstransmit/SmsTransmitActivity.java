package com.eason.smstransmit;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

/**
 * Created by eason.sun on 2018/3/24.
 */

public class SmsTransmitActivity extends Activity {
    private static final String TAG = "SmsTransmitActivity";

    private EditText senderNumber;
    private EditText senderContent;
    private EditText receiverNumber;
    private Button statusButton;
    private Thread checkServiceThread;

    private boolean isRunning = false;

    private final static String SMS_TRANSMIT = "sms_transmit";
    public final static String SENDER_NUMBER = "sender_number";
    public final static String RECEIVER_NUMBER = "receiver_number";
    public final static String SENDER_CONTENT = "sender_content";

    private final static int REQUEST_CODE = 1;

    private final static String[] PERMISSIONS = new String[] {
            Manifest.permission.SEND_SMS,
            Manifest.permission.BROADCAST_SMS,
            Manifest.permission.RECEIVE_SMS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = SmsTransmitActivity.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check permission
        if (checkActivityPermission()) {
            requestPermissions();
        }

        // init members
        senderNumber = (EditText) findViewById(R.id.senderNumber);
        senderContent = (EditText) findViewById(R.id.senderContent);
        receiverNumber = (EditText) findViewById(R.id.receiverNumber);
        statusButton = (Button) findViewById(R.id.statusButton);
        checkServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        boolean temp = isServiceRunning(context, SmsTransmitService.class.getName());
                        if (isRunning != temp) {
                            isRunning = temp;
                            ((SmsTransmitActivity) context).runOnUiThread(new Runnable( ) {
                                @Override
                                public void run() {
                                    updateStatus();
                                }
                            });
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }

                }
            }
        });

        // init actions
        statusButton.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                if (isRunning) {
                    stopSmsTransmitService(context);
                    isRunning = false;
                } else {
                    setSetting(context, SENDER_NUMBER, senderNumber.getText().toString());
                    setSetting(context, SENDER_CONTENT, senderContent.getText().toString());
                    setSetting(context, RECEIVER_NUMBER, receiverNumber.getText().toString());
                    startSmsTransmitService(context);
                    isRunning = true;
                }
                updateStatus();
            }
        });

        // run
//        checkServiceThread.start();
        isRunning = isServiceRunning(context, SmsTransmitService.class.getName());
        updateStatus();
    }

    public void updateStatus() {
        senderNumber.setText(getSetting(this, SENDER_NUMBER));
        senderContent.setText(getSetting(this, SENDER_CONTENT));
        receiverNumber.setText(getSetting(this, RECEIVER_NUMBER));

        if (isRunning) {
            senderNumber.setEnabled(false);
            senderContent.setEnabled(false);
            receiverNumber.setEnabled(false);
            statusButton.setText("停止");
        } else {
            senderNumber.setEnabled(true);
            senderContent.setEnabled(true);
            receiverNumber.setEnabled(true);
            statusButton.setText("开始");
        }
    }

    public void startSmsTransmitService(Context context) {
        Intent intent = new Intent(context, SmsTransmitService.class);
        intent.putExtra(SENDER_NUMBER, getSetting(context, SENDER_NUMBER));
        intent.putExtra(SENDER_CONTENT, getSetting(context, SENDER_CONTENT));
        intent.putExtra(RECEIVER_NUMBER, getSetting(context, RECEIVER_NUMBER));
        super.startService(intent);
    }

    public void stopSmsTransmitService(Context context) {
        Intent intent = new Intent(context, SmsTransmitService.class);
        super.stopService(intent);
    }

    public boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(Integer.MAX_VALUE);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                Log.d(TAG, serviceName + " is running.");
                return true;
            }
        }
        Log.d(TAG, serviceName + " is not running.");
        return false;
    }

    public boolean checkActivityPermission () {
        boolean granted = true;
        for (String permisson : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(SmsTransmitActivity.this, permisson) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }

        return granted;
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(SmsTransmitActivity.this, PERMISSIONS, REQUEST_CODE);
    }

    public static void setSetting (Context context, String key, String value){
        SharedPreferences.Editor note = context.getSharedPreferences(SMS_TRANSMIT, Activity.MODE_PRIVATE).edit();
        note.putString(key, value);
        note.commit();
    }

    public static String getSetting (Context context, String key){
        SharedPreferences read = context.getSharedPreferences(SMS_TRANSMIT, Activity.MODE_PRIVATE);
        return read.getString(key, "");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                for (int i = 0; i < grantResults.length; ++i) {
                    if (PackageManager.PERMISSION_GRANTED != grantResults[i]) {
                        Toast.makeText(this, "你没启动权限: " + permissions[i], Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }
}

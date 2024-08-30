package com.example.eyekeep.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.eyekeep.MainChildActivity;
import com.example.eyekeep.MainParentActivity;
import com.example.eyekeep.R;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.request.RequestFCMToken;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";
    private static final String CHANNEL_ID = "MyChannelId";
    private static final String EMERGENCY_CHANNEL_ID = "EmergencyId";

    @Override
    public void onNewToken(@NonNull String token){
        Log.i("test", token);
        RequestAccessIsActive.checkAccessToken();
        RequestFCMToken.saveFCMToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification().getTitle() == null) {
            Log.e("FCMRequestError", "Message title is null object.");
            return;
        }
        String title = remoteMessage.getNotification().getTitle();

        if (title.equals("EYEKEEP")) {
            // EYEKEEP 등록 요청
            if(remoteMessage.getNotification().getBody() == null || remoteMessage.getData().isEmpty()) {
                Log.e("FCMRequestError", "Message body or data is null object.");
                return;
            }

            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());

            //FCM 메세지의 데이터에서 닉네임과 이메일을 가져옵니다.
            String nickname = remoteMessage.getData().get("nickname");
            String email = remoteMessage.getData().get("email");

            Intent intent = new Intent("com.example.EyeKeep.ACTION_FCM_MESSAGE");
            intent.putExtra("nickname", nickname);
            intent.putExtra("email", email);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        else if (title.equals("EMERGENCY")) {
            // 응급 상황에서 아이캐쳐를 누른 상태
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendEmergencySituation(remoteMessage.getNotification().getBody());
            sendMsg();
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo_eyekeep)
                        .setContentTitle("EYEKEEP")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human-readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendEmergencySituation(String messageBody) {
        Intent intent = new Intent(this, MainParentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, EMERGENCY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo_eyekeep)
                        .setContentTitle("EMERGENCY!!")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(alarmSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(EMERGENCY_CHANNEL_ID,
                    "Channel human-readable title",
                    NotificationManager.IMPORTANCE_HIGH);  // 중요도 변경
            channel.setSound(alarmSoundUri, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    public void sendMsg() {

        try {
            String phonenumber = "1021283305";
            String message = "도와주세요. - EyeKeep에서 전송한 비상 메세지입니다.";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+82" + phonenumber, null, message, null, null);
            Log.i("dsfds", "Sdfs");
        } catch (Exception e) {
            Log.e("SmsManager", "Error occurred: " + e.getMessage());
        }
    }
}

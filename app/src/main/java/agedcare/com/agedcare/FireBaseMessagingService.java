package agedcare.com.agedcare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ibm.watson.developer_cloud.discovery.v1.model.configuration.ContentSelector;
import com.ibm.watson.developer_cloud.personality_insights.v3.model.Content;

import java.util.Map;

/**
 * Created by SANDIP on 7/19/2018.
 */

public class FireBaseMessagingService extends    {
    private static final String TAG = "MyFirebaseMsgService";
    private static int count = 0;

    public void onMessageReceived(RemoteMessage remoteMessage) {
       //Displaying data in log
       //It is optional
     int d = Log.d(TAG, "Notification Message TITLE: " + remoteMessage.getNotification().getTitle());
       Log.d(TAG, "Notification Message BODY: " + remoteMessage.getNotification().getBody());
       Log.d(TAG, "Notification Message DATA: " + remoteMessage.getData().toString());
////Calling method to generate notification
       sendNotification(remoteMessage.getNotification().getTitle(),
              remoteMessage.getNotification().getBody(), remoteMessage.getData());
//    }
    //This method is only generating push notification
    private void sendNotification(String messageTitle, String messageBody, Map<String, String> row) {
        PendingIntent contentIntent = null;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(contentIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(count, notificationBuilder.build());
        count++;
    }
}

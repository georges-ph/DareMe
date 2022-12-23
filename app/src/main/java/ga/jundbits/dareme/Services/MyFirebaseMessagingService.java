package ga.jundbits.dareme.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseAuth.getInstance()
                .addAuthStateListener(firebaseAuth -> {

                    if (firebaseAuth.getCurrentUser() != null)
                        saveToken(token);

                });

    }

    private void saveToken(String token) {

        HelperMethods
                .userDocumentRef(getApplicationContext(), HelperMethods.getCurrentUserID())
                .update("fcm_token", token); // TODO: 23-Dec-22 may need to change how to update document

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction(); // TODO: 23-Dec-22 i think i have to remove this
        long challengeID = Long.parseLong(remoteMessage.getData().get("challenge_id"));

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }

        Intent intent = new Intent(click_action);
        intent.putExtra("challenge_id", challengeID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_dare_me_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify((int) challengeID, notification);

    }

}

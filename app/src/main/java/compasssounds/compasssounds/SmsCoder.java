package compasssounds.compasssounds;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by james on 24/02/15.
 * Change this on branches to deal with the SMS messsage you receive
 */
public class SmsCoder {
    public static void messageReceived(Context context, String message) {
        if (message.contains("LAUNCHTASKS")) {
            ChristophTaskSet.schedule(context);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int icon = R.drawable.ic_launcher;
            CharSequence tickerText = "New Task Scheduled!";
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, tickerText, when);

            CharSequence contentTitle = "AudioCompass";
            CharSequence contentText = "New Task Scheduled!";

            notification.setLatestEventInfo(context, contentTitle, contentText, PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), 0));
            mNotificationManager.notify(123, notification);
        }

    }
}

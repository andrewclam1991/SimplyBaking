package com.andrewclam.bakingapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.andrewclam.bakingapp.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

/**
 * Created by Andrew Chi Heng Lam on 9/18/2017.
 */

public class NotificationUtil {
    private NotificationUtil(){}

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = PACKAGE_NAME + ".media_notification";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = PACKAGE_NAME +
            " media style notification";

    public static void showNotification(Context mContext,
                                        PlaybackStateCompat state,
                                        MediaSessionCompat mMediaSession,
                                        PendingIntent contentPendingIntent)
    {
        NotificationManager mNotificationManager = (NotificationManager)
                mContext.getSystemService(NOTIFICATION_SERVICE);

        assert mNotificationManager != null;

        /* Implement NotificationChannel for Devices running Android O or later*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_DESCRIPTION,
                    NotificationManager.IMPORTANCE_LOW
            );

            // Configure the notification channel.
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,
                NOTIFICATION_CHANNEL_ID);

        // Set the NotificationAction resources
        int icon;
        String play_pause;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = mContext.getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = mContext.getString(R.string.play);
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new NotificationCompat
                .Action(R.drawable.exo_controls_previous, mContext.getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (mContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        builder.setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.notification_text))
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_cupcake)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setOnlyAlertOnce(true) // No subsequent sound or vibration if it exists
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}

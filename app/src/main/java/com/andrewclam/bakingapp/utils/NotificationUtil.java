package com.andrewclam.bakingapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.andrewclam.bakingapp.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.andrewclam.bakingapp.Constants.PACKAGE_NAME;

/**
 * Created by Andrew Chi Heng Lam on 9/18/2017.
 * Contain Method to create and show a notification
 */

public class NotificationUtil {
    private NotificationUtil() {
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = PACKAGE_NAME + ".media_notification";

    /**
     * Create and show a media style notification that can act as the external client,
     * controls media playback through MediaSession callbacks.
     * <p>
     * <p
     * For the bakingApp
     * This notification shows the app name app icon and the current recipe step
     *
     * @param mContext      activity context
     * @param state         the playback state to update the notification
     * @param mMediaSession the media session to fire callback
     * @param contentIntent the pending intent to launch when user clicks the notification
     * @return a NotificationManager for canceling current notification when resource is no
     * longer needed
     */
    public static NotificationManager showNotification(Context mContext,
                                                       PlaybackStateCompat state,
                                                       MediaSessionCompat mMediaSession,
                                                       PendingIntent contentIntent,
                                                       String contentTitle,
                                                       String contentText,
                                                       @Nullable Bitmap largeIcon) {

        NotificationManager mNotificationManager = (NotificationManager)
                mContext.getSystemService(NOTIFICATION_SERVICE);

        assert mNotificationManager != null;

        /* Implement NotificationChannel for Devices running Android O or later*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String notificationChannelDescription = mContext.getString(
                    R.string.media_notification_description);

            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    notificationChannelDescription,
                    NotificationManager.IMPORTANCE_LOW
            );

            // Configure the notification channel.
            notificationChannel.setDescription(notificationChannelDescription);
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

        // Set the LargeIcon resource, if available
        if (largeIcon == null)
        {
            // use the resource default
            largeIcon = BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.ic_cupcake_notification);
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new NotificationCompat
                .Action(R.drawable.exo_controls_previous, mContext.getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (mContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        builder.setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_cupcake)
                .setLargeIcon(largeIcon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setOnlyAlertOnce(true) // No subsequent sound or vibration if it exists
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        return mNotificationManager;
    }
}

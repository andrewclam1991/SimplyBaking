package com.andrewclam.bakingapp.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.andrewclam.bakingapp.WidgetProvider;
import com.andrewclam.bakingapp.utils.NotificationUtil;

import static com.andrewclam.bakingapp.Constants.PACKAGE_NAME;
import static com.andrewclam.bakingapp.utils.NotificationUtil.DOWNLOAD_NOTIFICATION_ID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WidgetIntentService extends IntentService{
    /**
     * Debug Tag
     */
    private static final String TAG = WidgetIntentService.class.getSimpleName();

    /**
     * Actions that the intent service can perform
     */
    private static final String ACTION_UPDATE_WIDGET = PACKAGE_NAME
            + ".services.action.update.widget";

    public WidgetIntentService() {
        super(WidgetIntentService.class.getSimpleName());
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, WidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }
        else {
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action)
            {
                case ACTION_UPDATE_WIDGET:
                    Log.d(TAG, "ACTION_UPDATE_WIDGET received, " +
                            "calling handleActionUpdateWidget()");

                    handleActionUpdateWidget();
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "Unsupported Action");
            }
        }
    }

    /**
     * Handle ActionUpdateWidget in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWidget() {
        // Fetch recipe from the internet
        /* Async Load Recipe Data */
        Log.d(TAG, "handleActionUpdateWidget() called, " +
                "calling FetchRecipeAsyncTask() to get the recipes from the web");

        // Android 0 Requirement
        // Post a brief foreground notification, notifying the user the app is enabling widget
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            startForeground(
                    DOWNLOAD_NOTIFICATION_ID,
                    NotificationUtil.buildDownloadNotification(this)
            );
        }

        Context context = WidgetIntentService.this;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, WidgetProvider.class));

//        // Call the static method in the widget provider class to do widget update
//        WidgetProvider.updateSimplyBakingWidgets(
//                context,
//                appWidgetManager,
//                appWidgetIds);
    }
}

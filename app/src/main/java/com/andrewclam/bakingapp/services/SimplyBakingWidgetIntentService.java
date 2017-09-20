package com.andrewclam.bakingapp.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.andrewclam.bakingapp.SimplyBakingWidgetProvider;
import com.andrewclam.bakingapp.asyncTasks.FetchRecipeAsyncTask;
import com.andrewclam.bakingapp.models.Recipe;
import com.google.android.exoplayer2.ui.BuildConfig;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.DATA_URL;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_LIST;
import static com.andrewclam.bakingapp.Constants.PACKAGE_NAME;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SimplyBakingWidgetIntentService extends IntentService
        implements FetchRecipeAsyncTask.onFetchRecipeActionListener{
    /**
     * Debug Tag
     */
    private static final String TAG = SimplyBakingWidgetIntentService.class.getSimpleName();

    /**
     * Actions that the intent service can perform
     */
    private static final String ACTION_UPDATE_WIDGET = PACKAGE_NAME
            + ".services.action.update.widget";

    public SimplyBakingWidgetIntentService() {
        super(SimplyBakingWidgetIntentService.class.getSimpleName());
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidget(Context context) {

        Log.d(TAG, "startActionUpdateWidget() call received, " +
                "starting service with ACTION_UPDATE_WIDGET");

        Intent intent = new Intent(context, SimplyBakingWidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
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
                    if (BuildConfig.DEBUG) throw new UnsupportedOperationException(
                            "Unsupported Action");
                    break;
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
        new FetchRecipeAsyncTask()
                .setDataURL(DATA_URL)
                .setListener(this)
                .execute();
    }

    @Override
    public void onRecipesReady(ArrayList<Recipe> recipes) {
        // Log callback
        Log.d(TAG,"onRecipesReady() Got recipe ready callback from" +
                "FetchRecipeAsyncTask()");

        Context context = getApplicationContext();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, SimplyBakingWidgetProvider.class));

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_RECIPE_LIST, Parcels.wrap(recipes));

        // Call the static method in the widget provider class to do widget update
        SimplyBakingWidgetProvider.updateSimplyBakingWidgets(
                context,
                appWidgetManager,
                appWidgetIds,
                args);
    }
}

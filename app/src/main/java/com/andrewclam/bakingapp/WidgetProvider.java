package com.andrewclam.bakingapp;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.services.WidgetIntentService;
import com.andrewclam.bakingapp.services.WidgetRemoteViewService;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_LIST;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    /**
     * Debug Tag
     */
    private static final String TAG = WidgetProvider.class.getSimpleName();

    /**
     * updateAppWidget() is where UI bind the data
     *
     * @param context          the application context
     * @param appWidgetManager the widget manager used to update the appwidget by id
     * @param appWidgetId      the particular widget id to update
     * @param args             the arguments and data to populate the widget with, args
     *                         contain the list of recipes
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle args) {

        Log.d(TAG, "constructing the remoteViews");

        Parcelable recipesParcel = args.getParcelable(EXTRA_RECIPE_LIST);

        // Construct the RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_recipe);

        Log.d(TAG, "RemoteViews created with packageName" + context.getPackageName());

        Intent adapterIntent = new Intent(context, WidgetRemoteViewService.class);
        adapterIntent.putExtra(EXTRA_RECIPE_LIST, recipesParcel);
        adapterIntent.setData(Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setRemoteAdapter(R.id.widget_recipe_flipper,adapterIntent);

        // Handle empty recipe
        views.setEmptyView(R.id.widget_recipe_flipper, R.id.widget_empty_view);

        Log.d(TAG, "setRemoteAdapter with adapterIntent");

        appWidgetManager.updateAppWidget(appWidgetId, views);

        Log.d(TAG, "appWidgetManager.updateAppWidget() called for appWidgetid " + appWidgetId);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action, the service takes care of updating
        // the widgets UI. There may be multiple widgets active, so update all of them
        Log.d(TAG, "onUpdate() call received, calling IntentService to start" +
                "action UpdateWidget");
        WidgetIntentService.startActionUpdateWidget(context);
    }

    /**
     * Updates all widget instances given the widget Ids and display information in the bundle
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param args             The bundle arguments for widget views
     */
    public static void updateSimplyBakingWidgets(Context context, AppWidgetManager appWidgetManager,
                                                 int[] appWidgetIds, Bundle args) {
        Log.d(TAG,"updateSimplyBakingWidgets() is called by" +
                "WidgetIntentService, updating all the widgetIds");

        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "called updateAppWidget() with appWidgetId: " + appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId, args);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        WidgetIntentService.startActionUpdateWidget(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


package com.andrewclam.bakingapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.services.SimplyBakingWidgetIntentService;
import com.andrewclam.bakingapp.services.SimplyBakingWidgetRemoteViewService;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_LIST;

/**
 * Implementation of App Widget functionality.
 */
public class SimplyBakingWidgetProvider extends AppWidgetProvider {

    /**
     * Debug Tag
     */
    private static final String TAG = SimplyBakingWidgetProvider.class.getSimpleName();

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

        // Get the recipes from the bundle args
        ArrayList<Recipe> mRecipes = Parcels.unwrap(args.getParcelable(EXTRA_RECIPE_LIST));

        Log.d(TAG, "updateAppWidget() Got mRecipes from the intentService");

        if (mRecipes != null && mRecipes.size() > 0) {

            // Construct the RemoteViews
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_recipe);

            Intent adapterIntent = new Intent(context, SimplyBakingWidgetRemoteViewService.class);
            adapterIntent.putExtra(EXTRA_RECIPE_LIST,Parcels.wrap(mRecipes));
            views.setRemoteAdapter(R.id.widget_recipe_flipper,adapterIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        } else {
            Log.e(TAG, "updateAppWidget() recipes is empty and not available");
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action, the service takes care of updating
        // the widgets UI. There may be multiple widgets active, so update all of them
        Log.d(TAG, "onUpdate() call received, calling IntentService to start" +
                "action UpdateWidget");
        SimplyBakingWidgetIntentService.startActionUpdateWidget(context);
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
                "SimplyBakingWidgetIntentService, updating all the widgetIds");

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, args);
        }
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    @Override
//    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
//                                          int appWidgetId, Bundle newOptions) {
//        SimplyBakingWidgetIntentService.startActionUpdateWidget(context);
//        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
//    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


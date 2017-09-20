package com.andrewclam.bakingapp;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.services.SimplyBakingWidgetIntentService;
import com.google.android.exoplayer2.ui.BuildConfig;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;
import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

/**
 * Implementation of App Widget functionality.
 */
public class SimplyBakingWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle args) {

        // Get the recipes from the bundle args
        ArrayList<Recipe> mRecipes = Parcels.unwrap(args.getParcelable(EXTRA_RECIPE));

        if (BuildConfig.DEBUG) Log.d(TAG,"Got mRecipes from the intentService");

        // UI Text Strings
        String recipeNameStr = mRecipes.get(0).getName();
        String servingStr = context.getString(R.string.serving,mRecipes.get(0).getServings());

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simply_baking_widget);
        views.setTextViewText(R.id.recipe_name_tv, recipeNameStr);
        views.setTextViewText(R.id.recipe_servings_tv, servingStr);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        // Start the intent service update widget action, the service takes care of updating
        // the widgets UI
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
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, args);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        SimplyBakingWidgetIntentService.startActionUpdateWidget(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        SimplyBakingWidgetIntentService.startActionUpdateWidget(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


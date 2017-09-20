package com.andrewclam.bakingapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.models.Recipe;

import org.parceler.Parcels;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;

/**
 * Implementation of App Widget functionality.
 */
public class SimplyBakingWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle args) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);

        // Get the recipes from the bundle args
        Recipe recipe = Parcels.unwrap(args.getParcelable(EXTRA_RECIPE));

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simply_baking_widget);
        views.setTextViewText(R.id.recipe_name_tv, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Bundle args = new Bundle();
            updateAppWidget(context, appWidgetManager, appWidgetId, args);
        }
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


package com.andrewclam.bakingapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.services.WidgetRemoteViewService;

import org.parceler.Parcels;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    /**
     * Debug Tag
     */
    private static final String TAG = WidgetProvider.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Todo implement a way to find if the widget is fresh (initialized) or not
        boolean isFresh = true;

        // 1) Initial No Recipe Tracking
        // Construct the RemoteViews
        if (isFresh) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_recipe_fresh_start);

            // Create a pending intent to launch the mainActivity to start a recipe
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_start_recipe, pendingIntent);

            // Call appWidgetManger to update the particularAppWidgetId
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        // 2) Already Has Recipe, Load the steps
        else {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_recipe_tracking);

            // TODO get the recipe from the arg
            Recipe recipe = new Recipe();
            recipe.setName("Example");

            // Recipe Name On Click launches the StepsListActivity
            // Create pending intent to launch the StepListActivity
            Intent intent = new Intent(context, StepListActivity.class);
            intent.putExtra(EXTRA_RECIPE, Parcels.wrap(recipe));

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            // Use RemoteViewService to bind ingredients listView with listItem remoteViews
            // Intent to the WidgetRemoteViewService -> returns a factory with listItem remoteViews
            Intent serviceAdapterIntent = new Intent(context, WidgetRemoteViewService.class);

            // UI Biding
            views.setTextViewText(R.id.widget_recipe_name_tv, recipe.getName());
            views.setTextViewText(R.id.widget_recipe_servings_tv, context.getString(
                    R.string.serving,recipe.getServings()));
            views.setOnClickPendingIntent(R.id.widget_recipe_name_tv, pendingIntent);
            views.setRemoteAdapter(R.id.widget_ingredient_list_lv,serviceAdapterIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action, the service takes care of updating
        // the widgets UI. There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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


    /**
     * A place just to store code without commenting it out so it looks nice
     * @param context
     * @param views
     */
    private void codeholder(Context context, RemoteViews views)
    {
        // Setup the
        Intent adapterIntent = new Intent(context, WidgetRemoteViewService.class);
        views.setRemoteAdapter(R.id.widget_ingredient_list_lv,adapterIntent);

        // Handle empty recipe
        views.setEmptyView(R.id.widget_start_recipe, R.id.widget_empty_view);
    }
}


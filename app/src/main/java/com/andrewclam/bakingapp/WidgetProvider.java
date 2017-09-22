/*
 * Copyright (c) 2017 Andrew Chi Heng Lam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget_recipe_small);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        boolean done = false;
        if (done) {
            views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_recipe_large);

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
                    R.string.serving, recipe.getServings()));
            views.setOnClickPendingIntent(R.id.widget_recipe_name_tv, pendingIntent);
            views.setRemoteAdapter(R.id.widget_ingredient_list_lv, serviceAdapterIntent);

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
    }
}


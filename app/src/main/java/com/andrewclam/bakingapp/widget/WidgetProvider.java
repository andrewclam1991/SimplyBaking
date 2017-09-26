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

package com.andrewclam.bakingapp.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.RecipeDetailActivity;
import com.squareup.picasso.Picasso;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_ID;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    /**
     * Debug Tag
     */
    private static final String TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action, the service takes care of updating
        // the widgets UI. There may be multiple widgets active, so update each one

        for (int appWidgetId : appWidgetIds) {
            // Use intentService to update each appWidget by their id, query their respective recipe
            // when complete, the service calls this provider's updateAppWidget()
            WidgetIntentService.startActionUpdateWidget(context, appWidgetId);
        }
    }

    /**
     * updateAppWidget() method is called after WidgetIntentService.startActionUpdateWidget()
     * completes fetching the corresponding recipe data given the appWidgetId
     * {@link WidgetIntentService}
     *
     * @param context          application context
     * @param recipeId         the id of the app widget id's corresponding recipe
     * @param recipeName       the corresponding recipe's name
     * @param servings         the corresponding recipe's servings
     * @param imageUrl         the corresponding recipe's url to the image
     * @param appWidgetManager the current appWidgetManager instance
     * @param appWidgetId      the specific appWidgetId the intent service queried
     */
    static void updateAppWidget(Context context, Long recipeId, String recipeName, Long servings,
                                String imageUrl, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = createAppWidgetRemoteViews(context, recipeId, recipeName, servings,
                imageUrl,
                appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                R.id.widget_recipe_ingredients_list);
    }

    /**
     * createAppWidgetRemoteViews() Creates and returns the RemoteViews to be
     * displayed in the app widget, this method is called by the updateAppWidget()
     *
     * @param context      application context
     * @param recipeId     the id of the app widget id's corresponding recipe
     * @param recipeName   the corresponding recipe's name
     * @param servings     the corresponding recipe's servings
     * @param imageUrl     the corresponding recipe's url to the image
     * @param mAppWidgetId the specific appWidgetId the intent service queried
     * @return The RemoteViews for our app widget
     */
    private static RemoteViews createAppWidgetRemoteViews(Context context,
                                                          Long recipeId,
                                                          String recipeName,
                                                          Long servings,
                                                          String imageUrl,
                                                          int mAppWidgetId) {

        // Data - Create the pending intent, as the widget act as the shortcut to the recipe
        // the intent should launch the stepsListActivity by default with the recipe id
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                mAppWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // UI - Find and Bind Views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_recipe);

        views.setOnClickPendingIntent(R.id.widget_small_root_view, pendingIntent);
        views.setTextViewText(R.id.widget_small_recipe_name, recipeName);
        views.setTextViewText(R.id.widget_recipe_serving, context.getString(R.string.serving,
                servings));

        // UI - Set intent to act as the remoteView intent
        Intent remoteViewIntent = new Intent(context, WidgetRemoteViewService.class);

        /*
         remoteViewIntent.putExtra(EXTRA_RECIPE_ID, recipeId);

        // BUG: When multiple widgets are enabled, data is duplicated
        // FIX: Instead of putExtra, must setData with Uri, see for solution
        // https://stackoverflow.com/questions/11350287/ongetviewfactory-only-called-once-for
        -multiple-widgets//
        */

        remoteViewIntent.setData(Uri.fromParts("content", String.valueOf(recipeId), null));
        Log.d(TAG, "RecipeId of the ingredients passed: " + recipeId);
        views.setRemoteAdapter(R.id.widget_recipe_ingredients_list, remoteViewIntent);

        // UI - Image Icon Check if recipe has an image for icon
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Use picasso to load the image into the remoteView
            Picasso.with(context).load(imageUrl).into(
                    views,
                    R.id.widget_icon,
                    new int[]{mAppWidgetId}
            );
        } else {
            // default to cupcake icon
            views.setImageViewResource(R.id.widget_icon, R.drawable.ic_cupcake_full_color);
        }

        return views;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Log.d(TAG,"onAppWidgetOptionsChanged() called with the appWidgetId: " + appWidgetId);
        WidgetIntentService.startActionUpdateWidget(context,appWidgetId);
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

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Enter relevant functionality for when the a widget (or a group of widgets) is deleted
        // TODO delete the recipe-widgetid in the appWidgetId table

        super.onDeleted(context, appWidgetIds);
    }
}


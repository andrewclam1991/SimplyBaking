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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.widget.RemoteViews;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.RecipeDetailActivity;
import com.andrewclam.bakingapp.data.RecipeDbContract;
import com.andrewclam.bakingapp.models.Ingredient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_ID;
import static com.andrewclam.bakingapp.data.RecipeDbContract.AppWidgetIdEntry.CONTENT_URI_APP_WIDGET_ID;

/**
 * Created by Andrew Chi Heng Lam on 9/23/2017.
 */

public class WidgetUtils {

    /**
     * Private Constructor prevent instantiation
     */
    private WidgetUtils(){}


    /**
     * App Widget Configuration
     * <p>
     * createAppWidgetResult() creates the AppWidget user selecting a recipe from the list
     * new widget serves as the shortcut to the particular selected recipe.
     *
     * @param recipeId the id of the recipe object that the user clicked
     */

    public static Intent createAppWidgetResult(Context context, int mAppWidgetId,
                                                            long recipeId) {
        // If the app is started for AppWidget Configuration, upon user click the recipe
        // user is selecting the recipe to be displayed as the widget on the home screen
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Data - The vars that the widget needs
        String recipeName;
        Long servings;
        String imageUrl;
        ArrayList<Ingredient> ingredients;

        // Data - Query the recipe id from the database
        Uri recipeUriWithId = RecipeDbContract.buildRecipeUriWithId(recipeId);
        Cursor recipeCursor = context.getContentResolver().query(
                recipeUriWithId,
                null,
                null,
                null,
                null);

        if (recipeCursor != null) {
            recipeCursor.moveToNext();

            // Data - Get the recipe data from the cursor
            int recipeNameColIndex =
                    recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_NAME);
            int recipeServingColIndex =
                    recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_SERVINGS);
            int recipeImageUrlColIndex =
                    recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_IMAGE_URL);

            recipeName = recipeCursor.getString(recipeNameColIndex);
            servings = recipeCursor.getLong(recipeServingColIndex);
            imageUrl = recipeCursor.getString(recipeImageUrlColIndex);

            // Close the cursor
            recipeCursor.close();
        }else
        {
            // Error
            throw new SQLException("Cursor is null, can't find the recipe in the database");
        }

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
        views.setTextViewText(R.id.widget_recipe_serving, context.getString(R.string.serving,servings));

        // UI - Set intent to act as the remoteView intent
        Intent remoteViewIntent = new Intent(context, WidgetRemoteViewService.class);
        remoteViewIntent.putExtra(EXTRA_RECIPE_ID,recipeId);
        views.setRemoteAdapter(R.id.widget_recipe_ingredients_list,remoteViewIntent);

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
            views.setImageViewResource(R.id.widget_icon, R.drawable.ic_cupcake);
        }

        /* STORE APP WIDGET ID */
        // update appWidgetIdTable with the particular recipe in the database with the appWidgetId
        ContentValues cv = new ContentValues();
        cv.put(RecipeDbContract.AppWidgetIdEntry.COLUMN_APP_WIDGET_RECIPE_KEY, recipeId);
        cv.put(RecipeDbContract.AppWidgetIdEntry.COLUMN_APP_WIDGET_UID, mAppWidgetId);
        context.getContentResolver().insert(CONTENT_URI_APP_WIDGET_ID, cv);

        // Widget Update - Use appWidgetManager to update/create the particular widget by id
        appWidgetManager.updateAppWidget(mAppWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId,R.id.widget_recipe_ingredients_list);

        // Send out an intent with the resulting appWidgetId, with the result OK
        Intent resultValueIntent = new Intent();
        resultValueIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

        // return the resultValue intent
        return resultValueIntent;
    }

    public static RemoteViews updateAppWidget()
    {
        return null;
    }
}

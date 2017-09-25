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

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.andrewclam.bakingapp.data.RecipeDbContract;
import com.andrewclam.bakingapp.utils.NotificationUtil;

import static com.andrewclam.bakingapp.Constants.EXTRA_APP_WIDGET_ID;
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
    public static void startActionUpdateWidget(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        intent.putExtra(EXTRA_APP_WIDGET_ID,appWidgetId);

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
                    int appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, -1);
                    if (appWidgetId == -1)
                        throw new IllegalArgumentException(
                                "Invalid App Widget Id passed in WidgetIntentService, " +
                                        "ACTION_UPDATE_WIDGET"
                        );

                    handleActionUpdateWidget(appWidgetId);
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
    private void handleActionUpdateWidget(int appWidgetId) {
        // Fetch recipe from the internet
        /* Async Load Recipe Data */
        Log.d(TAG, "handleActionUpdateWidget() called with appWidgetId " + appWidgetId);

        // Android 0 + Requirement
        // Post a brief foreground work-in-progress notification, notifying the user the app
        // is enabling or updating a widget
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            startForeground(
                    DOWNLOAD_NOTIFICATION_ID,
                    NotificationUtil.buildDownloadNotification(this)
            );
        }

        // Data - Query the recipe id from the database, build the uri for querying
        // recipe with the corresponding appWidgetId
        Uri recipeUriWithAppWidgetId = RecipeDbContract.buildRecipeUriWithAppWidgetId(appWidgetId);

        Cursor mCursor = this.getContentResolver().query(
                recipeUriWithAppWidgetId,
                null,
                null,
                null,
                null);

        if (mCursor == null || mCursor.getCount() == 0){
            Log.e(TAG,"handleActionUpdateWidget() got a null or empty cursor is null, " +
                    "can't find a recipe corresponding with the app widget id: " + appWidgetId +
                    " in the database");
            return;
        }

        try {
            // Data - Get the recipe data from the cursor
            mCursor.moveToNext();

            int uidColIndex =
                    mCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_UID);
            int nameColIndex =
                    mCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_NAME);
            int servingColIndex =
                    mCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_SERVINGS);
            int imageUrlColIndex =
                    mCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_IMAGE_URL);

            Long recipeId = mCursor.getLong(uidColIndex);
            String recipeName = mCursor.getString(nameColIndex);
            Long servings = mCursor.getLong(servingColIndex);
            String imageUrl = mCursor.getString(imageUrlColIndex);

            // Close the cursor after use
            mCursor.close();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            WidgetProvider.updateAppWidget(
                    this,
                    recipeId,
                    recipeName,
                    servings,
                    imageUrl,
                    appWidgetManager,
                    appWidgetId);

            Log.d(TAG,"handleActionUpdateWidget() successfully fetched recipe with appWidgetId: "
                    + appWidgetId +
                    " called WidgetProvider.updateAppWidget() to continue the update process");

        }catch (Exception e)
        {
            Log.e(TAG,"handleActionUpdateWidget() error occurred " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}

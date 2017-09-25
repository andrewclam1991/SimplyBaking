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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.data.RecipeDbContract;

import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.CONTENT_URI_INGREDIENT;

/**
 * Created by Andrew Chi Heng Lam on 9/20/2017.
 * This remote view service will serve as the data adapter and create remote views for
 * the calling remote view
 */

public class WidgetRemoteViewService extends RemoteViewsService {
    /**
     * Debug Tag
     */
    private static final String TAG = WidgetRemoteViewService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // long mRecipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1L);

        long mRecipeId = Long.valueOf(intent.getData().getSchemeSpecificPart());

        if (mRecipeId == -1) throw new IllegalArgumentException("Invalid Recipe id");
        Log.d(TAG, "onGetViewFactory() call received with mRecipeId " + mRecipeId);
        return new WidgetRemoteViewsFactory(this.getApplicationContext(), mRecipeId);
    }
}

/**
 * A RemoteViewsFactory class implementation to produce individual item RemoteViews
 * for a collection view (Ex. ListView, StackView..etc)
 * <p>
 * (Like an Adapter populating each item ViewHolder for RecyclerView, ListView etc)
 */
class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    /**
     * Debug Tag
     */
    private final static String TAG = WidgetRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private long mRecipeId;
    private Cursor mCursor;

    WidgetRemoteViewsFactory(Context mContext, Long mRecipeId) {
        this.mContext = mContext;
        this.mRecipeId = mRecipeId;
        Log.d(TAG, "WidgetRemoteViewsFactory() constructed with recipeId: " + mRecipeId);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Implement to load cursor if using contentResolver and a SQLite database to store
        // recipe offline
        Log.d(TAG, "onDataSetChanged() called with mRecipeId " + mRecipeId);
        if (mCursor != null) mCursor.close();
        mCursor = mContext.getContentResolver().query(
                CONTENT_URI_INGREDIENT,
                new String[]{
                        RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_MEASURE,
                        RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_NAME,
                        RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_QUANTITY
                },
                COLUMN_INGREDIENT_RECIPE_KEY + "=?",
                new String[]{String.valueOf(mRecipeId)},
                null);

        if (mCursor == null || mCursor.getCount() == 0)
        {
            Log.e(TAG, "mCursor of the ingredient is null or empty with mRecipeId: " + mRecipeId);
        }
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * This method acts like the onBindViewHolder method in an Adapter
     *
     * @param position The current position of the item in the AdapterViewFlipper to
     *                 be displayed
     * @return The RemoteViews object to display for the provided position
     */
    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG,"getViewAt() called with position " + position);

        // Get the ingredient at the adapter position
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);

        int ingredientMeasureIndex =
                mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_MEASURE);
        int ingredientNameIndex =
                mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_NAME);
        int ingredientQuantityIndex =
                mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_QUANTITY);

        String name = mCursor.getString(ingredientNameIndex);
        String measure = mCursor.getString(ingredientMeasureIndex);
        double quantity = mCursor.getDouble(ingredientQuantityIndex);

        // Create the remote view from the list item layout file
        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.ingredient_list_item);

        // UI - Bind the data to the views
        views.setTextViewText(R.id.ingredient_name_tv, name);
        views.setTextViewText(R.id.ingredient_quantity_tv, String.valueOf(quantity));
        views.setTextViewText(R.id.ingredient_measure_tv, measure);
        views.setViewVisibility(R.id.list_divider, View.GONE);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // One type of view
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}

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
import com.andrewclam.bakingapp.models.Ingredient;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_ID;
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
        Log.d(TAG, "onGetViewFactory() call received");
        long mRecipeId = intent.getLongExtra(EXTRA_RECIPE_ID,-1L);
        if (mRecipeId == -1) throw new IllegalArgumentException("Recipe id is -1");
        return new CollectionViewsRemoteViewFactory(this.getApplicationContext(),mRecipeId);
    }
}

/**
 * A RemoteViewsFactory class implementation to produce individual item RemoteViews
 * for a collection view (Ex. ListView, StackView..etc)
 *
 * (Like an Adapter populating each item ViewHolder for RecyclerView, ListView etc)
 */
class CollectionViewsRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory{
    /**
     * Debug Tag
     */
    private final static String TAG = CollectionViewsRemoteViewFactory.class.getSimpleName();

    private final Context mContext;
    private long mRecipeId;
    private ArrayList<Ingredient> mIngredients;
    private Cursor mCursor;

    CollectionViewsRemoteViewFactory(Context mContext, Long mRecipeId) {
        this.mContext = mContext;
        this.mRecipeId = mRecipeId;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Implement to load cursor if using contentResolver and a SQLite database to store
        // recipe offline
        Log.d(TAG, "onDataSetChanged() called with intent");

        // Ingredient Child Table
        // Get its cursor, select only rows with the key that equals to the id
        try {
            if (mCursor != null) mCursor.close();
            mCursor = mContext.getContentResolver().query(
                    CONTENT_URI_INGREDIENT,
                    null,
                    COLUMN_INGREDIENT_RECIPE_KEY + "=?",
                    new String[]{String.valueOf(mRecipeId)},
                    null);

            mIngredients = new ArrayList<>();

            // Parse the ingredients
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    Ingredient ingredient = new Ingredient();

                    int ingredientUidIndex =
                            mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_UID);
                    int ingredientMeasureIndex =
                            mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_MEASURE);
                    int ingredientNameIndex =
                            mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_NAME);
                    int ingredientQuantityIndex =
                            mCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_QUANTITY);

                    ingredient.setUid(mCursor.getString(ingredientUidIndex));
                    ingredient.setIngredientName(mCursor.getString(ingredientNameIndex));
                    ingredient.setMeasure(mCursor.getString(ingredientMeasureIndex));
                    ingredient.setQuantity(mCursor.getDouble(ingredientQuantityIndex));

                    mIngredients.add(ingredient);
                }
            }
        }catch (Exception e)
        {
            Log.e(TAG,e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) mCursor.close();
    }

    @Override
    public int getCount() {
        if (mIngredients != null) return mIngredients.size();
        return 0;
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
        // Get the ingredient at the adapter position
        Ingredient ingredient = mIngredients.get(position);

        if (ingredient == null) return null;

        // Create the remote view from the list item layout file
        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.ingredient_list_item);

        // UI - Bind the data to the views
        views.setTextViewText(R.id.ingredient_name_tv,ingredient.getIngredientName());
        views.setTextViewText(R.id.ingredient_quantity_tv,String.valueOf(ingredient.getQuantity()));
        views.setTextViewText(R.id.ingredient_measure_tv,ingredient.getMeasure());
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

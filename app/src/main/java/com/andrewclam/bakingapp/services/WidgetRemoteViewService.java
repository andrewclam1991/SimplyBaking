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

package com.andrewclam.bakingapp.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.models.Ingredient;

import java.util.ArrayList;

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
        return new CollectionViewsRemoteViewFactory(this.getApplicationContext());
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
    private ArrayList<Ingredient> mIngredients;

    CollectionViewsRemoteViewFactory(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Implement to load cursor if using contentResolver and a SQLite database to store
        // recipe offline
        // FIXME ! get the list of ingredients using the content provider
        Log.d(TAG, "onDataSetChanged() called with intent");
    }

    @Override
    public void onDestroy() {
        mIngredients.clear();
    }

    @Override
    public int getCount() {
        return mIngredients.size();
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
        return false;
    }


}

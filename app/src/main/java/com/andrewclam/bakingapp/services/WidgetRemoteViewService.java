package com.andrewclam.bakingapp.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.asyncTasks.FetchRecipeAsyncTask;
import com.andrewclam.bakingapp.models.Recipe;

import java.util.ArrayList;
import java.util.List;

import static com.andrewclam.bakingapp.Constants.DATA_URL;

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
        return new ViewFlipperRemoteViewFactory(this.getApplicationContext());
    }
}

/**
 * A RemoteViewsFactory class implementation to produce individual item RemoteViews
 * for the collection view
 *
 * (Like an Adapter populating each item ViewHolder for RecyclerView, ListView etc)
 */
class ViewFlipperRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory,
        FetchRecipeAsyncTask.onFetchRecipeActionListener{
    /**
     * Debug Tag
     */
    private final static String TAG = ViewFlipperRemoteViewFactory.class.getSimpleName();

    /**
     * Pending Intent RC for the button in each recipe widget view
     */
    private final static int WIDGET_VIEWFLIPPER_PENDING_INTENT_RC = 4321;

    private final Context mContext;
    private List<Recipe> mRecipes;

    ViewFlipperRemoteViewFactory(Context mContext) {
        this.mContext = mContext;
        this.mRecipes = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Implement to load cursor if using contentResolver and a SQLite database to store
        // recipe offline
        Log.d(TAG, "onDataSetChanged() called with intent");
        new FetchRecipeAsyncTask().setDataURL(DATA_URL).setListener(this).execute();
    }

    @Override
    public void onDestroy() {
        mRecipes.clear();
    }

    @Override
    public int getCount() {
        return mRecipes.size();
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
        if (mRecipes == null || mRecipes.size() == 0) return null;

        Log.d(TAG,"getViewAt() call back received at position " + position);

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_recipe_item);

        // Data - Get the recipe at the position
        Recipe recipe = mRecipes.get(position);

        // Data - Get the recipe name
        String recipeNameStr = recipe.getName();
        String servingsStr = mContext.getString(R.string.serving, recipe.getServings());

        // Data - Create the pending intent for the button to launch to see full recipe
//        Intent intent = new Intent(mContext, StepListActivity.class);
//        intent.putExtra(EXTRA_RECIPE, Parcels.wrap(recipe));
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                mContext,
//                WIDGET_VIEWFLIPPER_PENDING_INTENT_RC,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);

        // UI - Bind the data to the views
        views.setTextViewText(R.id.widget_recipe_name_tv,recipeNameStr);
        views.setTextViewText(R.id.widget_recipe_servings_tv,servingsStr);
//        views.setOnClickPendingIntent(R.id.widget_see_directions_btn,pendingIntent);

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

    @Override
    public void onRecipesReady(ArrayList<Recipe> recipes) {
        mRecipes = recipes;
    }
}

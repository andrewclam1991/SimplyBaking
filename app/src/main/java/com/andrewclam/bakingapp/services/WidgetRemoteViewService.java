package com.andrewclam.bakingapp.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.models.Recipe;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_LIST;

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
        Log.d(TAG, "onGetViewFactory() called with intent");
        return new ViewFlipperRemoteViewFactory(this.getApplicationContext(),intent);
    }
}

/**
 * A RemoteViewsFactory class implementation to produce individual item RemoteViews
 * for the collection view
 *
 * (Like an Adapter populating each item ViewHolder for RecyclerView, ListView etc)
 */
class ViewFlipperRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    /**
     * Debug Tag
     */
    private final static String TAG = ViewFlipperRemoteViewFactory.class.getSimpleName();

    /**
     * Pending Intent RC for the button in each recipe widget view
     */
    private final static int WIDGET_VIEWFLIPPER_PENDING_INTENT_RC = 4321;

    private final Context mContext;
    private ArrayList<Recipe> mRecipes;

    ViewFlipperRemoteViewFactory(Context mContext, Intent intent) {
        if (intent.hasExtra(EXTRA_RECIPE_LIST))
        {
            this.mRecipes = Parcels.unwrap(intent.getParcelableExtra(EXTRA_RECIPE_LIST));
            this.mContext = mContext;
        }else
        {
            throw new RuntimeException("Stub!");
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Implement to load cursor if using contentResolver and a SQLite database to store
        // recipe offline
        Log.d(TAG, "onDataSetChanged() called with intent");
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
        return true;
    }
}
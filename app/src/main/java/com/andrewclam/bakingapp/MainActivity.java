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

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.andrewclam.bakingapp.adapters.RecipeRecyclerViewAdapter;
import com.andrewclam.bakingapp.asyncTasks.DbMultiTableParsingAsyncTask;
import com.andrewclam.bakingapp.asyncTasks.FetchRecipeAsyncTask;
import com.andrewclam.bakingapp.espresso.SimpleIdlingResource;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.services.SyncDbIntentService;
import com.andrewclam.bakingapp.utils.NetworkUtils;
import com.andrewclam.bakingapp.widget.WidgetUtils;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.ACTION_APPWIDGET_CONFIG;
import static com.andrewclam.bakingapp.Constants.ACTION_CONNECTIVITY_CHANGE;
import static com.andrewclam.bakingapp.Constants.DATA_URL;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;
import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.CONTENT_URI_RECIPE;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        FetchRecipeAsyncTask.onFetchRecipeActionListener,
        RecipeRecyclerViewAdapter.OnRecipeItemClickedListener {

    /**
     * Debug Tag
     */
    private final static String TAG = MainActivity.class.getSimpleName();

    /**
     * RecyclerView to show the list of recipes
     */
    private RecyclerView mRecipeRv;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecipeRecyclerViewAdapter mAdapter;

    /**
     * Progress bar to show user the data is loading
     */
    private ProgressBar mProgressBar;

    /**
     * App Widget Configuration
     */
    private boolean mStartedForAppWidgetConfig;
    private int mAppWidgetId;

    /**
     * Broadcast Receiver to listen to Network State Change broadcasts
     */
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private Snackbar mNetworkStateSnackBar;

    /**
     * LoaderManager Instance for Loading offline db data
     * This ID will be used to identify the Loader responsible for loading our offline database. In
     * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
     * We will still use this ID to initialize the loader and create the loader for best practice.
    */
    private static final int RECIPE_LOADER_ID = 8888;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Recipes List Setup */
        mRecipeRv = findViewById(R.id.recipe_list_rv);
        mAdapter = new RecipeRecyclerViewAdapter(this, this);
        mRecipeRv.setAdapter(mAdapter);

        /* Determine device's orientation and adjust layout type accordingly */
        View rootView = findViewById(R.id.recipe_list_container_land);
        if (rootView == null) {
            // The device is not in landscape mode, reference the single view
            // layout the recipe list in a linear layout
            rootView = findViewById(R.id.recipe_list_container);
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        } else {
            // The device is in landscape mode, use grid layout
            mLayoutManager = new GridLayoutManager(this, 3);
        }
        mRecipeRv.setLayoutManager(mLayoutManager);

        /* Create a Snack bar to show network disconnected (if it is) */
        mNetworkStateSnackBar = Snackbar.make(
                rootView,
                getString(R.string.network_unavailable),
                Snackbar.LENGTH_INDEFINITE);

        /* Loading Progress Bar - Visible*/
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Get the IdlingResource instance
        getIdlingResource();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* Check if started for and Setup AppWidget Configuration */
        initAppWidgetConfiguration();

        /* Monitor Network State Connection Changes */
        // Once the network state is determined, the network call/database operation would
        // be executed in the broadcast receiver onReceive callback

        // Create the networkChangeReceiver
        mNetworkChangeReceiver = new NetworkChangeReceiver();

        // Register the receiver with this Activity
        registerReceiver(mNetworkChangeReceiver, new IntentFilter(ACTION_CONNECTIVITY_CHANGE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup, unregister the dynamic broadcast receiver with context
        unregisterReceiver(mNetworkChangeReceiver);
        mNetworkChangeReceiver = null;
    }

    /**
     * BroadcastReceiver to listen to system's network state change broadcast, and handle
     * onReceive of such intent
     */
    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_CONNECTIVITY_CHANGE)) {

                // Network State Changed, Check the network state
                boolean isConnected = NetworkUtils.getNetworkState(context);

                if (isConnected) {
                    /* Connected  */
                    // Async Load The Latest Recipe Data
                    new FetchRecipeAsyncTask()
                            .setDataURL(DATA_URL)
                            .setListener(MainActivity.this)
                            .setIdlingResource(mIdlingResource)
                            .execute();

                     /* Show network is now connected from being connected*/
                     // dismiss the disconnected snack bar
                    if (mNetworkStateSnackBar != null) mNetworkStateSnackBar.dismiss();

                } else {
                    /* Disconnected */
                    // Load cached data from client database
                    getSupportLoaderManager().restartLoader(RECIPE_LOADER_ID, null,
                            MainActivity.this);

                    /* Show network is now disconnected from being connected*/
                    if (mNetworkStateSnackBar != null) mNetworkStateSnackBar.show();
                }
            }
        }
    }

    /**
     * Callback from the FetchRecipeAsyncTask with a list of recipe ready to populate the
     * recycler view.
     *
     * @param recipes the list of recipes parsed from the data source
     */
    @Override
    public void onRecipesReady(ArrayList<Recipe> recipes) {
        // Call intent service to update the database with the latest recipes
        SyncDbIntentService.syncRecipes(this, recipes);

        mAdapter.setRecipeData(recipes);
        mAdapter.notifyDataSetChanged();

        /* Loading Progress Bar - Data Loaded, Be GONE */
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Callback from the RecipeRecyclerViewAdapter when the user clicks a recipe from the list.
     *
     * @param recipe the user's clicked recipe
     */
    @Override
    public void onRecipeClicked(Recipe recipe) {
        if (mStartedForAppWidgetConfig) {
            // 1) Call create AppWidget to populate the RemoteView and create the widget with
            // AppWidgetManager
            Intent resultValue = WidgetUtils.createAppWidgetResult(
                    MainActivity.this,mAppWidgetId,recipe.getUid());
            setResult(RESULT_OK, resultValue);

            // Finish the configuration activity once the result is set
            finish();

        } else {
            // 2) Otherwise, should just launch the detailActivity showing the recipe's full info
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(EXTRA_RECIPE, Parcels.wrap(recipe));
            startActivity(intent);
        }
    }

    /**
     * App Widget Configuration
     * <p>
     * initAppWidgetConfiguration() gets the intent that started this Activity and initialize
     * the vars required for AppWidget configuration
     */
    private void initAppWidgetConfiguration() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (action != null && extras != null && action.equals(ACTION_APPWIDGET_CONFIG)) {
            // Set the flag to true, this indicate the activity was started
            // with action for the companion app widget configuration
            mStartedForAppWidgetConfig = true;

            // Get the App Widget Id, this is used for appWidgetManager to update
            // a particular id
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            // Set title to select recipe
            setTitle(getString(R.string.app_widget_select_recipe_title));
        } else {
            // Set the flag to false, activity was started normally
            mStartedForAppWidgetConfig = false;
        }
    }

    /**
     * CursorLoader and LoaderManager Implementation
     * Do db query off the main thread and communicate via these callbacks
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /* Load ALL Cached Recipe Data from Database */
        return new CursorLoader(this,
                CONTENT_URI_RECIPE,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            // Use an asyncTask to parse the multi-table db using the Cursor data
            new DbMultiTableParsingAsyncTask()
                    .setContentResolver(this.getContentResolver())
                    .setCursor(data)
                    .setIdlingResource(mIdlingResource)
                    .setListener(new DbMultiTableParsingAsyncTask.OnParsingActionComplete() {
                        @Override
                        public void onEntriesParsed(ArrayList<Recipe> recipes) {
                            mAdapter.setRecipeData(recipes);
                            mAdapter.notifyDataSetChanged();

                            /* Loading Progress Bar - Data Loaded, Be GONE */
                            mProgressBar.setVisibility(View.GONE);
                        }
            }).execute();
        }else
        {
            /* Loading Progress Bar - No Data, Be GONE */
            mProgressBar.setVisibility(View.GONE);

            // Show empty view, no data available
            // TODO design a cuter empty view and show that instead
            Toast.makeText(this,getString(R.string.data_unavailable),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mAdapter.setRecipeData(null);
    }

    /**
     * Espresso Test for idlingResource
     */
    // The Idling Resource which will be null in production.
    @Nullable
    private SimpleIdlingResource mIdlingResource;

    /**
     * For testing purposes to indicate whether the device is at an idle state
     * (no pending network transactions, downloads or other long running operations)
     * creates and returns a new {@link SimpleIdlingResource}.
     */
    @VisibleForTesting
    @NonNull
    public SimpleIdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }
}

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

import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.andrewclam.bakingapp.asyncTasks.DbMultiTableParsingAsyncTask;
import com.andrewclam.bakingapp.data.RecipeDbContract;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;
import com.google.android.exoplayer2.ui.BuildConfig;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_ID;
import static com.andrewclam.bakingapp.Constants.EXTRA_STEP_POSITION;
import static com.andrewclam.bakingapp.StepDetailFragment.EXTRA_TWO_PANE_MODE;

public class StepDetailActivity extends AppCompatActivity implements
        StepDetailFragment.OnStepDetailFragmentInteraction,
        LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * The list of steps to populate the dropdown spinner adapter
     */
    private String mRecipeName;
    private int mStepPosition;
    private ArrayList<Step> mSteps;
    private boolean mTwoPane;

    /**
     * Instance of the StepsAdapter
     */
    private StepsAdapter mStepsAdapter;

    /**
     * LoaderManager Implementation for Loading offline db data
     * This ID will be used to identify the Loader responsible for loading our offline database. In
     * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
     * We will still use this ID to initialize the loader and create the loader for best practice.
     */
    private static final int RECIPE_STEP_DETAIL_LOADER_ID = 1688;

    /**
     * The UID of the recipe
     */
    private long mRecipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Get the list of steps from intent extra
        if (getIntent().hasExtra(EXTRA_RECIPE_ID))
        {
            // Get the recipeId and form the extra recipeId
            mRecipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);

            // Init the cursorLoader, handle the callback with this activity
            // pass in the recipe id as the cursor loader argument
            Bundle args = new Bundle();
            args.putLong(EXTRA_RECIPE_ID,mRecipeId);
            getSupportLoaderManager().restartLoader(RECIPE_STEP_DETAIL_LOADER_ID,args,this);
        }

        if (getIntent().hasExtra(EXTRA_STEP_POSITION)) {
            mStepPosition = getIntent().getIntExtra(EXTRA_STEP_POSITION, 0);
        }

        if (getIntent().hasExtra(EXTRA_TWO_PANE_MODE)) {
            mTwoPane = getIntent().getBooleanExtra(EXTRA_TWO_PANE_MODE, false);

            if (BuildConfig.DEBUG)
            {
                // This activity should only be fired when the twoPane mode is false
                if (mTwoPane) throw new AssertionError("error, StepDetailActivity should never" +
                        "start in twoPane mode");
            }
        }

        // TODO Remove THESE TWO below extra recipe name and steps list, as they can be populated using the
        // cursor loader
//        if (getIntent().hasExtra(EXTRA_RECIPE_NAME)) {
//            mRecipeName = getIntent().getStringExtra(EXTRA_RECIPE_NAME);
//        }
//
//        if (getIntent().hasExtra(EXTRA_STEPS_LIST)) {
//            mSteps = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_STEPS_LIST));
//        }

    }

    /**
     * setupStepDetail() is fired when the steps are available
     * populated by a cursor query/parsing, given the position
     */
    private void setupStepDetail()
    {
        // Initialize the StepsAdapter
        mStepsAdapter = new StepsAdapter(this,mSteps);

        // Setup spinner
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(mStepsAdapter);

        // Select spinner to the parameter step position
        spinner.setSelection(mStepPosition);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Find the selected item in the adapter, this will be used
                // to start the fragment
                Step selectedStep = mStepsAdapter.getItem(position);

                // When the given dropdown item is selected, show its contents in the
                // container view. set twoPane to false because this activity will not be
                // fired in two panes mode
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, StepDetailFragment.newInstance(
                                mRecipeId,
                                mRecipeName,
                                position,
                                selectedStep,
                                mTwoPane))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * CursorLoader and LoaderManager Implementation
     * Do db query off the main thread and communicate via these callbacks
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /* Return a cursor with a particular Recipe Data from Database */
        long recipeId = args.getLong(EXTRA_RECIPE_ID);
        Uri recipeIdUri = RecipeDbContract.buildRecipeUriWithId(recipeId);

        return new CursorLoader(this,
                recipeIdUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null)
            new DbMultiTableParsingAsyncTask()
                    .setContentResolver(this.getContentResolver())
                    .setCursor(data)
                    .setListener(new DbMultiTableParsingAsyncTask.OnParsingActionComplete() {
                        @Override
                        public void onEntriesParsed(ArrayList<Recipe> recipes) {
                            Recipe recipe = recipes.get(0);
                            mRecipeName = recipe.getName();
                            mSteps = recipe.getSteps();

                            // Continue setting up the UI
                            setupStepDetail();
                        }
                    }).execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecipeName = null;
        mSteps.clear();
    }

    /**
     * Fragment Interaction Callbacks
     */
    @Override
    public void setTitle(String title) {
        // No need to set title
    }

    /**
     * Implementation of the ArrayAdapter to show user a spinner dropdown of all
     * the recipe steps above the video
     */
    private static class StepsAdapter extends ArrayAdapter<Step> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;
        private final Context mContext;

        public StepsAdapter(Context context, ArrayList<Step> steps) {
            super(context, android.R.layout.simple_list_item_1, steps);
            mContext = context;
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the step item from the current position
            Step step = getItem(position);
            assert step != null;

            // Inflate the convertView with the style, reuse if it is already inflated
            View view;
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }else
            {
                view = convertView;
            }

            // Set the spinner text to be the step's short description;
            TextView labelView = view.findViewById(android.R.id.text1);

            String spinnerText =  step.getShortDescription();

            labelView.setText(spinnerText);
            labelView.setTextColor(ContextCompat.getColor(mContext,R.color.colorWhite));

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            // For styling views when spinner drops down
            // Get the step item from the current position
            Step step = getItem(position);
            assert step != null;

            // Inflate the convertView with the style, reuse if it is already inflated
            View view;
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view =  inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }else
            {
                view = convertView;
            }

            // Set the spinner text to be the step's short description;
            TextView labelTv = view.findViewById(android.R.id.text1);

            String labelText = mContext.getString(R.string.step, step.getStepNum()) + " "
                    + step.getShortDescription();

            labelTv.setText(labelText);

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }

    // todo remove the getter methods since the activities may not be created ??

    /**
     * Package-Private getter method for the fragment to get the steps
     * The list of steps is used for the notification pendingIntent
     * to launch the StepDetailActivity
     * @return the list of steps of this current recipe
     */
    ArrayList<Step> getSteps()
    {
        return mSteps;
    }

    /**
     * Package-private getter method for the fragment to get the recipe name
     * The recipe name is used for the notification pendingIntent
     * to launch the StepDetailActivity
     * @return the recipe's name
     */
    String getRecipeName()
    {
        return mRecipeName;
    }

}

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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.andrewclam.bakingapp.models.Ingredient;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;

import org.parceler.Parcels;

import java.util.ArrayList;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;
import static com.andrewclam.bakingapp.StepDetailActivity.ARG_RECIPE_NAME;
import static com.andrewclam.bakingapp.StepDetailActivity.ARG_RECIPE_STEPS_LIST;
import static com.andrewclam.bakingapp.StepDetailActivity.ARG_RECIPE_STEP_POSITION;
import static com.andrewclam.bakingapp.StepDetailFragment.ARG_TWO_PANE_MODE;

/**
 * An activity representing a list of Steps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SimpleStepDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StepListActivity extends AppCompatActivity implements
        StepDetailFragment.OnStepDetailFragmentInteraction{

    /**
     * Log Tag
     */
    private static final String TAG = StepListActivity.class.getSimpleName();

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane = false;

    /**
     * Recipe Object, show a list of steps
     */
    private Recipe mRecipe;

    /**
     * List of steps for this particular recipe
     */
    private ArrayList<Step> mSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
         * Get the intent extra, store the passed in recipe object
         * The recipe object contains the list of ingredients and steps
         */
        if (getIntent().hasExtra(EXTRA_RECIPE)) {
            mRecipe = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_RECIPE));
        }

        if (findViewById(R.id.step_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        /* UI Setup - Recipe Header (Activity Title, Serving and Number of Steps)*/
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mRecipe.getName());
        }

        TextView servingTv = findViewById(R.id.recipe_servings_tv);
        TextView numStepsTv = findViewById(R.id.steps_num_tv);

        servingTv.setText(getString(R.string.serving, mRecipe.getServings()));
        numStepsTv.setText(getString(R.string.num_steps,mRecipe.getSteps().size()));

        /* UI Setup - RecyclerView Lists (Ingredients and Steps) */
        RecyclerView stepsRv = findViewById(R.id.step_list_rv);
        RecyclerView ingredientRv = findViewById(R.id.ingredient_list_rv);

        setupIngredientsRecyclerView(ingredientRv);
        setupStepsRecyclerView(stepsRv);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A method to setup the UI recyclerView to show the list of recipe ingredients
     *
     * @param recyclerView the recyclerView that contains the list of ingredients
     */
    private void setupIngredientsRecyclerView(@NonNull RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(new IngredientRecyclerViewAdapter(mRecipe.getIngredients()));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
    }

    /**
     * A method to setup the UI recyclerView to show the list of recipe steps
     *
     * @param recyclerView the recyclerView that contains the list of steps
     */
    private void setupStepsRecyclerView(@NonNull RecyclerView recyclerView) {
        // Get the data from the recipe
        mSteps = mRecipe.getSteps();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(new StepsRecyclerViewAdapter(mSteps));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(true);

        // if in twoPane mode, load the fragment with the intro step
        // intro step is always at position 0 of the list
        if (mTwoPane) {
            Step introStep = mSteps.get(0);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.step_detail_container,
                            StepDetailFragment.newInstance(introStep,mTwoPane))
                    .commit();
        }
    }

    /**
     * Step detail fragment interface callback, call to change the activity's title
     * @param title the formed particular title for a step fragment
     */
    @Override
    public void setTitle(String title) {
        if(getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    /**
     * Steps RecyclerView Adapter
     * use to back the recyclerView with the data, also contains an inner
     * class that hold the cache of views.
     */
    class StepsRecyclerViewAdapter
            extends RecyclerView.Adapter<StepsRecyclerViewAdapter.StepViewHolder> {

        private final ArrayList<Step> mSteps;

        StepsRecyclerViewAdapter(ArrayList<Step> steps) {
            mSteps = steps;
        }

        @Override
        public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.step_list_item, parent, false);
            return new StepViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final StepViewHolder holder, final int position) {
            // Get the step item at the position
            Step step = mSteps.get(position);

            // Bind the step data using holder's set methods
            holder.setStepItem(step);

            // UI Styling
            // FIXME [NEED ADVICE] What is the best practice to find the first/last visible item in recyclerView?

            // Intro-Step , Don't number it
            // Check if it is the intro-step (intro step is with id of 0)
            if (position == 0) {
                // Not the intro step, prepend the item with step number
                holder.mStepIdTv.setText(getString(R.string.start));
            }else
            {
                // Not the intro step, prepend the item with step number
                holder.mStepIdTv.setText(getString(R.string.step, step.getStepNum()));
            }

            // Last Step, Don't show divider
            // Check if the position is at the last step
            if (position == getItemCount() - 1)
            {
                holder.mItemDivider.setVisibility(View.GONE);
            }

            holder.mShortDescriptionTv.setText(String.valueOf(step.getShortDescription()));

            // Set onClickListener on each step view to open the
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTwoPane) {
                        // In two pane mode, replace a fragment with the step item to show the
                        // full detail
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.step_detail_container,
                                        StepDetailFragment.newInstance(holder.getStepItem(),
                                                mTwoPane))
                                .commit();
                    } else {
                        // In single pane mode, start a new detail activity showing the step's
                        // full detail
                        Context context = view.getContext();

                        Intent intent = new Intent(context, StepDetailActivity.class);
                        intent.putExtra(ARG_RECIPE_NAME,mRecipe.getName());
                        intent.putExtra(ARG_RECIPE_STEPS_LIST,Parcels.wrap(mSteps));
                        intent.putExtra(ARG_RECIPE_STEP_POSITION,holder.getAdapterPosition());
                        intent.putExtra(ARG_TWO_PANE_MODE,mTwoPane);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSteps.size();
        }

        /**
         * The ViewHolder class to store UI views of each step
         */
        class StepViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mStepIdTv;
            final TextView mShortDescriptionTv;
            final FrameLayout mItemDivider;
            private Step mStepItem;

            StepViewHolder(View view) {
                super(view);
                mView = view;
                mStepIdTv = view.findViewById(R.id.step_id_tv);
                mShortDescriptionTv = view.findViewById(R.id.step_short_description_tv);
                mItemDivider = view.findViewById(R.id.list_divider);
            }

            /**
             * Holder public method to return the stored step item
             *
             * @return the viewHolder's step item
             */
            Step getStepItem() {
                return this.mStepItem;
            }

            /**
             * Holder public method to set and store the item of the ViewHolder
             *
             * @param step the step object at the adapter position
             */
            void setStepItem(Step step) {
                this.mStepItem = step;
            }
        }
    }

    /**
     * Ingredient RecyclerView Adapter
     * use to back the recyclerView with the data, also contains an inner
     * class that hold the cache of views.
     */
    class IngredientRecyclerViewAdapter
            extends RecyclerView.Adapter<IngredientRecyclerViewAdapter.IngredientViewHolder> {

        private final ArrayList<Ingredient> mIngredients;

        IngredientRecyclerViewAdapter(ArrayList<Ingredient> ingredients) {
            mIngredients = ingredients;
        }

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ingredient_list_item, parent, false);
            return new IngredientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final IngredientViewHolder holder, int position) {
            // Get the step item at the position
            Ingredient ingredient = mIngredients.get(position);

            // Bind the step data using holder's set methods
            holder.mQuantityTv.setText(String.valueOf(ingredient.getQuantity()));
            holder.mMeasureTv.setText(ingredient.getMeasure());
            holder.mIngredientNameTv.setText(ingredient.getIngredientName());

            // UI Styling
            // Last Step, Don't show divider
            // Check if the position is at the last step
            if (position == getItemCount() - 1)
            {
                holder.mItemDivider.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mIngredients.size();
        }

        /**
         * The ViewHolder class to store UI views of each step
         */
        class IngredientViewHolder extends RecyclerView.ViewHolder {
            final TextView mQuantityTv;
            final TextView mMeasureTv;
            final TextView mIngredientNameTv;
            final FrameLayout mItemDivider;

            IngredientViewHolder(View view) {
                super(view);
                mQuantityTv = view.findViewById(R.id.ingredient_quantity_tv);
                mMeasureTv = view.findViewById(R.id.ingredient_measure_tv);
                mIngredientNameTv = view.findViewById(R.id.ingredient_name_tv);
                mItemDivider = view.findViewById(R.id.list_divider);
            }
        }
    }

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
        return mRecipe.getName();
    }

}

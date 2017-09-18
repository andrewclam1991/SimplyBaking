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
import android.widget.TextView;

import com.andrewclam.bakingapp.models.Ingredient;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;

import org.parceler.Parcels;

import java.util.ArrayList;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;

/**
 * An activity representing a list of Steps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StepDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StepListActivity extends AppCompatActivity {

    /**
     * Log Tag
     */
    private static final String TAG = StepListActivity.class.getSimpleName();

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Recipe Object, show a list of steps
     */
    private Recipe mRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.step_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        /*
         * Get the intent extra, store the passed in recipe object
         * The recipe object contains the list of ingredients and steps
         */
        if (getIntent().hasExtra(EXTRA_RECIPE)) {
            mRecipe = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_RECIPE));
        }

        /* UI Setup - Recipe Header (Name and Serving)*/
        TextView nameTv = findViewById(R.id.recipe_name_tv);
        TextView servingTv = findViewById(R.id.recipe_servings_tv);

        assert nameTv != null;
        toolbar.setTitle(mRecipe.getName());
        nameTv.setText(mRecipe.getName());
        assert servingTv != null;
        servingTv.setText(getString(R.string.serving, mRecipe.getServings()));

        /* UI Setup - RecyclerView Lists (Ingredients and Steps) */
        RecyclerView stepsRv = findViewById(R.id.step_list_rv);
        RecyclerView ingredientRv = findViewById(R.id.ingredient_list_rv);

        assert ingredientRv != null;
        setupIngredientsRecyclerView(ingredientRv);
        assert stepsRv != null;
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(new StepsRecyclerViewAdapter(mRecipe.getSteps()));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(true);

        // if in twoPane mode, load the fragment with the intro step
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(StepDetailFragment.ARG_RECIPE_STEP,
                    Parcels.wrap(mRecipe.getSteps().get(0)));

            StepDetailFragment fragment = new StepDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.step_detail_container, fragment)
                    .commit();
        }
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
        public void onBindViewHolder(final StepViewHolder holder, int position) {
            // Get the step item at the position
            Step step = mSteps.get(position);

            // Bind the step data using holder's set methods
            holder.setStepItem(step);

            // Check if it is the intro-step (intro step is with id of 0)
            if (step.getId() != 0) {
                // Not the intro step, prepend the item with step number
                holder.mStepIdTv.setText(getString(R.string.step, step.getId()));
            }

            holder.mShortDescriptionTv.setText(String.valueOf(step.getShortDescription()));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTwoPane) {
                        // In two pane mode, replace a fragment with the step item to show the
                        // full detail
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(StepDetailFragment.ARG_RECIPE_STEP,
                                Parcels.wrap(holder.getStepItem()));

                        StepDetailFragment fragment = new StepDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.step_detail_container, fragment)
                                .commit();
                    } else {
                        // In single pane mode, start a new detail activity showing the step's
                        // full detail
                        Context context = view.getContext();
                        Intent intent = new Intent(context, StepDetailActivity.class);
                        intent.putExtra(StepDetailFragment.ARG_RECIPE_STEP,
                                Parcels.wrap(holder.getStepItem()));

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
            private Step mStepItem;

            StepViewHolder(View view) {
                super(view);
                mView = view;
                mStepIdTv = view.findViewById(R.id.step_id_tv);
                mShortDescriptionTv = view.findViewById(R.id.step_short_description_tv);
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

            IngredientViewHolder(View view) {
                super(view);
                mQuantityTv = view.findViewById(R.id.ingredient_quantity_tv);
                mMeasureTv = view.findViewById(R.id.ingredient_measure_tv);
                mIngredientNameTv = view.findViewById(R.id.ingredient_name_tv);
            }
        }
    }

}

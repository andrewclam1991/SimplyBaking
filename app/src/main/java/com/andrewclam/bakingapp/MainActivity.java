package com.andrewclam.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.andrewclam.bakingapp.adapters.RecipeRecyclerViewAdapter;
import com.andrewclam.bakingapp.asyncTasks.FetchRecipeAsyncTask;
import com.andrewclam.bakingapp.models.Recipe;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.Constants.DATA_URL;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE;

public class MainActivity extends AppCompatActivity implements
        FetchRecipeAsyncTask.onFetchRecipeActionListener,
        RecipeRecyclerViewAdapter.OnRecipeItemClickedListener {

    /**
     * RecyclerView to show the list of recipes
     */
    private RecyclerView mRecipeRv;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecipeRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Recipes List Setup */
        mRecipeRv = findViewById(R.id.recipe_list_rv);
        mAdapter = new RecipeRecyclerViewAdapter(this, this);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecipeRv.setLayoutManager(mLayoutManager);
        mRecipeRv.setAdapter(mAdapter);

        /* Async Load Recipe Data */
        new FetchRecipeAsyncTask()
                .setDataURL(DATA_URL)
                .setListener(this)
                .execute();
    }


    /**
     * Callback from the FetchRecipeAsyncTask with a list of recipe ready to populate the
     * recycler view.
     *
     * @param recipes the list of recipes parsed from the data source
     */
    @Override
    public void onRecipesReady(ArrayList<Recipe> recipes) {
        mAdapter.setRecipeData(recipes);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Callback from the RecipeRecyclerViewAdapter when the user clicks a recipe from the list.
     *
     * @param recipe the user's clicked recipe
     */
    @Override
    public void onRecipeClicked(Recipe recipe) {
        // 1) should launch the detailActivity showing the recipe's full info
        Intent intent = new Intent(this, StepListActivity.class);
        intent.putExtra(EXTRA_RECIPE, Parcels.wrap(recipe));
        startActivity(intent);
    }
}

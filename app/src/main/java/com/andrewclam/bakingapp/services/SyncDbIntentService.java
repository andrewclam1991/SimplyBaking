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

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.andrewclam.bakingapp.models.Ingredient;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;

import org.parceler.Parcels;

import java.util.List;

import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_LIST;
import static com.andrewclam.bakingapp.Constants.PACKAGE_NAME;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_MEASURE;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_NAME;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_QUANTITY;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.CONTENT_URI_INGREDIENT;
import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.COLUMN_RECIPE_ID;
import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.COLUMN_RECIPE_IMAGE_URL;
import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.COLUMN_RECIPE_NAME;
import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.COLUMN_RECIPE_SERVINGS;
import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.CONTENT_URI_RECIPE;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_DESCRIPTION;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_ID;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_RECIPE_KEY;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_SHORT_DESCRIPTION;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_THUMBNAIL_URL;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_VIDEO_URL;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.CONTENT_URI_STEP;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SyncDbIntentService extends IntentService {
    /**
     * Debug Tag
     */
    private static final String TAG = SyncDbIntentService.class.getSimpleName();

    /**
     * Actions that the intent service can perform
     */
    private static final String ACTION_SYNC_RECIPES = PACKAGE_NAME
            + ".services.action.insert.recipes";

    public SyncDbIntentService() {
        super(SyncDbIntentService.class.getSimpleName());
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void syncRecipes(Context context, List<Recipe> recipes) {
        Intent intent = new Intent(context, SyncDbIntentService.class);
        intent.putExtra(EXTRA_RECIPE_LIST, Parcels.wrap(recipes));
        intent.setAction(ACTION_SYNC_RECIPES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_SYNC_RECIPES:
                    Log.d(TAG, "onHandleIntent() ACTION_SYNC_RECIPES received");
                    List<Recipe> recipes = Parcels.unwrap(
                            intent.getParcelableExtra(EXTRA_RECIPE_LIST));
                    handleSyncRecipes(recipes);
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "Unsupported Action");
            }
        }
    }

    /**
     * Handle ActionUpdateWidget in the provided background thread with the provided
     * parameters.
     */
    private void handleSyncRecipes(List<Recipe> recipes) {
        Log.d(TAG, "handleSyncRecipes() Got entries from the web services");

        ContentResolver contentResolver = this.getContentResolver();

        if (contentResolver == null)
            throw new NullPointerException("Unable to use " +
                    "context to get the contentResolver");

        // Iterate over all the recipes
        for (Recipe recipe : recipes) {
            // Get the unique recipe id first, this is used as foreign key for child tables
            Long recipeId = recipe.getId();

            // Put the java fields into key-value pairs
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_RECIPE_ID, recipeId);
            cv.put(COLUMN_RECIPE_IMAGE_URL, recipe.getImageURL());
            cv.put(COLUMN_RECIPE_NAME, recipe.getName());
            cv.put(COLUMN_RECIPE_SERVINGS, recipe.getServings());

            // PARENT TABLE - RECIPES
            syncRecipesTable(contentResolver, cv);

            // CHILD TABLE - INGREDIENTS
            syncIngredientsTable(contentResolver, recipe.getIngredients(), recipeId);

            // CHILD TABLE - STEPS
            syncStepsTable(contentResolver, recipe.getSteps(), recipeId);
        }
    }

    /**
     * Method to insert individual recipe into the recipe table
     *
     * @param contentResolver application context to
     * @param cv
     * @return
     */
    private void syncRecipesTable(@NonNull ContentResolver contentResolver, ContentValues cv) {
        // use the contentResolver bulkInsert to insert all the cv values
        final Uri contentUri = contentResolver.insert(CONTENT_URI_RECIPE, cv);
        if (contentUri == null) Log.e(TAG,"syncRecipesTable() failed");
    }

    /**
     * Method to bulkInsert syncStepsTable of a particular recipe into the IngredientsTable
     *
     * @param contentResolver
     * @param steps
     * @param recipeId
     */
    synchronized private void syncStepsTable(@NonNull ContentResolver contentResolver,
                                                List<Step> steps,
                                                Long recipeId) {

        ContentValues[] stepsCvArray = new ContentValues[steps.size()];

        try {
            // set a count index for the foreach loop, this index value is for
            // referencing the correct ContentValue in the ContentValue[] to
            // store each entry.
            int i = 0;

            // Iterate over all the ingredients
            for (Step step : steps) {
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_STEP_ID, step.getId());
                cv.put(COLUMN_STEP_DESCRIPTION, step.getDescription());
                cv.put(COLUMN_STEP_SHORT_DESCRIPTION, step.getShortDescription());
                cv.put(COLUMN_STEP_THUMBNAIL_URL, step.getThumbnailURL());
                cv.put(COLUMN_STEP_VIDEO_URL, step.getVideoURL());
                cv.put(COLUMN_STEP_RECIPE_KEY, recipeId);
                stepsCvArray[i] = cv;

                // increment one after iteration for the next set of cv
                i++;
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();

        } finally {
            // use the contentResolver bulkInsert to insert all the cv values
            final int rowInserted = contentResolver.bulkInsert(CONTENT_URI_STEP, stepsCvArray);
            if (rowInserted <= 0) Log.e(TAG,"syncStepsTable() failed");
        }
    }


    /**
     * Method to bulkInsert ingredients of a particular recipe into the IngredientsTable
     *
     * @param contentResolver
     * @param ingredients
     * @param recipeId
     */
    synchronized private void syncIngredientsTable(@NonNull ContentResolver contentResolver,
                                                      List<Ingredient> ingredients,
                                                      Long recipeId) {
        ContentValues[] cvArray = new ContentValues[ingredients.size()];

        try {
            // set a count index for the foreach loop, this index value is for
            // referencing the correct ContentValue in the ContentValue[] to
            // store each entry.
            int i = 0;

            // Iterate over all the ingredients
            for (Ingredient ingredient : ingredients) {
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_INGREDIENT_MEASURE, ingredient.getMeasure());
                cv.put(COLUMN_INGREDIENT_NAME, ingredient.getIngredientName());
                cv.put(COLUMN_INGREDIENT_QUANTITY, ingredient.getQuantity());
                cv.put(COLUMN_INGREDIENT_RECIPE_KEY, recipeId);
                cvArray[i] = cv;

                // increment one after iteration for the next set of cv
                i++;
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();

        } finally {
            // use the contentResolver bulkInsert to insert all the cv values
            final int rowInserted =
                    contentResolver.bulkInsert(CONTENT_URI_INGREDIENT, cvArray);
            if (rowInserted <= 0) Log.e(TAG,"syncIngredientsTable() failed");
        }
    }
}

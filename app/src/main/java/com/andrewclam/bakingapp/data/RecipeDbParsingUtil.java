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

package com.andrewclam.bakingapp.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.andrewclam.bakingapp.models.Ingredient;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY;
import static com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry.CONTENT_URI_INGREDIENT;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.COLUMN_STEP_RECIPE_KEY;
import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.CONTENT_URI_STEP;

/**
 * Created by Andrew Chi Heng Lam on 9/22/2017.
 * Rudimentary implementation of a multi-table query parsing to a list of model class
 */
@Deprecated
class RecipeDbParsingUtil {

    /**
     * Method to return an List of a model class from the client database to show the data
     * when the user is not online.
     *
     * @param recipeCursor the Cursor that is returned from the query from the client's database
     */
    @Deprecated
    public static ArrayList<Recipe> parseEntriesFromCursor (Context context, final Cursor recipeCursor) {
        final ArrayList<Recipe> entries = new ArrayList<>();
        final ContentResolver contentResolver = context.getContentResolver();

        while (recipeCursor.moveToNext()) {
            // Create a new entry to store the database
            Recipe entry = new Recipe();

            // Get the index of each column from the cursor
            int recipeIdColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_UID);
            int recipeNameColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_NAME);
            int servingColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_SERVINGS);
            int imageUrlColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_IMAGE_URL);

            // Set each field to the entry
            Long recipeId = recipeCursor.getLong(recipeIdColIndex);

            entry.setUid(recipeId);
            entry.setName(recipeCursor.getString(recipeNameColIndex));
            entry.setServings(recipeCursor.getInt(servingColIndex));
            entry.setImageURL(recipeCursor.getString(imageUrlColIndex));

            // Steps Child Table
            // Get its cursor, select only rows with the key that equals to the id
            ArrayList<Step> steps = new ArrayList<>();
            try {
                Cursor stepCursor = contentResolver.query(
                        CONTENT_URI_STEP,
                        null,
                        COLUMN_STEP_RECIPE_KEY + "=?",
                        new String[]{String.valueOf(recipeId)},
                        null);

                if (stepCursor != null) {
                    // Parse the steps from the stepCursor
                    while (stepCursor.moveToNext()) {
                        Step step = new Step();

                        int stepUidIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_UID);
                        int stepIdIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_NUM);
                        int shortDescriptionIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_SHORT_DESCRIPTION);
                        int descriptionIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_DESCRIPTION);
                        int thumbnailIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_THUMBNAIL_URL);
                        int videoIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_VIDEO_URL);

                        step.setUid(stepCursor.getString(stepUidIndex));
                        step.setStepNum(stepCursor.getLong(stepIdIndex));
                        step.setShortDescription(stepCursor.getString(shortDescriptionIndex));
                        step.setDescription(stepCursor.getString(descriptionIndex));
                        step.setThumbnailURL(stepCursor.getString(thumbnailIndex));
                        step.setVideoURL(stepCursor.getString(videoIndex));

                        steps.add(step);
                    }
                    stepCursor.close();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally
            {
                entry.setSteps(steps);
            }

            // Ingredient Child Table
            // Get its cursor, select only rows with the key that equals to the id
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            try {
                Cursor ingredientCursor = contentResolver.query(
                        CONTENT_URI_INGREDIENT,
                        null,
                        COLUMN_INGREDIENT_RECIPE_KEY + "=?",
                        new String[]{String.valueOf(recipeId)},
                        null);

                // Parse the ingredients
                if (ingredientCursor != null) {
                    while (ingredientCursor.moveToNext()) {
                        Ingredient ingredient = new Ingredient();

                        int ingredientUidIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_UID);
                        int ingredientMeasureIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_MEASURE);
                        int ingredientNameIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_NAME);
                        int ingredientQuantityIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_QUANTITY);

                        ingredient.setUid(ingredientCursor.getString(ingredientUidIndex));
                        ingredient.setIngredientName(ingredientCursor.getString(ingredientNameIndex));
                        ingredient.setMeasure(ingredientCursor.getString(ingredientMeasureIndex));
                        ingredient.setQuantity(ingredientCursor.getDouble(ingredientQuantityIndex));

                        ingredients.add(ingredient);
                    }
                    ingredientCursor.close();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally
            {
                entry.setIngredients(ingredients);
            }

            // Add the populated entry into the entry list
            entries.add(entry);
        }

        // Close the cursor after the loop
        recipeCursor.close();

        return entries;
    }
}

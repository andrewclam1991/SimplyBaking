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

package com.andrewclam.bakingapp.utils;

import android.content.Context;
import android.database.Cursor;

import com.andrewclam.bakingapp.data.RecipeDbContract;
import com.andrewclam.bakingapp.models.Ingredient;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry.CONTENT_URI_STEP;

/**
 * Created by Andrew Chi Heng Lam on 9/22/2017.
 */

public class RecipeDbParsingUtil {

    /**
     * Method to return an List of a model class from the client database to show the data
     * when the user is not online.
     *
     * @param recipeCursor the Cursor that is returned from the query from the client's database
     */
    public static ArrayList<Recipe> parseEntriesFromCursor (Context context, Cursor recipeCursor,
                                                            Cursor stepCursor,
                                                            Cursor ingredientCursor) {
        ArrayList<Recipe> entries = new ArrayList<>();

        while (recipeCursor.moveToNext()) {
            // Create a new entry to store the database
            Recipe entry = new Recipe();

            // Get the index of each column from the cursor
            int recipeIdColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_ID);
            int recipeNameColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_NAME);
            int servingColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_SERVINGS);
            int imageUrlColIndex = recipeCursor.getColumnIndex(RecipeDbContract.RecipeEntry.COLUMN_RECIPE_IMAGE_URL);

            // Parse the steps from the stepCursor
            ArrayList<Step> steps = new ArrayList<>();
            Cursor stepCursor = context.getContentResolver().query(
                    CONTENT_URI_STEP,
                    null,
                    )
            while (stepCursor.moveToNext())
            {
                Step step = new Step();

                int stepIdIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_ID);
                int shortDescriptionIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_SHORT_DESCRIPTION);
                int descriptionIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_DESCRIPTION);
                int thumbnailIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_THUMBNAIL_URL);
                int videoIndex = stepCursor.getColumnIndex(RecipeDbContract.StepEntry.COLUMN_STEP_VIDEO_URL);

                step.setId(stepCursor.getLong(stepIdIndex));
                step.setShortDescription(stepCursor.getString(shortDescriptionIndex));
                step.setDescription(stepCursor.getString(descriptionIndex));
                step.setThumbnialURL(stepCursor.getString(thumbnailIndex));
                step.setVideoURL(stepCursor.getString(videoIndex));

                steps.add(step);
            }

            // Parse the ingredients
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            while (ingredientCursor.moveToNext())
            {
                Ingredient ingredient = new Ingredient();

                int ingredientMeasureIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_MEASURE);
                int ingredientNameIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_NAME);
                int ingredientQuantityIndex = ingredientCursor.getColumnIndex(RecipeDbContract.IngredientEntry.COLUMN_INGREDIENT_QUANTITY);

                ingredient.setIngredientName(ingredientCursor.getString(ingredientNameIndex));
                ingredient.setMeasure(ingredientCursor.getString(ingredientMeasureIndex));
                ingredient.setQuantity(ingredientCursor.getDouble(ingredientQuantityIndex));

                ingredients.add(ingredient);
            }

            // Set each field to the entry
            entry.setId(recipeCursor.getLong(recipeIdColIndex));
            entry.setName(recipeCursor.getString(recipeNameColIndex));
            entry.setServings(recipeCursor.getInt(servingColIndex));
            entry.setImageURL(recipeCursor.getString(imageUrlColIndex));
            entry.setIngredients(ingredients);
            entry.setSteps(steps);

            // Add the populated entry into the entry list
            entries.add(entry);
        }

        // Close the cursor after the loop
        recipeCursor.close();

        return entries;
    }
}

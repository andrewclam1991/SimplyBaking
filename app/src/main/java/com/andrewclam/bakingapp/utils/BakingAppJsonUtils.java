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

import android.util.Log;

import com.andrewclam.bakingapp.models.Ingredient;
import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.models.Step;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andrew Chi Heng Lam on 8/19/2017.
 * <p>
 * BakingAppJsonUtils contain methods to parse the JSON response into individual JSON Object, and
 * store the data in a model class (eg. recipe)
 */

public final class BakingAppJsonUtils {
    /* JSON Key Constants */
    private static final String RECIPE_ID = "id";
    private static final String RECIPE_NAME = "name";

    private static final String RECIPE_INGREDIENTS = "ingredients";
    private static final String RECIPE_INGREDIENTS_QUANTITY = "quantity";
    private static final String RECIPE_INGREDIENTS_MEASURE = "measure";
    private static final String RECIPE_INGREDIENTS_INGREDIENT_NAME = "ingredient";

    private static final String RECIPE_STEPS = "steps";
    private static final String RECIPE_STEPS_ID = "id";
    private static final String RECIPE_STEPS_SHORT_DESCRIPTION = "shortDescription";
    private static final String RECIPE_STEPS_DESCRIPTION = "description";
    private static final String RECIPE_STEPS_VIDEO_URL = "videoURL";
    private static final String RECIPE_STEPS_THUMBNAIL_URL = "thumbnailURL";

    private static final String RECIPE_SERVINGS = "servings";
    private static final String RECIPE_IMAGE = "image";

    /* Log Tag */
    private static final String TAG = BakingAppJsonUtils.class.getSimpleName();

    /**
     * This method parses a JSON String from a web response and returns an ArrayList of objects
     *
     * @param jsonResponse a String JSON response from server
     * @return an ArrayList of Recipes objects, each containing the steps, ingredient and media
     * @throws JSONException If JSON data cannot be properly parsed
     */

    public static ArrayList<Recipe> getRecipesFromJson(String jsonResponse) throws JSONException {
        // Test if the response is null, return null if it is
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            Log.w(TAG, "Nothing to parse because jsonResponse is empty or undefined");
            return null;
        }

        // Create a new JSON array out of the jsonResponse
        JSONArray recipeJSONArray = new JSONArray(jsonResponse);

        // Initialize an arrayList to store objects. This data will back the recycler view adapter.
        ArrayList<Recipe> recipes = new ArrayList<>();

        // Loop through each element result in the resultArray
        for (int i = 0; i < recipeJSONArray.length(); i++) {

            // Get each element of the resultArray as a result element
            JSONObject recipeJSON = recipeJSONArray.getJSONObject(i);
            if (recipeJSON != null) {
                /* Create an instance of the model class to store the retrieved elements */
                Recipe recipe = new Recipe();

                /* Retrieve each element from the result JSONObject */
                long id = recipeJSON.getLong(RECIPE_ID);
                String name = recipeJSON.getString(RECIPE_NAME);
                int servings = recipeJSON.getInt(RECIPE_SERVINGS);
                String imageURL = recipeJSON.getString(RECIPE_IMAGE);

                /* Store each element into the data model class */
                recipe.setId(id);
                recipe.setName(name);
                recipe.setServings(servings);
                recipe.setImageURL(imageURL);
                recipe.setIngredients(getIngredientFromRecipeJson(recipeJSON));
                recipe.setSteps(getStepsFromRecipeJson(recipeJSON));


                /* Add the recipe object to the list */
                recipes.add(recipe);
            } else {
                Log.w(TAG, "Error retrieving the json object at index " + i +
                        ", skipping creating item");
            }
        }

        return recipes;
    }

    /**
     * getStepsFromRecipeJson() is a helper method to parse the steps in each recipe JSON
     * into a list of Steps.
     *
     * @return a list of steps in an array list
     */

    private static ArrayList<Step> getStepsFromRecipeJson(JSONObject recipeJSON)
            throws JSONException {
        // Get the steps as an array from the result
        JSONArray array = recipeJSON.getJSONArray(RECIPE_STEPS);

        // Check if steps is empty, if so just return an empty array list;
        if (array.length() == 0) return new ArrayList<>();

        // Initialize an ArrayList to store all the steps
        ArrayList<Step> steps = new ArrayList<>();

        // Loop through each element step in the stepsArray;
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            long id = jsonObject.getLong(RECIPE_STEPS_ID);
            String shortDescription = jsonObject.getString(RECIPE_STEPS_SHORT_DESCRIPTION);
            String description = jsonObject.getString(RECIPE_STEPS_DESCRIPTION);
            String videoURL = jsonObject.getString(RECIPE_STEPS_VIDEO_URL);
            String thumbnailURL = jsonObject.getString(RECIPE_STEPS_THUMBNAIL_URL);

            // Create an instance of the model class step to store the info*/
            Step step = new Step();
            step.setId(id);
            step.setShortDescription(shortDescription);
            step.setDescription(description);
            step.setVideoURL(videoURL);
            step.setThumbnialURL(thumbnailURL);

            // Add the step to the list of steps
            steps.add(step);
        }

        return steps;
    }

    /**
     * getStepsFromRecipeJson() is a helper method to parse the ingredients in each recipe JSON
     * into a list of ingredients.
     *
     * @return a list of ingredients in an array list
     */
    private static ArrayList<Ingredient> getIngredientFromRecipeJson(JSONObject recipeJSON)
            throws JSONException {
        // Get the ingredients as an array from the result
        JSONArray array = recipeJSON.getJSONArray(RECIPE_INGREDIENTS);

        // Check if ingredients is empty, if so just return an empty array list;
        if (array.length() == 0) return new ArrayList<>();

        // Initialize an ArrayList to store all the ingredients
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        // Loop through each element step in the stepsArray;
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);

            double quantity = jsonObject.getDouble(RECIPE_INGREDIENTS_QUANTITY);
            String measure = jsonObject.getString(RECIPE_INGREDIENTS_MEASURE);
            String ingredientName = jsonObject.getString(RECIPE_INGREDIENTS_INGREDIENT_NAME);

            // Create an instance of the model class step to set the info*/
            Ingredient ingredient = new Ingredient();

            ingredient.setQuantity(quantity);
            ingredient.setMeasure(measure);
            ingredient.setIngredientName(ingredientName);

            // Add the ingredient to the list of ingredients
            ingredients.add(ingredient);
        }

        return ingredients;
    }
}

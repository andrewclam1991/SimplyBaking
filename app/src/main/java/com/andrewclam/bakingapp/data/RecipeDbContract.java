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

import android.net.Uri;
import android.provider.BaseColumns;

import static com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry.CONTENT_URI_RECIPE;

/**
 * Created by Andrew Lam
 * Multi-table SQLite practice
 */

public class RecipeDbContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.andrewclam.bakingapp";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    public static final String PATH_RECIPES = "recipes";
    public static final String PATH_INGREDIENTS = "ingredients";
    public static final String PATH_STEPS = "steps";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_APP_WIDGET_IDS = "app_widget_ids";

    // Invalid Case Constants
    public static final long INVALID_RECIPE_ID = -1;
    public static final long INVALID_INGREDIENT_ID = -1;
    public static final long INVALID_STEP_ID = -1;
    public static final long INVALID_FAVORITE_ID = -1;
    public static final long INVALID_APP_WIDGET_ID = -1;

    // Declare a SQLiteQueryBuilder.setTable Statement to JOIN all tables for query
    static final String SET_TABLE_STATEMENT =
            RecipeEntry.TABLE_NAME +
                    " LEFT JOIN " +
                    IngredientEntry.TABLE_NAME +
                    " ON " +
                    RecipeEntry.TABLE_NAME + "." + RecipeEntry.COLUMN_RECIPE_UID +
                    " = " +
                    IngredientEntry.TABLE_NAME + "." + IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY +
                    " LEFT JOIN " +
                    StepEntry.TABLE_NAME +
                    " ON " +
                    StepEntry.TABLE_NAME + "." + StepEntry.COLUMN_STEP_RECIPE_KEY +
                    " = " +
                    RecipeEntry.TABLE_NAME + "." + RecipeEntry.COLUMN_RECIPE_UID;

    public static final class RecipeEntry implements BaseColumns {
        public static final Uri CONTENT_URI_RECIPE =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();

        public static final String TABLE_NAME = "recipes";
        public static final String COLUMN_RECIPE_UID = "recipe_uid";
        public static final String COLUMN_RECIPE_NAME = "recipe_name";
        public static final String COLUMN_RECIPE_SERVINGS = "recipe_servings";
        public static final String COLUMN_RECIPE_IMAGE_URL = "recipe_image_url";
    }

    public static final class IngredientEntry implements BaseColumns{
        public static final Uri CONTENT_URI_INGREDIENT =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INGREDIENTS).build();

        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_INGREDIENT_UID = "ingredient_uid";
        public static final String COLUMN_INGREDIENT_QUANTITY = "ingredient_quantity";
        public static final String COLUMN_INGREDIENT_MEASURE = "ingredient_measure";
        public static final String COLUMN_INGREDIENT_NAME = "ingredient_name";
        public static final String COLUMN_INGREDIENT_RECIPE_KEY = "recipe_id";
    }

    public static final class StepEntry implements BaseColumns{
        public static final Uri CONTENT_URI_STEP =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STEPS).build();

        public static final String TABLE_NAME = "steps";
        public static final String COLUMN_STEP_UID = "step_uid";
        public static final String COLUMN_STEP_NUM = "step_num";
        public static final String COLUMN_STEP_SHORT_DESCRIPTION = "step_short_description";
        public static final String COLUMN_STEP_DESCRIPTION = "step_description";
        public static final String COLUMN_STEP_VIDEO_URL = "step_video_url";
        public static final String COLUMN_STEP_THUMBNAIL_URL = "step_thumbnail_url";
        public static final String COLUMN_STEP_RECIPE_KEY = "recipe_id";
    }

    public static final class FavoriteEntry implements BaseColumns{
        public static final Uri CONTENT_URI_FAVORITE =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_FAVORITE_BOOL = "is_favorite";
        public static final String COLUMN_FAVORITE_RECIPE_KEY = "recipe_id";
    }

    public static final class AppWidgetIdEntry implements BaseColumns{
        public static final Uri CONTENT_URI_APP_WIDGET_ID =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_APP_WIDGET_IDS).build();

        public static final String TABLE_NAME = "app_widget_ids";
        public static final String COLUMN_APP_WIDGET_UID = "app_widget_uid";
        public static final String COLUMN_APP_WIDGET_RECIPE_KEY = "recipe_id";
    }

    /**
     * Easy to use helper method to build the uri that points to a specific resource
     * <p>
     * Recipe with given its unique recipe id
     *
     * @param recipe_id the unique recipe id as fetched from web service)
     * @return an Uri that points to that particular movie on the user's database
     */
    public static Uri buildRecipeUriWithId(Long recipe_id) {
        String idStr = String.valueOf(recipe_id);
        return CONTENT_URI_RECIPE.buildUpon().appendPath(idStr).build();
    }
}

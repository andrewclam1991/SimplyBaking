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
    // This is the path for the "recipes" directory
    public static final String PATH_RECIPES = "recipes";
    // This is the path for the "ingredient" directory
    public static final String PATH_INGREDIENTS = "ingredients";
    // This is the path for the "steps" directory
    public static final String PATH_STEPS = "steps";

    // Invalid Case Constants
    public static final long INVALID_RECIPE_ID = -1;
    public static final long INVALID_INGREDIENT_ID = -1;
    public static final long INVALID_STEP_ID = -1;

    // TODO Test this wildy untested sqlite table statement
    // Declare a SQLiteQueryBuilder.setTable Statement to JOIN all tables for query
    static final String SET_TABLE_STATEMENT =
            RecipeEntry.TABLE_NAME +
                    " INNER JOIN " +
                    IngredientEntry.TABLE_NAME +
                    " ON " +
                    RecipeEntry.TABLE_NAME + "." + RecipeEntry.COLUMN_RECIPE_ID +
                    " = " +
                    IngredientEntry.TABLE_NAME + "." + IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY +
                    " INNER JOIN " +
                    StepEntry.TABLE_NAME +
                    " ON " +
                    StepEntry.TABLE_NAME + "." + StepEntry.COLUMN_STEP_RECIPE_KEY +
                    " = " +
                    RecipeEntry.TABLE_NAME + "." + RecipeEntry.COLUMN_RECIPE_ID;

    public static final class RecipeEntry implements BaseColumns {
        // RecipeEntry
        // content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();

        public static final String TABLE_NAME = "recipes";
        public static final String COLUMN_RECIPE_ID = "id";
        public static final String COLUMN_RECIPE_NAME = "name";
        public static final String COLUMN_RECIPE_SERVINGS = "servings";
        public static final String COLUMN_RECIPE_IMAGE_URL = "image_url";
    }

    public static final class IngredientEntry implements BaseColumns{

        // IngredientEntry
        // content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).appendPath(PATH_INGREDIENTS).build();

        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_INGREDIENT_ID = "id";
        public static final String COLUMN_INGREDIENT_QUANTITY = "quantity";
        public static final String COLUMN_INGREDIENT_MEASURE = "measure";
        public static final String COLUMN_INGREDIENT_NAME = "name";
        public static final String COLUMN_INGREDIENT_RECIPE_KEY = "recipe_id";
    }

    public static final class StepEntry implements BaseColumns{

        // StepEntry
        // content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).appendPath(PATH_STEPS).build();

        public static final String TABLE_NAME = "steps";
        public static final String COLUMN_STEP_ID = "id";
        public static final String COLUMN_STEP_SHORT_DESCRIPTION = "short_description";
        public static final String COLUMN_STEP_DESCRIPTION = "description";
        public static final String COLUMN_STEP_VIDEO_URL = "video_url";
        public static final String COLUMN_STEP_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_STEP_RECIPE_KEY = "recipe_id";
    }
}

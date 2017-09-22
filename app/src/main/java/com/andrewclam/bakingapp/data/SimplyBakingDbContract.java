package com.andrewclam.bakingapp.data;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Andrew Lam
 * Multi-table SQLite practice
 */

public class SimplyBakingDbContract {

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
        public static final String COLUMN_FOREIGN_KEY_RECIPE_ID = "recipe_id";
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
        public static final String COLUMN_FOREIGN_KEY_RECIPE_ID = "recipe_id";
    }
}

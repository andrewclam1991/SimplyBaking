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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry;
import com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry;

import static com.andrewclam.bakingapp.data.RecipeDbContract.SET_TABLE_STATEMENT;

public class RecipeContentProvider extends ContentProvider {

    // Define final integer constants for the directory of plants and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int CODE_RECIPES = 100;
    public static final int CODE_RECIPE_WITH_ID = 101;
    public static final int CODE_INGREDIENTS = 200;
    public static final int CODE_INGREDIENT_WITH_ID = 201;
    public static final int CODE_STEPS = 300;
    public static final int CODE_STEP_WITH_ID = 301;

    // Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = RecipeContentProvider.class.getName();

    // Define a static buildUriMatcher method that associates URI's with their int match
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add URI matches

        // Recipe Paths
        uriMatcher.addURI(RecipeDbContract.AUTHORITY, RecipeDbContract.PATH_RECIPES,
                CODE_RECIPES);
        uriMatcher.addURI(RecipeDbContract.AUTHORITY, RecipeDbContract.PATH_RECIPES
                + "/#", CODE_RECIPE_WITH_ID);

        // Ingredient Paths
        uriMatcher.addURI(RecipeDbContract.AUTHORITY, RecipeDbContract.PATH_INGREDIENTS,
                CODE_INGREDIENTS);
        uriMatcher.addURI(RecipeDbContract.AUTHORITY, RecipeDbContract.PATH_INGREDIENTS
                + "/#", CODE_INGREDIENT_WITH_ID);

        // Step Paths
        uriMatcher.addURI(RecipeDbContract.AUTHORITY, RecipeDbContract.PATH_STEPS,
                CODE_STEPS);
        uriMatcher.addURI(RecipeDbContract.AUTHORITY, RecipeDbContract.PATH_STEPS
                + "/#", CODE_STEP_WITH_ID);
        return uriMatcher;
    }

    // Member variable for a RecipeDbHelper that's initialized in the onCreate() method
    private RecipeDbHelper mRecipeDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mRecipeDbHelper = new RecipeDbHelper(context);
        return true;
    }

    /***
     * Handles requests to insert a single new row of data
     *
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mRecipeDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned
        switch (match) {
            case CODE_RECIPES:
                // Insert new values into the database
                long recipeId = db.insert(
                       RecipeEntry.TABLE_NAME,
                        null,
                        values);

                if (recipeId > 0) {
                    returnUri = ContentUris.withAppendedId(RecipeEntry.CONTENT_URI, recipeId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case CODE_INGREDIENTS:
                // Insert new values into the database
                long ingredientId = db.insert(
                        IngredientEntry.TABLE_NAME,
                        null,
                        values);

                if (ingredientId > 0) {
                    returnUri = ContentUris.withAppendedId(RecipeEntry.CONTENT_URI, ingredientId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case CODE_STEPS:
                // Insert new values into the database
                long stepId = db.insert(
                        IngredientEntry.TABLE_NAME,
                        null,
                        values);

                if (stepId > 0) {
                    returnUri = ContentUris.withAppendedId(RecipeEntry.CONTENT_URI, stepId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    /***
     * Handles requests for data by URI
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mRecipeDbHelper.getReadableDatabase();

        // Create a SQLiteQueryBuilder to build the appropriate table
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            // Query for the recipes directory
            case CODE_RECIPES:
                queryBuilder.setTables(SET_TABLE_STATEMENT);

                retCursor = queryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_RECIPE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                queryBuilder.setTables(RecipeDbContract.RecipeEntry.TABLE_NAME);
                retCursor = queryBuilder.query(db,
                        projection,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }

    /***
     * Deletes a single row of data
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return number of rows affected
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // No implementation
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /***
     * Updates a single row of data
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return number of rows affected
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // No implementation
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {
        // No implementation
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

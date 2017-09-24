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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.andrewclam.bakingapp.data.RecipeDbContract.FavoriteEntry;
import com.andrewclam.bakingapp.data.RecipeDbContract.IngredientEntry;
import com.andrewclam.bakingapp.data.RecipeDbContract.RecipeEntry;
import com.andrewclam.bakingapp.data.RecipeDbContract.StepEntry;


public class RecipeDbHelper extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "simplyBaking.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    // SQL CREATE TABLE String
    // Create a table to hold the recipes data
    private final static String SQL_CREATE_RECIPES_TABLE =
            "CREATE TABLE " +
                    RecipeEntry.TABLE_NAME + " (" +
                    RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RecipeEntry.COLUMN_RECIPE_UID + " TEXT UNIQUE NOT NULL, " +
                    RecipeEntry.COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                    RecipeEntry.COLUMN_RECIPE_SERVINGS + " INTEGER NOT NULL, " +
                    RecipeEntry.COLUMN_RECIPE_IMAGE_URL + " TEXT, " +

                    "UNIQUE (" + RecipeEntry.COLUMN_RECIPE_UID + ") ON CONFLICT REPLACE "

                    +")";

    // Create a table to hold the ingredients data
    private final static String SQL_CREATE_INGREDIENTS_TABLE =
            "CREATE TABLE " +
                    IngredientEntry.TABLE_NAME + " (" +
                    IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    IngredientEntry.COLUMN_INGREDIENT_UID + " TEXT UNIQUE NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_NAME + " TEXT NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_MEASURE + " TEXT NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_QUANTITY + " REAL NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY + " INTEGER NOT NULL, " +

                    "UNIQUE (" + IngredientEntry.COLUMN_INGREDIENT_UID + ") ON CONFLICT REPLACE " +

                    "FOREIGN KEY (" + IngredientEntry.COLUMN_INGREDIENT_RECIPE_KEY + ") " +
                    "REFERENCES " + RecipeEntry.TABLE_NAME + "("+ RecipeEntry.COLUMN_RECIPE_UID + ") "
                    + "ON UPDATE NO ACTION "
                    + "ON DELETE SET NULL "

                    + ")";

    // Create a table to hold the steps data
    private final static String SQL_CREATE_STEPS_TABLE =
            "CREATE TABLE " +
                    StepEntry.TABLE_NAME + " (" +
                    StepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    StepEntry.COLUMN_STEP_UID + " TEXT UNIQUE NOT NULL, " +
                    StepEntry.COLUMN_STEP_NUM + " INTEGER NOT NULL, " +
                    StepEntry.COLUMN_STEP_SHORT_DESCRIPTION + " TEXT NOT NULL, " +
                    StepEntry.COLUMN_STEP_DESCRIPTION + " TEXT NOT NULL, " +
                    StepEntry.COLUMN_STEP_THUMBNAIL_URL + " TEXT, " +
                    StepEntry.COLUMN_STEP_VIDEO_URL + " TEXT, " +
                    StepEntry.COLUMN_STEP_RECIPE_KEY + " INTEGER NOT NULL, " +

                    "UNIQUE (" + StepEntry.COLUMN_STEP_UID + ") ON CONFLICT REPLACE " +

                    "FOREIGN KEY (" + StepEntry.COLUMN_STEP_RECIPE_KEY + ") " +
                    "REFERENCES " + RecipeEntry.TABLE_NAME + "("+ RecipeEntry.COLUMN_RECIPE_UID + ") "
                    + "ON UPDATE NO ACTION "
                    + "ON DELETE SET NULL "

                    + " )";

    // Create a table to hold the favorite data
    private final static String SQL_CREATE_FAVORITE_TABLE =
            "CREATE TABLE " +
                    FavoriteEntry.TABLE_NAME + " (" +
                    FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FavoriteEntry.COLUMN_FAVORITE_BOOL + " INTEGER DEFAULT 0, " +
                    FavoriteEntry.COLUMN_FAVORITE_RECIPE_KEY + " INTEGER NOT NULL, " +

                    "FOREIGN KEY (" + StepEntry.COLUMN_STEP_RECIPE_KEY + ") " +
                    "REFERENCES " + RecipeEntry.TABLE_NAME + "("+ RecipeEntry.COLUMN_RECIPE_UID + ") "
                    + "ON UPDATE NO ACTION "
                    + "ON DELETE SET NULL "

                    + " )";

    // Constructor
    public RecipeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_RECIPES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INGREDIENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STEPS_TABLE);
//        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // For now simply drop the table and create a new one.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                RecipeDbContract.RecipeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                RecipeDbContract.IngredientEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                RecipeDbContract.StepEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

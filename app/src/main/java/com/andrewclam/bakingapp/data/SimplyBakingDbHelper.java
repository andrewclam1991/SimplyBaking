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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.andrewclam.bakingapp.data.SimplyBakingDbContract.IngredientEntry;
import com.andrewclam.bakingapp.data.SimplyBakingDbContract.RecipeEntry;


public class SimplyBakingDbHelper extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "simplyBaking.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    // SQL CREATE TABLE String
    // Create a table to hold the recipes data
    private final String SQL_CREATE_RECIPES_TABLE =
            "CREATE TABLE " +
                RecipeEntry.TABLE_NAME + " (" +
                RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipeEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                RecipeEntry.COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                RecipeEntry.COLUMN_RECIPE_SERVINGS + " INTEGER NOT NULL, " +
                RecipeEntry.COLUMN_RECIPE_IMAGE_URL + " TEXT )";

    private final String SQL_CREATE_INGREDIENTS_TABLE =
            "CREATE TABLE " +
                IngredientEntry.TABLE_NAME + " (" +
                IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IngredientEntry.COLUMN_INGREDIENT_ID + " INTEGER NOT NULL, " +
                IngredientEntry.COLUMN_INGREDIENT_NAME + " TEXT NOT NULL, " +
                IngredientEntry.COLUMN_INGREDIENT_MEASURE + " TEXT NOT NULL, " +
                IngredientEntry.COLUMN_INGREDIENT_QUANTITY + " INTEGER NOT NULL )";

    private final String SQL_CREATE_STEPS_TABLE =
            "CREATE TABLE " +
                    IngredientEntry.TABLE_NAME + " (" +
                    IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    IngredientEntry.COLUMN_INGREDIENT_ID + " INTEGER NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_NAME + " TEXT NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_MEASURE + " INTEGER NOT NULL, " +
                    IngredientEntry.COLUMN_INGREDIENT_QUANTITY + " INTEGER NOT NULL )";

    // Constructor
    public SimplyBakingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_RECIPES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INGREDIENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STEPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // For now simply drop the table and create a new one.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                SimplyBakingDbContract.RecipeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                SimplyBakingDbContract.IngredientEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                SimplyBakingDbContract.StepEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

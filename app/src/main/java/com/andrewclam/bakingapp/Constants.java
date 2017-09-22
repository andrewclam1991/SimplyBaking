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

package com.andrewclam.bakingapp;

/**
 * Created by lamch on 9/15/2017.
 * Package-private constants for the application
 */

public class Constants {
    /* Package Name */
    public static final String PACKAGE_NAME = "com.andrewclam.bakingapp";

    /* Data Source URL */
    public static final String DATA_URL =
            "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    /* Intent Extra Key */
    public static final String EXTRA_RECIPE = PACKAGE_NAME + ".extra.recipe.object";

    /* EXTRA Key */
    public static final String EXTRA_RECIPE_LIST = PACKAGE_NAME + ".extra_recipe_list";


    /* App Widget Configuration Intent Action */
    final static String ACTION_APPWIDGET_CONFIG =
            "android.appwidget.action.APPWIDGET_CONFIGURE";

    private Constants() {
    }
}

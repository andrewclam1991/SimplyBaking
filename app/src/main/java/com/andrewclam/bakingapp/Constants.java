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
    static final String DATA_URL =
            "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    /* Intent Extra Key */
    static final String EXTRA_RECIPE = PACKAGE_NAME + ".extra.recipe.object";
    static final String EXTRA_RECIPE_NAME = PACKAGE_NAME + ".extra.recipe.name";
    public static final String EXTRA_RECIPE_LIST = PACKAGE_NAME + ".extra.recipe.list";
    public static final String EXTRA_RECIPE_ID = PACKAGE_NAME + ".extra.recipe.id";

    public static final String EXTRA_STEPS_LIST = PACKAGE_NAME + "extra.steps.list";
    static final String EXTRA_STEP_POSITION = PACKAGE_NAME + "extra.step.position";
    public static final String EXTRA_APP_WIDGET_ID = PACKAGE_NAME + ".extra.app.widget.ids";
    static final String EXTRA_RECIPE_STEP = PACKAGE_NAME + ".extra.recipe_step";
    static final String EXTRA_TWO_PANE_MODE = PACKAGE_NAME + ".extra.two_pane_mode";

    /* Intent Actions */
    static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    static final String ACTION_APPWIDGET_CONFIG =
            "android.appwidget.action.APPWIDGET_CONFIGURE";

    private Constants() {
    }
}

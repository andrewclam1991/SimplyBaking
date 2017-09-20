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

    private Constants() {
    }
}

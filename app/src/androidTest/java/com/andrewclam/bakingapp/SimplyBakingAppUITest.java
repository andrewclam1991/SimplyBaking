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

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * TODO [UI Test] Simple implementation to test ui elements of the app
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SimplyBakingAppUITest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    private IdlingRegistry mIdlingRegistry;
    private IdlingResource mIdlingResource;

    // Registers any resource that needs to be synchronized with Espresso before the test is run.
    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityRule.getActivity().getIdlingResource();

        if (mIdlingRegistry == null) mIdlingRegistry = IdlingRegistry.getInstance();
        mIdlingRegistry.register(mIdlingResource);
    }

    /**
     * (!) Change the constant recipe position value and the recipe name for testing,
     * Unless recipe data changes, refer to this table:
     * <p>
     * Recipe Num | Expected Recipe Name
     * 0            Nutella Pie
     * 1            Brownies
     * 2            Yellow Cake
     * 3            Cheesecake
     */

    private static final int RECIPE_NUM = 1;
    private static final String EXPECTED_RECIPE_NAME = "Brownies";

    private static final String STEP_0_INTRO_TITLE = "Recipe Introduction";

    @Test
    public void recipe_list_click_opens_recipe_detail_activity_test() {
        checkRecipeDetailActivityTitle(0, "Nutella Pie");
    }

    @Test
    public void recipe_detail_lists_exists_test() {
        checkRecipeDetailActivityTitle(1, "Brownies");
        checkIngredientListExists();
        checkStepListExists();
    }

    @Test
    public void recipe_detail_master_detail_flow_test() {
        checkRecipeDetailActivityTitle(2, "Yellow Cake");
        checkIngredientListExists();
        checkStepListExists();
        checkRecipeDetailIntroVideo();
    }

    @Test
    public void step_spinner_adapter_test() {
        checkRecipeDetailActivityTitle(3, "Cheesecake");
        checkIngredientListExists();
        checkStepListExists();
        checkRecipeDetailIntroVideo();
        checkSpinnerAdapterNavigation();
    }

    /**
     * Collection of all the above check to run in the last full test
     */
    @Test
    public void recipe_full_Test() {
        checkRecipeDetailActivityTitle(RECIPE_NUM, EXPECTED_RECIPE_NAME);
        checkIngredientListExists();
        checkStepListExists();
        checkRecipeDetailIntroVideo();
        checkSpinnerAdapterNavigation();
    }

    /**
     * This method tests whether clicking the the recipe list at the parameter positions
     * correctly opens the recipe detail activity with the expected title
     *
     * @param position          the recipe list position
     * @param assertRecipeTitle the expected recipe detail activity title
     */
    public static void checkRecipeDetailActivityTitle(int position, String assertRecipeTitle) {
        // First, scroll to the position that needs to be matched and click on it.
        onView(withId(R.id.recipe_list_rv))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));

        // Match the text in an item below the fold and check that it's displayed.
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(
                allOf(
                        isAssignableFrom(TextView.class),
                        withParent(isAssignableFrom(Toolbar.class))
                )
        ).check(matches(withText(assertRecipeTitle)));
    }

    public static void checkIngredientListExists() {
        onView(withId(R.id.ingredient_list_rv))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    public static void checkStepListExists() {
        onView(withId(R.id.step_list_rv))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    public static void checkRecipeDetailIntroVideo() {
        // perform click on the first element of the steps list,
        onView(withId(R.id.step_list_rv))
                .perform(scrollTo(),RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // should be in the step detail activity, check if it contains the exoPlayerview
        // and the description "Recipe Introduction", all recipes should have the intro video
        ViewInteraction frameLayout = onView(
                allOf(withId(R.id.step_video_player_view),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container),
                                        0),
                                0),
                        isDisplayed()));
        frameLayout.check(matches(isDisplayed()));

        onView(withId(R.id.step_detail_description_tv))
                .check(matches(withText(STEP_0_INTRO_TITLE)));
    }

    public static void checkSpinnerAdapterNavigation() {
        // For phone (both orientation) and (vertical) for wide screen devices
        // the view should also contain the step navigation adapter, check if it exists

        // Click the drop down spinner to show the spinner navigation
        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.steps_spinner),
                        withParent(allOf(withId(R.id.toolbar),
                                withParent(withId(R.id.appbar)))),
                        isDisplayed()));
        appCompatSpinner.perform(click());
    }

    // Remember to unregister resources when not needed to avoid malfunction.
    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            mIdlingRegistry.unregister(mIdlingResource);
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

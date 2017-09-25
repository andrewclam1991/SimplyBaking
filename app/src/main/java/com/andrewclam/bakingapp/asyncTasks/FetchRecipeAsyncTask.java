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

package com.andrewclam.bakingapp.asyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.andrewclam.bakingapp.models.Recipe;
import com.andrewclam.bakingapp.utils.NetworkUtils;
import com.andrewclam.bakingapp.utils.RecipeJsonUtil;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andrew Chi Heng Lam on 9/2/2017.
 * <p>
 * FetchRecipeAsyncTask
 * An implementation of the AsyncTask class to do network IO on a separate thread,
 */

public class FetchRecipeAsyncTask extends AsyncTask<Void, Void, ArrayList<Recipe>> {
    /* Debug Tag */
    private static final String TAG = FetchRecipeAsyncTask.class.getSimpleName();

    /* Listener to callback when the data is ready */
    private onFetchRecipeActionListener mListener;

    /* String of the URL to get the recipe */
    private String mDataURL;

    /* No-args default constructor */
    public FetchRecipeAsyncTask() {
    }

    /* Public Setters */
    public FetchRecipeAsyncTask setListener(onFetchRecipeActionListener mListener) {
        this.mListener = mListener;
        return this;
    }

    public FetchRecipeAsyncTask setDataURL(String dataURL) {
        this.mDataURL = dataURL;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Check for required parameter before doInBackground
        String msg = "";
        boolean hasError = false;

        if (mListener == null) {
            hasError = true;
            msg = msg.concat("Must set the mListener for this task." + "\n");
        }

        if (mDataURL == null) {
            hasError = true;
            msg = msg.concat("Must set the mDataURL for this task." + "\n");
        }

        if (hasError) {
            Log.e(TAG, msg);
            throw new IllegalArgumentException(msg);
        }

    }

    @Override
    protected ArrayList<Recipe> doInBackground(Void... voids) {
        // Init a arrayList to store the parsed movie entries
        ArrayList<Recipe> entries = new ArrayList<>();

        try {
            // Get the url required by the network util
            URL url = new URL(mDataURL);

            // Get httpResponse using the url
            String jsonResponse = NetworkUtils.getResponseFromHttpUrl(url);

            // Check for null response
            if (jsonResponse == null) return entries;

            // Got a JsonResponse from the web, parse the jsonResponse using the JsonUtils
            entries = RecipeJsonUtil.getRecipesFromJson(jsonResponse);

        } catch (IOException e) {
            Log.e(TAG, "FetchRecipeAsyncTask - doInBackground - " +
                    "IO Error occurred while getting the jsonResponse from the url");
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "FetchRecipeAsyncTask - doInBackground - " +
                    "JSONException occurred while parsing the jsonResponse into model class");
            e.printStackTrace();
            return null;
        }

        // return the entries
        return entries;
    }

    @Override
    protected void onPostExecute(ArrayList<Recipe> recipes) {
        super.onPostExecute(recipes);
        if (mListener != null) mListener.onRecipesReady(recipes);
    }

    /**
     * Interface for callback to the listener at stages where UI change is required
     * in preExecute and postExecute.
     */
    public interface onFetchRecipeActionListener {
        void onRecipesReady(ArrayList<Recipe> recipes);
    }
}

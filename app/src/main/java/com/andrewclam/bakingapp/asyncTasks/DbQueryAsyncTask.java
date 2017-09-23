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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Andrew Chi Heng Lam on 8/31/2017.
 * <p>
 * DbQueryAsyncTask
 * An implementation of the AsyncTask class to query database IO on a separate thread,
 */

public class DbQueryAsyncTask extends AsyncTask<Void, Void, Cursor> {
    /* Debug Tag */
    private static final String TAG = DbQueryAsyncTask.class.getSimpleName();

    /* Listener for callback */
    private OnCursorActionListener mListener;

    /* ContentResolver */
    private ContentResolver mContentResolver;

    /* Query Uri */
    private Uri mQueryUri;

    /* Optional Query Parameters*/
    /* Projection: of data column to return in the cursor*/
    private String[] mProjection = null;

    /* Selection: Column to select from the database*/
    private String mSelection = null;

    /* Selection Args: Arguments for the selection, if defined*/
    private String[] mSelectionArgs = null;

    /* Sort-order: The output cursor data sort order*/
    private String mSortOrder = null;

    /**
     * No-args constructor
     */
    private DbQueryAsyncTask() {

    }

    /**
     * Full-require-args constructor, taking all the required parameters
     *
     * @param contentResolver the application context's contentResolver
     * @param updateUri       the uri to the data to be updated on client's database
     */
    private DbQueryAsyncTask(ContentResolver contentResolver, Uri updateUri) {
        this.mContentResolver = contentResolver;
        this.mQueryUri = updateUri;
    }

    /* Public Setter methods */
    public DbQueryAsyncTask setContentResolver(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
        return this;
    }

    public DbQueryAsyncTask setQueryUri(Uri updateUri) {
        this.mQueryUri = updateUri;
        return this;
    }

    public DbQueryAsyncTask setProjection(String[] projection) {
        this.mProjection = projection;
        return this;
    }

    public DbQueryAsyncTask setSelection(String mSelection) {
        this.mSelection = mSelection;
        return this;
    }

    public DbQueryAsyncTask setSelectionArgs(String[] mSelectionArgs) {
        this.mSelectionArgs = mSelectionArgs;
        return this;
    }

    public DbQueryAsyncTask setSortOrder(String mSortOrder) {
        this.mSortOrder = mSortOrder;
        return this;
    }

    public DbQueryAsyncTask setListener(OnCursorActionListener mListener) {
        this.mListener = mListener;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Check for required parameter before doInBackground
        String msg = "";
        boolean hasError = false;

        if (mContentResolver == null) {
            hasError = true;
            msg = msg.concat("Must set the mContentResolver for this task." + "\n");
        }

        if (mQueryUri == null) {
            hasError = true;
            msg = msg.concat("Must set the mQueryUri for this task." + "\n");
        }

        if (hasError) {
            Log.e(TAG, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected Cursor doInBackground(Void... voids) {
        // Use the contentResolver to query the target uri with parameters
        // Return a cursor with the data;
        return mContentResolver.query(mQueryUri,
                mProjection,
                mSelection,
                mSelectionArgs,
                mSortOrder);
    }

    @Override
    protected void onPostExecute(Cursor dataCursor) {
        super.onPostExecute(dataCursor);
        if (mListener != null) mListener.onQueryComplete(dataCursor);
    }

    /**
     * Interface for callback to the listener at stages where UI change is required
     * postExecute to notify caller whether the contentResolver.update() was successful.
     */
    public interface OnCursorActionListener {
        void onQueryComplete(Cursor dataCursor);
    }
}

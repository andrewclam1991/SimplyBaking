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
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Andrew Chi Heng Lam on 8/31/2017.
 * <p>
 * DbUpdateAsyncTask
 * An implementation of the AsyncTask class to update database IO on a separate thread,
 */

public class DbUpdateAsyncTask extends AsyncTask<Void, Void, Integer> {
    /* Debug Tag */
    private static final String TAG = DbUpdateAsyncTask.class.getSimpleName();

    /* Listener for callback */
    private OnUpdateActionListener mListener;

    /* ContentResolver */
    private ContentResolver mContentResolver;

    /* Update Uri for the content resolver update method */
    private Uri mUpdateUri;

    /* Content values */
    private ContentValues mContentValues;

    /* Where Args for update, optional*/
    private String[] mWhereArgs = null;

    /* Where clause for update, optional*/
    private String mWhereClause = null;

    /**
     * No-args constructor
     */
    public DbUpdateAsyncTask() {

    }

    /**
     * Full-require-args constructor, taking all the required parameters
     *
     * @param contentResolver the application context's contentResolver
     * @param updateUri       the uri to the data to be updated on client's database
     * @param contentValues   the contentValues that contain the column value for the update
     */
    public DbUpdateAsyncTask(@NonNull ContentResolver contentResolver, @NonNull Uri updateUri, @NonNull ContentValues contentValues) {
        this.mContentResolver = contentResolver;
        this.mUpdateUri = updateUri;
        this.mContentValues = contentValues;
    }

    /* Public Setter methods */
    public DbUpdateAsyncTask setContentResolver(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
        return this;
    }

    public DbUpdateAsyncTask setUpdateUri(Uri updateUri) {
        this.mUpdateUri = updateUri;
        return this;
    }

    public DbUpdateAsyncTask setContentValues(ContentValues contentValues) {
        this.mContentValues = contentValues;
        return this;
    }

    public DbUpdateAsyncTask setWhereClause(String mWhereClause) {
        this.mWhereClause = mWhereClause;
        return this;
    }

    public DbUpdateAsyncTask setWhereArgs(String[] mWhereArgs) {
        this.mWhereArgs = mWhereArgs;
        return this;
    }

    public DbUpdateAsyncTask setListener(OnUpdateActionListener mListener) {
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

        if (mUpdateUri == null) {
            hasError = true;
            msg = msg.concat("Must set the mUpdateUri for this task." + "\n");
        }

        if (mContentValues == null) {
            hasError = true;
            msg = msg.concat("Must set the mContentValues for this task." + "\n");
        }

        if (hasError) {
            Log.e(TAG, msg);
            throw new IllegalArgumentException(msg);
        }
    }


    @Override
    protected Integer doInBackground(Void... voids) {
        // Use the contentResolver to update the target uri with the parameter contentValues
        // Positive return indicates the update was successful.
        return mContentResolver.update(mUpdateUri,
                mContentValues,
                mWhereClause,
                mWhereArgs);
    }

    @Override
    protected void onPostExecute(Integer rowsUpdated) {
        super.onPostExecute(rowsUpdated);
        if (mListener != null) mListener.onUpdateComplete(rowsUpdated);
    }

    /**
     * Interface for callback to the listener at stages where UI change is required
     * postExecute to notify caller whether the contentResolver.update() was successful.
     */
    public interface OnUpdateActionListener {
        void onUpdateComplete(Integer rowsUpdated);
    }
}

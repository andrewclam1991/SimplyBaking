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

package com.andrewclam.bakingapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andrewclam.bakingapp.R;
import com.andrewclam.bakingapp.models.Recipe;

import java.util.ArrayList;

/**
 * Created by Andrew Chi Heng Lam on 8/19/2017.
 * <p>
 * This is an implementation of the RecyclerViewAdapter, used to back the recyclerView
 * with the data, also contains an inner class that hold the cache of views.
 */

public class RecipeRecyclerViewAdapter extends RecyclerView.Adapter<RecipeRecyclerViewAdapter.RecipeViewHolder> {

    /* Log Tag */
    private final static String TAG = RecipeRecyclerViewAdapter.class.getSimpleName();

    /* Callback listener to handle user click on a recipe */
    private final OnRecipeItemClickedListener mOnItemClickedListener;

    /* Data, list of recipes */
    private ArrayList<Recipe> mRecipes;

    /* Context for getting application resources*/
    private final Context mContext;

    /* Default Constructor */
    public RecipeRecyclerViewAdapter(Context mContext, OnRecipeItemClickedListener mOnItemClickedListener) {
        this.mContext = mContext;
        this.mRecipes = new ArrayList<>();
        this.mOnItemClickedListener = mOnItemClickedListener;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent   The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (which ours doesn't) you
     *                 can use this viewType integer to provide a different layout. See
     *                 {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                 for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the layout inflater (required to inflate the itemView)
        Context context = parent.getContext(); // Context required by the layoutInflater
        LayoutInflater inflater = LayoutInflater.from(context);

        int layoutResId = R.layout.recipe_list_item;
        boolean shouldAttachToParentImmediately = false; // optional parameter to indicate exactly what it says

        // Uses the layoutId, viewGroup, boolean signature of the inflater.inflate()
        @SuppressWarnings("ConstantConditions")
        View view = inflater.inflate(layoutResId, parent, shouldAttachToParentImmediately);

        return new RecipeViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the item
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the
     *                 contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        /* This is where we bind data to the ViewHolder */

        // Get the posterPath info from the entry item at the adapter position
        Recipe recipe = mRecipes.get(position);
        String name = recipe.getName();
        int servings = recipe.getServings();

        // Call holder's set method to set the ui elements
        holder.setNameTv(name);
        holder.setServingTv(servings);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our movie entries
     */
    @Override
    public int getItemCount() {
        if (mRecipes == null || mRecipes.isEmpty()) return 0;
        return mRecipes.size();
    }

    /**
     * setRecipeData() updates the adapter's current data set
     *
     * @param mRecipes the new dataset that we want to update the adapter with
     */
    public void setRecipeData(ArrayList<Recipe> mRecipes) {
        this.mRecipes = mRecipes;
        notifyDataSetChanged();
    }

    /**
     * Callback Interface
     * Handle on itemClick event in each itemView inside the RecyclerView
     */
    public interface OnRecipeItemClickedListener {
        void onRecipeClicked(Recipe entry);
    }

    /**
     * RecipeViewHolder
     * an implementation of the RecyclerView.ViewHolder class that act as a cache fo the children
     * views for a single recipe item.
     */
    public class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView mNameTv;
        private final TextView mServingTv;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            // Reference the ui elements
            mNameTv = itemView.findViewById(R.id.recipe_name_tv);
            mServingTv = itemView.findViewById(R.id.recipe_servings_tv);

            // Set an onClickListener onto the itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 1) Get the current adapter position
                    int adapterPosition = getAdapterPosition();

                    // 2) Find the corresponding clicked entry in the entries
                    Recipe entry = mRecipes.get(adapterPosition);

                    // 3) Use onClickHandler to notify the activity of a onClick event
                    // pass in the retrieved object
                    mOnItemClickedListener.onRecipeClicked(entry);
                }
            });
        }

        /**
         * setNameTv takes the parameter name and sets it to the viewHolder
         *
         * @param name the recipe's name to be shown in the viewHolder
         */
        public void setNameTv(String name) {
            mNameTv.setText(name);
        }

        public void setServingTv(int servings) {
            mServingTv.setText(mContext.getString(R.string.serving, servings));
        }
    }
}

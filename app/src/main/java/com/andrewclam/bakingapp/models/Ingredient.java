package com.andrewclam.bakingapp.models;

import org.parceler.Parcel;

/**
 * Created by lamch on 9/14/2017.
 * The model class to store recipe's each ingredient data
 */

@Parcel(Parcel.Serialization.BEAN)
public class Ingredient {
    private double quantity;
    private String measure;
    private String ingredient;

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredientName() {
        return ingredient;
    }

    public void setIngredientName(String ingredient) {
        this.ingredient = ingredient;
    }
}

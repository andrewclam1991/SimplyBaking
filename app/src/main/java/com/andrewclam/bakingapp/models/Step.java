package com.andrewclam.bakingapp.models;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Created by lamch on 9/14/2017.
 * The model class to store recipe's each step data
 */

@Parcel(Parcel.Serialization.BEAN)
public class Step {
    private long id;
    private String shortDescription;
    private String description;
    private String videoURL;
    private String thumbnialURL;

    @ParcelConstructor
    public Step()
    {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getThumbnailURL() {
        return thumbnialURL;
    }

    public void setThumbnialURL(String thumbnialURL) {
        this.thumbnialURL = thumbnialURL;
    }
}

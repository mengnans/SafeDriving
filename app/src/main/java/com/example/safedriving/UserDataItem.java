package com.example.safedriving;

/**
 * Created by Dan on 23/09/2017.
 */

public class UserDataItem {

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("text")
    private String mText;

    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    /**
     * Indicates if the item is completed
     */
    @com.google.gson.annotations.SerializedName("complete")
    private boolean mComplete;

    @com.google.gson.annotations.SerializedName("latitude")
    private double mLat;
    @com.google.gson.annotations.SerializedName("latitudeStr")
    private String mLatString;

    @com.google.gson.annotations.SerializedName("longitude")
    private double mLong;
    @com.google.gson.annotations.SerializedName("longitudeStr")
    private String mLongString;

    @com.google.gson.annotations.SerializedName("mStreet")
    private String mStreet;

    @com.google.gson.annotations.SerializedName("mSpeed")
    private double mSpeed;
    @com.google.gson.annotations.SerializedName("mSpeedStr")
    private String mSpeedString;

    @com.google.gson.annotations.SerializedName("mLimit")
    private double mLimit;
    @com.google.gson.annotations.SerializedName("mLimitStr")
    private String mLimitString;

    /**
     * ToDoItem constructor
     */
    public UserDataItem() {

    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * Initializes a new ToDoItem
     *
     * @param text
     *            The item text
     * @param id
     *            The item id
     */
    public UserDataItem(String text, String id, double latitude, double longitude, String mStreet, double speed, double mLimit) {
        this.setText(text);
        this.setId(id);
        this.setLat(latitude);
        this.setLong(longitude);
        this.setmStreet(mStreet);
        this.setSpeed(speed);
        this.setLimit(mLimit);
    }

    public double getSpeed() { return mSpeed; }
    public String getmSpeedString() { return mSpeedString; }

    public double getLimit() { return mLimit; }
    public String getmLimitString() { return mLimitString; }

    public void setSpeed(double speed) {
        mSpeed = speed;
        mLimitString = Double.toString(speed);
    }

    public void setLimit(double limit) {
        mLimit = limit;
        mLimitString = Double.toString(limit);
    }

    public double getLat(){
        return mLat;
    }
    public String getmLatString() {
        return mLatString;
    }

    public double getLong(){
        return mLong;
    }
    public String getmLongString() {
        return mLongString;
    }

    public void setLat(double lat) {
        mLat = lat;
        mLatString = Double.toString(lat);
    }
    public void setLong(double longitude) {
        mLong = longitude;
        mLongString = Double.toString(longitude);
    }

    /**
     * Returns the item text
     */
    public String getText() {
        return mText;
    }

    /**
     * Sets the item text
     *
     * @param text
     *            text to set
     */
    public final void setText(String text) {
        mText = text;
    }

    public void setmStreet(String mStreet) {
        this.mStreet = mStreet;
    }

    public String getmStreet() {
        return mStreet;
    }

    /**
     * Returns the item id
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets the item id
     *
     * @param id
     *            id to set
     */
    public final void setId(String id) {
        mId = id;
    }

    /**
     * Indicates if the item is marked as completed
     */
    public boolean isComplete() {
        return mComplete;
    }

    /**
     * Marks the item as completed or incompleted
     */
    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserDataItem && ((UserDataItem) o).mId == mId;
    }
}
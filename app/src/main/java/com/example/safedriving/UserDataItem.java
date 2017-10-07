package com.example.safedriving;

import java.util.Date;

/**
 * This class contains speeding data generated from speeding instances
 * Each object represents a row in the Azure SQL table
 * Includes get/set methods
 *
 * @author Daniel Gray, Mengnan Shi, Stanley Sim
 *
 */

public class UserDataItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

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

    @com.google.gson.annotations.SerializedName("mAvgSpeed")
    private double mAvgSpeed;
    @com.google.gson.annotations.SerializedName("mAvgSpeedStr")
    private String mAvgSpeedString;

    @com.google.gson.annotations.SerializedName("mStartTime")
    private String startTime;

    @com.google.gson.annotations.SerializedName("mEndTime")
    private String endTime;


    @com.google.gson.annotations.SerializedName("mLimit")
    private double mLimit;
    @com.google.gson.annotations.SerializedName("mLimitStr")
    private String mLimitString;

    @com.google.gson.annotations.SerializedName("userId")
    private String mUserId;

    @com.google.gson.annotations.SerializedName("createdAt")
    private Date createdAt;


    public UserDataItem() {

    }

    public void setDate(Date date) { date = createdAt; }
    public String getDateString() { return createdAt.toString(); }

    public void setUserId(String id) { mUserId = id;}
    public String getUserId() { return mUserId; }

    public double getSpeed() { return mSpeed; }
    public String getmSpeedString() { return mSpeedString; }

    public double getLimit() { return mLimit; }
    public String getmLimitString() { return mLimitString; }

    public void setSpeed(double speed) {
        mSpeed = speed;
        mSpeedString = Double.toString(speed);
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

    public void setAvgSpeed(double avgSpeed){
        mAvgSpeed = avgSpeed;
        mAvgSpeedString = Double.toString(avgSpeed);
    }

    public double getAvgSpeed(){
        return this.mAvgSpeed;
    }

    public String getmAvgSpeedString(){
        return this.mAvgSpeedString;
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public String getStartTime(){
        return this.startTime;
    }

    public void setEndTime(String endTime){
        this.endTime = endTime;
    }

    public String getEndTime(){
        return this.endTime;
    }

    public void setmStreet(String mStreet) {
        this.mStreet = mStreet;
    }
    public String getmStreet() {
        return mStreet;
    }

    public String getId() {
        return mId;
    }
    public final void setId(String id) {
        mId = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserDataItem && ((UserDataItem) o).mId == mId;
    }
}
package com.example.safedriving;

/**
 * Created by Stanley on 04-Oct-17.
 */

public class UserItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("first-name")
    private String mFirstName;

    @com.google.gson.annotations.SerializedName("last-name")
    private String mLastName;


    public UserItem(String id, String firstname, String lastname){
        this.mId = id;
        this.mFirstName = firstname;
        this.mLastName = lastname;
    }

    public String getId(){
        return this.mId;
    }

    public String getFirstName(){
        return this.mFirstName;
    }

    public String getLastName(){
        return this.mLastName;
    }

}

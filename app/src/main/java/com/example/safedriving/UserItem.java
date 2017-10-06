package com.example.safedriving;

/**
 * This class contains user data generated at login
 * Each object represents a row in the Azure SQL table
 * Includes get/set methods
 *
 * @author Daniel Gray, Mengnan Shi, Stanley Sim
 *
 */

public class UserItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("mfirstname")
    private String mFirstName;

    @com.google.gson.annotations.SerializedName("mlastname")
    private String mLastName;

    public UserItem() {

    }

    public UserItem(String id, String firstname, String lastname){
        this.mId = id;
        this.mFirstName = firstname;
        this.mLastName = lastname;
    }

    public String getId(){
        return this.mId;
    }
    public void setId(String id) { mId = id; }

    public String getFirstName(){
        return this.mFirstName;
    }
    public void setFirstName(String firstname) { mFirstName = firstname; }

    public String getLastName(){
        return this.mLastName;
    }
    public void setLastName(String lastname) { mLastName = lastname; }


    @Override
    public boolean equals(Object o) {
        return o instanceof UserItem && ((UserItem) o).mId == mId;
    }
}

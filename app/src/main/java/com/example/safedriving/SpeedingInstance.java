package com.example.safedriving;

import java.util.ArrayList;

public class SpeedingInstance {

    private String startTime;
    private String endTime;
    private String street;
    private double estLong;
    private double estLat;
    private double speedLimit;
    private ArrayList<Double> speed;
    private double maxSpeed;

    public SpeedingInstance (String startTime, String street, double lat, double lng, double speedlimit
            , double speed){
        this.startTime = startTime;
        this.endTime = startTime;
        this.street = street;
        this.estLong = lng;
        this.estLat = lat;
        this.speedLimit = speedlimit;
        this.speed = new ArrayList<Double>();
        this.speed.add(speed);
        this.maxSpeed = speed;
    }

    private boolean checkData (String street, double speedLimit){
        if( startTime == null){
            return false;
        }

        if(street != this.street){
            return false;
        }
        if(speedLimit != this.speedLimit){
            return false;
        }

        return true;
    }

    public boolean updateData(String street, double speedLimit, double speed,String time){
        if(checkData(street, speedLimit)){
            this.speed.add(speed);
            if(speed > this.maxSpeed){
                this.maxSpeed = speed;
            }
            this.endTime = time;
            return true;
        }else{
            return false;
        }
    }

    public double getAvgSpeed(){
        double sum = 0.0;
        for(int i = 0 ; i < this.speed.size();i++ ){
            sum += this.speed.get(i);
        }
        return sum/this.speed.size();
    }

    public double getMaxSpeed(){
        return this.maxSpeed;
    }

    public double getEstLong(){
        return this.estLong;
    }

    public double getEstLat(){
        return this.estLat;
    }

    public double getSpeedLimit(){
        return this.speedLimit;
    }

    public String getEndTime(){
        return this.endTime;
    }

    public String getStreet(){
        return this.street;
    }

    public String getStartTime(){
        return this.startTime;
    }

}

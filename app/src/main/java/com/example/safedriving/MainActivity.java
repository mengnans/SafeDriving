package com.example.safedriving;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button getSpeedButton = (Button) this.findViewById(R.id.button8);
        final TextView speedView = (TextView) this.findViewById(R.id.textView);
        getSpeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getSpeed(speedView);
                Toast.makeText(getApplicationContext(), "start",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getSpeed(final TextView speedView) {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates

        LocationListener locationListener =
                new LocationListener() {
                    private Location mLastLocation = null;

                    @Override
                    public void onLocationChanged(Location pCurrentLocation) {
                        double speed = 0;
                        Log.i("Stone","longitude: "+pCurrentLocation.getLongitude());
                        Log.i("Stone","latitude: "+pCurrentLocation.getLatitude());
                        Toast.makeText(getApplicationContext(),"" + pCurrentLocation.getLongitude() + ", " + pCurrentLocation.getLatitude() ,Toast.LENGTH_SHORT).show();
                        if (this.mLastLocation != null){
                            speed = 1000 * getDistance(mLastLocation,pCurrentLocation)
                                    / (pCurrentLocation.getTime() - this.mLastLocation.getTime());
                            Log.i("Stone","distance " + getDistance(mLastLocation,pCurrentLocation));
                            Log.i("Stone","time " + (pCurrentLocation.getTime() - this.mLastLocation.getTime()));
                            // from meter per second to km per hour
                            speed *= 3.6;
                        }
                        if (pCurrentLocation.hasSpeed())
                            speed = pCurrentLocation.getSpeed();
                        this.mLastLocation = pCurrentLocation;
                        speedView.setText("Speed is " + speed +"km/h");
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                    /**
                     * Calculate distance between two points in latitude and longitude taking
                     * into account height difference. If you are not interested in height
                     * difference pass 0.0. Uses Haversine method as its base.
                     *
                     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
                     * el2 End altitude in meters
                     * @returns Distance in Meters
                     */
                    private double getDistance(Location mLastLocation, Location pCurrentLocation) {
                        if(mLastLocation == null || pCurrentLocation == null){
                            return 0;
                        }
                        double lat1 = mLastLocation.getLatitude();
                        double lat2 = pCurrentLocation.getLatitude();
                        double lon1 = mLastLocation.getLongitude();
                        double lon2 = pCurrentLocation.getLongitude();
                        double el1 = mLastLocation.getAltitude();
                        double el2 = pCurrentLocation.getAltitude();

                        final int R = 6371; // Radius of the earth

                        double latDistance = Math.toRadians(lat2 - lat1);
                        double lonDistance = Math.toRadians(lon2 - lon1);
                        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                        double distance = R * c * 1000; // convert to meters

                        double height = el1 - el2;

                        distance = Math.pow(distance, 2) + Math.pow(height, 2);

                        return Math.sqrt(distance);
                    }
                };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            while (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_CONTACTS
                );
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, locationListener);

    }
}


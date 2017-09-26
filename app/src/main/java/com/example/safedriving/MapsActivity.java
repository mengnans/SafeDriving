package com.example.safedriving;

import android.app.DownloadManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//daniel is here
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    public void setNewPlaceMarker(Place place){
        mMap.clear();
        LatLng latLng = place.getLatLng();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in " + place.getName()));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }


}

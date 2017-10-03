package com.example.safedriving;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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


    public void setDestionationPlaceMarker(Place place) {
        mMap.clear();
        LatLng latLng = place.getLatLng();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in " + place.getName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }

    public void setCurrentPositionMaker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }

    public String getAddress(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            add += obj.toString();

            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    public void drawPolyline(String response) {
        try {
            //Tranform the string into a json object
            JSONObject json = new JSONObject(response);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );

            boolean hasPoints = false;
            Double maxLat = null, minLat = null, minLon = null, maxLon = null;

            if (polyline != null && polyline.getPoints() != null) {
                List<LatLng> pts = polyline.getPoints();
                for (LatLng coordinate : pts) {
                    // Find out the maximum and minimum latitudes & longitudes
                    // Latitude
                    maxLat = maxLat != null ? Math.max(coordinate.latitude, maxLat) : coordinate.latitude;
                    minLat = minLat != null ? Math.min(coordinate.latitude, minLat) : coordinate.latitude;

                    // Longitude
                    maxLon = maxLon != null ? Math.max(coordinate.longitude, maxLon) : coordinate.longitude;
                    minLon = minLon != null ? Math.min(coordinate.longitude, minLon) : coordinate.longitude;

                    hasPoints = true;
                }
            }

            if (hasPoints) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(maxLat, maxLon));
                builder.include(new LatLng(minLat, minLon));
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
            }

        } catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


}



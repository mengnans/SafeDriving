package com.example.safedriving;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.windowsazure.mobileservices.MobileServiceActivityResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class SafeDrivingHomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = SafeDrivingHomeActivity.class.getSimpleName();

    private final static MapsActivity mapsActivity = new MapsActivity();

    private LinearLayout dashboardLinearLayout;
    private LinearLayout homeLinearLayout;
    private LinearLayout waitingLinearLayout;
    private LinearLayout notificationLayout;

    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;
    private SharedPreferences sharedPreferences;

    private MobileServiceClient mClient;
    private MobileServiceTable<UserDataItem> mToDoTable;
    private UserDataItemAdapter mAdapter;
    public static final int GOOGLE_LOGIN_REQUEST_CODE = 1;
    private Location oldLocation;
    private Place destinationPlace;

    private double speedLimit = -2;
    private String currentStreetName;
    private String currentDirection = "";

    private SensorManager sm;
    private Sensor aSensor;
    private Sensor mSensor;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    private float currentIndex = -555;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            hideAll();
            switch (item.getItemId()) {

                case R.id.navigation_home:
                    showMap();
                    return true;
                case R.id.navigation_dashboard:
                    showDashBoard();
                    return true;
                case R.id.navigation_notifications:
                    showNotification();
                    //dashboardLinearLayout.setVisibility(LinearLayout.GONE);
                    return true;
            }
            return false;
        }
    };

    /**
     * hide all other components
     */
    private void hideAll() {
        dashboardLinearLayout.setVisibility(LinearLayout.GONE);
        homeLinearLayout.setVisibility(LinearLayout.GONE);
        notificationLayout.setVisibility(LinearLayout.GONE);
    }

    private void showMap() {
        hideAll();
        homeLinearLayout.setVisibility(LinearLayout.VISIBLE);
    }

    private void showDashBoard() {
        hideAll();
        dashboardLinearLayout.setVisibility(LinearLayout.VISIBLE);
        final Button getSpeedButton = (Button) this.findViewById(R.id.get_start_button);
        final TextView speedView = (TextView) this.findViewById(R.id.textView_dashboard_speed);
        final TextView speedLimiView = (TextView) this.findViewById(R.id.textView_dashboard_speed_limit);
        final TextView directionView = (TextView) this.findViewById(R.id.textView_dashboard_direction);
        final TextView streetNameView = (TextView) this.findViewById(R.id.textView_dashboard_street_name);


        getSpeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start(speedView,speedLimiView,directionView,streetNameView);
                calculateOrientation(directionView);
                Toast.makeText(getApplicationContext(), "start",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNotification() {
        hideAll();
        notificationLayout.setVisibility(LinearLayout.VISIBLE);
        refreshItemsFromTable();
    }


    @Override
    protected void onPause() {
        sm.unregisterListener(myListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, mSensor,SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_safe_driving_index);

        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, mSensor,SensorManager.SENSOR_DELAY_NORMAL);

        dashboardLinearLayout = (LinearLayout) this.findViewById(R.id.dashboard_linear_layout);
        homeLinearLayout = (LinearLayout) this.findViewById(R.id.home_linear_layout);
        waitingLinearLayout = (LinearLayout) this.findViewById(R.id.waiting_linear_layout);
        notificationLayout = (LinearLayout) this.findViewById(R.id.notification_linear_layout);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(mapsActivity);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mapsActivity.setDestionationPlaceMarker(place);
                destinationPlace = place;
                Log.i(LOG_TAG, "Place: " + place.getName());

            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "An error occurred: " + status);
            }
        });


        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://safedriving.azurewebsites.net",
                    this);

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            //TODO userdata correct table name??
            mToDoTable = mClient.getTable("userdata", UserDataItem.class);
            initLocalStore().get();
            mAdapter = new UserDataItemAdapter(this, R.layout.row_list_to_do);
            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
            listViewToDo.setAdapter(mAdapter);

            //addItem();         authenticate();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

    }

    public void showContent() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.visualizer_menu, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void start(final TextView speedView,final TextView speedLimitView,final TextView directionView,final TextView streetNameView) {

        /**show loading screen*/
        waitingLinearLayout.setVisibility(LinearLayout.VISIBLE);
        speedView.setVisibility(View.GONE);
        directionView.setVisibility(View.GONE);
        streetNameView.setVisibility(View.GONE);
        speedLimitView.setVisibility(View.GONE);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates


        LocationListener locationListener =
                new LocationListener() {
                    boolean hasRoute = false;

                    @Override
                    public void onLocationChanged(Location newLocation) {


                        /**calculate the speed*/
                        double speed = 0;
                        Log.i(LOG_TAG, "longitude: " + newLocation.getLongitude());
                        Log.i(LOG_TAG, "latitude: " + newLocation.getLatitude());

                        Toast.makeText(getApplicationContext(), "" + newLocation.getLongitude() + ", " + newLocation.getLatitude(), Toast.LENGTH_SHORT).show();

                        String unit_of_measurement = sharedPreferences.getString(getString(R.string.pref_unit_of_measurement_key),
                                getString(R.string.pref_unit_of_measurement_default_value));

                        /**if it has speed*/
                        if (newLocation.hasSpeed()) {
                            speed = newLocation.getSpeed();
                        }
                        /**if we have previous location, then calculate the speed */
                        else if (oldLocation != null) {
                            Log.i(LOG_TAG, "distance: " + newLocation.distanceTo(oldLocation));
                            Log.i(LOG_TAG, "time: " + (newLocation.getTime() - oldLocation.getTime()));
                            speed = newLocation.distanceTo(oldLocation) / (newLocation.getTime() - oldLocation.getTime());
                            speed *= 1000;
                        }

                        /** if has speed limit*/
                        if(speedLimit != -2){
                            /**if overspeed*/
                            if(speed*3.6 > speedLimit){
                                //TODO: generate warning data
                                Log.e(LOG_TAG,"your speed is " + speed*3.6);
                                Log.e(LOG_TAG,"speed limit is " + speedLimit);
                            }
                        }
                        /** from meter per second to km per hour */
                        if (getString(R.string.pref_km_per_hour_value).equals(unit_of_measurement)) {
                            speed *= 3.6;
                        }



                        /** set the location on map*/
                        mapsActivity.setCurrentPositionMaker(newLocation);

                        /** get current location name */
//                        String streetName = mapsActivity.getAddress(SafeDrivingHomeActivity.this, oldLocation.getLatitude(), oldLocation.getLongitude());

//                        streetNameView.setText(streetName);


                        /** get speed limit */
                        if(oldLocation !=null){
                            getSpeedLimit(newLocation, oldLocation,speedLimitView,streetNameView);
                        }


                        /** set the route on map for the first time*/
                        if (!hasRoute) {
                            /**hide loading screen*/
                            waitingLinearLayout.setVisibility(LinearLayout.GONE);
                            speedView.setVisibility(View.VISIBLE);
                            directionView.setVisibility(View.VISIBLE);
                            streetNameView.setVisibility(View.VISIBLE);
                            speedLimitView.setVisibility(View.VISIBLE);
                            startRouting();
                            hasRoute = true;
                        }

                        /**save the latest position*/
                        oldLocation = newLocation;


                        String speedString = getNumberString(speed);

                        speedView.setText("Speed is " + speedString + unit_of_measurement);
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
                };

        /**get the permission*/
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

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void authenticate() {
        // Login using the Google provider.
        mClient.login("Google", "safedriving", GOOGLE_LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When request completes
        if (resultCode == RESULT_OK) {
            // Check the request code matches the one we send in the login request
            if (requestCode == GOOGLE_LOGIN_REQUEST_CODE) {
                MobileServiceActivityResult result = mClient.onActivityResult(data);
                if (result.isLoggedIn()) {
                    // login succeeded
                    createAndShowDialog(String.format("You are now logged in - %1$2s", mClient.getCurrentUser().getUserId()), "Success");
//                    showContent();

                } else {
                    // login failed, check the error message
                    String errorMessage = result.getErrorMessage();
                    createAndShowDialog(errorMessage, "Error");
                }
            }
        }
    }

    //part of setting up database interactions
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("latitudeStr", ColumnDataType.String);
                    tableDefinition.put("longitudeStr", ColumnDataType.String);
                    tableDefinition.put("mStreet", ColumnDataType.String);
                    tableDefinition.put("mSpeedStr", ColumnDataType.String);
                    tableDefinition.put("mLimitStr", ColumnDataType.String);

                    //TODO table name??
                    localStore.defineTable("userdata", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    //is called from initLocalStore()
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    //is called from initLocalStore()
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    //add userdata to database
    //TODO addItem(View view) is original
    public void addItem() {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final UserDataItem item = new UserDataItem();

        item.setLat(69);
        item.setLimit(80);
        item.setSpeed(150);
        item.setLong(101);
        item.setmStreet("kimmy street");
        //item.setText(mTextNewToDo.getText().toString());
        //item.setComplete(false);

        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final UserDataItem entity = addItemInTable(item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //if(!entity.isComplete()){

                            mAdapter.add(entity);
                            //}
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);

        //mTextNewToDo.setText("");
    }

    //add userdata to database
    public UserDataItem addItemInTable(UserDataItem item) throws ExecutionException, InterruptedException {
        UserDataItem entity = mToDoTable.insert(item).get();
        return entity;
    }

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<UserDataItem> results = refreshItemsFromMobileServiceTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();

                            for (UserDataItem item : results) {
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    private List<UserDataItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mToDoTable.where().execute().get();
        //return mToDoTable.where().field("complete").eq(val(false)).execute().get();
    }


    public void startRouting() {
        // Instantiate the RequestQueue.
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        RequestQueue queue = Volley.newRequestQueue(this);
        try{
        url += "" + oldLocation.getLatitude();
        url += "," + oldLocation.getLongitude() + "&destination=";
        url += destinationPlace.getName() + "&key=" + getString(R.string.google_maps_key);
//        url += "melbourne" + "&key=" + getString(R.string.google_maps_key);
        }catch(Exception exception){
            Toast.makeText(this,"You don't have a destination", Toast.LENGTH_SHORT);
            return;
        }
        Log.i(LOG_TAG, "" + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mapsActivity.drawPolyline(response);
                        showMap();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(LOG_TAG, "No Response");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getSpeedLimit(final Location currentLocation, Location oldLocation, final TextView speedLimitView, final TextView streetNameView){
        double lowLng = currentLocation.getLongitude();
        double lowLat = currentLocation.getLatitude();
        String url ="http://maps.google.com/maps/api/geocode/json?latlng="
                +lowLat+","+lowLng + "&sensor=false";
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.i(LOG_TAG, "" + url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JsonParser parser = new JsonParser();
                            JsonElement element = parser.parse(response);
                            JsonObject object = element.getAsJsonObject();
                            JsonObject results = object.getAsJsonArray("results").get(0).getAsJsonObject();
                            JsonObject route = results.getAsJsonArray("address_components").get(1).getAsJsonObject();
                            String long_name = route.get("long_name").toString();
                            long_name = long_name.substring(1,long_name.length()-1);
                            currentStreetName = long_name;
                            streetNameView.setText("You are on the " + currentStreetName);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "error street name response");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        double maxLng = oldLocation.getLongitude();
        double maxLat = oldLocation.getLatitude();
        if(lowLat > maxLat){
            double temp = lowLat;
            lowLat = maxLat;
            maxLat = temp;
        }
        if(lowLng > maxLng){
            double temp = lowLng;
            lowLng = maxLng;
            maxLng = temp;
        }

        url = "http://www.overpass-api.de/api/xapi?*[maxspeed=*][bbox=";
        try{
            url += "" + lowLng;
            url += "," + lowLat;
            url += "," + maxLng;
            url += "," + maxLat + "]";
        }catch(Exception exception){
            Toast.makeText(this,"url error", Toast.LENGTH_SHORT);
        }
        Log.i(LOG_TAG, "" + url);

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8.name()));
                            Document doc = dBuilder.parse(stream);

                            doc.getDocumentElement().normalize();


                            NodeList nList = doc.getElementsByTagName("tag");

                            Element previousOne = null;
                            for (int temp = 0; temp < nList.getLength(); temp++) {


                                Node nNode = nList.item(temp);
                                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) nNode;


                                    String k = eElement.getAttribute("k");
                                    String v = eElement.getAttribute("v");

                                    if("name".equals(k)){
                                        String name = v;
                                        if(currentStreetName != null){
                                            if(!currentStreetName.equals(name)){
                                                Log.e(LOG_TAG,"name" + name);
                                                Log.e(LOG_TAG,"currentStreetName" + currentStreetName);
                                                break;
                                            }
                                        }
                                        Log.e(LOG_TAG,"correctName" + name);
                                        Log.e(LOG_TAG,"currentStreetName" + currentStreetName);
                                        v = previousOne.getAttribute("v");
                                        String speedString = v;
                                        double speed = Double.parseDouble(speedString);

                                        String unit_of_measurement = sharedPreferences.getString(getString(R.string.pref_unit_of_measurement_key),
                                                getString(R.string.pref_unit_of_measurement_default_value));

                                        speedLimit = speed;

                                        /** from km per hour to meter per second */
                                        if (getString(R.string.pref_m_per_second_value).equals(unit_of_measurement)) {
                                            speed /= 3.6;
                                        }

                                        speedString = getNumberString(speed);
                                        speedLimitView.setText("Your speed limit is " + speedString + "" + unit_of_measurement);

                                    break;
                                    }

                                    previousOne = eElement;

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "error speed limit response");
                if(error != null) {
                    Log.e(LOG_TAG, " " + error.getMessage());
                }
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    public String getNumberString(double speed){
        DecimalFormat df = new DecimalFormat("######0.00");
        return df.format(speed);
    }

    final SensorEventListener myListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = sensorEvent.values;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = sensorEvent.values;
            TextView textView = (TextView) findViewById(R.id.textView_dashboard_direction);
            if(textView == null){
                // do nothing
            }else{
                calculateOrientation(textView);
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void calculateOrientation(final TextView directionView) {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        float newIndex = values[0];
        if(currentIndex == -555){
            currentIndex = newIndex;
        }else{
            float difference = newIndex - currentIndex;
            currentIndex = newIndex;
            if(difference < 0){
                difference = -difference;
            }
            Log.e(LOG_TAG,"" + difference);
            if(difference >= 30){
                if(oldLocation != null){
                    // TODO: generate warning data
                    Log.e(LOG_TAG,"turning too fast on " + oldLocation.getLatitude() + "," + oldLocation.getLongitude());
                }



            }
        }
        Log.i(LOG_TAG, values[0]+"");

        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);
        String direction = "";
        if(values[0] >= -5 && values[0] < 5){
            Log.v(LOG_TAG, "North");
            direction = "North";
        }
        else if(values[0] >= 5 && values[0] < 85){
            Log.v(LOG_TAG, "North East");
            direction = "North East";
        }
        else if(values[0] >= 85 && values[0] <=95){
            Log.v(LOG_TAG, "East");
            direction = "East";
        }
        else if(values[0] >= 95 && values[0] <175){
            Log.v(LOG_TAG, "South East");
            direction = "South East";
        }
        else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
            Log.v(LOG_TAG, "South");
            direction = "South";
        }
        else if(values[0] >= -175 && values[0] <-95){
            Log.v(LOG_TAG, "South West");
            direction = "South West";
        }
        else if(values[0] >= -95 && values[0] < -85){
            Log.v(LOG_TAG, "West");
            direction = "West";
        }
        else if(values[0] >= -85 && values[0] <-5){
            Log.v(LOG_TAG, "North West");
            direction = "North West";
        }

        if("".equals(currentDirection)){
            currentDirection = direction;
        }else if(currentDirection.equals(direction)){
            // do nothing
        }else{
            currentDirection = direction;
            directionView.setText("Your direction is " + direction);
        }



    }




}

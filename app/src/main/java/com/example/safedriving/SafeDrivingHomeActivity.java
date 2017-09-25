package com.example.safedriving;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.google.android.gms.maps.SupportMapFragment;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

public class SafeDrivingHomeActivity extends AppCompatActivity {

    private LinearLayout dashboardLinearLayout;
    private LinearLayout homeLinearLayout;
    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;
    private SharedPreferences sharedPreferences;

    private MobileServiceClient mClient;
    private MobileServiceTable<UserDataItem> mToDoTable;
    private UserDataItemAdapter mAdapter;
    public static final int GOOGLE_LOGIN_REQUEST_CODE = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            hideAll();
            switch (item.getItemId()) {

                case R.id.navigation_home:
                    homeLinearLayout.setVisibility(LinearLayout.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    showDashBoard();
                    return true;
                case R.id.navigation_notifications:
                    dashboardLinearLayout.setVisibility(LinearLayout.GONE);
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
    }

    private void showDashBoard() {

        dashboardLinearLayout.setVisibility(LinearLayout.VISIBLE);
        final Button getSpeedButton = (Button) this.findViewById(R.id.button8);
        final TextView speedView = (TextView) this.findViewById(R.id.textView_dashboard);
        getSpeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getSpeed(speedView);
                Toast.makeText(getApplicationContext(), "start",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_safe_driving_index);

        dashboardLinearLayout = (LinearLayout) this.findViewById(R.id.dashboard_linear_layout);
        homeLinearLayout = (LinearLayout) this.findViewById(R.id.home_linear_layout);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);


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

//            mToDoTable = mClient.getTable(UserDataItem.class);
//            initLocalStore().get();
//            mAdapter = new UserDataItemAdapter(this, R.layout.row_list_to_do);
//            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
//            listViewToDo.setAdapter(mAdapter);

//            authenticate();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

    }

    public void showContent() {
        dashboardLinearLayout = (LinearLayout) this.findViewById(R.id.dashboard_linear_layout);
        homeLinearLayout = (LinearLayout) this.findViewById(R.id.home_linear_layout);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new MapsActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                        Log.i("Stone", "longitude: " + pCurrentLocation.getLongitude());
                        Log.i("Stone", "latitude: " + pCurrentLocation.getLatitude());

                        Toast.makeText(getApplicationContext(), "" + pCurrentLocation.getLongitude() + ", " + pCurrentLocation.getLatitude(), Toast.LENGTH_SHORT).show();

                        String unit_of_measurement = sharedPreferences.getString(getString(R.string.pref_unit_of_measurement_key),
                                getString(R.string.pref_unit_of_measurement_default_value));

                        /**if it has speed*/
                        if (pCurrentLocation.hasSpeed()){
                            speed = pCurrentLocation.getSpeed();
                        }
                        /**if we have previous location, then calculate the speed */
                        else if (this.mLastLocation != null) {
                            speed =  pCurrentLocation.distanceTo(mLastLocation) / (pCurrentLocation.getTime() - this.mLastLocation.getTime());
                        }

                        // from meter per second to km per hour
                        if (getString(R.string.pref_km_per_hour_value).equals(unit_of_measurement)) {
                            speed *= 3.6;
                        }

                        this.mLastLocation = pCurrentLocation;


                        speedView.setText("Speed is " + speed + unit_of_measurement);
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
                        if (mLastLocation == null || pCurrentLocation == null) {
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
                    showContent();

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
                    //TODO create table columns
                    //tableDefinition.put("id", ColumnDataType.String);
                    //tableDefinition.put("text", ColumnDataType.String);
                    //tableDefinition.put("complete", ColumnDataType.Boolean);

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
    public void addItem(View view) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final UserDataItem item = new UserDataItem();

        //item.setText(mTextNewToDo.getText().toString());
        //item.setComplete(false);

        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
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

}

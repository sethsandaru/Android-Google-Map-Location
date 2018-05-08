package com.sethphat.seth_googlemap_location;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PendingResult;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.sethphat.seth_googlemap_location.HttpRequest.HttpRequestHelper;
import com.sethphat.seth_googlemap_location.HttpRequest.ImageHelper;
import com.sethphat.seth_googlemap_location.Models.InfoLocation;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private ArrayList<String> listLocation = new ArrayList<String>();
    private GoogleMap map = null;
    private boolean canUseModule = true;
    private GeoApiContext apiContext;
    private LatLng myLocation;

    // define...
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private ArrayList<Marker> founded_place = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_layout);

        // create api
        apiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_location_api_key)).build();

        // add location that supported
        listLocation.add("ATM");
        listLocation.add("Hospital");
        listLocation.add("Bus station");
        listLocation.add("Shopping mall");
        listLocation.add("Gym");
        listLocation.add("Cafe");
        listLocation.add("Bank");

        // Get map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_marker);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /*
    Load map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // settings
        map.getUiSettings().setZoomControlsEnabled(true);

        // check location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            getDeviceLocation();
        }
        else {
            Toast.makeText(this, "Please grant us the permission to use your location, otherwise you can't use this module!", Toast.LENGTH_SHORT).show();
            canUseModule = false;
        }


        // event
        map.setOnMapLongClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mnAdd:
                chooseLocation();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Choose freaking location for map
     */
    private void chooseLocation() {
        if (canUseModule == false)
        {
            Toast.makeText(this, "You can't use this module, please grant location permission first!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.dialog_location, null);

        // get item in this
        final Spinner spnLocation = (Spinner) v.findViewById(R.id.spnLocation);
        final EditText txtRadius = (EditText) v.findViewById(R.id.txtRadius);

        // set data for spinner
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listLocation);
        spnLocation.setAdapter(locationAdapter);

        // set view
        dialog.setView(v);

        AlertDialog.Builder select = dialog.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int x) {
                int radius = 0;
                if (txtRadius.getText().toString().isEmpty() == false)
                    radius = Integer.parseInt(txtRadius.getText().toString());

                // clear map
                map.clear();

                PlaceType type = getType((String) spnLocation.getSelectedItem());

                // current location
                Location location = mLastKnownLocation;

                // request API Places now
                NearbySearchRequest placesApi = PlacesApi.nearbySearchQuery(apiContext, new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude()));
                placesApi.radius(radius);
                placesApi.type(type);
                placesApi.setCallback(new PendingResult.Callback<PlacesSearchResponse>() {
                    @Override
                    public void onResult(final PlacesSearchResponse result) {
                        LocationActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LocationActivity.this.addToMapByAPI(result.results);
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Throwable e) {
                        LocationActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LocationActivity.this, "Failed to get location, error code #1:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                //String lattLong = location.getLatitude() + "," + location.getLongitude();
                /*
                // query now
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                url = String.format(url + "key=%s&location=%s&radius=%d&type=%s", getString(R.string.google_location_api_key), lattLong, radius, type);

                HttpRequestHelper request = new HttpRequestHelper(new HttpRequestHelper.AsyncResponse() {
                    @Override
                    public void processFinished(String output) {
                        LocationActivity.this.addToMap(output);
                    }
                });
                request.execute(url);
                */
            }
        });

        dialog.setNegativeButton("Cancel", null);

        dialog.show();
    }

    private void addToMapByAPI(PlacesSearchResult[] results)
    {
        if (results.length == 0)
        {
            Toast.makeText(LocationActivity.this, "No nearby found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // fetch
        founded_place.clear();
        for (PlacesSearchResult item : results)
        {
            double lat = item.geometry.location.lat;
            double lng = item.geometry.location.lng;

            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(item.name).snippet(item.vicinity));
            new ImageHelper().execute(marker, item.icon);

            founded_place.add(marker);
        }
    }

    /**
     * JSON Add to map
     * @param output
     */
    private void addToMap(String output) {

        if (output == null)
        {
            Toast.makeText(LocationActivity.this, "Failed to get location, error code #2", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // PARSE JSON
            JSONObject objLocation = new JSONObject(output);

            if (objLocation.getString("status").equals("OK")) {
                JSONArray arrLocation = objLocation.getJSONArray("results");

                // fetch location and add into map
                for (int i = 0; i < arrLocation.length(); i++) {
                    JSONObject obj = arrLocation.getJSONObject(i);

                    // get coord
                    double latt = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lonG = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                    // get name
                    String name = obj.getString("name");
                    String addr = obj.getString("vicinity");
                    String icon = obj.getString("icon");

                    // get map
                    Bitmap bmp = null;
                    try {
                        URL getUrl = new URL(icon);
                        bmp = BitmapFactory.decodeStream(getUrl.openConnection().getInputStream());
                    } catch (Exception e) {
                        bmp = null;
                    }

                    // add into map
                    Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(latt, lonG)).title(name).snippet(addr));

                    if (bmp != null)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
                }
            } else {
                Toast.makeText(LocationActivity.this, "No nearby found!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(LocationActivity.this, "Failed to get location, error code #3: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private PlaceType getType(String text)
    {
        switch (text)
        {
            case "ATM": return PlaceType.ATM;
            case "Hospital": return PlaceType.HOSPITAL;
            case "Bank": return PlaceType.BANK;
            case "Bus station": return PlaceType.BUS_STATION;
            case "Shopping mall": return PlaceType.SHOPPING_MALL;
            case "Gym": return PlaceType.GYM;
            case "Cafe": return PlaceType.CAFE;
            default: return PlaceType.CAFE;
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                    } else {
                        Log.d("LOCATION_FAILED", "GET LOCATION FAILED");
                    }
                }
            });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private String[] supported_banks = new String[] {
        "Agribank",
        "Vietcombank",
        "Vietinbank",
        "DongA",
    };

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (founded_place == null)
        {
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Filter by name");

        View v = getLayoutInflater().inflate(R.layout.dialog_filter_location, null);
        final Spinner spnLocation = (Spinner) v.findViewById(R.id.spnLocation);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, supported_banks);
        spnLocation.setAdapter(adapter);

        dialog.setView(v);
        dialog.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selected = (String) spnLocation.getSelectedItem();

                // filter now
                for (Marker item : founded_place)
                {
                    if (item.getTitle().indexOf(selected) < 0)
                    {
                        item.remove();
                    }
                }

                Toast.makeText(LocationActivity.this, "Filtered result!!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }
}

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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sethphat.seth_googlemap_location.HttpRequest.HttpRequestHelper;
import com.sethphat.seth_googlemap_location.Models.InfoLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<String> listLocation = new ArrayList<String>();
    private GoogleMap map = null;
    private boolean canUseModule = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_layout);

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
        }
        else {
            Toast.makeText(this, "Please grant us the permission to use your location, otherwise you can't use this module!", Toast.LENGTH_SHORT).show();
            canUseModule = false;
        }
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

        dialog.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int x) {
                int radius = 0;
                if (txtRadius.getText().toString().isEmpty() == false)
                    radius = Integer.parseInt(txtRadius.getText().toString());

                // clear map
                map.clear();

                String type = getType((String) spnLocation.getSelectedItem());

                // current location
                Location location = getMyLocation();

                String lattLong = location.getLatitude() + "," + location.getLongitude();

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

            }
        });

        dialog.setNegativeButton("Cancel", null);

        dialog.show();
    }

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

    private String getType(String text)
    {
        switch (text)
        {
            case "ATM": return "atm";
            case "Hospital": return "hospital";
            case "Bank": return "bank";
            case "Bus station": return "bus_station";
            case "Shopping mall": return "shopping_mall";
            case "Gym": return "gym";
            case "Cafe": return "cafe";
            default: return "cafe";
        }
    }

    private Location getMyLocation() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }
        return myLocation;
    }

}
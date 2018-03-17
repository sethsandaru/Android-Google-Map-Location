package com.sethphat.seth_googlemap_location;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MarkerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap map = null;
    private LatLng default_coord = new LatLng(10.851009, 106.758260);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_layout);

        // Get map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_marker);
        mapFragment.getMapAsync(this);

        // get btn
        Button btnClear = (Button) findViewById(R.id.btnClear);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);

        // clear all marker
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear();
            }
        });

        // add new marker with address
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MarkerActivity.this);

                final EditText txtAddr = new EditText(MarkerActivity.this);
                alert.setMessage("Add your address/Địa chỉ của bạn");
                alert.setTitle("Add marker");
                alert.setView(txtAddr);

                alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String address = txtAddr.getText().toString();
                        addMarkerByAddress(address, dialogInterface);
                    }
                });

                alert.setNegativeButton("Close", null);

                alert.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // settings
        map.getUiSettings().setZoomControlsEnabled(true);

        // check location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        // Event settings
        map.setOnMapClickListener(this);

        // add default marker
        map.addMarker(new MarkerOptions().position(default_coord));
    }

    /**
     * Add marker depend on address
     * @param addr String
     */
    private void addMarkerByAddress(String addr, DialogInterface dialog)
    {
        try {
            Geocoder selected_place_geocoder = new Geocoder(MarkerActivity.this);
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(addr, 5);

            if (address != null && address.size() > 0)
            {
                Address found = address.get(0);

                // add marker
                map.addMarker(new MarkerOptions().position(new LatLng(found.getLatitude(), found.getLongitude())));

                // close dialog
                dialog.dismiss();
            }
            else {
                Toast.makeText(this, "Your address is not found, please try another keyword.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error when trying to find your address location, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * On map click action
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        map.addMarker(new MarkerOptions().position(latLng));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.sethphat.seth_googlemap_location;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class InfoActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap map = null;
    private LatLng default_coord = new LatLng(10.851009, 106.758260);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);

        // Get map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_marker);
        mapFragment.getMapAsync(this);

        // get btn
        Button btnClear = (Button) findViewById(R.id.btnClear);

        // clear all marker
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear();
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

        // event settings
        map.setOnMapClickListener(this);

        // show default info windows
        Marker tdc = map.addMarker(new MarkerOptions().position(default_coord).title("CĐ Công Nghệ Thủ Đức").snippet("Nhóm 1 - DD2"));
        tdc.showInfoWindow();
    }

    /*
    Action khi click tai dia diem
     */
    @Override
    public void onMapClick(final LatLng latLng) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_add_info, null);

        // controls
        final EditText txtName = (EditText) layout.findViewById(R.id.txtName);
        final EditText txtDescription = (EditText) layout.findViewById(R.id.txtDescription);

        //Building dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new info marker");
        builder.setView(layout);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // show default info windows
                Marker newMarker = map.addMarker(new MarkerOptions().position(latLng).title(txtName.getText().toString()).snippet(txtDescription.getText().toString()));
                newMarker.showInfoWindow();
            }
        });

        builder.setNegativeButton("Close", null);

        builder.show();
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

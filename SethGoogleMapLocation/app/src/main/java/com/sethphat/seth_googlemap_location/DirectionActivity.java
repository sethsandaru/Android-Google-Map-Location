package com.sethphat.seth_googlemap_location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, DirectionCallback {

    private GoogleMap map = null;
    private LatLng start = null;
    private LatLng end = null;

    // Textview helper
    TextView lblHelp;
    LinearLayout llAction;
    Button btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direction_layout);

        // Get map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_default);
        mapFragment.getMapAsync(this);

        lblHelp = (TextView) findViewById(R.id.lblHelp);
        llAction = (LinearLayout) findViewById(R.id.llAction);
        btnClearAll = (Button) findViewById(R.id.btnClearAll);
    }

    @Override
    public void onMapReady(GoogleMap maps) {
        // set map
        map = maps;
        map.getUiSettings().setZoomControlsEnabled(true);

        // settings
        map.getUiSettings().setZoomControlsEnabled(true);

        // check location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        map.setOnMapClickListener(this);

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

    /*
    Click tren map
     */
    @Override
    public void onMapClick(LatLng latLng) {

        if (start != null && end != null)
            return;

        if (start == null)
        {
            start = latLng;
            lblHelp.setText("Choose 1 more to find direction!");
            map.addMarker(new MarkerOptions().position(start));
            return;
        }

        if (end == null)
        {
            end = latLng;
            map.addMarker(new MarkerOptions().position(end));
            lblHelp.setVisibility(View.GONE);
            llAction.setVisibility(View.VISIBLE);
        }
    }

    public void clearCoord(View view) {
        map.clear();
        start = end = null;
        lblHelp.setVisibility(View.VISIBLE);
        llAction.setVisibility(View.GONE);
        lblHelp.setText("Please place 2 markers in order to find direction!");
    }

    public void findDirection(View view) {
        GoogleDirection.withServerKey(getString(R.string.google_direction_api_key))
                .from(start)
                .to(end)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            map.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
            setCameraWithCoordinationBounds(route);

            llAction.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Find direction successfully!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, direction.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, "Find direction failed, please try again", Toast.LENGTH_SHORT).show();
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    /**
     * Clear all back to normal state
     * @param view
     */
    public void clearAll(View view) {
        btnClearAll.setVisibility(View.GONE);
        this.clearCoord(view);
    }
}

package com.sethphat.seth_googlemap_location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 1;
    private boolean isLocationAllowed = false;

    private GoogleMap map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        // check location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            isLocationAllowed = true;
        }

        // Get map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_default);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap maps) {
        // set map
        map = maps;
        map.getUiSettings().setZoomControlsEnabled(true);

        // action sau khi map da hien len
        if (isLocationAllowed)
            maps.setMyLocationEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_marker:
                {
                    Intent intent = new Intent(MapActivity.this, MarkerActivity.class);
                    startActivity(intent);
                    return true;
                }
            case R.id.mn_info:
                {
                    Intent intent = new Intent(MapActivity.this, InfoActivity.class);
                    startActivity(intent);
                    return true;
                }
            case R.id.mn_info2:
                {
                    Intent intent = new Intent(MapActivity.this, Infov2Activity.class);
                    startActivity(intent);
                    return true;
                }
            case R.id.mn_direction:
            {
                Intent intent = new Intent(MapActivity.this, DirectionActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.mn_location:
            {
                Intent intent = new Intent(MapActivity.this, LocationActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                Toast.makeText(MapActivity.this, "You can see your own location now. Thanks", Toast.LENGTH_SHORT).show();
                isLocationAllowed = true;
                if (map != null)
                {
                    map.setMyLocationEnabled(true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    /**
     * Change state of map
     * @param view
     */
    public void changeState(View view) {
        Button btnState = (Button) view;

        switch (btnState.getText().toString())
        {
            case "Normal": map.setMapType(GoogleMap.MAP_TYPE_NORMAL); break;
            case "Hybrid": map.setMapType(GoogleMap.MAP_TYPE_HYBRID); break;
            case "Satellite": map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break;
            case "Terrain": map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); break;
        }
    }
}

package com.sethphat.seth_googlemap_location;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sethphat.seth_googlemap_location.Models.InfoLocation;

public class Infov2Activity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap map = null;
    private LatLng default_coord = new LatLng(10.851009, 106.758260);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infov2_layout);

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

        // event settings
        map.setOnMapClickListener(this);

        // info custom view
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.dialog_info_in_map, null);

                // get data
                InfoLocation info = (InfoLocation) marker.getTag();

                // get control
                ImageView imgPic = (ImageView) v.findViewById(R.id.imgPic);
                TextView lblName = (TextView) v.findViewById(R.id.lblName);
                TextView lblAddress = (TextView) v.findViewById(R.id.lblAddress);
                TextView lblDescription = (TextView) v.findViewById(R.id.lblDescription);

                // set data
                switch (info.getImg())
                {
                    case 1: imgPic.setImageResource(R.drawable.p1); break;
                    case 2: imgPic.setImageResource(R.drawable.p2); break;
                    case 3: imgPic.setImageResource(R.drawable.p3); break;
                    default: imgPic.setImageResource(R.drawable.p1); break;
                }

                lblName.setText(info.getName());
                lblAddress.setText(info.getAddress());
                lblDescription.setText(info.getDescription());

                return v;
            }
        });

        // show default info windows
        Marker tdc = map.addMarker(new MarkerOptions().position(default_coord));
        tdc.setTag(new InfoLocation("CĐ Công Nghệ Thủ Đức", "Môn dd2 thầy Thái - Nhóm 1", "Võ Văn Ngân, Q.Thủ Đức, TPHCM", 1));
        tdc.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map));
        tdc.showInfoWindow();
    }

    /*
    Click map
     */
    @Override
    public void onMapClick(final LatLng latLng) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_add_infov2, null);

        // controls
        final EditText txtName = (EditText) layout.findViewById(R.id.txtName);
        final EditText txtDescription = (EditText) layout.findViewById(R.id.txtDescription);
        final EditText txtAddress = (EditText) layout.findViewById(R.id.txtAddress);
        final RadioGroup rdgImg = (RadioGroup) layout.findViewById(R.id.rdgImg);

        //Building dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new info marker (v2)");
        builder.setView(layout);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // solving img
                int img = 1;
                switch (rdgImg.getCheckedRadioButtonId())
                {
                    case R.id.rdbImg1: img = 1; break;
                    case R.id.rdbImg2: img = 2; break;
                    case R.id.rdbImg3: img = 3; break;
                    default: img = 1; break;
                }

                // compile data
                InfoLocation info = new InfoLocation(txtName.getText().toString(), txtDescription.getText().toString(), txtAddress.getText().toString(), img);

                // show default info windows
                Marker newMarker = map.addMarker(new MarkerOptions().position(latLng));
                newMarker.setTag(info);
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map));
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

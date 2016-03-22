package se.rickylagerkvist.mymaps;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    // different cities
    private static final double
            STOCKHOLM_LAT = 59.329040,
            STOCKHOLM_LNG = 18.068616;

    private GoogleApiClient mLocationClient;
    private LocationListener mListener;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                goToLocation(STOCKHOLM_LAT, STOCKHOLM_LNG, 15);

                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mLocationClient.connect();
            } else {
                Toast.makeText(MainActivity.this, "Map not connected!", Toast.LENGTH_SHORT).show();
            }

        } else {
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // toolbar option to change map type
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Check if the play services work
    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(MainActivity.this, "Can´t connect to mapping services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // initialise map, check if it was successful
    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();

            // inflates custom window with info about location
            if (mMap != null) {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View v = getLayoutInflater().inflate(R.layout.info_window, null);
                        TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                        TextView tvLat= (TextView) v.findViewById(R.id.tvLat);
                        TextView tvLng= (TextView) v.findViewById(R.id.tvLng);
                        TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);

                        LatLng latLng = marker.getPosition();
                        tvLocality.setText(marker.getTitle());
                        tvLat.setText("Latitude: " + latLng.latitude);
                        tvLng.setText("Longitude: " + latLng.longitude);
                        tvSnippet.setText(marker.getSnippet());

                        return v;
                    }
                });
            }
        }
        return (mMap != null);
    }

    // long, lat, zoom,
    private void goToLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void hideSoftKeyBoard(View v) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //
    public void geoLocate(View v) throws IOException {

        hideSoftKeyBoard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);
        String searchString = tv.getText().toString();
        Toast.makeText(MainActivity.this, searchString, Toast.LENGTH_SHORT).show();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address add = list.get(0);
            String locality = add.getLocality();
            //Toast.makeText(MainActivity.this, "Found: " + locality, Toast.LENGTH_SHORT).show();
            double lat = add.getLatitude();
            double lng = add.getLongitude();
            goToLocation(lat, lng, 15);

            // only one marker at a time
            if (marker != null) {
                marker.remove();
            }
            // marker with title, position and color
            MarkerOptions options = new MarkerOptions()
                    .title(locality)
                    .position(new LatLng(lat, lng));
                    /*.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)); custom icon*/

            addMarker(add, lat, lng);
        }
    }

    // marker with title, position and color
    private void addMarker(Address add, double lat, double lng) {
        MarkerOptions options = new MarkerOptions()
                .title(add.getLocality())
                .position(new LatLng(lat, lng));
                /*.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));*/

        String country = add.getCountryName();
        if(country.length() >0){
            options.snippet(country);
        }

        marker = mMap.addMarker(options);
    }

    //shows current location
    public void showCurrentLocation(MenuItem item) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(MainActivity.this, "Couldn´t connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                    latLng, 15
            );
            mMap.animateCamera(update);
        }
    }

    // if the app connects to the service
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(MainActivity.this, "Ready to map!", Toast.LENGTH_SHORT).show();

        // Listener for location, auto update every 60000 ms
       /* mListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MainActivity.this,
                        "Location changed: " + location.getLatitude() + ", " +
                                location.getLongitude(), Toast.LENGTH_SHORT).show();
                goToLocation(location.getLatitude(),
                        location.getLongitude(), 15);
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(60000);
        request.setFastestInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, request, mListener
        );*/
    }

    // stop the service
    @Override
    public void onConnectionSuspended(int i) {

    }

    // dont succeed to connect
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        /*LocationServices.FusedLocationApi.removeLocationUpdates(
                mLocationClient, mListener
        );*/
    }
}

package ca.philipyoung.astroforecast.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

import ca.philipyoung.astroforecast.R;

public class AstroGeoLocation {
    private static final String TAG = "AstroGeoLocation";
    private Float mLatitude, mLongitude;
    private String mObservatory;
    private Context mContext;
    private LocationListener locationListener;

    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String GPS_SWITCH_KEY = "gps_switch";
    private static final String GPS_SNAP_KEY = "gps_snap";

    private static final Double GPS_MINIMUM_DISTANCE_DEGREES = 0.015;
    private static final Float GPS_MINIMUM_DISTANCE_METRES = 10.0f;
    private static final Long GPS_MINIMUM_TIME_MILLISECONDS = 6000L;
    private static final Double GPS_MAXIMUM_SNAP_DISTANCE_DEGREES = 0.025;

    // Get the GPS location and save to the You-Are-Here location on request.
    // Parameters are in Shared Preferences.
    public AstroGeoLocation(Context context) {
        this.mContext = context;
    }

    public void goFetch() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null && sharedPreferences.contains(GPS_SWITCH_KEY)) {
            // Check if GPS is on and is desired
            if(sharedPreferences.getBoolean(GPS_SWITCH_KEY,false)) {
                LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                // getting GPS status
                Boolean isGPSEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                Boolean isNetworkEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(isGPSEnabled) {
                    int permissionFineGPS = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                    int permissionCoarseGPS = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permissionFineGPS == PackageManager.PERMISSION_GRANTED || permissionCoarseGPS == PackageManager.PERMISSION_GRANTED) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            trackYourLocaton(location);
                        }
                        locationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                trackYourLocaton(location);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                String fubar = provider;
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                                String fubar = provider;
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                String fubar = provider;
                            }
                        };
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MINIMUM_TIME_MILLISECONDS, GPS_MINIMUM_DISTANCE_METRES, locationListener);
                    }
                }
            }
        }
    }

    public void stopThat() {
        LocationManager locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        if( locationManager!=null && locationListener!=null) {
            locationManager.removeUpdates(locationListener);
        }
    }


    public Boolean isGeoLocationOn() {
        Boolean blnIsOn = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null && sharedPreferences.contains(GPS_SWITCH_KEY)) {
            // Check if GPS is on and is desired
            if(sharedPreferences.getBoolean(GPS_SWITCH_KEY,false)) {
                LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                // getting GPS status
                Boolean isGPSEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                Boolean isNetworkEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(isGPSEnabled) {
                    int permissionFineGPS = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                    int permissionCoarseGPS = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permissionFineGPS == PackageManager.PERMISSION_GRANTED || permissionCoarseGPS == PackageManager.PERMISSION_GRANTED) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            trackYourLocaton(location);
                        } else {
                            final LocationListener locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    trackYourLocaton(location);
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {
                                    String fubar = provider;
                                }

                                @Override
                                public void onProviderEnabled(String provider) {
                                    String fubar = provider;
                                }

                                @Override
                                public void onProviderDisabled(String provider) {
                                    String fubar = provider;
                                }
                            };
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MINIMUM_TIME_MILLISECONDS, GPS_MINIMUM_DISTANCE_METRES, locationListener);
                        }
                    }
                }
            }
        }
        return blnIsOn;
    }

    private void trackYourLocaton(Location location) {
        if (location != null) {
            // get the current location, if it exists, and if it moved enough, save default location
            Double dblLatitude = location.getLatitude(),
                    dblLongitude = location.getLongitude();
            String strCoordinates = String.format(
                    Locale.US,
                    "%1$.6f%3$s %2$.6f%4$s",
                    Math.abs(dblLatitude),
                    Math.abs(dblLongitude),
                    dblLatitude>0?"N":"S",
                    dblLongitude>0?"E":"W"
            );
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            if(sharedPreferences!=null &&
                    sharedPreferences.contains(COORDINATES_LAT_KEY) &&
                    sharedPreferences.contains(COORDINATES_LON_KEY)) {
                Float fltLat = sharedPreferences.getFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                Float fltLng = sharedPreferences.getFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                Double dblDistanceDeg = Math.sqrt(Math.pow(fltLat-dblLatitude,2) + Math.pow(fltLng-dblLongitude,2)),
                        dblDistanceKm = dblDistanceDeg * 40000.0 / 360.0;
                Toast.makeText(mContext,String.format(
                        Locale.US,
                        "New Location(distance:%3$.5f): %1$.6f, %2$.6f",
                        dblLatitude,
                        dblLongitude,
                        dblDistanceKm),
                        (GPS_MINIMUM_TIME_MILLISECONDS<15000L?Toast.LENGTH_SHORT:Toast.LENGTH_LONG)
                ).show();
                Log.d(TAG, "trackYourLocaton: "+String.format(Locale.US,"New Location(distance:%3$.5f): %1$.6f, %2$.6f",dblLatitude,dblLongitude,dblDistanceKm));

                if(dblDistanceDeg>GPS_MINIMUM_DISTANCE_DEGREES) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    // Check for Snap location
                    if(sharedPreferences.getBoolean(GPS_SNAP_KEY,true)) {
                        AstroDatabase astroDatabase = new AstroDatabase(mContext);
                        HashMap<String,String> mapNearby = astroDatabase.getNearbyObservatory(dblLatitude,dblLongitude);
                        astroDatabase.astroDBclose();
                        if(mapNearby.size()<7) {
                            editor.putString(OBSERVATORY_KEY,mContext.getString(R.string.pref_default_location_name));
                            editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                            editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                            editor.putString(COORDINATES_KEY,strCoordinates);
                        } else if(!mapNearby.get("location_name")
                                .equals(sharedPreferences.getString(OBSERVATORY_KEY,mContext.getString(R.string.pref_default_location_name)))) {
                            editor.putString(OBSERVATORY_KEY,mapNearby.get("location_name"));
                            editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(mapNearby.get("geo_latitude")));
                            editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(mapNearby.get("geo_longitude")));
                            editor.putString(COORDINATES_KEY,mapNearby.get("coordinates"));
                            Toast.makeText(mContext,String.format(Locale.US,"Oh, Snap!(%3$s): %1$.6f, %2$.6f",Float.valueOf(mapNearby.get("geo_latitude")),Float.valueOf(mapNearby.get("geo_longitude")),mapNearby.get("location_name")),Toast.LENGTH_LONG).show();
                            Log.d(TAG, "trackYourLocaton: "+String.format(Locale.US,"Oh, Snap!(%3$s): %1$.6f, %2$.6f",Float.valueOf(mapNearby.get("geo_latitude")),Float.valueOf(mapNearby.get("geo_longitude")),mapNearby.get("location_name")));
                        }
                    } else {
                        editor.putString(OBSERVATORY_KEY,mContext.getString(R.string.pref_default_location_name));
                        editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                        editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                        editor.putString(COORDINATES_KEY,strCoordinates);
                    }
                    editor.apply();
                    ((Activity)mContext).setTitle(sharedPreferences.getString(OBSERVATORY_KEY,mContext.getString(R.string.title_activity_main)));
                }
                FloatingActionButton fabGeoLocation = (FloatingActionButton)((Activity) mContext).findViewById(R.id.fab_gps_location);
                if(fabGeoLocation!=null && sharedPreferences.getBoolean(GPS_SWITCH_KEY,false)) {
                    fabGeoLocation.setImageDrawable(mContext.getResources().getDrawable( R.drawable.ic_gps_fixed_antares_24dp ));
                }
            }
        }
    }

}

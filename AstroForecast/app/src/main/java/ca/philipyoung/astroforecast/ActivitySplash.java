package ca.philipyoung.astroforecast;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import ca.philipyoung.astroforecast.util.AstroDatabase;
import ca.philipyoung.astroforecast.util.FileService;
import ca.philipyoung.astroforecast.util.AstroWebServices;

public class ActivitySplash extends AppCompatActivity {

    private static final Integer INITIALIZATION_DELAY = 500;
    private static final Integer GPS_LOCATION_REQUEST_KEY = 1;
    private static final String MOON_PHASE_KEY = "moon_phase_name";
    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String GPS_SWITCH_KEY = "gps_switch";
    private static final String GPS_SNAP_KEY = "gps_snap";

    private static final Double GPS_MINIMUM_DISTANCE_DEGREES = 0.015;
    private static final Float GPS_MINIMUM_DISTANCE_METRES = 1000.0f;
    private static final Long GPS_MINIMUM_TIME_MILLISECONDS = 60000L;
    private static final Long GPS_MINIMUM_DISPLAY_TIME = 5000L;
    private Context mContext;
    private Dialog dialog;
    private Long lngTimeDialogStarted;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // handle rotating the screen
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * Emercency SP corrections
         PreferenceManager.getDefaultSharedPreferences(this).edit().putString("observatory_id","-1").apply();
         * **/
        initializeScreen();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==GPS_LOCATION_REQUEST_KEY) {
            // Returned from Android granting or denying permission to use the device GPS
            if(dialog!=null && dialog.isShowing()) dialog.dismiss();
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_DENIED &&
                    grantResults[1]==PackageManager.PERMISSION_DENIED) {
                initializeInternet();
            } else {
                initializeGPS();
            }
        }
    }

    private void initializeScreen() {
        Toast.makeText(mContext, getString(R.string.toast_initializing_screen),Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeDatabase();
            }
        },INITIALIZATION_DELAY);
    }

    private void initializeDatabase() {
        Toast.makeText(mContext, getString(R.string.db_toast_initializing_database),Toast.LENGTH_SHORT).show();
        Integer intDisplayDelay = new AstroDatabase(mContext).astroSetupAndClose();
        new AstroDatabase(mContext).HouseKeeping();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeGPS();
            }
        },intDisplayDelay);
    }

    private void initializeGPS() {
        final Long dteAnchorStart = (new Date()).getTime();  // Get the time that the function started
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null && sharedPreferences.contains(GPS_SWITCH_KEY)) {
            // Check if GPS is on and is desired
            if(sharedPreferences.getBoolean(GPS_SWITCH_KEY,false)) {
                Toast.makeText(mContext, getString(R.string.gps_toast_initializing_location),Toast.LENGTH_SHORT).show();
                // Also check if GPS is allowed
                locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                // getting GPS status
                Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(isGPSEnabled) {
                    int permissionFineGPS = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                    int permissionCoarseGPS = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permissionFineGPS == PackageManager.PERMISSION_GRANTED || permissionCoarseGPS == PackageManager.PERMISSION_GRANTED) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            trackYourLocaton(location);
                        } else {
                            // Display a dialog to let the user know that the app is waiting for the GPS
                            if(dialog==null || !dialog.isShowing()) {
                                dialog = new AlertDialog.Builder(mContext)
                                        .setTitle(R.string.gps_dialog_updating_location_title)
                                        .setMessage(R.string.gps_dialog_updating_location_message)
                                        .setNeutralButton(R.string.gps_dialog_updating_location_neutral, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                initializeInternet();
                                            }
                                        })
                                        .create();
                                dialog.show();
                            }
                            locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(final Location location) {
                                    if((new Date()).getTime()>dteAnchorStart+GPS_MINIMUM_DISPLAY_TIME) {
                                        trackYourLocaton(location);
                                        locationManager.removeUpdates(locationListener);
                                    } else {
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                trackYourLocaton(location);
                                                locationManager.removeUpdates(locationListener);
                                            }
                                        }, GPS_MINIMUM_DISPLAY_TIME);
                                    }
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
                            /*
                            Looper looper = new Looper.myLooper();
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, looper);
                            */
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MINIMUM_TIME_MILLISECONDS, GPS_MINIMUM_DISTANCE_METRES, locationListener);
                        }
                    } else {
                        // GPS is not allowed
                        ActivityCompat.requestPermissions(this,
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                }, GPS_LOCATION_REQUEST_KEY);
                    }
                    return;
                }
            }
        } else {
            // GPS is not yet set up. Start now?
            firstRunGps();
            return;
        }
        initializeInternet();
    }

    private void firstRunGps() {
        dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.gps_dialog_first_run_location_title)
                .setMessage(R.string.gps_dialog_first_run_location_message)
                .setIcon(R.drawable.ic_gps_not_fixed_black_24dp)
                .setPositiveButton(R.string.gps_dialog_first_run_location_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                        if(sp!=null) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean(GPS_SWITCH_KEY,true);
                            editor.apply();
                            initializeGPS();
                        }
                    }
                })
                .setNegativeButton(R.string.gps_dialog_first_run_location_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                        if(sp!=null) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean(GPS_SWITCH_KEY,false);
                            editor.apply();
                            initializeInternet();
                        }
                    }
                })
                .create();
        dialog.show();
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
                    dblLatitude>0?"N":"S",dblLongitude>0?"E":"W");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            if(sharedPreferences!=null &&
                    sharedPreferences.contains(COORDINATES_LAT_KEY) &&
                    sharedPreferences.contains(COORDINATES_LON_KEY)) {
                Float fltLat = sharedPreferences.getFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                Float fltLng = sharedPreferences.getFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                Double dblDistance = Math.sqrt(Math.pow(fltLat-dblLatitude,2) + Math.pow((fltLng-dblLongitude)*Math.cos(dblLatitude*Math.PI/180.0),2));
                // Am I far enough away from my last location?
                if(dblDistance>GPS_MINIMUM_DISTANCE_DEGREES) {
                    // Check for Snap location
                    if(sharedPreferences.getBoolean(GPS_SNAP_KEY,true)) {
                        AstroDatabase astroDatabase = new AstroDatabase(mContext);
                        HashMap<String,String> mapNearby = astroDatabase.getNearbyObservatory(dblLatitude,dblLongitude);
                        astroDatabase.astroDBclose();
                        if(mapNearby.size()<7) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(OBSERVATORY_KEY,getString(R.string.pref_default_location_name));
                            editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                            editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                            editor.putString(COORDINATES_KEY,strCoordinates);
                            editor.apply();
                        } else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(OBSERVATORY_KEY,mapNearby.get("location_name"));
                            editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(mapNearby.get("geo_latitude")));
                            editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(mapNearby.get("geo_longitude")));
                            editor.putString(COORDINATES_KEY,mapNearby.get("coordinates"));
                            editor.apply();
                        }
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(OBSERVATORY_KEY,getString(R.string.pref_default_location_name));
                        editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                        editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                        editor.putString(COORDINATES_KEY,strCoordinates);
                        editor.apply();
                    }
                }
            } else if(sharedPreferences!=null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(OBSERVATORY_KEY,getString(R.string.pref_default_location_name));
                editor.putFloat(COORDINATES_LAT_KEY,Float.valueOf(Double.toString(dblLatitude)));
                editor.putFloat(COORDINATES_LON_KEY,Float.valueOf(Double.toString(dblLongitude)));
                editor.putString(COORDINATES_KEY,strCoordinates);
                editor.apply();
            }
        }
        initializeInternet();
    }

    private void initializeInternet() {
        Toast.makeText(mContext, getString(R.string.ws_toast_initializing_internet),Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, FileService.class);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String strObservatory = getString(R.string.gps_location_name), strCoordinates = "43°N 79°W", urlObservatory, urlLatLon;
        Float fltLat = 43f, fltLng = -79f;
        if(sharedPreferences!=null) {
            strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, strObservatory);
            strCoordinates = sharedPreferences.getString(COORDINATES_KEY, strCoordinates);
            fltLat = sharedPreferences.getFloat(COORDINATES_LAT_KEY,fltLat);
            fltLng = sharedPreferences.getFloat(COORDINATES_LON_KEY,fltLng);
        }
        if(!strCoordinates.equals(String.format(
                Locale.US,
                "%1$.6f%3$s %2$.6f%4$s",
                Math.abs(fltLat),
                Math.abs(fltLng),
                fltLat>0?"N":"S",
                fltLng>0?"E":"W"
        ) )) {
            if(strCoordinates.matches("-?[0-9.]+[NS] -?[0-9.]+[EW]")) {
                fltLat = (strCoordinates.contains("S")?-1f:1f)*
                        Float.valueOf(strCoordinates.substring(0,
                                strCoordinates.indexOf(strCoordinates.contains("S")?"S":"N")));
                fltLng = (strCoordinates.contains("W")?-1f:1f)*
                        Float.valueOf(strCoordinates.substring(strCoordinates.indexOf(" ")+1,
                                strCoordinates.indexOf(strCoordinates.contains("E")?"E":"W")));
            }
        }
        urlObservatory = strObservatory.replace(" ","+")        // replace space
                .replace(",","%2C")                             // replace comma
                .replace("?","%3F")                             // replace question mark
                .replace("&","%26")                             // replace ampersand
                .replace("=","%3D")                             // replace equals
                .replace("'","%27");                            // replace apostrophe
        urlLatLon = String.format(Locale.US,"%1$.6f%%2C%2$.6f",fltLat,fltLng);


        intent.putExtra("url", "http://www.philipyoung.ca/philslab/astroforecastXML.php" +
                        "?obs="+ urlObservatory +"&latlng="+ urlLatLon +"&moon&verbose"
        );
        intent.putExtra("file","weather");
        this.startService(intent);
        AstroWebServices astroWebServices = new AstroWebServices(mContext);
        if(astroWebServices.getInternetConnection())
            Toast.makeText(mContext, getString(R.string.ws_toast_initialized_internet),Toast.LENGTH_LONG).show();/*  */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                launchApp();
            }
        },INITIALIZATION_DELAY);
    }

    private void launchApp() {
        Toast.makeText(mContext, getString(R.string.toast_launching_app),Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext,ActivityMain.class);
                startActivity(intent);
                finish();
            }
        },INITIALIZATION_DELAY);
    }
}

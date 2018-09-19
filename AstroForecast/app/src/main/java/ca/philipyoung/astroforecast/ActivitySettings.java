package ca.philipyoung.astroforecast;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ca.philipyoung.astroforecast.util.AstroDatabase;
import ca.philipyoung.astroforecast.util.FileService;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ActivitySettings extends AppCompatPreferenceActivity {
    private static final String OBSERVATORY_ID_KEY = "observatory_id";
    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String FIELD_LAT_KEY = "geo_latitude";
    private static final String FIELD_LON_KEY = "geo_longitude";
    private static final String GPS_SWITCH_KEY = "gps_switch";
    private static final String GPS_SNAP_KEY = "gps_snap";

    private static final Float GPS_MINIMUM_DISTANCE_METRES = 1000.0f;
    private static final Long GPS_MINIMUM_TIME_MILLISECONDS = 60000L;
    private String mGetFrame;
    private Context mContext;
    private static final String TAG = "ActivitySettings";
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                Boolean blnLaunched = !stringValue.equals(PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

                // check if preference is Location and Set other values, if necessary
                if (preference.getKey().equals(OBSERVATORY_ID_KEY) && blnLaunched) {
                    AstroDatabase astroDatabase;
                    CharSequence charObservatory = listPreference.getSummary();
                    if (charObservatory != null) {
                        try {
                            String strObservatory = charObservatory.toString();
                            SharedPreferences.Editor editor = preference.getPreferenceManager().getSharedPreferences().edit();
                            Preference prefObservatory = preference.getPreferenceManager().findPreference(OBSERVATORY_KEY);
                            Preference prefCoordinates = preference.getPreferenceManager().findPreference(COORDINATES_KEY);
                            if (prefObservatory != null && prefCoordinates != null) {
                                prefObservatory.setSummary(charObservatory);
                                astroDatabase = new AstroDatabase(prefObservatory.getContext());
                                String strLatitude = astroDatabase.getObservatory(strObservatory).get(FIELD_LAT_KEY);
                                String strLongitude = astroDatabase.getObservatory(strObservatory).get(FIELD_LON_KEY);
                                astroDatabase.astroDBclose();
                                if (!strLatitude.isEmpty() && !strLongitude.isEmpty()) {
                                    Float fltLatitude = Float.valueOf(strLatitude),
                                            fltLongitude = Float.valueOf(strLongitude);
                                    String strCoordinates = String.format(
                                            Locale.US,
                                            "%1$.6f%3$s %2$.6f%4$s",
                                            Math.abs(fltLatitude),
                                            Math.abs(fltLongitude),
                                            fltLatitude>0?"N":"S",
                                            fltLongitude>0?"E":"W"
                                    );
                                    prefObservatory.setSummary(listPreference.getSummary());
                                    prefCoordinates.setSummary(strCoordinates);
                                    editor.putString(OBSERVATORY_KEY, listPreference.getSummary().toString());
                                    editor.putString(COORDINATES_KEY, strCoordinates);
                                    editor.putFloat(COORDINATES_LAT_KEY, fltLatitude);
                                    editor.putFloat(COORDINATES_LON_KEY, fltLongitude);
                                    editor.putString(OBSERVATORY_ID_KEY, "-1");
                                    editor.apply();
                                    listPreference.setValue("-1");
                                    listPreference.setSummary(null);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "onPreferenceChange: " + e.getLocalizedMessage());
                        }
                    }
                }

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof SwitchPreference) {
                Boolean blnLaunched = !stringValue.equals(PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
                if (preference.getKey().equals(GPS_SWITCH_KEY) && blnLaunched) {
                    LocationManager locationManager = (LocationManager) preference.getContext().getSystemService(Context.LOCATION_SERVICE);
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
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
                    int permissionFineGPS = ActivityCompat.checkSelfPermission(preference.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                    int permissionCoarseGPS = ActivityCompat.checkSelfPermission(preference.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permissionFineGPS == PackageManager.PERMISSION_GRANTED || permissionCoarseGPS == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MINIMUM_TIME_MILLISECONDS, GPS_MINIMUM_DISTANCE_METRES, locationListener);
                    } else {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                    }
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }
            } else if (preference instanceof EditTextPreference) {
                Boolean blnLaunched = !stringValue.equals(PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
                if(preference.getKey().equals(COORDINATES_KEY) && blnLaunched) {
                    // break apart the coordinates string and save to latitude and longitude
                    Float fltLat, fltLng;
                    if(stringValue!=null && !stringValue.isEmpty() && stringValue.matches("-?[0-9.]+[NS] -?[0-9.]+[EW]")) {
                        fltLat = (stringValue.contains("S")?-1f:1f)*
                                Float.valueOf(stringValue.substring(0,
                                        stringValue.indexOf(stringValue.contains("S")?"S":"N")));
                        fltLng = (stringValue.contains("W")?-1f:1f)*
                                Float.valueOf(stringValue.substring(stringValue.indexOf(" ")+1,
                                        stringValue.indexOf(stringValue.contains("E")?"E":"W")));
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat(COORDINATES_LAT_KEY,fltLat);
                        editor.putFloat(COORDINATES_LON_KEY,fltLng);
                        editor.apply();
                    }
                }
                if( (preference.getKey().equals(OBSERVATORY_KEY) || preference.getKey().equals(COORDINATES_KEY) ) && blnLaunched) {
                    // Offer to download the updated data
                    Dialog dialog = new AlertDialog.Builder(preference.getContext())
                            .setTitle(R.string.gps_dialog_update_location_title)
                            .setMessage(R.string.gps_dialog_update_location_message)
                            .setPositiveButton(R.string.gps_dialog_update_location_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Go get the weather
                                            String strObservatory, urlObservatory, urlLatLon;
                                            Float fltLat=43f, fltLng=79f;
                                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                                            if(sharedPreferences!=null) {
                                                Intent intent = new Intent(preference.getContext(),FileService.class);
                                                strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, preference.getContext().getString(R.string.pref_default_location_name));
                                                fltLat = sharedPreferences.getFloat(COORDINATES_LAT_KEY,fltLat);
                                                fltLng = sharedPreferences.getFloat(COORDINATES_LON_KEY,fltLng);
                                                urlObservatory = strObservatory.replace(" ","+")        // replace space
                                                        .replace(",","%2C")                             // replace comma
                                                        .replace("?","%3F")                             // replace question mark
                                                        .replace("&","%26")                             // replace ampersand
                                                        .replace("=","%3D")                             // replace equals
                                                        .replace("'","%27");                            // replace apostrophe
                                                urlLatLon = String.format(Locale.US,"%1$.6f%%2C%2$.6f",fltLat,fltLng);
                                                intent.putExtra("url", "http://www.philipyoung.ca/philslab/astroforecastXML.php" +
                                                        "?obs="+ urlObservatory +"&latlng="+ urlLatLon +"&moon&sun&verbose"
                                                );
                                                preference.getContext().startService(intent);
                                            }
                                        }
                                    }, 200);
                                }
                            })
                            .setNegativeButton(R.string.gps_dialog_update_location_negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /****
     * From Derek Banas' YouTube video (I8XBY1sqz70): How to Make Android Apps 18 - Broadcasts, etc.
     * @param strPackage
     * @return
     */
    public void startFileService(String strPackage) {
        Intent intent = new Intent(this, FileService.class);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String strObservatory = "Etobicoke", strCoordinates = "43°N 79°W", urlObservatory, urlLatLon;
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
                        )
                    )
                ) {
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

        if( strPackage!= null && strPackage.equals("events")) {
            intent.putExtra("url", "http://www.philipyoung.ca/philslab/astroforecast_events.php" +
                    "?obs="+ urlObservatory +"&latlng="+ urlLatLon +"&moon&aurora=1"
            );
        } else {
            intent.putExtra("url", "http://www.philipyoung.ca/philslab/astroforecastXML.php" +
                    "?obs="+ urlObservatory +"&latlng="+ urlLatLon +"&moon&sun&verbose"
            );
        }
        /* intent.putExtra("url", "http://www.newthinktank.com/wordpress/lotr.txt" ); */
        this.startService(intent);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(@NonNull Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        mGetFrame = getIntent().getStringExtra("frame");
        // TODO: Changed "mHandler" to constellation if mGetFrame is set
        // this.mHandler = {PreferenceActivity$1@4529} "Handler (android.preference.PreferenceActivity$1) {317e395}"
        // this.mHandler.name = "android.preference.PreferenceActivity$1"
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Called after {@link #onCreate} &mdash; or after {@link #onRestart} when
     * the activity had been stopped, but is now again being displayed to the
     * user.  It will be followed by {@link #onResume}.
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onCreate
     * @see #onStop
     * @see #onResume
     */
    @Override
    protected void onStart() {
        int permissionFineGPS = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseGPS = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionFineGPS!=PackageManager.PERMISSION_GRANTED && permissionCoarseGPS!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1);
        }
        super.onStart();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || ConstellationAlphaPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            if(findPreference("example_list")!=null) bindPreferenceSummaryToValue(findPreference("example_list"));

            Preference prefObservatory = this.findPreference(OBSERVATORY_ID_KEY);
            if(prefObservatory!=null && prefObservatory instanceof ListPreference) {
                // set observatory location names to entries and location ids to entry values
                AstroDatabase astroDatabase = new AstroDatabase(GeneralPreferenceFragment.this.getActivity().getBaseContext());
                HashMap<String,String> listObservatories = astroDatabase.getObservatories();
                astroDatabase.astroDBclose();
                String[] strObservatoryIDs = new String[listObservatories.size()],
                        strObservatoryNames = new String[listObservatories.size()],
                        strings;
                Integer intI = 0;
                for (String strObservatoryKey : listObservatories.keySet()) {
                    strings = listObservatories.get(strObservatoryKey).split(",");
                    if(strings.length==4) {
                        strObservatoryIDs[intI] = strObservatoryKey;
                        strObservatoryNames[intI++] = strings[0].replaceAll("%2C",",");
                    }
                }
                ((ListPreference)prefObservatory).setEntryValues(strObservatoryIDs);
                ((ListPreference)prefObservatory).setEntries(strObservatoryNames);
                ((ListPreference)prefObservatory).setValue("-1");
                bindPreferenceSummaryToValue(prefObservatory);
            }
            bindPreferenceSummaryToValue(findPreference(OBSERVATORY_KEY));
            bindPreferenceSummaryToValue(findPreference(COORDINATES_KEY));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferenceSummaryToValue(findPreference("notifications_key_2_phase"));
            bindPreferenceSummaryToValue(findPreference("notifications_key_3_planets"));
            bindPreferenceSummaryToValue(findPreference("notifications_key_3_conjunctions_distance"));
            bindPreferenceSummaryToValue(findPreference("notifications_key_7_constellations"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /*
      This fragment shows preferences for each constellation. It is used when the
      activity is showing a two-pane settings UI.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ConstellationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_constellations);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
     */

    /**
     * This fragment shows preferences for each constellation. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ConstellationAlphaPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_constellations_alphabetical);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /*
    public Location getLocation() {
        try {
            mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,  MIN_TIME_BW_UPDATES,  MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (mLocationManager != null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    }
                }
                //get the location by gps
                if (isGPSEnabled) {
                    if (location == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }*/

}

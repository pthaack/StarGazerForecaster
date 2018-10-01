package ca.philipyoung.astroforecast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.spec.IvParameterSpec;

import ca.philipyoung.astroforecast.ui.MenuSelection;
import ca.philipyoung.astroforecast.ui.PopUpApps;
import ca.philipyoung.astroforecast.ui.PopUpObjects;
import ca.philipyoung.astroforecast.util.AstroDatabase;
import ca.philipyoung.astroforecast.util.AstroGeoLocation;
import ca.philipyoung.astroforecast.util.AstroWebServices;
import ca.philipyoung.astroforecast.util.FileService;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ActivityMain";
    private static final Integer INIT_DELAY = 500;
    private static final Integer VERY_LONG_DELAY = 15000;
    private static final Integer PLANET_TYPE_KEY = 4;
    private static final Integer OBJECT_TYPE_KEY = 2;
    private static final Integer CONSTELLATION_TYPE_KEY = 3;
    private static final Integer CONJUNCTION_TYPE_KEY = 5;
    private static final Integer SATELLITE_TYPE_KEY = 6;
    private static final Integer COMET_TYPE_KEY = 7;
    private static final Integer LOCATION_TYPE_KEY = 10;
    private static final Integer DIALOG_DISMISS = 1;
    private static final Integer METEOR_LAUNCH_KEY = 101;
    private static final Integer AURORA_LAUNCH_KEY = 102;
    private static final Integer VARIABLE_STARS_LAUNCH_KEY = 103;
    private static final Integer SATELLITE_LAUNCH_KEY = 104;
    private static final Integer MOON_LAUNCH_KEY = 105;
    private static final Integer ECLIPSE_LAUNCH_KEY = 106;
    private static final Integer PLANET_POP_UP_KEY = 121;
    private static final Integer CONJUNCTION_POP_UP_KEY = 122;
    private static final Integer SATELLITE_ISS_POP_UP_KEY = 123;
    private static final Integer SATELLITE_IRIDIUM_POP_UP_KEY = 124;
    private static final Integer SATELLITE_POP_UP_KEY = 125;
    private static final Integer COMET_POP_UP_KEY = 126;
    private static final String SATELLITE_APP = "com.heavens_above.viewer";
    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String COORDINATES_ALT_KEY = "coordinate_altitude";
    private static final String AURORA_LEVEL_KEY = "aurora";
    private static final String AURORA_PERCENTAGE_KEY = "aurora_pct";
    private static final String AURORA_FORECAST_KEY = "aurora_forecast";
    private static final String MOON_RISE_KEY = "moon_rise";
    private static final String MOON_SET_KEY = "moon_set";

    private static final Long ONE_HOUR = 3600L; // the number of seconds in an hour.
    private static final Long ONE_FULL_DAY = 86400L; // the number of seconds in one day.
    private static final Long ONE_HALF_DAY = 43200L; // the number of seconds in half a day.
    private static final Long ONE_FULL_WEEK = 604800L; // the number of seconds in one week.

    private static final String FIELD_METEOR_NAME_KEY = "radiant_name";
    private static final String FIELD_METEOR_BEGIN_KEY = "date_start";
    private static final String FIELD_METEOR_PEAK_KEY = "date_peak";
    private static final String FIELD_METEOR_END_KEY = "date_stop";
    private static final String FIELD_ECLIPSE_BEGIN_KEY = "date_start";
    private static final String FIELD_ECLIPSE_TYPE_KEY = "eclipse_type";

    private static final String CONJUNCTION_DISTANCE_KEY = "notifications_key_3_conjunctions_distance";
    private static final String CONJUNCTION_PLANET_KEY = "notifications_key_3_planets";
    private static final String AURORA_KEY = "notifications_key_5";
    private static final String SUNTIMES_KEY = "notifications_key_9";
    private static final String MOONTIMES_KEY = "notifications_key_2";

    private static final String TIMESTAMP_KEY = "ts";
    private static final String TIMEHOUR_KEY = "hr";
    private static final String MOON_PHASE_KEY = "moon_phase_name";
    private static final String MOON_PHASE_0_KEY = "New Moon";
    private static final String MOON_PHASE_1_KEY = "Waxing Crescent";
    private static final String MOON_PHASE_2_KEY = "First Quarter";
    private static final String MOON_PHASE_3_KEY = "Waxing Gibbous";
    private static final String MOON_PHASE_4_KEY = "Full Moon";
    private static final String MOON_PHASE_5_KEY = "Waning Gibbous";
    private static final String MOON_PHASE_6_KEY = "Third Quarter";
    private static final String MOON_PHASE_7_KEY = "Waning Crescent";
    private static final String MOON_ILLUMINATION_KEY = "moon_illumination_fraction";

    private static final String GPS_SWITCH_KEY = "gps_switch";

    private Context mContext;
    private TextView downloadEditText;
    private FloatingActionButton fabReload, fabGeoLocation;
    private AstroGeoLocation astroGeoLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        downloadEditText = (TextView) findViewById(R.id.downloadedEditText);
        fabReload = (FloatingActionButton) findViewById(R.id.fab_reload);
        fabGeoLocation = (FloatingActionButton) findViewById(R.id.fab_gps_location);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileService.TRANSACTION_DONE);
        registerReceiver(downloadReceiver,intentFilter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if(downloadReceiver!=null) unregisterReceiver(downloadReceiver);
        astroGeoLocation.stopThat();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if( sharedPreferences!=null ) {
            // ToDo: This whole set up as an AsyncTask or background thread???
            /* preexecute */
            ArrayList<Integer> lstAlerts = new ArrayList<>();
            lstAlerts.add(R.id.overlayViewWeather);
            if( sharedPreferences.getBoolean("notifications_key_1",false)) lstAlerts.add(R.id.alertViewMeteor);
            if( sharedPreferences.getBoolean("notifications_key_2",false)) lstAlerts.add(R.id.alertViewMoon);
            if( sharedPreferences.getBoolean("notifications_key_3",false)) lstAlerts.add(R.id.alertViewPlanet);
            if( sharedPreferences.getBoolean("notifications_key_4",false)) lstAlerts.add(R.id.alertViewComet);
            if( sharedPreferences.getBoolean("notifications_key_5",false)) lstAlerts.add(R.id.alertViewAurora);
            if( sharedPreferences.getBoolean("notifications_key_6",false)) lstAlerts.add(R.id.alertViewSatellite);
            if( sharedPreferences.getBoolean("notifications_key_7",false)) lstAlerts.add(R.id.alertViewConstellation);
            if( sharedPreferences.getBoolean("notifications_key_8",false)) lstAlerts.add(R.id.alertViewVariableStar);
            if( sharedPreferences.getBoolean("notifications_key_9",false)) lstAlerts.add(R.id.alertViewEclipse);

            /* doinbackground */
            for (int intI = 0; intI < lstAlerts.size(); intI++) {
                int intAlert = lstAlerts.get(intI);
                View view = findViewById(intAlert);
                if(view!=null) {
                    view.setVisibility(View.VISIBLE);
                    if(view instanceof ImageView)
                    switch(intAlert) {
                        case R.id.overlayViewWeather:
                            if(false) {
                                view.setVisibility(View.VISIBLE);
                            } else {
                                view.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case R.id.alertViewMeteor:
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                ((ImageView) view).setImageResource(R.drawable.ic_star_4_point_yellow_24dp);
                            } else {
                                ((ImageView) view).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_star_4_point_yellow_24dp));
                            }
                            break;
                        case R.id.alertViewMoon:
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                ((ImageView) view).setImageResource(R.drawable.ic_favorite_border_yellow_24dp);
                            } else {
                                ((ImageView) view).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_yellow_24dp));
                            }
                            break;
                        case R.id.alertViewSatellite:
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                ((ImageView) view).setImageResource(R.drawable.ic_star_5_point_yellow_24dp);
                            } else {
                                ((ImageView) view).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_star_5_point_yellow_24dp));
                            }
                            break;
                        case R.id.alertViewConstellation:
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                ((ImageView) view).setImageResource(R.drawable.ic_favorite_border_yellow_24dp);
                            } else {
                                ((ImageView) view).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_yellow_24dp));
                            }
                            break;
                        default:
                            /* progressreport */
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                ((ImageView) view).setImageResource(R.drawable.ic_star_3_point_yellow_24dp);
                            } else {
                                ((ImageView) view).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_star_3_point_yellow_24dp));
                            }
                            break;
                    }
                }
            }

            setMoonPhase();
            setWeather();
            setEvents();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initAllListeners();
            }
        },INIT_DELAY);
    }

    /* read the preference settings, get the current status, and update the screen icons */
    private class updateEventStatusAsyncTask extends AsyncTask<Void,Void,Void> {
        /**
         * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
         */
        public updateEventStatusAsyncTask() {
            super();
        }

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /* Get the shared preferences */
        }

        /**
         * <p>Applications should preferably override {@link #onCancelled(Object)}.
         * This method is invoked by the default implementation of
         * {@link #onCancelled(Object)}.</p>
         * <p>
         * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
         * {@link #doInBackground(Object[])} has finished.</p>
         *
         * @see #onCancelled(Object)
         * @see #cancel(boolean)
         * @see #isCancelled()
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(Void... voids) {
            /* for each of the statuses, update the flag  */
            return null;
        }

        /**
         * Runs on the UI thread after {@link #publishProgress} is invoked.
         * The specified values are the values passed to {@link #publishProgress}.
         *
         * @param values The values indicating progress.
         * @see #publishProgress
         * @see #doInBackground
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param aVoid The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /* get the status in the background and display on main screen */
    private class updateEventThread<Token> extends HandlerThread {
        public updateEventThread(String name) {
            super(name);
        }
    }

    /* fetch the status and loop it back to the display */
    private class updateEventRunnable implements Runnable {
        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {

        }
    }

    private void initAllListeners() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String sPSyncFrequency;
        View vwButton;
        if( fabReload!=null && fabReload instanceof FloatingActionButton ) {
            fabReload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, mContext.getString(R.string.ws_downloading_updates), Snackbar.LENGTH_LONG)
                            .setActionTextColor(mContext.getResources().getColor(R.color.foregroundSkyAntares))
                            .setAction("Stop", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(downloadReceiver!=null) {
                                        unregisterReceiver(downloadReceiver);
                                        IntentFilter intentFilter = new IntentFilter();
                                        intentFilter.addAction(FileService.TRANSACTION_DONE);
                                        registerReceiver(downloadReceiver,intentFilter);
                                        /* downloadReceiver.abortBroadcast();*/
                                        Toast.makeText(mContext, mContext.getString(R.string.ws_downloading_cancelled), Toast.LENGTH_SHORT).show();
                                        fabReload.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sync_problem_antares_24dp));
                                        fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_problem));
                                    }
                                }
                            }).show();
                    // TODOne: Connect and get updates
                    startFileService(view,"weather");
                    if(sharedPreferences!=null) sharedPreferences.edit().remove(AURORA_LEVEL_KEY).apply();
                    fabReload.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sync_disabled_antares_24dp));
                    fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_disabled));
                }
            });
            fabReload.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_popup_sync));
            fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_sync));
        }

        if( fabGeoLocation!=null && fabGeoLocation instanceof FloatingActionButton ) {
            fabGeoLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences spGeoLocation = PreferenceManager.getDefaultSharedPreferences(mContext);
                    Boolean blnGPSisOn = !spGeoLocation.getBoolean(GPS_SWITCH_KEY,true);
                    SharedPreferences.Editor editor = spGeoLocation.edit();
                    editor.putBoolean(GPS_SWITCH_KEY,blnGPSisOn);
                    editor.apply();
                    if(blnGPSisOn) {
                        fabGeoLocation.setImageDrawable(mContext.getResources().getDrawable(
                                R.drawable.ic_gps_not_fixed_antares_24dp
                        ));
                        fabGeoLocation.setContentDescription(mContext.getString(R.string.main_screen_location_fab_description_not_fixed));
                        astroGeoLocation.goFetch();
                    } else {
                        fabGeoLocation.setImageDrawable(mContext.getResources().getDrawable(
                                R.drawable.ic_gps_off_antares_24dp
                        ));
                        fabGeoLocation.setContentDescription(mContext.getString(R.string.main_screen_location_fab_description_off));
                        astroGeoLocation.stopThat();
                    }
                }
            });
            if(sharedPreferences.getBoolean(GPS_SWITCH_KEY, true)) {
                fabGeoLocation.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.ic_gps_not_fixed_antares_24dp
                ));
                fabGeoLocation.setContentDescription(mContext.getString(R.string.main_screen_location_fab_description_not_fixed));
            } else {
                fabGeoLocation.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.ic_gps_off_antares_24dp
                ));
                fabGeoLocation.setContentDescription(mContext.getString(R.string.main_screen_location_fab_description_off));
            }

        }
        astroGeoLocation = new AstroGeoLocation(mContext);
        astroGeoLocation.goFetch();

        vwButton = findViewById(R.id.imageViewMeteor);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View meteor details",Toast.LENGTH_LONG).show();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
                    HashMap<String,String> mapMeteor = astroDatabase.getMeteorShower();
                    astroDatabase.astroDBclose();
                    String strMessage = "";
                    if(mapMeteor.size()>=1) {
                        Date dteNextMeteor = new Date(Long.valueOf(mapMeteor.get(FIELD_METEOR_BEGIN_KEY))*1000L),
                                dtePeakMeteor = new Date(Long.valueOf(mapMeteor.get(FIELD_METEOR_PEAK_KEY))*1000L),
                                dteEndMeteor = new Date(Long.valueOf(mapMeteor.get(FIELD_METEOR_END_KEY))*1000L),
                                dteNow = new Date();
                        if(dteNextMeteor.after(dteNow)) {
                            strMessage += String.format(Locale.US,
                                    mContext.getString(R.string.dialog_pop_up_meteor_next),
                                    mapMeteor.get(FIELD_METEOR_NAME_KEY),
                                    dteNextMeteor
                            );
                        } else if(dtePeakMeteor.after(dteNow)) {
                            strMessage += String.format(Locale.US,
                                    mContext.getString(R.string.dialog_pop_up_meteor_current_peak),
                                    mapMeteor.get(FIELD_METEOR_NAME_KEY),
                                    dtePeakMeteor
                            );
                        } else {
                            strMessage += String.format(Locale.US,
                                    mContext.getString(R.string.dialog_pop_up_meteor_current_ends),
                                    mapMeteor.get(FIELD_METEOR_NAME_KEY),
                                    dteEndMeteor
                            );
                        }
                    }
                    strMessage += mContext.getString(R.string.dialog_pop_up_meteor_message);
                    final PopUpApps popUp = new PopUpApps(mContext,METEOR_LAUNCH_KEY);
                    popUp.setCancelable(true);
                    popUp.show();
                    popUp.setDialog(popUp);
                    popUp.setTitle(R.string.dialog_pop_up_meteor_title);
                    popUp.setMessage(strMessage);
                    popUp.setPositiveButtonText(R.string.dialog_pop_up_meteor_positive);
                    popUp.setPositiveClick(METEOR_LAUNCH_KEY);
                    popUp.setCancelClick(DIALOG_DISMISS);
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_meteor_image_description));
        }

        vwButton = findViewById(R.id.imageViewMoon);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View moon and weather details",Toast.LENGTH_LONG).show();
                    String strMessage = getMoonMonthName(new Date())+"\n"+getMoonPhase()+getMoonTimes()+getWeather();
                    final PopUpApps popUp = new PopUpApps(mContext,MOON_LAUNCH_KEY);
                    popUp.setCancelable(true);
                    popUp.show();
                    popUp.setDialog(popUp);
                    popUp.setTitle(R.string.dialog_pop_up_moon_title);
                    popUp.setMessage(strMessage);
                    popUp.setPositiveButtonText(R.string.dialog_pop_up_moon_positive);
                    popUp.setPositiveClick(MOON_LAUNCH_KEY);
                    popUp.setCancelClick(DIALOG_DISMISS);
                }
            });
        }

        vwButton = findViewById(R.id.imageViewPlanet);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View planet and conjunction details",Toast.LENGTH_LONG).show();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
                    Integer intCount = astroDatabase.getConjunctionsCount(
                            Float.valueOf(
                                    sharedPreferences
                                            .getString(CONJUNCTION_DISTANCE_KEY,"2.5°")
                                            .replace("°","")
                            )
                    );
                    if(sharedPreferences.getBoolean(MOONTIMES_KEY,false)) intCount+=astroDatabase.getConjunctionsCount("Moon");
                    astroDatabase.astroDBclose();
                    if(intCount==0) {
                        showPlanetPopUp();
                    } else {
                        showConjunctionPopUp();
                    }
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_planet_image_description));
        }

        vwButton = findViewById(R.id.imageViewComet);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View comet details",Toast.LENGTH_LONG).show();
                    showCometPopUp();
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_comet_image_description));
        }

        vwButton = findViewById(R.id.imageViewAurora);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View aurora details",Toast.LENGTH_LONG).show();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    final PopUpApps popUp = new PopUpApps(mContext,AURORA_LAUNCH_KEY);
                    popUp.setCancelable(true);
                    popUp.show();
                    popUp.setDialog(popUp);
                    popUp.setTitle(R.string.dialog_pop_up_aurora_title);
                    if (sharedPreferences.getString(AURORA_LEVEL_KEY,null) == null) {
                        popUp.setMessage(String.format(Locale.US,
                                "  %s %s\n%s",
                                mContext.getString(R.string.dialog_pop_up_aurora_status),
                                sharedPreferences.getString(AURORA_LEVEL_KEY, getString(R.string.dialog_pop_up_aurora_kp_default)),
                                mContext.getString(R.string.dialog_pop_up_aurora_message)
                                )
                        );
                    }else {
                        popUp.setMessage(String.format(Locale.US,
                                "  %s %s\n  %s %.0f%%\n  %s %.0f%%\n%s",
                                mContext.getString(R.string.dialog_pop_up_aurora_status),
                                sharedPreferences.getString(AURORA_LEVEL_KEY, getString(R.string.dialog_pop_up_aurora_kp_default)),
                                mContext.getString(R.string.dialog_pop_up_aurora_forecast),
                                sharedPreferences.getFloat(AURORA_PERCENTAGE_KEY, 0f),
                                mContext.getString(R.string.dialog_pop_up_aurora_3day_forecast),
                                sharedPreferences.getFloat(AURORA_FORECAST_KEY, 0f),
                                mContext.getString(R.string.dialog_pop_up_aurora_message)
                                )
                        );
                    }
                    popUp.setPositiveButtonText(R.string.dialog_pop_up_aurora_positive);
                    popUp.setPositiveClick(AURORA_LAUNCH_KEY);
                    popUp.setCancelClick(DIALOG_DISMISS);
                }
            });
        }

        vwButton = findViewById(R.id.imageViewSatellite);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View satellite details",Toast.LENGTH_LONG).show();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
                    Integer intISSCount = astroDatabase.getSatelliteCountTonightsISS();
                    Integer intIridiumCount = astroDatabase.getSatelliteCountTonightsIridium();
                    astroDatabase.astroDBclose();
                    if(intISSCount>0) {
                        showSatelliteISSPopUp();
                    } else if(intIridiumCount>0) {
                        showSatelliteIridiumPopUp();
                    } else {
                        showSatellitePopUp();
                    }
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_satellite_image_description));
        }

        vwButton = findViewById(R.id.imageViewConstellation);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View constellation details",Toast.LENGTH_LONG).show();
                    final PopUpObjects popUp = new PopUpObjects(mContext,CONSTELLATION_TYPE_KEY);
                    popUp.setCancelable(true);
                    popUp.show();
                    popUp.setDialog(popUp);
                    popUp.setTitle(R.string.dialog_pop_up_constellations_title);
                    popUp.setMessage(R.string.dialog_pop_up_constellations_message);
                    popUp.setCancelClick(DIALOG_DISMISS);
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_constellation_image_description));
        }

        vwButton = findViewById(R.id.imageViewVariableStar);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View variable star details",Toast.LENGTH_LONG).show();
                    final PopUpApps popUp = new PopUpApps(mContext,VARIABLE_STARS_LAUNCH_KEY);
                    popUp.setCancelable(true);
                    popUp.show();
                    popUp.setDialog(popUp);
                    popUp.setTitle(R.string.dialog_pop_up_app_variable_stars_title);
                    popUp.setMessage(R.string.dialog_pop_up_app_variable_stars_message);
                    popUp.setPositiveButtonText(R.string.dialog_pop_up_app_variable_stars_positive);
                    popUp.setPositiveClick(VARIABLE_STARS_LAUNCH_KEY);
                    popUp.setNeutralButtonText(R.string.dialog_pop_up_app_variable_stars_neutral);
                    popUp.setNeutralClick(OBJECT_TYPE_KEY);
                    popUp.setCancelClick(DIALOG_DISMISS);
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_variable_stars_image_description));
        }

        vwButton = findViewById(R.id.imageViewEclipse);
        if( vwButton!=null && vwButton instanceof ImageView ) {
            vwButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"View eclipse details",Toast.LENGTH_LONG).show();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
                    HashMap<String,String> mapEclipse = astroDatabase.getNextEclipse();
                    astroDatabase.astroDBclose();
                    String strMessage ="";
                    if(mapEclipse.size()>1) {
                        Date dteNextEclipse = new Date(Long.valueOf(mapEclipse.get(FIELD_ECLIPSE_BEGIN_KEY))*1000L);
                        strMessage += String.format(Locale.US,
                                mContext.getString(R.string.dialog_pop_up_eclipse_next),
                                mapEclipse.get(FIELD_ECLIPSE_TYPE_KEY),
                                dteNextEclipse
                        );
                    }
                    strMessage += mContext.getString(R.string.dialog_pop_up_eclipse_message);
                    final PopUpApps popUp = new PopUpApps(mContext,ECLIPSE_LAUNCH_KEY);
                    popUp.setCancelable(true);
                    popUp.show();
                    popUp.setDialog(popUp);
                    popUp.setTitle(R.string.dialog_pop_up_eclipse_title);
                    popUp.setMessage(strMessage);
                    popUp.setPositiveButtonText(R.string.dialog_pop_up_eclipse_positive);
                    popUp.setPositiveClick(ECLIPSE_LAUNCH_KEY);
                    popUp.setCancelClick(DIALOG_DISMISS);
                }
            });
            vwButton.setContentDescription(mContext.getString(R.string.main_screen_eclipse_image_description));
        }

        // ToDo: Set clock listeners
        if(sharedPreferences!=null) {
            sPSyncFrequency = sharedPreferences.getString("sync_frequency", "");
            if(!sPSyncFrequency.isEmpty() && !sPSyncFrequency.equals("-1")) {
                Integer intOneHour=36000000,
                        intSyncCountDown=intOneHour;
                if(sPSyncFrequency.equals("180")) {
                    // sync every 3 hours.  good for aurora
                    intSyncCountDown = 3 * intOneHour;
                } else if(sPSyncFrequency.equals("360")) {
                    // sync every 6 hours.  good for most
                    intSyncCountDown = 6 * intOneHour;
                } else if(sPSyncFrequency.equals("720")) {
                    // sync every 12 hours.  adequate
                    intSyncCountDown = 12 * intOneHour;
                } else if(sPSyncFrequency.equals("-1440")) {
                    // sync every morning.  once per day
                    Calendar dteNow = Calendar.getInstance();  // Get local time
                    if( dteNow.get(Calendar.HOUR_OF_DAY)<=12 ) {
                        intSyncCountDown = 1000;  // Get it now
                    } else {
                        intSyncCountDown = (13 - dteNow.get(Calendar.HOUR_OF_DAY)) * intOneHour;  // Get it later
                    }
                } else if(sPSyncFrequency.equals("1440")) {
                    // sync every afternoon.  once per day
                    Calendar dteNow = Calendar.getInstance();  // Get local time
                    if( dteNow.get(Calendar.HOUR_OF_DAY)>=12 ) {
                        intSyncCountDown = 1000;  // Get it now
                    } else {
                        intSyncCountDown = (13 - dteNow.get(Calendar.HOUR_OF_DAY)) * intOneHour;  // Get it later
                    }
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFileService(fabReload,"weather");
                    }
                },intSyncCountDown);
            }
        }

    }

    /****
     * From Derek Banas' YouTube video (I8XBY1sqz70): How to Make Android Apps 18 - Broadcasts, etc.
     * @param view
     * @return
     */
    public void startFileService(View view, String strPackage) {
        Intent intent = new Intent(this, FileService.class);
        fabReload.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sync_disabled_antares_24dp));
        fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_disabled));
        findViewById(R.id.overlayViewAurora).setVisibility(View.VISIBLE);
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
        urlObservatory = strObservatory.replaceAll("\\s","+")      // replace whitespace
                .replaceAll(",","%2C")                             // replace comma
                .replaceAll("\\?","%3F")                           // replace question mark
                .replaceAll("&","%26")                             // replace ampersand
                .replaceAll("=","%3D")                             // replace equals
                .replaceAll("\"","%22")                            // replace quotes
                .replaceAll("'","%27");                            // replace apostrophe
        urlLatLon = String.format(Locale.US,"%1$.6f%%2C%2$.6f",fltLat,fltLng);

        if( strPackage!= null && strPackage.equals("events")) {
            intent.putExtra("url", "http://www.philipyoung.ca/philslab/astroforecast_events.php" +
                    "?obs="+ urlObservatory +"&latlng="+ urlLatLon +"&moon&aurora=1"
            );
        } else {
            intent.putExtra("url", "http://www.philipyoung.ca/philslab/astroforecastXML.php" +
                            "?obs="+ urlObservatory +"&latlng="+ urlLatLon +"&moon&verbose"
            );
        }
        /* intent.putExtra("url", "http://www.newthinktank.com/wordpress/lotr.txt" ); */
        this.startService(intent);
        fabReload.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sync_disabled_antares_24dp));
        fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_disabled));
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( TAG, "Service Received");
            showFileContents();
        }
    };

    public void showFileContents() {
        StringBuilder stringBuilder;
        try {
            FileInputStream fileInputStream = this.openFileInput("myFile");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine())!=null ) {
                stringBuilder.append(line).append("\n");
            }
            if( stringBuilder.length()>1 ) {
                AstroWebServices astroWebServices = new AstroWebServices(mContext);
                astroWebServices.parseXML();
                downloadEditText.setText(stringBuilder.toString());
                // downloadEditText.setVisibility(View.VISIBLE);
                if(stringBuilder.toString().contains("<viewing")) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloadEditText.setVisibility(View.INVISIBLE);
                            startFileService(downloadEditText,"events");
                        }
                    },INIT_DELAY);
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloadEditText.setVisibility(View.INVISIBLE);
                        }
                    },VERY_LONG_DELAY);
                }
                setWeather();
                setMoonPhase();
                setEvents();
                fabReload.setImageDrawable(mContext.getResources().getDrawable(android.R.drawable.ic_popup_sync));
                fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_sync));
            } else {
                Toast.makeText(mContext,mContext.getString(R.string.ws_no_internet),Toast.LENGTH_LONG).show();
                fabReload.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sync_problem_antares_24dp));
                fabReload.setContentDescription(mContext.getString(R.string.main_screen_reload_fab_description_problem));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        MenuSelection menuSelection = new MenuSelection();
        menuSelection.doSettingSelection(ActivityMain.this, item);

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        MenuSelection menuSelection = new MenuSelection();
        menuSelection.doMenuSelection(ActivityMain.this, item);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setMoonPhase() {
        // set the phase of the Moon
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String strMoonPhase = sharedPreferences.getString(MOON_PHASE_KEY,"No Moon");
        View vwMoon = findViewById(R.id.imageViewMoon),
                vwWeather = findViewById(R.id.overlayViewWeather);
        // findViewById(R.id.overlayViewWeather).setVisibility(View.INVISIBLE);
        if(vwMoon!=null && vwWeather!=null
                && vwMoon instanceof ImageView
                && vwWeather instanceof ImageView) {
            switch (strMoonPhase) {
                case MOON_PHASE_0_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_0);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_0));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase());
                    break;
                case MOON_PHASE_1_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_1);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_1));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+ " "+ getMoonMonthName(new Date())+
                                    " "+getWeather());
                    break;
                case MOON_PHASE_2_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_2);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_2));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+
                                    " "+getWeather());
                    break;
                case MOON_PHASE_3_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_3);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_3));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+
                                    " "+getWeather());
                    break;
                case MOON_PHASE_4_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_4);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_4));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonMonthName(new Date())+
                                    " "+getWeather());
                    break;
                case MOON_PHASE_5_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_5);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_5));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+
                                    " "+getWeather());
                    break;
                case MOON_PHASE_6_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_6);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_6));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+
                                    " "+getWeather());
                    break;
                case MOON_PHASE_7_KEY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_7);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_7));
                    }
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+
                                    " "+getWeather());
                    break;
                default:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwMoon).setImageResource(R.drawable.ic_astro_moon_phase_0);
                        ((ImageView) vwWeather).setImageResource(R.drawable.ic_no_data_overlay_grey_24dp);
                    } else {
                        ((ImageView) vwMoon).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_moon_phase_0));
                        ((ImageView) vwWeather).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_no_data_overlay_grey_24dp));
                    }
                    vwWeather.setVisibility(View.VISIBLE);
                    vwMoon.setContentDescription(
                            mContext.getString(R.string.main_screen_moon_image_description)+
                                    " "+getMoonPhase()+
                                    " "+getWeather());
                    break;
            }
        }
    }
    private String getMoonPhase() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String strMoonPhase = sharedPreferences.getString(MOON_PHASE_KEY,"No Moon"),
                strMoonPhaseName = "";
        switch (strMoonPhase) {
            case MOON_PHASE_0_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_0);
                break;
            case MOON_PHASE_1_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_1);
                break;
            case MOON_PHASE_2_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_2);
                break;
            case MOON_PHASE_3_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_3);
                break;
            case MOON_PHASE_4_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_4);
                break;
            case MOON_PHASE_5_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_5);
                break;
            case MOON_PHASE_6_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_6);
                break;
            case MOON_PHASE_7_KEY:
                strMoonPhaseName = getString(R.string.moon_phase_7);
                break;
            default:
                strMoonPhaseName = getString(R.string.moon_phase_null);
                break;
        }
        return strMoonPhaseName;
    }
    private String getMoonMonthName(Calendar calMonthNumber) {
        String strMoonMonthName = getString(R.string.moon_month_null);
        Integer intMonth,intDay;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String strMoonPhase = sharedPreferences.getString(MOON_PHASE_KEY,"No Moon");
        switch (strMoonPhase) {
            case MOON_PHASE_0_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,1);
                break;
            case MOON_PHASE_1_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-3);
                break;
            case MOON_PHASE_2_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-6);
                break;
            case MOON_PHASE_3_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-10);
                break;
            case MOON_PHASE_4_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-14);
                break;
            case MOON_PHASE_5_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-17);
                break;
            case MOON_PHASE_6_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-21);
                break;
            case MOON_PHASE_7_KEY:
                calMonthNumber.add(Calendar.DAY_OF_MONTH,-25);
                break;
            default:
                break;
        }
        intMonth = calMonthNumber.get(Calendar.MONTH);
        intDay = calMonthNumber.get(Calendar.DAY_OF_MONTH);
        if(intDay<21) {
            switch (intMonth) {
                case Calendar.JANUARY:
                    strMoonMonthName = getString(R.string.moon_month_01);
                    break;
                case Calendar.FEBRUARY:
                    strMoonMonthName = getString(R.string.moon_month_02);
                    break;
                case Calendar.MARCH:
                    strMoonMonthName = getString(R.string.moon_month_03);
                    break;
                case Calendar.APRIL:
                    strMoonMonthName = getString(R.string.moon_month_04);
                    break;
                case Calendar.MAY:
                    strMoonMonthName = getString(R.string.moon_month_05);
                    break;
                case Calendar.JUNE:
                    strMoonMonthName = getString(R.string.moon_month_06);
                    break;
                case Calendar.JULY:
                    strMoonMonthName = getString(R.string.moon_month_07);
                    break;
                case Calendar.AUGUST:
                    strMoonMonthName = getString(R.string.moon_month_08);
                    break;
                case Calendar.SEPTEMBER:
                    strMoonMonthName = getString(R.string.moon_month_09);
                    break;
                case Calendar.OCTOBER:
                    strMoonMonthName = getString(R.string.moon_month_10);
                    break;
                case Calendar.NOVEMBER:
                    strMoonMonthName = getString(R.string.moon_month_11);
                    break;
                case Calendar.DECEMBER:
                    strMoonMonthName = getString(R.string.moon_month_12);
                    break;
                default:
                    strMoonMonthName = getString(R.string.moon_month_null);
                    break;
            }
        } else {
            switch (intMonth) {
                case Calendar.JANUARY:
                    strMoonMonthName = getString(R.string.moon_month_02);
                    break;
                case Calendar.FEBRUARY:
                    strMoonMonthName = getString(R.string.moon_month_03);
                    break;
                case Calendar.MARCH:
                    strMoonMonthName = getString(R.string.moon_month_04);
                    break;
                case Calendar.APRIL:
                    strMoonMonthName = getString(R.string.moon_month_05);
                    break;
                case Calendar.MAY:
                    strMoonMonthName = getString(R.string.moon_month_06);
                    break;
                case Calendar.JUNE:
                    strMoonMonthName = getString(R.string.moon_month_07);
                    break;
                case Calendar.JULY:
                    strMoonMonthName = getString(R.string.moon_month_08);
                    break;
                case Calendar.AUGUST:
                    strMoonMonthName = getString(R.string.moon_month_09);
                    break;
                case Calendar.SEPTEMBER:
                    strMoonMonthName = getString(R.string.moon_month_10);
                    break;
                case Calendar.OCTOBER:
                    strMoonMonthName = getString(R.string.moon_month_11);
                    break;
                case Calendar.NOVEMBER:
                    strMoonMonthName = getString(R.string.moon_month_12);
                    break;
                case Calendar.DECEMBER:
                    strMoonMonthName = getString(R.string.moon_month_01);
                    break;
                default:
                    strMoonMonthName = getString(R.string.moon_month_null);
                    break;
            }
        }
        return strMoonMonthName;
    }
    private String getMoonMonthName(Date dteMonthDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteMonthDate);
        return getMoonMonthName(calendar);
    }
    private String getMoonMonthName(Long dteMonthDate) {
        Date date = new Date(dteMonthDate);
        return getMoonMonthName(dteMonthDate);
    }
    private String getMoonMonthName(Integer intMonthNumber) {
        String strMoonMonthName;
        switch (intMonthNumber) {
            case 1:
                strMoonMonthName = getString(R.string.moon_month_01);
                break;
            case 2:
                strMoonMonthName = getString(R.string.moon_month_02);
                break;
            case 3:
                strMoonMonthName = getString(R.string.moon_month_03);
                break;
            case 4:
                strMoonMonthName = getString(R.string.moon_month_04);
                break;
            case 5:
                strMoonMonthName = getString(R.string.moon_month_05);
                break;
            case 6:
                strMoonMonthName = getString(R.string.moon_month_06);
                break;
            case 7:
                strMoonMonthName = getString(R.string.moon_month_07);
                break;
            case 8:
                strMoonMonthName = getString(R.string.moon_month_08);
                break;
            case 9:
                strMoonMonthName = getString(R.string.moon_month_09);
                break;
            case 10:
                strMoonMonthName = getString(R.string.moon_month_10);
                break;
            case 11:
                strMoonMonthName = getString(R.string.moon_month_11);
                break;
            case 12:
                strMoonMonthName = getString(R.string.moon_month_12);
                break;
            default:
                strMoonMonthName = getString(R.string.moon_month_null);
                break;
        }
        return strMoonMonthName;
    }
    private String getMoonTimes() {
        String strMoonTimes = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null) {
            Long lngRise = sharedPreferences.getLong(MOON_RISE_KEY,0L)*1000L,
                    lngSet = sharedPreferences.getLong(MOON_SET_KEY,0L)*1000L;
            String strRise="", strSet="";
            if(lngRise>((new Date()).getTime()-ONE_HALF_DAY*1000L)) {
                strRise += "\n" + mContext.getString(R.string.dialog_pop_up_weather_moon_rise) +
                        " : " + String.format(
                                mContext.getString(R.string.dialog_pop_up_moon_time_format),
                                new Date(lngRise)
                );
            }
            if(lngSet>((new Date()).getTime()-ONE_HALF_DAY*1000L)) {
                strSet += "\n" + mContext.getString(R.string.dialog_pop_up_weather_moon_set) +
                        " : " + String.format(
                                mContext.getString(R.string.dialog_pop_up_moon_time_format),
                                new Date(lngSet)
                );
            }
            if(lngSet<lngRise) {
                strMoonTimes += strSet + strRise;
            } else {
                strMoonTimes += strRise + strSet;
            }
        }
        return strMoonTimes;
    }
    private String getWeather() {
        String strWeather = "";
        Long dteNow = new Date().getTime(), dteLast=0L;
        Long[] dtesWeather;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null) {
            AstroDatabase astroDatabase = new AstroDatabase(mContext, sharedPreferences.getString(OBSERVATORY_KEY, null));
            Map<Long, String> mapWeather = astroDatabase.getWeather();
            astroDatabase.astroDBclose();
            dtesWeather = new Long[mapWeather.size()];
            Integer intI = 0;
            for (Long dteWeather : mapWeather.keySet()) {
                dtesWeather[intI++] = dteWeather;
                Boolean blnContinue;
                do {
                    blnContinue = false;
                    for (int intJ = 1; intJ < intI; intJ++) {
                        if(dtesWeather[intJ-1]>dtesWeather[intJ]) {
                            Long lngJ = dtesWeather[intJ-1];
                            dtesWeather[intJ-1] = dtesWeather[intJ];
                            dtesWeather[intJ] = lngJ;
                            blnContinue = true;
                        }
                    }
                } while (blnContinue);
            }
            for (Long dteWeather : dtesWeather) {
                Date dteWeatherTime = new Date(dteWeather * 1000L);
                if(dteLast==0L && dteWeatherTime.getTime()>dteNow) dteLast=dteWeather+3600*3;
                if(dteWeather>dteNow/1000 && dteWeather<=dteLast) {
                    strWeather += "\n" + String.format(getString(R.string.dialog_pop_up_weather_time_format),dteWeatherTime) + " : " + mapWeather.get(dteWeather);
                }
            }
        }
        return strWeather;
    }
    private String getSatelliteTimes(String strType) {
        String strSatelliteTimes = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
        Integer intISSCount = astroDatabase.getSatelliteCountTonightsISS();
        Integer intIridiumCount = astroDatabase.getSatelliteCountTonightsIridium();
        String[] strsTonightsSatellites = astroDatabase.getTonightsSatellites();
        astroDatabase.astroDBclose();
        if(strType.equals("ISS")) {
            if(intISSCount==0 || strsTonightsSatellites.length==0) {
                strSatelliteTimes = "\n" + mContext.getString(R.string.dialog_pop_up_satellite_no_passes);
            } else {
                for (String strSatellite : strsTonightsSatellites) {
                    String[] strsSatellite = strSatellite.split(",");
                    if(strsSatellite.length==6 && strsSatellite[1].equals("ISS")) {
                        Date dteSatellitePeak = new Date(Long.valueOf(strsSatellite[4])*1000);
                        strSatelliteTimes += "\n" + String.format(
                                getString(R.string.dialog_pop_up_satellite_time_format),
                                dteSatellitePeak
                        ) + " : " + strsSatellite[0];
                    }
                }
            }
        } else if(strType.equals("Iridium")) {
            if(intIridiumCount==0 || strsTonightsSatellites.length==0) {
                strSatelliteTimes = "\n" + mContext.getString(R.string.dialog_pop_up_satellite_no_passes);
            } else {
                for (String strSatellite : strsTonightsSatellites) {
                    String[] strsSatellite = strSatellite.split(",");
                    if(strsSatellite.length==6 && strsSatellite[1].equals("Iridium")) {
                        Date dteSatellitePeak = new Date(Long.valueOf(strsSatellite[4])*1000);
                        strSatelliteTimes += "\n" + String.format(
                                getString(R.string.dialog_pop_up_satellite_time_format),
                                dteSatellitePeak
                        ) + " : " + strsSatellite[0];
                    }
                }
            }
        } else {
            if(strsTonightsSatellites.length==0) {
                strSatelliteTimes = "\n" + mContext.getString(R.string.dialog_pop_up_satellite_no_passes);
            } else {
                for (String strSatellite : strsTonightsSatellites) {
                    String[] strsSatellite = strSatellite.split(",");
                    if(strsSatellite.length==6) {
                        Date dteSatellitePeak = new Date(Long.valueOf(strsSatellite[4])*1000);
                        strSatelliteTimes += "\n" + String.format(
                                getString(R.string.dialog_pop_up_satellite_time_format),
                                dteSatellitePeak
                        ) + " : " + strsSatellite[0];
                    }
                }
            }
        }
        return strSatelliteTimes;
    }
    private void setWeather() {
        // make the clouds appear if inclement evening
        ImageView vwMoon = findViewById(R.id.imageViewMoon),
                vwWeather = findViewById(R.id.overlayViewWeather);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null && vwWeather!=null) {
            AstroDatabase astroDatabase = new AstroDatabase(mContext, sharedPreferences.getString(OBSERVATORY_KEY, null));
            switch (astroDatabase.getEveningWeather()) {
                case 0:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_astro_cloud_9);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_cloud_9));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 1:
                case 2:
                case 3:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_astro_cloud_5);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_cloud_5));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 4:
                case 5:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_astro_cloud_3);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_cloud_3));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 6:
                case 7:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_astro_cloud_1);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_cloud_1));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 8:
                case 9:
                case 10:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_astro_clear);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_clear));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 108:
                case 109:
                case 110:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_hazy_1_24dp);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_hazy_1_24dp));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 208:
                case 209:
                case 210:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_hazy_2_24dp);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_hazy_2_24dp));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 308:
                case 309:
                case 310:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_hazy_3_24dp);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_hazy_3_24dp));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 408:
                case 409:
                case 410:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_hazy_4_24dp);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_hazy_4_24dp));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                case 508:
                case 509:
                case 510:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwWeather.setImageResource(R.drawable.ic_astro_clear);
                    } else {
                        vwWeather.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_clear));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    vwWeather.setVisibility(View.VISIBLE);
                    break;
                default:
                    // do nothing
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) findViewById(R.id.overlayViewWeather)).setImageResource(R.drawable.ic_no_data_overlay_white_24dp);
                    } else {
                        ((ImageView) findViewById(R.id.overlayViewWeather)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_no_data_overlay_white_24dp));
                    }
                    vwMoon.setContentDescription(mContext.getString(R.string.main_screen_moon_image_description)+
                            " "+getMoonPhase()+
                            " "+mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null));
                    findViewById(R.id.overlayViewWeather).setVisibility(View.VISIBLE);
                    break;
            }
            astroDatabase.astroDBclose();
            astroDatabase = null;
        }
    }

    private void setEvents() {
        ImageView imageView;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPreferences!=null) {
            ((Activity) mContext).setTitle(sharedPreferences.getString(OBSERVATORY_KEY, getString(R.string.title_activity_main)));
            AstroDatabase astroDatabase = new AstroDatabase(mContext, sharedPreferences.getString(OBSERVATORY_KEY, null));

            // Update conjunctions
            Integer intCountConjunctions = astroDatabase.getConjunctionsCount(
                    Float.valueOf(
                            sharedPreferences
                                    .getString(CONJUNCTION_DISTANCE_KEY, "2.5")
                                    .replace("°", "")
                    )
            );
            if (intCountConjunctions > 0) {
                imageView = findViewById(R.id.alertViewPlanet);
                if (imageView != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imageView.setImageResource(R.drawable.ic_astro_conjunctions);
                    } else {
                        imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_conjunctions));
                    }
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setContentDescription(getString(R.string.pref_description_key_3_conjunctions));
                }
            } else {
                imageView = findViewById(R.id.alertViewPlanet);
                if (imageView != null) {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
            findViewById(R.id.overlayViewPlanet).setVisibility(View.INVISIBLE);

            // Update Satellites
            Integer intCountISS = astroDatabase.getSatelliteCountTonightsISS(),
                    intCountIridium = astroDatabase.getSatelliteCountTonightsIridium();
            String[] strsTonightsSatellites = astroDatabase.getTonightsSatellites();
            // is there an ISS tonight?
            if (intCountISS > 0) {
                imageView = findViewById(R.id.imageViewSatellite);
                if (imageView != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imageView.setImageResource(R.drawable.ic_satellite_iss);
                    } else {
                        imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_satellite_iss));
                    }
                    imageView.setContentDescription(mContext.getString(R.string.main_screen_satellite_image_description_iss));
                }
                ImageView vwOverlay = findViewById(R.id.overlayViewSatellite);
                if(vwOverlay != null) vwOverlay.setVisibility(View.INVISIBLE);
            } else {
                imageView = findViewById(R.id.imageViewSatellite);
                ImageView vwOverlay = findViewById(R.id.overlayViewSatellite);
                if (imageView != null && vwOverlay != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imageView.setImageResource(R.drawable.ic_astro_satellites);
                    } else {
                        imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_satellites));
                    }
                    if (strsTonightsSatellites.length == 0 && intCountISS == 0 && intCountIridium == 0) {
                        // Where did all the satellites go?
                        if (vwOverlay != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                vwOverlay.setImageResource(R.drawable.ic_no_data_overlay_white_24dp);
                            } else {
                                vwOverlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_no_data_overlay_white_24dp));
                            }
                            vwOverlay.setVisibility(View.VISIBLE);
                        }
                        imageView.setContentDescription(mContext.getString(R.string.main_screen_satellite_image_description_no_pass));
                    } else {
                        findViewById(R.id.overlayViewSatellite).setVisibility(View.INVISIBLE);
                        imageView.setContentDescription(mContext.getString(R.string.main_screen_satellite_image_description));
                    }
                }
            }
            // is there a flare tonight?
            if (intCountIridium > 0) {
                ImageView vwAlert = findViewById(R.id.alertViewSatellite);
                if (imageView != null && vwAlert != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vwAlert.setImageResource(R.drawable.ic_iridium_flare_yellow_24dp);
                    } else {
                        vwAlert.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_iridium_flare_yellow_24dp));
                    }
                    vwAlert.setVisibility(View.VISIBLE);
                    imageView.setContentDescription(imageView.getContentDescription() +
                            " " + mContext.getString(R.string.main_screen_satellite_alert_description_iridium));
                }
            } else {
                imageView = findViewById(R.id.alertViewSatellite);
                if (imageView != null) {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }

            astroDatabase.astroDBclose();
            astroDatabase = null;

            // Update Aurora
            imageView = findViewById(R.id.imageViewAurora);
            if (imageView != null) {
                findViewById(R.id.overlayViewAurora).setVisibility(View.VISIBLE);
                switch (sharedPreferences.getString(AURORA_LEVEL_KEY, "-1")) {
                    case "0":
                    case "1":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageResource(R.drawable.ic_astro_aurora_0);
                        } else {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_aurora_0));
                        }
                        imageView.setContentDescription(mContext.getString(R.string.dialog_pop_up_aurora_hint) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_status) +
                                " " + sharedPreferences.getString(AURORA_LEVEL_KEY, mContext.getString(R.string.dialog_pop_up_aurora_kp_default)));
                        findViewById(R.id.overlayViewAurora).setVisibility(View.INVISIBLE);
                        break;
                    case "2":
                    case "3":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageResource(R.drawable.ic_astro_aurora_2);
                        } else {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_aurora_2));
                        }
                        imageView.setContentDescription(mContext.getString(R.string.dialog_pop_up_aurora_hint) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_status) +
                                " " + sharedPreferences.getString(AURORA_LEVEL_KEY, mContext.getString(R.string.dialog_pop_up_aurora_kp_default)));
                        findViewById(R.id.overlayViewAurora).setVisibility(View.INVISIBLE);
                        break;
                    case "4":
                    case "5":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageResource(R.drawable.ic_astro_aurora_4);
                        } else {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_aurora_4));
                        }
                        imageView.setContentDescription(mContext.getString(R.string.dialog_pop_up_aurora_hint) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_status) +
                                " " + sharedPreferences.getString(AURORA_LEVEL_KEY, mContext.getString(R.string.dialog_pop_up_aurora_kp_default)));
                        findViewById(R.id.overlayViewAurora).setVisibility(View.INVISIBLE);
                        break;
                    case "6":
                    case "7":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageResource(R.drawable.ic_astro_aurora_6);
                        } else {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_aurora_6));
                        }
                        imageView.setContentDescription(mContext.getString(R.string.dialog_pop_up_aurora_hint) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_status) +
                                " " + sharedPreferences.getString(AURORA_LEVEL_KEY, mContext.getString(R.string.dialog_pop_up_aurora_kp_default)));
                        findViewById(R.id.overlayViewAurora).setVisibility(View.INVISIBLE);
                        break;
                    case "8":
                    case "9":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageResource(R.drawable.ic_astro_aurora_8);
                        } else {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_aurora_8));
                        }
                        imageView.setContentDescription(mContext.getString(R.string.dialog_pop_up_aurora_hint) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_status) +
                                " " + sharedPreferences.getString(AURORA_LEVEL_KEY, mContext.getString(R.string.dialog_pop_up_aurora_kp_default)));
                        findViewById(R.id.overlayViewAurora).setVisibility(View.INVISIBLE);
                        break;
                    default:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageResource(R.drawable.ic_astro_aurora_0);
                        } else {
                            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_aurora_0));
                        }
                        findViewById(R.id.overlayViewAurora).setVisibility(View.VISIBLE);
                        imageView.setContentDescription(mContext.getString(R.string.dialog_pop_up_aurora_hint) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_status) +
                                " " + mContext.getString(R.string.dialog_pop_up_aurora_kp_default));
                        break;
                }
            }
            View vwAlert = findViewById(R.id.alertViewAurora);
            if (vwAlert != null && vwAlert instanceof ImageView) {
                if (sharedPreferences.getFloat(AURORA_FORECAST_KEY, 0f) > 50f) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwAlert).setImageResource(R.drawable.ic_sun_watch_alert_yellow_24dp);
                    } else {
                        ((ImageView) vwAlert).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_watch_alert_yellow_24dp));
                    }
                } else if (sharedPreferences.getFloat(AURORA_FORECAST_KEY, 0f) > 25f) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwAlert).setImageResource(R.drawable.ic_sun_active_alert_yellow_24dp);
                    } else {
                        ((ImageView) vwAlert).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_active_alert_yellow_24dp));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((ImageView) vwAlert).setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
                    } else {
                        ((ImageView) vwAlert).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
                    }
                }
            }
        }
    }
    private void showPlanetPopUp() {
        String strTitle = mContext.getString(R.string.dialog_pop_up_planet_title);
        String strMessage = getString(R.string.dialog_pop_up_planet_message);
        final PopUpObjects popUp = new PopUpObjects(mContext,PLANET_TYPE_KEY);
        popUp.setCancelable(true);
        popUp.show();
        popUp.setDialog(popUp);
        popUp.setTitle(strTitle);
        popUp.setMessage(strMessage);
        popUp.setPositiveButtonText(R.string.dialog_pop_up_planet_positive);
        popUp.setPositiveClick(PLANET_POP_UP_KEY);
        popUp.setCancelClick(DIALOG_DISMISS);
    }
    private void showConjunctionPopUp() {
        String strTitle = mContext.getString(R.string.dialog_pop_up_conjunctions_title);
        String strMessage = getString(R.string.dialog_pop_up_conjunctions_message);
        final PopUpObjects popUp = new PopUpObjects(mContext,CONJUNCTION_TYPE_KEY);
        popUp.setCancelable(true);
        popUp.show();
        popUp.setDialog(popUp);
        popUp.setTitle(strTitle);
        popUp.setMessage(strMessage);
        popUp.setPositiveButtonText(R.string.dialog_pop_up_conjunctions_positive);
        popUp.setPositiveClick(CONJUNCTION_POP_UP_KEY);
        popUp.setCancelClick(DIALOG_DISMISS);
    }
    private void showCometPopUp() {
        String strTitle = mContext.getString(R.string.dialog_pop_up_comet_title);
        String strMessage = getString(R.string.dialog_pop_up_comet_message);
        final PopUpObjects popUp = new PopUpObjects(mContext,COMET_TYPE_KEY);
        popUp.setCancelable(true);
        popUp.show();
        popUp.setDialog(popUp);
        popUp.setTitle(strTitle);
        popUp.setMessage(strMessage);
        popUp.setCancelClick(DIALOG_DISMISS);
    }
    private void showSatellitePopUp() {
        String strTitle = mContext.getString(R.string.dialog_pop_up_satellite_title);
        String strMessage = getString(R.string.dialog_pop_up_satellite_message_normal)+
                getSatelliteTimes("normal");
        final PopUpApps popUp = new PopUpApps(mContext,SATELLITE_TYPE_KEY);
        popUp.setCancelable(true);
        popUp.show();
        popUp.setDialog(popUp);
        popUp.setTitle(strTitle);
        popUp.setMessage(strMessage);
        popUp.setPositiveButtonText(R.string.dialog_pop_up_satellite_positive);
        popUp.setPositiveClick(SATELLITE_LAUNCH_KEY);
        popUp.setCancelClick(DIALOG_DISMISS);
    }
    private void showSatelliteISSPopUp() {
        String strTitle = mContext.getString(R.string.dialog_pop_up_satellite_title);
        String strMessage = getString(R.string.dialog_pop_up_satellite_message_iss)+
                getSatelliteTimes("ISS");
        final PopUpApps popUp = new PopUpApps(mContext,SATELLITE_TYPE_KEY);
        popUp.setCancelable(true);
        popUp.show();
        popUp.setDialog(popUp);
        popUp.setTitle(strTitle);
        popUp.setMessage(strMessage);
        popUp.setPositiveButtonText(R.string.dialog_pop_up_satellite_positive);
        popUp.setPositiveClick(SATELLITE_LAUNCH_KEY);
        popUp.setCancelClick(DIALOG_DISMISS);
    }
    private void showSatelliteIridiumPopUp() {
        String strTitle = mContext.getString(R.string.dialog_pop_up_satellite_title);
        String strMessage = getString(R.string.dialog_pop_up_satellite_message_iridium)+
                getSatelliteTimes("Iridium");
        final PopUpApps popUp = new PopUpApps(mContext,SATELLITE_TYPE_KEY);
        popUp.setCancelable(true);
        popUp.show();
        popUp.setDialog(popUp);
        popUp.setTitle(strTitle);
        popUp.setMessage(strMessage);
        popUp.setPositiveButtonText(R.string.dialog_pop_up_satellite_positive);
        popUp.setPositiveClick(SATELLITE_LAUNCH_KEY);
        popUp.setCancelClick(DIALOG_DISMISS);
    }
}

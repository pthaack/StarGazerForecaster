package ca.philipyoung.astroforecast.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.philipyoung.astroforecast.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Philip Young on 2018-06-02.
 * Connect to database and do CRUD
 *
 * ToDone: create and update the tables here
 *
 * Creates a record class that can be used in ListView items.
 * Handles all database requests.
 */

public class AstroDatabase {
    private static final String TAG = "AstroDatabase";
    private SQLiteDatabase astroDB = null;
    private Context mContext;
    private Integer idObservatory=-1;
    HashMap<String,String> mapObservatory = new HashMap<>();

    private static final int SPLASH_TIME_OUT = 500;  // Set timeout for splash page to 4 seconds
    private static final int LONG_DELAY = 1500; // Assumed length of delay for Toast.LENGTH_LONG, 3½ seconds
    private static final int SHORT_DELAY = 500; // Assumed length of delay for Toast.LENGTH_SHORT, 2 seconds
    private static final Long ONE_HOUR = 3600L; // the number of seconds in an hour.
    private static final Long ONE_FULL_DAY = 86400L; // the number of seconds in one day.
    private static final Long ONE_HALF_DAY = 43200L; // the number of seconds in half a day.
    private static final Long ONE_FULL_WEEK = 604800L; // the number of seconds in one week.
    private static final Double GPS_MINIMUM_DISTANCE = 0.015;
    private static final Double GPS_MAXIMUM_SNAP_DISTANCE = 0.025;

    // shared preference keys
    private static final String CONJUNCTION_DISTANCE_KEY = "notifications_key_3_conjunctions_distance";
    private static final String CONJUNCTION_PLANET_KEY = "notifications_key_3_planets";
    private static final String AURORA_KEY = "notifications_key_5";
    private static final String SUNTIMES_KEY = "notifications_key_9";
    private static final String MOONTIMES_KEY = "notifications_key_2";

    // static ic planet strings
    private static final String CONJUNCTION_PLANET_MOON_KEY = "Moon";
    private static final String CONJUNCTION_PLANET_MERCURY_KEY = "Mercury";
    private static final String CONJUNCTION_PLANET_VENUS_KEY = "Venus";
    private static final String CONJUNCTION_PLANET_MARS_KEY = "Mars";
    private static final String CONJUNCTION_PLANET_JUPITER_KEY = "Jupiter";
    private static final String CONJUNCTION_PLANET_SATURN_KEY = "Saturn";
    private static final String CONJUNCTION_PLANET_URANUS_KEY = "Uranus";
    private static final String CONJUNCTION_PLANET_NEPTUNE_KEY = "Neptune";

    public AstroDatabase(Context context) {
        // Instantiate and open database
        this.mContext = context;
        astroDBopen();
    }

    public AstroDatabase(Context context, String observatory) {
        // Instantiate and open database and get observatory
        this.mContext = context;
        astroDBopen();
        this.idObservatory=Integer.valueOf((this.getObservatory(observatory)).get("id"));
    }

    public void astroDBopen() {
        // Reopen database
        try{
            astroDB = mContext.openOrCreateDatabase("AstroForecast", MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void astroDBclose() {
        astroDB.close();
    }

    public Integer astroSetupAndClose() {
        // Create tables and update fields in database
        // this.astroDBopen(); // creating the AstroDatabase class object opens the database automatically

        int intSplashTimeOut = SPLASH_TIME_OUT;
        try {
            String strQuery;
            String strQryAlter;
            Cursor cursor;
            int ndxColumn;

            /**
             * Table astroLocation
             *    location_name     - name to give to the location as indicated by user
             *    nearby_city       - name of a nearby city if off-map weather service is used
             *    geo_latitude    - decimal coordinate
             *    geo_longitude   - decimal coordinate
             *    geo_altitude    - decimal coordinate
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroLocation "
                    +"(id INTEGER primary key, "
                    +"location_name VARCHAR, "
                    +"nearby_city VARCHAR, "
                    +"geo_latitude REAL, "
                    +"geo_longitude REAL, "
                    +"geo_altitude REAL "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroLocation", null);
            ndxColumn = cursor.getColumnIndex("nearby_city");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroLocation ADD "
                        + "nearby_city VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            ndxColumn = cursor.getColumnIndex("geo_latitude");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroLocation ADD "
                        + "geo_latitude VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            ndxColumn = cursor.getColumnIndex("geo_longitude");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroLocation ADD "
                        + "geo_longitude VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            ndxColumn = cursor.getColumnIndex("geo_altitude");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroLocation ADD "
                        + "geo_altitude VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroLocation-" + strDebug);
                }
                while (cursor.moveToNext());
            }
            cursor.close();

            /**
             * Table astroWeather
             *    location_id       - id given to the location by system
             *    weather_start     - date and time when the weather report starts
             *    weather_stop      - date and time when the weather report stops, could be an hour or not
             *    twilight          - level of twilight: dusk/dawn, civil, nautical, astronomical, dark
             *    cloud_cover       - cloud cover rating
             *    transparency      - transparency rating
             *    seeing            - seeing rating
             *    wind              - wind rating
             *    humidity          - humidity rating
             *    temperature       - temperature rating
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroWeather "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"weather_start LONG, "
                    +"weather_stop LONG, "
                    +"twilight VARCHAR, "
                    +"cloud_cover INTEGER, "
                    +"transparency INTEGER, "
                    +"seeing INTEGER, "
                    +"wind INTEGER, "
                    +"humidity INTEGER, "
                    +"temperature INTEGER "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroWeather", null);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroWeather-" + strDebug);
                }
                while (cursor.moveToNext());
            }
            cursor.close();

            /**
             * Table astroWeatherSummary
             *    location_id       - id given to the location by system
             *    weather_start     - date and time when the weather report starts
             *    weather_stop      - date and time when the weather report stops, could be an hour or not
             *    twilight          - level of twilight: dusk/dawn, civil, nautical, astronomical, dark
             *    summary           - in general, is it worth going out to look?
             *    cloud_cover       - cloud cover average rating
             *    transparency      - transparency average rating
             *    seeing            - seeing average rating
             *    wind              - wind average rating
             *    humidity          - humidity average rating
             *    temperature       - temperature average rating
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroWeatherSummary "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"weather_start LONG, "
                    +"weather_stop LONG, "
                    +"twilight VARCHAR, "
                    +"summary VARCHAR "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroWeatherSummary", null);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroWeatherSummary-" + strDebug);
                }
                while (cursor.moveToNext());
            } else
                Log.d(TAG, "astroSetupAndClose: astroWeatherSummary" );
            cursor.close();

            /**
             * Table astroPlanets
             *    location_id       - id given to the location by system
             *    planet_name       - name of the primary planet object in conjunction
             *    planet_ra         - decimal coordinate, right ascension (in hours)
             *    planet_decl       - decimal coordinate, declination (in degrees)
             *    planet_start - date and time when the sky is dark enough to see the conjunction
             *    planet_rise  - date and time when both objects have risen above the horizon
             *    planet_set   - date and time when either object approaches the horizon
             *    planet_stop  - date and time when the sky gets bright enough to extinguish one of the objects
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroPlanets "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"planet_name VARCHAR, "
                    +"planet_ra REAL, "
                    +"planet_decl REAL, "
                    +"planet_start LONG, "
                    +"planet_rise LONG, "
                    +"planet_set LONG, "
                    +"planet_stop LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroPlanets", null);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroPlanets-" + strDebug);
                }
                while (cursor.moveToNext());
            } else
                Log.d(TAG, "astroSetupAndClose: astroPlanets");
            cursor.close();

            /**
             * Table astroEventsConjunctions
             *    location_id       - id given to the location by system
             *    planet_name       - name of the primary planet object in conjunction
             *    planet_ra         - decimal coordinate, right ascension (in hours)
             *    planet_decl       - decimal coordinate, declination (in degrees)
             *    conjunction_name  - name of the secondary object (planet, moon, or star) in conjunction
             *    conjunction_ra    - decimal coordinate, right ascension (in hours)
             *    conjunction_decl  - decimal coordinate, declination (in degrees)
             *    angular_distance  - distance between objects in degrees
             *    conjunction_start - date and time when the sky is dark enough to see the conjunction
             *    conjunction_rise  - date and time when both objects have risen above the horizon
             *    conjunction_set   - date and time when either object approaches the horizon
             *    conjunction_stop  - date and time when the sky gets bright enough to extinguish one of the objects
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroEventsConjunctions "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"planet_name VARCHAR, "
                    +"planet_ra REAL, "
                    +"planet_decl REAL, "
                    +"conjunction_name VARCHAR, "
                    +"conjunction_ra REAL, "
                    +"conjunction_decl REAL, "
                    +"angular_distance REAL, "
                    +"conjunction_start LONG, "
                    +"conjunction_rise LONG, "
                    +"conjunction_set LONG, "
                    +"conjunction_stop LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroEventsConjunctions", null);
            cursor.moveToFirst();
            ndxColumn = cursor.getColumnIndex("planet_ra");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroEventsConjunctions ADD "
                        + "planet_ra VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            ndxColumn = cursor.getColumnIndex("planet_decl");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroEventsConjunctions ADD "
                        + "planet_decl VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            ndxColumn = cursor.getColumnIndex("conjunction_ra");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroEventsConjunctions ADD "
                        + "conjunction_ra VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            ndxColumn = cursor.getColumnIndex("conjunction_decl");
            if( ndxColumn == -1 ) {
                strQryAlter = "ALTER TABLE astroEventsConjunctions ADD "
                        + "conjunction_decl VARCHAR;";
                astroDB.execSQL(strQryAlter);
                Toast.makeText(mContext, mContext.getString(R.string.db_toast_updating), Toast.LENGTH_SHORT).show();
                intSplashTimeOut += SHORT_DELAY;
            }
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroEventsConjunctions-" + strDebug);
                }
                while (cursor.moveToNext());
            } else
                Log.d(TAG, "astroSetupAndClose: astroEventsConjunctions");
            cursor.close();

            /**
             * Table astroEventsSatellites
             *    location_id       - id given to the location by system
             *    vehicle_type      - type of pass based on vehicle used
             *    vehicle_name      - name of vehicle used
             *    pass_magnitude    - observable magnitude at brightest point
             *    pass_rise         - date and time when the vehicle rises or emerges from shadow
             *    pass_peak         - date and time when the vehicle reaches its brightest
             *    pass_set          - date and time when the vehicle sets or passes into shadow
             *    url_link          - link to chart for the pass
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroEventsSatellites "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"vehicle_type VARCHAR, "
                    +"vehicle_name VARCHAR, "
                    +"pass_magnitude REAL, "
                    +"url_link VARCHAR, "
                    +"pass_rise LONG, "
                    +"pass_peak LONG, "
                    +"pass_set LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroEventsSatellites", null);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroEventsSatellites-" + strDebug);
                }
                while (cursor.moveToNext());
            } else
                Log.d(TAG, "astroSetupAndClose: astroEventsSatellites" );
            cursor.close();

            /**
             * Table astroEventsVariables
             *    location_id       - id given to the location by system
             *    variable_name     - name to give to the variable
             *    ra                - decimal coordinate, right ascension (in hours)
             *    decl              - decimal coordinate, declination (in degrees)
             *    time_rise         - time the variable rises
             *    time_set          - time the variable sets
             *    mag_max           - decimal value, estimated magnitude at maximum
             *    mag_min           - decimal value, estimated magnitude at minimum
             *    date_max          - estimated date of maximum
             *    date_min          - estimated date of minimum
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroEventsVariables "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"variable_name VARCHAR, "
                    +"ra REAL, "
                    +"decl REAL, "
                    +"time_rise LONG, "
                    +"time_set LONG, "
                    +"mag_max REAL, "
                    +"mag_min REAL, "
                    +"date_max LONG, "
                    +"date_min LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroEventsVariables", null);
            if(cursor.getColumnIndex("nearby_city")>=0){
                cursor.close();
                strQryAlter = "DROP TABLE astroEventsVariables";
                astroDB.execSQL(strQryAlter);
                astroDB.execSQL(strQuery);
                cursor = astroDB.rawQuery("SELECT * FROM astroEventsVariables", null);
            }
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroEventsVariables-" + strDebug);
                }
                while (cursor.moveToNext());
            } else {
                Log.d(TAG, "astroSetupAndClose: astroEventsVariables");
            }
            cursor.close();

            /**
             * Table astroEventsMeteorShowers
             *    location_id       - id given to the location by system
             *    radiant_name      - name to give to the meteor shower
             *    ra                - decimal coordinate, right ascension (in hours)
             *    decl              - decimal coordinate, declination (in degrees)
             *    time_rise         - time the radiant point rises
             *    time_set          - time the radiant point sets
             *    mag               - decimal value, estimated magnitude of average meteor
             *    rate              - number of visible meteors per hour
             *    date_start        - estimated date the meteor shower begins
             *    date_peak         - estimated date the meteor shower peaks
             *    date_stop         - estimated date the meteor shower stops
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroEventsMeteorShowers "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"radiant_name VARCHAR, "
                    +"ra REAL, "
                    +"decl REAL, "
                    +"time_rise LONG, "
                    +"time_set LONG, "
                    +"mag REAL, "
                    +"rate INTEGER, "
                    +"date_start LONG, "
                    +"date_peak LONG, "
                    +"date_stop LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroEventsMeteorShowers", null);
            if(cursor.getColumnIndex("rate")<0){
                cursor.close();
                strQryAlter = "DROP TABLE astroEventsMeteorShowers";
                astroDB.execSQL(strQryAlter);
                astroDB.execSQL(strQuery);
                cursor = astroDB.rawQuery("SELECT * FROM astroEventsMeteorShowers", null);
            }
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroEventsMeteorShowers-" + strDebug);
                }
                while (cursor.moveToNext());
            } else Log.d(TAG, "astroSetupAndClose: astroEventsMeteorShowers" );
            cursor.close();

            /**
             * Table astroEventsComets
             *    location_id       - id given to the location by system
             *    comet_name        - name to give to the comet
             *    ra                - decimal coordinate, right ascension (in hours)
             *    decl              - decimal coordinate, declination (in degrees)
             *    time_rise         - time the radiant point rises
             *    time_set          - time the radiant point sets
             *    mag               - decimal value, estimated magnitude of comet
             *    date_start        - estimated date the comet appears
             *    date_peak         - estimated date the comet peaks
             *    date_stop         - estimated date the comet is too faint to see
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroEventsComets "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"comet_name VARCHAR, "
                    +"ra REAL, "
                    +"decl REAL, "
                    +"time_rise LONG, "
                    +"time_set LONG, "
                    +"mag REAL, "
                    +"date_start LONG, "
                    +"date_peak LONG, "
                    +"date_stop LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroEventsComets", null);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroEventsComets-" + strDebug);
                }
                while (cursor.moveToNext());
            } else Log.d(TAG, "astroSetupAndClose: astroEventsComets" );
            cursor.close();

            /**
             * Table astroEventsEclipses
             *    location_id       - id given to the location by system
             *    eclipse_name      - name to give to the eclipse, MMMM D, YYYY
             *    eclipse_type      - type of eclipse, lunar/solar, total/partial/hybrid/annular/penumbra
             *    ra                - decimal coordinate, right ascension (in hours)
             *    decl              - decimal coordinate, declination (in degrees)
             *    time_rise         - time the radiant point rises
             *    time_set          - time the radiant point sets
             *    date_start        - estimated date the eclipse penumbra begins
             *    date_umbra_start  - estimated date the eclipse umbra begins
             *    date_totality_start - estimated date the totality begins
             *    date_peak         - estimated date the eclipse peaks
             *    date_totality_stop - estimated date the eclipse totality ends
             *    date_umbra_stop   - estimated date the eclipse umbra ends
             *    date_stop         - estimated date the eclipse penumbra ends
             *
             *    Eclipse - Name, Date, Time, Constellation
             *
             *    Moon -  RA(), Dec(), Alt(), Az(), Rise(), Set(), Distance( RA, Dec )
             *         -  Penumbra Begins, Umbra Begins, Totality begins, Totality Ends, Umbra Ends, Penumbra Ends
             */
            strQuery = "CREATE TABLE IF NOT EXISTS astroEventsEclipses "
                    +"(id INTEGER primary key, "
                    +"location_id INTEGER, "
                    +"eclipse_name VARCHAR, "
                    +"eclipse_type VARCHAR, "
                    +"ra REAL, "
                    +"decl REAL, "
                    +"time_rise LONG, "
                    +"time_set LONG, "
                    +"date_start LONG, "
                    +"date_umbra_start LONG, "
                    +"date_totality_start LONG, "
                    +"date_peak LONG, "
                    +"date_totality_stop LONG, "
                    +"date_umbra_stop LONG, "
                    +"date_stop LONG "
                    +");";
            astroDB.execSQL(strQuery);
            cursor = astroDB.rawQuery("SELECT * FROM astroEventsEclipses", null);
            if(cursor.getColumnIndex("eclipse_type")<0){
                cursor.close();
                strQryAlter = "DROP TABLE astroEventsEclipses";
                astroDB.execSQL(strQryAlter);
                astroDB.execSQL(strQuery);
                cursor = astroDB.rawQuery("SELECT * FROM astroEventsEclipses", null);
            }
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                // What's in the file
                String strDebug;
                do {
                    strDebug = "";
                    for (String strColumn :
                            cursor.getColumnNames()) {
                        ndxColumn = cursor.getColumnIndex(strColumn);
                        strDebug += (strDebug.isEmpty()?"":",") +
                                "{" + strColumn +
                                ":" + cursor.getString(ndxColumn) + "}";
                    }
                    Log.d(TAG, "astroSetupAndClose: astroEventsEclipses-" + strDebug);
                }
                while (cursor.moveToNext());
            } else Log.d(TAG, "astroSetupAndClose: astroEventsEclipses" );
            cursor.close();

            /**
             * Table configs
             * Create backup file for shared preferences
             */

        } catch (Exception e){
            e.printStackTrace();
            Log.e("STASHDB ERROR", "Error Creating Database: " + e.getMessage());
        }

        astroDBclose();
        return intSplashTimeOut;

    }

    // Build a location record object
    public class AstroLocationRecord {
        private Integer intID;
        private String strIdObservatory, strLatitude, strLongitude, strAltitude;
        private Float fltLatitude, fltLongitude, fltAltitude;

        // retrieve this record object
        public void setRecord() {
            String strRowID = Integer.toString(this.intID),
                    strQuery = "SELECT * FROM astroLocation WHERE id=?";
            Cursor cursor = astroDB.rawQuery(strQuery, new String[]{strRowID});
            if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) return;
            Integer ndxColumn = cursor.getColumnIndex("id");
            if( !cursor.isNull(ndxColumn) && cursor.getInt(ndxColumn)>=0 ) {

            }
            cursor.close();

        }
    }

    public HashMap<String,String> getObservatories() {
        Cursor cursor;
        HashMap<String,String> map = new HashMap<>();
        String strQuery, strFieldNames = "id,location_name,nearby_city,geo_latitude," +
                "geo_longitude,geo_altitude";
        strQuery = "SELECT " + strFieldNames + " FROM astroLocation";
        cursor = astroDB.rawQuery(strQuery, null );
        cursor.moveToFirst();
        if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) {
            map.put("-1",mContext.getString(R.string.pref_default_location_name)+","+
                    mContext.getString(R.string.pref_default_display_location)+
                    ",null,null");
        } else {
            Integer ndxID = cursor.getColumnIndex("id");
            Integer ndxLocationName = cursor.getColumnIndex("location_name");
            Integer ndxNearbyCity = cursor.getColumnIndex("nearby_city");
            Integer ndxGeoLatitude = cursor.getColumnIndex("geo_latitude");
            Integer ndxGeoLongitude = cursor.getColumnIndex("geo_longitude");
            Integer ndxGeoAltitude = cursor.getColumnIndex("geo_altitude");
            String strCoordinates;
            Float fltLatitude,fltLongitude;
            if(ndxID>=0 && ndxLocationName>=0 && ndxGeoLatitude>=0 && ndxGeoLongitude>=0 && ndxNearbyCity>=0 && ndxGeoAltitude>=0)
            do {
                if(cursor.isNull(ndxGeoLatitude)||cursor.isNull(ndxGeoLongitude)) {
                    strCoordinates = "null";
                } else {
                    fltLatitude = cursor.getFloat(ndxGeoLatitude);
                    fltLongitude = cursor.getFloat(ndxGeoLongitude);
                    strCoordinates = String.format(
                            Locale.US,
                            "%1$.6f%3$s %2$.6f%4$s",
                            Math.abs(fltLatitude),
                            Math.abs(fltLongitude),
                            fltLatitude>0?"N":"S",
                            fltLongitude>0?"E":"W"
                    );
                }
                if(cursor.getString(ndxLocationName).equals(mContext.getString(R.string.pref_default_location_name))) {
                    map.put("-1",
                            cursor.getString(ndxLocationName) + "," +
                                    strCoordinates + "," +
                                    (cursor.isNull(ndxNearbyCity)?
                                            cursor.getString(ndxNearbyCity):
                                            cursor.getString(ndxNearbyCity).replaceAll(",","%2C")) + "," +
                                    String.format(Locale.US, "%.1f", cursor.getFloat(ndxGeoAltitude))
                    );
                } else {
                    map.put(Integer.toString(cursor.getInt(ndxID)),
                            cursor.getString(ndxLocationName).replaceAll(",","%2C") + "," +
                                    strCoordinates + "," +
                                    (cursor.isNull(ndxNearbyCity)?
                                            cursor.getString(ndxNearbyCity):
                                            cursor.getString(ndxNearbyCity).replaceAll(",","%2C")) + "," +
                                    String.format(Locale.US, "%.1f", cursor.getFloat(ndxGeoAltitude))
                    );
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }
    public HashMap<String,String> getObservatory() {
        if(this.idObservatory==-1) {
            return getObservatory(null);
        } else {
            mapObservatory.put("id",Integer.toString(this.idObservatory));
            return mapObservatory;
        }
    }
    public HashMap<String,String> getObservatory(Float fltLatitude, Float fltLongitude, Float fltAltitude) {
        if(fltLatitude==null || fltLongitude==null || fltAltitude==null) {
            return getObservatory(null);
        } else {
            Cursor cursor;
            HashMap<String,String> map = new HashMap<>();
            String strQuery, strFieldNames = "id,location_name,nearby_city,geo_latitude," +
                    "geo_longitude,geo_altitude";
            strQuery = "SELECT " + strFieldNames + " FROM astroLocation" +
                    " WHERE geo_latitude=? AND geo_longitude=? AND geo_altitude=?" +
                    " LIMIT 1";
            cursor = astroDB.rawQuery(strQuery,
                    new String[]{
                            String.format(Locale.US,"%.6f",fltLatitude),
                            String.format(Locale.US,"%.6f",fltLongitude),
                            String.format(Locale.US,"%.6f",fltAltitude)
                        } );
            cursor.moveToFirst();
            if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) {
                map.put("id","-1");
            } else {
                for (String strFieldName :
                        strFieldNames.split(",")) {
                    Integer ndxColumn = cursor.getColumnIndex(strFieldName);
                    if (ndxColumn >= 0 && !cursor.isNull(ndxColumn)) {
                        map.put(strFieldName, cursor.getString(ndxColumn));
                    }
                }
            }
            cursor.close();
            return map;
        }
    }
    public HashMap<String,String> getObservatory(String strObservatory) {
        Cursor cursor;
        HashMap<String,String> map = new HashMap<>();
        String strQuery, strFieldNames = "id,location_name,nearby_city,geo_latitude," +
                "geo_longitude,geo_altitude";
        if(strObservatory==null) {
            strQuery = "SELECT " + strFieldNames + " FROM astroLocation LIMIT 1";
            cursor = astroDB.rawQuery(strQuery, null );
        } else {
            strQuery = "SELECT " + strFieldNames + " FROM astroLocation" +
                    " WHERE location_name=?" +
                    " LIMIT 1";
            cursor = astroDB.rawQuery(strQuery, new String[]{strObservatory} );
        }
        cursor.moveToFirst();
        if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) {
            map.put("id","-1");
        } else {
            for (String strFieldName :
                    strFieldNames.split(",")) {
                Integer ndxColumn = cursor.getColumnIndex(strFieldName);
                if (ndxColumn >= 0 && !cursor.isNull(ndxColumn)) {
                    map.put(strFieldName, cursor.getString(ndxColumn));
                }
            }
        }
        cursor.close();
        return map;
    }
    public HashMap<String,String> getNearbyObservatory(Double dblLatitude, Double dblLongitude) {
        Cursor cursor;
        HashMap<String,String> map = new HashMap<>();
        Double dblCos = Math.cos(dblLatitude/180.0*Math.PI);
        Float fltLatitude,fltLongitude;
        String strQuery, strFieldNames = "id,location_name,nearby_city,geo_latitude," +
                "geo_longitude,geo_altitude,geo_dist,is_too_far";
        strQuery = "SELECT id,location_name,nearby_city,geo_latitude," +
                "geo_longitude,geo_altitude," +
                "(geo_latitude-?1)*(geo_latitude-?1) + (geo_longitude-?2)*(geo_longitude-?2)*?3*?3 AS geo_dist," +
                "CAST(?4 AS REAL)>((geo_latitude-?1)*(geo_latitude-?1) + (geo_longitude-?2)*?3*(geo_longitude-?2)*?3) AS is_too_far," +
                "(geo_latitude-?1)*(geo_latitude-?1),(geo_longitude-?2)*?3*(geo_longitude-?2)*?3,?4" +
                " FROM astroLocation" +
                " WHERE CAST(?4 AS REAL)>((geo_latitude-?1)*(geo_latitude-?1) + (geo_longitude-?2)*?3*(geo_longitude-?2)*?3)" +
                " AND location_name<>?5" +
                " ORDER BY geo_dist DESC" +
                " LIMIT 10";
        String[] strsParams = new String[]{
                String.format(Locale.US,"%.6f",dblLatitude),
                String.format(Locale.US,"%.6f",dblLongitude),
                String.format(Locale.US,"%.6f",dblCos),
                String.format(Locale.US,"%.6f",GPS_MINIMUM_DISTANCE*GPS_MINIMUM_DISTANCE),  // SQLite does not have square root function
                mContext.getString(R.string.pref_default_location_name)
        };
        cursor = astroDB.rawQuery(strQuery, strsParams);
        cursor.moveToLast();
        Integer ndxSqDistance = cursor.getColumnIndex("geo_dist");
        if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) {
            map.put("id","-1");
        } else if((ndxSqDistance>=0 ? Math.sqrt(cursor.getDouble(ndxSqDistance)) : 0.0)>GPS_MAXIMUM_SNAP_DISTANCE) {
            map.put("id","-1");
        } else {
            cursor.moveToFirst();
            do {
            fltLatitude = Float.valueOf(Double.toString(dblLatitude));      // initialize but it will be replaced
            fltLongitude = Float.valueOf(Double.toString(dblLongitude));    // initialize but it will be replaced
            for (String strFieldName :
                    strFieldNames.split(",")) {
                Integer ndxColumn = cursor.getColumnIndex(strFieldName);
                if (ndxColumn >= 0 && !cursor.isNull(ndxColumn)) {
                    map.put(strFieldName, cursor.getString(ndxColumn));
                    if(strFieldName.equals("geo_latitude")) fltLatitude = cursor.getFloat(ndxColumn);
                    if(strFieldName.equals("geo_longitude")) fltLongitude = cursor.getFloat(ndxColumn);
                }
            }
            String strCoordinates = String.format(
                    Locale.US,
                    "%1$.6f%3$s %2$.6f%4$s",
                    Math.abs(fltLatitude),
                    Math.abs(fltLongitude),
                    fltLatitude>0?"N":"S",
                    fltLongitude>0?"E":"W"
            );
            map.put("coordinates",strCoordinates);  } while(cursor.moveToNext()); //
        }
        cursor.close();
        return map;
    }
    public void setObservatory(String strObservatory,Float fltLatitude, Float fltLongitude, Float fltAltitude) {
        setObservatory(strObservatory,fltLatitude,fltLongitude,fltAltitude,null);
    }
    public void setObservatory(String strObservatory,Float fltLatitude, Float fltLongitude, Float fltAltitude, String strNearbyCity) {
        String strQuery, strQryExec, strFieldNames, strFieldReplace, strFieldValues;
        String[] strsFieldValues;
        // Add a new location if BOTH the observatory name and location have changed.
        if(strNearbyCity==null) {
            strsFieldValues = new String[]{
                    strObservatory,
                    String.format(Locale.US,"%.6f",fltLatitude),
                    String.format(Locale.US,"%.6f",fltLongitude),
                    String.format(Locale.US,"%.6f",fltAltitude)
            };
            strFieldNames = "location_name,geo_latitude,geo_longitude,geo_altitude";
            strFieldReplace = "location_name=?,geo_latitude=?,geo_longitude=?,geo_altitude=?";
            strFieldValues = "?,?,?,?";
        } else {
            strsFieldValues = new String[]{
                    strObservatory,
                    strNearbyCity,
                    String.format(Locale.US,"%.6f",fltLatitude),
                    String.format(Locale.US,"%.6f",fltLongitude),
                    String.format(Locale.US,"%.6f",fltAltitude)
            };
            strFieldNames = "location_name,nearby_city,geo_latitude,geo_longitude,geo_altitude";
            strFieldReplace = "location_name=?,nearby_city=?,geo_latitude=?,geo_longitude=?,geo_altitude=?";
            strFieldValues = "?,?,?,?,?";
        }
        strQuery = "SELECT id FROM astroLocation" +
                " WHERE location_name=?";
        Cursor cursor = astroDB.rawQuery(strQuery,new String[]{strObservatory});
        cursor.moveToFirst();
        if(cursor.getCount()==0) {
            cursor.close();
            strQuery = "SELECT id FROM astroLocation" +
                    " WHERE geo_latitude=? AND geo_longitude=?";
            cursor = astroDB.rawQuery(strQuery,new String[]{
                    String.format(Locale.US,"%.6f",fltLatitude),
                    String.format(Locale.US,"%.6f",fltLongitude)
            });
            cursor.moveToFirst();
            if(cursor.getCount()==0) {
                // Add a new location if BOTH the observatory name and location have changed.
                strQryExec = "INSERT INTO astroLocation " +
                        "(" + strFieldNames + ") " +
                        "VALUES (" + strFieldValues + ")";
            } else {
                // Only the name has been adjusted.
                Integer ndxColumn = cursor.getColumnIndex("id");
                String strRowID = cursor.getString(ndxColumn);
                strQryExec = "UPDATE astroLocation SET " + strFieldReplace +
                        " WHERE id='" + strRowID + "'";
            }
        } else {
            // Only the location has been adjusted.
            Integer ndxColumn = cursor.getColumnIndex("id");
            String strRowID = cursor.getString(ndxColumn);
            strQryExec = "UPDATE astroLocation SET " + strFieldReplace +
                    " WHERE id='"+ strRowID +"'" ;
        }
        cursor.close();
        astroDB.execSQL(strQryExec,strsFieldValues);
        cursor = astroDB.rawQuery("SELECT last_insert_rowid()",null);
        cursor.moveToFirst();
        if(cursor.getCount()==1) {
            this.idObservatory = cursor.getInt(0);
            this.mapObservatory.put("id",Integer.toString(this.idObservatory));
            String[] strsFieldNames = strFieldNames.split(",");
            for (int intI = 0; intI < strsFieldValues.length; intI++) {
                this.mapObservatory.put(strsFieldNames[intI],strsFieldValues[intI]);
            }
        }
        cursor.close();
    }
    public void deleteObservatory() {
        Cursor cursor;
        String strQuery, strQryExec, strFieldNames = "id,location_name,nearby_city," +
                "geo_latitude,geo_longitude,geo_altitude";
        String[] strsFieldValues = new String[]{
                Integer.toString(this.idObservatory)
        };
        // if the observatory exists, delete it with extreme prejudice.
        // ToDo: delete observatory and widows and orphans
        try {
            strQuery = "SELECT * FROM astroLocation WHERE id=?";
            strQryExec = "DELETE FROM astroLocation WHERE id=?";
            cursor = astroDB.rawQuery(strQuery,strsFieldValues);
            cursor.moveToFirst();
            if(cursor.getCount()==1) {
                astroDB.execSQL(strQryExec,strsFieldValues);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "deleteObservatory: SQL-"+ e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "deleteObservatory: "+ e.getLocalizedMessage() );
        }
    }

    // Build a weather record object
    public class AstroWeatherRecord {
        private Integer intID;
        private String strIdObservatory, strLatitude, strLongitude, strAltitude;
        private Float fltLatitude, fltLongitude, fltAltitude;

        // retrieve this record object
        public void setRecord() {
            String strRowID = Integer.toString(this.intID),
                    strQuery = "SELECT * FROM astroWeather WHERE id=?";
            Cursor cursor = astroDB.rawQuery(strQuery, new String[]{strRowID});
            if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) return;
            Integer ndxColumn = cursor.getColumnIndex("id");
            if( !cursor.isNull(ndxColumn) && cursor.getInt(ndxColumn)>=0 ) {

            }
            cursor.close();
        }
    }

    public void saveWeatherRecord(Long dteStart, Long dteFinish,
                                  Integer intCloudCover, Integer intTransparency,
                                  Integer intSeeing, Integer intWind,
                                  Integer intHumidity, Integer intTemperature) {

        /*
        *    location_id       - id given to the location by system
        *    weather_start     - date and time when the weather report starts
        *    weather_stop      - date and time when the weather report stops, could be an hour or not
        *    twilight          - level of twilight: dusk/dawn, civil, nautical, astronomical, dark
        *    cloud_cover       - cloud cover rating
        *    transparency      - transparency rating
        *    seeing            - seeing rating
        *    wind              - wind rating
        *    humidity          - humidity rating
        *    temperature       - temperature rating
        * */

        String strFieldNames = "location_id,weather_start,weather_stop,cloud_cover,transparency,seeing,wind,humidity,temperature",
                strFieldValues = "?,?,?,?,?,?,?,?,?", strQryExec;
        String[] strsFieldValues = new String[]{
                Integer.toString(this.idObservatory),Long.toString(dteStart),Long.toString(dteFinish),
                Integer.toString(intCloudCover),Integer.toString(intTransparency),Integer.toString(intSeeing),
                Integer.toString(intWind),Integer.toString(intHumidity),Integer.toString(intTemperature)
        };
        try {
            strQryExec = "SELECT * FROM astroWeather WHERE location_id=? AND weather_start=?";
            if(astroDB.rawQuery(strQryExec,new String[]{Integer.toString(this.idObservatory),Long.toString(dteStart)}).getCount()>0) {
                strQryExec = "DELETE FROM astroWeather WHERE location_id=? AND weather_start=?";
                astroDB.execSQL(strQryExec,new String[]{Integer.toString(this.idObservatory),Long.toString(dteStart)});
            }
            strQryExec = "INSERT INTO astroWeather " +
                    "(" + strFieldNames + ") " +
                    "VALUES (" + strFieldValues + ")";
            astroDB.execSQL(strQryExec, strsFieldValues);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveWeatherRecord: SQL-"+ e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveWeatherRecord: "+ e.getLocalizedMessage() );
        }

        return;
    }
    public Map<Long,String> getWeather(){
        // get weather for the whole night
        String strQuery;
        Map<Long,String> mapWeather = new HashMap<>();
        String[] strsKeyValues = new String[]{
                Integer.toString(this.idObservatory),
                Long.toString(this.getMidnightFromWeather()-ONE_HALF_DAY),
                Long.toString(this.getMidnightFromWeather()+ONE_HALF_DAY)
        };
        strQuery = "SELECT weather_start,cloud_cover,transparency,seeing,wind,humidity,temperature" +
                " FROM astroWeather" +
                " WHERE location_id=? AND weather_stop>? AND weather_start<?";
        try {
            Cursor cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                Integer ndxColumn, intValue;
                Long dteStart;
                String strWeather;
                do {
                    ndxColumn = cursor.getColumnIndex("weather_start");
                    dteStart = cursor.getLong(ndxColumn);
                    strWeather = getRowWeather(cursor);
                    mapWeather.put(dteStart,strWeather);
                } while(cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getWeather: SQL-" + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getWeather: " +e.getLocalizedMessage() );
        }

        return mapWeather;
    }
    private String getRowWeather(Cursor cursor) {
        String strWeather;
        Integer ndxColumn = cursor.getColumnIndex("cloud_cover");
        Integer intValue = cursor.getInt(ndxColumn);
        switch (intValue) {
            case 0:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_00);
                break;
            case 1:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_01);
                break;
            case 2:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_02);
                break;
            case 3:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_03);
                break;
            case 4:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_04);
                break;
            case 5:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_05);
                break;
            case 6:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_06);
                break;
            case 7:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_07);
                strWeather += getRowTransparency(cursor);
                break;
            case 8:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_08);
                strWeather += getRowTransparency(cursor);
                break;
            case 9:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_09);
                strWeather += getRowTransparency(cursor);
                break;
            case 10:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_10);
                strWeather += getRowTransparency(cursor);
                break;
            default:
                strWeather = mContext.getString(R.string.dialog_pop_up_weather_cloud_cover_null);
                break;
        }
        return strWeather;
    }
    private String getRowTransparency(Cursor cursor) {
        String strWeather;
        Integer ndxColumn = cursor.getColumnIndex("transparency");
        Integer intValue = cursor.getInt(ndxColumn);
        switch (intValue) {
            case 0:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_0);
                break;
            case 1:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_1);
                break;
            case 2:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_2);
                break;
            case 3:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_3);
                break;
            case 4:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_4);
                strWeather += getRowSeeing(cursor);
                break;
            case 5:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_5);
                strWeather += getRowSeeing(cursor);
                break;
            default:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_transparency_null);
                break;
        }
        return strWeather;
    }
    private String getRowSeeing(Cursor cursor) {
        String strWeather;
        Integer ndxColumn = cursor.getColumnIndex("seeing");
        Integer intValue = cursor.getInt(ndxColumn);
        switch (intValue) {
            case 0:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_0);
                break;
            case 1:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_1);
                break;
            case 2:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_2);
                break;
            case 3:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_3);
                break;
            case 4:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_4);
                break;
            case 5:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_5);
                break;
            default:
                strWeather = "\n    "+ mContext.getString(R.string.dialog_pop_up_weather_seeing_null);
                break;
        }
        return strWeather;
    }

    public void saveWeatherTwilight(String strTwilight,Long dteStart, Long dteFinish, String strSummary) {
        String strFieldNames,strFieldValues,strFieldReplace = "twilight=?";
        String[] strsFieldValues = new String[]{strTwilight};
        String strQuery = "SELECT * FROM astroWeather" +
                " WHERE" +
                " location_id=" + Integer.toString(this.idObservatory) +
                " AND weather_start<=" + Long.toString(dteFinish) +
                " AND weather_stop>=" + Long.toString(dteStart);
        String strQryAppend = "UPDATE astroWeather SET " + strFieldReplace +
                " WHERE" +
                " location_id=" + Integer.toString(this.idObservatory) +
                " AND weather_start<=" + Long.toString(dteFinish) +
                " AND weather_stop>=" + Long.toString(dteStart);
        Cursor cursor = astroDB.rawQuery(strQuery,null);
        if(cursor.getCount()>0) {
            astroDB.execSQL(strQryAppend, strsFieldValues);
        }
        cursor.close();
        strFieldNames = "location_id,weather_start,weather_stop,twilight";
        strFieldValues = "?,?,?,?";
        strsFieldValues = new String[]{
                Integer.toString(this.idObservatory),
                Long.toString(dteStart),
                Long.toString(dteFinish),
                strTwilight
        };
        try {
            String strQryExec = "SELECT * FROM astroWeatherSummary WHERE location_id=? AND weather_start=?";
            if(astroDB.rawQuery(strQryExec,new String[]{Integer.toString(this.idObservatory),Long.toString(dteStart)}).getCount()>0) {
                strQryExec = "DELETE FROM astroWeatherSummary WHERE location_id=? AND weather_start=?";
                astroDB.execSQL(strQryExec,new String[]{Integer.toString(this.idObservatory),Long.toString(dteStart)});
            }
            strQryAppend = "INSERT INTO astroWeatherSummary " +
                    "(" + strFieldNames + ") " +
                    "VALUES (" + strFieldValues + ")";
            astroDB.execSQL(strQryAppend,strsFieldValues);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveWeatherTwilight: SQL-"+ e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveWeatherTwilight: " +e.getLocalizedMessage() );
        }
        return;
    }

    public Integer getEveningWeather() {
        // get the latest evening cloud cover for this observatory
        String strQuery;
        Integer intWeather = -1;
        Long lngDateNow = new Date().getTime(),
                lngDateLater = lngDateNow + ONE_HOUR*4;
        String[] strsKeyValues = new String[]{
                Integer.toString(this.idObservatory),
                Long.toString(this.getMidnightFromWeather()-ONE_HALF_DAY),
                Long.toString(this.getMidnightFromWeather())
        };
        if(Long.valueOf(strsKeyValues[1])*1000L<lngDateNow) {
            strsKeyValues[1] = Long.toString(lngDateNow / 1000L);
        }
        if(Long.valueOf(strsKeyValues[2])*1000L<lngDateLater) {
            strsKeyValues[2] = Long.toString(lngDateLater / 1000L);
        }
        /** for (String strNightTime : new String[]{"evening", "overnight", "darkestnight"}) {
            strQuery = "SELECT cloud_cover FROM astroWeather" +
                    " WHERE location_id=? AND twilight='"+ strNightTime +"'" +
                    " ORDER BY weather_start DESC LIMIT 4";
        } */
        strQuery = "SELECT * FROM astroWeather" +
                " WHERE location_id=? AND weather_stop>? AND weather_start<?";
        strQuery = "SELECT AVG(cloud_cover) AS cloud_cover, AVG(transparency) AS transparency" +
                " FROM ("+ strQuery +")" +
                " GROUP BY location_id";
        try {
            Cursor cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            if(cursor.getCount()==1) {
                Integer ndxCloud = cursor.getColumnIndex("cloud_cover");
                Integer ndxTransparency = cursor.getColumnIndex("transparency");
                Integer intCloud = cursor.getInt(ndxCloud);
                Integer intTransparency = cursor.getInt(ndxTransparency);
                intWeather = (intCloud<8?0:intTransparency)*100 + intCloud;
                cursor.close();
            } else {
                cursor.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getEveningWeather: SQL-" + e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getEveningWeather: " +e.getLocalizedMessage() );
        }
        return intWeather;
    }
    private Long getMidnightFromWeather() {
        Cursor cursor;
        String strQuery;
        String[] strsKeyValues;
        Date dteNow = new Date();
        Long dteMidnight = dteNow.getTime()/1000L;
        try {
            strsKeyValues = new String[]{
                    Integer.toString(this.idObservatory),
                    "overnight"
            };
            strQuery = "SELECT weather_start,weather_stop FROM astroWeatherSummary " +
                    "WHERE location_id=? AND twilight=? " +
                    "ORDER BY weather_start DESC LIMIT 1";
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            if (cursor.getCount() == 1) {
                dteMidnight = (cursor.getLong(cursor.getColumnIndex("weather_start")) +
                        cursor.getLong(cursor.getColumnIndex("weather_stop"))) / 2L;
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getMidnightFromWeather: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getMidnightFromWeather: " +e.getLocalizedMessage() );
        }

        return dteMidnight;
    }

    public String[] getTonightsSatellites() {
        // return the satellites that could be seen up to midnight, tonight.
        ArrayList<AstroEventsSatelliteRecord> listSatellites = new ArrayList<>();
        String[] strsSatellites = new String[0];
        Cursor cursor;
        String strQuery, strQryExec;
        String[] strsKeyValues;
        Long lngDateNow = new Date().getTime(),
                lngDateLater = lngDateNow + ONE_HOUR*4;
        try {
            // Get the code for midnight
            Long dteMidnight = getMidnightFromWeather();
            strQuery = "SELECT * FROM astroEventsSatellites " +
                    "WHERE location_id=? AND pass_rise>? AND  pass_rise<?";
            strsKeyValues = new String[]{
                    Integer.toString(this.idObservatory),
                    Long.toString(dteMidnight - ONE_HALF_DAY),
                    Long.toString(dteMidnight)
            };
            if(Long.valueOf(strsKeyValues[1])*1000L<lngDateNow) {
                strsKeyValues[1] = Long.toString(lngDateNow / 1000L);
            }
            if(Long.valueOf(strsKeyValues[2])*1000L<lngDateLater) {
                strsKeyValues[2] = Long.toString(lngDateLater / 1000L);
            }
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                do {
                    listSatellites.add(new AstroEventsSatelliteRecord(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
            strsSatellites = new String[listSatellites.size()];
            for (int intI = 0; intI < listSatellites.size(); intI++) {
                strsSatellites[intI] = listSatellites.get(intI).getStrName() +","+
                        listSatellites.get(intI).getStrType() +","+
                        listSatellites.get(intI).getFltMagnitude() +","+
                        listSatellites.get(intI).getDteRise() +","+
                        listSatellites.get(intI).getDtePeak() +","+
                        listSatellites.get(intI).getDteSet();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getTonightsSatellites: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getTonightsSatellites: " +e.getLocalizedMessage() );
        }

        return strsSatellites;
    }
    public Integer getSatelliteCountTonightsISS() {
        Integer intCount = 0;
        Cursor cursor;
        String strQuery, strQryExec;
        String[] strsKeyValues;
        Long lngDateNow = new Date().getTime(),
                lngDateLater = lngDateNow + ONE_HOUR*4;
        try {
            // Get the code for midnight
            Long dteMidnight = getMidnightFromWeather();
            strQuery = "SELECT * FROM astroEventsSatellites " +
                    "WHERE location_id=? AND vehicle_type=? AND pass_rise>? AND  pass_rise<?";
            strsKeyValues = new String[]{
                    Integer.toString(this.idObservatory),
                    "ISS",
                    Long.toString(dteMidnight - ONE_HALF_DAY),
                    Long.toString(dteMidnight)
            };
            if(Long.valueOf(strsKeyValues[2])*1000L<lngDateNow) {
                strsKeyValues[2] = Long.toString(lngDateNow / 1000L);
            }
            if(Long.valueOf(strsKeyValues[3])*1000L<lngDateLater) {
                strsKeyValues[3] = Long.toString(lngDateLater / 1000L);
            }
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            intCount = cursor.getCount();
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getSatelliteCountTonightsISS: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getSatelliteCountTonightsISS: " +e.getLocalizedMessage() );
        }

        return intCount;
    }
    public Integer getSatelliteCountTonightsIridium() {
        Integer intCount = 0;
        Cursor cursor;
        String strQuery;
        String[] strsKeyValues;
        Long lngDateNow = new Date().getTime(),
                lngDateLater = lngDateNow + ONE_HOUR*4;
        try {
            // Get the code for midnight
            Long dteMidnight = getMidnightFromWeather();
            strQuery = "SELECT * FROM astroEventsSatellites " +
                    "WHERE location_id=? AND vehicle_type=? AND pass_rise>? AND  pass_rise<?";
            strsKeyValues = new String[]{
                    Integer.toString(this.idObservatory),
                    "Iridium",
                    Long.toString(dteMidnight - ONE_HALF_DAY),
                    Long.toString(dteMidnight)
            };
            if(Long.valueOf(strsKeyValues[2])*1000L<lngDateNow) {
                strsKeyValues[2] = Long.toString(lngDateNow / 1000L);
            }
            if(Long.valueOf(strsKeyValues[3])*1000L<lngDateLater) {
                strsKeyValues[3] = Long.toString(lngDateLater / 1000L);
            }
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            intCount = cursor.getCount();
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getSatelliteCountTonightsIridium: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getSatelliteCountTonightsIridium: " +e.getLocalizedMessage() );
        }

        return intCount;
    }
    public class AstroEventsSatelliteRecord {
        /**
         * Table astroEventsSatellites
         *    location_id       - id given to the location by system
         *    vehicle_type      - type of pass based on vehicle used
         *    vehicle_name      - name of vehicle used
         *    pass_magnitude    - observable magnitude at brightest point
         *    pass_rise         - date and time when the vehicle rises or emerges from shadow
         *    pass_peak         - date and time when the vehicle reaches its brightest
         *    pass_set          - date and time when the vehicle sets or passes into shadow
         *    url_link          - link to chart for the pass
         */
        private Integer intID, intLocation;
        private Float fltMagnitude;
        private Long dteRise, dtePeak, dteSet;
        private String strType, strName, strUrl;

        public AstroEventsSatelliteRecord(Cursor cursor) {
            if(cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) return;

            for (String strColumnName : cursor.getColumnNames()) {
                Integer ndxColumn = cursor.getColumnIndex(strColumnName);
                if(!cursor.isNull(ndxColumn)) {
                    switch (strColumnName) {
                        case "id":
                            this.intID = cursor.getInt(ndxColumn);
                            break;
                        case "location_id":
                            this.intLocation = cursor.getInt(ndxColumn);
                            break;
                        case "vehicle_type":
                            this.strType = cursor.getString(ndxColumn);
                            break;
                        case "vehicle_name":
                            this.strName = cursor.getString(ndxColumn);
                            break;
                        case "pass_magnitude":
                            this.fltMagnitude = cursor.getFloat(ndxColumn);
                            break;
                        case "pass_rise":
                            this.dteRise = cursor.getLong(ndxColumn);
                            break;
                        case "pass_peak":
                            this.dtePeak = cursor.getLong(ndxColumn);
                            break;
                        case "pass_set":
                            this.dteSet = cursor.getLong(ndxColumn);
                            break;
                        case "url_link":
                            this.strUrl = cursor.getString(ndxColumn);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        public Integer getIntID() {
            return intID;
        }

        public Integer getIntLocation() {
            return intLocation;
        }

        public Float getFltMagnitude() {
            return fltMagnitude;
        }

        public Long getDteRise() {
            return dteRise;
        }

        public Long getDtePeak() {
            return dtePeak;
        }

        public Long getDteSet() {
            return dteSet;
        }

        public String getStrType() {
            return strType;
        }

        public String getStrName() {
            return strName;
        }

        public String getStrUrl() {
            return strUrl;
        }
    }
    public void saveSatellite(String strSatelliteName,String strType,Float fltMagnitude,Long dteStart,Long dtePeak,Long dteEnd){
        String strQuery, strQryExec, strFieldNames, strFieldValues;
        String[] strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strSatelliteName,
                strType,
                String.format(Locale.US,"%.6f",fltMagnitude),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dtePeak),
                String.format(Locale.US,"%d",dteEnd)
        }, strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strSatelliteName,
                String.format(Locale.US,"%d",dtePeak-1800),
                String.format(Locale.US,"%d",dtePeak+1800)
        };
        try {
            strQuery = "SELECT id FROM astroEventsSatellites WHERE location_id=? AND vehicle_name=? AND pass_rise>? AND pass_set<?";
            strQryExec = "DELETE FROM astroEventsSatellites WHERE id in("+ strQuery +")";
            if(astroDB.rawQuery(strQuery,strsKeyValues).getCount()>0) {
                astroDB.execSQL(strQryExec,strsKeyValues);
            }
            strFieldNames = "location_id,vehicle_name,vehicle_type,pass_magnitude,pass_rise,pass_peak,pass_set";
            strFieldValues = "?,?,?,?,?,?,?";
            strQryExec = "INSERT INTO astroEventsSatellites " +
                    "(" + strFieldNames + ") " +
                    "VALUES (" + strFieldValues + ")";
            astroDB.execSQL(strQryExec,strsFieldValues);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveSatellite: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveSatellite: "+e.getLocalizedMessage());
        }
    }

    public Integer getConjunctionsCount() {
        Integer intCount=0;
        Cursor cursor;
        String strQuery;
        String[] strsKeyValues;
        try {
            // Get the code for midnight
            Long dteMidnight = getMidnightFromWeather();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            Float fltMaxDistance = Float.valueOf(sharedPreferences.getString(CONJUNCTION_DISTANCE_KEY,"2.5°").replace("°",""));
            if(sharedPreferences
                    .getString(CONJUNCTION_PLANET_KEY,mContext.getString(R.string.pref_title_key_3_planets_All))
                    .equals(mContext.getString(R.string.pref_title_key_3_planets_All))
                    ) {
                strQuery = "SELECT * FROM astroEventsConjunctions" +
                        " WHERE location_id=? AND angular_distance<?" +
                        " AND conjunction_start>? AND conjunction_start<?";
                strsKeyValues = new String[]{
                        Integer.toString(this.idObservatory),
                        String.format(Locale.US,"%.6f",fltMaxDistance),
                        String.format(Locale.US, "%d", dteMidnight - ONE_HALF_DAY),
                        String.format(Locale.US, "%d", dteMidnight + ONE_HALF_DAY)
                };
            } else {
                strQuery = "SELECT * FROM astroEventsConjunctions" +
                        " WHERE location_id=? AND planet_name=? AND angular_distance<?" +
                        " AND conjunction_start>? AND conjunction_start<?";
                strsKeyValues = new String[]{
                        Integer.toString(this.idObservatory),
                        sharedPreferences.getString(CONJUNCTION_PLANET_KEY,""),
                        String.format(Locale.US,"%.6f",fltMaxDistance),
                        String.format(Locale.US, "%d", dteMidnight - ONE_HALF_DAY),
                        String.format(Locale.US, "%d", dteMidnight + ONE_HALF_DAY)
                };
            }
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            intCount = cursor.getCount();
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsCount: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsCount: " +e.getLocalizedMessage() );
        }
        return intCount;
    }
    public Integer getConjunctionsCount(Float fltMaxDistance) {
        Integer intCount=0;
        Cursor cursor;
        String strQuery;
        String[] strsKeyValues;
        try {
            // Get the code for midnight
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            Long dteMidnight = getMidnightFromWeather();
            if(sharedPreferences
                    .getString(CONJUNCTION_PLANET_KEY,mContext.getString(R.string.pref_title_key_3_planets_All))
                    .equals(mContext.getString(R.string.pref_title_key_3_planets_All))
                    ) {
                strQuery = "SELECT * FROM astroEventsConjunctions " +
                        "WHERE location_id=? AND angular_distance<? AND conjunction_start>? AND conjunction_start<?";
                strsKeyValues = new String[]{
                        String.format(Locale.US, "%d", this.idObservatory),
                        String.format(Locale.US, "%.6f", fltMaxDistance),
                        String.format(Locale.US, "%d", dteMidnight - ONE_HALF_DAY),
                        String.format(Locale.US, "%d", dteMidnight + ONE_HALF_DAY)
                };
            } else {
                strQuery = "SELECT * FROM astroEventsConjunctions" +
                        " WHERE location_id=? AND planet_name=? AND angular_distance<?" +
                        " AND conjunction_start>? AND conjunction_start<?";
                strsKeyValues = new String[]{
                        Integer.toString(this.idObservatory),
                        sharedPreferences.getString(CONJUNCTION_PLANET_KEY,""),
                        String.format(Locale.US,"%.6f",fltMaxDistance),
                        String.format(Locale.US, "%d", dteMidnight - ONE_HALF_DAY),
                        String.format(Locale.US, "%d", dteMidnight + ONE_HALF_DAY)
                };
            }
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            intCount = cursor.getCount();
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsCount: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsCount: " +e.getLocalizedMessage() );
        }
        return intCount;
    }
    public Integer getConjunctionsCount(String strPlanet) {
        Integer intCount=0;
        Cursor cursor;
        String strQuery;
        String[] strsKeyValues;
        try {
            // Get the code for midnight
            Long dteMidnight = getMidnightFromWeather();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            Float fltMaxDistance = Float.valueOf(sharedPreferences.getString(CONJUNCTION_DISTANCE_KEY,"2.5°").replace("°",""));
            switch (strPlanet) {
                case CONJUNCTION_PLANET_MOON_KEY:
                case CONJUNCTION_PLANET_MERCURY_KEY:
                case CONJUNCTION_PLANET_VENUS_KEY:
                case CONJUNCTION_PLANET_MARS_KEY:
                case CONJUNCTION_PLANET_JUPITER_KEY:
                case CONJUNCTION_PLANET_SATURN_KEY:
                case CONJUNCTION_PLANET_URANUS_KEY:
                case CONJUNCTION_PLANET_NEPTUNE_KEY:
                    strQuery = "SELECT * FROM astroEventsConjunctions " +
                            "WHERE location_id=?1 AND (planet_name=?2 OR conjunction_name=?2) AND" +
                            " angular_distance<?3 AND conjunction_start>?4 AND conjunction_start<?5";
                    strsKeyValues = new String[]{
                            Integer.toString(this.idObservatory),
                            strPlanet,
                            String.format(Locale.US,"%.6f",fltMaxDistance),
                            Long.toString(dteMidnight - ONE_HALF_DAY),
                            Long.toString(dteMidnight + ONE_HALF_DAY)
                    };
                    break;
                default:
                    strQuery = "SELECT * FROM astroEventsConjunctions " +
                            "WHERE location_id=? AND angular_distance<? AND" +
                            " conjunction_start>? AND conjunction_start<?";
                    strsKeyValues = new String[]{
                            Integer.toString(this.idObservatory),
                            String.format(Locale.US,"%.6f",fltMaxDistance),
                            Long.toString(dteMidnight - ONE_HALF_DAY),
                            Long.toString(dteMidnight + ONE_HALF_DAY)
                    };
                    break;
            }
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            intCount = cursor.getCount();
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsCount: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsCount: " +e.getLocalizedMessage() );
        }
        return intCount;
    }
    public String[] getConjunctionsStrings(){
        String[] strsConjunctions = new String[0];
        ArrayList<String> listConjunctions = new ArrayList<>();
        Cursor cursor;
        String strQuery,
                strFavouritePlanet=".Moon.Mercury.Venus.Mars.Jupiter.Saturn.Uranus.Neptune.";
        String[] strsKeyValues;
        Integer ndxObject1,ndxObject2,ndxRightAscension1,ndxRightAscension2,ndxDeclination1,ndxDeclination2,ndxDistance;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        try {
            if(sharedPreferences
                    .getString(CONJUNCTION_PLANET_KEY,mContext.getString(R.string.pref_title_key_3_planets_All))
                    .equals(mContext.getString(R.string.pref_title_key_3_planets_All))
                    ) {
                strFavouritePlanet = ".";
                for (String strPlanet :
                        mContext.getResources().getStringArray(R.array.pref_title_key_3_planets_values)) {
                    if (!strPlanet.equals(mContext.getString(R.string.pref_title_key_3_planets_All))) {
                        strFavouritePlanet += strPlanet +".";
                    }
                }
            } else {
                strFavouritePlanet = "."+ sharedPreferences.getString(CONJUNCTION_PLANET_KEY,"") +".";
            }
            if(sharedPreferences.getBoolean(MOONTIMES_KEY,false)) {
                strFavouritePlanet += mContext.getString(R.string.pref_title_key_2) + ".";
            }
            Float fltDistance,fltMaxDistance = Float.valueOf(sharedPreferences.getString(CONJUNCTION_DISTANCE_KEY,"2.5°").replace("°",""));
            Long dteMidnight = getMidnightFromWeather();
            strQuery = "SELECT * FROM astroEventsConjunctions " +
                    "WHERE location_id=? AND conjunction_start>? AND conjunction_start<?";
            strsKeyValues = new String[]{
                    Integer.toString(this.idObservatory),
                    Long.toString(dteMidnight - ONE_HALF_DAY),
                    Long.toString(dteMidnight + ONE_HALF_DAY)
            };
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            ndxObject1=cursor.getColumnIndex("planet_name");
            ndxObject2=cursor.getColumnIndex("conjunction_name");
            ndxRightAscension1=cursor.getColumnIndex("planet_ra");
            ndxRightAscension2=cursor.getColumnIndex("conjunction_ra");
            ndxDeclination1=cursor.getColumnIndex("planet_decl");
            ndxDeclination2=cursor.getColumnIndex("conjunction_decl");
            ndxDistance=cursor.getColumnIndex("angular_distance");
            if(cursor.getCount()>0) {
                String strObject1,
                        strObject2,
                        strRightAscension,  // return the midpoint right ascension
                        strDeclination,     // return the midpoint declination
                        strJoin; // return conjuction as comma separated values: conjunction name, obj1, obj2, ra, decl
                Float fltRightAscension, fltRightAscension1, fltRightAscension2,
                        fltDeclination, fltDeclination1, fltDeclination2;
                do {
                    strObject1=cursor.getString(ndxObject1);
                    strObject2=cursor.getString(ndxObject2);
                    if(cursor.isNull(ndxRightAscension1)||cursor.isNull(ndxRightAscension2)) {
                        strRightAscension = "";
                    } else {
                        fltRightAscension1=cursor.getFloat(ndxRightAscension1);
                        fltRightAscension2=cursor.getFloat(ndxRightAscension2);
                        // check if the line between points crosses the midnight meridian
                        if(Math.abs(fltRightAscension1-fltRightAscension2)>12.0) {
                            fltRightAscension = (24.0f + fltRightAscension1 + fltRightAscension2)/2.0f;
                        } else {
                            fltRightAscension = (fltRightAscension1 + fltRightAscension2)/2.0f;
                        }
                        strRightAscension=String.format(Locale.US,"%.6f",fltRightAscension);
                    }
                    if(cursor.isNull(ndxDeclination1)||cursor.isNull(ndxDeclination2)) {
                        strDeclination = "";
                    } else {
                        fltDeclination1=cursor.getFloat(ndxDeclination1);
                        fltDeclination2=cursor.getFloat(ndxDeclination2);
                        fltDeclination=(fltDeclination1+fltDeclination2)/2.0f;
                        strDeclination=String.format(Locale.US,"%.6f",fltDeclination);
                    }
                    if(cursor.isNull(ndxDistance)) {
                        fltDistance = 180f;
                    } else {
                        fltDistance=cursor.getFloat(ndxDistance);
                    }
                    switch (strObject1) {
                        case CONJUNCTION_PLANET_MOON_KEY:
                        case CONJUNCTION_PLANET_MERCURY_KEY:
                        case CONJUNCTION_PLANET_VENUS_KEY:
                        case CONJUNCTION_PLANET_MARS_KEY:
                        case CONJUNCTION_PLANET_JUPITER_KEY:
                        case CONJUNCTION_PLANET_SATURN_KEY:
                        case CONJUNCTION_PLANET_URANUS_KEY:
                        case CONJUNCTION_PLANET_NEPTUNE_KEY:
                            strJoin = strObject1+
                                    mContext.getString(R.string.dialog_pop_up_conjunctions_conjunction) +
                                    strObject2 +","+
                                    strObject1 +","+
                                    strObject2 +","+
                                    strRightAscension +","+
                                    strDeclination +","+
                                    String.format(Locale.US,"%.6f",fltDistance);
                            break;
                        default:
                            strJoin = strObject2+
                                    mContext.getString(R.string.dialog_pop_up_conjunctions_conjunction) +
                                    strObject1 +","+
                                    strObject2 +","+
                                    strObject1 +","+
                                    strRightAscension +","+
                                    strDeclination +","+
                                    String.format(Locale.US,"%.6f",fltDistance);
                            break;
                    }
                    if( (strFavouritePlanet.contains(strObject1) ||
                            strFavouritePlanet.contains(strObject2)) &&
                            fltDistance<fltMaxDistance
                            ) {
                        listConjunctions.add(strJoin);
                    }
                } while(cursor.moveToNext());
            }
            cursor.close();
            strsConjunctions = new String[listConjunctions.size()];
            Integer intI = 0;
            for (String strConjunction :
                    listConjunctions) {
                strsConjunctions[intI++] = strConjunction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsStrings: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getConjunctionsStrings: " +e.getLocalizedMessage() );
        }

        return strsConjunctions;
    }
    // Build a event conjunction record object
    public class AstroEventsConjunctionsRecord {
        private Integer intID,idObservatory;
        private String strIdObservatory, strPlanet, strObject,
                strLatitude, strLongitude, strAltitude;
        private Float fltDistance, fltLatitude, fltLongitude, fltAltitude;
        private Long dtePeak, dteStart, dteStop, dteRise, dteSet;

        public String getStrPlanet() {
            return strPlanet;
        }

        public String getStrObject() {
            return strObject;
        }

        public Long getDteStart() {
            return dteStart;
        }

        public Long getDteStop() {
            return dteStop;
        }

        public Long getDteRise() {
            return dteRise;
        }

        public Long getDteSet() {
            return dteSet;
        }

        public AstroEventsConjunctionsRecord(Cursor cursor) {
            if (cursor.isAfterLast() || cursor.isBeforeFirst() || cursor.isClosed()) return;

            for (String strColumnName : cursor.getColumnNames()) {
                Integer ndxColumn = cursor.getColumnIndex(strColumnName);
                if (!cursor.isNull(ndxColumn)) {
                    switch (strColumnName) {
                        case "id":
                            intID = cursor.getInt(ndxColumn);
                            break;
                        case "location_id":
                            idObservatory = cursor.getInt(ndxColumn);
                            break;
                        case "planet_name":
                            strPlanet = cursor.getString(ndxColumn);
                            break;
                        case "conjunction_name":
                            strObject = cursor.getString(ndxColumn);
                            break;
                        case "angular_distance":
                            fltDistance = cursor.getFloat(ndxColumn);
                            break;
                        case "conjunction_start":
                            dteStart = cursor.getLong(ndxColumn);
                            break;
                        case "conjunction_rise":
                            dteRise = cursor.getLong(ndxColumn);
                            break;
                        case "conjunction_set":
                            dteSet = cursor.getLong(ndxColumn);
                            break;
                        case "conjunction_stop":
                            dteStop = cursor.getLong(ndxColumn);
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }
    public String[] getPlanetStrings(){
        String[] strsPlanets = new String[0];
        ArrayList<String> listPlanets = new ArrayList<>();
        Cursor cursor;
        String strQuery,
                strFavouritePlanet=".Mercury.Venus.Mars.Jupiter.Saturn.Uranus.Neptune.";
        String[] strsKeyValues;
        Integer ndxPlanet,ndxRightAscension,ndxDeclination;
        Long dtePeak, dteStart, dteStop, dteRise, dteSet;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        try {
            dteStart = (new Date()).getTime()/1000L;
            dteStop = getMidnightFromWeather();
            strFavouritePlanet = sharedPreferences.getString(CONJUNCTION_PLANET_KEY,mContext.getString(R.string.pref_title_key_3_planets_All));
            if(strFavouritePlanet.equals(mContext.getString(R.string.pref_title_key_3_planets_All))) {
                if(dteStart>dteStop) {
                    strQuery = "SELECT DISTINCT planet_name,planet_ra,planet_decl" +
                            " FROM astroPlanets WHERE location_id=? AND planet_stop>?";
                    strsKeyValues = new String[]{
                            Integer.toString(this.idObservatory),
                            String.format(Locale.US, "%d", dteStart)
                    };
                } else {
                    strQuery = "SELECT DISTINCT planet_name,planet_ra,planet_decl" +
                            " FROM astroPlanets WHERE location_id=? AND planet_stop>? AND planet_start<?";
                    strsKeyValues = new String[]{
                            Integer.toString(this.idObservatory),
                            String.format(Locale.US, "%d", dteStart),
                            String.format(Locale.US, "%d", dteStop)
                    };
                }
            } else {
                if(dteStart>dteStop) {
                    strQuery = "SELECT DISTINCT planet_name,planet_ra,planet_decl" +
                            " FROM astroPlanets WHERE location_id=? AND planet_stop>? AND planet_name=?";
                    strsKeyValues = new String[]{
                            Integer.toString(this.idObservatory),
                            String.format(Locale.US,"%d",dteStart),
                            strFavouritePlanet
                    };
                } else {
                    strQuery = "SELECT DISTINCT planet_name,planet_ra,planet_decl" +
                            " FROM astroPlanets" +
                            " WHERE location_id=? AND planet_stop>? AND planet_start<? AND planet_name=?";
                    strsKeyValues = new String[]{
                            Integer.toString(this.idObservatory),
                            String.format(Locale.US,"%d",dteStart),
                            String.format(Locale.US,"%d",dteStop),
                            strFavouritePlanet
                    };
                }
            }
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxPlanet=cursor.getColumnIndex("planet_name");
            ndxRightAscension=cursor.getColumnIndex("planet_ra");
            ndxDeclination=cursor.getColumnIndex("planet_decl");
            if(cursor.getCount()>0) {
                String strJoin, strPlanet, strRightAscension, strDeclination;
                Float fltRightAscension, fltDeclination;
                do {
                    strPlanet=cursor.getString(ndxPlanet);
                    if(cursor.isNull(ndxRightAscension)) {
                        strRightAscension = "";
                    } else {
                        fltRightAscension=cursor.getFloat(ndxRightAscension);
                        strRightAscension=String.format(Locale.US,"%.6f",fltRightAscension);
                    }
                    if(cursor.isNull(ndxDeclination)) {
                        strDeclination = "";
                    } else {
                        fltDeclination=cursor.getFloat(ndxDeclination);
                        strDeclination=String.format(Locale.US,"%.6f",fltDeclination);
                    }
                    strJoin = String.format(
                            Locale.US,
                            "%s,%s,%s",
                            strPlanet,
                            strRightAscension,
                            strDeclination);
                    listPlanets.add(strJoin);
                } while (cursor.moveToNext());
            }
            cursor.close();
            strsPlanets = new String[listPlanets.size()];
            Integer intI = 0;
            for (String strPlanet :
                    listPlanets) {
                strsPlanets[intI++] = strPlanet;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getPlanetStrings: SQL-" +e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getPlanetStrings: " +e.getLocalizedMessage() );
        }
        return strsPlanets;
    }
    public void saveConjunction(String strPlanet, Float fltPlanetRightAscension, Float fltPlanetDeclination, Long dtePlanetRise, Long dtePlanetSet,
                                String strObject, Float fltObjectRightAscension, Float fltObjectDeclination, Long dteObjectRise, Long dteObjectSet,
                                Float fltDistance, Long dteStart, Long dteStop) {
        String strQuery, strQryExec, strFieldNames, strFieldReplace, strFieldValues;
        String[] strsFieldValues, strsKeyValues;
        /**
         * Table astroEventsConjunctions
         *    location_id       - id given to the location by system
         *    planet_name       - name of the primary planet object in conjunction
         *    planet_ra         - decimal coordinate, right ascension (in hours)
         *    planet_decl       - decimal coordinate, declination (in degrees)
         *    conjunction_name  - name of the secondary object (planet, moon, or star) in conjunction
         *    conjunction_ra    - decimal coordinate, right ascension (in hours)
         *    conjunction_decl  - decimal coordinate, declination (in degrees)
         *    angular_distance  - distance between objects in degrees
         *    conjunction_start - date and time when the sky is dark enough to see the conjunction
         *    conjunction_rise  - date and time when both objects have risen above the horizon
         *    conjunction_set   - date and time when either object approaches the horizon
         *    conjunction_stop  - date and time when the sky gets bright enough to extinguish one of the objects
         */
        /**
         * Table astroPlanets
         *    location_id       - id given to the location by system
         *    planet_name       - name of the primary planet object in conjunction
         *    planet_ra         - decimal coordinate, right ascension (in hours)
         *    planet_decl       - decimal coordinate, declination (in degrees)
         *    planet_start - date and time when the sky is dark enough to see the conjunction
         *    planet_rise  - date and time when both objects have risen above the horizon
         *    planet_set   - date and time when either object approaches the horizon
         *    planet_stop  - date and time when the sky gets bright enough to extinguish one of the objects
         */
        strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strPlanet,
                String.format(Locale.US,"%.6f",fltPlanetRightAscension),
                String.format(Locale.US,"%.6f",fltPlanetDeclination),
                String.format(Locale.US,"%d",dtePlanetRise),
                String.format(Locale.US,"%d",dtePlanetSet),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dteStop)
        };
        strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strPlanet,
                String.format(Locale.US,"%d",dteStart - 1800),
                String.format(Locale.US,"%d",dteStop + 1800)
        };
        try {
            if(strPlanet!=null && !strPlanet.isEmpty() && ".Mercury.Venus.Mars.Jupiter.Saturn.Uranus.Neptune.".contains(strPlanet)) {
                strQuery = "SELECT id FROM astroPlanets WHERE location_id=? AND planet_name=? AND planet_start>? AND planet_stop<?";
                if (astroDB.rawQuery(strQuery, strsKeyValues).getCount() == 0) {
                    strFieldNames = "location_id,planet_name,planet_ra,planet_decl," +
                            "planet_rise,planet_set," +
                            "planet_start,planet_stop";
                    strFieldValues = "?,?,?,?," +
                            "?,?," +
                            "?,?";
                    strQryExec = "INSERT INTO astroPlanets " +
                            "(" + strFieldNames + ") " +
                            "VALUES (" + strFieldValues + ")";
                    astroDB.execSQL(strQryExec, strsFieldValues);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveConjunction: SQL-" + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveConjunction: " + e.getLocalizedMessage());
        }
        strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strObject,
                String.format(Locale.US,"%.6f",fltObjectRightAscension),
                String.format(Locale.US,"%.6f",fltObjectDeclination),
                String.format(Locale.US,"%d",dteObjectRise),
                String.format(Locale.US,"%d",dteObjectSet),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dteStop)
        };
        strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strObject,
                String.format(Locale.US,"%d",dteStart - 1800),
                String.format(Locale.US,"%d",dteStop + 1800)
        };
        try {
            if(strObject!=null && !strObject.isEmpty() && ".Mercury.Venus.Mars.Jupiter.Saturn.Uranus.Neptune.".contains(strObject)) {
                strQuery = "SELECT id FROM astroPlanets WHERE location_id=? AND planet_name=? AND planet_start>? AND planet_stop<?";
                if (astroDB.rawQuery(strQuery, strsKeyValues).getCount() == 0) {
                    strFieldNames = "location_id,planet_name,planet_ra,planet_decl," +
                            "planet_rise,planet_set," +
                            "planet_start,planet_stop";
                    strFieldValues = "?,?,?,?," +
                            "?,?," +
                            "?,?";
                    strQryExec = "INSERT INTO astroPlanets " +
                            "(" + strFieldNames + ") " +
                            "VALUES (" + strFieldValues + ")";
                    astroDB.execSQL(strQryExec, strsFieldValues);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveConjunction: SQL-" + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveConjunction: " + e.getLocalizedMessage());
        }
        strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strPlanet,
                String.format(Locale.US,"%.6f",fltPlanetRightAscension),
                String.format(Locale.US,"%.6f",fltPlanetDeclination),
                strObject,
                String.format(Locale.US,"%.6f",fltObjectRightAscension),
                String.format(Locale.US,"%.6f",fltObjectDeclination),
                String.format(Locale.US,"%.6f",fltDistance),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dteStop)
        };
        strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strPlanet,
                strObject,
                String.format(Locale.US,"%d",dteStart - 1800),
                String.format(Locale.US,"%d",dteStop + 1800)
        };
        try {
            strQuery = "SELECT id FROM astroEventsConjunctions WHERE location_id=? AND planet_name=? AND conjunction_name=? AND conjunction_start>? AND conjunction_stop<?";
            strQryExec = "DELETE FROM astroEventsConjunctions WHERE id in(" + strQuery + ")";
            if (astroDB.rawQuery(strQuery, strsKeyValues).getCount() > 0) {
                astroDB.execSQL(strQryExec, strsKeyValues);
            }
            strFieldNames = "location_id,planet_name,planet_ra,planet_decl," +
                    "conjunction_name,conjunction_ra,conjunction_decl," +
                    "angular_distance,conjunction_start,conjunction_stop";
            strFieldValues = "?,?,?,?,?,?,?,?,?,?";
            strQryExec = "INSERT INTO astroEventsConjunctions " +
                    "(" + strFieldNames + ") " +
                    "VALUES (" + strFieldValues + ")";
            astroDB.execSQL(strQryExec, strsFieldValues);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveConjunction: SQL-" + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveConjunction: " + e.getLocalizedMessage());
        }
    }

    public void saveVariableStar(String strPlanet, String strObject, Float fltDistance, Long dteStart, Long dteStop) {
        String strQuery, strQryExec, strFieldNames, strFieldReplace, strFieldValues;
        String[] strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strPlanet,
                strObject,
                String.format(Locale.US,"%.6f",fltDistance),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dteStop)
        }, strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strPlanet,
                strObject,
                String.format(Locale.US,"%d",dteStart - 1800),
                String.format(Locale.US,"%d",dteStop + 1800)
        };
        /**
         * Table astroEventsVariables
         *    location_id       - id given to the location by system
         *    variable_name     - name to give to the variable
         *    ra                - decimal coordinate, right ascension (in hours)
         *    decl              - decimal coordinate, declination (in degrees)
         *    time_rise         - time the variable rises
         *    time_set          - time the variable sets
         *    mag_max           - decimal value, estimated magnitude at maximum
         *    mag_min           - decimal value, estimated magnitude at minimum
         *    date_max          - estimated date of maximum
         *    date_min          - estimated date of minimum
         */
        try {
            strQuery = "SELECT id FROM astroEventsVariables WHERE location_id=? AND variable_name=? AND date_max>? AND date_min<?";
            if (astroDB.rawQuery(strQuery, strsKeyValues).getCount() == 0) {
                // Data is updated annually. No need to update what already exists.
                strFieldNames = "location_id,variable_name,ra,decl,mag_max,mag_min,date_max,date_min";
                strFieldValues = "?,?,?,?,?,?,?,?";
                strQryExec = "INSERT INTO astroEventsVariables " +
                        "(" + strFieldNames + ") " +
                        "VALUES (" + strFieldValues + ")";
                astroDB.execSQL(strQryExec, strsFieldValues);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveVariableStar: SQL-" + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveVariableStar: " + e.getLocalizedMessage());
        }
    }
    public HashMap<String,String> getVariableStar() {
        // return the next or current meteor shower
        Cursor cursor;
        HashMap<String, String> map = new HashMap<>();

        return map;
    }

    public void saveMeteorShower(String strShowerName, Float fltRightAscension, Float fltDeclination,
                                 Long dteRise, Long dteSet, Float fltMagnitude, Integer intHourlyRate,
                                 Long dteStart, Long dtePeak, Long dteStop) {
        String strQuery, strQryExec, strFieldNames, strFieldReplace, strFieldValues;
        String[] strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strShowerName,
                String.format(Locale.US,"%.6f",fltRightAscension),
                String.format(Locale.US,"%.6f",fltDeclination),
                String.format(Locale.US,"%d",dteRise),
                String.format(Locale.US,"%d",dteSet),
                String.format(Locale.US,"%.6f",fltMagnitude),
                String.format(Locale.US,"%d",intHourlyRate),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dtePeak),
                String.format(Locale.US,"%d",dteStop)
        }, strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strShowerName,
                String.format(Locale.US,"%d",dteStart-1800)
        };
        /**
         * Table astroEventsMeteorShowers
         *    location_id       - id given to the location by system
         *    radiant_name      - name to give to the meteor shower
         *    ra                - decimal coordinate, right ascension (in hours)
         *    decl              - decimal coordinate, declination (in degrees)
         *    time_rise         - time the radiant point rises
         *    time_set          - time the radiant point sets
         *    mag               - decimal value, estimated magnitude of average meteor
         *    rate              - number of visible meteors per hour
         *    date_start        - estimated date the meteor shower begins
         *    date_peak         - estimated date the meteor shower peaks
         *    date_stop         - estimated date the meteor shower stops
         */
        try {
            strQuery = "SELECT id FROM astroEventsMeteorShowers WHERE location_id=? AND radiant_name=? AND date_start>?";
            if(astroDB.rawQuery(strQuery,strsKeyValues).getCount()==0) {
                strFieldNames = "location_id,radiant_name,ra,decl,time_rise,time_set,mag,rate,date_start,date_peak,date_stop";
                strFieldValues = "?,?,?,?,?,?,?,?,?,?,?";
                strQryExec = "INSERT INTO astroEventsMeteorShowers " +
                        "(" + strFieldNames + ") " +
                        "VALUES (" + strFieldValues + ")";
                astroDB.execSQL(strQryExec,strsFieldValues);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveMeteorShower: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveMeteorShower: "+ e.getLocalizedMessage());
        }

    }
    public HashMap<String,String> getMeteorShower() {
        // return the next or current meteor shower
        Cursor cursor;
        HashMap<String, String> map = new HashMap<>();
        Long dteStart,dtePeak,dteStop, dteNow = ((Date) new Date()).getTime()/1000L;
        Integer ndxColumn;
        String strQuery, strFieldNames = "location_id,radiant_name,ra,decl,time_rise,time_set,mag," +
                "rate,date_start,date_peak,date_stop";
        String[] strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                String.format(Locale.US,"%d",dteNow-1800)
        };
        try {
            strQuery = "SELECT " + strFieldNames +
                    " FROM astroEventsMeteorShowers WHERE location_id=? AND date_stop>?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            if(cursor.getCount()!=0) {
                for (String strField : cursor.getColumnNames()) {
                    ndxColumn = cursor.getColumnIndex(strField);
                    if(ndxColumn>0) map.put(strField,cursor.getString(ndxColumn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getMeteorShower: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getMeteorShower: "+ e.getLocalizedMessage());
        }

        return map;
    }

    public void saveComet(String strCometName, Float fltRightAscension, Float fltDeclination,
                                 Long dteRise, Long dteSet, Float fltMagnitude,
                                 Long dteStart, Long dtePeak, Long dteStop) {
        String strQuery, strQryExec, strFieldNames, strFieldReplace, strFieldValues;
        String[] strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strCometName,
                String.format(Locale.US,"%.6f",fltRightAscension),
                String.format(Locale.US,"%.6f",fltDeclination),
                String.format(Locale.US,"%d",dteRise),
                String.format(Locale.US,"%d",dteSet),
                String.format(Locale.US,"%.6f",fltMagnitude),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dtePeak),
                String.format(Locale.US,"%d",dteStop)
        }, strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                strCometName
        };
        /**
         * Table astroEventsComets
         *    location_id       - id given to the location by system
         *    comet_name        - name to give to the comet
         *    ra                - decimal coordinate, right ascension (in hours)
         *    decl              - decimal coordinate, declination (in degrees)
         *    time_rise         - time the radiant point rises
         *    time_set          - time the radiant point sets
         *    mag               - decimal value, estimated magnitude of comet
         *    date_start        - estimated date the comet appears
         *    date_peak         - estimated date the comet peaks
         *    date_stop         - estimated date the comet is too faint to see
         */
        try {
            strQuery = "SELECT id FROM astroEventsComets WHERE location_id=?1 AND comet_name=?2";
            if(astroDB.rawQuery(strQuery,strsKeyValues).getCount()==0) {
                strFieldNames = "location_id,comet_name,ra,decl,time_rise,time_set,mag,date_start,date_peak,date_stop";
                strFieldValues = "?,?,?,?,?,?,?,?,?,?";
                strQryExec = "INSERT INTO astroEventsComets " +
                        "(" + strFieldNames + ") " +
                        "VALUES (" + strFieldValues + ")";
                astroDB.execSQL(strQryExec,strsFieldValues);
            } else {
                strFieldReplace = "ra=?3,decl=?4,time_rise=?5,time_set=?6,mag=?7,date_start=?8,date_peak=?9,date_stop=?10";
                strQryExec = "UPDATE astroEventsComets SET " + strFieldReplace +
                        " WHERE location_id=?1 AND comet_name=?2";
                astroDB.execSQL(strQryExec,strsFieldValues);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveComet: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveComet: "+ e.getLocalizedMessage());
        }

    }
    public String[] getCometsStrings() {
        String[] strsComets = new String[0];
        ArrayList<String> listComets = new ArrayList<>();
        Cursor cursor;
        String strQuery;
        String[] strsKeyValues;
        Integer ndxComet, ndxRightAscension, ndxDeclination, ndxMagnitude;
        try {
            strQuery = "SELECT * FROM astroEventsComets " +
                    "WHERE location_id=?";
            // strFieldNames = "location_id,comet_name,ra,decl,time_rise,time_set,mag,date_start,date_peak,date_stop";
            strsKeyValues = new String[]{
                    Integer.toString(this.idObservatory)
            };
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToFirst();
            ndxComet=cursor.getColumnIndex("comet_name");
            ndxRightAscension=cursor.getColumnIndex("ra");
            ndxDeclination=cursor.getColumnIndex("decl");
            ndxMagnitude=cursor.getColumnIndex("mag");
            if(cursor.getCount()>0) {
                String strJoin;
                do {
                    if(!cursor.isNull(ndxComet) && !cursor.isNull(ndxMagnitude) &&
                            !cursor.isNull(ndxRightAscension) && !cursor.isNull(ndxDeclination)) {
                        strJoin = cursor.getString(ndxComet) +","+
                                cursor.getString(ndxRightAscension) +","+
                                cursor.getString(ndxDeclination) +","+
                                cursor.getString(ndxMagnitude);
                        listComets.add(strJoin);
                    }
                } while (cursor.moveToNext());
                strsComets = new String[listComets.size()];
                for (int intI = 0; intI < listComets.size(); intI++) {
                    strsComets[intI] = listComets.get(intI);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getCometsStrings: SQL-" + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getCometsStrings: " + e.getLocalizedMessage());
        }

        return strsComets;
    }

    public void saveEclipse(String strEclipseType, Float fltRightAscension, Float fltDeclination,
                          Long dteRise, Long dteSet,
                          Long dteStart, Long dtePeak, Long dteStop) {
        String strQuery, strQryExec, strFieldNames, strFieldReplace, strFieldValues, strPeak;
        strPeak = String.format("%1$tY%1$tm%1$td",new Date(dtePeak*1000L));
        String[] strsFieldValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                String.format(Locale.US,"%s",strPeak),
                strEclipseType,
                String.format(Locale.US,"%.6f",fltRightAscension),
                String.format(Locale.US,"%.6f",fltDeclination),
                String.format(Locale.US,"%d",dteRise),
                String.format(Locale.US,"%d",dteSet),
                String.format(Locale.US,"%d",dteStart),
                String.format(Locale.US,"%d",dtePeak),
                String.format(Locale.US,"%d",dteStop)
        }, strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                String.format(Locale.US,"%s",strPeak)
        };
        /**
         * Table astroEventsEclipses
         *    location_id       - id given to the location by system
         *    eclipse_name      - name to give to the eclipse, MMMM D, YYYY
         *    eclipse_type      - type of eclipse, lunar/solar, total/partial/hybrid/annular/penumbra
         *    ra                - decimal coordinate, right ascension (in hours)
         *    decl              - decimal coordinate, declination (in degrees)
         *    time_rise         - time the radiant point rises
         *    time_set          - time the radiant point sets
         *    date_start        - estimated date the eclipse penumbra begins
         *    date_umbra_start  - estimated date the eclipse umbra begins
         *    date_totality_start - estimated date the totality begins
         *    date_peak         - estimated date the eclipse peaks
         *    date_totality_stop - estimated date the eclipse totality ends
         *    date_umbra_stop   - estimated date the eclipse umbra ends
         *    date_stop         - estimated date the eclipse penumbra ends
         */
        try {
            strQuery = "SELECT id FROM astroEventsEclipses WHERE location_id=? AND eclipse_name=?";
            if(astroDB.rawQuery(strQuery,strsKeyValues).getCount()==0) {
                strFieldNames = "location_id,eclipse_name,eclipse_type,ra,decl,time_rise,time_set,date_start,date_peak,date_stop";
                strFieldValues = "?,?,?,?,?,?,?,?,?,?";
                strQryExec = "INSERT INTO astroEventsEclipses " +
                        "(" + strFieldNames + ") " +
                        "VALUES (" + strFieldValues + ")";
                astroDB.execSQL(strQryExec,strsFieldValues);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "saveEclipse: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveEclipse: "+ e.getLocalizedMessage());
        }

    }
    public HashMap<String,String> getNextEclipse() {
        // return the next or current eclipse
        Cursor cursor;
        HashMap<String, String> map = new HashMap<>();
        Long dteStart,dtePeak,dteStop, dteNow = ((Date) new Date()).getTime()/1000L;
        Integer ndxColumn;
        String strQuery, strFieldNames = "location_id,eclipse_name,eclipse_type,ra,decl," +
                "time_rise,time_set,date_start,date_peak,date_stop";
        String[] strsKeyValues = new String[]{
                String.format(Locale.US,"%d",this.idObservatory),
                String.format(Locale.US,"%d",dteNow-1800)
        };
        try {
            strQuery = "SELECT " + strFieldNames +
                    " FROM astroEventsEclipses WHERE location_id=? AND date_stop>?" +
                    " ORDER BY date_peak" +
                    " LIMIT 1";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            if(cursor.getCount()!=0) {
                for (String strField : cursor.getColumnNames()) {
                    ndxColumn = cursor.getColumnIndex(strField);
                    if(ndxColumn>0) map.put(strField,cursor.getString(ndxColumn));
                }
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getNextEclipse: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getNextEclipse: "+ e.getLocalizedMessage());
        }

        return map;
    }

    public ArrayList<String> getEvents(@NonNull Date dteFrom, @NonNull Date dteUntil) {
        // return the next or current eclipse
        Cursor cursor;
        ArrayList<String> map = new ArrayList<>();
        Long dteStart=dteFrom.getTime()/1000L, dtePeak,
                dteStop=dteUntil.getTime()/1000L,
                dteNow = ((Date) new Date()).getTime() / 1000L;
        Integer ndxName, ndxType, ndxDate;
        String strQuery, strFieldNames = "location_id,event_name,event_type,date_start";
        String[] strsKeyValues = new String[]{
                String.format(Locale.US, "%d", this.idObservatory),
                String.format(Locale.US, "%d", dteStart - 1800),
                String.format(Locale.US, "%d", dteStop + 1800)
        };
        try {
            strQuery = "SELECT location_id, vehicle_name AS event_name, vehicle_type AS event_type, pass_peak AS date_start" +
                    " FROM astroEventsSatellites" +
                    " WHERE location_id=? AND pass_peak>=? AND pass_peak<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            strQuery = "SELECT location_id, variable_name AS event_name, 'VARIABLEMAX' AS event_type, date_max AS date_start" +
                    " FROM astroEventsVariables" +
                    " WHERE location_id=? AND date_max>=? AND date_max<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            strQuery = "SELECT location_id, variable_name AS event_name, 'VARIABLEMIN' AS event_type, date_min AS date_start" +
                    " FROM astroEventsVariables" +
                    " WHERE location_id=? AND date_min>=? AND date_min<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            strQuery = "SELECT location_id, radiant_name AS event_name, 'METEORSHOWERBEG' AS event_type, date_start AS date_start" +
                    " FROM astroEventsMeteorShowers" +
                    " WHERE location_id=? AND date_start>=? AND date_start<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            strQuery = "SELECT location_id, radiant_name AS event_name, 'METEORSHOWERPEAK' AS event_type, date_peak AS date_start" +
                    " FROM astroEventsMeteorShowers" +
                    " WHERE location_id=? AND date_peak>=? AND date_peak<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            strQuery = "SELECT location_id, radiant_name AS event_name, 'METEORSHOWERFIN' AS event_type, date_stop AS date_start" +
                    " FROM astroEventsMeteorShowers" +
                    " WHERE location_id=? AND date_stop>=? AND date_stop<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            strQuery = "SELECT location_id, eclipse_name AS event_name, eclipse_type AS event_type, date_start AS date_start" +
                    " FROM astroEventsEclipses" +
                    " WHERE location_id=? AND date_start>=? AND date_start<?";
            cursor = astroDB.rawQuery(strQuery,strsKeyValues);
            cursor.moveToFirst();
            ndxDate = cursor.getColumnIndexOrThrow("date_start");
            ndxName = cursor.getColumnIndexOrThrow("event_name");
            ndxType = cursor.getColumnIndexOrThrow("event_type");
            if(cursor.getCount()!=0) {
                do {
                    map.add(String.format(
                            Locale.US,
                            "%d,%s,%s",
                            cursor.getLong(ndxDate),
                            cursor.getString(ndxName),
                            cursor.getString(ndxType)
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "getEvents: SQL-"+e.getLocalizedMessage() );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getEvents: "+ e.getLocalizedMessage());
        }

        return map;
    }

    public void HouseKeeping() {
        // Delete any records that are old, depending on what old means
        // Suggested to perform on boot up before the next download
        /*
        *
        * astroLocation             - never gets old, but may contain duplicates
        * astroWeather              - keep just the most recent. nothing older than yesterday's weather
        * astroWeatherSummary       - keep just the most recent
        * astroEventsConjunctions   - keep just the most recent
        * astroPlanets              - keep just the most recent
        * astroEventsSatellites     - keep just the most recent
        * astroEventsVariables      - data is for the whole year, keep until new data comes in
        * astroEventsMeteorShowers  - data does not change year-to-year. Move dates up 365¼ days.
        * astroEventsComets         - data does not repeat, but magnitude might no be as predicted
        * astroEventsEclipses       - data is available a decade ahead of time.
        *
        * */
        String strQuery, strQryExec;
        String[] strsKeyValues;
        Cursor cursor;
        int ndxColumn;

        // Useful parameters
        Long lngYesterday, lngLastWeek, lngLastYear, lngLastNewYear,
                lngSqlYesterday, lngSqlLastWeek, lngSqlLastYear;
        Date dteYesterday, dteLastWeek, dteLastYear, dteLastNewYear;
        Calendar calLastNewYear;

        lngLastWeek = new Date().getTime()-ONE_FULL_WEEK*1000;
        lngYesterday = new Date().getTime()-ONE_FULL_DAY*1000;
        lngLastYear = new Date().getTime()-ONE_FULL_DAY*1000*365;

        dteLastWeek = new Date(lngLastWeek);
        dteYesterday = new Date(lngYesterday);
        dteLastYear = new Date(lngLastYear);

        calLastNewYear = Calendar.getInstance(Locale.US);
        calLastNewYear.setTime(dteYesterday);
        calLastNewYear.set(calLastNewYear.get(Calendar.YEAR),0,1);

        lngSqlLastWeek = lngLastWeek/1000;

        // Check for duplicate locations.  Suggest that the widow gets deleted
        strQuery = "SELECT * FROM astroLocation" +
                " WHERE id IN(" +
                ")";
        strsKeyValues = new String[]{Long.toString(lngSqlLastWeek)};

        // Delete past weather, but keep most recent night
        strQuery = "SELECT id FROM astroWeather" +
                " WHERE weather_stop<?";
        cursor = astroDB.rawQuery(strQuery,strsKeyValues); cursor.moveToLast();
        if(cursor.getCount()>0) {
            strQryExec = "DELETE FROM astroWeather " +
                    "WHERE id IN(" + strQuery +
                    ")";
            astroDB.execSQL(strQryExec,strsKeyValues);
        }

        strQuery = "SELECT id FROM astroWeatherSummary" +
                " WHERE weather_stop<?";
        if(cursor.getCount()>0) {
            cursor = astroDB.rawQuery(strQuery, strsKeyValues);
            cursor.moveToLast();
            strQryExec = "DELETE FROM astroWeatherSummary " +
                    "WHERE id IN(" + strQuery +
                    ")";
            astroDB.execSQL(strQryExec,strsKeyValues);
        }

        // Delete past planetary conjunctions, but keep most recent night
        strQuery = "SELECT id FROM astroEventsConjunctions" +
                " WHERE conjunction_stop<?";
        cursor = astroDB.rawQuery(strQuery,strsKeyValues); cursor.moveToLast();
        if(cursor.getCount()>0) {
            strQryExec = "DELETE FROM astroEventsConjunctions " +
                    "WHERE id IN(" + strQuery +
                    ")";
            astroDB.execSQL(strQryExec,strsKeyValues);
        }

        // Delete past satellites, but keep most recent night
        strQuery = "SELECT id FROM astroEventsSatellites" +
                " WHERE pass_set<?";
        cursor = astroDB.rawQuery(strQuery,strsKeyValues); cursor.moveToLast();
        if(cursor.getCount()>0) {
            strQryExec = "DELETE FROM astroEventsSatellites " +
                    "WHERE id IN(" + strQuery +
                    ")";
            astroDB.execSQL(strQryExec,strsKeyValues);
        }

        // Delete ancient variables, repeat for next year
        strQuery = "SELECT id FROM astroEventsVariables";

        // Delete ancient meteor showers, repeat for next year
        strQuery = "SELECT id FROM astroEventsMeteorShowers";

        // Delete past comets
        strQuery = "SELECT id FROM astroEventsComets";

        // Delete past eclipses
        strQuery = "SELECT id FROM astroEventsEclipses";


    }

}

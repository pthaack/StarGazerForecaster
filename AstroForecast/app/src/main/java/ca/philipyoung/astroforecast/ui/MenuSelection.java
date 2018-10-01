package ca.philipyoung.astroforecast.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.BuildConfig;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ca.philipyoung.astroforecast.ActivityCalendar;
import ca.philipyoung.astroforecast.ActivityComingSoon;
import ca.philipyoung.astroforecast.ActivitySettings;
import ca.philipyoung.astroforecast.R;
import ca.philipyoung.astroforecast.util.AstroDatabase;
import ca.philipyoung.astroforecast.util.FileService;

public class MenuSelection {
    // shared preference keys
    private static final String CASCADED_CLOSE_KEY = "cascaded_close_key";
    private static final String OBSERVATORY_ID_KEY = "observatory_id";
    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String AURORA_LEVEL_KEY = "aurora";
    private static final String FIELD_LAT_KEY = "geo_latitude";
    private static final String FIELD_LON_KEY = "geo_longitude";
    private static final String FIELD_CITY_KEY = "nearby_city";
    private static final String FIELD_START_KEY = "date_start";
    private static final String FIELD_PEAK_KEY = "date_peak";
    private static final String FIELD_STOP_KEY = "date_stop";
    private static final String FIELD_METEOR_SHOWER_NAME_KEY = "radiant_name";
    private static final String GPS_SWITCH_KEY = "gps_switch";
    private static final String GPS_SNAP_KEY = "gps_snap";

    private static final Integer DIALOG_DISMISS = 1;
    private static final Integer LOCATION_TYPE_KEY = 10;
    private static final Integer LOCATION_EDIT_KEY = 301;
    private static final Integer LOCATION_DELETE_KEY = 302;
    private static final Integer LOCATION_GOTO_KEY = 303;

    private static final Integer SMS_MESSAGE_LENGTH_SHORT = 160;
    private static final Integer SMS_MESSAGE_LENGTH_LONG = 918;

    Dialog dialog;
    String versionName = BuildConfig.VERSION_NAME;

    public boolean doSettingSelection(Context context, MenuItem menuItem){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = menuItem.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(context, ActivitySettings.class);
            context.startActivity(intent);
            return true;

        }/* else if (id == R.id.action_constellation) {

            Intent intent = new Intent(context, ActivitySettings.class);
            intent.putExtra("frame", "constellation");
            context.startActivity(intent);
            return true;

        } */

        return false;
    }

    public void doMenuSelection(final Context context, MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_location) {
            // Pop up navigation of locations
            locationLibrary(context);

        // } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_calendar) {
            Intent intent = new Intent(context, ActivityComingSoon.class);
            context.startActivity(intent);

        } else if (id == R.id.nav_share) {
            // context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jcycreative.studio/")));
            StarsWithFriends starsWithFriends = new StarsWithFriends(context);

        } else if (id == R.id.nav_send) {
            // From: https://stackoverflow.com/questions/8701634/send-email-intent
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {context.getString(R.string.document_contact_email_address)});
                intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.document_contact_email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.document_contact_email_body));

                context.startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException errAnfe) {
                // From: https://stackoverflow.com/questions/8701634/send-email-intent
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.document_contact_email_url))));
            }

        } else if (id == R.id.nav_sync) {
            Toast.makeText(context, context.getString(R.string.ws_downloading_updates), Toast.LENGTH_LONG).show();
            startFileService(context);

        } else if (id == R.id.nav_exit) {
            Boolean sPclose = true;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(CASCADED_CLOSE_KEY, sPclose);
            editor.apply();
            ((Activity)context).finish();   // ToDone; Exit right out of the app
        }

    }

    private void locationLibrary(final Context context) {
        String strObservatory = context.getString(R.string.pref_default_location_name);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences!=null && sharedPreferences.contains(OBSERVATORY_KEY)) {
            strObservatory = sharedPreferences.getString(OBSERVATORY_KEY,strObservatory);
        }
        AstroDatabase astroDatabase = new AstroDatabase(context);
        HashMap<String,String> mapObs = astroDatabase.getObservatories();
        astroDatabase.astroDBclose();

        if(mapObs.size()==0 || mapObs.size()==1 && mapObs.containsKey("-1")) {
            if(dialog==null || !dialog.isShowing()) {
                if(strObservatory.equals(R.string.pref_default_location_name)) {
                    dialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.loclib_dialog_new_library_title)
                            .setMessage(R.string.loclib_dialog_new_library_message)
                            .setPositiveButton(R.string.loclib_dialog_new_library_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                } else {
                    dialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.loclib_dialog_empty_library_title)
                            .setMessage(R.string.loclib_dialog_empty_library_message)
                            .setPositiveButton(R.string.loclib_dialog_empty_library_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(context, context.getString(R.string.ws_downloading_updates), Toast.LENGTH_LONG).show();
                                    startFileService(context);
                                }
                            })
                            .create();
                    dialog.show();
                }
            }
        } else {
            String strTitle = context.getString(R.string.dialog_pop_up_locations_title);
            String strMessage = context.getString(R.string.dialog_pop_up_locations_message);
            /** if (mapObs.size() > 1) {
                Set<String> keySet = mapObs.keySet();
                if (keySet.size()>1) {
                    String[] strsObservatory;
                    for (String strKey : keySet) {
                        strsObservatory = mapObs.get(strKey).split(",");
                        if(!strsObservatory[0].equals(context.getString(R.string.pref_default_location_name))) {
                            strMessage += "\n" + strsObservatory[0] +
                                    "\n "+ strsObservatory[1] +
                                    (strsObservatory[2].equals("null") ? "" : "\n " +strsObservatory[2]) +
                                    "\n Altitude:"+ strsObservatory[3];
                            break;
                        }
                    }
                }
            }    **/
            final PopUpApps popUp = new PopUpApps(context,LOCATION_TYPE_KEY);
            popUp.setCancelable(true);
            popUp.show();
            popUp.setDialog(popUp);
            popUp.setTitle(strTitle);
            popUp.setMessage(strMessage);
            popUp.setViewArray(mapObs);
            popUp.setNegativeButtonText(R.string.dialog_pop_up_locations_positive);
            popUp.setNegativeClick(LOCATION_EDIT_KEY);
            popUp.setPositiveButtonText(R.string.dialog_pop_up_locations_negative);
            popUp.setPositiveClick(LOCATION_DELETE_KEY);
            popUp.setNeutralButtonText(R.string.dialog_pop_up_locations_neutral);
            popUp.setNeutralClick(LOCATION_GOTO_KEY);
            popUp.setCancelClick(DIALOG_DISMISS);

        }
    }

    /*
     * Build a bear.
     * Assemble a set of events to share with friends, and invite them over.
     */
    public class StarsWithFriends {
        private static final String MOON_PHASE_KEY = "moon_phase_name";
        private static final String MOON_PHASE_0_KEY = "New Moon";
        private static final String MOON_PHASE_1_KEY = "Waxing Crescent";
        private static final String MOON_PHASE_2_KEY = "First Quarter";
        private static final String MOON_PHASE_3_KEY = "Waxing Gibbous";
        private static final String MOON_PHASE_4_KEY = "Full Moon";
        private static final String MOON_PHASE_5_KEY = "Waning Gibbous";
        private static final String MOON_PHASE_6_KEY = "Third Quarter";
        private static final String MOON_PHASE_7_KEY = "Waning Crescent";
        private final Long ONE_HOUR = 3600000L; // the number of seconds in an hour.
        private final Long ONE_FULL_DAY = 86400000L; // the number of seconds in one day.
        private final Long ONE_HALF_DAY = 43200000L; // the number of seconds in half a day.
        private final Long ONE_FULL_WEEK = 604800000L; // the number of seconds in one week.

        private Context mContext;
        private Integer mCurrentMessage, mGoDirection;
        private String[] mSavedMessages;
        private Integer[] mTemplates = new Integer[]{
                R.string.document_share_introduction,
                R.string.document_share_weather,
                R.string.document_share_moon,
                R.string.document_share_meteor_shower,
                R.string.document_share_planets,
                R.string.document_share_conjunction,
                R.string.document_share_comet,
                R.string.document_share_aurora,
                R.string.document_share_iss,
                R.string.document_share_variable_star_visible,
                R.string.document_share_variable_star_eclipse,
                R.string.document_share_eclipse,
                R.string.document_share_closing
        };

        public StarsWithFriends(Context context){
            this.mContext = context;
            this.mCurrentMessage = 0;
            this.mGoDirection = 1;
            this.mSavedMessages = new String[this.mTemplates.length];
            this.displayCurrentMessage();
        }

        public void displayCurrentMessage() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String strObservatory = sharedPreferences.getString(OBSERVATORY_KEY,mContext.getString(R.string.pref_default_location_name));
            String strMessage, strLatitude, strLongitude, strNearbyCity;
            Float fltLat, fltLng;
            Date dteNow = new Date(),
                    dteFarFuture = new Date((new Date().getTime())+365*ONE_FULL_DAY),
                    dteStart, dtePeak, dteStop;
            AstroDatabase astroDatabase;
            final android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this.mContext);
            StringBuilder stringBuilder = new StringBuilder("");
            for (String string : mSavedMessages) {
                if(string!=null) {
                    if (stringBuilder.length() > 0) stringBuilder.append("\n");
                    stringBuilder.append(string);
                }
            }
            final EditText editText = new EditText(this.mContext);
            final TextView textView = new TextView(this.mContext);
            editText.setMaxLines(3);
            textView.setText(String.format("%d/%d",stringBuilder.length(), SMS_MESSAGE_LENGTH_SHORT));
            if(this.mSavedMessages[this.mCurrentMessage]==null) {
                switch (this.mTemplates[this.mCurrentMessage]) {
                    case R.string.document_share_weather:
                        String strWeatherGrade, strCloudCover, strTransparency;
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        Integer intWeatherSummary = astroDatabase.getEveningWeather();
                        Map<Long,String> mapWeatherData = astroDatabase.getWeather();
                        astroDatabase.astroDBclose();
                        if( intWeatherSummary>=8 ) {
                            strWeatherGrade = mContext.getString(R.string.document_share_weather_grade_great);
                        } else if( intWeatherSummary>=6 ) {
                            strWeatherGrade = mContext.getString(R.string.document_share_weather_grade_good);
                        } else if( intWeatherSummary==-1 ) {
                            strWeatherGrade = mContext.getString(R.string.document_share_weather_grade_unknown);
                        } else {
                            strWeatherGrade = mContext.getString(R.string.document_share_weather_grade_bad);
                        }
                        if( intWeatherSummary==-1 ) {
                            this.mCurrentMessage+=this.mGoDirection;
                            this.displayCurrentMessage();
                            return;
                        } else {
                            Long[] dtesWeather = new Long[mapWeatherData.size()];
                            Integer intI = 0;
                            for (Long dteWeather : mapWeatherData.keySet()) {
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
                            editText.setText(String.format(
                                    Locale.US,
                                    mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                    strWeatherGrade,
                                    mapWeatherData.get(dtesWeather[0]),
                                    mapWeatherData.get(dtesWeather[mapWeatherData.size()-1])
                            ));
                        }
                        break;

                    case R.string.document_share_moon:
                        String strPhase=getMoonPhase(),
                                strName=getMoonMonthName(new Date());
                        if(strPhase.equals(mContext.getString(R.string.moon_phase_null))) {
                            this.mCurrentMessage+=this.mGoDirection;
                            this.displayCurrentMessage();
                            return;
                        } else {
                            editText.setText(String.format(
                                    Locale.US,
                                    mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                    strName,
                                    strPhase
                                    )
                            );
                        }
                        break;

                    case R.string.document_share_meteor_shower:
                        // Display only if a meteor shower is in progress.
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        HashMap<String,String> mapMeteorShower = astroDatabase.getMeteorShower();
                        astroDatabase.astroDBclose();
                        if(mapMeteorShower.size() == 0) {
                            this.mCurrentMessage+=this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        String strMeteorShowCondition = "No shower";
                        strName = mapMeteorShower.get(FIELD_METEOR_SHOWER_NAME_KEY);
                        dteStart = new Date(
                                mapMeteorShower.get(FIELD_START_KEY)==null ||
                                mapMeteorShower.get(FIELD_START_KEY).isEmpty() ||
                                        mapMeteorShower.get(FIELD_START_KEY).equals("null")?
                                        0L :
                                        Long.valueOf(mapMeteorShower.get(FIELD_START_KEY))
                        );
                        dtePeak = new Date(
                                mapMeteorShower.get(FIELD_PEAK_KEY) == null ||
                                        mapMeteorShower.get(FIELD_PEAK_KEY).isEmpty() ||
                                        mapMeteorShower.get(FIELD_PEAK_KEY).equals("null") ?
                                        0L :
                                        Long.valueOf(mapMeteorShower.get(FIELD_PEAK_KEY))
                        );
                        dteStop = new Date(
                                mapMeteorShower.get(FIELD_STOP_KEY)==null ||
                                        mapMeteorShower.get(FIELD_STOP_KEY).isEmpty() ||
                                        mapMeteorShower.get(FIELD_STOP_KEY).equals("null")?
                                        dteFarFuture.getTime() :
                                        Long.valueOf(mapMeteorShower.get(FIELD_STOP_KEY))
                        );
                        if(dteNow.getTime()<dteStart.getTime() || dteNow.getTime()>dteStop.getTime()) {
                            // no meteor show right now
                            this.mCurrentMessage += this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        } else if(Math.abs(dteNow.getTime() - dteStart.getTime())<=ONE_FULL_DAY) {
                            // meteor shower just started
                            strMeteorShowCondition = mContext.getString(R.string.document_share_meteor_shower_begin);
                        } else if(Math.abs(dteNow.getTime() - dtePeak.getTime())<=ONE_FULL_DAY) {
                            // meteor shower is peaking
                            strMeteorShowCondition = mContext.getString(R.string.document_share_meteor_shower_peak);
                        } else if(dteNow.getTime()>=dteStart.getTime() && dteNow.getTime()<=dteStop.getTime()) {
                            // meteor shower is occurring
                            strMeteorShowCondition = mContext.getString(R.string.document_share_meteor_shower_continue);
                        }
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strName,
                                strMeteorShowCondition,
                                dtePeak
                                )
                        );
                        break;

                    case R.string.document_share_planets:
                        // Display only if a user has selected a favoured planet.
                        String strNamesOfPlanets = "",strFieldString;
                        String[] strsNamesOfPlanets;
                        astroDatabase = new AstroDatabase(mContext, strObservatory);
                        strsNamesOfPlanets = astroDatabase.getPlanetStrings();
                        astroDatabase.astroDBclose();
                        if(strsNamesOfPlanets.length == 0) {
                            this.mCurrentMessage+=this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        for (int intI = 0; intI < strsNamesOfPlanets.length; intI++) {
                            strFieldString = strsNamesOfPlanets[intI];
                            strNamesOfPlanets += (strNamesOfPlanets.isEmpty()?
                                    "":
                                    (intI<strsNamesOfPlanets.length-1?", ":", and ")
                            )+
                                    strFieldString.split(",")[0];
                        }
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strNamesOfPlanets
                                )
                        );
                        break;

                    case R.string.document_share_conjunction:
                        // Display only if a user has selected to know about conjunctions.
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        String[] strsConjunctions = astroDatabase.getConjunctionsStrings(); // get the CSV list of conjunctions
                        astroDatabase.astroDBclose();
                        if(strsConjunctions.length == 0) {
                            this.mCurrentMessage+=this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        // find the closest conjunction
                        String strNameOfPlanet = "",
                                strNameOfObject = "";
                        Float fltPreviousDistance = 180f, fltCurrentDistance;
                        for (String strConjunction : strsConjunctions) {
                            String[] strings = strConjunction.split(",");
                            if(strings.length==6) {
                                fltCurrentDistance = Float.valueOf(strings[5]);
                                if(fltCurrentDistance<fltPreviousDistance) {
                                    strNameOfPlanet = strings[1];
                                    strNameOfObject = strings[2];
                                }
                            }
                        }
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strNameOfPlanet,
                                strNameOfObject
                                )
                        );
                        break;

                    case R.string.document_share_comet:
                        // Display only if a user has selected to know about comets.
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        String[] strsComets = astroDatabase.getCometsStrings();
                        astroDatabase.astroDBclose();
                        String strNameOfComet = mContext.getString(R.string.dialog_pop_up_comet_no_passes);
                        Float fltMagnitude = 100f;
                        if(strsComets.length == 0) {
                            this.mCurrentMessage+=this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        for (String strComet :
                                strsComets) {
                            if (fltMagnitude > Float.valueOf(strComet.split(",")[3])) {
                                strNameOfComet = strComet.split(",")[0];
                                fltMagnitude = Float.valueOf(strComet.split(",")[3]);
                            }
                        }
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strNameOfComet,
                                fltMagnitude
                                )
                        );
                        break;

                    case R.string.document_share_aurora:
                        // Display only if kp-index is above the threshold.
                        Integer intKPindex = Integer.valueOf(sharedPreferences.getString(AURORA_LEVEL_KEY,"-1"));
                        Float fltPercentage = 1f;
                        if(intKPindex <= 4) {
                            this.mCurrentMessage+=this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                fltPercentage
                                )
                        );
                        break;

                    case R.string.document_share_iss:
                        // Display only if the ISS is coming into view.
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        Integer intISSCount = astroDatabase.getSatelliteCountTonightsISS();
                        String[] strsISSlist = astroDatabase.getTonightsSatellites();
                        astroDatabase.astroDBclose();
                        if(intISSCount==0 || strsISSlist.length==0) {
                            this.mCurrentMessage += this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        } else {
                            dtePeak = new Date();
                            for (String strSatellite : strsISSlist) {
                                String[] strsSatellite = strSatellite.split(",");
                                if(strsSatellite.length==6 && strsSatellite[1].equals("ISS")) {
                                    dtePeak = new Date(Long.valueOf(strsSatellite[4])*1000);
                                    break;
                                }
                            }
                        }
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                dtePeak
                                )
                        );
                        break;

                    case R.string.document_share_variable_star_visible:
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        HashMap<String,String> mapVariableStar = astroDatabase.getVariableStar();
                        astroDatabase.astroDBclose();
                        if(mapVariableStar.size()==0) {
                            this.mCurrentMessage += this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        strNameOfObject = mapVariableStar.get("variable_name");
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strNameOfObject
                                )
                        );
                        break;

                    case R.string.document_share_variable_star_eclipse:
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        HashMap<String,String> mapVariableEclipseStar = astroDatabase.getVariableStar();
                        astroDatabase.astroDBclose();
                        if(mapVariableEclipseStar.size()==0) {
                            this.mCurrentMessage += this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        strNameOfObject = mapVariableEclipseStar.get("variable_name");
                        dteStart = new Date(Integer.valueOf(mapVariableEclipseStar.get("date_max"))*1000L);
                        dtePeak = new Date(Integer.valueOf(mapVariableEclipseStar.get("date_min"))*1000L);
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strNameOfObject,
                                dteStart,
                                dtePeak
                                )
                        );
                        break;

                    case R.string.document_share_eclipse:
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        HashMap<String,String> mapNextEclipse = astroDatabase.getNextEclipse();
                        astroDatabase.astroDBclose();
                        if(mapNextEclipse.size()==0) {
                            this.mCurrentMessage += this.mGoDirection;
                            displayCurrentMessage();
                            return;
                        }
                        String strTypeOfEclipse = mapNextEclipse.get("eclipse_type");
                        dteStart = new Date(Integer.valueOf(mapNextEclipse.get("date_start"))*1000L);
                        editText.setText(String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strTypeOfEclipse,
                                dteStart
                                )
                        );
                        break;

                    case R.string.document_share_closing:
                        astroDatabase = new AstroDatabase(mContext,strObservatory);
                        strNearbyCity = astroDatabase.getObservatory(strObservatory).get(FIELD_CITY_KEY);
                        astroDatabase.astroDBclose();
                        fltLat = sharedPreferences.getFloat(COORDINATES_LAT_KEY,0f);
                        fltLng = sharedPreferences.getFloat(COORDINATES_LON_KEY,0f);
                        strLatitude = fltLat>=0? "N": "S";
                        strLongitude = fltLng>=0? "E": "W";
                        strMessage = String.format(
                                Locale.US,
                                mContext.getString(this.mTemplates[this.mCurrentMessage]),
                                strObservatory,
                                Math.abs(fltLat),
                                strLatitude,
                                Math.abs(fltLng),
                                strLongitude,
                                strNearbyCity
                        );
                        editText.setText(strMessage);
                        break;

                    default:
                        editText.setText(this.mTemplates[this.mCurrentMessage]);
                        break;
                }
            } else {
                editText.setText(this.mSavedMessages[this.mCurrentMessage]);
            }
            editText.setId(R.id.text_message);
            dialogBuilder
                    .setIcon(R.drawable.ic_menu_share)
                    .setTitle(R.string.document_share_subject)
                    .setMessage(textView.getText().toString())
                    .setView(editText)
                    .setPositiveButton(R.string.document_share_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSavedMessages[mCurrentMessage] = editText.getText().toString();
                            if (++mCurrentMessage >= mTemplates.length) {
                                displayFinishedMessage();
                            } else {
                                displayCurrentMessage();
                                mGoDirection = 1;
                            }
                        }
                    })
                    .setNegativeButton(R.string.document_share_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSavedMessages[mCurrentMessage]=null;
                            // dialog.dismiss();
                            if(++mCurrentMessage>=mTemplates.length) {
                                displayFinishedMessage();
                            } else {
                                displayCurrentMessage();
                                mGoDirection=1;
                            }
                        }
                    })
                    .setNeutralButton(R.string.document_share_neutral, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSavedMessages[mCurrentMessage] = editText.getText().toString();
                            if (--mCurrentMessage < 0) {
                                mCurrentMessage = 0;
                            }
                            mGoDirection = -1;
                            displayCurrentMessage();
                        }
                    })
                    .create();
            dialogBuilder.show();
        }
        public void displayFinishedMessage() {
            StringBuilder stringBuilder = new StringBuilder("");
            for (String string : mSavedMessages) {
                if(string!=null) {
                    if (stringBuilder.length() > 0) stringBuilder.append("\n");
                    stringBuilder.append(string);
                }
            }
            if(stringBuilder.length() > SMS_MESSAGE_LENGTH_LONG) {
                stringBuilder.append("\n\n" + mContext.getString(R.string.document_share_much_too_long));
            } else if(stringBuilder.length() > SMS_MESSAGE_LENGTH_SHORT) {
                stringBuilder.append("\n\n" + mContext.getString(R.string.document_share_too_long));
            }
            final AlertDialog alertDialog = new AlertDialog.Builder(this.mContext)
                    .setTitle(R.string.document_share_subject)
                    .setPositiveButton(R.string.document_share_done, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StringBuilder stringBuilder = new StringBuilder("");
                            for (String string : mSavedMessages) {
                                if(string!=null) {
                                    if (stringBuilder.length() > 0) stringBuilder.append("\n");
                                    stringBuilder.append(string);
                                }
                            }
                            Intent intentSendText = new Intent(); intentSendText.setAction(Intent.ACTION_SEND);
                            intentSendText.setType("text/plain");
                            intentSendText.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString() );
                            mContext.startActivity(Intent.createChooser(intentSendText, "Share via"));
                        }
                    })
                    .setNegativeButton(R.string.document_share_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton(R.string.document_share_neutral, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(--mCurrentMessage<0) {
                                mCurrentMessage=0;
                            }
                            displayCurrentMessage();
                        }
                    })
                    .setMessage(stringBuilder.toString())
                    .create();
            alertDialog.show();
            if(stringBuilder.length()> SMS_MESSAGE_LENGTH_LONG && alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)!=null) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        }
        private String getMoonPhase() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String strMoonPhase = sharedPreferences.getString(MOON_PHASE_KEY,"No Moon"),
                    strMoonPhaseName = "";
            switch (strMoonPhase) {
                case MOON_PHASE_0_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_0);
                    break;
                case MOON_PHASE_1_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_1);
                    break;
                case MOON_PHASE_2_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_2);
                    break;
                case MOON_PHASE_3_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_3);
                    break;
                case MOON_PHASE_4_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_4);
                    break;
                case MOON_PHASE_5_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_5);
                    break;
                case MOON_PHASE_6_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_6);
                    break;
                case MOON_PHASE_7_KEY:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_7);
                    break;
                default:
                    strMoonPhaseName = mContext.getString(R.string.moon_phase_null);
                    break;
            }
            return strMoonPhaseName;
        }
        private String getMoonMonthName(Calendar calMonthNumber) {
            String strMoonMonthName = mContext.getString(R.string.moon_month_null);
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
                        strMoonMonthName = mContext.getString(R.string.moon_month_01);
                        break;
                    case Calendar.FEBRUARY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_02);
                        break;
                    case Calendar.MARCH:
                        strMoonMonthName = mContext.getString(R.string.moon_month_03);
                        break;
                    case Calendar.APRIL:
                        strMoonMonthName = mContext.getString(R.string.moon_month_04);
                        break;
                    case Calendar.MAY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_05);
                        break;
                    case Calendar.JUNE:
                        strMoonMonthName = mContext.getString(R.string.moon_month_06);
                        break;
                    case Calendar.JULY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_07);
                        break;
                    case Calendar.AUGUST:
                        strMoonMonthName = mContext.getString(R.string.moon_month_08);
                        break;
                    case Calendar.SEPTEMBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_09);
                        break;
                    case Calendar.OCTOBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_10);
                        break;
                    case Calendar.NOVEMBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_11);
                        break;
                    case Calendar.DECEMBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_12);
                        break;
                    default:
                        strMoonMonthName = mContext.getString(R.string.moon_month_null);
                        break;
                }
            } else {
                switch (intMonth) {
                    case Calendar.JANUARY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_02);
                        break;
                    case Calendar.FEBRUARY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_03);
                        break;
                    case Calendar.MARCH:
                        strMoonMonthName = mContext.getString(R.string.moon_month_04);
                        break;
                    case Calendar.APRIL:
                        strMoonMonthName = mContext.getString(R.string.moon_month_05);
                        break;
                    case Calendar.MAY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_06);
                        break;
                    case Calendar.JUNE:
                        strMoonMonthName = mContext.getString(R.string.moon_month_07);
                        break;
                    case Calendar.JULY:
                        strMoonMonthName = mContext.getString(R.string.moon_month_08);
                        break;
                    case Calendar.AUGUST:
                        strMoonMonthName = mContext.getString(R.string.moon_month_09);
                        break;
                    case Calendar.SEPTEMBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_10);
                        break;
                    case Calendar.OCTOBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_11);
                        break;
                    case Calendar.NOVEMBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_12);
                        break;
                    case Calendar.DECEMBER:
                        strMoonMonthName = mContext.getString(R.string.moon_month_01);
                        break;
                    default:
                        strMoonMonthName = mContext.getString(R.string.moon_month_null);
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
        private String getMoonMonthName(Long lngMonthDate) {
            Date date = new Date(lngMonthDate);
            return getMoonMonthName(date);
        }
    }

    /****
     * From Derek Banas' YouTube video (I8XBY1sqz70): How to Make Android Apps 18 - Broadcasts, etc.
     * @param context
     * @return
     */
    public void startFileService(Context context) {
        // Go get the weather
        String strObservatory, urlObservatory, urlLatLon;
        Float fltLat=43f, fltLng=79f;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences!=null) {
            sharedPreferences.edit().remove(AURORA_LEVEL_KEY).apply();
            Intent intent = new Intent(context,FileService.class);
            strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, context.getString(R.string.pref_default_location_name));
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
            context.startService(intent);
        }
    }

}

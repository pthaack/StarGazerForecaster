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
import android.support.design.widget.Snackbar;
import android.support.v4.BuildConfig;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import ca.philipyoung.astroforecast.ActivityCalendar;
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
    private static final String FIELD_LAT_KEY = "geo_latitude";
    private static final String FIELD_LON_KEY = "geo_longitude";
    private static final String GPS_SWITCH_KEY = "gps_switch";
    private static final String GPS_SNAP_KEY = "gps_snap";

    private static final Integer DIALOG_DISMISS = 1;
    private static final Integer LOCATION_TYPE_KEY = 10;
    private static final Integer LOCATION_EDIT_KEY = 301;
    private static final Integer LOCATION_DELETE_KEY = 302;
    private static final Integer LOCATION_GOTO_KEY = 303;

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
            Intent intent = new Intent(context, ActivityCalendar.class);
            context.startActivity(intent);

        } else if (id == R.id.nav_share) {
            // context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jcycreative.studio/")));

        } else if (id == R.id.nav_send) {

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

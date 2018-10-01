package ca.philipyoung.astroforecast.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import ca.philipyoung.astroforecast.R;
import ca.philipyoung.astroforecast.util.AstroDatabase;
import ca.philipyoung.astroforecast.util.FileService;

/**
 * Created by Philip Young on 2018-07-27.
 */

public class PopUpApps extends Dialog implements View.OnClickListener  {
    private Context mContext;
    private Activity mActivity;
    private Dialog mDialog=null;
    private Integer mChartType=null,
            mCurrentView =null;
    private HashMap<String,String> mapMessages;

    private MotionEvent mevPreviousDown = null,
            mevPreviousUp = null;

    private static final String SKEYE_APP = "com.lavadip.skeye";
    private static final String SATELLITE_APP = "com.heavens_above.viewer";
    private static final String VARIABLE_STARS_APP = "com.astroapps.satrid.variablestars";
    private static final String AURORA_APP = "com.beebeetle.auroranotifier";
    private static final String METEOR_APP = "com.ccwilcox.meteorshower";

    private static final int METEOR_LAUNCH_KEY = 101;
    private static final int AURORA_LAUNCH_KEY = 102;
    private static final int VARIABLE_STARS_LAUNCH_KEY = 103;
    private static final int SATELLITE_LAUNCH_KEY = 104;
    private static final int MOON_LAUNCH_KEY = 105;
    private static final int ECLIPSE_LAUNCH_KEY = 106;
    private static final int PLANET_POP_UP_KEY = 121;
    private static final int CONJUNCTION_POP_UP_KEY = 122;
    private static final int LOCATION_EDIT_KEY = 301;
    private static final int LOCATION_DELETE_KEY = 302;
    private static final int LOCATION_GOTO_KEY = 303;

    private static final String SATELLITE_WARNING_KEY = "satellite_warning";
    private static final String AURORA_WARNING_KEY = "aurora_warning";
    private static final String METEOR_WARNING_KEY = "meteor_warning";

    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String COORDINATES_ALT_KEY = "coordinate_altitude";

    private static final int DIALOG_DISMISS = 1;
    private static final int OBJECT_TYPE_KEY = 2;
    private static final int CONSTELLATION_TYPE_KEY = 3;
    private static final int PLANET_TYPE_KEY = 4;
    private static final int METEOR_TYPE_KEY = 8;
    private static final int ECLIPSE_TYPE_KEY = 9;
    private static final int LOCATION_TYPE_KEY = 10;

    private static final Integer LOCATION_ARRAY_OBS_NAME = 0;
    private static final Integer LOCATION_ARRAY_OBS_COORDINATES = 1;
    private static final Integer LOCATION_ARRAY_OBS_CITY = 2;
    private static final Integer LOCATION_ARRAY_OBS_ALTITUDE = 3;

    /**
     * Creates a dialog window that uses the default dialog theme.
     * <p>
     * The supplied {@code context} is used to obtain the window manager and
     * base theme used to present the dialog.
     *
     * @param context the context in which the dialog should run
     */
    public PopUpApps(@NonNull Context context, @NonNull Integer intChartKey) {
        super(context);
        this.mContext=context;
        this.mActivity=(Activity)context;
        this.mChartType=intChartKey;
    }

    public void setDialog(Dialog mDialog) {
        this.mDialog = mDialog;
    }

    /**
     * Similar to {@link Activity#onCreate}, you should initialize your dialog
     * in this method, including calling {@link #setContentView}.
     *
     * @param savedInstanceState If this dialog is being reinitialized after a
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // android.util.AndroidRuntimeException: You cannot combine custom titles with other title features
        setContentView(R.layout.layout_pop_up_planets);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {

    }

    /**
     * Called when the dialog is starting.
     */
    @Override
    protected void onStart() {
        super.onStart();

        View vwScroll = findViewById(R.id.planet_radio_scroll);
        if(vwScroll!=null && vwScroll instanceof ScrollView) vwScroll.setVisibility(View.GONE);
        if(this.mChartType==LOCATION_TYPE_KEY) {
            // enable swipe left and right
            View view = findViewById(R.id.text_message);
            if(view!=null  && view instanceof TextView) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent me) {
                        return popUp_dialog_motion(v,me);
                    }
                });
            }
        }
    }

    /**
     * Set the message text for this dialog's window. The text is retrieved
     * from the resources with the supplied identifier.
     *
     * @param idMessage the messages's text resource identifier
     */
    public void setMessage(Integer idMessage) {
        setMessage(mContext.getString(idMessage));
    }
    /**
     * Set the message text for this dialog's window.
     *
     * @param strMessage The new text to display in the message
     */
    public void setMessage(String strMessage) {
        View vwText = this.findViewById(R.id.text_message);
        if(strMessage!=null && vwText!=null && vwText instanceof TextView) {
            ((TextView)vwText).setText(strMessage);
            vwText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set the title text for this dialog's window. The text is retrieved
     * from the resources with the supplied identifier.
     *
     * @param titleId the title's text resource identifier
     */
    @Override
    public void setTitle(int titleId) {
        setTitle((CharSequence) mContext.getString(titleId));
    }
    /**
     * Set the title text for this dialog's window.
     *
     * @param chrTitle The new text to display in the title.
     */
    @Override
    public void setTitle(@Nullable CharSequence chrTitle) {
        View vwText = this.findViewById(R.id.text_title);
        if(chrTitle!=null && vwText!=null && vwText instanceof TextView) {
            // requestWindowFeature(Window.FEATURE_NO_TITLE); // android.util.AndroidRuntimeException: requestFeature() must be called before adding content
            ((TextView)vwText).setText(chrTitle);
            vwText.setVisibility(View.VISIBLE);
        } else if(chrTitle!=null) {
            super.setTitle(chrTitle);
            // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // android.util.AndroidRuntimeException: requestFeature() must be called before adding content
        }
    }

    public void setViewArray(HashMap<String,String> map) {
        this.mapMessages = map;
        if(this.mCurrentView==null) this.mCurrentView=0; else this.mCurrentView++;
        if(map.get(map.keySet().toArray()[this.mCurrentView]).split(",")[LOCATION_ARRAY_OBS_NAME].equals(mContext.getString(R.string.pref_default_location_name))) {
            this.mCurrentView++;
        }
        if (this.mCurrentView >= this.mapMessages.size()) this.mCurrentView = 0;
        this.setViewFromArray(this.mCurrentView);
    }
    public void setViewFromArray(Integer intView) {
        Set<String> stringSet = this.mapMessages.keySet();
        Object[] strsKeys = stringSet.toArray();
        String strKey = (String) strsKeys[intView];
        String[] strsView = this.mapMessages.get(strKey).split(",");
        if(this.mChartType==LOCATION_TYPE_KEY) {
            View view = findViewById(R.id.text_message);
            if(view!=null && view instanceof TextView) {
                String strMessage = mContext.getString(R.string.dialog_pop_up_locations_message);
                strMessage += "\n" + strsView[LOCATION_ARRAY_OBS_NAME].replaceAll("%2C",",") +
                        "\n "+ strsView[LOCATION_ARRAY_OBS_COORDINATES] +
                        (strsView[LOCATION_ARRAY_OBS_CITY].equals("null") ? "" : "\n " +strsView[LOCATION_ARRAY_OBS_CITY].replaceAll("%2C",",")) +
                        "\n Altitude:"+ strsView[LOCATION_ARRAY_OBS_ALTITUDE];
                ((TextView) view).setText(strMessage);
            }
        }
    }

    public void setPositiveButtonText(Integer idButtonText) {
        setPositiveButtonText(mContext.getString(idButtonText));
    }

    public void setPositiveButtonText(String strButtonText) {
        View vwButton = this.findViewById(R.id.button_positive);
        if (strButtonText != null && vwButton != null && vwButton instanceof Button) {
            ((Button) vwButton).setText(strButtonText);
            vwButton.setVisibility(View.VISIBLE);
        }
    }

    public void setPositiveClick(Integer intMethod) {
        View vwButton = this.findViewById(R.id.button_positive);
        if (intMethod != null && vwButton != null && vwButton instanceof Button) {
            defineClickEvents(vwButton, intMethod);
        }
    }

    public void setNegativeButtonText(Integer idButtonText) {
        setNegativeButtonText(mContext.getString(idButtonText));
    }

    public void setNegativeButtonText(String strButtonText) {
        View vwButton = this.findViewById(R.id.button_negative);
        if (strButtonText != null && vwButton != null && vwButton instanceof Button) {
            ((Button) vwButton).setText(strButtonText);
            vwButton.setVisibility(View.VISIBLE);
        }
    }

    public void setNegativeClick(Integer intMethod) {
        View vwButton = this.findViewById(R.id.button_negative);
        if (intMethod != null && vwButton != null && vwButton instanceof Button) {
            defineClickEvents(vwButton, intMethod);
        }
    }

    public void setNeutralButtonText(Integer idButtonText) {
        setNeutralButtonText(mContext.getString(idButtonText));
    }

    public void setNeutralButtonText(String strButtonText) {
        View vwButton = this.findViewById(R.id.button_neutral);
        if (strButtonText != null && vwButton != null && vwButton instanceof Button) {
            ((Button) vwButton).setText(strButtonText);
            vwButton.setVisibility(View.VISIBLE);
        }
    }

    public void setNeutralClick(Integer intMethod) {
        View vwButton = this.findViewById(R.id.button_neutral);
        if (intMethod != null && vwButton != null && vwButton instanceof Button) {
            defineClickEvents(vwButton, intMethod);
        }
    }

    public void setCancelButtonText(Integer idButtonText) {
        setCancelButtonText(mContext.getString(idButtonText));
    }
    public void setCancelButtonText(String strButtonText) {
        View vwButton = findViewById(R.id.button_cancel);
        if(strButtonText!=null && vwButton!=null && vwButton instanceof Button) {
            ((Button)vwButton).setText(strButtonText);
            vwButton.setVisibility(View.VISIBLE);
        } else if(strButtonText!=null && vwButton!=null && vwButton instanceof ImageView) {
            ((ImageView)vwButton).setContentDescription(strButtonText);
            vwButton.setVisibility(View.VISIBLE);
        }
    }
    public void setCancelClick(Integer intMethod) {
        View vwButton = this.findViewById(R.id.button_cancel);
        if(intMethod!=null && vwButton!=null && vwButton instanceof ImageView) {
            defineClickEvents(vwButton,intMethod);
            vwButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayNext() {
        this.mCurrentView++;
        if (this.mCurrentView >= this.mapMessages.size()) this.mCurrentView = 0;
        if(this.mapMessages.get(this.mapMessages.keySet().toArray()[this.mCurrentView]).split(",")[LOCATION_ARRAY_OBS_NAME].equals(mContext.getString(R.string.pref_default_location_name))) {
            this.mCurrentView++;
            if (this.mCurrentView >= this.mapMessages.size()) this.mCurrentView = 0;
        }
        this.setViewFromArray(this.mCurrentView);
    }

    private void displayPrev() {
        this.mCurrentView--;
        if (this.mCurrentView < 0) this.mCurrentView = this.mapMessages.size() - 1;
        if(this.mapMessages.get(this.mapMessages.keySet().toArray()[this.mCurrentView]).split(",")[LOCATION_ARRAY_OBS_NAME].equals(mContext.getString(R.string.pref_default_location_name))) {
            this.mCurrentView--;
            if (this.mCurrentView < 0) this.mCurrentView = this.mapMessages.size() - 1;
        }
        this.setViewFromArray(this.mCurrentView);
    }

    public boolean onFling(MotionEvent motionEventPress, MotionEvent motionEventRelease, float velocityX, float velocityY) {
        switch (swipeDirection(motionEventPress, motionEventRelease)) {
            case 0:
                displayPrev();
                break;
            case 1:
            case 2:
            case 3:
                break;
            case 4:
                displayNext();
                break;
            case 5:
            case 6:
            case 7:
                break;
            default:
                break;
        }
        return false;
    }

    // Detect motions, and Jay double tap.
    @NonNull
    public Boolean popUp_dialog_motion(View view, MotionEvent motionEvent){
        // Detect one finger, and that it stopped touching the dialog box
        if(motionEvent.getPointerCount()==1 && (
                motionEvent.getAction()==MotionEvent.ACTION_UP ||
                        motionEvent.getAction()==MotionEvent.ACTION_CANCEL) ) {
            // tell the difference between a scroll and a fling
            if(mevPreviousDown!=null && (
                    motionEvent.getAction()==MotionEvent.ACTION_UP ||
                            motionEvent.getAction()==MotionEvent.ACTION_CANCEL) &&
                    motionEvent.getEventTime()-motionEvent.getDownTime()<mContext.getResources().getInteger(R.integer.double_tap_duration))
            {
                // Detect swipe event
                if(mevPreviousDown!=null &&
                        ( Math.abs(mevPreviousDown.getX(0)-motionEvent.getX(0))>10 ||
                                Math.abs(mevPreviousDown.getY(0)-motionEvent.getY(0))>10 ) &&
                        motionEvent.getEventTime()-mevPreviousDown.getEventTime()<mContext.getResources().getInteger(R.integer.jay_double_tap_duration))
                {
                    onFling(mevPreviousDown,motionEvent,0f,0f);
                }
            }
            mevPreviousUp = MotionEvent.obtain(motionEvent);
            return true;
        } else if( motionEvent.getPointerCount()==1 &&
                motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
            mevPreviousDown = MotionEvent.obtain(motionEvent);
            return true;
        }
        return false;
    }

    private Integer swipeDirection(MotionEvent downPress, MotionEvent upPress) {
        Integer intDirection = -1;
        Float fltMotionVertical = upPress.getY()-downPress.getY();
        Float fltMotionHorizontal = upPress.getX()-downPress.getX();
        /*
        * Directions: 0-7; -1 is undetermined
        * 3 2 1
        *  \|/
        * 4- -0
        *  /|\
        * 5 6 7
        * */
        Double dblDirection = Math.atan2( -fltMotionVertical, fltMotionHorizontal )*28/22+.5;
        intDirection = (int) Math.floor(dblDirection+8);
        intDirection = intDirection%8;  // or just use the negative numbers
        return intDirection;
    }

    private void defineClickEvents(View vwButton, Integer intMethod) {
        switch (intMethod) {
            case DIALOG_DISMISS:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDialog != null) mDialog.dismiss();
                    }
                });
                break;
            case OBJECT_TYPE_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopUpObjects popUpObject = new PopUpObjects(mContext,OBJECT_TYPE_KEY,SKEYE_APP);
                        popUpObject.setCancelable(true);
                        popUpObject.show();
                        popUpObject.setDialog(popUpObject);
                        popUpObject.setTitle(R.string.dialog_pop_up_object_title);
                        popUpObject.setMessage(R.string.dialog_pop_up_object_message);
                        popUpObject.setCancelClick(DIALOG_DISMISS);
                        // dismiss();
                    }
                });
                break;
            case CONSTELLATION_TYPE_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopUpObjects popUpConstellation = new PopUpObjects(mContext,CONSTELLATION_TYPE_KEY,SKEYE_APP);
                        popUpConstellation.setCancelable(true);
                        popUpConstellation.show();
                        popUpConstellation.setDialog(popUpConstellation);
                        popUpConstellation.setTitle(R.string.dialog_pop_up_constellations_title);
                        popUpConstellation.setMessage(R.string.dialog_pop_up_constellations_message);
                        popUpConstellation.setCancelClick(DIALOG_DISMISS);
                    }
                });
                break;
            case PLANET_TYPE_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopUpObjects popUpPlanet = new PopUpObjects(mContext,PLANET_TYPE_KEY,SKEYE_APP);
                        popUpPlanet.setCancelable(true);
                        popUpPlanet.show();
                        popUpPlanet.setDialog(popUpPlanet);
                        popUpPlanet.setTitle(R.string.dialog_pop_up_planet_title);
                        popUpPlanet.setMessage(R.string.dialog_pop_up_planet_message);
                        popUpPlanet.setCancelClick(DIALOG_DISMISS);
                    }
                });
                break;
            case AURORA_LAUNCH_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        safeLaunchAurora();
                        // dismiss(); // cannot dismiss if not installed
                    }
                });
                break;
            case METEOR_LAUNCH_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        safeLaunchMeteor();
                        // dismiss(); // cannot dismiss if not installed
                    }
                });
                break;
            case SATELLITE_LAUNCH_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        safeLaunchSatellite();
                        // dismiss(); // cannot dismiss if not installed

                    }
                });
                break;
            case VARIABLE_STARS_LAUNCH_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopUpObjects popUpObject = new PopUpObjects(mContext,OBJECT_TYPE_KEY,VARIABLE_STARS_APP);
                        popUpObject.setCancelable(true);
                        popUpObject.show();
                        popUpObject.setDialog(popUpObject);
                        popUpObject.setTitle(R.string.dialog_pop_up_object_title);
                        popUpObject.setMessage(R.string.dialog_pop_up_object_message);
                        popUpObject.setCancelClick(DIALOG_DISMISS);
                    }
                });
                break;
            case MOON_LAUNCH_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                break;
            case PLANET_POP_UP_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                break;
            case CONJUNCTION_POP_UP_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                break;
            case ECLIPSE_LAUNCH_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        Float fltLat = sharedPreferences.getFloat(COORDINATES_LAT_KEY,0f),
                                fltLng = sharedPreferences.getFloat(COORDINATES_LON_KEY,0f);
                        try {
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    String.format(
                                            Locale.US,
                                            "https://www.timeanddate.com/eclipse/in/@%f,%f",
                                            fltLat,
                                            fltLng
                                    )
                            )));
                        } catch (android.content.ActivityNotFoundException errAnfe) {
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    String.format(
                                            Locale.US,
                                            "https://www.google.com/search?q=eclipse+visible+from+%fN+%fE",
                                            fltLat,
                                            fltLng
                                    )
                            )));
                        }
                        dismiss();
                    }
                });
                break;
            case LOCATION_EDIT_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // edit the selected profile
                        mDialog.show();
                        dismiss();
                    }
                });
                break;
            case LOCATION_DELETE_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialog dialog = new AlertDialog.Builder(mContext)
                                .setTitle(R.string.dialog_locations_delete_confirm_title)
                                .setMessage(R.string.dialog_locations_delete_confirm_message)
                                .setPositiveButton(R.string.dialog_locations_delete_confirm_positive, new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // delete the selected profile
                                        Set<String> stringSet = mapMessages.keySet();
                                        Object[] strsKeys = stringSet.toArray();
                                        String strKey = (String) strsKeys[mCurrentView];
                                        String[] strsView = mapMessages.get(strKey).split(",");
                                        AstroDatabase astroDatabase = new AstroDatabase(mContext,strsView[LOCATION_ARRAY_OBS_NAME].replaceAll("%2C",","));
                                        astroDatabase.deleteObservatory();
                                        astroDatabase.astroDBclose();
                                        if(((Activity)mContext).getTitle().equals(strsView[LOCATION_ARRAY_OBS_NAME].replaceAll("%2C",","))) {
                                            ((Activity) mContext).setTitle(mContext.getString(R.string.pref_default_location_name));
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.dialog_locations_delete_confirm_negative, new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismiss();
                                    }
                                })
                                .create();
                        dialog.show();
                        dismiss();
                    }
                });
                break;
            case LOCATION_GOTO_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // make the selected profile the current profile
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Set<String> stringSet = mapMessages.keySet();
                        Object[] strsKeys = stringSet.toArray();
                        String strKey = (String) strsKeys[mCurrentView];
                        String[] strsView = mapMessages.get(strKey).split(",");
                        Float fltLat,fltLng,fltAlt;
                        String strCoordinates = strsView[LOCATION_ARRAY_OBS_COORDINATES];
                        fltAlt = Float.valueOf(strsView[LOCATION_ARRAY_OBS_ALTITUDE]);
                        editor.putString(OBSERVATORY_KEY,strsView[LOCATION_ARRAY_OBS_NAME].replaceAll("%2C",","));
                        editor.putString(COORDINATES_KEY,strCoordinates);
                        editor.putFloat(COORDINATES_ALT_KEY, fltAlt);
                        if(strCoordinates.matches("-?[0-9.]+[NS] -?[0-9.]+[EW]")) {
                            fltLat = (strCoordinates.contains("S")?-1f:1f)*
                                    Float.valueOf(strCoordinates.substring(0,
                                            strCoordinates.indexOf(strCoordinates.contains("S")?"S":"N")));
                            fltLng = (strCoordinates.contains("W")?-1f:1f)*
                                    Float.valueOf(strCoordinates.substring(strCoordinates.indexOf(" ")+1,
                                            strCoordinates.indexOf(strCoordinates.contains("E")?"E":"W")));
                            editor.putFloat(COORDINATES_LAT_KEY, fltLat);
                            editor.putFloat(COORDINATES_LON_KEY, fltLng);
                        }
                        editor.apply();
                        ((Activity)mContext).setTitle(strsView[LOCATION_ARRAY_OBS_NAME].replaceAll("%2C",","));
                        dismiss();
                        // Offer to download the updated data
                        Dialog dialog = new android.app.AlertDialog.Builder(mContext)
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
                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                                if(sharedPreferences!=null) {
                                                    Intent intent = new Intent(mContext,FileService.class);
                                                    strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, mContext.getString(R.string.pref_default_location_name));
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
                                                    mContext.startService(intent);
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
                });
                break;
            default:
                break;
        }
    }

    // The following two helper functions will help launch SkEye safely
    private void safeLaunchSkEye(String objPath) {
        final Intent skEyeIntent = new Intent(Intent.ACTION_SEARCH);
        final Uri targetUri = new Uri.Builder().path(objPath).build();
        skEyeIntent.setDataAndType(targetUri, "text/astro_object");

        safeLaunchActivity(skEyeIntent, "SkEye v1.2 or higher", SKEYE_APP);
    }

    private void safeLaunchSkEyeForViewing(final double ra, final double dec) {
        final Intent skEyeIntent = new Intent(Intent.ACTION_VIEW);
        skEyeIntent.setType("text/astro_position");
        skEyeIntent.putExtra("RA", Math.toRadians(ra*15)); // where ra is in hours
        skEyeIntent.putExtra("Declination", Math.toRadians(dec)); // where dec is in degrees

        safeLaunchActivity(skEyeIntent, "SkEye v5.4 or higher", SKEYE_APP);
    }

    private void safeLaunchAurora() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean(AURORA_WARNING_KEY, true)) {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setMessage(R.string.dialog_pop_up_other_app_disclaimer)
                    .setCancelable(true).setPositiveButton(R.string.dialog_pop_up_other_app_positive, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent auroraIntent = mContext.getPackageManager().getLaunchIntentForPackage(AURORA_APP);
                            safeLaunchActivity(auroraIntent,mContext.getString(R.string.dialog_pop_up_aurora_hint), AURORA_APP);
                        }
                    }).setNegativeButton(R.string.dialog_pop_up_other_app_negative, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }).setNeutralButton(R.string.dialog_pop_up_other_app_neutral, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(AURORA_WARNING_KEY,false);
                            editor.apply();
                            final Intent auroraIntent = mContext.getPackageManager().getLaunchIntentForPackage(AURORA_APP);
                            safeLaunchActivity(auroraIntent,mContext.getString(R.string.dialog_pop_up_aurora_hint), AURORA_APP);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            final Intent auroraIntent = mContext.getPackageManager().getLaunchIntentForPackage(AURORA_APP);
            safeLaunchActivity(auroraIntent,mContext.getString(R.string.dialog_pop_up_aurora_hint), AURORA_APP);
        }
    }

    private void safeLaunchMeteor() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean(METEOR_WARNING_KEY, true)) {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setMessage(R.string.dialog_pop_up_other_app_disclaimer)
                    .setCancelable(true).setPositiveButton(R.string.dialog_pop_up_other_app_positive, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent meteorIntent = mContext.getPackageManager().getLaunchIntentForPackage(METEOR_APP);
                            safeLaunchActivity(meteorIntent, mContext.getString(R.string.dialog_pop_up_meteor_hint), METEOR_APP);
                        }
                    }).setNegativeButton(R.string.dialog_pop_up_other_app_negative, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }).setNeutralButton(R.string.dialog_pop_up_other_app_neutral, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(METEOR_WARNING_KEY, false);
                            editor.apply();
                            final Intent meteorIntent = mContext.getPackageManager().getLaunchIntentForPackage(METEOR_APP);
                            safeLaunchActivity(meteorIntent, mContext.getString(R.string.dialog_pop_up_meteor_hint), METEOR_APP);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            final Intent meteorIntent = mContext.getPackageManager().getLaunchIntentForPackage(METEOR_APP);
            safeLaunchActivity(meteorIntent, mContext.getString(R.string.dialog_pop_up_meteor_hint), METEOR_APP);
        }
    }

    private void safeLaunchSatellite() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean(SATELLITE_WARNING_KEY, true)) {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setMessage(R.string.dialog_pop_up_other_app_disclaimer)
                    .setCancelable(true).setPositiveButton(R.string.dialog_pop_up_other_app_positive, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent satelliteIntent = mContext.getPackageManager().getLaunchIntentForPackage(SATELLITE_APP);
                            safeLaunchActivity(satelliteIntent, mContext.getString(R.string.dialog_pop_up_satellite_hint), SATELLITE_APP);
                        }
                    }).setNegativeButton(R.string.dialog_pop_up_other_app_negative, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }).setNeutralButton(R.string.dialog_pop_up_other_app_neutral, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(SATELLITE_WARNING_KEY, false);
                            editor.apply();
                            final Intent satelliteIntent = mContext.getPackageManager().getLaunchIntentForPackage(SATELLITE_APP);
                            safeLaunchActivity(satelliteIntent, mContext.getString(R.string.dialog_pop_up_satellite_hint), SATELLITE_APP);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            final Intent satelliteIntent = mContext.getPackageManager().getLaunchIntentForPackage(SATELLITE_APP);
            safeLaunchActivity(satelliteIntent, mContext.getString(R.string.dialog_pop_up_satellite_hint), SATELLITE_APP);
        }
    }

    private void safeLaunchVariableStars() {
        final Intent variableStarsIntent = mContext.getPackageManager().getLaunchIntentForPackage(VARIABLE_STARS_APP);
        safeLaunchActivity(variableStarsIntent,mContext.getString(R.string.dialog_pop_up_app_variable_stars_hint), VARIABLE_STARS_APP);
    }

    private void safeLaunchActivity (Intent i, String hint, final String appPackageName) {
        if (i!=null && i.resolveActivity(mContext.getPackageManager())!= null) {
            mContext.startActivity(i);
        } else {
            // Toast.makeText(mContext, "Please install "+hint, Toast.LENGTH_SHORT).show();
            Snackbar.make(this.findViewById(R.id.button_cancel),mContext.getString(R.string.safe_launch_not_installed)+hint,Snackbar.LENGTH_LONG)
                    .setActionTextColor(mContext.getResources().getColor(R.color.foregroundSkyAntares))
                    .setAction("Install now", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // From: https://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
                            try {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe){
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + appPackageName)));
                            }
                        }
                    }).show();
        }
    }
}


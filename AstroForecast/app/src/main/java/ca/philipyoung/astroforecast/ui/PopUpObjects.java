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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import ca.philipyoung.astroforecast.R;
import ca.philipyoung.astroforecast.util.AstroDatabase;

/**
 * Created by Philip Young on 2018-07-27.
 * Display a list of radio buttons, each representing a different object in the sky.
 * If the list is long, make it scroll.
 */

public class PopUpObjects extends Dialog implements View.OnClickListener  {
    private Context mContext;
    private Activity mActivity;
    private Dialog mDialog=null;
    private Integer mChartType=-1;
    private String mExternalApp = null;
    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final Integer PLANET_TYPE_KEY = 4;
    private static final Integer OBJECT_TYPE_KEY = 2;
    private static final Integer CONSTELLATION_TYPE_KEY = 3;
    private static final Integer CONJUNCTION_TYPE_KEY = 5;
    private static final Integer SATELLITE_TYPE_KEY = 6;
    private static final Integer COMET_TYPE_KEY = 7;
    private static final Integer METEOR_TYPE_KEY = 8;
    private static final Integer ECLIPSE_TYPE_KEY = 9;
    private static final Integer LOCATION_TYPE_KEY = 10;
    private static final int SATELLITE_LAUNCH_KEY = 104;
    private static final int PLANET_POP_UP_KEY = 121;
    private static final int CONJUNCTION_POP_UP_KEY = 122;
    private static final int SATELLITE_ISS_POP_UP_KEY = 123;
    private static final int SATELLITE_IRIDIUM_POP_UP_KEY = 124;
    private static final int SATELLITE_POP_UP_KEY = 125;
    private static final int COMET_POP_UP_KEY = 126;
    private static final String SKEYE_WARNING_KEY = "skeye_warning";
    private static final String VARIABLE_STARS_WARNING_KEY = "variable_stars_warning";
    private static final String SATELLITE_WARNING_KEY = "satellite_warning";
    private static final int GENERIC_REF_KEY = R.array.star_finder_objects;
    private static final int OBJECT_RA_KEY = R.array.star_finder_object_ras;
    private static final int OBJECT_DECL_KEY = R.array.star_finder_object_decs;
    private static final String SKEYE_APP = "com.lavadip.skeye";
    private static final String VARIABLE_STARS_APP = "com.astroapps.satrid.variablestars";
    private static final String SATELLITE_APP = "com.heavens_above.viewer";
    private static final int DIALOG_DISMISS = 1;

    /**
     * Creates a dialog window that uses the default dialog theme.
     * <p>
     * The supplied {@code context} is used to obtain the window manager and
     * base theme used to present the dialog.
     *
     * @param context the context in which the dialog should run
     */
    public PopUpObjects(@NonNull Context context, @NonNull Integer intChartKey){
        super(context);
        this.mContext=context;
        this.mActivity=(Activity)context;
        this.mChartType=intChartKey;
        this.mExternalApp=SKEYE_APP;
    }
    public PopUpObjects(@NonNull Context context, @NonNull Integer intChartKey, @NonNull String strExternalApp) {
        super(context);
        this.mContext=context;
        this.mActivity=(Activity)context;
        this.mChartType=intChartKey;
        this.mExternalApp=strExternalApp;
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
        String[] strsPlanets = mContext.getResources().getStringArray(R.array.star_finder_planets);
        String[] strsLinks = mContext.getResources().getStringArray(R.array.star_finder_planet_searches);
        String[] strsObjects = mContext.getResources().getStringArray(R.array.star_finder_objects);
        String[] strsObjectRA = mContext.getResources().getStringArray(R.array.star_finder_object_ras);
        String[] strsObjectDec = mContext.getResources().getStringArray(R.array.star_finder_object_decs);
        if(mChartType==CONSTELLATION_TYPE_KEY) {
            strsObjects = mContext.getResources().getStringArray(R.array.star_finder_constellations);
            strsObjectRA = mContext.getResources().getStringArray(R.array.star_finder_constellation_ras);
            strsObjectDec = mContext.getResources().getStringArray(R.array.star_finder_constellation_decs);
        } else if(mChartType==CONJUNCTION_TYPE_KEY) {
            strsObjects = new String[]{view.getTag(GENERIC_REF_KEY).toString()};
            strsObjectRA = new String[]{view.getTag(OBJECT_RA_KEY).toString()};
            strsObjectDec = new String[]{view.getTag(OBJECT_DECL_KEY).toString()};
        } else if(mChartType==COMET_TYPE_KEY) {
            strsObjects = new String[]{view.getTag(GENERIC_REF_KEY).toString()};
            strsObjectRA = new String[]{view.getTag(OBJECT_RA_KEY).toString()};
            strsObjectDec = new String[]{view.getTag(OBJECT_DECL_KEY).toString()};
        }

        if(view instanceof RadioButton) {
            for (int intI = 0; intI < Math.max(strsObjects.length,strsPlanets.length); intI++) {
                if(mExternalApp.equals(SKEYE_APP)) {
                    if (intI < strsPlanets.length && view.getTag(GENERIC_REF_KEY).equals(strsPlanets[intI])) {
                        safeLaunchSkEye("astro_object//" + (intI < 9 ? "solarsys/" : "star/") + strsLinks[intI]);
                        break;
                    } else if (intI < strsObjects.length && view.getTag(GENERIC_REF_KEY).equals(strsObjects[intI])) {
                        safeLaunchSkEyeForViewing(Double.valueOf(strsObjectRA[intI]), Double.valueOf(strsObjectDec[intI]));
                        break;
                    }
                } else if(mExternalApp.equals(VARIABLE_STARS_APP)) {
                    safeLaunchVariableStars();
                    break;
                } else if(mExternalApp.equals(SATELLITE_APP)) {
                    safeLaunchSatellite();
                    break;
                }
            }
        }
    }

    /**
     * Called when the dialog is starting.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Float fltTextSize = 18.0f; //mContext.getResources().getFraction(R.fraction.pop_up_dialog_message_text_size,);// ((TextView)findViewById(R.id.text_message)).getTextSize();
        String[] strsPlanets = mContext.getResources().getStringArray(R.array.star_finder_planets);
        String[] strsObjects = mContext.getResources().getStringArray(R.array.star_finder_objects);
        String[] strsConjunctions = new String[0];
        String[] strsSatellites = new String[0];
        String[] strsComets = new String[0];
        if(mChartType==CONSTELLATION_TYPE_KEY) {
            strsObjects = mContext.getResources().getStringArray(R.array.star_finder_constellations);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        RadioGroup radioGroupPlanets = findViewById(R.id.planet_radio_group);
        RadioButton radioButtonPlanet;
        if(mChartType==PLANET_TYPE_KEY) {
            for (String strPlanet : strsPlanets) {
                radioButtonPlanet = new RadioButton(mContext);
                radioButtonPlanet.setText(strPlanet);
                radioButtonPlanet.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                radioButtonPlanet.setTextSize(fltTextSize);
                radioButtonPlanet.setTag(GENERIC_REF_KEY,strPlanet);
                radioButtonPlanet.setOnClickListener(this);
                radioGroupPlanets.addView(radioButtonPlanet, layoutParams);
            }
        } else if(mChartType==OBJECT_TYPE_KEY) {
            for (String strObject : strsObjects) {
                radioButtonPlanet = new RadioButton(mContext);
                radioButtonPlanet.setText(strObject);
                radioButtonPlanet.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                radioButtonPlanet.setTextSize(fltTextSize);
                radioButtonPlanet.setTag(GENERIC_REF_KEY,strObject);
                radioButtonPlanet.setOnClickListener(this);
                radioGroupPlanets.addView(radioButtonPlanet, layoutParams);

            }
        } else if(mChartType==CONSTELLATION_TYPE_KEY) {
            for (String strConstellation : strsObjects) {
                radioButtonPlanet = new RadioButton(mContext);
                radioButtonPlanet.setText(strConstellation);
                radioButtonPlanet.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                radioButtonPlanet.setTextSize(fltTextSize);
                radioButtonPlanet.setTag(GENERIC_REF_KEY,strConstellation);
                radioButtonPlanet.setOnClickListener(this);
                radioGroupPlanets.addView(radioButtonPlanet, layoutParams);

            }
        } else if(mChartType==CONJUNCTION_TYPE_KEY) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, null);
            AstroDatabase astroDatabase = new AstroDatabase(mContext, strObservatory);
            strsConjunctions = astroDatabase.getConjunctionsStrings(); // get the CSV list of conjunctions
            astroDatabase.astroDBclose();
            for (String strConjunction : strsConjunctions) {
                String[] strings = strConjunction.split(",");
                if(strings.length==5) {
                    radioButtonPlanet = new RadioButton(mContext);
                    radioButtonPlanet.setText(strings[0]);
                    radioButtonPlanet.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                    radioButtonPlanet.setTextSize(fltTextSize);
                    radioButtonPlanet.setTag(OBJECT_RA_KEY,strings[3]);
                    radioButtonPlanet.setTag(OBJECT_DECL_KEY,strings[4]);
                    radioButtonPlanet.setTag(GENERIC_REF_KEY,strings[0]);
                    radioButtonPlanet.setOnClickListener(this);
                    radioGroupPlanets.addView(radioButtonPlanet, layoutParams);
                }

            }
        } else if(mChartType==SATELLITE_TYPE_KEY) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, null);
            AstroDatabase astroDatabase = new AstroDatabase(mContext, strObservatory);
            strsSatellites = astroDatabase.getTonightsSatellites(); // get the CSV list of satellites
            astroDatabase.astroDBclose();
            for (String strSatellite : strsSatellites) {
                radioButtonPlanet = new RadioButton(mContext);
                radioButtonPlanet.setText(strSatellite);
                radioButtonPlanet.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                radioButtonPlanet.setTextSize(fltTextSize);
                radioButtonPlanet.setTag(GENERIC_REF_KEY,strSatellite);
                radioButtonPlanet.setOnClickListener(this);
                radioGroupPlanets.addView(radioButtonPlanet,layoutParams);
            }
        } else if(mChartType==COMET_TYPE_KEY) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String strObservatory = sharedPreferences.getString(OBSERVATORY_KEY, null);
            AstroDatabase astroDatabase = new AstroDatabase(mContext, strObservatory);
            strsComets = astroDatabase.getCometsStrings(); // get the CSV list of comets
            astroDatabase.astroDBclose();
            for (String strComet : strsComets) {
                String[] strings = strComet.split(",");
                if(strings.length==4) {
                    radioButtonPlanet = new RadioButton(mContext);
                    radioButtonPlanet.setText(strings[0]+" : Mag "+strings[3]);
                    radioButtonPlanet.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                    radioButtonPlanet.setTextSize(fltTextSize);
                    radioButtonPlanet.setTag(OBJECT_RA_KEY,strings[1]);
                    radioButtonPlanet.setTag(OBJECT_DECL_KEY,strings[2]);
                    radioButtonPlanet.setTag(GENERIC_REF_KEY,strings[0]);
                    radioButtonPlanet.setOnClickListener(this);
                    radioGroupPlanets.addView(radioButtonPlanet, layoutParams);
                }

            }
        }
        radioGroupPlanets.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resizeDialogScroll();
            }
        }, 50);
    }

    public void resizeDialogScroll() {
        View vwScroll = findViewById(R.id.planet_radio_scroll);
        if(vwScroll instanceof ScrollView) {
            int intScrollHeight = mContext.getResources().getInteger(R.integer.pop_up_scroll_height);
            if(intScrollHeight <= vwScroll.getHeight()) {
                // ((ScrollView)vwScroll).setHeight(intScrollHeight);
                ViewGroup.LayoutParams layoutParams = vwScroll.getLayoutParams();
                layoutParams.height = intScrollHeight;
                vwScroll.setLayoutParams(layoutParams);
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

    private void defineClickEvents(View vwButton, Integer intMethod) {
        switch (intMethod) {
            case PLANET_POP_UP_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strTitle = mContext.getString(R.string.dialog_pop_up_conjunctions_title);
                        String strMessage = mContext.getString(R.string.dialog_pop_up_conjunctions_message);
                        final PopUpObjects popUp = new PopUpObjects(mContext,CONJUNCTION_TYPE_KEY);
                        popUp.setCancelable(true);
                        popUp.show();
                        popUp.setDialog(popUp);
                        popUp.setTitle(strTitle);
                        popUp.setMessage(strMessage);
                        popUp.setPositiveButtonText(R.string.dialog_pop_up_conjunctions_positive);
                        popUp.setPositiveClick(CONJUNCTION_POP_UP_KEY);
                        popUp.setCancelClick(DIALOG_DISMISS);
                        if (mDialog != null) mDialog.dismiss();
                    }
                });
                break;
            case CONJUNCTION_POP_UP_KEY:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strTitle = mContext.getString(R.string.dialog_pop_up_planet_title);
                        String strMessage = mContext.getString(R.string.dialog_pop_up_planet_message);
                        final PopUpObjects popUp = new PopUpObjects(mContext,PLANET_TYPE_KEY);
                        popUp.setCancelable(true);
                        popUp.show();
                        popUp.setDialog(popUp);
                        popUp.setTitle(strTitle);
                        popUp.setMessage(strMessage);
                        popUp.setPositiveButtonText(R.string.dialog_pop_up_planet_positive);
                        popUp.setPositiveClick(PLANET_POP_UP_KEY);
                        popUp.setCancelClick(DIALOG_DISMISS);
                        if (mDialog != null) mDialog.dismiss();
                    }
                });
                break;
            case DIALOG_DISMISS:
                vwButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDialog != null) mDialog.dismiss();
                    }
                });
                break;
            default:
                break;
        }
    }

    // The following two helper functions will help launch SkEye safely
    private void safeLaunchSkEye(final String objPath) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean(SKEYE_WARNING_KEY, true)) {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setMessage(R.string.dialog_pop_up_other_app_disclaimer)
                    .setCancelable(true).setPositiveButton(R.string.dialog_pop_up_other_app_positive, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent skEyeIntent = new Intent(Intent.ACTION_SEARCH);
                            final Uri targetUri = new Uri.Builder().path(objPath).build();
                            skEyeIntent.setDataAndType(targetUri, "text/astro_object");

                            safeLaunchActivity(skEyeIntent, "SkEye v1.2 or higher", SKEYE_APP);
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
                            editor.putBoolean(SKEYE_WARNING_KEY,false);
                            editor.apply();
                            final Intent skEyeIntent = new Intent(Intent.ACTION_SEARCH);
                            final Uri targetUri = new Uri.Builder().path(objPath).build();
                            skEyeIntent.setDataAndType(targetUri, "text/astro_object");

                            safeLaunchActivity(skEyeIntent, "SkEye v1.2 or higher", SKEYE_APP);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            final Intent skEyeIntent = new Intent(Intent.ACTION_SEARCH);
            final Uri targetUri = new Uri.Builder().path(objPath).build();
            skEyeIntent.setDataAndType(targetUri, "text/astro_object");

            safeLaunchActivity(skEyeIntent, "SkEye v1.2 or higher", SKEYE_APP);
        }
    }

    private void safeLaunchSkEyeForViewing(final double ra, final double dec) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean(SKEYE_WARNING_KEY, true)) {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setMessage(R.string.dialog_pop_up_other_app_disclaimer)
                    .setCancelable(true).setPositiveButton(R.string.dialog_pop_up_other_app_positive, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent skEyeIntent = new Intent(Intent.ACTION_VIEW);
                            skEyeIntent.setType("text/astro_position");
                            skEyeIntent.putExtra("RA", Math.toRadians(ra * 15.0)); // where ra is in hours
                            skEyeIntent.putExtra("Declination", Math.toRadians(dec)); // where dec is in degrees

                            safeLaunchActivity(skEyeIntent, "SkEye v5.4 or higher", SKEYE_APP);
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
                            editor.putBoolean(SKEYE_WARNING_KEY,false);
                            editor.apply();
                            final Intent skEyeIntent = new Intent(Intent.ACTION_VIEW);
                            skEyeIntent.setType("text/astro_position");
                            skEyeIntent.putExtra("RA", Math.toRadians(ra * 15.0)); // where ra is in hours
                            skEyeIntent.putExtra("Declination", Math.toRadians(dec)); // where dec is in degrees

                            safeLaunchActivity(skEyeIntent, "SkEye v5.4 or higher", SKEYE_APP);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            final Intent skEyeIntent = new Intent(Intent.ACTION_VIEW);
            skEyeIntent.setType("text/astro_position");
            skEyeIntent.putExtra("RA", Math.toRadians(ra * 15.0)); // where ra is in hours
            skEyeIntent.putExtra("Declination", Math.toRadians(dec)); // where dec is in degrees

            safeLaunchActivity(skEyeIntent, "SkEye v5.4 or higher", SKEYE_APP);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean(VARIABLE_STARS_WARNING_KEY, true)) {
            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setMessage(R.string.dialog_pop_up_other_app_disclaimer)
                    .setCancelable(true).setPositiveButton(R.string.dialog_pop_up_other_app_positive, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent variableStarsIntent = mContext.getPackageManager().getLaunchIntentForPackage(VARIABLE_STARS_APP);
                            safeLaunchActivity(variableStarsIntent,mContext.getString(R.string.dialog_pop_up_app_variable_stars_hint), VARIABLE_STARS_APP);
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
                            editor.putBoolean(VARIABLE_STARS_WARNING_KEY,false);
                            editor.apply();
                            final Intent variableStarsIntent = mContext.getPackageManager().getLaunchIntentForPackage(VARIABLE_STARS_APP);
                            safeLaunchActivity(variableStarsIntent,mContext.getString(R.string.dialog_pop_up_app_variable_stars_hint), VARIABLE_STARS_APP);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            final Intent variableStarsIntent = mContext.getPackageManager().getLaunchIntentForPackage(VARIABLE_STARS_APP);
            safeLaunchActivity(variableStarsIntent,mContext.getString(R.string.dialog_pop_up_app_variable_stars_hint), VARIABLE_STARS_APP);
        }
    }

    private void safeLaunchActivity (Intent i, String hint, final String appPackageName) {
        if (i!=null && i.resolveActivity(mContext.getPackageManager())!= null) {
            mContext.startActivity(i);
        } else {
            // Toast.makeText(mContext, "Please install "+hint, Toast.LENGTH_SHORT).show();
            Snackbar.make(this.findViewById(R.id.button_cancel),"Please install "+hint,Snackbar.LENGTH_LONG)
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

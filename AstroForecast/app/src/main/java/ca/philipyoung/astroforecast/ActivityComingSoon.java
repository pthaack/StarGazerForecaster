package ca.philipyoung.astroforecast;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Inflater;

import ca.philipyoung.astroforecast.util.AstroDatabase;

public class ActivityComingSoon extends AppCompatActivity {

    private static final String TAG = "ActivityComingSoon";
    Context mContext;
    private static final int CALENDAR_DATE_KEY = R.string.calendar_default_message;
    private static final String OBSERVATORY_KEY = "observatory_text";

    private static final Long ONE_HOUR = 3600000L; // the number of milliseconds in an hour.
    private static final Long ONE_FULL_DAY = 86400000L; // the number of milliseconds in one day.
    private static final Long ONE_HALF_DAY = 43200000L; // the number of milliseconds in half a day.
    private static final Long ONE_FULL_WEEK = 604800000L; // the number of milliseconds in one week.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon);
        mContext = this;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Display calendar for current month
        Date dteNow = new Date();
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(dteNow);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayCalendar();
            }
        },50L);
    }

    /*
    * Display calendar for selected month and highlight day
    * Display up to 6 weeks including the lead up days and trail off days
    *   If February has 28 days and starts on Sunday, show only 4 weeks
    *   If September starts on Saturday, show August 26-31 and October 1-6
    * Let the calendar begin on Monday, but show 7 days and default to Sunday
    * */

    /*
    * Build the cell view for the given day
    *   Each cell has the date and up to three events
    *       ISS Pass
    *       Eclipse or Moon Phase
    *       Prominent Meteor Shower begins, peaks, or ends
    *       Variable star maximum/minimum, or becomes visible, and is above the horizon at night
    *       Comet reaches best visibility
    *       Not Aurora because its meteorology is too unpredictable
    * Inflate two linear views with horizontal orientations
    * Inflate a text view with a date and an image
    * Inflate two images
    * */
    private void displayCalendar() {
        displayCalendar(new Date());
    }
    private void displayCalendar(final Date dteCalendar) {
        Integer[] intsList = new Integer[] {
                R.id.w1d1, R.id.w1d2, R.id.w1d3, R.id.w1d4, R.id.w1d5, R.id.w1d6, R.id.w1d7,
                R.id.w2d1, R.id.w2d2, R.id.w2d3, R.id.w2d4, R.id.w2d5, R.id.w2d6, R.id.w2d7,
                R.id.w3d1, R.id.w3d2, R.id.w3d3, R.id.w3d4, R.id.w3d5, R.id.w3d6, R.id.w3d7,
                R.id.w4d1, R.id.w4d2, R.id.w4d3, R.id.w4d4, R.id.w4d5, R.id.w4d6, R.id.w4d7,
                R.id.w5d1, R.id.w5d2, R.id.w5d3, R.id.w5d4, R.id.w5d5, R.id.w5d6, R.id.w5d7,
                R.id.w6d1, R.id.w6d2, R.id.w6d3, R.id.w6d4, R.id.w6d5, R.id.w6d6, R.id.w6d7
        };
        View vwDateCell;
        /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LayoutInflater inflater = LayoutInflater.from(mContext);
        Integer intRow=1, intColumn=1;
        Date dteNow = new Date();*/
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteCalendar);
        // go to the first day of the month
        calendar.add(Calendar.DATE, 1-calendar.get(Calendar.DAY_OF_MONTH));
        // show at least one day of the previous month
        calendar.add(Calendar.DATE, 1-(calendar.get(Calendar.DAY_OF_WEEK)==1?
                8:
                calendar.get(Calendar.DAY_OF_WEEK)));
        TextView vwText = findViewById(R.id.textCalendarMessage);
        if(vwText != null) {
            vwText.setText(CalendarScreenMessage(dteCalendar));
        }

        // Default layout parameters:
        // android:orientation="vertical"
        // android:layout_gravity="center_horizontal"
        // layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        for (Integer idDateCell : intsList) {
            vwDateCell = findViewById(idDateCell);
            if (vwDateCell != null && vwDateCell instanceof LinearLayout) {
                // vwDateCell.setLayoutParams(layoutParams);
                /*
                ((LinearLayout)vwDateCell).setOrientation(LinearLayout.VERTICAL);
                LinearLayout linearLayout1 = new LinearLayout(mContext),
                        linearLayout2 = new LinearLayout(mContext);
                linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                TextView txDate;
                txDate = new TextView(mContext) ;
                txDate.setText(String.format(Locale.US,"%te",calendar.getTime()));
                vwDateCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View vwText = findViewById(R.id.textCalendarMessage);
                        if(vwText!=null && vwText instanceof TextView)
                        ((TextView) vwText).setText(String.format(Locale.US,getString(R.string.calendar_default_message),calendar.getTime()));
                    }
                });
                if(!String.format(Locale.US,"%1$tY%1$tm",calendar.getTime())
                        .equals(String.format(Locale.US,"%1$tY%1$tm",dteCalendar))) {
                    txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyAntares));
                    if(calendar.getTime().before(dteCalendar)) {
                        vwDateCell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                calendar.add(Calendar.MONTH,-1);
                                displayCalendar(calendar.getTime());
                            }
                        });
                    } else {
                        vwDateCell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                calendar.add(Calendar.MONTH, 1);
                                displayCalendar(calendar.getTime());
                            }
                        });
                    }
                } else if(String.format(Locale.US,"%1$tY%1$tm%1$td",calendar.getTime())
                        .equals(String.format(Locale.US,"%1$tY%1$tm%1$td",dteNow))) {
                    txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkySun));
                } else {
                    txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
                    // txDate.setStyle(TextView.BOLD);
                }
                txDate.setGravity(Gravity.END);
                ImageView imageView1 = new ImageView(mContext);
                imageView1.setVisibility(View.INVISIBLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView1.setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
                } else {
                    imageView1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
                }
                ImageView imageView2 = new ImageView(mContext);
                imageView2.setVisibility(View.INVISIBLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView2.setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
                } else {
                    imageView2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
                }
                ImageView imageView3 = new ImageView(mContext);
                imageView3.setVisibility(View.INVISIBLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView3.setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
                } else {
                    imageView3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
                }
                linearLayout1.addView(txDate);
                linearLayout1.addView(imageView1);
                ((LinearLayout) vwDateCell).removeAllViews();
                ((LinearLayout) vwDateCell).addView(linearLayout1);

                // add line two
                linearLayout2.addView(imageView2);
                linearLayout2.addView(imageView3);
                ((LinearLayout) vwDateCell).addView(linearLayout2);
                */
                /*
                // LinearLayout linearLayout = new LinearLayout(mContext);
                // ((LinearLayout) vwDateCell).addView(displayCell(linearLayout,calendar,dteCalendar));
                // ((LinearLayout)vwDateCell).infl
                // layoutParams = vwDateCell.getLayoutParams();
                */
                CalendarDateItem calendarDateItem = new CalendarDateItem(calendar.getTime());
                displayCell((LinearLayout) vwDateCell,calendarDateItem,dteCalendar);
            }
            /*if(++intColumn>7) {
                intColumn=1;
                intRow++;
            }*/
            calendar.add(Calendar.DATE,1);
        }
        calendar.setTime(dteCalendar);
        View vwMonth = findViewById(R.id.textCalendarMonth);
        if(vwMonth!=null && vwMonth instanceof TextView) {
            ((TextView) vwMonth).setText(String.format(Locale.US, "%1$tB %1$tY", dteCalendar));
        }
        View vwButton = findViewById(R.id.imgNextMonth);
        if(vwButton!=null) vwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH,1);
                displayCalendar(calendar.getTime());
            }
        });
        vwButton = findViewById(R.id.imgPreviousMonth);
        if(vwButton!=null) vwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH,-1);
                displayCalendar(calendar.getTime());
            }
        });
    }

    private void displayCell(ViewGroup viewGroup, final CalendarDateItem calDateItem, Date dteMonth) {
        /*
        @LayoutRes int mResource = R.layout.fragment_calendar_cell;
        TextView txDate;
        ImageView imvEvent1,imvEvent2,imvEvent3;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View convertView = inflater.inflate(mResource, viewGroup, false);
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(dteMonth);
        txDate = (TextView) convertView.findViewById(R.id.textDate);
        imvEvent1 = (ImageView) convertView.findViewById(R.id.imageOne);
        imvEvent2 = (ImageView) convertView.findViewById(R.id.imageTwo);
        imvEvent3 = (ImageView) convertView.findViewById(R.id.imageThree);

        txDate.setText(String.format(Locale.US,"%td",calDate.getTime()));
        if(calDate.get(Calendar.MONTH)!=calNow.get(Calendar.MONTH)) {
            txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyAntares));
        } else if(calDate.get(Calendar.YEAR)==calNow.get(Calendar.YEAR) &&
                calDate.get(Calendar.MONTH)==calNow.get(Calendar.MONTH) &&
                calDate.get(Calendar.DAY_OF_MONTH)==calNow.get(Calendar.DAY_OF_MONTH)) {
            txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkySun));
        }
        imvEvent1.setVisibility(View.INVISIBLE);
        imvEvent2.setVisibility(View.INVISIBLE);
        imvEvent3.setVisibility(View.INVISIBLE);
        return viewGroup;
        */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        ((LinearLayout)viewGroup).setOrientation(LinearLayout.VERTICAL);
        LinearLayout linearLayout1 = new LinearLayout(mContext),
                linearLayout2 = new LinearLayout(mContext);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        TextView txDate;
        Calendar calStart = Calendar.getInstance(),
                calEnd = Calendar.getInstance();
        calStart.setTime(calDateItem.getDateItem());
        calStart.set(calStart.get(Calendar.YEAR),calStart.get(Calendar.MONTH),calStart.get(Calendar.DAY_OF_MONTH),0,0,0);
        calEnd.set(calStart.get(Calendar.YEAR),calStart.get(Calendar.MONTH),calStart.get(Calendar.DAY_OF_MONTH)+1,0,0,0);
        AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
        ArrayList<String> strsEvents = astroDatabase.getEvents(calStart.getTime(),calEnd.getTime());
        astroDatabase.astroDBclose();
        txDate = new TextView(mContext) ;
        txDate.setText(String.format(Locale.US,"%te",calDateItem.getDateItem()));
        viewGroup.setTag(CALENDAR_DATE_KEY,calDateItem.getDateItem().getTime());
        viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View vwText = findViewById(R.id.textCalendarMessage);
                Long lngCalDate = (Long) v.getTag(CALENDAR_DATE_KEY);
                if(vwText!=null && vwText instanceof TextView &&
                        lngCalDate!=null) {
                        ((TextView) vwText).setText(CalendarScreenMessage(new Date(lngCalDate)));
                }
            }
        });
        if(!String.format(Locale.US,"%1$tY%1$tm",calDateItem.getDateItem())
                .equals(String.format(Locale.US,"%1$tY%1$tm",dteMonth))) {
            txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyAntares));
            if(calDateItem.getDateItem().before(dteMonth)) {
                viewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long lngCalDate = (Long) v.getTag(CALENDAR_DATE_KEY);
                        if(lngCalDate!=null)
                        displayCalendar(new Date(lngCalDate));
                    }
                });
            } else {
                viewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long lngCalDate = (Long) v.getTag(CALENDAR_DATE_KEY);
                        if(lngCalDate!=null)
                            displayCalendar(new Date(lngCalDate));
                    }
                });
            }
        } else if(String.format(Locale.US,"%1$tY%1$tm%1$td",calDateItem.getDateItem())
                .equals(String.format(Locale.US,"%1$tY%1$tm%1$td",new Date()))) {
            txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkySun));
        } else {
            txDate.setTextColor(mContext.getResources().getColor(R.color.foregroundSkyDeneb));
            // txDate.setStyle(TextView.BOLD);
        }
        txDate.setGravity(Gravity.END);
        ImageView imageViewMoon = new ImageView(mContext);
        imageViewMoon.setVisibility(View.INVISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageViewMoon.setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
        } else {
            imageViewMoon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
        }
        ImageView imageView2 = new ImageView(mContext);
        imageView2.setVisibility(View.INVISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView2.setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
        } else {
            imageView2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
        }
        ImageView imageView3 = new ImageView(mContext);
        imageView3.setVisibility(View.INVISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView3.setImageResource(R.drawable.ic_sun_quiet_alert_yellow_24dp);
        } else {
            imageView3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sun_quiet_alert_yellow_24dp));
        }
        if(strsEvents.size()>0) {
            ImageView imageViewToUse = imageView3;
            Boolean blnISS = false, blnIridium = false, blnMeteor = false;
            for (String arrEvents :
                    strsEvents) {
                Log.d(TAG, "displayCell: "+arrEvents);
                Long lngTime = Long.valueOf(arrEvents.split(",")[0]);
                String strName = arrEvents.split(",")[1],
                        strType = arrEvents.split(",")[2];
                switch (strType) {
                    case "ISS":
                        imageView3.setVisibility(View.VISIBLE);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView3.setImageResource(R.drawable.ic_satellite_iss_24dp);
                        } else {
                            imageView3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_satellite_iss_24dp));
                        }
                        imageViewToUse = imageView2;
                        blnISS = true;
                        break;
                    case "Iridium":
                        if(!blnISS || !blnMeteor) {
                            imageViewToUse.setVisibility(View.VISIBLE);
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                imageViewToUse.setImageResource(R.drawable.ic_iridium_flare_yellow_24dp);
                            } else {
                                imageViewToUse.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_iridium_flare_yellow_24dp));
                            }
                            blnIridium = true;
                        }
                        break;
                    case "METEORSHOWERBEG":
                    case "METEORSHOWERPEAK":
                    case "METEORSHOWERFIN":
                        imageView2.setVisibility(View.VISIBLE);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView2.setImageResource(R.drawable.ic_astro_meteors_24dp);
                        } else {
                            imageView2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_meteors_24dp));
                        }
                        blnMeteor = true;
                        break;
                    case "Partial Solar":
                    case "Total Solar":
                    case "Annular Solar":
                    case "Hybrid Solar":
                        imageViewMoon.setVisibility(View.VISIBLE);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageViewMoon.setImageResource(R.drawable.ic_astro_eclipse_solar_24dp);
                        } else {
                            imageViewMoon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_eclipse_solar_24dp));
                        }
                        break;
                    case "Partial Lunar":
                    case "Total Lunar":
                        imageViewMoon.setVisibility(View.VISIBLE);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageViewMoon.setImageResource(R.drawable.ic_astro_eclipse_lunar_24dp);
                        } else {
                            imageViewMoon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_astro_eclipse_lunar_24dp));
                        }
                        break;
                }
            }
        }
        linearLayout1.addView(txDate);
        linearLayout1.addView(imageViewMoon);
        ((LinearLayout) viewGroup).removeAllViews();
        ((LinearLayout) viewGroup).addView(linearLayout1);

        // add line two
        linearLayout2.addView(imageView2);
        linearLayout2.addView(imageView3);
        ((LinearLayout) viewGroup).addView(linearLayout2);
    }

    private String CalendarScreenMessage(Date dteCalendarTime) {
        String strMessage;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteCalendarTime);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,0);
        Date dteStart = calendar.getTime(),
                dteEnd = new Date(calendar.getTime().getTime() + ONE_FULL_DAY);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        AstroDatabase astroDatabase = new AstroDatabase(mContext,sharedPreferences.getString(OBSERVATORY_KEY,null));
        ArrayList<String> strsEvents = astroDatabase.getEvents(dteStart,dteEnd);
        astroDatabase.astroDBclose();
        if(strsEvents.size()==0) {
            strMessage = String.format(
                    Locale.US,
                    getString(R.string.calendar_default_message),
                    dteStart
            );
        } else if(strsEvents.size()==1) {
            strMessage = String.format(
                    Locale.US,
                    getString(R.string.calendar_single_event_message),
                    dteStart
            );
            Long lngEvent = Long.valueOf(strsEvents.get(0).split(",")[0])*1000L;
            switch (strsEvents.get(0).split(",")[2]) {
                case "ISS":
                    strMessage += "\n" + String.format(Locale.US,mContext.getString(R.string.calendar_iss_event_message),new Date(lngEvent));
                    break;
                case "Iridium":
                case "normal":
                    strMessage += "\n" + String.format(Locale.US,mContext.getString(R.string.calendar_satellite_event_message),new Date(lngEvent));
                    break;
                case "METEORSHOWERBEG":
                case "METEORSHOWERPEAK":
                case "METEORSHOWERFIN":
                    strMessage += "\n" + String.format(Locale.US,mContext.getString(R.string.calendar_meteor_event_message),new Date(lngEvent));
                    break;
                case "Partial Solar":
                case "Total Solar":
                case "Annular Solar":
                case "Hybrid Solar":
                case "Partial Lunar":
                case "Total Lunar":
                    strMessage += "\n" + String.format(Locale.US,mContext.getString(R.string.calendar_eclipse_event_message),new Date(lngEvent));
                    break;
            }
        } else {
            strMessage = String.format(
                    Locale.US,
                    getString(R.string.calendar_multiple_event_message),
                    dteStart,
                    strsEvents.size()
            );
            for (String arrEvent : strsEvents) {
                Long lngEvent = Long.valueOf(arrEvent.split(",")[0]) * 1000L;
                switch (arrEvent.split(",")[2]) {
                    case "ISS":
                        strMessage += "\n" + String.format(Locale.US, mContext.getString(R.string.calendar_iss_event_message), new Date(lngEvent));
                        break;
                    case "Iridium":
                    case "normal":
                        strMessage += "\n" + String.format(Locale.US, mContext.getString(R.string.calendar_satellite_event_message), new Date(lngEvent));
                        break;
                    case "METEORSHOWERBEG":
                    case "METEORSHOWERPEAK":
                    case "METEORSHOWERFIN":
                        strMessage += "\n" + String.format(Locale.US, mContext.getString(R.string.calendar_meteor_event_message), new Date(lngEvent));
                        break;
                    case "Partial Solar":
                    case "Total Solar":
                    case "Annular Solar":
                    case "Hybrid Solar":
                    case "Partial Lunar":
                    case "Total Lunar":
                        strMessage += "\n" + String.format(Locale.US, mContext.getString(R.string.calendar_eclipse_event_message), new Date(lngEvent));
                        break;
                }
            }
        }
        return strMessage;
    }
}

// Each cell contains a date and up to 3 event images
class CalendarDateItem {
    Integer intDate, idEvent1, idEvent2, idEvent3;
    Date dteItem;
    Context mContext;
    CalendarDateItem(Date dteItem) {
        this.dteItem=dteItem;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteItem);
        this.intDate=calendar.get(Calendar.DAY_OF_MONTH);
        this.idEvent1=null;
        this.idEvent2=null;
        this.idEvent3=null;
    }
    CalendarDateItem(Date dteItem, Integer idEvent) {
        this.dteItem=dteItem;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteItem);
        this.intDate=calendar.get(Calendar.DAY_OF_MONTH);
        this.idEvent1=null;
        this.idEvent2=null;
        this.idEvent3=idEvent;
    }
    CalendarDateItem(Date dteItem, Integer idEvent1, Integer idEvent2) {
        this.dteItem=dteItem;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteItem);
        this.intDate=calendar.get(Calendar.DAY_OF_MONTH);
        this.idEvent1=null;
        this.idEvent2=idEvent1;
        this.idEvent3=idEvent2;
    }
    CalendarDateItem(Date dteItem, Integer idEvent1, Integer idEvent2, Integer idEvent3) {
        this.dteItem=dteItem;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dteItem);
        this.intDate=calendar.get(Calendar.DAY_OF_MONTH);
        this.idEvent1=idEvent1;
        this.idEvent2=idEvent2;
        this.idEvent3=idEvent3;
    }

    public Integer getDayOfMonth() {
        return intDate;
    }

    public Integer getIdEvent1() {
        return idEvent1;
    }

    public Integer getIdEvent2() {
        return idEvent2;
    }

    public Integer getIdEvent3() {
        return idEvent3;
    }

    public Date getDateItem() {
        return dteItem;
    }
}

// Each cell is an extension of the base adapter
class DateCellAdapter extends BaseAdapter {
    ArrayList<CalendarDateItem> listDateItems;

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return listDateItems.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
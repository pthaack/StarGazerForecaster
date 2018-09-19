package ca.philipyoung.astroforecast;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ActivityCalendar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final View vwCal = findViewById(R.id.viewCalendar),
                vwText = findViewById(R.id.textCalendar);
        if(vwCal!=null && vwCal instanceof CalendarView &&
                vwText!=null && vwText instanceof TextView) {
            ((CalendarView)vwCal).setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year,month,dayOfMonth);
                    Date date = calendar.getTime();
                    ((TextView) vwText).setText(String.format(Locale.US,"%1$tB %1$te, %1$tY",date));

                    // change the properties of a date once selected
                    // view.setDateTextAppearance();
                }
            });
        }
    }
}

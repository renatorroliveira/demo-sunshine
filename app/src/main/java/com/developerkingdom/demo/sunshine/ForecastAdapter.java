package com.developerkingdom.demo.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.developerkingdom.demo.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    final Context mContext;
    boolean mUseTodayLayout = true;

    static final String[] FORECAST_COLUMNS = new String[] {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_WEATHER_HUMIDITY = 9;
    static final int COL_WEATHER_WIND = 10;
    static final int COL_WEATHER_WIND_DIR = 11;
    static final int COL_WEATHER_PRESSURE = 12;

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_REGULAR = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    public void setUseTodayLayout(boolean value) {
        mUseTodayLayout = value;
    }

    @Override
    public int getItemViewType(int position) {
        return (((position == 0) && mUseTodayLayout) ? VIEW_TYPE_TODAY:VIEW_TYPE_REGULAR);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        View view;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast_today, parent, false);
                break;
            }
            default: {
                view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast2, parent, false);
                break;
            }
        }
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View root, Context context, Cursor cursor) {
        String description = cursor.getString(COL_WEATHER_DESC);
        String date = Utility.getFriendlyDayString(context, cursor.getLong(COL_WEATHER_DATE));
        boolean isMetric = Utility.isMetric(context);
        String max = Utility.formatTemperature(
                cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric
        )+"º"+(isMetric ? "C":"F");
        String min = Utility.formatTemperature(
                cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric
        )+"º"+(isMetric ? "C":"F");
        int weatherId = cursor.getInt(ForecastAdapter.COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(cursor.getPosition());
        ViewHolder holder = (ViewHolder) root.getTag();

        holder.date.setText(date);
        holder.description.setText(description);
        holder.max.setText(max);
        holder.min.setText(min);
        if (viewType == VIEW_TYPE_TODAY) {
            holder.icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        } else {
            holder.icon.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }
    }

    static class ViewHolder {

        public ImageView icon;
        public TextView description;
        public TextView date;
        public TextView max;
        public TextView min;

        public ViewHolder(View root) {
            icon = ((ImageView) root.findViewById(R.id.list_item_icon));
            date = ((TextView) root.findViewById(R.id.list_item_date_textview));
            description = ((TextView) root.findViewById(R.id.list_item_forecast_textview));
            max = ((TextView) root.findViewById(R.id.list_item_high_textview));
            min = ((TextView) root.findViewById(R.id.list_item_low_textview));
        }
    }
}

package com.developerkingdom.demo.sunshine;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.developerkingdom.demo.sunshine.data.WeatherContract;

public class DetailedForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;

    private Uri forecastDataUri;
    private ShareActionProvider shareActionProvider;
    private TextView dateView;
    private TextView maxView;
    private TextView minView;
    private ImageView iconView;
    private TextView descriptionView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public static DetailedForecastFragment newInstance(String location, long date) {
        DetailedForecastFragment instance = new DetailedForecastFragment();
        Bundle args = new Bundle();
        args.putString(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, location);
        args.putLong(WeatherContract.WeatherEntry.COLUMN_DATE, date);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if (args != null) {
            forecastDataUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                    args.getString(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING),
                    args.getLong(WeatherContract.WeatherEntry.COLUMN_DATE)
            );
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailedforecastfragment, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        shareActionProvider = ((ShareActionProvider) item.getActionProvider());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_forecast, container, false);
        dateView = (TextView) rootView.findViewById(R.id.detailed_date_textview);
        maxView = (TextView) rootView.findViewById(R.id.detailed_high_textview);
        minView = (TextView) rootView.findViewById(R.id.detailed_low_textview);
        iconView = (ImageView) rootView.findViewById(R.id.detailed_icon);
        descriptionView = (TextView) rootView.findViewById(R.id.detailed_forecast_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detailed_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detailed_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detailed_pressure_textview);

        getLoaderManager().initLoader(LOADER_ID, savedInstanceState, this);
        return rootView;
    }

    public void onLocationChanged(String location) {
        Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                location, WeatherContract.WeatherEntry.getDateFromUri(forecastDataUri)
        );
        getArguments().putString(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, location);
        forecastDataUri = updatedUri;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    Intent createShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                maxView.getText() + " - " + descriptionView.getText());
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), forecastDataUri, ForecastAdapter.FORECAST_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst())
            return;
        String description = cursor.getString(ForecastAdapter.COL_WEATHER_DESC);
        String date = Utility.getFriendlyDayString(getActivity(), cursor.getLong(
                ForecastAdapter.COL_WEATHER_DATE));
        boolean isMetric = Utility.isMetric(getActivity());
        String max = Utility.formatTemperature(
                cursor.getDouble(ForecastAdapter.COL_WEATHER_MAX_TEMP), isMetric
        )+"º"+(isMetric ? "C":"F");
        String min = Utility.formatTemperature(
                cursor.getDouble(ForecastAdapter.COL_WEATHER_MIN_TEMP), isMetric
        )+"º"+(isMetric ? "C":"F");
        String humidity = getString(R.string.detailed_humidity, cursor.getDouble(ForecastAdapter.COL_WEATHER_HUMIDITY))+"%";
        String wind = getString(R.string.detailed_wind,
                cursor.getDouble(ForecastAdapter.COL_WEATHER_WIND),
                cursor.getString(ForecastAdapter.COL_WEATHER_WIND_DIR)
        );
        String pressure = getString(R.string.detailed_pressure, cursor.getDouble(ForecastAdapter.COL_WEATHER_PRESSURE));
        int weatherId = cursor.getInt(ForecastAdapter.COL_WEATHER_CONDITION_ID);

        dateView.setText(date);
        maxView.setText(max);
        minView.setText(min);
        descriptionView.setText(description);
        humidityView.setText(humidity);
        windView.setText(wind);
        pressureView.setText(pressure);
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

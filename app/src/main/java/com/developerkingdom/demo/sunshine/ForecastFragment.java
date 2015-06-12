package com.developerkingdom.demo.sunshine;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.developerkingdom.demo.sunshine.data.WeatherContract;
import com.developerkingdom.demo.sunshine.service.FetchWeatherService;
import com.developerkingdom.demo.sunshine.sync.SunshineSyncAdapter;
import com.developerkingdom.demo.sunshine.util.WeatherDataParser;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Forecasts view fragment.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    static final String FORECAST_Y_KEY = "forecastListScrollYSaved";
    protected ForecastAdapter forecastAdapter;
    private OnForecastClickCallback iForecastClickCallback;
    ListView mForecastList;
    String mLocation;
    int scrollYSaved = 0;

    static final int WEATHER_LOADER_ID = 1;

    public ForecastAdapter getForecastAdapter() {
        return forecastAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mLocation = Utility.getPreferredLocation(getActivity());
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null)
            outState.putInt(FORECAST_Y_KEY, scrollYSaved);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mForecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastList.setAdapter(forecastAdapter);
        mForecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                scrollYSaved = mForecastList.getScrollY();
                if (cursor != null) {
                    if (iForecastClickCallback == null) {
                        throw new UnsupportedOperationException("You must set a forecast click callback.");
                    }
                    iForecastClickCallback.onForecastClick(getActivity(), cursor);
                }
            }
        });
        if (savedInstanceState != null)
            scrollYSaved = savedInstanceState.getInt(FORECAST_Y_KEY);
        else
            scrollYSaved = 0;
        return rootView;
    }

    public void onLocationChanged(String location) {
        mLocation = location;
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    protected void updateWeather() {
        Log.v(LOG_TAG, "Starting refresh task...");
        /*String location = Utility.getPreferredLocation(getActivity());
        Intent intent = new Intent(getActivity(), FetchWeatherService.AlarmReceiver.class);
        intent.putExtra(FetchWeatherService.EXTRA_LOCATION, location);
        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 5000L,
                alarmIntent);*/
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
            mLocation, System.currentTimeMillis());
        return new CursorLoader(getActivity(), weatherForLocationUri,
            ForecastAdapter.FORECAST_COLUMNS,
            null, null, sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
        mForecastList.scrollTo(0, scrollYSaved);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            forecastAdapter.swapCursor(null);
        }

    public void setOnForecastClickCallback(OnForecastClickCallback callback) {
        iForecastClickCallback = callback;
    }
    public interface OnForecastClickCallback {
        void onForecastClick(Context context, Cursor data);
    }
}

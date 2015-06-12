package com.developerkingdom.demo.sunshine;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.developerkingdom.demo.sunshine.data.WeatherContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();
    protected static final String TAG_DETAILED_FORECAST_FRAGMENT = "TAG_DETAILEDFORECASTFRAGMENT";

    boolean mIsLandscape;

    private String mLocation;

    @Override
    protected void onResume() {
        super.onResume();
        String actual = Utility.getPreferredLocation(this);
        if (!actual.equals(mLocation)) {
            if (mIsLandscape) {
                DetailedForecastFragment frag = (DetailedForecastFragment)
                        getFragmentManager().findFragmentByTag(TAG_DETAILED_FORECAST_FRAGMENT);
                if (frag != null)
                    frag.onLocationChanged(actual);
            }
            ForecastFragment frag = (ForecastFragment)
                    getFragmentManager().findFragmentById(R.id.forecast_container);
            if (frag != null)
                frag.onLocationChanged(actual);
            mLocation = actual;
            setTitle(getString(R.string.app_name)+" ("+mLocation+")");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.detailed_forecast_container) != null) {
            mIsLandscape = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.detailed_forecast_container,
                            DetailedForecastFragment.newInstance(
                                    Utility.getPreferredLocation(this), System.currentTimeMillis()
                            ), TAG_DETAILED_FORECAST_FRAGMENT)
                        .commit();
            }
        } else {
            mIsLandscape = false;
            getActionBar().setElevation(0.0f);
        }
        ForecastFragment fFrag = ((ForecastFragment) getFragmentManager().findFragmentById(R.id.forecast_container));
        fFrag.setOnForecastClickCallback(new ForecastClickListener());
        fFrag.getForecastAdapter().setUseTodayLayout(!mIsLandscape);

        mLocation = Utility.getPreferredLocation(this);
        setTitle(getString(R.string.app_name)+" ("+mLocation+")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_view_location) {
            launchLocationOnMap(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void launchLocationOnMap(Context context) {
        Uri uri = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("z", "4")
                .appendQueryParameter("q", Utility.getPreferredLocation(context))
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "No maps application available on your phone.", Toast.LENGTH_LONG).show();
        }
    }

    class ForecastClickListener implements ForecastFragment.OnForecastClickCallback {
        @Override
        public void onForecastClick(Context context, Cursor data) {
            if (mIsLandscape) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.detailed_forecast_container,
                                DetailedForecastFragment.newInstance(
                                        data.getString(ForecastAdapter.COL_LOCATION_SETTING),
                                        data.getLong(ForecastAdapter.COL_WEATHER_DATE)
                                ), TAG_DETAILED_FORECAST_FRAGMENT)
                        .commit();
            } else {
                Intent intent = new Intent(context, DetailedForecastActivity.class)
                        .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                data.getString(ForecastAdapter.COL_LOCATION_SETTING),
                                data.getLong(ForecastAdapter.COL_WEATHER_DATE)
                        ));
                startActivity(intent);
            }
        }
    }
}

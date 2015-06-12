package com.developerkingdom.demo.sunshine;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.developerkingdom.demo.sunshine.data.WeatherContract;


public class DetailedForecastActivity extends Activity {

    public static final String PARAM_FORECAST = "forecastText";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_forecast);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String location;
            long date;
            if (intent != null) {
                Uri uri = intent.getData();
                location = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
                date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            } else {
                location = Utility.getPreferredLocation(this);
                date = System.currentTimeMillis();
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.detailed_forecast_container,
                        DetailedForecastFragment.newInstance(
                                location, date
                        ))
                    .commit();
        }
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
        }  else if (id == R.id.action_view_location) {
            MainActivity.launchLocationOnMap(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

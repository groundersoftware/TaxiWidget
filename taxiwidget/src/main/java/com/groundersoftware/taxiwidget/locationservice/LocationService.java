package com.groundersoftware.taxiwidget.locationservice;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Location Service is responsible for detecting position from Network provider. We are manually receiving position each 30 second in separated timer
 * for better battery consumption.
 */
public class LocationService implements LocationListener {

    // Context
    private Context mContext;

    // Timer for detecting location
    private Timer mTimer;

    // Location manager
    private LocationManager mLocationManager;

    // Last valid detected location
    private Location _lastValidLocation;

    /**
     * Creates location service.
     * @param context widget context
     */
    public LocationService(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
    }

    /**
     * Enable location manager.
     */
    public void enable() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                detectPosition();
            }

        }, 0, 15000);
        //Toast.makeText(mContext, "Location Service enabled.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Disable location manager.
     */
    public void disable() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            //Toast.makeText(mContext, "Location Service disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns last valid position if exist.
     * @return last valid position or null if any position was detected.
     */
    public Location getLocation() {
        return _lastValidLocation;
    }

    /**
     * Detecting position from Network provider.
     */
    private void detectPosition()
    {
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            // Requesting one single position update from location service
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, mContext.getMainLooper());
        }
    }

    /**
     * Tests if new better location is better than valid previous one.
     * Algorithm used from http://developer.android.com/guide/topics/location/strategies.html.
     * @param location New location
     * @param currentBestLocation Current one location
     * @return True if new location is better, otherwise false.
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation)
    {
        final int oneMinute = 1000 * 60;       // one minute

        if (currentBestLocation == null)
        {
            // A new location is always better than no location
            return true;
        }

        // if distance is according to 50 meters returns false
        float distance = location.distanceTo(currentBestLocation);
        if (distance < 50) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > oneMinute;
        boolean isSignificantlyOlder = timeDelta < -oneMinute;
        boolean isNewer = timeDelta > 0;

        // If it's been more than one minute since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer)
        {
            return true;
        }
        else if (isSignificantlyOlder)
        {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate)
        {
            return true;
        }
        else if (isNewer && !isLessAccurate)
        {
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
        {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     * @param provider1 first provider
     * @param provider2 second provider
     * @return true if they are equal, otherwise false
     */
    private static boolean isSameProvider(String provider1, String provider2)
    {
        if (provider1 == null)
        {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {

        String quality = "[Not Better]";

        if (isBetterLocation(location, _lastValidLocation)) {
            _lastValidLocation = location;
            quality = "[Better]";
        }

        //Toast.makeText(mContext, "Position: Longitude: " + location.getLongitude() + ", Latitude: " + location.getLatitude() + " " + quality, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // nothing for now
    }

    @Override
    public void onProviderEnabled(String s) {
        // nothing for now
    }

    @Override
    public void onProviderDisabled(String s) {
        // nothing for now
    }
}

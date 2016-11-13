package org.flexites.plugin;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Geolocation extends CordovaPlugin implements LocationProvider.LocationCallback {

    private static final String TAG = "FlexitesGeolocationPlugin";
    private LocationProvider mLocationProvider;
    private Location mLastLocation;

    /**
     * Gets the application context from cordova's main activity.
     * @return the application context
     */
    private Context getApplicationContext() {
        return this.cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("addWatch")) {
            if (mLocationProvider == null) {
                final LocationProvider.LocationCallback cb = this;
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        Log.i(TAG, "initing location provider");
                        mLocationProvider = new LocationProvider(getApplicationContext(), cb);
                        PluginResult r = new PluginResult(PluginResult.Status.OK);
                        callbackContext.sendPluginResult(r);
                    }
                });
            } else {
                mLocationProvider.connect();
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(r);
            }
            return true;
        } else if (action.equals("clearWatch")) {
            PluginResult r;
            if (mLocationProvider != null) {
                mLocationProvider.disconnect();
                r = new PluginResult(PluginResult.Status.OK);
            } else
                r = new PluginResult(PluginResult.Status.ERROR);
            callbackContext.sendPluginResult(r);
            return true;
        } else if (action.equals("getLocation")) {
            if (mLastLocation != null) {
                JSONObject result = new JSONObject();
                result.put("latitude", mLastLocation.getLatitude());
                result.put("longitude", mLastLocation.getLongitude());
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
            } else
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            return true;
        }
        return false;
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.i(TAG, "onResume");
        mLocationProvider.connect();
    }

    @Override
    public void onPause(boolean multitasking) {
        Log.i(TAG, "onPause");
        mLocationProvider.disconnect();
    }

    @Override
    public void handleNewLocation(Location location) {
        Log.i(TAG, "handleNewLocation " + location.getLatitude() + " " + location.getLongitude());
        mLastLocation = location;
    }
}
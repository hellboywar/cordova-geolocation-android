/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
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

public class GoogleGPSLocation extends CordovaPlugin {

    private static final String TAG = "GoogleGPSLocation";
    private Location mLastLocation;
    private LocationListener mListener;

    /**
     * Gets the application context from cordova's main activity.
     * @return the application context
     */
    public Context getApplicationContext() {
        return this.cordova.getActivity();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action
     *            The action to execute.
     * @param args
     *            JSONArry of arguments for the plugin.
     * @param callbackContext
     *            The callback id used when calling back into JavaScript.
     * @return True if the action was valid, or false if not.
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action == null || !action.matches("getLocation|addWatch|clearWatch"))
            return false;

        final String id = args.optString(0, "");

        if (action.equals("clearWatch")) {
            clearWatch(id);
            return true;
        }

        /*
        if (isGPSdisabled()) {
            fail(LocationListener.POSITION_UNAVAILABLE, "GPS is disabled on this device.", callbackContext, false);
            return true;
        }
        */

        if (action.equals("getLocation"))
            getLastLocation(args, callbackContext);
        else if (action.equals("addWatch"))
            addWatch(id, callbackContext);

        return true;
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.i(TAG, "onResume");
        getListener().start();
    }

    @Override
    public void onPause(boolean multitasking) {
        Log.i(TAG, "onPause");
        getListener().stop();
    }

    public JSONObject returnLocationJSON(Location loc) {
        JSONObject o = new JSONObject();

        try {
            o.put("latitude", loc.getLatitude());
            o.put("longitude", loc.getLongitude());
            o.put("altitude", (loc.hasAltitude() ? loc.getAltitude() : null));
            o.put("accuracy", loc.getAccuracy());
            o.put("heading",
                    (loc.hasBearing() ? (loc.hasSpeed() ? loc.getBearing()
                            : null) : null));
            o.put("velocity", loc.getSpeed());
            o.put("timestamp", loc.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }

    public void win(Location loc, CallbackContext callbackContext, boolean keepCallback) {
        mLastLocation = loc;
        PluginResult result = new PluginResult(PluginResult.Status.OK, this.returnLocationJSON(loc));
        result.setKeepCallback(keepCallback);
        callbackContext.sendPluginResult(result);
    }

    /**
     * Location failed. Send error back to JavaScript.
     *
     * @param code
     *            The error code
     * @param msg
     *            The error message
     * @throws JSONException
     */
    public void fail(int code, String msg, CallbackContext callbackContext, boolean keepCallback) {
        JSONObject obj = new JSONObject();
        String backup = null;
        try {
            obj.put("code", code);
            obj.put("message", msg);
        } catch (JSONException e) {
            obj = null;
            backup = "{'code':" + code + ",'message':'" + msg.replaceAll("'", "\'") + "'}";
        }
        PluginResult result;
        if (obj != null) {
            result = new PluginResult(PluginResult.Status.ERROR, obj);
        } else {
            result = new PluginResult(PluginResult.Status.ERROR, backup);
        }

        result.setKeepCallback(keepCallback);
        callbackContext.sendPluginResult(result);
    }

    private void getLastLocation(JSONArray args, CallbackContext callbackContext) {
        int maximumAge;
        try {
            maximumAge = args.getInt(0);
        } catch (JSONException e) {
            e.printStackTrace();
            maximumAge = 0;
        }
        // Check if we can use lastKnownLocation to get a quick reading and use
        // less battery
        if (mLastLocation != null && (System.currentTimeMillis() - mLastLocation.getTime()) <= maximumAge) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, returnLocationJSON(mLastLocation));
            callbackContext.sendPluginResult(result);
        } else {
            getCurrentLocation(callbackContext, Integer.MAX_VALUE);
        }
    }

    private void clearWatch(String id) {
        getListener().clearWatch(id);
    }

    private void getCurrentLocation(CallbackContext callbackContext, int timeout) {
        getListener().addCallback(callbackContext, timeout);
    }

    private void addWatch(String timerId, CallbackContext callbackContext) {
        getListener().addWatch(timerId, callbackContext);
    }

    private LocationListener getListener() {
        if (mListener == null)
            mListener = new LocationListener(this, TAG);
        return mListener;
    }
}
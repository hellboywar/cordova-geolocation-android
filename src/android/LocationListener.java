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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class LocationListener implements LocationProvider.LocationCallback {

    public static int PERMISSION_DENIED = 1;
    public static int POSITION_UNAVAILABLE = 2;
    public static int TIMEOUT = 3;

    private HashMap<String, CallbackContext> watches = new HashMap<String, CallbackContext>();
    private LocationProvider mLocationProvider;
    protected boolean mIsRunning = false;

    private GoogleGPSLocation mOwner;
    private List<CallbackContext> mCallbacks = new ArrayList<CallbackContext>();
    private Timer mTimer = null;
    private String TAG;

    public LocationListener(GoogleGPSLocation owner, String tag) {
        mOwner = owner;
        TAG = tag;
        mLocationProvider = new LocationProvider(owner.getApplicationContext(), this);
    }

    @Override
    public void handleNewLocation(Location location) {
        Log.i(TAG, "handleNewLocation " + location.getLatitude() + " " + location.getLongitude());
        win(location);
    }

    public int size() {
        return watches.size() + mCallbacks.size();
    }

    public void addWatch(String timerId, CallbackContext callbackContext) {
        watches.put(timerId, callbackContext);

        if (size() == 1)
            start();
    }

    public void addCallback(CallbackContext callbackContext, int timeout) {
        if (mTimer == null)
            mTimer = new Timer();

        mTimer.schedule(new LocationTimeoutTask(callbackContext, this), timeout);
        mCallbacks.add(callbackContext);

        if (size() == 1)
            start();
    }

    public void clearWatch(String timerId) {
        if (watches.containsKey(timerId))
            watches.remove(timerId);

        if (size() == 0)
            stop();
    }

    protected void fail(int code, String message) {
        cancelTimer();

        for (CallbackContext callbackContext : mCallbacks)
            mOwner.fail(code, message, callbackContext, false);

        if (watches.size() == 0)
            stop();

        mCallbacks.clear();

        for (CallbackContext callbackContext : watches.values())
            mOwner.fail(code, message, callbackContext, true);
    }

    protected void win(Location loc) {
        cancelTimer();

        for (CallbackContext callbackContext : mCallbacks)
            mOwner.win(loc, callbackContext, false);

        if (watches.size() == 0)
            stop();

        mCallbacks.clear();

        for (CallbackContext callbackContext : watches.values())
            mOwner.win(loc, callbackContext, true);
    }

    public void start() {
        mLocationProvider.connect();
    }

    public void stop() {
        cancelTimer();
        mLocationProvider.disconnect();
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private class LocationTimeoutTask extends TimerTask {

        private CallbackContext mCallbackContext = null;
        private LocationListener mListener = null;

        public LocationTimeoutTask(CallbackContext callbackContext, LocationListener listener) {
            mCallbackContext = callbackContext;
            mListener = listener;
        }

        @Override
        public void run() {
            for (CallbackContext callbackContext : mListener.mCallbacks) {
                if (mCallbackContext == callbackContext) {
                    mListener.mCallbacks.remove(callbackContext);
                    break;
                }
            }

            if (mListener.size() == 0)
                mListener.stop();
        }
    }
}
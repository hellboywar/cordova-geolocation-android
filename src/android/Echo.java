package org.flexites.plugin;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * This class echoes a string called from JavaScript.
 */
public class Echo extends CordovaPlugin implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "GeoPlugin";
    private static CallbackContext geoContext;
//    private static CordovaWebView gWebView;
//    private static Bundle gCachedExtras = null;
//    private static boolean gForeground = false;

    /**
     * Gets the application context from cordova's main activity.
     * @return the application context
     */
    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("echo")) {
            geoContext = callbackContext;
//            String message = args.getString(0);
//            this.echo(message, callbackContext);
            Context appContext = getApplicationContext();
            mGoogleApiClient = new GoogleApiClient.Builder(appContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            return true;
        }
        return false;
    }

//    private void echo(String message, CallbackContext callbackContext) {
//        if (message != null && message.length() > 0) {
//            callbackContext.success(message);
//        } else {
//            callbackContext.error("Expected one non-empty string argument.");
//        }
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        geoContext.success("connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        geoContext.success("connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        geoContext.success("connection failed");
    }
}
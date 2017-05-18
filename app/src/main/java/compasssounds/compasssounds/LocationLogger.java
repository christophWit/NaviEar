package compasssounds.compasssounds;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.*;

/**
 * Created by james on 30/04/15.
 */
public class LocationLogger {

    //Location Variables
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    public float mLocationAccuracy = 0;
    private long mLocationTime = 0; //UTC time of this fix, in milliseconds since January 1, 1970.
    public double mLatitude = 0;
    public double mLongitude = 0;
    private boolean locationServiceIsRunning = false;
    private Context mContext;

    //Orientation Variables
    public float mAzimuth = 0;
    public float mPitch = 0;
    public float mRoll = 0;

    public void start(Context context) {
        mContext = context;
        locationServiceIsRunning = true;
        // Set up GPS
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        mLocationManager.requestLocationUpdates(LocationManager
                .GPS_PROVIDER, 5000, 10, mLocationListener);


        // Start logging timer
        timerHandler.postDelayed(timerRunnable, 1000); //starts logging a second after start
    }

    public void stop() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        timerHandler.removeCallbacksAndMessages(null);
    }

    public boolean canUseGPS(final Activity activity) {
        ContentResolver contentResolver = activity.getBaseContext().getContentResolver();
        boolean gpsStatus = android.provider.Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);

        if (!gpsStatus) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Your Device's GPS is Disable")
                    .setCancelable(false)
                    .setTitle("** Gps Status **")
                    .setPositiveButton("Gps On",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // finish the current activity
                                    // AlertBoxAdvance.this.finish();
                                    Intent myIntent = new Intent(
                                            android.provider.Settings.ACTION_SECURITY_SETTINGS);
                                    activity.startActivity(myIntent);
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // cancel the dialog box
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        return true;
    }

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {


            mLongitude = loc.getLongitude();
            mLatitude = loc.getLatitude();
            mLocationAccuracy = loc.getAccuracy();
            mLocationTime = loc.getTime();

            /* [Google] define accuracy as the radius of 68% confidence. In other
             * words, if you draw a circle centered at this location's
             * latitude and longitude, and with a radius equal to the accuracy,
             * then there is a 68% probability that the true location is inside
             * the circle. */
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (locationServiceIsRunning) {
                logMessageLine();

                //two minutes in ms = 1000 * 60 * 2
                timerHandler.postDelayed(this, 120000);
            }
        }
    };

    public void logMessageLine() {
        String strDate = ParticipantLog.getDateTime();
        ParticipantLog.writeLogMessageLine(strDate + "," + mLatitude + "," + mLongitude + "," + mLocationAccuracy + "," + mLocationTime + "," + mAzimuth + "," + mPitch + "," + mRoll, mContext, ParticipantLog.LOCATION_LOG);
    }
}

package compasssounds.compasssounds;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.content.Context;
import java.util.ArrayList;



public class CompassSoundService extends Service {


    public static final CompassSensor sensor = new InternalCompassSensor(); // Yei or Internal CompassSensor();

    protected static boolean serviceIsRunning = false;
    protected static boolean soundGeneratorUpdatesPaused = false;
    private static CompassSoundService mInstance;
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_ORIENTATION = 3;

    public LocationLogger locationLogger;
    private float currentAzimuth;

    //System interaction variables
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    private PowerManager.WakeLock wl;


    public static CompassSoundService getInstance(){
        //service needs to be up and running before calling this
        // (not going to use default constructor, as should be launched from intent)
        return mInstance;
    }
    public static boolean isRunning() {
        //this alerts if the actual service is producing sounds (not just whether singleton has been created)
        return serviceIsRunning;
    }
    public static boolean AssertServiceRunning(final Activity activity) {
        if (!isRunning()) {
            activity.startService(new Intent(activity.getBaseContext(), CompassSoundService.class));
        }
        return true;
    }
    public static void stopSounds() {
        mInstance.stopSoundProcess();
    }
    public static void pause(boolean pauseOn) {
        Log.i("CompassSoundService","Pause: " + String.valueOf(pauseOn));
        soundGeneratorUpdatesPaused = pauseOn;
        SoundGenerator.getInstance().pauseEngine(soundGeneratorUpdatesPaused);
    }
    //plays a tone of specific azimuth for time_ms.  if time_ms is -1, then will go on for infinite,
    //use pause(true) to stop
    public static void playTone(float azimuth, int time_ms) {
        if (!soundGeneratorUpdatesPaused) { pause(true); }
        SoundGenerator.getInstance().updateAzimuth(azimuth);

        Log.i("CompassSoundService", "Playing tone: " + String.valueOf(azimuth));
        SoundGenerator.getInstance().pauseEngine(false); //allow sounds (but not updates)

        if (time_ms > -1) {
            Handler timerHandler = new Handler();

            timerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pause(true);
                }
            }, time_ms);
        }
    }

    public float getCurrentAzimuth() {

        return currentAzimuth;
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    public void orientationUpdate(float azimuth, float pitch, float roll) {

        if (!soundGeneratorUpdatesPaused) {
            SoundGenerator.getInstance().updateAzimuth(azimuth);
            Thread.yield(); //to allow sound engine to update as quickly as possible
        }

        currentAzimuth = azimuth;
        azimuth = azimuth % 360;

        if (locationLogger != null) {
            locationLogger.mAzimuth = azimuth;
            locationLogger.mPitch = pitch;
            locationLogger.mRoll = roll;
        }

        //send to UI
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Bundle b = new Bundle();
                b.putFloat("azimuth", azimuth);
                b.putFloat("pitch", pitch);
                b.putFloat("roll",roll);
                Message msg = Message.obtain(null, MSG_SET_ORIENTATION);
                msg.setData(b);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                Log.i("CompassSoundService","Could not send client messages as it appears to be dead");
                mClients.remove(i);
            }
        }
    }



    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
        Log.i("CompassSoundService", "Service Created");

    }




    @Override
    public IBinder onBind(Intent intent) {
        Log.i("CompassSoundService", "Service onBind called");
        return mMessenger.getBinder();
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("CompassSoundService", "Received start id " + startId + ": " + intent);
        if (!serviceIsRunning) {

            // Acquire a wake lock to improve processing
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Tag");
            wl.acquire();

            // Set up internal sensor
            sensor.setup(this);

            // Start logging location
            locationLogger = new LocationLogger();
            locationLogger.start(this);

            // Start playing sounds
            SoundGenerator.getInstance().initializeEngine(this);
            AdjustsCodingActivity.setCoding(this);
            serviceIsRunning = true;
        } else {
            Log.i("CompassSoundService", "Service already running.");
        }

        return Service.START_STICKY;
    }



    @Override
    public void onDestroy() {
        Log.i("CompassSoundService", "Service being destroyed");
        super.onDestroy();
        stopSoundProcess();
    }

    public void stopSoundProcess() {

        if (serviceIsRunning) {
            SoundGenerator.getInstance().stopEngine();
            serviceIsRunning = false;
        }

        Log.i("CompassSoundService", "Service Stopped");

        if (locationLogger != null) {
            locationLogger.stop();
            locationLogger = null;
        }

        if (wl != null) {
            wl.release();
            wl = null;
        }
    }

    public static BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothDataService mDataService = null;

}

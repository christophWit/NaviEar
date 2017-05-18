package compasssounds.compasssounds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by james on 30/04/15.
 */
public class InternalCompassSensor extends CompassSensor implements SensorEventListener {

    public InternalCompassSensor() {
        // initialize the ring buffer for orientation values
        mNumAngles=0;
        mRingBufferIndex=0;
        mAnglesRingBuffer=new float[RING_BUFFER_SIZE][3][2];
        mAngles=new float[3][2];
        mAngles[0][0]=0;
        mAngles[0][1]=0;
        mAngles[1][0]=0;
        mAngles[1][1]=0;
        mAngles[2][0]=0;
        mAngles[2][1]=0;
    }
    static final int SCREEN_OFF_RECEIVER_DELAY = 1000; //ms

    public BroadcastReceiver mReceiver = null; // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    private SensorManager mSensorManager; // device sensor manager
    private HandlerThread sensorThread;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    //INTERNAL SENSOR VARIABLES
    static private final int RING_BUFFER_SIZE=10;
    private float[][][] mAnglesRingBuffer;
    private int mNumAngles;
    private int mRingBufferIndex;
    private float[][] mAngles;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private Context mContext;
    private float[] mOrientation = new float[3];

    public void setup(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorThread = new HandlerThread("sensorThread");
        sensorThread.start();
        sensorThread.setPriority(Thread.MAX_PRIORITY);
        // Restart sensors
        registerSensors();
        // Register our receiver for the ACTION_SCREEN_OFF action. This reregisters the sensors
        // when the screen turns off.
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Check action just to be on the safe side.
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Log.v("CompassSoundService", "Reregistering sensors on screen off...");
                    Runnable runnable = new Runnable() {
                        public void run() {
                            registerSensors();
                        }
                    };

                    new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);

                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(mReceiver, filter);


    }

    private void registerSensors() {
        mSensorManager.unregisterListener(this);
        Handler handler = new Handler(sensorThread.getLooper());
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME, handler);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME, handler);
    }


    public void stop() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        if (sensorThread != null) {
            sensorThread.quit();
            sensorThread = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation); //Azimut, pitch, roll

            if(mNumAngles==RING_BUFFER_SIZE) {
                // subtract oldest vector
                mAngles[0][0]-=mAnglesRingBuffer[mRingBufferIndex][0][0];
                mAngles[0][1]-=mAnglesRingBuffer[mRingBufferIndex][0][1];
                mAngles[1][0]-=mAnglesRingBuffer[mRingBufferIndex][1][0];
                mAngles[1][1]-=mAnglesRingBuffer[mRingBufferIndex][1][1];
                mAngles[2][0]-=mAnglesRingBuffer[mRingBufferIndex][2][0];
                mAngles[2][1]-=mAnglesRingBuffer[mRingBufferIndex][2][1];
            } else {
                mNumAngles++;
            }

            // convert angles into x/y
            mAnglesRingBuffer[mRingBufferIndex][0][0]=(float) Math.cos(mOrientation[0]);
            mAnglesRingBuffer[mRingBufferIndex][0][1]=(float) Math.sin(mOrientation[0]);
            mAnglesRingBuffer[mRingBufferIndex][1][0]=(float) Math.cos(mOrientation[1]);
            mAnglesRingBuffer[mRingBufferIndex][1][1]=(float) Math.sin(mOrientation[1]);
            mAnglesRingBuffer[mRingBufferIndex][2][0]=(float) Math.cos(mOrientation[2]);
            mAnglesRingBuffer[mRingBufferIndex][2][1]=(float) Math.sin(mOrientation[2]);

            // accumulate new x/y vector
            mAngles[0][0]+=mAnglesRingBuffer[mRingBufferIndex][0][0];
            mAngles[0][1]+=mAnglesRingBuffer[mRingBufferIndex][0][1];
            mAngles[1][0]+=mAnglesRingBuffer[mRingBufferIndex][1][0];
            mAngles[1][1]+=mAnglesRingBuffer[mRingBufferIndex][1][1];
            mAngles[2][0]+=mAnglesRingBuffer[mRingBufferIndex][2][0];
            mAngles[2][1]+=mAnglesRingBuffer[mRingBufferIndex][2][1];

            mRingBufferIndex++;
            if(mRingBufferIndex==RING_BUFFER_SIZE) {
                mRingBufferIndex=0;
            }

            // convert back x/y into angles
            float azimuth=(float) Math.toDegrees(Math.atan2((double)mAngles[0][1], (double)mAngles[0][0]));
            float pitch=(float) Math.toDegrees(Math.atan2((double)mAngles[1][1], (double)mAngles[1][0]));
            float roll=(float) Math.toDegrees(Math.atan2((double)mAngles[2][1], (double)mAngles[2][0]));

            //North is 0 range is from 0 - 360 (java % is remainder no modulus)
            float modulo_azimuth = azimuth % 360;
            modulo_azimuth =  modulo_azimuth < 0 ? modulo_azimuth + 360 : modulo_azimuth;
            CompassSoundService.getInstance().orientationUpdate(modulo_azimuth, pitch, roll);
        }
    }

    public void tare() {

    }

}

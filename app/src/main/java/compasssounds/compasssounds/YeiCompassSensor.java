package compasssounds.compasssounds;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 * Created by james on 30/04/15.
 */
public class YeiCompassSensor extends CompassSensor {

    private boolean running = false;

    public void setup(Context context) {
        running = true;
        TSSBTSensor sensor = TSSBTSensor.getInstance();
        sensor.startConnection();
        //Start polling
        yeiConnectionThread = createNewThread();
        yeiConnectionThread.start();
    }

    public void stop() {
        running = false;
        TSSBTSensor.getInstance().closeConnection();
    }

    //runs without a timer by reposting this handler at the end of the runnable
    Thread yeiConnectionThread;

    private Thread createNewThread() {
        Thread newThread = new Thread() {
            final Handler yeiHandler = new Handler();

            public void run() {
                Looper.prepare();
                yeiHandler.post(yeiRunnable);
                Looper.loop();

            }

            Runnable yeiRunnable = new Runnable() {

                @Override
                public void run() {
                    if (!running) {
                        Log.i("CompassSoundService", "Exiting update thread");
                        return;
                    }

                    try {
                        //TODO: See if can handle turning off sensor better. Hangs when turning off sensor before stopping...
                        float[] angles = TSSBTSensor.getInstance().getFiltTaredOrientEuler();
                        // convert back x/y into angles
                        float azimuth = (float) Math.toDegrees(angles[1]);
                        float pitch = (float) Math.toDegrees(angles[0]);
                        float roll = (float) Math.toDegrees(angles[2]);


                        //North is 0 range is from 0 - 360 (java % is remainder no modulus)
                        float modulo_azimuth = azimuth % 360;
                        modulo_azimuth = 360 - modulo_azimuth;
                        modulo_azimuth =  modulo_azimuth < 0 ? modulo_azimuth + 360 : modulo_azimuth;

                        CompassSoundService.getInstance().orientationUpdate(modulo_azimuth, pitch, roll);
                        yeiHandler.postDelayed(this, 10);
                    } catch (Exception e) {
                        Log.i("CompassSoundService", "Bluetooth Sensor not available - trying to connect again in 5 seconds");
                        yeiHandler.postDelayed(this, 5000);
                    }
                }
            };
        };
        return newThread;
    }



    public void tare() {
        try {
            TSSBTSensor.getInstance().setTareCurrentOrient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

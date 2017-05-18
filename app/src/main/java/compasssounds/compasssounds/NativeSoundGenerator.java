package compasssounds.compasssounds;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by james on 23/02/15.
 */
public class NativeSoundGenerator extends SoundGenerator {

    private static int DEFAULT_SOUND_ENGINE = 4;
    private static int TEST_SOUND_ENGINE = 3;


    /** Native methods, implemented in jni folder */
    private static native void createEngine(int sample_rate, int buffer_size);
    private static native void setAzimuth(float new_azimuth);
    private static native void setPitch(float new_pitch);
    private static native void setRoll(float new_roll);
    private static native void setSoundEngine(int engine);
    private static native void shutdown();
    private static native void pause(int pause);
    private static native void setLoudnessSigmoidParameters(float slopeA,
                                                            float posA,
                                                            float slopeB,
                                                            float posB,
                                                            float freqLeft,
                                                            float freqRight,
                                                            float volLeft,
                                                            float volRight,
                                                            float rampLeft,
                                                            float rampRight);

    //ramp speed is how quickly target volume will be reached:
    // if it is 1, then it will take 1 second to go from 0 to 1 in volume
    // if it is 2, then it will take half a second
    // if it is 4, then quarter and so on...

    /** Load jni .so on initialization */
    static {
        System.loadLibrary("native-audio-jni");
    }


    public boolean testModeActive = false;

    public void initializeEngine(Context context) {
        //get most efficient sample rate and buffer size
        //https://www.youtube.com/watch?v=d3kfEeMZ65c
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String rate = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String size = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        final int sampleRate = Integer.parseInt(rate);
        final int bufferSize = Integer.parseInt(size);
        copyWavs(context);
        setSoundEngine(DEFAULT_SOUND_ENGINE);
        createEngine(sampleRate,bufferSize);
    }

    public void pauseEngine(boolean paused) {
        if (paused) {
            pause(1);
        } else {
            pause(0);
        }
    }

    public void setTransformedAzimuth(float transformedAzimuth) {
        setAzimuth(transformedAzimuth);
    }




    public void testMode(boolean on) {
        testModeActive = on;
        if (on) {
            setSoundEngine(TEST_SOUND_ENGINE);
        } else {
            setSoundEngine(DEFAULT_SOUND_ENGINE);
        }
    }

    public void stopEngine() {
        shutdown();
    }

    public void setParameters(float slopeA,
                              float posA,
                              float slopeB,
                              float posB,
                              float freqLeft,
                              float freqRight,
                              float volLeft,
                              float volRight,
                              float rampLeft,
                              float rampRight) {
        setLoudnessSigmoidParameters(slopeA, posA,
                slopeB, posB,
                freqLeft, freqRight,
                volLeft, volRight,
                rampLeft, rampRight);
    }

    protected void copyWavs(Context context) {
        try {
            copyWavFile(context, "leftsoundfile.wav");
            copyWavFile(context, "rightsoundfile.wav");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void copyWavFile(Context activity, String filename)
            throws IOException {

        InputStream is = activity.getAssets().open(filename);

        // Destination
        File outFile = new File(activity.getFilesDir(), filename);

        Log.i("SoundGenerator", "retrieveFromAssets( .. ) copying "
                + filename
                + " to "
                + outFile.getParent());

        FileOutputStream fos = new FileOutputStream(outFile);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        // Close the streams
        fos.flush();
        fos.close();
        is.close();

    }


}

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
public abstract class SoundGenerator {


    private static double PIBy180 = Math.PI / 180;
    private static SoundGenerator mInstance;

    public static SoundGenerator getInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new NativeSoundGenerator(); // Samsung or Native SoundGenerator();
            return mInstance;
        }
    }

    public boolean testModeActive = false;
    private float azimuthDistortionCentre = 0;
    private double azimuthDisortionCentreXRads = 0;
    private double azimuthDisortionCentreYRads = 0;
    private float azimuthDistortionWidth = 0;
    private double azimuthDistortionWidthRads = 0;
    private float azimuthDistortionAmplitude = 0;
    private float azimithConstant = 0;
    private float azimuthMultiplier = 1;
    private float currentAzimuth = 0;
    public float lastAzimuthSent = 0;

    public abstract void initializeEngine(Context context);
    public abstract void pauseEngine(boolean paused);
    public abstract void setTransformedAzimuth(float transformedAzimuth);
    public abstract void testMode(boolean on);
    public abstract void stopEngine();
    public abstract void setParameters(float slopeA,
                                       float posA,
                                       float slopeB,
                                       float posB,
                                       float freqLeft,
                                       float freqRight,
                                       float volLeft,
                                       float volRight,
                                       float rampLeft,
                                       float rampRight);

    public void setDistortionParameters(float constant, float multiplier,
                                               float distortionAmplitude, float distortionCentre,
                                               float distortionWidth) {
        azimithConstant = constant;
        azimuthMultiplier = multiplier;
        azimuthDisortionCentreXRads = Math.cos(distortionCentre * PIBy180);
        azimuthDisortionCentreYRads = Math.sin(distortionCentre * PIBy180);
        azimuthDistortionWidth = distortionWidth;
        azimuthDistortionWidthRads = distortionWidth * PIBy180;
        azimuthDistortionAmplitude = distortionAmplitude;
        azimuthDistortionCentre = distortionCentre;
    }

    public float[] getParams() {
        float[] params =  {azimuthDistortionAmplitude, azimuthDistortionCentre, azimuthDistortionWidth};
        return params;
    }




    public void updateAzimuth(float new_azimuth) {
        //transform azimuth
        currentAzimuth = new_azimuth;
        float transformed_azimuth = new_azimuth * azimuthMultiplier;

        transformed_azimuth += azimithConstant;
        double original_azimuth_rads = new_azimuth * PIBy180;
        double x = Math.cos(original_azimuth_rads) * azimuthDisortionCentreXRads;
        double y = Math.sin(original_azimuth_rads) * azimuthDisortionCentreYRads;
        double dot_product_rads = Math.acos(x + y);
        double addition = azimuthDistortionAmplitude * Math.max(azimuthDistortionWidthRads - dot_product_rads,0) / azimuthDistortionWidthRads;

        transformed_azimuth += addition;

        //Java % is remainder not modulus
        transformed_azimuth += 180;
        transformed_azimuth = transformed_azimuth % 360;
        transformed_azimuth = transformed_azimuth < 0 ? transformed_azimuth + 360 : transformed_azimuth;

        lastAzimuthSent = transformed_azimuth;
        //Log.v("Azimuth Update","Real azimuth in: " + String.valueOf(currentAzimuth) + " Distorted azimuth: " + String.valueOf(lastAzimuthSent));


        setTransformedAzimuth(transformed_azimuth);
    }


    public void updatePitch(float new_pitch) {
        setTransformedAzimuth(new_pitch);
    }
    public void updateRoll(float new_roll) {
        setTransformedAzimuth(new_roll);
    }
    public float getAzimuthConstant() { return azimithConstant; }
    public float getAzimuthMultiplier() { return azimuthMultiplier; }
}

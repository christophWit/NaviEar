
#include "native-audio-jni.h"
#include "native-audio.h"

void Java_compasssounds_compasssounds_NativeSoundGenerator_setSoundEngine(JNIEnv* env, jclass clazz, jint engine_to_use) {
    int f = (int) engine_to_use;
    setEngine(f);
}

//called to update azimuth
void Java_compasssounds_compasssounds_NativeSoundGenerator_setAzimuth(JNIEnv* env, jclass clazz, jfloat new_azmuth)
{

    double az = new_azmuth;
    setAzimuth(new_azmuth);
}

//called to update roll
void Java_compasssounds_compasssounds_NativeSoundGenerator_setRoll(JNIEnv* env, jclass clazz, jfloat new_roll)
{
    setRoll(new_roll);
}

//called to update pitch
void Java_compasssounds_compasssounds_NativeSoundGenerator_setPitch(JNIEnv* env, jclass clazz, jfloat new_pitch)
{
    setPitch(new_pitch);
}

void Java_compasssounds_compasssounds_NativeSoundGenerator_pause(JNIEnv* env, jclass clazz, jint pauseSetting) {
    int pause_int = pauseSetting;
    setPause(pause_int);

}

void Java_compasssounds_compasssounds_NativeSoundGenerator_setLoudnessSigmoidParameters(JNIEnv* env, jclass clazz,
                                                                                  jfloat slopeA,
                                                                                  jfloat posA,
                                                                                  jfloat slopeB,
                                                                                  jfloat posB,
                                                                                  jfloat freqLeft,
                                                                                  jfloat freqRight,
                                                                                  jfloat volLeft,
                                                                                  jfloat volRight,
                                                                                  jfloat rampLeft,
                                                                                  jfloat rampRight) {

    setLoudnessSigmoidParameters((double) slopeA,
                                  (double) posA,
                                  (double) slopeB,
                                  (double) posB,
                                  (double) freqLeft,
                                  (double) freqRight,
                                  (double) volLeft,
                                  (double) volRight,
                                  (double) rampLeft,
                                  (double) rampRight);
}


// create the engine and output mix objects
void Java_compasssounds_compasssounds_NativeSoundGenerator_createEngine(JNIEnv* env, jclass clazz, jint sample_rate, jint buffer_size)
{
    int sr = (int)sample_rate;
    int bs = (int)buffer_size;
    createEngine(sr, bs);
}

// shut down the native audio system
void Java_compasssounds_compasssounds_NativeSoundGenerator_shutdown(JNIEnv* env, jclass clazz)
{
    shutDown();
}


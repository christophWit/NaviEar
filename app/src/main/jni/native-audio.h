#pragma once


#define PI 3.14159265
#define TWOPI 6.2831853
#define CONV16BIT 32768
#define CONVMYFLT (1./32768.)
#define BUFFERLOGSIZE 5000
#define LOG_TAG    "CompassSoundsNativeAudio"
#define ALOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#include <sys/types.h>
#include <stdio.h>
#include <assert.h>
#include <jni.h>
#include <string.h>
#include <sys/time.h>
#include <math.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <stdio.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "wav-engine.h"


typedef enum { false, true } bool;

typedef struct {
    short left;
    short right;
} stereo;

typedef struct _log_time {
    long long start;
    long long end;
    int azimuthUpdates;
    int pitchUpdates;
    int rollUpdates;
} log_time;


// input variables
extern double latestAzimuth;
extern double latestPitch;
extern double latestRoll;
extern bool pause;

//sound buffer variables
extern int bufferFrameSize;
extern int sampleRate;
extern stereo *stereoBuffer;

void setEngine(int f);

void setAzimuth(double new_azimuth);
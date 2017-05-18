#include "native-audio.h"
#include "loudness-parameters.h"
#include "sine-engine.h"
#include "wav-engine.h"

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;
static SLObjectItf outputMixObject = NULL;
static SLObjectItf bqPlayerObject = NULL;
static SLPlayItf bqPlayerPlay;
static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;

// buffers
static stereo testBuffer[12000];
stereo *stereoBuffer;


// input variables
double latestAzimuth = 0;
double latestPitch = 0;
double latestRoll = 0;

int bufferFrameSize;
int sampleRate;


//pitch function variables
static double freq = 440;
static double freq2 = 440;
static double phase = 0.0;
static double phase2 = 0.0;
static double coeff = 0; // arbitrary coefficient to convert azimuth to frequency


//playing variables
bool pause = false;
static int soundEngine = 4;

//logging stuff
static log_time *bufferLog;
static int bufferqueueindex = 0;
static int azimuthUpdates;
static int pitchUpdates;
static int rollUpdates;

//latency test variables
static double latencyTestQuietAzimuth = 0;
static int latencyTestThreshold = 1;

void setEngine(int f) {
    soundEngine = f;
}

void fillBuffer() {
    if (soundEngine == 1) {
        loudnessSigmoidEngineFillBuffer();
    } else if (soundEngine == 2) {
        pitchSoundEngineFillBuffer();
    } else if (soundEngine == 3) {
        latencyTestEngineFillBuffer();
    } else if (soundEngine == 4) {
        wavEngineFillBuffer();
    }
}


void writeLogFile() {
    FILE* file = fopen("/sdcard/bufferqueue.txt","w+");

    if (file != NULL)
    {
        fprintf(file,"start,end,azimuthUpdates,pitchUpdates,rollUpdates\n");
        int i;
        for (i = 0; i < bufferqueueindex; ++i) {
            fprintf(file,"%lld,%lld,%i,%i,%i\n", bufferLog[i].start,
                                                 bufferLog[i].end,
                                                 bufferLog[i].azimuthUpdates,
                                                 bufferLog[i].pitchUpdates,
                                                 bufferLog[i].rollUpdates);
        }
        fflush(file);
        fclose(file);
    }
}

long long currentTimeInMilliseconds()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return ((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
}

void latencyTestEngineFillBuffer() {

    int playToneThisBuffer = 0;
    if (latestAzimuth > (latencyTestQuietAzimuth + latencyTestThreshold)  || latestAzimuth < (latencyTestQuietAzimuth - latencyTestThreshold)) {
        latencyTestQuietAzimuth = latestAzimuth;
        playToneThisBuffer = 1;
    }

    int frame;
    for(frame = 0; frame < bufferFrameSize; frame++) {
        stereoBuffer[frame].left = (short) (sin(phase) * CONV16BIT * playToneThisBuffer);
        stereoBuffer[frame].right = (short) (sin(phase) * CONV16BIT * playToneThisBuffer);
        phase += (TWOPI * 440 / (double)sampleRate);
    }
}


void pitchSoundEngineFillBuffer() {

    double d1 = fabs(-3.8951);

    int frame;
    for(frame = 0; frame < bufferFrameSize; frame++) {
        coeff = 2 * (latestAzimuth > 180 ? (360 - latestAzimuth) : latestAzimuth);;
        freq = 440 + coeff;
        freq2 = 440 + (180 - coeff);
        //freq = 440;
        //freq = 660;
        //ALOG("pcm %f",pcm);
        stereoBuffer[frame].left = (short) (sin(phase) * CONV16BIT);
        stereoBuffer[frame].right = (short) (sin(phase2) * CONV16BIT);

        phase += (TWOPI * freq / (double)sampleRate);
        phase2 += (TWOPI * freq2 / (double)sampleRate);
    }
}





void setAzimuth(double new_azimuth) {

    latestAzimuth = new_azimuth;
    azimuthUpdates++; //counting the number of sensor updates for each calback to log threading
}

void setRoll(float new_roll) {
    latestRoll = new_roll;
    rollUpdates++; //counting the number of sensor updates for each calback to log threading
}

void setPitch(float new_pitch) {
    latestAzimuth = new_pitch;
    pitchUpdates++; //counting the number of sensor updates for each calback to log threading
}

// this callback handler is called every time a buffer finishes playing
// according to google, for optimal latency processing should be done here
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    fillBuffer();
    if (bqPlayerBufferQueue != NULL) {
        (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, stereoBuffer, bufferFrameSize * sizeof(stereo));
    }
    sched_yield();
}

void updateBufferingLog(long long received, long long finished) {
    if (bufferqueueindex < BUFFERLOGSIZE) {
        bufferLog[bufferqueueindex].start = received;
        bufferLog[bufferqueueindex].end = finished;
        bufferLog[bufferqueueindex].azimuthUpdates = azimuthUpdates;
        bufferLog[bufferqueueindex].pitchUpdates = pitchUpdates;
        bufferLog[bufferqueueindex].rollUpdates = rollUpdates;
        bufferqueueindex++;
    }
    azimuthUpdates = 0; //reset count
    pitchUpdates = 0; //reset count
    rollUpdates = 0; //reset count
}



void createEngine(int sample_rate, int buffer_size) {
    ALOG("Loading wave buffers");

    //load the soundfile.wav
    loadWaveBuffers();


    //currentEnv = env;
    sampleRate = sample_rate;
    bufferFrameSize = buffer_size;

    ALOG("Creating Engine");
    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Engine created");

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Realized");

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Got interface");

    // configure audio source
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    ALOG("Configured buffer queue");

    int sl_sampleRate = sampleRate * 1000;
    ALOG("Sample rate: %d", sampleRate);

    //calculate how long in ms the buffer is
    float bs = (float)bufferFrameSize;
    float srate = (float)sampleRate;
    float bufms = 1000 * bufferFrameSize / srate;
    ALOG("Buffer ms: %f", bufms);

    // create output mix,
    const SLInterfaceID ids[1] = {SL_IID_NULL};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Created Output Mix");

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Realized");


    int speakers = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;


    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, sl_sampleRate,
        SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
        speakers, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID apids[] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean apreq[] = {SL_BOOLEAN_TRUE};
    ALOG("Creating Audio Player");
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc, &audioSnk,
                                                1, apids, apreq);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Created Audio Player");

    // realize the player
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Realized");

    // get the play interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Got play interface");

    // get the buffer queue interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                             &bqPlayerBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Got Buffer queue interface");

    // register callback on the buffer queue
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Callback registered");

    // set the player's state to playing
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    ALOG("Playstate set");

    if((stereoBuffer = (short *) calloc(bufferFrameSize, sizeof(stereo))) == NULL) {
        ALOG("Stereo buffer memory allocation error");
        return;
    }

    if ((bufferLog = calloc(BUFFERLOGSIZE, sizeof(log_time))) == NULL) {
        ALOG("Buffer log queue memory allocation error");
        return;
    }


    //test tone
    // initialize the test tone to be a sine sweep from 441 Hz to 882 Hz
    unsigned nframes = sizeof(testBuffer) / sizeof(testBuffer[0]);
    float nframes_ = (float) nframes;
    SLuint32 i;
    for (i = 0; i < nframes; ++i) {
        float i_ = (float) i;
        float pcm_ = sin((i_ * (1.0f + 0.5f * (i_ / nframes_)) * 0.01 * M_PI * 2.0));
        int pcm = (int) (pcm_ * 32766.0 * 1);
        testBuffer[i].right = pcm;
        testBuffer[i].right = pcm;
    }

    //unpause
    ALOG("UNPAUSING IN STARTUP");
    pause == false;
    (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, testBuffer, sizeof(testBuffer));
}


void shutDown() {
    writeLogFile();

    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (bqPlayerObject != NULL) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        bqPlayerBufferQueue = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    if (stereoBuffer != NULL) {
        free(stereoBuffer);
        stereoBuffer= NULL;
    }

    if (bufferLog != NULL) {
        free(bufferLog);
        bufferLog = NULL;
    }
}

void setPause(int pause_int) {
    if (pause_int == 1) {
        actualLeftLoudness = 0.0;
        actualRightLoudness = 0.0;
        ALOG("PAUSING IN SETPAUSE");
        pause = true;
    } else {
        if (pause == true) {
            ALOG("UNPAUSING IN SETPAUSE");
            pause = false;
            fillBuffer();
            (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, stereoBuffer, bufferFrameSize * sizeof(stereo));
        }
    }
}



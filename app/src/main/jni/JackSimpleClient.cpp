#include "JackSimpleClient.h"
#include "mylog.h"
#define CONVMYFLT (1./32768.)

namespace android {

    jack_client_t *JackSimpleClient::jackClient = NULL;


    JackSimpleClient::JackSimpleClient(){
        leftPort = NULL;
        rightPort = NULL;
        sinePhase = 0;
        sampleRate = 48000;
        latencyTestQuietAzimuth = 0;
        latencyTestThreshold = 0.1;
        latencyTestModeOn = 0;
        currentlyPlaying = 1;
        loudnessSigmoidAMin = 0.07;
        loudnessSigmoidBMin = 0.00;
        loudnessSigmoidARange = 0.911;
        loudnessSigmoidBRange = 0.989;

        loudnessSigmoidASlope = 13;
        loudnessSigmoidAPosition = 0.2;
        loudnessSigmoidBSlope = 30;
        loudnessSigmoidBPosition = 0.35;
        loudnessFreqLeft = 440;
        loudnessFreqRight = 880;
        loudnessLeftMult = 1.0;
        loudnessRightMult = 1.0;
        loudnessRampSpeedLeft = 440 / 96000;
        loudnessRampSpeedRight = 880 / 96000;

        actualLeftLoudness = 0.0;
        actualRightLoudness = 0.0;
    }

    JackSimpleClient::~JackSimpleClient(){
        jackClient = NULL;
    }

    int JackSimpleClient::processFrames (jack_nframes_t frames, void *arg)
    {
        JackSimpleClient *thiz = (JackSimpleClient*)arg;

        if (thiz->latencyTestModeOn == 1) {
            thiz->fillLatencyTestBuffer(frames);
        } else {
            thiz->fillWaveBuffer(frames);
        }

        return 0;
    }

    void JackSimpleClient::fillSineBuffer(jack_nframes_t frames) {
        jack_default_audio_sample_t *leftOut = (jack_default_audio_sample_t*)jack_port_get_buffer (leftPort, frames);
        jack_default_audio_sample_t *rightOut = (jack_default_audio_sample_t*)jack_port_get_buffer (rightPort, frames);

        double phaseSpeed = 2. * 3.14159265 * (440. + currentAzimuth) / sampleRate;

        for(unsigned int i=0; i<frames; i++ )
        {
            leftOut[i] = sin(sinePhase);
            rightOut[i] = sin(sinePhase);
            sinePhase += (phaseSpeed);
        }
    }

    void JackSimpleClient::fillLatencyTestBuffer(jack_nframes_t frames) {

        float playToneThisBuffer = 0;
        if (currentAzimuth > (latencyTestQuietAzimuth + latencyTestThreshold)  || currentAzimuth < (latencyTestQuietAzimuth - latencyTestThreshold)) {
            latencyTestQuietAzimuth = currentAzimuth;
            playToneThisBuffer = 1;
        }

        jack_default_audio_sample_t *leftOut = (jack_default_audio_sample_t*)jack_port_get_buffer (leftPort, frames);
        jack_default_audio_sample_t *rightOut = (jack_default_audio_sample_t*)jack_port_get_buffer (rightPort, frames);

        double phaseSpeed = 2. * 3.14159265 * 440. / sampleRate;

        for(unsigned int i=0; i<frames; i++ )
        {
            leftOut[i] = sin(sinePhase) * playToneThisBuffer;
            rightOut[i] = sin(sinePhase) * playToneThisBuffer;
            sinePhase += (phaseSpeed);
        }
    }

    void JackSimpleClient::fillWaveBuffer(jack_nframes_t frames) {
        unsigned int frame;
        double azimuthLeft = currentAzimuth;
        double azimuthRight = (360 - currentAzimuth);

        double leftLoudness = loudnessSigmoidFunc(azimuthLeft) * (double)currentlyPlaying;
        double rightLoudness = loudnessSigmoidFunc(azimuthRight) * (double)currentlyPlaying;

        //ALOG("JackSimpleClient:: left: %f, right: %f, left location: %d", leftLoudness, rightLoudness, leftWavLocation);
        if (leftLoudness < 0.05) { leftLoudness = 0; }
        if (rightLoudness < 0.05) { rightLoudness = 0; }

        //ALOG("JackSimpleClient:: left: %f, right: %f, left location: %d", leftLoudness, rightLoudness, leftWavLocation);

        jack_default_audio_sample_t *leftOut = (jack_default_audio_sample_t*)jack_port_get_buffer (leftPort, frames);
        jack_default_audio_sample_t *rightOut = (jack_default_audio_sample_t*)jack_port_get_buffer (rightPort, frames);

        for(frame = 0; frame < frames; frame++) {

            if (leftLoudness > (actualLeftLoudness + loudnessRampSpeedLeft)) {
                actualLeftLoudness += loudnessRampSpeedLeft;
            }


            if (leftLoudness < actualLeftLoudness) {
                actualLeftLoudness -= loudnessRampSpeedLeft;
            }

            if (rightLoudness > (actualRightLoudness + loudnessRampSpeedRight)) {
                actualRightLoudness += loudnessRampSpeedRight;
            }

            if (rightLoudness < actualRightLoudness) {
                actualRightLoudness -= loudnessRampSpeedRight;
            }


            if (actualLeftLoudness > 1) { actualLeftLoudness = 1; }
            if (actualLeftLoudness < 0.0) { actualLeftLoudness = 0; }
            if (actualRightLoudness > 1) { actualRightLoudness = 1; }
            if (actualRightLoudness < 0.0) { actualRightLoudness = 0; }
            //need to interpolate loudness change over buffer (can try shorter interpolation times

            //short sample = b1b2;
            short leftPCM = leftWav.waveFileData[leftWavLocation + 1] <<8| leftWav.waveFileData[leftWavLocation ];
            short rightPCM = rightWav.waveFileData[rightWavLocation + 1] <<8| rightWav.waveFileData[rightWavLocation ];

            //leftOut[frame] = actualLeftLoudness * ((double)leftPCM);
            //rightOut[frame] = actualRightLoudness * ((double)rightPCM);
            leftOut[frame] =  actualLeftLoudness * ((double)leftPCM) * CONVMYFLT;
            rightOut[frame] = actualRightLoudness * ((double)rightPCM * CONVMYFLT);

            leftWavLocation += 2;
            rightWavLocation += 2;

            if (leftWavLocation >= leftWav.waveFileHeader.dataSize)
            {
                leftWavLocation = 0;
            }

            if (rightWavLocation >= rightWav.waveFileHeader.dataSize)
            {
                rightWavLocation = 0;
            }
        }
    }

    int JackSimpleClient::setUp (int argc, char *argv[])
    {
        LOGD("setUp argc %d", argc);
        for(int i = 0;i< argc; i++){
            LOGD("setup argv %s", argv[i]);
        }


        // make a alias
        char* name = strrchr(argv[0], '.');
        if(name == NULL){
            name = argv[0];
        }
        jackClient = jack_client_open (name, JackNullOption, NULL, NULL);
        if (jackClient == NULL) {
            LOGD("Couldnt open jackClient");
            return APA_RETURN_ERROR;
        }

        // sets the callback for processSine
        jack_set_process_callback (jackClient, processFrames, this);

        leftPort = jack_port_register (jackClient, "out_left",
                          JACK_DEFAULT_AUDIO_TYPE,
                          JackPortIsOutput, 0);

        if(leftPort == NULL){
            LOGD("Coudlnt open leftPort");
            return APA_RETURN_ERROR;
        }

        rightPort = jack_port_register (jackClient, "out_right",
                              JACK_DEFAULT_AUDIO_TYPE,
                              JackPortIsOutput, 0);

        if(rightPort == NULL) {
            LOGD("Coudlnt open rightPort");
            return APA_RETURN_ERROR;
        }



        //Load wavs


        LOGD("SUCCESSFUL SETUP");
        return APA_RETURN_SUCCESS;
    }

    int JackSimpleClient::tearDown(){
        jack_client_close (jackClient);
        leftWav.release();
        rightWav.release();
        return APA_RETURN_SUCCESS;
    }

    int JackSimpleClient::activate(){
        jack_activate (jackClient);
        LOGD("JackSimpleClient::activating...");

        const char* leftfnameptr = "/data/data/compasssounds.compasssounds/files/leftsoundfile.wav";
        const char* rightfnameptr = "/data/data/compasssounds.compasssounds/files/rightsoundfile.wav";
        leftWav.readWav(leftfnameptr);
        rightWav.readWav(rightfnameptr);
        LOGD("JackSimpleClient::wavs now loaded...");
        leftWavLocation = 0;
        rightWavLocation = 0;

        const char **systemInputs = jack_get_ports (jackClient, NULL, NULL,
                    JackPortIsPhysical|JackPortIsInput);
        if (systemInputs == NULL) {
            LOGD("system input port is null\n");
            return APA_RETURN_ERROR;
        }

        jack_connect (jackClient, jack_port_name (leftPort), systemInputs[0]);
        jack_connect (jackClient, jack_port_name (rightPort), systemInputs[1]);

        free (systemInputs);

        return APA_RETURN_SUCCESS;
    }

    void JackSimpleClient::updateAzimuth(float azimuth) {
        currentAzimuth = azimuth;
    }

    void JackSimpleClient::setLatencyTestModeOn(int on) {
        LOGD("JackSimpleClient::latency test mode set to: %d", on);
        latencyTestModeOn = on;
    }

    void JackSimpleClient::setPauseModeOn(int on) {
        LOGD("JackSimpleClient::pause mode set to: %d", on);
        if (on == 1) {
            currentlyPlaying = 0;
        } else {
            currentlyPlaying = 1;
        }
    }

    int JackSimpleClient::deactivate(){
        LOGD("JackSimpleClient::deactivate");
        jack_deactivate (jackClient);
        return APA_RETURN_SUCCESS;
    }

    int JackSimpleClient::transport(TransportType type) {
        return APA_RETURN_SUCCESS;
    }

    int JackSimpleClient::sendMidi(char* midi){
        LOGD("Midi recieved");
        return APA_RETURN_SUCCESS;
    }


    double JackSimpleClient::sigmoid(double x, double a, double c) {
        return (1 / (1 + exp(-c * (x - a))));
    }


    double JackSimpleClient::loudnessSigmoidFunc(double azimuth) {
        double azimuth_normalised = azimuth / 360;
        if (azimuth_normalised < 0.5) {
            return (sigmoid(azimuth_normalised,loudnessSigmoidAPosition, loudnessSigmoidASlope) - loudnessSigmoidAMin ) / loudnessSigmoidARange;
        } else {
            return (sigmoid((1 - azimuth_normalised),loudnessSigmoidBPosition, loudnessSigmoidBSlope) - loudnessSigmoidBMin ) / loudnessSigmoidBRange;
        }
    }

    void JackSimpleClient::setLoudnessSigmoidParameters(double slopeA,
                                      double posA,
                                     double slopeB,
                                      double posB,
                                      double freqLeft,
                                      double freqRight,
                                      double volLeft,
                                      double volRight,
                                      double rampLeft,
                                      double rampRight) {

        loudnessSigmoidASlope = (double)slopeA;
        loudnessSigmoidAPosition = (double)posA;
        loudnessSigmoidBSlope = (double)slopeB;
        loudnessSigmoidBPosition = (double)posB;
        loudnessSigmoidAMin = sigmoid(0, loudnessSigmoidAPosition,loudnessSigmoidASlope );
        loudnessSigmoidBMin = sigmoid(0, loudnessSigmoidBPosition,loudnessSigmoidBSlope );
        loudnessSigmoidARange = sigmoid(0.5, loudnessSigmoidAPosition,loudnessSigmoidASlope ) - loudnessSigmoidAMin;
        loudnessSigmoidBRange = sigmoid(0.5, loudnessSigmoidBPosition,loudnessSigmoidBSlope ) - loudnessSigmoidBMin;
        loudnessFreqLeft = (double)freqLeft;
        loudnessFreqRight = (double)freqRight;
        loudnessLeftMult = (double) volLeft;
        loudnessRightMult = (double) volRight;
        loudnessRampSpeedRight = (double) rampRight / sampleRate;
        loudnessRampSpeedLeft = (double) rampLeft / sampleRate;

        //error check this stuff
        if (loudnessLeftMult > 1) { loudnessLeftMult = 1.0f; }
        if (loudnessLeftMult < 0) { loudnessLeftMult = 0.0f; }
        if (loudnessRightMult > 1) { loudnessRightMult = 1.0f; }
        if (loudnessRightMult < 0) { loudnessRightMult = 0.0f; }
        LOGD("JackSimpleClient::setLoudnessSigmoidParameters");
    }


};
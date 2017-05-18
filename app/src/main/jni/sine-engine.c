#include "sine-engine.h"
#include "native-audio.h"
#include "loudness-parameters.h"

double loudnessPhaseLeft = 0.0;
double loudnessPhaseRight = 0.0;


void loudnessSigmoidEngineFillBuffer() {
    int frame;
    double azimuthLeft = latestAzimuth;
    double azimuthRight = (360 - latestAzimuth);

    double leftLoudness = loudnessSigmoidFunc(azimuthLeft);
    double rightLoudness = loudnessSigmoidFunc(azimuthRight);
    double leftPhaseSpeed = TWOPI * loudnessFreqLeft / (double)sampleRate;
    double rightPhaseSpeed = TWOPI * loudnessFreqRight / (double)sampleRate;
    //ALOG("LoudnessRampSpeedLeft: %f, LoudnessRampSpeedRight: %f",loudnessRampSpeedLeft,loudnessRampSpeedRight);
    for(frame = 0; frame < bufferFrameSize; frame++) {

        if (leftLoudness > (actualLeftLoudness + loudnessRampSpeedLeft)) {
            actualLeftLoudness += loudnessRampSpeedLeft;
        }


        if (leftLoudness < (actualLeftLoudness - loudnessRampSpeedLeft)) {
            actualLeftLoudness -= loudnessRampSpeedLeft;
        }

        if (rightLoudness > (actualRightLoudness + loudnessRampSpeedRight)) {
            actualRightLoudness += loudnessRampSpeedRight;
        }

        if (rightLoudness < (actualRightLoudness - loudnessRampSpeedRight)) {
            actualRightLoudness -= loudnessRampSpeedRight;
        }

        if (actualLeftLoudness > 1) { actualLeftLoudness = 1; }
        if (actualLeftLoudness < 0) { actualLeftLoudness = 0; }
        if (actualRightLoudness > 1) { actualRightLoudness = 1; }
        if (actualRightLoudness < 0) { actualRightLoudness = 0; }
        //need to interpolate loudness change over buffer (can try shorter interpolation times
        stereoBuffer[frame].left = (short) (actualLeftLoudness * sin(loudnessPhaseLeft) * CONV16BIT * loudnessLeftMult);
        stereoBuffer[frame].right = (short) (actualRightLoudness * sin(loudnessPhaseRight) * CONV16BIT * loudnessRightMult);
        //stereoBuffer[frame].left = waveFileData[frame];
        loudnessPhaseLeft += (leftPhaseSpeed);
        loudnessPhaseRight += (rightPhaseSpeed);
    }
}
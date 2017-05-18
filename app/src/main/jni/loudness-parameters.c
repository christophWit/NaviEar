#include "loudness-parameters.h"
#include "native-audio.h"

//internal parameters
double loudnessSigmoidAMin = 0.07;
double loudnessSigmoidBMin = 0.00;
double loudnessSigmoidARange = 0.911;
double loudnessSigmoidBRange = 0.989;

//settable parameters
double loudnessSigmoidASlope = 30;
double loudnessSigmoidAPosition = 0.4;
double loudnessSigmoidBSlope = 14;
double loudnessSigmoidBPosition = 0.13;
double loudnessFreqLeft = 440;
double loudnessFreqRight = 880;
double loudnessLeftMult = 1.0;
double loudnessRightMult = 0.9;
double loudnessRampSpeedLeft = 440 / 96000;
double loudnessRampSpeedRight = 880 / 96000;

//loudness internal variables
double actualLeftLoudness = 0.0;
double actualRightLoudness = 0.0;


double sigmoid(double x, double a, double c) {
    return (1 / (1 + exp(-c * (x - a))));
}


double loudnessSigmoidFunc(double azimuth) {
    double azimuth_normalised = azimuth / 360;
    if (azimuth_normalised < 0.5) {
        return (sigmoid(azimuth_normalised,loudnessSigmoidAPosition, loudnessSigmoidASlope) - loudnessSigmoidAMin ) / loudnessSigmoidARange;
    } else {
        return (sigmoid((1 - azimuth_normalised),loudnessSigmoidBPosition, loudnessSigmoidBSlope) - loudnessSigmoidBMin ) / loudnessSigmoidBRange;
    }
}

void setLoudnessSigmoidParameters(double slopeA,
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
}
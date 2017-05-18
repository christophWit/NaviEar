#pragma once

//internal parameters
extern double loudnessSigmoidAMin;
extern double loudnessSigmoidBMin;
extern double loudnessSigmoidARange;
extern double loudnessSigmoidBRange;

//settable parameters
extern double loudnessSigmoidASlope;
extern double loudnessSigmoidAPosition;
extern double loudnessSigmoidBSlope;
extern double loudnessSigmoidBPosition;
extern double loudnessFreqLeft;
extern double loudnessFreqRight;
extern double loudnessLeftMult;
extern double loudnessRightMult;
extern double loudnessRampSpeedLeft;
extern double loudnessRampSpeedRight;

//loudness internal variables
extern double loudnessPhaseLeft;
extern double loudnessPhaseRight;
extern double actualLeftLoudness;
extern double actualRightLoudness;


double sigmoid(double x, double a, double c);
double loudnessSigmoidFunc(double azimuth);

void setLoudnessSigmoidParameters(double slopeA,
                                  double posA,
                                  double slopeB,
                                  double posB,
                                  double freqLeft,
                                  double freqRight,
                                  double volLeft,
                                  double volRight,
                                  double rampLeft,
                                  double rampRight);
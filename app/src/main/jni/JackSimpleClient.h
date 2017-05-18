
#ifndef ANDROID_JACK_SIMPLE_CLIENT_H
#define ANDROID_JACK_SIMPLE_CLIENT_H


#include <jack/jack.h>
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <signal.h>
#include <unistd.h>
#include "wave-file.h"

#include "IJackClientInterface.h"
#include "APACommon.h"

namespace android {

	class JackSimpleClient: public IJackClientInterface {

		public:
			JackSimpleClient();
			virtual ~JackSimpleClient();
			int setUp(int argc, char *argv[]);
			int tearDown();
			int activate();
			int deactivate();
			int transport(TransportType type);
			int sendMidi(char* midi);
			void updateAzimuth(float azimuth);
			void setLatencyTestModeOn(int on);
			void setPauseModeOn(int on);
			void setLoudnessSigmoidParameters(double slopeA, double posA,
													  double slopeB, double posB,
													  double freqLeft, double freqRight,
													  double volLeft, double volRight,
													  double rampLeft, double rampRight);
		private:
			//main callback
			static int processFrames (jack_nframes_t nframes, void *arg);

			//buffer fill routines
			void fillWaveBuffer(jack_nframes_t frames);
			void fillSineBuffer(jack_nframes_t frames);
			void fillLatencyTestBuffer(jack_nframes_t frames);

			//helper functions
			double sigmoid(double x, double a, double c);
            double loudnessSigmoidFunc(double azimuth);

            //jack objects
			jack_port_t * leftPort;
			jack_port_t * rightPort;
			static jack_client_t *jackClient; // jack client object

			//current azimuth + pauseed or playing
			float currentAzimuth;
			int currentlyPlaying;

			//sample rate
			float sampleRate;

			//sine wave variable
			double sinePhase;

			//latency test variables
			float latencyTestQuietAzimuth;
			float latencyTestThreshold;
			int latencyTestModeOn;

			//wave objects
			WaveFile leftWav;
			WaveFile rightWav;
			unsigned int leftWavLocation;
			unsigned int rightWavLocation;

			//internal parameters
			double loudnessSigmoidAMin;
			double loudnessSigmoidBMin;
			double loudnessSigmoidARange;
			double loudnessSigmoidBRange;

			//settable parameters
			double loudnessSigmoidASlope;
			double loudnessSigmoidAPosition;
			double loudnessSigmoidBSlope;
			double loudnessSigmoidBPosition;
			double loudnessFreqLeft;
			double loudnessFreqRight;
			double loudnessLeftMult;
			double loudnessRightMult;
			double loudnessRampSpeedLeft;
			double loudnessRampSpeedRight;

			//loudness internal variables
			double actualLeftLoudness;
			double actualRightLoudness;
	};

};

#endif // ANDROID_JACK_SIMPLE_CLIENT_H


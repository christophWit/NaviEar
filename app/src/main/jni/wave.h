
#include <stdio.h>
#include <IAPAInterface.h>
#include "JackSimpleClient.h"
#include "APACommon.h"
#include <string>
#include <stdlib.h>

namespace android {

class APAWave : IAPAInterface {

	public:
		APAWave();
		virtual ~APAWave();
		int init();
		int sendCommand(const char* command);
		IJackClientInterface* getJackClientInterface();
		int request(const char* what, const long ext1, const long capacity, size_t &len, void* data);
	private:
		JackSimpleClient mSimpleClient;
		int parseParams(std::string commandString);

		//commands
		std::string AZIMUTH;
        std::string PARAMS;
        std::string TESTMODEON;
        std::string TESTMODEOFF;
        std::string PAUSEON;
        std::string PAUSEOFF;
};

DECLARE_APA_INTERFACE(APAWave)

};



#include "wave.h"
#include <stdio.h>
#include "mylog.h"


#define PARAMCOUNT 10

namespace android {

    IMPLEMENT_APA_INTERFACE(APAWave)

    APAWave::APAWave(){
        AZIMUTH = std::string("AZIMUTH:");
        PARAMS = std::string("PARAMS:");
        TESTMODEON = std::string("TESTMODEON");
        TESTMODEOFF = std::string("TESTMODEOFF");
        PAUSEON = std::string("PAUSEON");
        PAUSEOFF = std::string("PAUSEOFF");
    }

    APAWave::~APAWave(){
    }

    int APAWave::init(){
        LOGD("APAWave initialized");
        return APA_RETURN_SUCCESS;
    }

    int APAWave::sendCommand(const char* command){

        std::string commandString = std::string(command);
        std::string::size_type i = commandString.find(AZIMUTH);

        if (i != std::string::npos) {
            commandString.erase(i, AZIMUTH.length());
            float azimuth = strtod(commandString.c_str(), NULL);
            mSimpleClient.updateAzimuth(azimuth);
            return APA_RETURN_SUCCESS;
        }


        i = commandString.find(PARAMS);
        if (i != std::string::npos) {
            return parseParams(commandString);
        }

        if (commandString.find(TESTMODEON) != std::string::npos) {
            mSimpleClient.setLatencyTestModeOn(1);
            return APA_RETURN_SUCCESS;
        }


        if (commandString.find(TESTMODEOFF) != std::string::npos) {
            mSimpleClient.setLatencyTestModeOn(0);
            return APA_RETURN_SUCCESS;
        }


        if (commandString.find(PAUSEON) != std::string::npos) {
            mSimpleClient.setPauseModeOn(1);
            return APA_RETURN_SUCCESS;
        }


        if (commandString.find(PAUSEOFF) != std::string::npos) {
            mSimpleClient.setPauseModeOn(0);
            return APA_RETURN_SUCCESS;
        }

        return APA_RETURN_SUCCESS;
    }

    int APAWave::parseParams(std::string commandString) {
        LOGD("Parsing parameter string: %s", commandString.c_str());
        std::string::size_type i = commandString.find(PARAMS);
        size_t pos = 0;
        std::string token;
        std::string delimiter = ":";
        commandString.erase(i, PARAMS.length());

        float params[PARAMCOUNT];
        unsigned int paramCount = 0;
        while ((pos = commandString.find(delimiter)) != std::string::npos) {
            token = commandString.substr(0, pos);

            if (paramCount < PARAMCOUNT) {
                params[paramCount] = strtod(token.c_str(), NULL);
                LOGD("Parsing parameter string: %s, parsed token: %s", commandString.c_str(), token.c_str());
                paramCount++;
            } else {
                LOGD("too many parameters");
            }
            commandString.erase(0, pos + delimiter.length());
        }

        //final parameter
        params[paramCount] = strtod(commandString.c_str(), NULL);
        LOGD("Final parsed token: %s", commandString.c_str(), token.c_str());

        if (paramCount != PARAMCOUNT - 1) {
            LOGD("Incorrect number of parameters, found %d expected %d",paramCount,PARAMCOUNT);
        }

        mSimpleClient.setLoudnessSigmoidParameters(params[0],params[1],
                                                   params[2],params[3],
                                                   params[4],params[5],
                                                   params[6],params[7],
                                                   params[8],params[9]);


        return APA_RETURN_SUCCESS;
    }

    IJackClientInterface* APAWave::getJackClientInterface(){
        return &mSimpleClient;
    }

    int APAWave::request(const char* what, const long ext1, const long capacity, size_t &len, void*data)
    {
        return APA_RETURN_SUCCESS;
    }

};

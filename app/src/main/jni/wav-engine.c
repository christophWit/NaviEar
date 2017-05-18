#include "wav-engine.h"
#include "native-audio.h"
#include "loudness-parameters.h"

//wave files
char* leftWaveFileData;
BasicWAVEHeader leftWaveFileHeader;
char* rightWaveFileData;
BasicWAVEHeader rightWaveFileHeader;

unsigned int leftDataLocation = 0;
unsigned int rightDataLocation = 0;

//WARNING: This Doesn't Check To See If These Pointers Are Valid
// This method is from http://www.gamedev.net/community/forums/topic.asp?topic_id=505152&whichpage=1&#3296091
char* readWav(char* filename, BasicWAVEHeader* header){
   ALOG("readWAVE() - reading file with path: %s",filename);
  char* buffer = 0;
  //FILE* file = fopen("/sdcard/accessiblemap/lake.wav","rb");
  FILE* file = fopen(filename,"rb");
  if (!file) {
	ALOG("readWAVE() - fopen failed");
	return 0;
  }

  if (fread(header,sizeof(BasicWAVEHeader),1,file)){

    ALOG("readWAVE() - loaded file riff: %s, wave %s, fmt %s, data %s",header->riff, header->wave, header->fmt, header->data);
    ALOG("readWAVE() - data size: %d", header->dataSize);


    if (!(//these things *must* be valid with this basic header
      memcmp("RIFF",header->riff,4) ||
      memcmp("WAVE",header->wave,4) ||
      memcmp("fmt ",header->fmt,4)  ||
      memcmp("data",header->data,4)
    )){
      buffer = (char*)malloc(header->dataSize);
      if (buffer){
        if (fread(buffer,header->dataSize,1,file)){
          fclose(file);
          return buffer;
        } else {
			ALOG("readWAVE() - fread (inner) failed");
		}
        free(buffer);
      } else {
		ALOG("readWAVE() - buffer not allocated");
	  }
    }else{
		ALOG("readWAVE() - header not valid");
	}
  }else{
	ALOG("readWAVE() - fread failed");
  }
  fclose(file);
  return 0;
}

int loadWaveBuffers() {

    const char* leftfnameptr = "/data/data/compasssounds.compasssounds/files/leftsoundfile.wav";
    const char* rightfnameptr = "/data/data/compasssounds.compasssounds/files/rightsoundfile.wav";
	leftWaveFileData = (short*)readWav(leftfnameptr,&leftWaveFileHeader);
	if (leftWaveFileData){
		//Now We've Got A Wave In Memory, Time To Turn It Into A Usable Buffer
        ALOG("Loaded left wave file");
		//free(leftWaveFileData);

		unsigned int i = 0;
		for(i = 0; i < leftWaveFileHeader.dataSize; i++)
		{
		    //ALOG("%u, %d", i, (short*)leftWaveFileData[i]);
		}
	} else ALOG("Failed to read left wave file");

    rightWaveFileData = (short*)readWav(rightfnameptr,&rightWaveFileHeader);
	if (rightWaveFileData){
		//Now We've Got A Wave In Memory, Time To Turn It Into A Usable Buffer
        ALOG("Loaded right wave file");
		//free(rightWaveFileData);
	} else ALOG("Failed to read right wave file");

	leftDataLocation = 0;
	rightDataLocation = 0;
	return 0;
}

void wavEngineFillBuffer() {
    int frame;

    double azimuthLeft = latestAzimuth;
    double azimuthRight = (360 - latestAzimuth);


    double pauseMultiplier = 1;

    if (pause == true) {
        pauseMultiplier = 0;
    }

    double leftLoudness = loudnessSigmoidFunc(azimuthLeft) * pauseMultiplier;
    double rightLoudness = loudnessSigmoidFunc(azimuthRight) * pauseMultiplier;

    if (leftLoudness < 0.07) { leftLoudness = 0; }
    if (rightLoudness < 0.07) { rightLoudness = 0; }

    for(frame = 0; frame < bufferFrameSize; frame++) {

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
        short leftPCM = leftWaveFileData[leftDataLocation + 1] <<8| leftWaveFileData[leftDataLocation ];
        short rightPCM = rightWaveFileData[rightDataLocation + 1] <<8| rightWaveFileData[rightDataLocation ];

        stereoBuffer[frame].left = ((short) (actualLeftLoudness * leftPCM));
        stereoBuffer[frame].right = ((short) (actualRightLoudness * rightPCM));

        leftDataLocation += 2;
        rightDataLocation += 2;

        if (leftDataLocation >= leftWaveFileHeader.dataSize)
        {
            leftDataLocation = 0;
        }

        if (rightDataLocation >= rightWaveFileHeader.dataSize)
        {
            rightDataLocation = 0;
        }
    }

}
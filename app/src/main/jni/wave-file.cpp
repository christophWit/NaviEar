#include "wave-file.h"
#include "mylog.h"
#include <stdio.h>
#include <string.h>

namespace android {


//WARNING: This Doesn't Check To See If These Pointers Are Valid
// This method is from http://www.gamedev.net/community/forums/topic.asp?topic_id=505152&whichpage=1&#3296091
int WaveFile::readWav(const char* filename){
  ALOG("Reading file with path: %s",filename);

  waveFileData = readWav(filename,&waveFileHeader);
  if (waveFileData){
       ALOG("Loaded wave file");
  } else ALOG("Failed to read wave file");
  return 0;
}


//WARNING: This Doesn't Check To See If These Pointers Are Valid
// This method is from http://www.gamedev.net/community/forums/topic.asp?topic_id=505152&whichpage=1&#3296091
char* WaveFile::readWav(const char* filename, BasicWAVEHeader* header){
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

void WaveFile::release() {
    if (waveFileData != NULL) {
        free(waveFileData);
    }
}

};

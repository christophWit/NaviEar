#pragma once

typedef struct {
  char  riff[4];//'RIFF'
  unsigned int riffSize;
  char  wave[4];//'WAVE'
  char  fmt[4];//'fmt '
  unsigned int fmtSize;
  unsigned short format;
  unsigned short channels;
  unsigned int samplesPerSec;
  unsigned int bytesPerSec;
  unsigned short blockAlign;
  unsigned short bitsPerSample;
  char  data[4];//'data'
  unsigned int dataSize;
}BasicWAVEHeader;


int loadWaveBuffers();

char* readWav(char* filename,BasicWAVEHeader* header);

void wavEngineFillBuffer();
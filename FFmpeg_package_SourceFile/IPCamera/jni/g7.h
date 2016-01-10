#ifndef _G7_H
#define _G7_H

// #ifdef _cplusplus
// extern "C" {
// #endif

extern unsigned char _l2A[2048];
extern signed short _A2l[256];
extern unsigned char _l2u[4096];
extern signed short _u2l[256];



unsigned char linear2alaw(signed short l);
signed short alaw2linear(unsigned char a);
unsigned char linear2ulaw(signed short l);


// #ifdef _cplusplus
// extern "C" {
// #endif

#endif
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

#include "g711.h"


//Have a try->short->char->short->Better to Use short!!!
//jint Java_com_tutk_sample_AVAPI_G726Decoder_VoiceEncode(JNIEnv* env, jobject thiz, jbyteArray SpeechData, jbyteArray BitstreamData)
//{
//	jbyte * speech = (jbyte*)(*env)->GetByteArrayElements(env, SpeechData, 0);
//	jbyte * bitstream= (jbyte*)(*env)->GetByteArrayElements(env, BitstreamData, 0);
//
//	g726_Encode(speech,bitstream);
//
//	(*env)->ReleaseByteArrayElements(env, SpeechData, speech, 0);
//	(*env)->ReleaseByteArrayElements(env, BitstreamData, bitstream, 0);
//	return 0;
//}

//g726_state_t *g_state726_16 = NULL;

int dstlen;

jint Java_com_tutk_sample_AVAPI_G711Decoder_VoiceEncode(JNIEnv* env, jobject thiz, jbyteArray SpeechData, jbyteArray BitstreamData, jint srclen)
{
	jbyte * speech = (jbyte*)(*env)->GetByteArrayElements(env, SpeechData, 0);
	jbyte * bitstream= (jbyte*)(*env)->GetByteArrayElements(env, BitstreamData, 0);

//	if(g_state726_16 == NULL)
//	{
//		g_state726_16 = (g726_state_t *)malloc(sizeof(g726_state_t));
//		g726_init(g_state726_16, 8000*2);
//	}


	g711u_Encode(speech, bitstream, srclen, &dstlen);


//	int iRet = g726_decode(g_state726_16, (short*)speech, (char*)bitstream, 320);

	(*env)->ReleaseByteArrayElements(env, SpeechData, speech, 0);
	(*env)->ReleaseByteArrayElements(env, BitstreamData, bitstream, 0);
	return dstlen;
}

jint Java_com_tutk_sample_AVAPI_G711Decoder_VoiceDecode(JNIEnv* env, jobject thiz, jbyteArray BitstreamData, jbyteArray SpeechData, jint srclen)
{
	jbyte * bitstream= (jbyte*)(*env)->GetByteArrayElements(env, BitstreamData, 0);
	jbyte * speech = (jbyte*)(*env)->GetByteArrayElements(env, SpeechData, 0);

//	if(g_state726_16 == NULL)
//	{
//		g_state726_16 = (g726_state_t *)malloc(sizeof(g726_state_t));
//		g726_init(g_state726_16, 8000*2);
//	}


	g711u_Decode(bitstream, speech, srclen, &dstlen);


//	int iRet = g726_decode(g_state726_16, (short*)speech, (char*)bitstream, 320);

	(*env)->ReleaseByteArrayElements(env, BitstreamData, bitstream, 0);
	(*env)->ReleaseByteArrayElements(env, SpeechData, speech, 0);
	return dstlen;
}

#ifndef	_G711_H
#define _G711_H

//#ifdef WIN32
//#include <windows.h>
//#define CALLMETHOD	   __stdcall
//#define CALLBACK	   __stdcall
//#ifdef G711DECODE_EXPORTS
//#define G711DECODE_API __declspec(dllexport)
//#else
//#define G711DECODE_API __declspec(dllimport)
//#endif
//#else
//#define G711DECODE_API extern "C"
#define CALLMETHOD
#define CALLBACK
//#endif

#ifdef __cplusplus
extern "C" {
#endif

//enum g711DEC_ERR
//{
#define g711_ERR_NOERROR  0			// �޴���
#define 	g711_ERR_HANDLE_INVALIDE 1		// �����Ч
#define 	g711_ERR_PARAM_INVALIDE 2			// ������Ч
#define 	g711_ERR_ALLOC_FAILED 3			// �ڴ�������
#define 	g711_ERR_GENERAL 4					// һ�����
//};
// 
// _declspec(dllexport) decode_Init(void);
// 
// _declspec(dllexport) int g711a_Encode(unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
// 
// _declspec(dllexport) int g711a_Decode( unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
// 
// _declspec(dllexport) int g711u_Encode(unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
// 
// _declspec(dllexport) int g711u_Decode( unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
// 
// _declspec(dllexport) decode_Cleanup(void);

/*G711DECODE_API */int  decode_Init(void);
/*G711DECODE_API */int  g711a_Encode(unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
/*G711DECODE_API */int  g711a_Decode( unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
/*G711DECODE_API */int  g711u_Encode(unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
/*G711DECODE_API */int  g711u_Decode( unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen);
/*G711DECODE_API */int  decode_Cleanup(void);

#ifdef __cplusplus
}
#endif
#endif

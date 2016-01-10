
#include <jni.h>

#ifndef _Included_com_jiuan_it_ipc_utils_RtspRecordVideo
#define _Included_com_jiuan_it_ipc_utils_RtspRecordVideo
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspRecordVideo_start(JNIEnv *, jobject, jstring);
//int RecordVideo_record(AV_RecordVideo_Content *obj, void* indata, int iSize);
JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspRecordVideo_record
  (JNIEnv *, jobject, jbyteArray, jint,jbyteArray, jint,jint);
JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_RtspRecordVideo_stop(JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif

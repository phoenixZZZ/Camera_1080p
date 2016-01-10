#ifndef __THREAD_H__
#define __THREAD_H__

//#pragma comment( lib, "pthreadVCE2.lib")  
//#pragma comment( lib, "pthreadVSE2.lib")  

#define OPER_OK 0
#define THREAD_MUTEX_INIT_ERROR 1
#define MUTEX_DESTROY_ERROR 2
#define THREAD_MUTEX_LOCK_ERROR 3
#define THREAD_MUTEX_UNLOCK_ERROR 4
#define THREAD_COND_INIT_ERROR 5
#define COND_DESTROY_ERROR 6
#define COND_SIGNAL_ERROR 7
#define COND_WAIT_ERROR 8

#include "pthread.h"
#pragma comment( lib, "pthread.lib") 

#ifdef __cplusplus
extern "C" {
#endif

/* Define to prevent recursive inclusion 
-------------------------------------*/
typedef    struct _mutex
{
	pthread_mutex_t        mutex;
}mutex_t;


typedef    struct _cond
{
	pthread_cond_t        cond;
}cond_t;


typedef    pthread_t        tid_t;
typedef    pthread_attr_t    attr_t;
typedef    void*    (* thread_fun_t)(void*);


typedef    struct _thread
{
	tid_t            tid;
	cond_t            *cv;
	int            state;
	int            stack_size;
	attr_t         attr;
	thread_fun_t    fun;
}thread_t;



/* mutex */
extern    int        mutex_init(mutex_t    *m);
extern    int        mutex_destroy(mutex_t    *m);
extern    int        mutex_lock(mutex_t    *m);
extern    int        mutex_unlock(mutex_t    *m);


/* cond */
extern    int        cond_init(cond_t    *c);
extern    int        cond_destroy(cond_t    *c);
extern    int        cond_signal(cond_t *c);
extern    int        cond_wait(cond_t    *c,mutex_t *m);



/* thread */
/* 线程的创建，其属性的设置等都封装在里面 */
extern    int        thread_create(thread_t *t);
//extern    int        thread_init(thread_t    *t);

#define    thread_join(t, p)     pthread_join(t, p)
#define    thread_self()        pthread_self()
//#define    thread_sigmask        pthread_sigmask


#ifdef __cplusplus
}
#endif

#endif
/* END OF FILE 
---------------------------------------------------------------*/
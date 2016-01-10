#ifndef __CBUF_H__
#define __CBUF_H__

#ifdef __cplusplus
extern "C" {
#endif

	/* Define to prevent recursive inclusion 
	-------------------------------------*/
#define CBUF_MAX 65535
#include "thread.h"

	typedef    struct _cbuf
	{
		int        size;
		int        next_in;
		int        next_out;
		int        capacity;
		mutex_t        mutex;
		cond_t        not_full;
		cond_t        not_empty;
		void        *data[CBUF_MAX];
	}cbuf_t;


	/* ��ʼ�����λ����� */
	extern    int        cbuf_init(cbuf_t *c);

	/* ���ٻ��λ����� */
	extern    void       cbuf_destroy(cbuf_t    *c);

	/* ѹ������ */
	extern    int        cbuf_enqueue(cbuf_t *c,void *data);

	/* ȡ������ */
	extern    void*      cbuf_dequeue(cbuf_t *c);

	extern    void*      cbuf_dequeue_2opera_bef(cbuf_t *c);
	extern    void*      cbuf_dequeue_2opera_aft(cbuf_t *c);

	/* �жϻ������Ƿ�Ϊ�� */
	extern    int        cbuf_full(cbuf_t    *c);

	/* �жϻ������Ƿ�Ϊ�� */
	extern    int        cbuf_empty(cbuf_t *c);

	/* ��ȡ�������ɴ�ŵ�Ԫ�ص��ܸ��� */
	extern    int        cbuf_capacity(cbuf_t *c);


#ifdef __cplusplus
}
#endif

#endif
/* END OF FILE 
---------------------------------------------------------------*/

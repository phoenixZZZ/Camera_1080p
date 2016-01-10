#include "cbuf.h"
#define DEBUG_CBUF 0


/* ��ʼ�����λ����� */
int cbuf_init(cbuf_t *c)
{
    int    ret = OPER_OK;

    if((ret = mutex_init(&c->mutex)) != OPER_OK)    
    {
//#ifdef DEBUG_CBUF
//    debug("cbuf init fail ! mutex init fail !\n");
//#endif
        return ret;
    }

    if((ret = cond_init(&c->not_full)) != OPER_OK)    
    {
//#ifdef DEBUG_CBUF
//    debug("cbuf init fail ! cond not full init fail !\n");
//#endif
        mutex_destroy(&c->mutex);
        return ret;
    }

    if((ret = cond_init(&c->not_empty)) != OPER_OK)
    {
//#ifdef DEBUG_CBUF
//    debug("cbuf init fail ! cond not empty init fail !\n");
//#endif
        cond_destroy(&c->not_full);
        mutex_destroy(&c->mutex);
        return ret;
    }

    c->size     = 0;
    c->next_in    = 0;
    c->next_out = 0;
    c->capacity    = CBUF_MAX;

//#ifdef DEBUG_CBUF
//    debug("cbuf init success !\n");
//#endif

    return ret;
}


/* ���ٻ��λ����� */
void        cbuf_destroy(cbuf_t    *c)
{
    cond_destroy(&c->not_empty);
    cond_destroy(&c->not_full);
    mutex_destroy(&c->mutex);

//#ifdef DEBUG_CBUF
//    debug("cbuf destroy success \n");
//#endif
}



/* ѹ������ */
int        cbuf_enqueue(cbuf_t *c,void *data)
{
    int    ret = OPER_OK;

    if((ret = mutex_lock(&c->mutex)) != OPER_OK)    return ret;

    /*
     * Wait while the buffer is full.
     */
    while(cbuf_full(c))
    {
//#ifdef DEBUG_CBUF
//    debug("cbuf is full !!!\n");
//#endif
        cond_wait(&c->not_full,&c->mutex);
    }

    c->data[c->next_in++] = data;
    c->size++;
    c->next_in %= c->capacity;

    mutex_unlock(&c->mutex);

    /*
     * Let a waiting consumer know there is data.
     */
    cond_signal(&c->not_empty);

//#ifdef DEBUG_CBUF
////    debug("cbuf enqueue success ,data : %p\n",data);
//    debug("enqueue\n");
//#endif

    return ret;
}

void* cbuf_dequeue(cbuf_t *c)
{
    void   *data   = NULL;
    int    ret     = OPER_OK;

    if((ret = mutex_lock(&c->mutex)) != OPER_OK)    return NULL;

    if(cbuf_empty(c))
    {
    	mutex_unlock(&c->mutex);
    	return NULL;
    }

    data = c->data[c->next_out++];
    c->size--;
    c->next_out %= c->capacity;

    mutex_unlock(&c->mutex);

    cond_signal(&c->not_full);

    return data;
}

void* cbuf_dequeue_2opera_bef(cbuf_t *c)
{
    void   *data   = NULL;
    int    ret     = OPER_OK;

    if((ret = mutex_lock(&c->mutex)) != OPER_OK)    return NULL;

    if(cbuf_empty(c))
    {
    	mutex_unlock(&c->mutex);
    	return NULL;
    }

    data = c->data[c->next_out];

    mutex_unlock(&c->mutex);

    cond_signal(&c->not_full);

    return data;
}

void* cbuf_dequeue_2opera_aft(cbuf_t *c)
{
    void   *data   = NULL;
    int    ret     = OPER_OK;

    if((ret = mutex_lock(&c->mutex)) != OPER_OK)    return NULL;

    if(cbuf_empty(c))
    {
    	mutex_unlock(&c->mutex);
    	return NULL;
    }

    c->next_out++;
    c->size--;
    c->next_out %= c->capacity;

    mutex_unlock(&c->mutex);

    cond_signal(&c->not_full);

    return NULL;
}

/* �жϻ������Ƿ�Ϊ�� */
int        cbuf_full(cbuf_t    *c)
{
	if (c->size == c->capacity)
		return 1;
	else
		return 0;
}

/* �жϻ������Ƿ�Ϊ�� */
int        cbuf_empty(cbuf_t *c)
{
    if (c->size == 0)
		return 1;
	else
		return 0;
}

/* ��ȡ�������ɴ�ŵ�Ԫ�ص��ܸ��� */
int        cbuf_capacity(cbuf_t *c)
{
    return c->capacity;
}

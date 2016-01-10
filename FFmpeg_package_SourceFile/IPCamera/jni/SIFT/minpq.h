/**@file
Functions and structures for implementing a minimizing priority queue.

Copyright (C) 2006-2010  Rob Hess <hess@eecs.oregonstate.edu>
@version 1.1.2-20100521
*/

/*
此文件中是最小优先队列(小顶堆)相关的一些结构和函数声明
*/

#ifndef MINPQ_H
#define MINPQ_H

#include <stdlib.h>


/******************************* Defs and macros *****************************/

/* initial # of priority queue elements for which to allocate space */
#define MINPQ_INIT_NALLOCD 512  //初始分配空间个数

/********************************** Structures *******************************/
/*结点结构*/
/** an element in a minimizing priority queue */
struct pq_node
{
	void* data;
	int key;
};

/*最小优先队列结构*/
/** a minimizing priority queue */
struct min_pq
{
    struct pq_node* pq_array;    /* array containing priority queue */ //结点指针
    int nallocd;                 /* number of elements allocated */ //分配的空间个数
    int n;                       /**< number of elements in pq */ //元素个数
};


/*************************** Function Prototypes *****************************/
/*初始化最小优先级队列
*/
/**
Creates a new minimizing priority queue.
*/
extern struct min_pq* minpq_init();

/*插入元素到优先队列
参数：
min_pq：优先队列
data:要插入的数据
key:与data关联的键值
返回值：0：成功，1：失败
*/
/**
Inserts an element into a minimizing priority queue.

@param min_pq a minimizing priority queue
@param data the data to be inserted
@param key the key to be associated with \a data

@return Returns 0 on success or 1 on failure.
*/
extern int minpq_insert( struct min_pq* min_pq, void* data, int key );


/*返回优先队列中键值最小的元素，但并不删除它
参数：min_pq：优先队列
返回值：最小元素的指针
*/
/**
Returns the element of a minimizing priority queue with the smallest key
without removing it from the queue.
@param min_pq a minimizing priority queue
@return Returns the element of \a min_pq with the smallest key or NULL if \a min_pq is empty
*/
extern void* minpq_get_min( struct min_pq* min_pq );


/*返回并移除具有最小键值的元素
参数：min_pq：优先级队列
返回值：最小元素的指针
*/
/**
Removes and returns the element of a minimizing priority queue with the smallest key.
@param min_pq a minimizing priority queue
@return Returns the element of \a min_pq with the smallest key of NULL if \a min_pq is empty
*/
extern void* minpq_extract_min( struct min_pq* min_pq );

/*释放优先队列
*/
/**
De-allocates the memory held by a minimizing priorioty queue
@param min_pq pointer to a minimizing priority queue
*/
extern void minpq_release( struct min_pq** min_pq );


#endif

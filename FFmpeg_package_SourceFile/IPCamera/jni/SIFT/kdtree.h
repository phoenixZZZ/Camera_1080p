/**@file
Functions and structures for maintaining a k-d tree database of image
features.

For more information, refer to:

Beis, J. S. and Lowe, D. G.  Shape indexing using approximate
nearest-neighbor search in high-dimensional spaces.  In <EM>Conference
on Computer Vision and Pattern Recognition (CVPR)</EM> (2003),
pp. 1000--1006.

Copyright (C) 2006-2010  Rob Hess <hess@eecs.oregonstate.edu>

@version 1.1.2-20100521
*/

/*
  此文件中包含K-D树的建立与搜索函数的声明
*/

#ifndef KDTREE_H
#define KDTREE_H

#include "cxcore.h"


/********************************* Structures ********************************/

struct feature;

/*K-D树中的结点结构*/
/** a node in a k-d tree */
struct kd_node
{
    int ki;                      /**< partition key index */ //分割位置(枢轴)的维数索引(哪一维是分割位置)，取值为1-128
    double kv;                   /**< partition key value */  //枢轴的值(所有特征向量在枢轴索引维数上的分量的中值)
    int leaf;                    /**< 1 if node is a leaf, 0 otherwise */ //是否叶子结点的标志
    struct feature* features;    /**< features at this node */  //此结点对应的特征点集合(数组)
    int n;                       /**< number of features */ //特征点的个数
    struct kd_node* kd_left;     /**< left child */  //左子树
    struct kd_node* kd_right;    /**< right child */  //右子树
};


/*************************** Function Prototypes *****************************/
/*根据给定的特征点集合建立k-d树
参数：
features：特征点数组，注意：此函数将会改变features数组中元素的排列顺序
n：特征点个数
返回值：建立好的k-d树的树根指针
*/
/**
A function to build a k-d tree database from keypoints in an array.

@param features an array of features; <EM>this function rearranges the order
	of the features in this array, so you should take appropriate measures
	if you are relying on the order of the features (e.g. call this function
	before order is important)</EM>
@param n the number of features in \a features
@return Returns the root of a kd tree built from \a features.
*/
extern struct kd_node* kdtree_build( struct feature* features, int n );


/*用BBF算法在k-d树中查找指定特征点的k个最近邻特征点
参数：
kd_root：图像特征的k-d树的树根
feat：目标特征点
k：近邻个数
nbrs：k个近邻特征点的指针数组，按到目标特征点的距离升序排列
      此数组的内存将在本函数中被分配，使用完后必须在调用出释放：free(*nbrs)
max_nn_chks：搜索的最大次数，超过此值不再搜索
返回值：存储在nbrs中的近邻个数，返回-1表示失败
*/
/**
Finds an image feature's approximate k nearest neighbors in a kd tree using
Best Bin First search.

@param kd_root root of an image feature kd tree
@param feat image feature for whose neighbors to search
@param k number of neighbors to find
@param nbrs pointer to an array in which to store pointers to neighbors
	in order of increasing descriptor distance; memory for this array is
	allocated by this function and must be freed by the caller using
	free(*nbrs)
@param max_nn_chks search is cut off after examining this many tree entries

@return Returns the number of neighbors found and stored in \a nbrs, or
	-1 on error.
*/
extern int kdtree_bbf_knn( struct kd_node* kd_root, struct feature* feat,
						  int k, struct feature*** nbrs, int max_nn_chks );


/**
Finds an image feature's approximate k nearest neighbors within a specified
spatial region in a kd tree using Best Bin First search.

@param kd_root root of an image feature kd tree
@param feat image feature for whose neighbors to search
@param k number of neighbors to find
@param nbrs pointer to an array in which to store pointers to neighbors
	in order of increasing descriptor distance; memory for this array is
	allocated by this function and must be freed by the caller using
	free(*nbrs)
@param max_nn_chks search is cut off after examining this many tree entries
@param rect rectangular region in which to search for neighbors
@param model if true, spatial search is based on kdtree features' model
	locations; otherwise it is based on their image locations

@return Returns the number of neighbors found and stored in \a nbrs
	(in case \a k neighbors could not be found before examining
	\a max_nn_checks keypoint entries).
*/
extern int kdtree_bbf_spatial_knn( struct kd_node* kd_root,
								struct feature* feat, int k,
								struct feature*** nbrs, int max_nn_chks,
								CvRect rect, int model );


/*释放k-d树占用的存储空间
*/
/**
De-allocates memory held by a kd tree

@param kd_root pointer to the root of a kd tree
*/
extern void kdtree_release( struct kd_node* kd_root );


#endif

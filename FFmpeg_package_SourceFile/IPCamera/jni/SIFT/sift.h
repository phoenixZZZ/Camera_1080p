/**@file
Functions for detecting SIFT image features.

For more information, refer to:

Lowe, D.  Distinctive image features from scale-invariant keypoints.
<EM>International Journal of Computer Vision, 60</EM>, 2 (2004),
pp.91--110.

Copyright (C) 2006-2010  Rob Hess <hess@eecs.oregonstate.edu>
Note: The SIFT algorithm is patented in the United States and cannot be
used in commercial products without a license from the University of
British Columbia.  For more information, refer to the file LICENSE.ubc
that accompanied this distribution.

@version 1.1.2-20100521
*/

/*
  此文件中包括SIFT特征点检测函数的声明，以及一些实现SIFT算法中一些默认参数的定义
*/

#ifndef SIFT_H
#define SIFT_H

#include "cxcore.h"

/******************************** Structures *********************************/

//极值点检测中用到的结构
//在SIFT特征提取过程中，此类型数据会被赋值给feature结构的feature_data成员
/** holds feature data relevant to detection */
struct detection_data
{
    int r;      //特征点所在的行
    int c;      //特征点所在的列
    int octv;   //高斯差分金字塔中，特征点所在的组
    int intvl;  //高斯差分金字塔中，特征点所在的组中的层
    double subintvl;  //特征点在层方向(σ方向,intvl方向)上的亚像素偏移量
    double scl_octv;  //特征点所在的组的尺度
};

struct feature;


/******************************* 一些默认参数 *****************************/
/******************************* Defs and macros *****************************/

//高斯金字塔每组内的层数
/** default number of sampled intervals per octave */
#define SIFT_INTVLS 3

//第0层的初始尺度，即第0层高斯模糊所使用的参数
/** default sigma for initial gaussian smoothing */
#define SIFT_SIGMA 1.6

//对比度阈值，针对归一化后的图像，用来去除不稳定特征
/** default threshold on keypoint contrast |D(x)| */
#define SIFT_CONTR_THR 0.04

//主曲率比值的阈值，用来去除边缘特征
/** default threshold on keypoint ratio of principle curvatures */
#define SIFT_CURV_THR 10

//是否将图像放大为之前的两倍
/** double image size before pyramid construction? */
#define SIFT_IMG_DBL 1

//输入图像的尺度为0.5
/* assumed gaussian blur for input image */
#define SIFT_INIT_SIGMA 0.5

//边界的像素宽度，检测过程中将忽略边界线中的极值点，即只检测边界线以内是否存在极值点
/* width of border in which to ignore keypoints */
#define SIFT_IMG_BORDER 5

//通过插值进行极值点精确定位时，最大差值次数，即关键点修正次数
/* maximum steps of keypoint interpolation before failure */
#define SIFT_MAX_INTERP_STEPS 5

//特征点方向赋值过程中，梯度方向直方图中柱子(bin)的个数
/* default number of bins in histogram for orientation assignment */
#define SIFT_ORI_HIST_BINS 36

//特征点方向赋值过程中，搜索邻域的半径为：3 * 1.5 * σ
/* determines gaussian sigma for orientation assignment */
#define SIFT_ORI_SIG_FCTR 1.5

//特征点方向赋值过程中，搜索邻域的半径为：3 * 1.5 * σ
/* determines the radius of the region used in orientation assignment */
#define SIFT_ORI_RADIUS 3.0 * SIFT_ORI_SIG_FCTR

//特征点方向赋值过程中，梯度方向直方图的平滑次数，计算出梯度直方图后还要进行高斯平滑
/* number of passes of orientation histogram smoothing */
#define SIFT_ORI_SMOOTH_PASSES 2

//特征点方向赋值过程中，梯度幅值达到最大值的80%则分裂为两个特征点
/* orientation magnitude relative to max that results in new feature */
#define SIFT_ORI_PEAK_RATIO 0.8

//计算特征描述子过程中，计算方向直方图时，将特征点附近划分为d*d个区域，每个区域生成一个直方图，SIFT_DESCR_WIDTH即d的默认值
/** default width of descriptor histogram array */
#define SIFT_DESCR_WIDTH 4

//计算特征描述子过程中，每个方向直方图的bin个数
/** default number of bins per histogram in descriptor array */
#define SIFT_DESCR_HIST_BINS 8

//计算特征描述子过程中，特征点周围的d*d个区域中，每个区域的宽度为m*σ个像素，SIFT_DESCR_SCL_FCTR即m的默认值，σ为特征点的尺度
/* determines the size of a single descriptor orientation histogram */
#define SIFT_DESCR_SCL_FCTR 3.0

//计算特征描述子过程中，特征描述子向量中元素的阈值(最大值，并且是针对归一化后的特征描述子)，超过此阈值的元素被强行赋值为此阈值
/* threshold on magnitude of elements of descriptor vector */
#define SIFT_DESCR_MAG_THR 0.2

//计算特征描述子过程中，将浮点型的特征描述子变为整型时乘以的系数
/* factor used to convert floating-point descriptor to unsigned char */
#define SIFT_INT_DESCR_FCTR 512.0

//定义了一个带参数的函数宏，用来提取参数f中的feature_data成员并转换为detection_data格式的指针
/* returns a feature's detection data */
#define feat_detection_data(f) ( (struct detection_data*)(f->feature_data) )


/*************************** Function Prototypes *****************************/

/*使用默认参数在图像中提取SIFT特征点
参数：
img：图像指针
feat：用来存储特征点的feature数组的指针
      此数组的内存将在本函数中被分配，使用完后必须在调用出释放：free(*feat)
返回值：提取的特征点个数，若返回-1表明提取失败
*/
/**
Finds SIFT features in an image using default parameter values.  All
detected features are stored in the array pointed to by \a feat.
@param img the image in which to detect features
@param feat a pointer to an array in which to store detected features; memory
	for this array is allocated by this function and must be freed by the caller
	using free(*feat)
@return Returns the number of features stored in \a feat or -1 on failure
@see _sift_features()
*/
extern int sift_features( IplImage* img, struct feature** feat );


/*使用用户指定的参数在图像中提取SIFT特征点
参数：
img：输入图像
feat：存储特征点的数组的指针
      此数组的内存将在本函数中被分配，使用完后必须在调用出释放：free(*feat)
intvls：每组的层数
sigma：初始高斯平滑参数σ
contr_thr：对比度阈值，针对归一化后的图像，用来去除不稳定特征
curv_thr：去除边缘的特征的主曲率阈值
img_dbl：是否将图像放大为之前的两倍
descr_width：特征描述过程中，计算方向直方图时，将特征点附近划分为descr_width*descr_width个区域，每个区域生成一个直方图
descr_hist_bins：特征描述过程中，每个直方图中bin的个数
返回值：提取的特征点个数，若返回-1表明提取失败
*/
/**
Find a SIFT features in an image using user-specified parameter values.  All
detected features are stored in the array pointed to by \a feat.

@param img the image in which to detect features
@param feat a pointer to an array in which to store detected features; memory
	for this array is allocated by this function and must be freed by the caller
	using free(*feat)
@param intvls the number of intervals sampled per octave of scale space
@param sigma the amount of Gaussian smoothing applied to each image level
	before building the scale space representation for an octave
@param contr_thr a threshold on the value of the scale space function
	\f$\left|D(\hat{x})\right|\f$, where \f$\hat{x}\f$ is a vector specifying
	feature location and scale, used to reject unstable features;  assumes
pixel values in the range [0, 1]
@param curv_thr threshold on a feature's ratio of principle curvatures
	used to reject features that are too edge-like
@param img_dbl should be 1 if image doubling prior to scale space
	construction is desired or 0 if not
@param descr_width the width, \f$n\f$, of the \f$n \times n\f$ array of
	orientation histograms used to compute a feature's descriptor
@param descr_hist_bins the number of orientations in each of the
	histograms in the array used to compute a feature's descriptor

@return Returns the number of keypoints stored in \a feat or -1 on failure
@see sift_features()
*/
extern int _sift_features( IplImage* img, struct feature** feat, int intvls,
						  double sigma, double contr_thr, int curv_thr,
						  int img_dbl, int descr_width, int descr_hist_bins );


#endif

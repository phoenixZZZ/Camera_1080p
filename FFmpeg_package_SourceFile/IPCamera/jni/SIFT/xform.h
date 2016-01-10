/**@file
Functions for computing transforms from image feature correspondences

Copyright (C) 2006-2010  Rob Hess <hess@eecs.oregonstate.edu>
@version 1.1.2-20100521
*/

/*
此文件中包括的函数声明有：
1、RANSAC算法特征匹配筛选
2、其他一些与RANSAC算法相关函数
*/


#ifndef XFORM_H
#define XFORM_H

#include <cxcore.h>


/********************************** Structures *******************************/

struct feature;

//RANSAC算法中用到的结构
//在RANSAC算法过程中，此类型数据会被赋值给feature结构的feature_data成员
/** holds feature data relevant to ransac */
struct ransac_data
{
    void* orig_feat_data; //保存此特征点的feature_data域的以前的值
    int sampled; //标识位，值为1标识此特征点是否被选为样本
};

//一些宏定义
/******************************* Defs and macros *****************************/

/*RANSAC算法的容错度
对于匹配点对<pt,mpt>，以及变换矩阵H，
如果pt经H变换后的点和mpt之间的距离的平方小于RANSAC_ERR_TOL，则可将其加入当前一致集
*/
/* RANSAC error tolerance in pixels */
#define RANSAC_ERR_TOL 3

//内点数目占样本总数目的百分比的最小值
/** pessimistic estimate of fraction of inliers for RANSAC */
#define RANSAC_INLIER_FRAC_EST 0.25

//一个匹配点对支持错误模型的概率（不知道是干什么用的）
/** estimate of the probability that a correspondence supports a bad model */
#define RANSAC_PROB_BAD_SUPP 0.10

//定义了一个带参数的函数宏，用来提取参数feat中的feature_data成员并转换为ransac_data格式的指针
/* extracts a feature's RANSAC data */
#define feat_ransac_data( feat ) ( (struct ransac_data*) (feat)->feature_data )


/*定义了一个函数指针类型ransac_xform_fn，其返回值是CvMat*类型，有三个参数
之后可以用ransac_xform_fn来定义函数指针
此类型的函数指针被用在ransac_form()函数的参数中
此类型的函数会根据匹配点对集合计算出一个变换矩阵作为返回值
参数：
pts：点的集合
mpts：点的集合，pts[i]与mpts[i]是互相对应的匹配点
n：pts和mpts数组中点的个数，pts和mpts中点的个数必须相同
返回值：一个变换矩阵，将pts中的每一个点转换为mpts中的对应点，返回值为空表示失败
*/
/**
Prototype for transformation functions passed to ransac_xform().  Functions
of this type should compute a transformation matrix given a set of point
correspondences.
@param pts array of points
@param mpts array of corresponding points; each \a pts[\a i], \a i=0..\a n-1,
	corresponds to \a mpts[\a i]
@param n number of points in both \a pts and \a mpts
@return Should return a transformation matrix that transforms each point in
	\a pts to the corresponding point in \a mpts or NULL on failure.
*/
typedef CvMat* (*ransac_xform_fn)( CvPoint2D64f* pts, CvPoint2D64f* mpts,int n );


/*定义了一个函数指针类型ransac_err_fn，其返回值是double类型，有三个参数
之后可以用ransac_err_fn来定义函数指针
此类型的函数指针被用在ransac_form()函数的参数中
此类型的函数会根据匹配点对(pt,mpt)和变换矩阵M计算出一个double类型的错误度量值作为返回值
此错误度量值用来评判"点mpt"和"点pt经M矩阵变换后的点"之间是否相一致
参数：
pt：一个点
mpt：点pt的对应匹配点
M：变换矩阵
返回值："点mpt"和"点pt经M矩阵变换后的点"之间的错误度量值
*/
/**
Prototype for error functions passed to ransac_xform().  For a given
point, its correspondence, and a transform, functions of this type should
compute a measure of error between the correspondence and the point after
the point has been transformed by the transform.
@param pt a point
@param mpt \a pt's correspondence
@param T a transform
@return Should return a measure of error between \a mpt and \a pt after
	\a pt has been transformed by the transform \a T.
*/
typedef double (*ransac_err_fn)( CvPoint2D64f pt, CvPoint2D64f mpt, CvMat* M );


/***************************** Function Prototypes ***************************/

/*利用RANSAC算法进行特征点筛选，计算出最佳匹配的变换矩阵
参数：
features：特征点数组，只有当mtype类型的匹配点存在时才被用来进行单应性计算
n：特征点个数
mtype：决定使用每个特征点的哪个匹配域进行变换矩阵的计算，应该是FEATURE_MDL_MATCH，
    FEATURE_BCK_MATCH，FEATURE_MDL_MATCH中的一个。若是FEATURE_MDL_MATCH，
    对应的匹配点对坐标是每个特征点的img_pt域和其匹配点的mdl_pt域，
    否则，对应的匹配点对是每个特征点的img_pt域和其匹配点的img_pt域。
xform_fn：函数指针，指向根据输入的点对进行变换矩阵计算的函数，一般传入lsq_homog()函数
m：在函数xform_fn中计算变换矩阵需要的最小特征点对个数
p_badxform：允许的错误概率，即允许RANSAC算法计算出的变换矩阵错误的概率，当前计算出的模型的错误概率小于p_badxform时迭代停止
err_fn：函数指针，对于给定的变换矩阵，计算推定的匹配点对之间的变换误差，一般传入homog_xfer_err()函数
err_tol：容错度，对于给定的变换矩阵，在此范围内的点对被认为是内点
inliers：输出参数，指针数组，指向计算出的最终的内点集合，若为空，表示没计算出符合要求的一致集。
        此数组的内存将在本函数中被分配，使用完后必须在调用出释放：free(*inliers)
n_in：输出参数，最终计算出的内点的数目
返回值：RANSAC算法计算出的变换矩阵，若为空，表示出错或无法计算出可接受矩阵
*/
/**
Calculates a best-fit image transform from image feature correspondences using RANSAC.

For more information refer to:
Fischler, M. A. and Bolles, R. C.  Random sample consensus: a paradigm for
model fitting with applications to image analysis and automated cartography.
<EM>Communications of the ACM, 24</EM>, 6 (1981), pp. 381--395.

@param features an array of features; only features with a non-NULL match
	of type \a mtype are used in homography computation
@param n number of features in \a feat
@param mtype determines which of each feature's match fields to use
	for transform computation; should be one of FEATURE_FWD_MATCH,
	FEATURE_BCK_MATCH, or FEATURE_MDL_MATCH; if this is FEATURE_MDL_MATCH,
	correspondences are assumed to be between a feature's img_pt field
	and its match's mdl_pt field, otherwise correspondences are assumed to
	be between the the feature's img_pt field and its match's img_pt field
@param xform_fn pointer to the function used to compute the desired
	transformation from feature correspondences
@param m minimum number of correspondences necessary to instantiate the
	transform computed by \a xform_fn
@param p_badxform desired probability that the final transformation
	returned by RANSAC is corrupted by outliers (i.e. the probability that
	no samples of all inliers were drawn)
@param err_fn pointer to the function used to compute a measure of error
	between putative correspondences for a given transform
@param err_tol correspondences within this distance of each other are
	considered as inliers for a given transform
@param inliers if not NULL, output as an array of pointers to the final
	set of inliers; memory for this array is allocated by this function and
	must be freed by the caller using free(*inliers)
@param n_in if not NULL, output as the final number of inliers

@return Returns a transformation matrix computed using RANSAC or NULL
	on error or if an acceptable transform could not be computed.
*/
extern CvMat* ransac_xform( struct feature* features, int n, int mtype,
						   ransac_xform_fn xform_fn, int m,
						   double p_badxform, ransac_err_fn err_fn,
						   double err_tol, struct feature*** inliers,
						   int* n_in );


/*运用线性变换，进行点匹配计算平面单应性
参数：
pts：点的集合
mpts：点的集合，pts[i]与mpts[i]是互相对应的匹配点
n：pts和mpts数组中点的个数，pts和mpts中点的个数必须相同，并且点对个数至少为4
返回值：变换矩阵，可将pts中的点变换为mpts中的点，若点个数少于4则返回空
*/
/**
Calculates a planar homography from point correspondeces using the direct
linear transform.  Intended for use as a ransac_xform_fn.
@param pts array of points
@param mpts array of corresponding points; each \a pts[\a i], \a i=0..\a
	n-1, corresponds to \a mpts[\a i]
@param n number of points in both \a pts and \a mpts; must be at least 4
@return Returns the \f$3 \times 3\f$ planar homography matrix that
	transforms points in \a pts to their corresponding points in \a mpts
	or NULL if fewer than 4 correspondences were provided
*/
extern CvMat* dlt_homog( CvPoint2D64f* pts, CvPoint2D64f* mpts, int n );


/*根据4对坐标点计算最小二乘平面单应性变换矩阵
参数：
pts：点的集合
mpts：点的集合，pts[i]与mpts[i]是互相对应的匹配点
n：pts和mpts数组中点的个数，pts和mpts中点的个数必须相同，并且点对个数至少为4
返回值：变换矩阵，可将pts中的点变换为mpts中的点，若点个数少于4则返回空
*/
/**
Calculates a least-squares planar homography from point correspondeces.
Intended for use as a ransac_xform_fn.
@param pts array of points
@param mpts array of corresponding points; each \a pts[\a i], \a i=0..\a n-1,
	corresponds to \a mpts[\a i]
@param n number of points in both \a pts and \a mpts; must be at least 4
@return Returns the \f$3 \times 3\f$ least-squares planar homography
	matrix that transforms points in \a pts to their corresponding points
	in \a mpts or NULL if fewer than 4 correspondences were provided
*/
extern CvMat* lsq_homog( CvPoint2D64f* pts, CvPoint2D64f* mpts, int n );


/*对于给定的单应性矩阵H，计算输入点pt精H变换后的点与其匹配点mpt之间的误差
例如：给定点x，其对应点x'，单应性矩阵H，则计算x'与Hx之间的距离的平方，d(x', Hx)^2
参数：
pt：一个点
mpt：pt的对应匹配点
H：单应性矩阵
返回值：转换误差
*/
/**
Calculates the transfer error between a point and its correspondence for
a given homography, i.e. for a point \f$x\f$, it's correspondence \f$x'\f$,
and homography \f$H\f$, computes \f$d(x', Hx)^2\f$.  Intended for use as a
ransac_err_fn.
@param pt a point
@param mpt \a pt's correspondence
@param H a homography matrix
@return Returns the transfer error between \a pt and \a mpt given \a H
*/
extern double homog_xfer_err( CvPoint2D64f pt, CvPoint2D64f mpt, CvMat* H );


/*计算点pt经透视变换后的点，即给定一点pt和透视变换矩阵T，计算变换后的点
给定点(x,y)，变换矩阵M，计算[x',y',w']^T = M * [x,y,1]^T(^T表示转置)，
则变换后的点是(u,v) = (x'/w', y'/w')
注意：仿射变换是透视变换的特例
参数：
pt：一个二维点
T：透视变换矩阵
返回值：pt经透视变换后的点
*/
/**
Performs a perspective transformation on a single point.  That is, for a
point \f$(x, y)\f$ and a \f$3 \times 3\f$ matrix \f$T\f$ this function
returns the point \f$(u, v)\f$, where<BR>
\f$[x' \ y' \ w']^T = T \times [x \ y \ 1]^T\f$,<BR>
and<BR>
\f$(u, v) = (x'/w', y'/w')\f$.
Note that affine transforms are a subset of perspective transforms.
@param pt a 2D point
@param T a perspective transformation matrix
@return Returns the point \f$(u, v)\f$ as above.
*/
extern CvPoint2D64f persp_xform_pt( CvPoint2D64f pt, CvMat* T );


#endif

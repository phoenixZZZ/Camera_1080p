/**@file
Functions and structures for dealing with image features

Copyright (C) 2006-2010  Rob Hess <hess@eecs.oregonstate.edu>

@version 1.1.2-20100521
*/

/*
  此文件中定义了存储特征点的结构体feature，以及几个函数原型的声明：
1、特征点的导入导出
2、特征点绘制
*/


#ifndef IMGFEATURES_H
#define IMGFEATURES_H

#include "cxcore.h"

/*特征点的类型：
FEATURE_OXFD表示是牛津大学VGG提供的源码中的特征点格式，
FEATURE_LOWE表示是David.Lowe提供的源码中的特征点格式
*/
/** FEATURE_OXFD <BR> FEATURE_LOWE */
enum feature_type
{
	FEATURE_OXFD,
	FEATURE_LOWE,
};

/*特征点匹配类型：
FEATURE_FWD_MATCH：表明feature结构中的fwd_match域是对应的匹配点
FEATURE_BCK_MATCH：表明feature结构中的bck_match域是对应的匹配点
FEATURE_MDL_MATCH：表明feature结构中的mdl_match域是对应的匹配点
*/
/** FEATURE_FWD_MATCH <BR> FEATURE_BCK_MATCH <BR> FEATURE_MDL_MATCH */
enum feature_match_type
{
	FEATURE_FWD_MATCH,
	FEATURE_BCK_MATCH,
	FEATURE_MDL_MATCH,
};

/*画出的特征点的颜色*/
/* colors in which to display different feature types */
//#define FEATURE_OXFD_COLOR CV_RGB(255,255,0)
//#define FEATURE_LOWE_COLOR CV_RGB(255,0,255)
#define FEATURE_OXFD_COLOR cvScalar(0,255,255,0) //r,g,b
#define FEATURE_LOWE_COLOR cvScalar(255,0,255,0)

/*最大特征描述子长度，定为128*/
/** max feature descriptor length */
#define FEATURE_MAX_D 128

/*特征点结构体
此结构体可存储2中类型的特征点：
FEATURE_OXFD表示是牛津大学VGG提供的源码中的特征点格式，
FEATURE_LOWE表示是David.Lowe提供的源码中的特征点格式。
如果是OXFD类型的特征点，结构体中的a,b,c成员描述了特征点周围的仿射区域(椭圆的参数)，即邻域。
如果是LOWE类型的特征点，结构体中的scl和ori成员描述了特征点的大小和方向。
fwd_match，bck_match，mdl_match一般同时只有一个起作用，用来指明此特征点对应的匹配点
*/
/**
Structure to represent an affine invariant image feature.  The fields
x, y, a, b, c represent the affine region around the feature:
a(x-u)(x-u) + 2b(x-u)(y-v) + c(y-v)(y-v) = 1
*/
struct feature
{
    double x;                      /**< x coord */ //特征点的x坐标
    double y;                      /**< y coord */ //特征点的y坐标
    double a;                      /**< Oxford-type affine region parameter */ //OXFD特征点中椭圆的参数
    double b;                      /**< Oxford-type affine region parameter */ //OXFD特征点中椭圆的参数
    double c;                      /**< Oxford-type affine region parameter */ //OXFD特征点中椭圆的参数
    double scl;                    /**< scale of a Lowe-style feature *///LOWE特征点的尺度
    double ori;                    /**< orientation of a Lowe-style feature */ //LOWE特征点的方向
    int d;                         /**< descriptor length */ //特征描述子的长度，即维数，一般是128
    double descr[FEATURE_MAX_D];   /**< descriptor */ //128维的特征描述子，即一个double数组
    int type;                      /**< feature type, OXFD or LOWE */ //特征点类型
	int category;                  /**< all-purpose feature category */
    struct feature* fwd_match;     /**< matching feature from forward image */   //指明此特征点对应的匹配点
    struct feature* bck_match;     /**< matching feature from backmward image */ //指明此特征点对应的匹配点
    struct feature* mdl_match;     /**< matching feature from model */           //指明此特征点对应的匹配点
    CvPoint2D64f img_pt;           /**< location in image */ //特征点的坐标,等于(x,y)
    CvPoint2D64f mdl_pt;           /**< location in model */ //当匹配类型是mdl_match时用到
    void* feature_data;            /**< user-definable data */ //用户定义的数据:
                                                               //在SIFT极值点检测中，是detection_data结构的指针
                                                               //在k-d树搜索中，是bbf_data结构的指针
                                                               //在RANSAC算法中，是ransac_data结构的指针
};


/*从文件中读入图像特征
文件中的特征点格式必须是FEATURE_OXFD或FEATURE_LOWE格式
参数：
filename：文件名
type：特征点类型
feat：用来存储特征点的feature数组的指针
返回值：导入的特征点个数
*/
/**
Reads image features from file.  The file should be formatted as from
the code provided by the Visual Geometry Group at Oxford or from the
code provided by David Lowe.
@param filename location of a file containing image features
@param type determines how features are input.  If \a type is FEATURE_OXFD,
	the input file is treated as if it is from the code provided by the VGG
	at Oxford: http://www.robots.ox.ac.uk:5000/~vgg/research/affine/index.html
	<BR><BR>
	If \a type is FEATURE_LOWE, the input file is treated as if it is from
	David Lowe's SIFT code: http://www.cs.ubc.ca/~lowe/keypoints  
@param feat pointer to an array in which to store imported features; memory for
    this array is allocated by this function and must be freed by the caller using free(*feat)
@return Returns the number of features imported from filename or -1 on error
*/
extern int import_features( char* filename, int type, struct feature** feat );


/*导出feature数组到文件
参数：
filename：文件名
feat：特征数组
n：特征点个数
返回值：0：成功；1：失败
*/
/**
Exports a feature set to a file formatted depending on the type of
features, as specified in the feature struct's type field.
@param filename name of file to which to export features
@param feat feature array
@param n number of features 
@return Returns 0 on success or 1 on error
*/
extern int export_features( char* filename, struct feature* feat, int n );


/*在图片上画出特征点
参数：
img：图像
feat：特征点数组
n：特征点个数
*/
/**
Displays a set of features on an image
@param img image on which to display features
@param feat array of Oxford-type features
@param n number of features
*/
extern void draw_features( IplImage* img, struct feature* feat, int n );


/*计算两个特征描述子间的欧氏距离的平方
参数：
f1:第一个特征点
f2:第二个特征点
返回值：欧氏距离的平方
*/
/**
Calculates the squared Euclidian distance between two feature descriptors.
@param f1 first feature
@param f2 second feature
@return Returns the squared Euclidian distance between the descriptors of
\a f1 and \a f2.
*/
extern double descr_dist_sq( struct feature* f1, struct feature* f2 );


#endif

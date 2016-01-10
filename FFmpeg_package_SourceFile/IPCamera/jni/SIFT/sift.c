/*
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
  此文件最重要
  包含SIFT特征点检测的实现
*/

#include "sift.h"
#include "imgfeatures.h"
#include "utils.h"

#include <cxcore.h>
#include <cv.h>

/************************ 未暴露接口的一些本地函数的声明 **************************/
/************************* Local Function Prototypes *************************/

//将原图转换为32位灰度图并归一化，然后进行一次高斯平滑，并根据参数img_dbl决定是否将图像尺寸放大为原图的2倍
static IplImage* create_init_img( IplImage*, int, double );
//将输入图像转换为32位灰度图,并进行归一化
static IplImage* convert_to_gray32( IplImage* );
//根据输入参数建立高斯金字塔
static IplImage*** build_gauss_pyr( IplImage*, int, int, double );
//对输入图像做下采样生成其四分之一大小的图像(每个维度上减半)，使用最近邻差值方法
static IplImage* downsample( IplImage* );
//通过对高斯金字塔中每相邻两层图像相减来建立高斯差分金字塔
static IplImage*** build_dog_pyr( IplImage***, int, int );
//在尺度空间中检测极值点，通过插值精确定位，去除低对比度的点，去除边缘点，返回检测到的特征点序列
static CvSeq* scale_space_extrema( IplImage***, int, int, double, int, CvMemStorage*);
//通过在尺度空间中将一个像素点的值与其周围3*3*3邻域内的点比较来决定此点是否极值点(极大值或极小都行)
static int is_extremum( IplImage***, int, int, int, int );
//通过亚像素级插值进行极值点精确定位(修正极值点坐标)，并去除低对比度的极值点，将修正后的特征点组成feature结构返回
static struct feature* interp_extremum( IplImage***, int, int, int, int, int, double);
//进行一次极值点差值，计算x，y，σ方向(层方向)上的子像素偏移量(增量)
static void interp_step( IplImage***, int, int, int, int, double*, double*, double* );
//在DoG金字塔中计算某点的x方向、y方向以及尺度方向上的偏导数
static CvMat* deriv_3D( IplImage***, int, int, int, int );
//在DoG金字塔中计算某点的3*3海森矩阵
static CvMat* hessian_3D( IplImage***, int, int, int, int );
//计算被插值点的对比度：D + 0.5 * dD^T * X
static double interp_contr( IplImage***, int, int, int, int, double, double, double );
//为一个feature结构分配空间并初始化
static struct feature* new_feature( void );
//去除边缘响应，即通过计算主曲率比值判断某点是否边缘点
static int is_too_edge_like( IplImage*, int, int, int );
//计算特征点序列中每个特征点的尺度
static void calc_feature_scales( CvSeq*, double, int );
//将特征点序列中每个特征点的坐标减半(当设置了将图像放大为原图的2倍时，特征点检测完之后调用)
static void adjust_for_img_dbl( CvSeq* );
//计算每个特征点的梯度直方图，找出其主方向，若一个特征点有不止一个主方向，将其分为两个特征点
static void calc_feature_oris( CvSeq*, IplImage*** );
//计算指定像素点的梯度方向直方图，返回存放直方图的数组
static double* ori_hist( IplImage*, int, int, int, int, double );
//计算指定点的梯度的幅值magnitude和方向orientation
static int calc_grad_mag_ori( IplImage*, int, int, double*, double* );
//对梯度方向直方图进行高斯平滑，弥补因没有仿射不变性而产生的特征点不稳定的问题
static void smooth_ori_hist( double*, int );
//查找梯度直方图中主方向的梯度幅值，即查找直方图中最大bin的值
static double dominant_ori( double*, int );
//若当前特征点的直方图中某个bin的值大于给定的阈值，则新生成一个特征点并添加到特征点序列末尾
static void add_good_ori_features( CvSeq*, double*, int, double, struct feature* );
//对输入的feature结构特征点做深拷贝，返回克隆生成的特征点的指针
static struct feature* clone_feature( struct feature* );
//计算特征点序列中每个特征点的特征描述子向量
static void compute_descriptors( CvSeq*, IplImage***, int, int );
//计算特征点附近区域的方向直方图，此直方图在计算特征描述子中要用到，返回值是一个d*d*n的三维数组
static double*** descr_hist( IplImage*, int, int, double, double, int, int );
static void interp_hist_entry( double***, double, double, double, double, int, int);
//将某特征点的方向直方图转换为特征描述子向量，对特征描述子归一化并将所有元素转化为整型，存入指定特征点中
static void hist_to_descr( double***, int, int, struct feature* );
//归一化特征点的特征描述子，即将特征描述子数组中每个元素除以特征描述子的模
static void normalize_descr( struct feature* );
//比较函数，将特征点按尺度的降序排列，用在序列排序函数CvSeqSort中
static int feature_cmp( void*, void*, void* );
//释放计算特征描述子过程中用到的方向直方图的内存空间
static void release_descr_hist( double****, int );
//释放金字塔图像组的存储空间
static void release_pyr( IplImage****, int, int );


/*********************** Functions prototyped in sift.h **********************/

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
@param feat a pointer to an array in which to store detected features

@return Returns the number of features stored in \a feat or -1 on failure
@see _sift_features()
*/
int sift_features( IplImage* img, struct feature** feat )
{
    //调用_sift_features()函数进行特征点检测
	return _sift_features( img, feat, SIFT_INTVLS, SIFT_SIGMA, SIFT_CONTR_THR,
							SIFT_CURV_THR, SIFT_IMG_DBL, SIFT_DESCR_WIDTH,
							SIFT_DESCR_HIST_BINS );
}


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
Finds SIFT features in an image using user-specified parameter values.  All
detected features are stored in the array pointed to by \a feat.

@param img the image in which to detect features
@param feat a pointer to an array in which to store detected features
@param intvls the number of intervals sampled per octave of scale space
@param sigma the amount of Gaussian smoothing applied to each image level
	before building the scale space representation for an octave
@param cont_thr a threshold on the value of the scale space function
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
@see sift_keypoints()
*/
int _sift_features( IplImage* img, struct feature** feat, int intvls,
				   double sigma, double contr_thr, int curv_thr,
				   int img_dbl, int descr_width, int descr_hist_bins )
{
    IplImage* init_img;//原图经初始化后的图像，归一化的32位灰度图
    IplImage*** gauss_pyr, *** dog_pyr;//三级指针，高斯金字塔图像组，DoG金字塔图像组
    CvMemStorage* storage;//存储器
    CvSeq* features;//存储特征点的序列，序列中存放的是struct feature类型的指针
	int octvs, i, n = 0;

    //输入参数检查
	/* check arguments */
	if( ! img )
		fatal_error( "NULL pointer error, %s, line %d",  __FILE__, __LINE__ );

	if( ! feat )
		fatal_error( "NULL pointer error, %s, line %d",  __FILE__, __LINE__ );

	/* build scale space pyramid; smallest dimension of top level is ~4 pixels */

    //步骤一：建立尺度空间，即建立高斯差分(DoG)金字塔dog_pyr
    //将原图转换为32位灰度图并归一化，然后进行一次高斯平滑，并根据参数img_dbl决定是否将图像尺寸放大为原图的2倍
	init_img = create_init_img( img, img_dbl, sigma );
    //计算高斯金字塔的组数octvs
    octvs = log( MIN( init_img->width, init_img->height ) ) / log(2) - 2;
    //为了保证连续性，在每一层的顶层继续用高斯模糊生成3幅图像，所以高斯金字塔每组有intvls+3层，DOG金字塔每组有intvls+2层
    //建立高斯金字塔gauss_pyr，是一个octvs*(intvls+3)的图像数组
	gauss_pyr = build_gauss_pyr( init_img, octvs, intvls, sigma );
    //建立高斯差分(DoG)金字塔dog_pyr，是一个octvs*(intvls+2)的图像数组
	dog_pyr = build_dog_pyr( gauss_pyr, octvs, intvls );

    //步骤二：：在尺度空间中检测极值点，并进行精确定位和筛选
    //创建默认大小的内存存储器
	storage = cvCreateMemStorage( 0 );
    //在尺度空间中检测极值点，通过插值精确定位，去除低对比度的点，去除边缘点，返回检测到的特征点序列
    features = scale_space_extrema( dog_pyr, octvs, intvls, contr_thr, curv_thr, storage );
    //计算特征点序列features中每个特征点的尺度
	calc_feature_scales( features, sigma, intvls );
    //若设置了将图像放大为原图的2倍
    if( img_dbl )//将特征点序列中每个特征点的坐标减半(当设置了将图像放大为原图的2倍时，特征点检测完之后调用)
		adjust_for_img_dbl( features );

    //步骤三：特征点方向赋值，完成此步骤后，每个特征点有三个信息：位置、尺度、方向
    //计算每个特征点的梯度直方图，找出其主方向，若一个特征点有不止一个主方向，将其分为两个特征点
	calc_feature_oris( features, gauss_pyr );

    //步骤四：计算特征描述子
    //计算特征点序列中每个特征点的特征描述子向量
	compute_descriptors( features, gauss_pyr, descr_width, descr_hist_bins );

	/* sort features by decreasing scale and move from CvSeq to array */
    //按特征点尺度的降序排列序列中元素的顺序，feature_cmp是自定义的比较函数
	cvSeqSort( features, (CvCmpFunc)feature_cmp, NULL );

    //将CvSeq类型的特征点序列features转换为通用的struct feature类型的数组feat
    n = features->total;//特征点个数
    *feat = calloc( n, sizeof(struct feature) );//分配控件
    //将序列features中的元素拷贝到数组feat中，返回数组指针给feat
	*feat = cvCvtSeqToArray( features, *feat, CV_WHOLE_SEQ );

    //释放特征点数组feat中所有特征点的feature_data成员，因为此成员中的数据在检测完特征点后就没用了
	for( i = 0; i < n; i++ )
	{
		free( (*feat)[i].feature_data );
		(*feat)[i].feature_data = NULL;
	}

    //释放各种临时数据的存储空间
    cvReleaseMemStorage( &storage );//释放内存存储器
    cvReleaseImage( &init_img );//释放初始化后的图像
    release_pyr( &gauss_pyr, octvs, intvls + 3 );//释放高斯金字塔图像组
    release_pyr( &dog_pyr, octvs, intvls + 2 );//释放高斯差分金字塔图像组

    return n;//返回检测到的特征点的个数
}



/******************************* 本地函数的实现 ********************************/
/************************ Functions prototyped here **************************/

/*将原图转换为32位灰度图并归一化，然后进行一次高斯平滑，并根据参数img_dbl决定是否将图像尺寸放大为原图的2倍
参数：
img：输入的原图像
img_dbl：是否将图像放大为之前的两倍
sigma：初始高斯平滑参数σ
返回值：初始化完成的图像
*/
/*
Converts an image to 32-bit grayscale and Gaussian-smooths it.  The image is
optionally doubled in size prior to smoothing.
@param img input image
@param img_dbl if true, image is doubled in size prior to smoothing
@param sigma total std of Gaussian smoothing
*/
static IplImage* create_init_img( IplImage* img, int img_dbl, double sigma )
{
	IplImage* gray, * dbl;
	float sig_diff;

    //调用函数，将输入图像转换为32位灰度图，并归一化
	gray = convert_to_gray32( img );

    //若设置了将图像放大为原图的2倍
	if( img_dbl )
	{
        //将图像长宽扩展一倍时，便有了底-1层，该层尺度为：
		sig_diff = sqrt( sigma * sigma - SIFT_INIT_SIGMA * SIFT_INIT_SIGMA * 4 );
        dbl = cvCreateImage( cvSize( img->width*2, img->height*2 ),IPL_DEPTH_32F, 1 );//创建放大图像
        cvResize( gray, dbl, CV_INTER_CUBIC );//放大原图的尺寸
        //高斯平滑，高斯核在x,y方向上的标准差都是sig_diff
        cvSmooth( dbl, dbl, CV_GAUSSIAN, 0, 0, sig_diff, sig_diff );
		cvReleaseImage( &gray );
		return dbl;
	}
    else//不用放大为原图的2倍
	{
        //计算第0层的尺度
		sig_diff = sqrt( sigma * sigma - SIFT_INIT_SIGMA * SIFT_INIT_SIGMA );
        //高斯平滑，高斯核在x,y方向上的标准差都是sig_diff
		cvSmooth( gray, gray, CV_GAUSSIAN, 0, 0, sig_diff, sig_diff );
		return gray;
	}
}


/*将输入图像转换为32位灰度图,并进行归一化
参数：
img：输入图像，3通道8位彩色图(BGR)或8位灰度图
返回值：32位灰度图
*/
/*
Converts an image to 32-bit grayscale
@param img a 3-channel 8-bit color (BGR) or 8-bit gray image
@return Returns a 32-bit grayscale image
*/
static IplImage* convert_to_gray32( IplImage* img )
{
	IplImage* gray8, * gray32;

    gray32 = cvCreateImage( cvGetSize(img), IPL_DEPTH_32F, 1 );//创建32位单通道图像

    //首先将原图转换为8位单通道图像
    if( img->nChannels == 1 )//若原图本身就是单通道，直接克隆原图
		gray8 = cvClone( img );
    else//若原图是3通道图像
	{
        gray8 = cvCreateImage( cvGetSize(img), IPL_DEPTH_8U, 1 );//创建8位单通道图像
        cvCvtColor( img, gray8, CV_BGR2GRAY );//将原图转换为8为单通道图像
	}

    //然后将8为单通道图像gray8转换为32位单通道图像，并进行归一化处理(除以255)
	cvConvertScale( gray8, gray32, 1.0 / 255.0, 0 );

    cvReleaseImage( &gray8 );//释放临时图像

    return gray32;//返回32位单通道图像
}


/*根据输入参数建立高斯金字塔
参数：
base：输入图像，作为高斯金字塔的基图像
octvs：高斯金字塔的组数
intvls：每组的层数
sigma：初始尺度
返回值：高斯金字塔，是一个octvs*(intvls+3)的图像数组
*/
/*
Builds Gaussian scale space pyramid from an image
@param base base image of the pyramid
@param octvs number of octaves of scale space
@param intvls number of intervals per octave
@param sigma amount of Gaussian smoothing per octave
@return Returns a Gaussian scale space pyramid as an octvs x (intvls + 3) array
*/
static IplImage*** build_gauss_pyr( IplImage* base, int octvs,
									int intvls, double sigma )
{
	IplImage*** gauss_pyr;
    //为了保证连续性，在每一层的顶层继续用高斯模糊生成3幅图像，所以高斯金字塔每组有intvls+3层，DOG金字塔每组有intvls+2层
    double* sig = calloc( intvls + 3, sizeof(double));//每层的sigma参数数组
	double sig_total, sig_prev, k;
	int i, o;

    //为高斯金字塔gauss_pyr分配空间，共octvs个元素，每个元素是一组图像的首指针
    gauss_pyr = calloc( octvs, sizeof( IplImage** ) );
    //为第i组图像gauss_pyr[i]分配空间，共intvls+3个元素，每个元素是一个图像指针
	for( i = 0; i < octvs; i++ )
		gauss_pyr[i] = calloc( intvls + 3, sizeof( IplImage* ) );

	/*	precompute Gaussian sigmas using the following formula:
        sigma_{total}^2 = sigma_{i}^2 + sigma_{i-1}^2   */
    //计算每次高斯模糊的sigma参数
    sig[0] = sigma;//初始尺度
	k = pow( 2.0, 1.0 / intvls );
	for( i = 1; i < intvls + 3; i++ )
	{
        sig_prev = pow( k, i - 1 ) * sigma;//i-1层的尺度
        sig_total = sig_prev * k;//i层的尺度
        sig[i] = sqrt( sig_total * sig_total - sig_prev * sig_prev );//不懂这里为什么？
	}

    //逐组逐层生成高斯金字塔
    for( o = 0; o < octvs; o++ )//遍历组
        for( i = 0; i < intvls + 3; i++ )//遍历层
		{
            if( o == 0  &&  i == 0 )//第0组，第0层，就是原图像
				gauss_pyr[o][i] = cvCloneImage(base);
            else if( i == 0 )//新的一组的首层图像是由上一组最后一层图像下采样得到
				gauss_pyr[o][i] = downsample( gauss_pyr[o-1][intvls] );
            else//对上一层图像进行高斯平滑得到当前层图像
            {   //创建图像
				gauss_pyr[o][i] = cvCreateImage( cvGetSize(gauss_pyr[o][i-1]),IPL_DEPTH_32F, 1 );
                //对上一层图像gauss_pyr[o][i-1]进行参数为sig[i]的高斯平滑，得到当前层图像gauss_pyr[o][i]
                cvSmooth( gauss_pyr[o][i-1], gauss_pyr[o][i], CV_GAUSSIAN, 0, 0, sig[i], sig[i] );
			}
		}

    free( sig );//释放sigma参数数组

    return gauss_pyr;//返回高斯金字塔
}


/*对输入图像做下采样生成其四分之一大小的图像(每个维度上减半)，使用最近邻差值方法
参数：
img：输入图像
返回值：下采样后的图像
*/
/*
Downsamples an image to a quarter of its size (half in each dimension)
using nearest-neighbor interpolation
@param img an image
@return Returns an image whose dimensions are half those of img
*/
static IplImage* downsample( IplImage* img )
{
    //下采样图像
    IplImage* smaller = cvCreateImage( cvSize(img->width / 2, img->height / 2), img->depth, img->nChannels );
    cvResize( img, smaller, CV_INTER_NN );//尺寸变换

	return smaller;
}


/*通过对高斯金字塔中每相邻两层图像相减来建立高斯差分金字塔
参数：
gauss_pyr：高斯金字塔
octvs：组数
intvls：每组的层数
返回值：高斯差分金字塔，是一个octvs*(intvls+2)的图像数组
*/
/*
Builds a difference of Gaussians scale space pyramid by subtracting adjacent
intervals of a Gaussian pyramid
@param gauss_pyr Gaussian scale-space pyramid
@param octvs number of octaves of scale space
@param intvls number of intervals per octave
@return Returns a difference of Gaussians scale space pyramid as an octvs x (intvls + 2) array
*/
static IplImage*** build_dog_pyr( IplImage*** gauss_pyr, int octvs, int intvls )
{
	IplImage*** dog_pyr;
	int i, o;

    //为高斯差分金字塔分配空间，共octvs个元素，每个元素是一组图像的首指针
	dog_pyr = calloc( octvs, sizeof( IplImage** ) );
    //为第i组图像dog_pyr[i]分配空间，共(intvls+2)个元素，每个元素是一个图像指针
	for( i = 0; i < octvs; i++ )
		dog_pyr[i] = calloc( intvls + 2, sizeof(IplImage*) );

    //逐组逐层计算差分图像
    for( o = 0; o < octvs; o++ )//遍历组
        for( i = 0; i < intvls + 2; i++ )//遍历层
        {   //创建DoG金字塔的第o组第i层的差分图像
            dog_pyr[o][i] = cvCreateImage( cvGetSize(gauss_pyr[o][i]), IPL_DEPTH_32F, 1 );
            //将高斯金字塔的第o组第i+1层图像和第i层图像相减来得到DoG金字塔的第o组第i层图像
			cvSub( gauss_pyr[o][i+1], gauss_pyr[o][i], dog_pyr[o][i], NULL );
		}

    return dog_pyr;//返回高斯差分金字塔
}


/*在尺度空间中检测极值点，通过插值精确定位，去除低对比度的点，去除边缘点，返回检测到的特征点序列
参数：
dog_pyr：高斯差分金字塔
octvs：高斯差分金字塔的组数
intvls：每组的层数
contr_thr：对比度阈值，针对归一化后的图像，用来去除不稳定特征
cur_thr：主曲率比值的阈值，用来去除边缘特征
storage：存储器
返回值：返回检测到的特征点的序列
*/
/*
Detects features at extrema in DoG scale space.  Bad features are discarded
based on contrast and ratio of principal curvatures.
@param dog_pyr DoG scale space pyramid
@param octvs octaves of scale space represented by dog_pyr
@param intvls intervals per octave
@param contr_thr low threshold on feature contrast
@param curv_thr high threshold on feature ratio of principal curvatures
@param storage memory storage in which to store detected features
@return Returns an array of detected features whose scales, orientations,
	and descriptors are yet to be determined.
*/
static CvSeq* scale_space_extrema( IplImage*** dog_pyr, int octvs, int intvls,
								   double contr_thr, int curv_thr, CvMemStorage* storage )
{
    CvSeq* features;//特征点序列
    double prelim_contr_thr = 0.5 * contr_thr / intvls;//像素的对比度阈值
	struct feature* feat;
	struct detection_data* ddata;
	int o, i, r, c;

    //在存储器storage上创建存储极值点的序列，其中存储feature结构类型的数据
	features = cvCreateSeq( 0, sizeof(CvSeq), sizeof(struct feature), storage );

    /*遍历高斯差分金字塔，检测极值点*/
    //SIFT_IMG_BORDER指明边界宽度，只检测边界线以内的极值点
    for( o = 0; o < octvs; o++ )//第o组
        for( i = 1; i <= intvls; i++ )//遍i层
            for(r = SIFT_IMG_BORDER; r < dog_pyr[o][0]->height-SIFT_IMG_BORDER; r++)//第r行
                for(c = SIFT_IMG_BORDER; c < dog_pyr[o][0]->width-SIFT_IMG_BORDER; c++)//第c列
                    //进行初步的对比度检查，只有当归一化后的像素值大于对比度阈值prelim_contr_thr时才继续检测此像素点是否可能是极值
                    //调用函数pixval32f获取图像dog_pyr[o][i]的第r行第c列的点的坐标值，然后调用ABS宏求其绝对值
					if( ABS( pixval32f( dog_pyr[o][i], r, c ) ) > prelim_contr_thr )
                        //通过在尺度空间中将一个像素点的值与其周围3*3*3邻域内的点比较来决定此点是否极值点(极大值或极小都行)
                        if( is_extremum( dog_pyr, o, i, r, c ) )//若是极值点
						{
                            //由于极值点的检测是在离散空间中进行的，所以检测到的极值点并不一定是真正意义上的极值点
                            //因为真正的极值点可能位于两个像素之间，而在离散空间中只能精确到坐标点精度上
                            //通过亚像素级插值进行极值点精确定位(修正极值点坐标)，并去除低对比度的极值点，将修正后的特征点组成feature结构返回
							feat = interp_extremum(dog_pyr, o, i, r, c, intvls, contr_thr);
                            //返回值非空，表明此点已被成功修正
                            if( feat )
							{
                                //调用宏feat_detection_data来提取参数feat中的feature_data成员并转换为detection_data类型的指针
								ddata = feat_detection_data( feat );
                                //去除边缘响应，即通过计算主曲率比值判断某点是否边缘点,返回值为0表示不是边缘点，可做特征点
                                if( ! is_too_edge_like( dog_pyr[ddata->octv][ddata->intvl], ddata->r, ddata->c, curv_thr ) )
								{
                                    cvSeqPush( features, feat );//向特征点序列features末尾插入新检测到的特征点feat
								}
								else
									free( ddata );
								free( feat );
							}
						}

    return features;//返回特征点序列
}


/*通过在尺度空间中将一个像素点的值与其周围3*3*3邻域内的点比较来决定此点是否极值点(极大值或极小都行)
参数：
dog_pyr：高斯差分金字塔
octv：像素点所在的组
intvl：像素点所在的层
r：像素点所在的行
c：像素点所在的列
返回值：若指定的像素点是极值点(极大值或极小值)，返回1；否则返回0
*/
/*
Determines whether a pixel is a scale-space extremum by comparing it to it's 3x3x3 pixel neighborhood.
@param dog_pyr DoG scale space pyramid
@param octv pixel's scale space octave
@param intvl pixel's within-octave interval
@param r pixel's image row
@param c pixel's image col
@return Returns 1 if the specified pixel is an extremum (max or min) among it's 3x3x3 pixel neighborhood.
*/
static int is_extremum( IplImage*** dog_pyr, int octv, int intvl, int r, int c )
{
    //调用函数pixval32f获取图像dog_pyr[octv][intvl]的第r行第c列的点的坐标值
	float val = pixval32f( dog_pyr[octv][intvl], r, c );
	int i, j, k;

    //检查是否最大值
	if( val > 0 )
	{
        for( i = -1; i <= 1; i++ )//层
            for( j = -1; j <= 1; j++ )//行
                for( k = -1; k <= 1; k++ )//列
					if( val < pixval32f( dog_pyr[octv][intvl+i], r + j, c + k ) )
						return 0;
	}
    //检查是否最小值
	else
	{
        for( i = -1; i <= 1; i++ )//层
            for( j = -1; j <= 1; j++ )//行
                for( k = -1; k <= 1; k++ )//列
					if( val > pixval32f( dog_pyr[octv][intvl+i], r + j, c + k ) )
						return 0;
	}

	return 1;
}


/*通过亚像素级插值进行极值点精确定位(修正极值点坐标)，并去除低对比度的极值点，将修正后的特征点组成feature结构返回
参数：
dog_pyr：高斯差分金字塔
octv：像素点所在的组
intvl：像素点所在的层
r：像素点所在的行
c：像素点所在的列
intvls：每组的层数
contr_thr：对比度阈值，针对归一化后的图像，用来去除不稳定特征
返回值：返回经插值修正后的特征点(feature类型)；若经有限次插值依然无法精确到理想情况或者该点对比度过低，返回NULL
*/
/*
Interpolates a scale-space extremum's location and scale to subpixel
accuracy to form an image feature.  Rejects features with low contrast.
Based on Section 4 of Lowe's paper.
@param dog_pyr DoG scale space pyramid
@param octv feature's octave of scale space
@param intvl feature's within-octave interval
@param r feature's image row
@param c feature's image column
@param intvls total intervals per octave
@param contr_thr threshold on feature contrast
@return Returns the feature resulting from interpolation of the given
	parameters or NULL if the given location could not be interpolated or
	if contrast at the interpolated loation was too low.  If a feature is
	returned, its scale, orientation, and descriptor are yet to be determined.
*/
static struct feature* interp_extremum( IplImage*** dog_pyr, int octv, int intvl,
										int r, int c, int intvls, double contr_thr )
{
    struct feature* feat;//修正后的特征点
    struct detection_data* ddata;//与特征检测有关的结构，存在feature结构的feature_data成员中
    double xi, xr, xc, contr;//xi,xr,xc分别为亚像素的intvl(层),row(y),col(x)方向上的增量(偏移量)
    int i = 0;//插值次数

    //SIFT_MAX_INTERP_STEPS指定了关键点的最大插值次数，即最多修正多少次，默认是5
	while( i < SIFT_MAX_INTERP_STEPS )
	{
        //进行一次极值点差值，计算σ(层方向,intvl方向)，y，x方向上的子像素偏移量(增量)
		interp_step( dog_pyr, octv, intvl, r, c, &xi, &xr, &xc );
        //若在任意方向上的偏移量大于0.5时，意味着差值中心已经偏移到它的临近点上，所以必须改变当前关键点的位置坐标
        if( ABS( xi ) < 0.5  &&  ABS( xr ) < 0.5  &&  ABS( xc ) < 0.5 )//若三方向上偏移量都小于0.5，表示已经够精确，则不用继续插值
			break;

        //修正关键点的坐标，x,y,σ三方向上的原坐标加上偏移量取整(四舍五入)
        c += cvRound( xc );//x坐标修正
        r += cvRound( xr );//y坐标修正
        intvl += cvRound( xi );//σ方向，即层方向

        //若坐标修正后超出范围，则结束插值，返回NULL
        if( intvl < 1  ||           //层坐标插之后越界
			intvl > intvls  ||
            c < SIFT_IMG_BORDER  ||   //行列坐标插之后到边界线内
			r < SIFT_IMG_BORDER  ||
			c >= dog_pyr[octv][0]->width - SIFT_IMG_BORDER  ||
			r >= dog_pyr[octv][0]->height - SIFT_IMG_BORDER )
		{
			return NULL;
		}

		i++;
	}

    //若经过SIFT_MAX_INTERP_STEPS次插值后还没有修正到理想的精确位置，则返回NULL，即舍弃此极值点
	if( i >= SIFT_MAX_INTERP_STEPS )
		return NULL;

    //计算被插值点的对比度：D + 0.5 * dD^T * X
	contr = interp_contr( dog_pyr, octv, intvl, r, c, xi, xr, xc );
    if( ABS( contr ) < contr_thr / intvls )//若该点对比度过小，舍弃，返回NULL
		return NULL;

    //为一个特征点feature结构分配空间并初始化，返回特征点指针
	feat = new_feature();
    //调用宏feat_detection_data来提取参数feat中的feature_data成员并转换为detection_data类型的指针
	ddata = feat_detection_data( feat );

    //将修正后的坐标赋值给特征点feat
    //原图中特征点的x坐标，因为第octv组中的图的尺寸比原图小2^octv倍，所以坐标值要乘以2^octv
    feat->img_pt.x = feat->x = ( c + xc ) * pow( 2.0, octv );
    //原图中特征点的y坐标，因为第octv组中的图的尺寸比原图小2^octv倍，所以坐标值要乘以2^octv
	feat->img_pt.y = feat->y = ( r + xr ) * pow( 2.0, octv );

    ddata->r = r;//特征点所在的行
    ddata->c = c;//特征点所在的列
    ddata->octv = octv;//高斯差分金字塔中，特征点所在的组
    ddata->intvl = intvl;//高斯差分金字塔中，特征点所在的组中的层
    ddata->subintvl = xi;//特征点在层方向(σ方向,intvl方向)上的亚像素偏移量

    return feat;//返回特征点指针
}


/*进行一次极值点差值，计算x，y，σ方向(层方向)上的子像素偏移量(增量)
参数：
dog_pyr：高斯差分金字塔
octv：像素点所在的组
intvl：像素点所在的层
r：像素点所在的行
c：像素点所在的列
xi：输出参数，层方向上的子像素增量(偏移)
xr：输出参数，y方向上的子像素增量(偏移)
xc：输出参数，x方向上的子像素增量(偏移)
*/
/*
Performs one step of extremum interpolation.  Based on Eqn. (3) in Lowe's paper.
@param dog_pyr difference of Gaussians scale space pyramid
@param octv octave of scale space
@param intvl interval being interpolated
@param r row being interpolated
@param c column being interpolated
@param xi output as interpolated subpixel increment to interval
@param xr output as interpolated subpixel increment to row
@param xc output as interpolated subpixel increment to col
*/
static void interp_step( IplImage*** dog_pyr, int octv, int intvl, int r, int c,
						 double* xi, double* xr, double* xc )
{
	CvMat* dD, * H, * H_inv, X;
	double x[3] = { 0 };

    //在DoG金字塔中计算某点的x方向、y方向以及尺度方向上的偏导数，结果存放在列向量dD中
	dD = deriv_3D( dog_pyr, octv, intvl, r, c );
    //在DoG金字塔中计算某点的3*3海森矩阵
	H = hessian_3D( dog_pyr, octv, intvl, r, c );
    H_inv = cvCreateMat( 3, 3, CV_64FC1 );//海森矩阵的逆阵
	cvInvert( H, H_inv, CV_SVD );
	cvInitMatHeader( &X, 3, 1, CV_64FC1, x, CV_AUTOSTEP );
    //X = - H^(-1) * dD，H的三个元素分别是x,y,σ方向上的偏移量(具体见SIFT算法说明)
	cvGEMM( H_inv, dD, -1, NULL, 0, &X, 0 );

	cvReleaseMat( &dD );
	cvReleaseMat( &H );
	cvReleaseMat( &H_inv );

    *xi = x[2];//σ方向(层方向)偏移量
    *xr = x[1];//y方向上偏移量
    *xc = x[0];//x方向上偏移量
}


/*在DoG金字塔中计算某点的x方向、y方向以及尺度方向上的偏导数
参数：
dog_pyr：高斯差分金字塔
octv：像素点所在的组
intvl：像素点所在的层
r：像素点所在的行
c：像素点所在的列
返回值：返回3个偏导数组成的列向量{ dI/dx, dI/dy, dI/ds }^T
*/
/*
Computes the partial derivatives in x, y, and scale of a pixel in the DoG scale space pyramid.
@param dog_pyr DoG scale space pyramid
@param octv pixel's octave in dog_pyr
@param intvl pixel's interval in octv
@param r pixel's image row
@param c pixel's image col
@return Returns the vector of partial derivatives for pixel I
	{ dI/dx, dI/dy, dI/ds }^T as a CvMat*
*/
static CvMat* deriv_3D( IplImage*** dog_pyr, int octv, int intvl, int r, int c )
{
	CvMat* dI;
	double dx, dy, ds;

    //求差分来代替偏导，这里是用的隔行求差取中值的梯度计算方法
    //求x方向上的差分来近似代替偏导数
	dx = ( pixval32f( dog_pyr[octv][intvl], r, c+1 ) -
		pixval32f( dog_pyr[octv][intvl], r, c-1 ) ) / 2.0;
    //求y方向上的差分来近似代替偏导数
	dy = ( pixval32f( dog_pyr[octv][intvl], r+1, c ) -
		pixval32f( dog_pyr[octv][intvl], r-1, c ) ) / 2.0;
    //求层间的差分来近似代替尺度方向上的偏导数
	ds = ( pixval32f( dog_pyr[octv][intvl+1], r, c ) -
		pixval32f( dog_pyr[octv][intvl-1], r, c ) ) / 2.0;

    //组成列向量
	dI = cvCreateMat( 3, 1, CV_64FC1 );
	cvmSet( dI, 0, 0, dx );
	cvmSet( dI, 1, 0, dy );
	cvmSet( dI, 2, 0, ds );

	return dI;
}


/*在DoG金字塔中计算某点的3*3海森矩阵
    / Ixx  Ixy  Ixs \
    | Ixy  Iyy  Iys |
    \ Ixs  Iys  Iss /
参数：
dog_pyr：高斯差分金字塔
octv：像素点所在的组
intvl：像素点所在的层
r：像素点所在的行
c：像素点所在的列
返回值：返回3*3的海森矩阵
*/
/*
Computes the 3D Hessian matrix for a pixel in the DoG scale space pyramid.
@param dog_pyr DoG scale space pyramid
@param octv pixel's octave in dog_pyr
@param intvl pixel's interval in octv
@param r pixel's image row
@param c pixel's image col
@return Returns the Hessian matrix (below) for pixel I as a CvMat*

    / Ixx  Ixy  Ixs \
    | Ixy  Iyy  Iys |
	\ Ixs  Iys  Iss /
*/
static CvMat* hessian_3D( IplImage*** dog_pyr, int octv, int intvl, int r, int c )
{
	CvMat* H;
	double v, dxx, dyy, dss, dxy, dxs, dys;

    v = pixval32f( dog_pyr[octv][intvl], r, c );//该点的像素值

    //用差分近似代替倒数(具体公式见各种梯度的求法)
    //dxx = f(i+1,j) - 2f(i,j) + f(i-1,j)
    //dyy = f(i,j+1) - 2f(i,j) + f(i,j-1)
	dxx = ( pixval32f( dog_pyr[octv][intvl], r, c+1 ) +
			pixval32f( dog_pyr[octv][intvl], r, c-1 ) - 2 * v );
	dyy = ( pixval32f( dog_pyr[octv][intvl], r+1, c ) +
			pixval32f( dog_pyr[octv][intvl], r-1, c ) - 2 * v );
	dss = ( pixval32f( dog_pyr[octv][intvl+1], r, c ) +
			pixval32f( dog_pyr[octv][intvl-1], r, c ) - 2 * v );
	dxy = ( pixval32f( dog_pyr[octv][intvl], r+1, c+1 ) -
			pixval32f( dog_pyr[octv][intvl], r+1, c-1 ) -
			pixval32f( dog_pyr[octv][intvl], r-1, c+1 ) +
			pixval32f( dog_pyr[octv][intvl], r-1, c-1 ) ) / 4.0;
	dxs = ( pixval32f( dog_pyr[octv][intvl+1], r, c+1 ) -
			pixval32f( dog_pyr[octv][intvl+1], r, c-1 ) -
			pixval32f( dog_pyr[octv][intvl-1], r, c+1 ) +
			pixval32f( dog_pyr[octv][intvl-1], r, c-1 ) ) / 4.0;
	dys = ( pixval32f( dog_pyr[octv][intvl+1], r+1, c ) -
			pixval32f( dog_pyr[octv][intvl+1], r-1, c ) -
			pixval32f( dog_pyr[octv][intvl-1], r+1, c ) +
			pixval32f( dog_pyr[octv][intvl-1], r-1, c ) ) / 4.0;

    //组成海森矩阵
	H = cvCreateMat( 3, 3, CV_64FC1 );
	cvmSet( H, 0, 0, dxx );
	cvmSet( H, 0, 1, dxy );
	cvmSet( H, 0, 2, dxs );
	cvmSet( H, 1, 0, dxy );
	cvmSet( H, 1, 1, dyy );
	cvmSet( H, 1, 2, dys );
	cvmSet( H, 2, 0, dxs );
	cvmSet( H, 2, 1, dys );
	cvmSet( H, 2, 2, dss );

	return H;
}


/*计算被插值点的对比度：D + 0.5 * dD^T * X
参数：
dog_pyr：高斯差分金字塔
octv：像素点所在的组
intvl：像素点所在的层
r：像素点所在的行
c：像素点所在的列
xi：层方向上的子像素增量
xr：y方向上的子像素增量
xc：x方向上的子像素增量
返回值：插值点的对比度
*/
/*
Calculates interpolated pixel contrast.  Based on Eqn. (3) in Lowe's paper.
@param dog_pyr difference of Gaussians scale space pyramid
@param octv octave of scale space
@param intvl within-octave interval
@param r pixel row
@param c pixel column
@param xi interpolated subpixel increment to interval
@param xr interpolated subpixel increment to row
@param xc interpolated subpixel increment to col
@param Returns interpolated contrast.
*/
static double interp_contr( IplImage*** dog_pyr, int octv, int intvl, int r,
							int c, double xi, double xr, double xc )
{
	CvMat* dD, X, T;
	double t[1], x[3] = { xc, xr, xi };

    //偏移量组成的列向量X，其中是x，y，σ三方向上的偏移量
	cvInitMatHeader( &X, 3, 1, CV_64FC1, x, CV_AUTOSTEP );
    //矩阵乘法的结果T，是一个数值
	cvInitMatHeader( &T, 1, 1, CV_64FC1, t, CV_AUTOSTEP );
    //在DoG金字塔中计算某点的x方向、y方向以及尺度方向上的偏导数，结果存放在列向量dD中
	dD = deriv_3D( dog_pyr, octv, intvl, r, c );
    //矩阵乘法：T = dD^T * X
	cvGEMM( dD, &X, 1, NULL, 0, &T,  CV_GEMM_A_T );
	cvReleaseMat( &dD );

    //返回计算出的对比度值：D + 0.5 * dD^T * X (具体公式推导见SIFT算法说明)
	return pixval32f( dog_pyr[octv][intvl], r, c ) + t[0] * 0.5;
}


/*为一个feature结构分配空间并初始化
返回值：初始化完成的feature结构的指针
*/
/*
Allocates and initializes a new feature
@return Returns a pointer to the new feature
*/
static struct feature* new_feature( void )
{
    struct feature* feat;//特征点指针
    struct detection_data* ddata;//与特征检测相关的结构

    feat = malloc( sizeof( struct feature ) );//分配空间
    memset( feat, 0, sizeof( struct feature ) );//清零
	ddata = malloc( sizeof( struct detection_data ) );
	memset( ddata, 0, sizeof( struct detection_data ) );
    feat->feature_data = ddata;//将特征检测相关的结构指针赋值给特征点的feature_data成员
    feat->type = FEATURE_LOWE;//默认是LOWE类型的特征点

	return feat;
}


/*去除边缘响应，即通过计算主曲率比值判断某点是否边缘点
参数：
dog_img：此特征点所在的DoG图像
r：特征点所在的行
c：特征点所在的列
cur_thr：主曲率比值的阈值，用来去除边缘特征
返回值：0：此点是非边缘点；1：此点是边缘点
*/
/*
Determines whether a feature is too edge like to be stable by computing the
ratio of principal curvatures at that feature.  Based on Section 4.1 of Lowe's paper.
@param dog_img image from the DoG pyramid in which feature was detected
@param r feature row
@param c feature col
@param curv_thr high threshold on ratio of principal curvatures
@return Returns 0 if the feature at (r,c) in dog_img is sufficiently corner-like or 1 otherwise.
*/
static int is_too_edge_like( IplImage* dog_img, int r, int c, int curv_thr )
{
	double d, dxx, dyy, dxy, tr, det;

    /*某点的主曲率与其海森矩阵的特征值成正比，为了避免直接计算特征值，这里只考虑特征值的比值
      可通过计算海森矩阵的迹tr(H)和行列式det(H)来计算特征值的比值
      设a是海森矩阵的较大特征值，b是较小的特征值，有a = r*b，r是大小特征值的比值
      tr(H) = a + b; det(H) = a*b;
      tr(H)^2 / det(H) = (a+b)^2 / ab = (r+1)^2/r
      r越大，越可能是边缘点；伴随r的增大，(r+1)^2/r 的值也增大，所以可通过(r+1)^2/r 判断主曲率比值是否满足条件*/
	/* principal curvatures are computed using the trace and det of Hessian */
    d = pixval32f(dog_img, r, c);//调用函数pixval32f获取图像dog_img的第r行第c列的点的坐标值

    //用差分近似代替偏导，求出海森矩阵的几个元素值
    /*  / dxx  dxy \
        \ dxy  dyy /   */
	dxx = pixval32f( dog_img, r, c+1 ) + pixval32f( dog_img, r, c-1 ) - 2 * d;
	dyy = pixval32f( dog_img, r+1, c ) + pixval32f( dog_img, r-1, c ) - 2 * d;
	dxy = ( pixval32f(dog_img, r+1, c+1) - pixval32f(dog_img, r+1, c-1) -
			pixval32f(dog_img, r-1, c+1) + pixval32f(dog_img, r-1, c-1) ) / 4.0;
    tr = dxx + dyy;//海森矩阵的迹
    det = dxx * dyy - dxy * dxy;//海森矩阵的行列式

    //若行列式为负，表明曲率有不同的符号，去除此点
	/* negative determinant -> curvatures have different signs; reject feature */
	if( det <= 0 )
        return 1;//返回1表明此点是边缘点

    //通过式子：(r+1)^2/r 判断主曲率的比值是否满足条件，若小于阈值，表明不是边缘点
	if( tr * tr / det < ( curv_thr + 1.0 )*( curv_thr + 1.0 ) / curv_thr )
        return 0;//不是边缘点
    return 1;//是边缘点
}


/*计算特征点序列中每个特征点的尺度
参数：
features：特征点序列
sigma：初始高斯平滑参数，即初始尺度
intvls：尺度空间中每组的层数
*/
/*
Calculates characteristic scale for each feature in an array.
@param features array of features
@param sigma amount of Gaussian smoothing per octave of scale space
@param intvls intervals per octave of scale space
*/
static void calc_feature_scales( CvSeq* features, double sigma, int intvls )
{
	struct feature* feat;
	struct detection_data* ddata;
	double intvl;
	int i, n;

    n = features->total;//总的特征点个数

    //遍历特征点
	for( i = 0; i < n; i++ )
	{
        //调用宏，获取序列features中的第i个元素，并强制转换为struct feature类型
        feat = CV_GET_SEQ_ELEM( struct feature, features, i );
        //调用宏feat_detection_data来提取参数feat中的feature_data成员并转换为detection_data类型的指针
		ddata = feat_detection_data( feat );
        //特征点所在的层数ddata->intvl加上特征点在层方向上的亚像素偏移量，得到特征点的较为精确的层数
        intvl = ddata->intvl + ddata->subintvl;
        //计算特征点的尺度(公式见SIFT算法说明)，并赋值给scl成员
		feat->scl = sigma * pow( 2.0, ddata->octv + intvl / intvls );
        //计算特征点所在的组的尺度，给detection_data的scl_octv成员赋值
		ddata->scl_octv = sigma * pow( 2.0, intvl / intvls );
	}
}


/*将特征点序列中每个特征点的坐标减半(当设置了将图像放大为原图的2倍时，特征点检测完之后调用)
参数：
features：特征点序列
*/
/*
Halves feature coordinates and scale in case the input image was doubled
prior to scale space construction.
@param features array of features
*/
static void adjust_for_img_dbl( CvSeq* features )
{
	struct feature* feat;
	int i, n;

    n = features->total;//总的特征点个数

    //遍历特征点
	for( i = 0; i < n; i++ )
	{
        //调用宏，获取序列features中的第i个元素，并强制转换为struct feature类型
		feat = CV_GET_SEQ_ELEM( struct feature, features, i );
        //将特征点的x,y坐标和尺度都减半
		feat->x /= 2.0;
		feat->y /= 2.0;
		feat->scl /= 2.0;
		feat->img_pt.x /= 2.0;
		feat->img_pt.y /= 2.0;
	}
}


/*计算每个特征点的梯度直方图，找出其主方向，若一个特征点有不止一个主方向，将其分为两个特征点
参数：
features：特征点序列
gauss_pyr：高斯金字塔
*/
/*
Computes a canonical orientation for each image feature in an array.  Based
on Section 5 of Lowe's paper.  This function adds features to the array when
there is more than one dominant orientation at a given feature location.
@param features an array of image features
@param gauss_pyr Gaussian scale space pyramid
*/
static void calc_feature_oris( CvSeq* features, IplImage*** gauss_pyr )
{
	struct feature* feat;
	struct detection_data* ddata;
    double* hist;//存放梯度直方图的数组
	double omax;
    int i, j, n = features->total;//特征点个数

    //遍历特征点序列
	for( i = 0; i < n; i++ )
	{
        //给每个特征点分配feature结构大小的内存
		feat = malloc( sizeof( struct feature ) );
        //移除列首元素，放到feat中
		cvSeqPopFront( features, feat );
        //调用宏feat_detection_data来提取参数feat中的feature_data成员并转换为detection_data类型的指针
        //detection_data数据中存放有此特征点的行列坐标和尺度，以及所在的层和组
		ddata = feat_detection_data( feat );

        //计算指定像素点的梯度方向直方图，返回存放直方图的数组给hist
        hist = ori_hist( gauss_pyr[ddata->octv][ddata->intvl],       //特征点所在的图像
                        ddata->r, ddata->c,                          //特征点的行列坐标
                        SIFT_ORI_HIST_BINS,                          //默认的梯度直方图的bin(柱子)个数
                        cvRound( SIFT_ORI_RADIUS * ddata->scl_octv ),//特征点方向赋值过程中，搜索邻域的半径为：3 * 1.5 * σ
                        SIFT_ORI_SIG_FCTR * ddata->scl_octv );       //计算直翻图时梯度幅值的高斯权重的初始值

        //对梯度直方图进行高斯平滑，弥补因没有仿射不变性而产生的特征点不稳定的问题,SIFT_ORI_SMOOTH_PASSES指定了平滑次数
		for( j = 0; j < SIFT_ORI_SMOOTH_PASSES; j++ )
			smooth_ori_hist( hist, SIFT_ORI_HIST_BINS );

        //查找梯度直方图中主方向的梯度幅值，即查找直方图中最大bin的值,返回给omax
		omax = dominant_ori( hist, SIFT_ORI_HIST_BINS );
        /*若当前特征点的直方图中某个bin的值大于给定的阈值，则新生成一个特征点并添加到特征点序列末尾
          传入的特征点指针feat是已经从特征点序列features中移除的，所以即使此特征点没有辅方向(第二个大于幅值阈值的方向)
          在函数add_good_ori_features中也会执行一次克隆feat，对其方向进行插值修正，并插入特征点序列的操作
          幅值阈值一般设置为当前特征点的梯度直方图的最大bin值的80%                   */
		add_good_ori_features( features, hist, SIFT_ORI_HIST_BINS,
								omax * SIFT_ORI_PEAK_RATIO, feat );
        //释放内存
		free( ddata );
		free( feat );
		free( hist );
	}
}


/*计算指定像素点的梯度方向直方图，返回存放直方图的数组
参数：
img：图像指针
r：特征点所在的行
c：特征点所在的列
n：直方图中柱(bin)的个数，默认是36
rad：区域半径，在此区域中计算梯度方向直方图
sigma：计算直翻图时梯度幅值的高斯权重的初始值
返回值：返回一个n元数组，其中是方向直方图的统计数据
*/
/*
Computes a gradient orientation histogram at a specified pixel.
@param img image
@param r pixel row
@param c pixel col
@param n number of histogram bins
@param rad radius of region over which histogram is computed
@param sigma std for Gaussian weighting of histogram entries
@return Returns an n-element array containing an orientation histogram
	representing orientations between 0 and 2 PI.
*/
static double* ori_hist( IplImage* img, int r, int c, int n, int rad, double sigma)
{
    double* hist;//直方图数组
	double mag, ori, w, exp_denom, PI2 = CV_PI * 2.0;
	int bin, i, j;

    //为直方图数组分配空间，共n个元素，n是柱的个数
	hist = calloc( n, sizeof( double ) );
	exp_denom = 2.0 * sigma * sigma;

    //遍历以指定点为中心的搜索区域
	for( i = -rad; i <= rad; i++ )
		for( j = -rad; j <= rad; j++ )
            //计算指定点的梯度的幅值mag和方向ori，返回值为1表示计算成功
			if( calc_grad_mag_ori( img, r + i, c + j, &mag, &ori ) )
			{
                w = exp( -( i*i + j*j ) / exp_denom );//该点的梯度幅值权重
                bin = cvRound( n * ( ori + CV_PI ) / PI2 );//计算梯度的方向对应的直方图中的bin下标
				bin = ( bin < n )? bin : 0;
                hist[bin] += w * mag;//在直方图的某个bin中累加加权后的幅值
			}

    return hist;//返回直方图数组
}


/*计算指定点的梯度的幅值magnitude和方向orientation
参数：
img：图像指针
r：特征点所在的行
c：特征点所在的列
img：输出参数，此点的梯度幅值
ori：输出参数，此点的梯度方向
返回值：如果指定的点是合法点并已计算出幅值和方向，返回1；否则返回0
*/
/*
Calculates the gradient magnitude and orientation at a given pixel.
@param img image
@param r pixel row
@param c pixel col
@param mag output as gradient magnitude at pixel (r,c)
@param ori output as gradient orientation at pixel (r,c)
@return Returns 1 if the specified pixel is a valid one and sets mag and
	ori accordingly; otherwise returns 0
*/
static int calc_grad_mag_ori( IplImage* img, int r, int c, double* mag, double* ori )
{
	double dx, dy;

    //对输入的坐标值进行检查
	if( r > 0  &&  r < img->height - 1  &&  c > 0  &&  c < img->width - 1 )
	{
        //用差分近似代替偏导，来求梯度的幅值和方向
        dx = pixval32f( img, r, c+1 ) - pixval32f( img, r, c-1 );//x方向偏导
        dy = pixval32f( img, r-1, c ) - pixval32f( img, r+1, c );//y方向偏导
        *mag = sqrt( dx*dx + dy*dy );//梯度的幅值，即梯度的模
        *ori = atan2( dy, dx );//梯度的方向
		return 1;
	}
    //行列坐标值不合法，返回0
	else
		return 0;
}


/*对梯度方向直方图进行高斯平滑，弥补因没有仿射不变性而产生的特征点不稳定的问题
参数：
hist：存放梯度直方图的数组
n：梯度直方图中bin的个数
*/
/*
Gaussian smooths an orientation histogram.
@param hist an orientation histogram
@param n number of bins
*/
static void smooth_ori_hist( double* hist, int n )
{
	double prev, tmp, h0 = hist[0];
	int i;

	prev = hist[n-1];
    //类似均值漂移的一种邻域平滑，减少突变的影响
	for( i = 0; i < n; i++ )
	{
		tmp = hist[i];
		hist[i] = 0.25 * prev + 0.5 * hist[i] +
			0.25 * ( ( i+1 == n )? h0 : hist[i+1] );
		prev = tmp;
	}
}


/*查找梯度直方图中主方向的梯度幅值，即查找直方图中最大bin的值
参数：
hist：存放直方图的数组
n：直方图中bin的个数
返回值：返回直方图中最大的bin的值
*/
/*
Finds the magnitude of the dominant orientation in a histogram
@param hist an orientation histogram
@param n number of bins
@return Returns the value of the largest bin in hist
*/
static double dominant_ori( double* hist, int n )
{
	double omax;
	int maxbin, i;

	omax = hist[0];
	maxbin = 0;

    //遍历直方图，找到最大的bin
	for( i = 1; i < n; i++ )
		if( hist[i] > omax )
		{
			omax = hist[i];
			maxbin = i;
		}
    return omax;//返回最大的bin的值
}


//根据左、中、右三个bin的值对当前bin进行直方图插值，以求取更精确的方向角度值
/*
Interpolates a histogram peak from left, center, and right values
*/
#define interp_hist_peak( l, c, r ) ( 0.5 * ((l)-(r)) / ((l) - 2.0*(c) + (r)) )


/*若当前特征点的直方图中某个bin的值大于给定的阈值，则新生成一个特征点并添加到特征点序列末尾
  传入的特征点指针feat是已经从特征点序列features中移除的，所以即使此特征点没有辅方向(第二个大于幅值阈值的方向)
  也会执行一次克隆feat，对其方向进行插值修正，并插入特征点序列的操作
参数：
features：特征点序列
hist：梯度直方图
n：直方图中bin的个数
mag_thr：幅值阈值，若直方图中有bin的值大于此阈值，则增加新特征点
feat：一个特征点指针，新的特征点克隆自feat，但方向不同
*/
/*
Adds features to an array for every orientation in a histogram greater than a specified threshold.
@param features new features are added to the end of this array
@param hist orientation histogram
@param n number of bins in hist
@param mag_thr new features are added for entries in hist greater than this
@param feat new features are clones of this with different orientations
*/
static void add_good_ori_features( CvSeq* features, double* hist, int n,
								   double mag_thr, struct feature* feat )
{
	struct feature* new_feat;
	double bin, PI2 = CV_PI * 2.0;
	int l, r, i;

    //遍历直方图
	for( i = 0; i < n; i++ )
	{
        l = ( i == 0 )? n - 1 : i-1;//前一个(左边的)bin的下标
        r = ( i + 1 ) % n;//后一个(右边的)bin的下标

        //若当前的bin是局部极值(比前一个和后一个bin都大)，并且值大于给定的幅值阈值，则新生成一个特征点并添加到特征点序列末尾
		if( hist[i] > hist[l]  &&  hist[i] > hist[r]  &&  hist[i] >= mag_thr )
		{
            //根据左、中、右三个bin的值对当前bin进行直方图插值
			bin = i + interp_hist_peak( hist[l], hist[i], hist[r] );
            bin = ( bin < 0 )? n + bin : ( bin >= n )? bin - n : bin;//将插值结果规范到[0,n]内
            new_feat = clone_feature( feat );//克隆当前特征点为新特征点
            new_feat->ori = ( ( PI2 * bin ) / n ) - CV_PI;//新特征点的方向
            cvSeqPush( features, new_feat );//插入到特征点序列末尾
			free( new_feat );
		}
	}
}


/*对输入的feature结构特征点做深拷贝，返回克隆生成的特征点的指针
参数：
feat：将要被克隆的特征点的指针
返回值：拷贝生成的特征点的指针
*/
/*
Makes a deep copy of a feature
@param feat feature to be cloned
@return Returns a deep copy of feat
*/
static struct feature* clone_feature( struct feature* feat )
{
	struct feature* new_feat;
	struct detection_data* ddata;

    //为一个feature结构分配空间并初始化
    new_feat = new_feature();
    //调用宏feat_detection_data来提取参数feat中的feature_data成员并转换为detection_data类型的指针
	ddata = feat_detection_data( new_feat );
    //对内存空间进行赋值
	memcpy( new_feat, feat, sizeof( struct feature ) );
	memcpy( ddata, feat_detection_data(feat), sizeof( struct detection_data ) );
	new_feat->feature_data = ddata;

    return new_feat;//返回克隆生成的特征点的指针
}


/*计算特征点序列中每个特征点的特征描述子向量
参数：
features：特征点序列
gauss_pyr：高斯金字塔图像组
d：计算方向直方图时，将特征点附近划分为d*d个区域，每个区域生成一个直方图
n：每个方向直方图的bin个数
*/
/*
Computes feature descriptors for features in an array.  Based on Section 6 of Lowe's paper.
@param features array of features
@param gauss_pyr Gaussian scale space pyramid
@param d width of 2D array of orientation histograms
@param n number of bins per orientation histogram
*/
static void compute_descriptors( CvSeq* features, IplImage*** gauss_pyr, int d, int n)
{
	struct feature* feat;
	struct detection_data* ddata;
    double*** hist;//d*d*n的三维直方图数组
    int i, k = features->total;//特征点的个数

    //遍历特征点序列中的特征点
	for( i = 0; i < k; i++ )
	{
        //调用宏，获取序列features中的第i个元素，并强制转换为struct feature类型
		feat = CV_GET_SEQ_ELEM( struct feature, features, i );
        //调用宏feat_detection_data来提取参数feat中的feature_data成员并转换为detection_data类型的指针
		ddata = feat_detection_data( feat );
        //计算特征点附近区域的方向直方图，此直方图在计算特征描述子中要用到，返回值是一个d*d*n的三维数组
		hist = descr_hist( gauss_pyr[ddata->octv][ddata->intvl], ddata->r,
			ddata->c, feat->ori, ddata->scl_octv, d, n );
        //将某特征点的方向直方图转换为特征描述子向量，对特征描述子归一化并将所有元素转化为整型，存入特征点feat中
		hist_to_descr( hist, d, n, feat );
        //释放特征点的方向直方图
		release_descr_hist( &hist, d );
	}
}


/*计算特征点附近区域的方向直方图，此直方图在计算特征描述子中要用到，返回值是一个d*d*n的三维数组
参数：
img：图像指针
r：特征点所在的行
c：特征点所在的列
ori：特征点的主方向
scl：特征点的尺度
d：计算方向直方图时，将特征点附近划分为d*d个区域，每个区域生成一个直方图，默认d为4
n：每个直方图中bin的个数
返回值：double类型的三维数组，即一个d*d的二维数组，数组中每个元素是一个有n个bin的直方图数组
*/
/*
Computes the 2D array of orientation histograms that form the feature
descriptor.  Based on Section 6.1 of Lowe's paper.
@param img image used in descriptor computation
@param r row coord of center of orientation histogram array
@param c column coord of center of orientation histogram array
@param ori canonical orientation of feature whose descr is being computed
@param scl scale relative to img of feature whose descr is being computed
@param d width of 2d array of orientation histograms
@param n bins per orientation histogram
@return Returns a d x d array of n-bin orientation histograms.
*/
static double*** descr_hist( IplImage* img, int r, int c, double ori,
							 double scl, int d, int n )
{
    double*** hist;//d*d*n的三维直方图数组
	double cos_t, sin_t, hist_width, exp_denom, r_rot, c_rot, grad_mag,
		grad_ori, w, rbin, cbin, obin, bins_per_rad, PI2 = 2.0 * CV_PI;
	int radius, i, j;

    //为直方图数组分配空间
    hist = calloc( d, sizeof( double** ) );//为第一维分配空间
	for( i = 0; i < d; i++ )
	{
        hist[i] = calloc( d, sizeof( double* ) );//为第二维分配空间
		for( j = 0; j < d; j++ )
            hist[i][j] = calloc( n, sizeof( double ) );//为第三维分配空间
	}

    //为了保证特征描述子具有旋转不变性，要以特征点为中心，在附近邻域内旋转θ角，即旋转为特征点的方向
	cos_t = cos( ori );
	sin_t = sin( ori );

	bins_per_rad = n / PI2;
	exp_denom = d * d * 0.5;
    //计算特征描述子过程中，特征点周围的d*d个区域中，每个区域的宽度为m*σ个像素，SIFT_DESCR_SCL_FCTR即m的默认值，σ为特征点的尺度
	hist_width = SIFT_DESCR_SCL_FCTR * scl;
    //考虑到要进行双线性插值，每个区域的宽度应为:SIFT_DESCR_SCL_FCTR * scl * ( d + 1.0 )
    //在考虑到旋转因素，每个区域的宽度应为：SIFT_DESCR_SCL_FCTR * scl * ( d + 1.0 ) * sqrt(2)
    //所以搜索的半径是：SIFT_DESCR_SCL_FCTR * scl * ( d + 1.0 ) * sqrt(2) / 2
	radius = hist_width * sqrt(2) * ( d + 1.0 ) * 0.5 + 0.5;

    //遍历每个区域的像素
	for( i = -radius; i <= radius; i++ )
		for( j = -radius; j <= radius; j++ )
		{
			/*
			Calculate sample's histogram array coords rotated relative to ori.
			Subtract 0.5 so samples that fall e.g. in the center of row 1 (i.e.
			r_rot = 1.5) have full weight placed in row 1 after interpolation.
			*/
            //坐标旋转为主方向
            //下面看不懂了
			c_rot = ( j * cos_t - i * sin_t ) / hist_width;
			r_rot = ( j * sin_t + i * cos_t ) / hist_width;
			rbin = r_rot + d / 2 - 0.5;
			cbin = c_rot + d / 2 - 0.5;

			if( rbin > -1.0  &&  rbin < d  &&  cbin > -1.0  &&  cbin < d )
				if( calc_grad_mag_ori( img, r + i, c + j, &grad_mag, &grad_ori ))
				{
					grad_ori -= ori;
					while( grad_ori < 0.0 )
						grad_ori += PI2;
					while( grad_ori >= PI2 )
						grad_ori -= PI2;

					obin = grad_ori * bins_per_rad;
					w = exp( -(c_rot * c_rot + r_rot * r_rot) / exp_denom );
					interp_hist_entry( hist, rbin, cbin, obin, grad_mag * w, d, n );
				}
		}

	return hist;
}


//好像是双线性插值，具体的看不懂
/*
Interpolates an entry into the array of orientation histograms that form the feature descriptor.
@param hist 2D array of orientation histograms
@param rbin sub-bin row coordinate of entry
@param cbin sub-bin column coordinate of entry
@param obin sub-bin orientation coordinate of entry
@param mag size of entry
@param d width of 2D array of orientation histograms
@param n number of bins per orientation histogram
*/
static void interp_hist_entry( double*** hist, double rbin, double cbin,
							   double obin, double mag, int d, int n )
{
	double d_r, d_c, d_o, v_r, v_c, v_o;
	double** row, * h;
	int r0, c0, o0, rb, cb, ob, r, c, o;

	r0 = cvFloor( rbin );
	c0 = cvFloor( cbin );
	o0 = cvFloor( obin );
	d_r = rbin - r0;
	d_c = cbin - c0;
	d_o = obin - o0;

	/*
	The entry is distributed into up to 8 bins.  Each entry into a bin
	is multiplied by a weight of 1 - d for each dimension, where d is the
	distance from the center value of the bin measured in bin units.
	*/
	for( r = 0; r <= 1; r++ )
	{
		rb = r0 + r;
		if( rb >= 0  &&  rb < d )
		{
			v_r = mag * ( ( r == 0 )? 1.0 - d_r : d_r );
			row = hist[rb];
			for( c = 0; c <= 1; c++ )
			{
				cb = c0 + c;
				if( cb >= 0  &&  cb < d )
				{
					v_c = v_r * ( ( c == 0 )? 1.0 - d_c : d_c );
					h = row[cb];
					for( o = 0; o <= 1; o++ )
					{
						ob = ( o0 + o ) % n;
						v_o = v_c * ( ( o == 0 )? 1.0 - d_o : d_o );
						h[ob] += v_o;
					}
				}
			}
		}
	}
}


/*将某特征点的方向直方图转换为特征描述子向量，对特征描述子归一化并将所有元素转化为整型，存入指定特征点中
参数：
hist：d*d*n的三维直方图数组
d：计算方向直方图时，将特征点附近划分为d*d个区域，每个区域生成一个直方图
n：每个直方图的bin个数
feat：特征点指针，将计算好的特征描述子存入其中
*/
/*
Converts the 2D array of orientation histograms into a feature's descriptor vector.
@param hist 2D array of orientation histograms
@param d width of hist
@param n bins per histogram
@param feat feature into which to store descriptor
*/
static void hist_to_descr( double*** hist, int d, int n, struct feature* feat )
{
	int int_val, i, r, c, o, k = 0;

    //遍历d*d*n的三维直方图数组，将其中的所有数据(一般是128个)都存入feat结构的descr成员中
	for( r = 0; r < d; r++ )
		for( c = 0; c < d; c++ )
			for( o = 0; o < n; o++ )
				feat->descr[k++] = hist[r][c][o];

    feat->d = k;//特征描述子的维数，一般是128
    //归一化特征点的特征描述子，即将特征描述子数组中每个元素除以特征描述子的模
	normalize_descr( feat );

    //遍历特征描述子向量，将超过阈值SIFT_DESCR_MAG_THR的元素强行赋值为SIFT_DESCR_MAG_THR
	for( i = 0; i < k; i++ )
		if( feat->descr[i] > SIFT_DESCR_MAG_THR )
			feat->descr[i] = SIFT_DESCR_MAG_THR;
    //再次归一化特征描述子向量
	normalize_descr( feat );

	/* convert floating-point descriptor to integer valued descriptor */
    //遍历特征描述子向量，每个元素乘以系数SIFT_INT_DESCR_FCTR来变为整型，并且最大值不能超过255
	for( i = 0; i < k; i++ )
	{
		int_val = SIFT_INT_DESCR_FCTR * feat->descr[i];
		feat->descr[i] = MIN( 255, int_val );
	}
}


/*归一化特征点的特征描述子，即将特征描述子数组中每个元素除以特征描述子的模
*/
/*
Normalizes a feature's descriptor vector to unitl length
@param feat feature
*/
static void normalize_descr( struct feature* feat )
{
	double cur, len_inv, len_sq = 0.0;
    int i, d = feat->d;//特征描述子的维数

    //求特征描述子的模
	for( i = 0; i < d; i++ )
	{
		cur = feat->descr[i];
		len_sq += cur*cur;
	}
	len_inv = 1.0 / sqrt( len_sq );
    //特征描述子中每个元素除以特征描述子的模，完成归一化
	for( i = 0; i < d; i++ )
		feat->descr[i] *= len_inv;
}


/*比较函数，将特征点按尺度的降序排列，用在序列排序函数CvSeqSort中
参数：
feat1：第一个特征点的指针
feat2：第二个特征点的指针
param：用户自定义参数，这里不使用
返回值：如果feat1的尺度大于feat2的尺度，返回1；否则返回-1；若相等返回0(好像反了)
*/
/*
Compares features for a decreasing-scale ordering.  Intended for use with CvSeqSort
@param feat1 first feature
@param feat2 second feature
@param param unused
@return Returns 1 if feat1's scale is greater than feat2's, -1 if vice versa,
and 0 if their scales are equal
*/
static int feature_cmp( void* feat1, void* feat2, void* param )
{
    //将输入的参数强制转换为struct feature类型的指针
	struct feature* f1 = (struct feature*) feat1;
	struct feature* f2 = (struct feature*) feat2;

    //比较两个特征点的尺度值
	if( f1->scl < f2->scl )
		return 1;
	if( f1->scl > f2->scl )
		return -1;
	return 0;
}


/*释放计算特征描述子过程中用到的方向直方图的内存空间
参数：
hist：方向直方图的指针，是一个d*d*n的三维直方图数组
d：直方图数组前两维的维数
  */
/*
De-allocates memory held by a descriptor histogram
@param hist pointer to a 2D array of orientation histograms
@param d width of hist
*/
static void release_descr_hist( double**** hist, int d )
{
	int i, j;

	for( i = 0; i < d; i++)
	{
		for( j = 0; j < d; j++ )
            free( (*hist)[i][j] );//释放第三维的内存
        free( (*hist)[i] );//释放第二维的内存
	}
    free( *hist );//释放第一维的内存
	*hist = NULL;
}

/*释放金字塔图像组的存储空间
参数:
pyr：金字塔图像组的指针
octvs：金字塔的组数
n：每一组的图像数
*/
/*
De-allocates memory held by a scale space pyramid
@param pyr scale space pyramid
@param octvs number of octaves of scale space
@param n number of images per octave
*/
static void release_pyr( IplImage**** pyr, int octvs, int n )
{
	int i, j;
	for( i = 0; i < octvs; i++ )
	{
		for( j = 0; j < n; j++ )
            cvReleaseImage( &(*pyr)[i][j] );//释放每个图像
        free( (*pyr)[i] );//释放每个组
	}
    free( *pyr );//释放金字塔
	*pyr = NULL;
}

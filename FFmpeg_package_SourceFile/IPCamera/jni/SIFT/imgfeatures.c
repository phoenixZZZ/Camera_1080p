/*
Functions and structures for dealing with image features

Copyright (C) 2006-2010  Rob Hess <hess@eecs.oregonstate.edu>

@version 1.1.2-20100521
*/

/*
  此文件中有几个函数的实现：特征点的导入导出，特征点的绘制
*/

#include "utils.h"
#include "imgfeatures.h"

#include <cxcore.h>

#include <math.h>

/************************ 未暴露接口的一些本地函数的声明 **************************/
static int import_oxfd_features( char*, struct feature** );//导入OXFD格式特征点
static int export_oxfd_features( char*, struct feature*, int );//导出OXFD格式特征点
static void draw_oxfd_features( IplImage*, struct feature*, int );//画OXFD格式特征点
static void draw_oxfd_feature( IplImage*, struct feature*, CvScalar );//画单个点

static int import_lowe_features( char*, struct feature** );//导入LOWE格式特征点
static int export_lowe_features( char*, struct feature*, int );//导出LOWE格式特征点
static void draw_lowe_features( IplImage*, struct feature*, int );//画LOWE格式特征点
static void draw_lowe_feature( IplImage*, struct feature*, CvScalar );//画单个点


/*从文件中读入图像特征
文件中的特征点格式必须是FEATURE_OXFD或FEATURE_LOWE格式
参数：
filename：文件名
type：特征点类型
feat：用来存储特征点的feature数组的指针
返回值：导入的特征点个数
*/
/*
Reads image features from file.  The file should be formatted as from
the code provided by the Visual Geometry Group at Oxford:
@param filename location of a file containing image features
@param type determines how features are input.  If \a type is FEATURE_OXFD,
	the input file is treated as if it is from the code provided by the VGG
	at Oxford:http://www.robots.ox.ac.uk:5000/~vgg/research/affine/index.html
	If \a type is FEATURE_LOWE, the input file is treated as if it is from
	David Lowe's SIFT code:http://www.cs.ubc.ca/~lowe/keypoints
@param feat pointer to an array in which to store features
@return Returns the number of features imported from filename or -1 on error
*/
int import_features( char* filename, int type, struct feature** feat )
{
	int n;

    //根据特征点类型，调用不同的函数完成导入功能
	switch( type )
	{
	case FEATURE_OXFD:
        n = import_oxfd_features( filename, feat );//调用函数，导入OXFD格式特征点
		break;
	case FEATURE_LOWE:
        n = import_lowe_features( filename, feat );//调用函数，导入LOWE格式特征点
		break;
    default: //特征点格式无法识别
        fprintf( stderr, "Warning: import_features(): unrecognized feature" \
				"type, %s, line %d\n", __FILE__, __LINE__ );
		return -1;
	}

    //导入失败
    if( n == -1 )
		fprintf( stderr, "Warning: unable to import features from %s,"	\
			" %s, line %d\n", filename, __FILE__, __LINE__ );
	return n;
}


/*导出feature数组到文件
参数：
filename：文件名
feat：特征数组
n：特征点个数
返回值：0：成功；1：失败
*/
/*
Exports a feature set to a file formatted depending on the type of
features, as specified in the feature struct's type field.
@param filename name of file to which to export features
@param feat feature array
@param n number of features
@return Returns 0 on success or 1 on error
*/
int export_features( char* filename, struct feature* feat, int n )
{
	int r, type;

    //参数合法性检查
	if( n <= 0  ||  ! feat )
	{
		fprintf( stderr, "Warning: no features to export, %s line %d\n",
				__FILE__, __LINE__ );
		return 1;
	}
    type = feat[0].type;//特征点的类型、

    //根据特征点类型，调用不同的函数完成导出功能
	switch( type )
	{
	case FEATURE_OXFD:
        r = export_oxfd_features( filename, feat, n );//调用函数，导出OXFD格式特征点
		break;
	case FEATURE_LOWE:
        r = export_lowe_features( filename, feat, n );//调用函数，导出LOWE格式特征点
		break;
	default:
		fprintf( stderr, "Warning: export_features(): unrecognized feature" \
				"type, %s, line %d\n", __FILE__, __LINE__ );
		return -1;
	}

    if( r ) //导出函数返回值非0，表示导出失败
		fprintf( stderr, "Warning: unable to export features to %s,"	\
				" %s, line %d\n", filename, __FILE__, __LINE__ );
	return r;
}


/*在图片上画出特征点
参数：
img：图像
feat：特征点数组
n：特征点个数
*/
/*
Draws a set of features on an image
@param img image on which to draw features
@param feat array of features
@param n number of features
*/
void draw_features( IplImage* img, struct feature* feat, int n )
{
	int type;

    //参数合法性检查
	if( n <= 0  ||  ! feat )
	{
		fprintf( stderr, "Warning: no features to draw, %s line %d\n",
				__FILE__, __LINE__ );
		return;
	}
    type = feat[0].type;//特征点的类型

    //根据特征点类型，调用不同的函数完成绘图功能
	switch( type )
	{
	case FEATURE_OXFD:
        draw_oxfd_features( img, feat, n );//调用函数，在图像上画OXFD格式特征点
		break;
	case FEATURE_LOWE:
        draw_lowe_features( img, feat, n );//调用函数，在图像上画LOWE格式特征点
		break;
	default:
		fprintf( stderr, "Warning: draw_features(): unrecognized feature" \
			" type, %s, line %d\n", __FILE__, __LINE__ );
		break;
	}
}


/*计算两个特征描述子间的欧氏距离的平方
参数：
f1:第一个特征点
f2:第二个特征点
返回值：欧氏距离的平方
*/
/*
Calculates the squared Euclidian distance between two feature descriptors.
@param f1 first feature
@param f2 second feature
@return Returns the squared Euclidian distance between the descriptors off1 and f2.
*/
double descr_dist_sq( struct feature* f1, struct feature* f2 )
{
	double diff, dsq = 0;
	double* descr1, * descr2;
	int i, d;

    d = f1->d;//f1的特征描述子的长度
    if( f2->d != d )//若f1和f2的特征描述子长度不同，返回
		return DBL_MAX;
    descr1 = f1->descr;//f1的特征描述子，一个double数组
    descr2 = f2->descr;//f2的特征描述子，一个double数组

    //计算欧氏距离的平方，即对应元素的差的平方和
	for( i = 0; i < d; i++ )
	{
		diff = descr1[i] - descr2[i];
		dsq += diff*diff;
	}
	return dsq;
}


/***************************** 一些未暴露接口的内部函数 *******************************/
/***************************** Local Functions *******************************/


/*从文件中读入OXFD格式的图像特征
参数：
filename：文件名
features：用来存储特征点的feature数组的指针
返回值：导入的特征点个数
*/
/*
Reads image features from file.  The file should be formatted as from
the code provided by the Visual Geometry Group at Oxford:
http://www.robots.ox.ac.uk:5000/~vgg/research/affine/index.html
@param filename location of a file containing image features
@param features pointer to an array in which to store features
@return Returns the number of features imported from filename or -1 on error
*/
static int import_oxfd_features( char* filename, struct feature** features )
{
    struct feature* f;//第一个特征点的指针
	int i, j, n, d;
	double x, y, a, b, c, dv;
    FILE* file;//文件指针

    if( ! features )
		fatal_error( "NULL pointer error, %s, line %d",  __FILE__, __LINE__ );

    //打开文件
	if( ! ( file = fopen( filename, "r" ) ) )
	{
		fprintf( stderr, "Warning: error opening %s, %s, line %d\n",
				filename, __FILE__, __LINE__ );
		return -1;
	}

    //读入特征描述子维数和特征点个数
	/* read dimension and number of features */
	if( fscanf( file, " %d %d ", &d, &n ) != 2 )
	{
		fprintf( stderr, "Warning: file read error, %s, line %d\n",
				__FILE__, __LINE__ );
		return -1;
	}
    //特征描述子维数大于定义的最大维数，出错
    if( d > FEATURE_MAX_D )
	{
		fprintf( stderr, "Warning: descriptor too long, %s, line %d\n",
				__FILE__, __LINE__ );
		return -1;
	}

    //分配内存，n个feature结构大小，返回首地址给f
	f = calloc( n, sizeof(struct feature) );

    //遍历文件中的n个特征点
	for( i = 0; i < n; i++ )
	{
        //读入仿射区域参数
		/* read affine region parameters */
		if( fscanf( file, " %lf %lf %lf %lf %lf ", &x, &y, &a, &b, &c ) != 5 )
		{
			fprintf( stderr, "Warning: error reading feature #%d, %s, line %d\n",
					i+1, __FILE__, __LINE__ );
            free( f );//发生错误后释放内存
			return -1;
		}
        //给第i个特征点赋值
        f[i].img_pt.x = f[i].x = x;//特征点的x坐标
        f[i].img_pt.y = f[i].y = y;//特征点的y坐标
		f[i].a = a;
		f[i].b = b;
		f[i].c = c;
        f[i].d = d;
        f[i].type = FEATURE_OXFD;//特征点类型

        //读入特征描述子
		/* read descriptor */
		for( j = 0; j < d; j++ )
		{
			if( ! fscanf( file, " %lf ", &dv ) )
			{
				fprintf( stderr, "Warning: error reading feature descriptor" \
						" #%d, %s, line %d\n", i+1, __FILE__, __LINE__ );
                free( f );//发生错误后释放内存
				return -1;
			}
            f[i].descr[j] = dv;//赋给第i个特征点的第j个特征描述符
		}

        //其他一些没什么用的参数
        f[i].scl = f[i].ori = 0;//OXFD特征点无此参数
		f[i].category = 0;
		f[i].fwd_match = f[i].bck_match = f[i].mdl_match = NULL;
		f[i].mdl_pt.x = f[i].mdl_pt.y = -1;
		f[i].feature_data = NULL;
	}

    //关闭文件
	if( fclose(file) )
	{
		fprintf( stderr, "Warning: file close error, %s, line %d\n",
				__FILE__, __LINE__ );
        free( f );//发生错误后释放内存
		return -1;
	}

    *features = f;//将第一个特征点的指针赋给*feature
    return n;//返回读入的特征点个数
}


/*导出OXFD格式的特征点集到文件
参数：
filename：文件名
feat：特征数组
n：特征点个数
返回值：0：成功；1：失败
*/
/*
Exports a feature set to a file formatted as one from the code provided
by the Visual Geometry Group at Oxford:
http://www.robots.ox.ac.uk:5000/~vgg/research/affine/index.html
@param filename name of file to which to export features
@param feat feature array
@param n number of features
@return Returns 0 on success or 1 on error
*/
static int export_oxfd_features( char* filename, struct feature* feat, int n )
{
	FILE* file;
	int i, j, d;

	if( n <= 0 )
	{
		fprintf( stderr, "Warning: feature count %d, %s, line %s\n",
				n, __FILE__, __LINE__ );
		return 1;
	}
    //打开文件
	if( ! ( file = fopen( filename, "w" ) ) )
	{
		fprintf( stderr, "Warning: error opening %s, %s, line %d\n",
				filename, __FILE__, __LINE__ );
		return 1;
	}

    d = feat[0].d;//特征描述子的维数
    fprintf( file, "%d\n%d\n", d, n );//首先写入特征描述子的维数和特征点个数

    //依次写入每个特征点的信息
	for( i = 0; i < n; i++ )
	{
        //写入仿射区域参数
		fprintf( file, "%f %f %f %f %f", feat[i].x, feat[i].y, feat[i].a,
				feat[i].b, feat[i].c );
        //写入d个特征描述子的元素
		for( j = 0; j < d; j++ )
			fprintf( file, " %f", feat[i].descr[j] );
        fprintf( file, "\n" );//换行
	}

    //关闭文件
	if( fclose(file) )
	{
		fprintf( stderr, "Warning: file close error, %s, line %d\n",
				__FILE__, __LINE__ );
		return 1;
	}

	return 0;
}


/*在图像上画出OXFD类型的特征点
参数：
img：图像指针
feat：特征数组
n：特征个数
*/
/*
Draws Oxford-type affine features
@param img image on which to draw features
@param feat array of Oxford-type features
@param n number of features
*/
static void draw_oxfd_features( IplImage* img, struct feature* feat, int n )
{
    //CvScalar color = CV_RGB( 255, 255, 255 );//颜色
	CvScalar color = cvScalar( 255, 255, 255, 0);//颜色
	int i;

	if( img-> nChannels > 1 )
		color = FEATURE_OXFD_COLOR;

    //调用函数，依次画出每个特征点
	for( i = 0; i < n; i++ )
		draw_oxfd_feature( img, feat + i, color );
}


/*在图像上画单个OXFD特征点
参数：
img：图像指针
feat：要画的特征点
color：颜色
*/
/*
Draws a single Oxford-type feature
@param img image on which to draw
@param feat feature to be drawn
@param color color in which to draw
*/
static void draw_oxfd_feature( IplImage* img, struct feature* feat, CvScalar color )
{
	double m[4] = { feat->a, feat->b, feat->b, feat->c };
    double v[4] = { 0 };//特征向量的数据
    double e[2] = { 0 };//特征值的数据
	CvMat M, V, E;
	double alpha, l1, l2;

    //计算椭圆的轴线和方向
	/* compute axes and orientation of ellipse surrounding affine region */
    cvInitMatHeader( &M, 2, 2, CV_64FC1, m, CV_AUTOSTEP );//矩阵
    cvInitMatHeader( &V, 2, 2, CV_64FC1, v, CV_AUTOSTEP );//2个2*1的特征向量组成的矩阵
    cvInitMatHeader( &E, 2, 1, CV_64FC1, e, CV_AUTOSTEP );//特征值
    cvEigenVV( &M, &V, &E, DBL_EPSILON, 0, 0 );//计算特征值和特征向量
	l1 = 1 / sqrt( e[1] );
	l2 = 1 / sqrt( e[0] );
	alpha = -atan2( v[1], v[0] );
	alpha *= 180 / CV_PI;

    //画椭圆和十字星
	cvEllipse( img, cvPoint( feat->x, feat->y ), cvSize( l2, l1 ), alpha,
				0, 360, cvScalar(0,0,0,0), 3, 8, 0 );
	cvEllipse( img, cvPoint( feat->x, feat->y ), cvSize( l2, l1 ), alpha,
				0, 360, color, 1, 8, 0 );
	cvLine( img, cvPoint( feat->x+2, feat->y ), cvPoint( feat->x-2, feat->y ),
			color, 1, 8, 0 );
	cvLine( img, cvPoint( feat->x, feat->y+2 ), cvPoint( feat->x, feat->y-2 ),
			color, 1, 8, 0 );
}


/*从文件中读入LOWE特征点
参数：
filename:文件名
features：存放特征点的特征数组的指针
返回值：读入的特征点个数
*/
/*
Reads image features from file.  The file should be formatted as from
the code provided by David Lowe:http://www.cs.ubc.ca/~lowe/keypoints/
@param filename location of a file containing image features
@param features pointer to an array in which to store features
@return Returns the number of features imported from filename or -1 on error
*/
static int import_lowe_features( char* filename, struct feature** features )
{
    struct feature* f;//第一个特征点的指针
	int i, j, n, d;
	double x, y, s, o, dv;
	FILE* file;

	if( ! features )
		fatal_error( "NULL pointer error, %s, line %d",  __FILE__, __LINE__ );

    //打开文件
	if( ! ( file = fopen( filename, "r" ) ) )
	{
		fprintf( stderr, "Warning: error opening %s, %s, line %d\n",
			filename, __FILE__, __LINE__ );
		return -1;
	}

    //首先读入特征点个数和特征描述子维数
	/* read number of features and dimension */
	if( fscanf( file, " %d %d ", &n, &d ) != 2 )
	{
		fprintf( stderr, "Warning: file read error, %s, line %d\n",
				__FILE__, __LINE__ );
		return -1;
	}

    //特征描述子维数大于定义的最大维数，出错
	if( d > FEATURE_MAX_D )
	{
		fprintf( stderr, "Warning: descriptor too long, %s, line %d\n",
				__FILE__, __LINE__ );
		return -1;
	}

    //分配内存，n个feature结构大小，返回首地址给f
	f = calloc( n, sizeof(struct feature) );

    //依次读入n个特征点
	for( i = 0; i < n; i++ )
	{
        //读入特征点的坐标(注意x,y顺序)，尺度和方向
		/* read affine region parameters */
		if( fscanf( file, " %lf %lf %lf %lf ", &y, &x, &s, &o ) != 4 )
		{
			fprintf( stderr, "Warning: error reading feature #%d, %s, line %d\n",
					i+1, __FILE__, __LINE__ );
            free( f );//出错后释放内存
			return -1;
		}
        //给第i个特征点赋值
        f[i].img_pt.x = f[i].x = x;//特征点的x坐标
        f[i].img_pt.y = f[i].y = y;//特征点的y坐标
        f[i].scl = s;//特征点的大小，即其主方向的梯度的模值
        f[i].ori = o;//特征点的方向，即其主方向
        f[i].d = d;//特征描述子的维数
        f[i].type = FEATURE_LOWE;//类型

        //读入特征描述子
		/* read descriptor */
		for( j = 0; j < d; j++ )
		{
			if( ! fscanf( file, " %lf ", &dv ) )
			{
				fprintf( stderr, "Warning: error reading feature descriptor" \
						" #%d, %s, line %d\n", i+1, __FILE__, __LINE__ );
                free( f );//出错后释放内存
				return -1;
			}
			f[i].descr[j] = dv;
		}

        //其他一些没什么用的参数
		f[i].a = f[i].b = f[i].c = 0;
		f[i].category = 0;
		f[i].fwd_match = f[i].bck_match = f[i].mdl_match = NULL;
		f[i].mdl_pt.x = f[i].mdl_pt.y = -1;
	}

    //关闭文件
	if( fclose(file) )
	{
		fprintf( stderr, "Warning: file close error, %s, line %d\n",
				__FILE__, __LINE__ );
        free( f );//出错后释放内存
		return -1;
	}

    *features = f;//首地址赋给*features
    return n;//返回读入的特征点个数
}


/*导出LOWE格式特征点集合到文件
参数：
filename：文件名
feat：特征点数组
n：特征点个数
返回值：0：成功；1：失败
*/
/*
Exports a feature set to a file formatted as one from the code provided
by David Lowe:http://www.cs.ubc.ca/~lowe/keypoints/

@param filename name of file to which to export features
@param feat feature array
@param n number of features

@return Returns 0 on success or 1 on error
*/
static int export_lowe_features( char* filename, struct feature* feat, int n )
{
	FILE* file;
	int i, j, d;

	if( n <= 0 )
	{
		fprintf( stderr, "Warning: feature count %d, %s, line %s\n",
				n, __FILE__, __LINE__ );
		return 1;
	}

    //打开文件
	if( ! ( file = fopen( filename, "w" ) ) )
	{
		fprintf( stderr, "Warning: error opening %s, %s, line %d\n",
				filename, __FILE__, __LINE__ );
		return 1;
	}

    d = feat[0].d;//特征描述子维数
    fprintf( file, "%d %d\n", n, d );//首先写入特征点个数和特征描述子维数

    //依次写入每个特征点的信息
	for( i = 0; i < n; i++ )
	{
        //写入特征点坐标(注意x,y顺序),尺度，方向
		fprintf( file, "%f %f %f %f", feat[i].y, feat[i].x,
				feat[i].scl, feat[i].ori );
        //写入特征描述子
		for( j = 0; j < d; j++ )
		{
            //每行20个元素
			/* write 20 descriptor values per line */
			if( j % 20 == 0 )
				fprintf( file, "\n" );
			fprintf( file, " %d", (int)(feat[i].descr[j]) );
		}
		fprintf( file, "\n" );
	}

    //关闭文件
	if( fclose(file) )
	{
		fprintf( stderr, "Warning: file close error, %s, line %d\n",
				__FILE__, __LINE__ );
		return 1;
	}

	return 0;
}


/*在图像上画LOWE特征点
参数：
img：图像指针
feat：特征点数组
n：特征点个数
*/
/*
Draws Lowe-type features
@param img image on which to draw features
@param feat array of Oxford-type features
@param n number of features
*/
static void draw_lowe_features( IplImage* img, struct feature* feat, int n )
{
    CvScalar color = cvScalar( 255, 255, 255, 0);//颜色
	int i;

	if( img-> nChannels > 1 )
		color = FEATURE_LOWE_COLOR;

    //调用函数，依次画n个特征点
	for( i = 0; i < n; i++ )
		draw_lowe_feature( img, feat + i, color );
}


/*画单个LOWE特征点
参数：
img：图像指针
feat：要画的特征点
color：颜色
*/
/*
Draws a single Lowe-type feature
@param img image on which to draw
@param feat feature to be drawn
@param color color in which to draw
*/
static void draw_lowe_feature( IplImage* img, struct feature* feat, CvScalar color )
{
	int len, hlen, blen, start_x, start_y, end_x, end_y, h1_x, h1_y, h2_x, h2_y;
	double scl, ori;
	double scale = 5.0;
	double hscale = 0.75;
	CvPoint start, end, h1, h2;

	/* compute points for an arrow scaled and rotated by feat's scl and ori */
    //箭头杆的起点的坐标
	start_x = cvRound( feat->x );
	start_y = cvRound( feat->y );
    scl = feat->scl;//特征点的大小
    ori = feat->ori;//特征点的方向，弧度
    len = cvRound( scl * scale );//箭头杆的长度
    hlen = cvRound( scl * hscale );//箭头分叉的长度
	blen = len - hlen;
    //箭头杆的终点的坐标
	end_x = cvRound( len *  cos( ori ) ) + start_x;
    end_y = cvRound( len * -sin( ori ) ) + start_y;
    //箭头的右分叉的起点的坐标
	h1_x = cvRound( blen *  cos( ori + CV_PI / 18.0 ) ) + start_x;
	h1_y = cvRound( blen * -sin( ori + CV_PI / 18.0 ) ) + start_y;
    //箭头的左分叉的起点的坐标
	h2_x = cvRound( blen *  cos( ori - CV_PI / 18.0 ) ) + start_x;
	h2_y = cvRound( blen * -sin( ori - CV_PI / 18.0 ) ) + start_y;
    start = cvPoint( start_x, start_y );//箭头杆的起点
    end = cvPoint( end_x, end_y );//箭头杆的终点
    h1 = cvPoint( h1_x, h1_y );//箭头的右分叉的起点
    h2 = cvPoint( h2_x, h2_y );//箭头的左分叉的起点

    cvLine( img, start, end, color, 1, 8, 0 );//画箭头杆
    cvLine( img, end, h1, color, 1, 8, 0 );//画右分叉
    cvLine( img, end, h2, color, 1, 8, 0 );//画左分叉
}



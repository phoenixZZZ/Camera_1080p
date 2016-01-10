/*
 * com_example_myrtspplayer_OpenCV_Test.c
 *
 *  Created on: 2015-9-30
 *      Author: Administrator
 */

#include <jni.h>
#include <string.h>
#include <stdio.h>
#include "com_jiuan_it_ipc_utils_OpenCV.h"

#include <android/log.h>

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/features2d/features2d.hpp"

extern "C"{
#include "SIFT/imgfeatures.h"
#include "SIFT/kdtree.h"
#include "SIFT/minpq.h"
#include "SIFT/sift.h"
#include "SIFT/utils.h"
#include "SIFT/xform.h"
};


#define  LOG_TAG    "SIFT"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

//VIDEO宏定义
#define    NOFRAME         0x00000C01
#define    VIDEOINVALID    0x00000C02

//在k-d树上进行BBF搜索的最大次数
/* the maximum number of keypoint NN candidates to check during BBF search */
#define KDTREE_BBF_MAX_NN_CHKS 200

//目标点与最近邻和次近邻的距离的比值的阈值，若大于此阈值，则剔除此匹配点对
//通常此值取0.6，值越小找到的匹配点对越精确，但匹配数目越少
/* threshold on squared ratio of distances between NN and 2nd NN */
#define NN_SQ_DIST_RATIO_THR 0.49

//全局变量用于基于sift的图像拼接技术
struct feature *feat1;
struct feature *feat2;//feat1：图1的特征点数组，feat2：图2的特征点数组
struct feature *feat;//每个特征点
struct kd_node *kd_root;//k-d树的树根
struct feature **nbrs;//当前特征点的最近邻点数组

using namespace cv;
using namespace std;

#define IMG1 "图1"
#define IMG2 "图2"
#define IMG1_FEAT "图1特征点"
#define IMG2_FEAT "图2特征点"
#define IMG_MATCH1 "距离比值筛选后的匹配结果"
#define IMG_MATCH2 "RANSAC筛选后的匹配结果"

#define IMG_MOSAIC_TEMP "临时拼接图像"
#define IMG_MOSAIC_SIMPLE "简易拼接图"
#define IMG_MOSAIC_BEFORE_FUSION "重叠区域融合前"
#define IMG_MOSAIC_PROC "处理后的拼接图"

IplImage* img1;      //IplImage格式的原图
IplImage* img2;

IplImage* img1_Feat; //画上特征点之后的图
IplImage* img2_Feat;

IplImage *stacked;		 //显示匹配结果的合成图像，显示经距离比值法筛选后的匹配结果
IplImage *stacked_ransac;//显示匹配结果的合成图像，显示经RANSAC算法筛选后的匹配结果

CvMat * H;				 //RANSAC算法求出的变换矩阵
struct feature **inliers;//精RANSAC筛选后的内点数组
int n_inliers;			 //经RANSAC算法筛选后的内点个数,即feat2中具有符合要求的特征点的个数

IplImage *xformed;		 //临时拼接图，即只将图2变换后的图
IplImage *xformed_simple;//简易拼接图
IplImage *xformed_proc;  //处理后的拼接图

//图2的四个角经矩阵H变换后的坐标
CvPoint leftTop,leftBottom,rightTop,rightBottom;

void CalcFourCorner();

void CalcFourCorner()
{
	//计算图2的四个角经矩阵H变换后的坐标
	double v2[]={0,0,1};//左上角
	double v1[3];//变换后的坐标值
	CvMat V2 = cvMat(3,1,CV_64FC1,v2);
	CvMat V1 = cvMat(3,1,CV_64FC1,v1);
	cvGEMM(H,&V2,1,0,1,&V1);//矩阵乘法
	leftTop.x = cvRound(v1[0]/v1[2]);
	leftTop.y = cvRound(v1[1]/v1[2]);
	//cvCircle(xformed,leftTop,7,CV_RGB(255,0,0),2);

	//将v2中数据设为左下角坐标
	v2[0] = 0;
	v2[1] = img2->height;
	V2 = cvMat(3,1,CV_64FC1,v2);
	V1 = cvMat(3,1,CV_64FC1,v1);
	cvGEMM(H,&V2,1,0,1,&V1);
	leftBottom.x = cvRound(v1[0]/v1[2]);
	leftBottom.y = cvRound(v1[1]/v1[2]);
	//cvCircle(xformed,leftBottom,7,CV_RGB(255,0,0),2);

	//将v2中数据设为右上角坐标
	v2[0] = img2->width;
	v2[1] = 0;
	V2 = cvMat(3,1,CV_64FC1,v2);
	V1 = cvMat(3,1,CV_64FC1,v1);
	cvGEMM(H,&V2,1,0,1,&V1);
	rightTop.x = cvRound(v1[0]/v1[2]);
	rightTop.y = cvRound(v1[1]/v1[2]);
	//cvCircle(xformed,rightTop,7,CV_RGB(255,0,0),2);

	//将v2中数据设为右下角坐标
	v2[0] = img2->width;
	v2[1] = img2->height;
	V2 = cvMat(3,1,CV_64FC1,v2);
	V1 = cvMat(3,1,CV_64FC1,v1);
	cvGEMM(H,&V2,1,0,1,&V1);
	rightBottom.x = cvRound(v1[0]/v1[2]);
	rightBottom.y = cvRound(v1[1]/v1[2]);
	//cvCircle(xformed,rightBottom,7,CV_RGB(255,0,0),2);

}

/*
函数名称： Andon_MosaicIMG
函数功能： 两张图像进行拼接，去除重叠部分，引用sift开源算法
时    间： 2015.08.31
作    者： 王鹏飞
*/
JNIEXPORT jint JNICALL Java_com_jiuan_it_ipc_utils_OpenCV_Andon_1MosaicIMG
  (JNIEnv *env, jobject obj, jstring jstrInputFileName1, jstring jstrInputFileName2, jstring jstrOutputFileName)
{

	const char *inputFile1 = env->GetStringUTFChars(jstrInputFileName1, 0);
	const char *inputFile2 = env->GetStringUTFChars(jstrInputFileName2, 0);
	const char *outputFile = env->GetStringUTFChars(jstrOutputFileName, 0);
//	const char *inputFile1 = (*env)->GetStringUTFChars(env, jstrInputFileName1, 0);
//	const char *inputFile2 = (*env)->GetStringUTFChars(env, jstrInputFileName2, 0);
//	const char *outputFile = (*env)->GetStringUTFChars(env, jstrOutputFileName, 0);


	if ((inputFile1 == NULL) | inputFile2 == NULL)
	{
		return -1;
	}
	img1 = NULL;
	img2 = NULL;
	img1_Feat = NULL;
	img2_Feat = NULL;

	stacked = NULL;
	H = NULL;
	xformed = NULL;
	img1 = cvLoadImage(inputFile1);
	img2 = cvLoadImage(inputFile2);


	//cvShowImage("图1", img1);
	//cvShowImage("图2", img2);
	img1_Feat = cvCloneImage(img1);//复制图1,深拷贝,用来画特征点
	img2_Feat = cvCloneImage(img2);
	//提取图像的特征点, 默认提取的是LOWE格式的SIFT特征点
	int n1 = sift_features(img1, &feat1);
	draw_features(img1_Feat, feat1, n1);
	int n2 = sift_features(img2, &feat2);
	draw_features(img2_Feat, feat2, n2);
	//两幅图合成一幅图
	stacked = stack_imgs_horizontal(img1, img2);
	kd_root = kdtree_build(feat1, n1); //根据图1特征点集feat1建立k-d树,返回k-d树根给kd_root
	//cvShowImage("直接拼接成一张图1", stacked);

	Point pt1, pt2;   //连线的两个端点
	double d0, d1;    //feat2中每个特征点到最紧邻和次紧邻的距离
	int matchNum = 0; //经距离比值法筛选后的匹配点对的个数
	LOGD("%s-------%d进入111函数111111", __FUNCTION__, __LINE__);
	//遍历特征点集feat2，针对feat2中每个特征点feat，选取符合距离比值条件的匹配点，放到feat的fwd_match域中
	for(int i = 0; i < n2; i++ )
	{
		feat = feat2+i;//第i个特征点的指针
		//在kd_root中搜索目标点feat的2个最近邻点，存放在nbrs中，返回实际找到的近邻点个数
		int k = kdtree_bbf_knn( kd_root, feat, 2, &nbrs, KDTREE_BBF_MAX_NN_CHKS );
		if( k == 2 )
		{
			d0 = descr_dist_sq( feat, nbrs[0] );//feat与最近邻点的距离的平方
			d1 = descr_dist_sq( feat, nbrs[1] );//feat与次近邻点的距离的平方
			//若d0和d1的比值小于阈值NN_SQ_DIST_RATIO_THR，则接受此匹配，否则剔除
			if( d0 < d1 * NN_SQ_DIST_RATIO_THR )
			{   //将目标点feat和最近邻点作为匹配点对
				pt2 = Point( cvRound( feat->x ), cvRound( feat->y ) );//图2中点的坐标
				pt1 = Point( cvRound( nbrs[0]->x ), cvRound( nbrs[0]->y ) );//图1中点的坐标(feat的最近邻点)
				pt2.x += img1->width;//由于两幅图是左右排列的，pt2的横坐标加上图1的宽度，作为连线的终点
				cvLine( stacked, pt1, pt2, CV_RGB(255,0,255), 1, 8, 0 );//画出连线
				matchNum++;//统计匹配点对的个数
				feat2[i].fwd_match = nbrs[0];//使点feat的fwd_match域指向其对应的匹配点
			}
		}
		free( nbrs );//释放近邻数组
	}
	LOGD("%s-------%d进入111函数222222", __FUNCTION__, __LINE__);
	//显示并保存经距离比值法筛选后的匹配图
// 	cvNamedWindow(IMG_MATCH1);//创建窗口
// 	cvShowImage(IMG_MATCH1,stacked);//显示

	//利用RANSAC算法筛选匹配点,计算变换矩阵H,无论img1和img2的左右顺序,H永远是将feat2中的特征点变换为其匹配点,即将img2中的点变换为img1中的对应点
	//H = ransac_xform(feat2, n2, FEATURE_FWD_MATCH, lsq_homog, 4, 0.01, homog_xfer_err, 3.0, &inliers, &n_inliers);
	H = ransac_xform(feat2, n2, FEATURE_FWD_MATCH, lsq_homog, 10, 0.01, homog_xfer_err, 3.0, &inliers, &n_inliers);
	if (H) //如能计算出变换矩阵,即两幅图中有共同区域
	{
		//拼接图像,img1是左图, img2是右图
		CalcFourCorner();
		xformed = cvCreateImage(cvSize(MIN(rightTop.x,rightBottom.x),MIN(img1->height, img2->height)), IPL_DEPTH_8U, 3);//为拼接结果图xformed分配空间,高度为图1图2高度的较小者，根据图2右上角和右下角变换后的点的位置决定拼接图的宽度
		cvWarpPerspective(img2,xformed,H,CV_INTER_LINEAR + CV_WARP_FILL_OUTLIERS,cvScalarAll(0));//用变换矩阵H对右图img2做投影变换(变换后会有坐标右移)，结果放到xformed中
		//cvNamedWindow("拼接临时图");
		//cvShowImage("拼接临时图",xformed);


// 		//简易拼接法：直接将将左图img1叠加到xformed的左边
// 		xformed_simple = cvCloneImage(xformed);//简易拼接图，可笼子xformed
// 		cvSetImageROI(xformed_simple,cvRect(0,0,img1->width,img1->height));
// 		cvAddWeighted(img1,1,xformed_simple,0,0,xformed_simple);
// 		cvResetImageROI(xformed_simple);
// 		//cvNamedWindow(IMG_MOSAIC_SIMPLE);//创建窗口
// 		cvShowImage(IMG_MOSAIC_SIMPLE,xformed_simple);//显示简易拼接图

		//图像保存至本地
//		cvSaveImage(outputFile, xformed_simple);

		//处理后的拼接图，克隆自xformed
		xformed_proc = cvCloneImage(xformed);

		//重叠区域左边的部分完全取自图1
		cvSetImageROI(img1,cvRect(0,0,MIN(leftTop.x,leftBottom.x),xformed_proc->height));
		cvSetImageROI(xformed,cvRect(0,0,MIN(leftTop.x,leftBottom.x),xformed_proc->height));
		cvSetImageROI(xformed_proc,cvRect(0,0,MIN(leftTop.x,leftBottom.x),xformed_proc->height));
		cvAddWeighted(img1,1,xformed,0,0,xformed_proc);
		cvResetImageROI(img1);
		cvResetImageROI(xformed);
		cvResetImageROI(xformed_proc);

		//采用加权平均的方法融合重叠区域
		int start = MIN(leftTop.x,leftBottom.x) ;//开始位置，即重叠区域的左边界
		double processWidth = img1->width - start;//重叠区域的宽度
		double alpha = 1;//img1中像素的权重
		for(int i=0; i<xformed_proc->height; i++)//遍历行
		{
			const uchar * pixel_img1 = ((uchar *)(img1->imageData + img1->widthStep * i));//img1中第i行数据的指针
			const uchar * pixel_xformed = ((uchar *)(xformed->imageData + xformed->widthStep * i));//xformed中第i行数据的指针
			uchar * pixel_xformed_proc = ((uchar *)(xformed_proc->imageData + xformed_proc->widthStep * i));//xformed_proc中第i行数据的指针
			for(int j=start; j<img1->width; j++)//遍历重叠区域的列
			{
				//如果遇到图像xformed中无像素的黑点，则完全拷贝图1中的数据
				if(pixel_xformed[j*3] < 50 && pixel_xformed[j*3+1] < 50 && pixel_xformed[j*3+2] < 50 )
				{
					alpha = 1;
				}
				else
				{   //img1中像素的权重，与当前处理点距重叠区域左边界的距离成正比
					alpha = (processWidth-(j-start)) / processWidth ;
				}
				pixel_xformed_proc[j*3] = pixel_img1[j*3] * alpha + pixel_xformed[j*3] * (1-alpha);//B通道
				pixel_xformed_proc[j*3+1] = pixel_img1[j*3+1] * alpha + pixel_xformed[j*3+1] * (1-alpha);//G通道
				pixel_xformed_proc[j*3+2] = pixel_img1[j*3+2] * alpha + pixel_xformed[j*3+2] * (1-alpha);//R通道
			}
		}
//		cvNamedWindow(IMG_MOSAIC_PROC);//创建窗口
 		//cvShowImage(IMG_MOSAIC_PROC,xformed_proc);//显示处理后的拼接图
		//cvShowImage("合成后的图",xformed_proc);//显示处理后的拼接图
		cvSaveImage(outputFile, xformed_proc);
	}
	else
	{
		cvSaveImage(outputFile, stacked);
		//cvShowImage("直接拼接成一张图2", stacked);
		//cvShowImage("合成后的图", stacked);
	}
	LOGD("%s-------%d进入111函数333333", __FUNCTION__, __LINE__);
	//释放内存空间
	cvReleaseImage(&img1);
	cvReleaseImage(&img2);
	cvReleaseImage(&img1_Feat);
	free(feat1);//释放特征点数组
	cvReleaseImage(&img2_Feat);
	free(feat2);//释放特征点数组
	cvReleaseImage(&stacked);
	kdtree_release(kd_root);//释放kd树
	cvReleaseMat(&H);//释放变换矩阵H
	free(inliers);//释放内点数组
	cvReleaseImage(&xformed);
	cvReleaseImage(&xformed_simple);
	return 0;
}

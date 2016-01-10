/*
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
  此文件中有k-d树的建立和BBF查找函数的实现
*/

#include "kdtree.h"
#include "minpq.h"
#include "imgfeatures.h"
#include "utils.h"

#include <cxcore.h>

#include <stdio.h>

//BBF中用到的结构，可存储当前点到目标点的距离
//在kd树搜索过程中，此类型数据会被赋值给feature结构的feature_data成员
struct bbf_data
{
    double d;  //此特征点到目标点的欧式距离值
    void* old_data; //保存此特征点的feature_data域的以前的值
};

/************************ 未暴露接口的一些本地函数的声明 **************************/
/************************* Local Function Prototypes *************************/

//用给定的特征点集初始化k-d树节点
static struct kd_node* kd_node_init( struct feature*, int );
//扩展指定的k-d树节点及其左右孩子
static void expand_kd_node_subtree( struct kd_node* );
//确定输入节点的枢轴索引和枢轴值
static void assign_part_key( struct kd_node* );
//找到输入数组的中值
static double median_select( double*, int );
//找到输入数组中第r小的数
static double rank_select( double*, int, int );
//用插入法对输入数组进行升序排序
static void insertion_sort( double*, int );
//根据给定的枢轴值分割数组，使数组前部分小于pivot，后部分大于pivot
static int partition_array( double*, int, double );
//在指定的k-d树节点上划分特征点集
static void partition_features( struct kd_node* );
//从给定结点搜索k-d树直到叶节点，搜索过程中将未搜索的节点根据优先级放入队列
static struct kd_node* explore_to_leaf( struct kd_node*, struct feature*,struct min_pq* );
//插入一个特征点到最近邻数组，使数组中的点按到目标点的距离升序排列
static int insert_into_nbr_array( struct feature*, struct feature**, int, int );
//判断给定点是否在某矩形中
static int within_rect( CvPoint2D64f, CvRect );


/******************** 已在kdtree.h中声明的函数 **********************/
/******************** Functions prototyped in kdtree.h **********************/

/*根据给定的特征点集合建立k-d树
参数：
features：特征点数组，注意：此函数将会改变features数组中元素的排列顺序
n：特征点个数
返回值：建立好的k-d树的树根指针
*/
/*
A function to build a k-d tree database from keypoints in an array.

@param features an array of features
@param n the number of features in features

@return Returns the root of a kd tree built from features or NULL on error.
*/
struct kd_node* kdtree_build( struct feature* features, int n )
{
	struct kd_node* kd_root;

    //输入参数检查
	if( ! features  ||  n <= 0 )
	{
		fprintf( stderr, "Warning: kdtree_build(): no features, %s, line %d\n",
				__FILE__, __LINE__ );
		return NULL;
	}

    //调用函数，用给定的特征点集初始化k-d树节点，返回值作为树根
	kd_root = kd_node_init( features, n );
    //调用函数，扩展根节点kd_root及其左右孩子
	expand_kd_node_subtree( kd_root );

	return kd_root;
}


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
/*
Finds an image feature's approximate k nearest neighbors in a kd tree using
Best Bin First search.
@param kd_root root of an image feature kd tree
@param feat image feature for whose neighbors to search
@param k number of neighbors to find
@param nbrs pointer to an array in which to store pointers to neighbors
	in order of increasing descriptor distance
@param max_nn_chks search is cut off after examining this many tree entries
@return Returns the number of neighbors found and stored in nbrs, or -1 on error.
*/
int kdtree_bbf_knn( struct kd_node* kd_root, struct feature* feat, int k,
					struct feature*** nbrs, int max_nn_chks )
{
    struct kd_node* expl; //expl是当前搜索节点
    struct min_pq* min_pq; //优先级队列
    struct feature* tree_feat, ** _nbrs; //tree_feat是单个SIFT特征，_nbrs中存放着查找出来的近邻特征节点
    struct bbf_data* bbf_data; //bbf_data是一个用来存放临时特征数据和特征间距离的缓存结构
    int i, t = 0, n = 0; //t是搜索的最大次数，n是当前最近邻数组中的元素个数

    //输入参数检查
	if( ! nbrs  ||  ! feat  ||  ! kd_root )
	{
        fprintf( stderr, "Warning: NULL pointer error, %s, line %d\n", __FILE__, __LINE__ );
		return -1;
	}

    _nbrs = calloc( k, sizeof( struct feature* ) ); //给查找结果分配相应大小的内存
    min_pq = minpq_init(); //min_pq队列初始化，分配默认大小的空间
    minpq_insert( min_pq, kd_root, 0 ); //将根节点先插入到min_pq优先级队列中

    //min_pq队列没有回溯完且未达到搜索最大次数
	while( min_pq->n > 0  &&  t < max_nn_chks )
	{
        //从min_pq中提取(并移除)优先级最高的节点，赋值给当前节点expl
		expl = (struct kd_node*)minpq_extract_min( min_pq );
		if( ! expl )
        {   //出错处理
			fprintf( stderr, "Warning: PQ unexpectedly empty, %s line %d\n",__FILE__, __LINE__ );
			goto fail;
		}
        //从当前搜索节点expl一直搜索到叶子节点，搜索过程中将未搜索的节点根据优先级放入队列，返回值为叶子节点
		expl = explore_to_leaf( expl, feat, min_pq );
		if( ! expl )
        {   //出错处理
			fprintf( stderr, "Warning: PQ unexpectedly empty, %s line %d\n",__FILE__, __LINE__ );
			goto fail;
		}

        //比较查找最近邻
		for( i = 0; i < expl->n; i++ )
		{
            tree_feat = &expl->features[i];//第i个特征点的指针
            bbf_data = malloc( sizeof( struct bbf_data ) );//新建bbf结构
			if( ! bbf_data )
            {   //出错处理
                fprintf( stderr, "Warning: unable to allocate memory," " %s line %d\n", __FILE__, __LINE__ );
				goto fail;
			}
            bbf_data->old_data = tree_feat->feature_data;//保存第i个特征点的feature_data域以前的值
            bbf_data->d = descr_dist_sq(feat, tree_feat);//当前搜索点和目标点之间的欧氏距离
            tree_feat->feature_data = bbf_data;//将bbf结构赋给此特征点的feature_data域
            //判断并插入符合条件的特征点到最近邻数组_nbrs中,插入成功返回1
            //当最近邻数组中元素个数已达到k时，继续插入元素个数不会增加，但会更新元素的值
			n += insert_into_nbr_array( tree_feat, _nbrs, n, k );
		}
        t++;//搜索次数
	}

    minpq_release( &min_pq );//释放优先队列

    //对于最近邻数组中的特征点，恢复其feature_data域的值
	for( i = 0; i < n; i++ )
	{
		bbf_data = _nbrs[i]->feature_data;
        _nbrs[i]->feature_data = bbf_data->old_data;//将之前的数据赋值给feature_data域
		free( bbf_data );
	}
	*nbrs = _nbrs;
	return n;

    //失败处理
fail:
	minpq_release( &min_pq );
    //对于最近邻数组中的特征点，恢复其feature_data域的值
	for( i = 0; i < n; i++ )
	{
		bbf_data = _nbrs[i]->feature_data;
		_nbrs[i]->feature_data = bbf_data->old_data;
		free( bbf_data );
	}
	free( _nbrs );
	*nbrs = NULL;
	return -1;
}



/*
Finds an image feature's approximate k nearest neighbors within a specified
spatial region in a kd tree using Best Bin First search.

@param kd_root root of an image feature kd tree
@param feat image feature for whose neighbors to search
@param k number of neighbors to find
@param nbrs pointer to an array in which to store pointers to neighbors
	in order of increasing descriptor distance
@param max_nn_chks search is cut off after examining this many tree entries
@param rect rectangular region in which to search for neighbors
@param model if true, spatial search is based on kdtree features' model
	locations; otherwise it is based on their image locations

@return Returns the number of neighbors found and stored in \a nbrs
	(in case \a k neighbors could not be found before examining
	\a max_nn_checks keypoint entries).
*/
int kdtree_bbf_spatial_knn( struct kd_node* kd_root, struct feature* feat,
						   int k, struct feature*** nbrs, int max_nn_chks,
						   CvRect rect, int model )
{
	struct feature** all_nbrs, ** sp_nbrs;
	CvPoint2D64f pt;
	int i, n, t = 0;

	n = kdtree_bbf_knn( kd_root, feat, max_nn_chks, &all_nbrs, max_nn_chks );
	sp_nbrs = calloc( k, sizeof( struct feature* ) );
	for( i = 0; i < n; i++ )
	{
		if( model )
			pt = all_nbrs[i]->mdl_pt;
		else
			pt = all_nbrs[i]->img_pt;

		if( within_rect( pt, rect ) )
		{
			sp_nbrs[t++] = all_nbrs[i];
			if( t == k )
				goto end;
		}
	}
end:
	free( all_nbrs );
	*nbrs = sp_nbrs;
	return t;
}


/*释放k-d树占用的存储空间
*/
/*
De-allocates memory held by a kd tree

@param kd_root pointer to the root of a kd tree
*/
void kdtree_release( struct kd_node* kd_root )
{
	if( ! kd_root )
		return;
	kdtree_release( kd_root->kd_left );
	kdtree_release( kd_root->kd_right );
	free( kd_root );
}


/************************ 未暴露接口的一些本地函数 **************************/
/************************ Functions prototyped here **************************/

/*用给定的特征点集初始化k-d树节点
参数：
features：特征点集
n：特征点个数
返回值：k-d树节点指针
*/
/*
Initializes a kd tree node with a set of features.  The node is not
expanded, and no ordering is imposed on the features.

@param features an array of image features
@param n number of features

@return Returns an unexpanded kd-tree node.
*/
static struct kd_node* kd_node_init( struct feature* features, int n )
{
	struct kd_node* kd_node;

    kd_node = malloc( sizeof( struct kd_node ) );//分配内存
	memset( kd_node, 0, sizeof( struct kd_node ) );
    kd_node->ki = -1;//枢轴索引
    kd_node->features = features;//节点对应的特征点集
    kd_node->n = n;//特征点的个数

	return kd_node;
}


/*递归的扩展指定的k-d树节点及其左右孩子
*/
/*
Recursively expands a specified kd tree node into a tree whose leaves
contain one entry each.

@param kd_node an unexpanded node in a kd tree
*/
static void expand_kd_node_subtree( struct kd_node* kd_node )
{
    //基本情况：叶子节点
	/* base case: leaf node */
	if( kd_node->n == 1  ||  kd_node->n == 0 )
	{
        kd_node->leaf = 1;//叶节点标志位设为1
		return;
	}

    //调用函数，确定节点的枢轴索引和枢轴值
	assign_part_key( kd_node );
    //在指定k-d树节点上划分特征点集(即根据指定节点的ki和kv值来划分特征点集)
	partition_features( kd_node );

    //继续扩展左右孩子
	if( kd_node->kd_left )
		expand_kd_node_subtree( kd_node->kd_left );
	if( kd_node->kd_right )
		expand_kd_node_subtree( kd_node->kd_right );
}


/*确定输入节点的枢轴索引和枢轴值
参数：kd_node：输入的k-d树节点
函数执行完后将给kd_node的ki和kv成员复制
*/
/*
Determines the descriptor index at which and the value with which to
partition a kd tree node's features.

@param kd_node a kd tree node
*/
static void assign_part_key( struct kd_node* kd_node )
{
	struct feature* features;
    //枢轴的值kv，均值mean，方差var，方差最大值var_max
	double kv, x, mean, var, var_max = 0;
	double* tmp;
    int d, n, i, j, ki = 0; //枢轴索引ki

	features = kd_node->features;
    n = kd_node->n;//结点个数
    d = features[0].d;//特征向量的维数

    //枢轴的索引值就是方差最大的那一维的维数,即n个128维的特征向量中，若第k维的方差最大，则k就是枢轴(分割位置)
	/* partition key index is that along which descriptors have most variance */
	for( j = 0; j < d; j++ )
	{
		mean = var = 0;
        //求第j维的均值
		for( i = 0; i < n; i++ )
			mean += features[i].descr[j];
		mean /= n;
        //求第j维的方差
		for( i = 0; i < n; i++ )
		{
			x = features[i].descr[j] - mean;
			var += x * x;
		}
		var /= n;
        //找到最大方差的维数
		if( var > var_max )
		{
            ki = j;//最大方差的维数就是枢轴
			var_max = var;
		}
	}

    //枢轴的值就是所有特征向量的ki维的中值(按ki维排序后中间的那个值)
	/* partition key value is median of descriptor values at ki */
	tmp = calloc( n, sizeof( double ) );
	for( i = 0; i < n; i++ )
		tmp[i] = features[i].descr[ki];
    //调用函数，找tmp数组的中值
	kv = median_select( tmp, n );
	free( tmp );

    kd_node->ki = ki;//枢轴的维数索引
    kd_node->kv = kv;//枢轴的值
}


/*找到输入数组的中值
参数：
array：输入数组，元素顺序将会被改动
n：元素个数
返回值：中值
*/
/*
Finds the median value of an array.  The array's elements are re-ordered
by this function.

@param array an array; the order of its elelemts is reordered
@param n number of elements in array

@return Returns the median value of array.
*/
static double median_select( double* array, int n )
{
    //调用函数，找array数组中的第(n-1)/2小的数，即中值
	return rank_select( array, n, (n - 1) / 2 );
}


/*找到输入数组中第r小的数
参数：
array：输入数组，元素顺序将会被改动
n：元素个数
r：找第r小元素
返回值：第r小的元素值
*/
/*
Finds the element of a specified rank in an array using the linear time
median-of-medians algorithm by Blum, Floyd, Pratt, Rivest, and Tarjan.
The elements of the array are re-ordered by this function.
@param array an array; the order of its elelemts is reordered
@param n number of elements in array
@param r the zero-based rank of the element to be selected
@return Returns the element from array with zero-based rank r.
*/
static double rank_select( double* array, int n, int r )
{
	double* tmp, med;
	int gr_5, gr_tot, rem_elts, i, j;

	/* base case */
	if( n == 1 )
		return array[0];

    //将数组分成5个一组，共gr_tot组
	/* divide array into groups of 5 and sort them */
    gr_5 = n / 5; //组的个数-1，n/5向下取整
    gr_tot = cvCeil( n / 5.0 ); //组的个数，n/5向上取整
    rem_elts = n % 5;//最后一组中的元素个数
	tmp = array;
    //对每组进行插入排序
	for( i = 0; i < gr_5; i++ )
	{
		insertion_sort( tmp, 5 );
		tmp += 5;
	}
    //最后一组
	insertion_sort( tmp, rem_elts );

    //找中值的中值
	/* recursively find the median of the medians of the groups of 5 */
	tmp = calloc( gr_tot, sizeof( double ) );
    //将每个5元组中的中值(即下标为2,2+5,...的元素)复制到temp数组
	for( i = 0, j = 2; i < gr_5; i++, j += 5 )
		tmp[i] = array[j];
    //最后一组的中值
	if( rem_elts )
		tmp[i++] = array[n - 1 - rem_elts/2];
    //找temp中的中值med，即中值的中值
	med = rank_select( tmp, i, ( i - 1 ) / 2 );
	free( tmp );

    //利用中值的中值划分数组，看划分结果是否是第r小的数，若不是则递归调用rank_select重新选择
	/* partition around median of medians and recursively select if necessary */
    j = partition_array( array, n, med );//划分数组，返回med在新数组中的索引
    if( r == j )//结果是第r小的数
		return med;
    else if( r < j )//第r小的数在前半部分
		return rank_select( array, j, r );
    else//第r小的数在后半部分
	{
		array += j+1;
		return rank_select( array, ( n - j - 1 ), ( r - j - 1 ) );
	}
}


/*用插入法对输入数组进行升序排序
参数：
array：输入数组
n：元素个数
*/
/*
Sorts an array in place into increasing order using insertion sort.

@param array an array
@param n number of elements
*/
static void insertion_sort( double* array, int n )
{
	double k;
	int i, j;

	for( i = 1; i < n; i++ )
	{
		k = array[i];
		j = i-1;
		while( j >= 0  &&  array[j] > k )
		{
			array[j+1] = array[j];
			j -= 1;
		}
		array[j+1] = k;
	}
}


/*根据给定的枢轴值分割数组，使数组前部分小于pivot，后部分大于pivot
参数：
array：输入数组
n：数组的元素个数
pivot：枢轴值
返回值：分割后枢轴的下标
*/
/*
Partitions an array around a specified value.
@param array an array
@param n number of elements
@param pivot value around which to partition
@return Returns index of the pivot after partitioning
*/
static int partition_array( double* array, int n, double pivot )
{
	double tmp;
	int p, i, j;

	i = -1;
	for( j = 0; j < n; j++ )
		if( array[j] <= pivot )
		{
			tmp = array[++i];
			array[i] = array[j];
			array[j] = tmp;
			if( array[i] == pivot )
                p = i;//p保存枢轴的下标
		}
    //将枢轴和最后一个小于枢轴的数对换
	array[p] = array[i];
	array[i] = pivot;

	return i;
}


/*在指定的k-d树节点上划分特征点集
使得特征点集的前半部分是第ki维小于枢轴的点，后半部分是第ki维大于枢轴的点
*/
/*
Partitions the features at a specified kd tree node to create its two
children.

@param kd_node a kd tree node whose partition key is set
*/
static void partition_features( struct kd_node* kd_node )
{
	struct feature* features, tmp;
	double kv;
	int n, ki, p, i, j = -1;

    features = kd_node->features;//特征点数组
    n = kd_node->n;//特征点个数
    //printf("%d\n",n);
    ki = kd_node->ki;//枢轴的维数索引(哪一维是枢轴)
    kv = kd_node->kv;//枢轴的值
	for( i = 0; i < n; i++ )
    {
        //若第i个特征点的特征向量的第ki维的值小于kv
		if( features[i].descr[ki] <= kv )
		{
			tmp = features[++j];
			features[j] = features[i];
			features[i] = tmp;
			if( features[j].descr[ki] == kv )
                p = j;//p保存枢轴所在的位置
		}
    }
    //将枢轴features[p]和最后一个小于枢轴的点features[j]对换
	tmp = features[p];
	features[p] = features[j];
	features[j] = tmp;
    //此后，枢轴的位置下标为j

    //若所有特征点落在同一侧，则此节点成为叶节点
	/* if all records fall on same side of partition, make node a leaf */
	if( j == n - 1 )
	{
		kd_node->leaf = 1;
		return;
	}

    //初始化左孩子的根节点，左孩子共j+1个特征点
	kd_node->kd_left = kd_node_init( features, j + 1 );
    //初始化右孩子的根节点，右孩子共n-j-1个特征点
	kd_node->kd_right = kd_node_init( features + ( j + 1 ), ( n - j - 1 ) );
}


/*从给定结点搜索k-d树直到叶节点，搜索过程中将未搜索的节点根据优先级放入队列
优先级队列和搜索路径是同时生成的，这也是BBF算法的精髓所在：在二叉搜索的时
候将搜索路径另一侧的分支加入到优先级队列中，供回溯时查找。而优先级队列的排
序就是根据目标特征与分割超平面的距离ABS(kv - feat->descr[ki])
参数：
kd_node：要搜索的子树的树根
feat：目标特征点
min_pq：优先级队列
返回值：叶子节点的指针
*/
/*
Explores a kd tree from a given node to a leaf.  Branching decisions are
made at each node based on the descriptor of a given feature.  Each node
examined but not explored is put into a priority queue to be explored
later, keyed based on the distance from its partition key value to the
given feature's desctiptor.
@param kd_node root of the subtree to be explored
@param feat feature upon which branching decisions are based
@param min_pq a minimizing priority queue into which tree nodes are placed as described above
@return Returns a pointer to the leaf node at which exploration ends or
	NULL on error.
*/
static struct kd_node* explore_to_leaf( struct kd_node* kd_node, struct feature* feat,
										struct min_pq* min_pq )
{
    //unexpl中存放着优先级队列的候选节点(还未搜索的节点)，expl为当前搜索节点
	struct kd_node* unexpl, * expl = kd_node;
    double kv;//分割枢轴的值
    int ki;//分割枢轴的维数索引

    //一直搜索到叶子节点，搜索过程中将未搜索的节点根据优先级放入队列
	while( expl  &&  ! expl->leaf )
	{
		ki = expl->ki;
		kv = expl->kv;

        //枢轴的维数索引大于特征点的维数，出错
		if( ki >= feat->d )
		{
            fprintf( stderr, "Warning: comparing imcompatible descriptors, %s" " line %d\n", __FILE__, __LINE__ );
			return NULL;
		}
        //目标特征点ki维的数据小于等于kv，进入左子树搜索
		if( feat->descr[ki] <= kv )
		{
            unexpl = expl->kd_right;//候选搜索节点是expl的右子树的根
            expl = expl->kd_left;//当前搜索节点是expl的左子树的根
		}
        else//目标特征点ki维的数据大于kv，进入右子树搜索
		{
            unexpl = expl->kd_left;//候选搜索节点是expl的左子树
            expl = expl->kd_right;//当前搜索节点是expl的右子树
		}

        //将候选节点unexpl根据目标特征点ki维与其父节点的距离插入到优先队列中，距离越小，优先级越大
		if( minpq_insert( min_pq, unexpl, ABS( kv - feat->descr[ki] ) ) )
		{
			fprintf( stderr, "Warning: unable to insert into PQ, %s, line %d\n",__FILE__, __LINE__ );
			return NULL;
		}
	}

    return expl;//返回叶子节点的指针
}


/*插入一个特征点到最近邻数组，使数组中的点按到目标点的距离升序排列
参数：
feat：要插入的特征点，其feature_data域应是指向bbf_data结构的指针，其中的d值时feat和目标点的距离的平方
nbrs：最近邻数组
n：已在最近邻数组中的元素个数
k：最近邻数组元素个数的最大值
返回值：若feat成功插入，返回1，否则返回0
*/
/*
Inserts a feature into the nearest-neighbor array so that the array remains
in order of increasing descriptor distance from the search feature.

@param feat feature to be inderted into the array; it's feature_data field
	should be a pointer to a bbf_data with d equal to the squared descriptor
	distance between feat and the search feature
@param nbrs array of nearest neighbors neighbors
@param n number of elements already in nbrs and
@param k maximum number of elements in nbrs

@return If feat was successfully inserted into nbrs, returns 1; otherwise
	returns 0.
*/
static int insert_into_nbr_array( struct feature* feat, struct feature** nbrs, int n, int k )
{
    struct bbf_data* fdata, * ndata;//fdata是要插入的点的bbf结构，ndata是最近邻数组中的点的bbf结构
    double dn, df; //dn是最近邻数组中特征点的bbf结构中的距离值，df是要插入的特征点的bbf结构中的距离值
	int i, ret = 0;

    //原最近邻数组为空
	if( n == 0 )
	{
		nbrs[0] = feat;
        return 1;//插入成功，返回1
	}

	/* check at end of array */
    fdata = (struct bbf_data*)feat->feature_data;//要插入点的bbf结构
    df = fdata->d;//要插入的特征点的bbf结构中的距离值
    ndata = (struct bbf_data*)nbrs[n-1]->feature_data;//最近邻数组中的点的bbf结构
    dn = ndata->d;//最近邻数组中最后一个特征点的bbf结构中的距离值

    //df>=dn，说明要插入在最近邻数组的末尾
	if( df >= dn )
	{
        //最近邻数组中元素个数已达到最大值，无法插入
		if( n == k )
		{
            feat->feature_data = fdata->old_data;//不明白这里是干什么？
			free( fdata );
            return 0;//插入失败，返回0
		}
        nbrs[n] = feat;//插入到末尾
        return 1;//插入成功，返回1
	}

    //运行到此处说明插入位置不在数组末尾
	/* find the right place in the array */
    if( n < k )//最近邻数组中元素个数小于最大值，可插入
	{
        nbrs[n] = nbrs[n-1];//原数组最后一个点后移
        ret = 1;//插入结果设为1
	}
    else//最近邻数组中元素个数大于或等于最大值，无法插入，插入结果ret还是0
    {//其实也不是无法插入，而是最近邻数组中元素个数不变，但值会更新
		nbrs[n-1]->feature_data = ndata->old_data;
		free( ndata );
	}
	i = n-2;
    //在最近邻数组中查找要插入的位置
	while( i >= 0 )
	{
		ndata = (struct bbf_data*)nbrs[i]->feature_data;
		dn = ndata->d;
        if( dn <= df )//找到插入点
			break;
        nbrs[i+1] = nbrs[i];//一次后移
		i--;
	}
	i++;
    nbrs[i] = feat;//插入

    return ret;//返回结果
}


/*判断给定点是否在某矩形中
*/
/*
Determines whether a given point lies within a specified rectangular region
@param pt point
@param rect rectangular region
@return Returns 1 if pt is inside rect or 0 otherwise
*/
static int within_rect( CvPoint2D64f pt, CvRect rect )
{
	if( pt.x < rect.x  ||  pt.y < rect.y )
		return 0;
	if( pt.x > rect.x + rect.width  ||  pt.y > rect.y + rect.height )
		return 0;
	return 1;
}

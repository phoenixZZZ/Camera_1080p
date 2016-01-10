#include <stdio.h>
#include <stdlib.h>
#include "g7.h"
#include "g711.h"




/*G711DECODE_API */int decode_Init(void)
{
	return g711_ERR_NOERROR;
}



/*G711DECODE_API */int g711a_Encode(unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen)
{
	int	i,tmp;
	signed short src_data;
	unsigned char	mask;
	unsigned short *ps=(unsigned short*)src;	
	
	*dstlen=0;
	tmp = srclen/2;

	for(i=0; i<tmp; i++)
	{
		//dest[i]=linear2alaw(ps[i]);
		src_data = ps[i];
		mask = (src_data < 0) ? 0x7f : 0xff;
		if (src_data < 0)
			src_data = -src_data;
		src_data >>= 4;
		dest[i] = _l2A[src_data] & mask;
	}
	*dstlen = srclen/2;
	return 1;
}



int g711a_Decode( unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen)
{
	int	i;
	unsigned short *pd=(unsigned short*)dest;
	for(i=0; i<srclen; i++)
	{	
	
		pd[i]=(unsigned short)_A2l[src[i]];
	}
	*dstlen = srclen<<1;

	return 1;
}



int g711u_Encode(unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen)
{
	int	i,tmp;
	signed short src_data;
	unsigned char	mask;
	unsigned short *ps=(unsigned short*)src;	
	static unsigned int data_type = 0xffffffff;
	
	*dstlen=0;
	tmp = srclen/2;

	for(i=0; i<tmp; i++)
	{
		dest[i]=linear2ulaw(ps[i]);

	}
	*dstlen = srclen/2;
	return 1;
}


int g711u_Decode( unsigned char *src,unsigned char *dest,unsigned int srclen,unsigned int *dstlen)
{
	int	i;
	unsigned short *pd=(unsigned short*)dest;
	for(i=0; i<srclen; i++)
	{	
	
		pd[i]=(unsigned short)_u2l[src[i]];
//		pd[i]=(unsigned short)_l2u[src[i]];
	}
	*dstlen = srclen<<1;

	return 1;
}

int decode_Cleanup(void)
{
	return g711_ERR_NOERROR;
}

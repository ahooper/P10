#include "native/p10a.h"
#include "std/lang/Array.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

extern struct mtab_std_lang_Array MTab_std_lang_Array;
	
/*
booleans static(size int) boolean[] native
bytes static(size int) byte[] native
chars static(size int) char[] native
doubles static(size int) double[] native
floats static(size int) float[] native
ints static(size int) int[] native
longs static(size int) long[] native
shorts static(size int) short[] native
objects static(size int) Object[] native

copy static(src boolean[], srcPos int, length int, dest boolean[], destPos int) native
copy static(src byte[], srcPos int, length int, dest byte[], destPos int) native
copy static(src char[], srcPos int, length int, dest char[], destPos int) native
copy static(src double[], srcPos int, length int, dest double[], destPos int) native
copy static(src float[], srcPos int, length int, dest float[], destPos int) native
copy static(src int[], srcPos int, length int, dest int[], destPos int) native
copy static(src long[], srcPos int, length int, dest long[], destPos int) native
copy static(src short[], srcPos int, length int, dest short[], destPos int) native
copy static(src Object[], srcPos int, length int, dest Object[], destPos int) native
*/

#define TEMPLATE(pType,cType)													\
array_##cType* std_lang_Array_##pType##s_Pint(int32_t length) {					\
	/*DEBUG**fprintf(stderr,"std_lang_Array_"#pType"s %d\n",length);*/			\
	array_##cType* a = calloc(sizeof(array_##cType)+sizeof(a->elements[0])*length,1);	\
	a->_mtt = &MTab_std_lang_Array;												\
	a->length = length;															\
	return a;																	\
}																				\
void std_lang_Array_copy_P##pType##_Y_Pint_Pint_P##pType##_Y_Pint(array_##cType* src, int32_t srcPos, int32_t length, array_##cType* dest, int32_t destPos) { \
	memcpy(dest->elements+destPos, src->elements+srcPos, length*sizeof(src->elements[0]));	\
}
//TODO check null pointers and other parameters valid
	
TEMPLATE(boolean,int1_t)
TEMPLATE(byte,int8_t)
TEMPLATE(char,int32_t)
TEMPLATE(double,double)
TEMPLATE(float,float)
TEMPLATE(int,int32_t)
TEMPLATE(long,int64_t)
TEMPLATE(short,int16_t)
TEMPLATE(Object,obj_std_lang_Object)

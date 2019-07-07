class std.lang.Array
length int // Must be first field to match CodeGen emitArrayLength

booleans static(length int) boolean[] native
bytes static(length int) byte[] native
chars static(length int) char[] native
doubles static(length int) double[] native
floats static(length int) float[] native
ints static(length int) int[] native
longs static(length int) long[] native
shorts static(length int) short[] native
Objects static(length int) Object[] native

copy static(src boolean[], srcPos int, length int, dest boolean[], destPos int) native
copy static(src byte[], srcPos int, length int, dest byte[], destPos int) native
copy static(src char[], srcPos int, length int, dest char[], destPos int) native
copy static(src double[], srcPos int, length int, dest double[], destPos int) native
copy static(src float[], srcPos int, length int, dest float[], destPos int) native
copy static(src int[], srcPos int, length int, dest int[], destPos int) native
copy static(src long[], srcPos int, length int, dest long[], destPos int) native
copy static(src short[], srcPos int, length int, dest short[], destPos int) native
copy static(src Object[], srcPos int, length int, dest Object[], destPos int) native

copyOf static(a boolean[], pos int, length int) boolean[]
	c boolean[]
	c : booleans(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a byte[], pos int, length int) byte[]
	c byte[] : bytes(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a char[], pos int, length int) char[]
	c char[] : chars(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a double[], pos int, length int) double[]
	c double[] : doubles(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a float[], pos int, length int) float[]
	c float[] : floats(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a int[], pos int, length int) int[]
	c int[] : ints(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a long[], pos int, length int) long[]
	c long[] : longs(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a short[], pos int, length int) short[]
	c short[] : shorts(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a Object[], pos int, length int) Object[]
	c Object[] : Objects(length)
	copy(a,pos,length,c,0)
	return c
.
copyOf static(a byte[]) byte[]
	return copyOf(a,0,a.length)
.
copyOf static(a char[]) char[]
	return copyOf(a,0,a.length)
.
copyOf static(a double[]) double[]
	return copyOf(a,0,a.length)
.
copyOf static(a float[]) float[]
	return copyOf(a,0,a.length)
.
copyOf static(a int[]) int[]
	return copyOf(a,0,a.length)
.
copyOf static(a long[]) long[]
	return copyOf(a,0,a.length)
.
copyOf static(a short[]) short[]
	return copyOf(a,0,a.length)
.
copyOf static(a Object[]) Object[]
	return copyOf(a,0,a.length)
.

// fill static (type[],position,length,type)
// fill(type[],type)

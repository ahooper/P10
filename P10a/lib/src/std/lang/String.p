class std.lang.String
//interface std.test.TestSuite

#value byte[]
#count int
#offset int
EMPTY static String : "" 

new()
    value : EMPTY.value
    offset : 0
    count : 0
.

/** Initialize a new String as a clone of another 'str'.
 */
new(str String)
    value : str.value
    offset : str.offset
    count : str.count
.

/** Initialize a new String from a byte array 'bytes', starting at index 'offset'
 *	with a length of 'count' bytes.
 */
new(bytes byte[], offset int, count int)
    this.value : bytes
    this.offset : offset
    this.count : count
.

/** Initialize a new String from a byte array 'bytes'.
 */
new(bytes byte[])
//	new(bytes,0,bytes.length)
	this.value : bytes
	this.offset : 0
	this.count : bytes.length
.

/** Get the number of UTF-8 encoded bytes in the string
 *	@return the number of UTF-8 encoded bytes
 */ 
byteSize() int
	return count
.

/** Get the byte at index 'x' in the string
 *	@return the byte
 */ 
byteAt(x int) byte
	//TODO if x ≥ count throw index error
	return value[x+offset]
.

/** Get the number of bytes in the UTF-8 encoding of the character 'c'.
 *	@return the number of UTF-8 encoded bytes
 */ 
charSize(c char) int
    if c < 0x80
        return 1
    elsif c < 0x800
        return 2
    elsif c < 0x10000
        return 3
    elsif c < 0x110000
        return 4
    .
    return -1 // not a valid character code
.

/** Get the number of UTF-8 code points in the string.
 *	@return the number of UTF-8 codepoints
 */ 
charCount() int
	// more correctly, codepointCount
	cc int : 0
	for x int : 0 while x < count
		cc : cc + 1
		c char : value[x+offset] as char
		if c < 0x80
			x : x + 1		
		//TODO elsif c < 0xC0
		//TODO throw bad encoding
		elsif c < 0xE0
			x : x + 2		
		elsif c < 0xF0
			x : x + 3		
		else
			x : x + 4		
		.		
	.
	return cc
.

/** Get the Unicode code point at the byte index 'x' in the string
 *	@return the Unicode code point
 */ 
charAt(x int) char
	//TODO if x ≥ count throw index error
	c char : value[x+offset] as char
	if c < 0x80
	elsif c < 0xC0
		//TODO throw bad encoding
	elsif c < 0xE0
		//TODO if x+1 ≥ count throw bad encoding
		c : ( ((c & 0x3F)                  << 6) |
		      (value[x+offset+1] & 0x3F) ) as char
	elsif c < 0xF0
		//TODO if x+2 ≥ count throw bad encoding
		c : ( (( ((c & 0x3F)                  << 6) |
		         (value[x+offset+1] & 0x3F) ) << 6) |
		         (value[x+offset+2] & 0x3F) ) as char
	else
		//TODO if x+3 ≥ count throw bad encoding
		c : ( (( (( ((c & 0x3F)                  << 6) |
		            (value[x+offset+1] & 0x3F) ) << 6) |
		            (value[x+offset+2] & 0x3F) ) << 6) |
		            (value[x+offset+3] & 0x3F) ) as char
	.		
	return c
.

equals(str String) boolean
	if str.count = count
		return regionMatches(0, str, 0, str.count)
	.
	return false
.

isEmpty() boolean
	return count = 0
.

indexOf(str String) int
	return indexOf(str, 0)
.

indexOf(str String, fromIndex int) int
	if fromIndex < 0
		fromIndex : 0
	.
	limit int : count - str.count
	while fromIndex ≤ limit
		if regionMatches(fromIndex, str, 0, str.count)
			return fromIndex
		.
		fromIndex : fromIndex + 1
	.
    return -1 // not found
.

indexOf(c char) int
	return indexOf(c, 0)
.

indexOf(c char, fromIndex int) int
	if fromIndex < 0
		fromIndex : 0
	.
	limit int
    if c < 0x80
        limit : count - 1
		while fromIndex ≤ limit
			if value[fromIndex+offset] = c
				return fromIndex
			.
			fromIndex : fromIndex + 1
		.
    elsif c < 0x800
        limit : count - 2
        b1 int : (c >> 6)   | 0xC0
        b2 int : (c & 0x3F) | 0x80
		while fromIndex ≤ limit
			if value[fromIndex+offset]   = b1 &&
			   value[fromIndex+offset+1] = b2
				return fromIndex
			.
			fromIndex : fromIndex + 1
		.
    elsif c < 0x10000
        limit : count - 3
        b1 int : (c >> 12)         | 0xE0
        b2 int : ((c >> 6) & 0x3F) | 0x80
        b3 int : (c        & 0x3F) | 0x80
		while fromIndex ≤ limit
			if value[fromIndex+offset]   = b1 &&
			   value[fromIndex+offset+1] = b2 &&
			   value[fromIndex+offset+2] = b3
				return fromIndex
			.
			fromIndex : fromIndex + 1
		.
    elsif c < 0x110000
        limit : count - 4
        b1 int : (c >> 18)          | 0xF0
        b2 int : ((c >> 12) & 0x3F) | 0x80
        b3 int : ((c >>  6) & 0x3F) | 0x80
        b4 int : (c         & 0x3F) | 0x80
		while fromIndex ≤ limit
			if value[fromIndex+offset]   = b1 &&
			   value[fromIndex+offset+1] = b2 &&
			   value[fromIndex+offset+2] = b3 &&
			   value[fromIndex+offset+3] = b4
				return fromIndex
			.
			fromIndex : fromIndex + 1
		.
    .
    return -1 // not found
.

lastIndexOf(str String) int
	return lastIndexOf(str, count - str.count)
.

lastIndexOf(str String, fromIndex int) int
    f int : count - str.count
    if f < fromIndex
		fromIndex : f
	.
	while fromIndex ≥ 0
		if regionMatches(fromIndex, str, 0, str.count)
			return fromIndex
		.
		fromIndex : fromIndex - 1
	.
    return -1
.

lastIndexOf(c char) int
	return indexOf(c, 0)
.

lastIndexOf(c char, fromIndex int) int
    if c < 0x80
		f int : count - 1
	    if f < fromIndex
			fromIndex : f
		.
		while fromIndex ≥ 0
			if value[fromIndex+offset] = c
				return fromIndex
			.
			fromIndex : fromIndex - 1
		.
    elsif c < 0x800
		f int : count - 2
	    if f < fromIndex
			fromIndex : f
		.
        b1 int : (c >> 6)   | 0xC0
        b2 int : (c & 0x3F) | 0x80
		while fromIndex ≥ 0
			if value[fromIndex+offset]   = b1 &&
			   value[fromIndex+offset+1] = b2
				return fromIndex
			.
			fromIndex : fromIndex - 1
		.
    elsif c < 0x10000
		f int : count - 3
	    if f < fromIndex
			fromIndex : f
		.
        b1 int : (c >> 12)         | 0xE0
        b2 int : ((c >> 6) & 0x3F) | 0x80
        b3 int : (c        & 0x3F) | 0x80
		while fromIndex ≥ 0
			if value[fromIndex+offset]   = b1 &&
			   value[fromIndex+offset+1] = b2 &&
			   value[fromIndex+offset+2] = b3
				return fromIndex
			.
			fromIndex : fromIndex - 1
		.
    elsif c < 0x110000
		f int : count - 4
	    if f < fromIndex
			fromIndex : f
		.
        b1 int : (c >> 18)          | 0xF0
        b2 int : ((c >> 12) & 0x3F) | 0x80
        b3 int : ((c >>  6) & 0x3F) | 0x80
        b4 int : (c         & 0x3F) | 0x80
		while fromIndex ≥ 0
			if value[fromIndex+offset]   = b1 &&
			   value[fromIndex+offset+1] = b2 &&
			   value[fromIndex+offset+2] = b3 &&
			   value[fromIndex+offset+3] = b4
				return fromIndex
			.
			fromIndex : fromIndex - 1
		.
    .
    return -1 // not found
.

regionMatches(sIndex int, other String, oIndex int, len int) boolean
	if sIndex < 0 || sIndex+len > count ||
		oIndex < 0 || oIndex+len > other.count
		return false
	.
	sIndex : sIndex + offset
	oIndex : oIndex + other.offset
	while len > 0
		if value[sIndex] ≠ other.value[oIndex]
			return false
		.
		len : len - 1
	.
    return true
.

startsWith(prefix String, sIndex int) boolean
	return regionMatches(sIndex, prefix, 0, prefix.count)
.

startsWith(prefix String) boolean
	return regionMatches(0, prefix, 0, prefix.count)
.

endsWith(suffix String) boolean
    return regionMatches(count - suffix.count, suffix, 0, suffix.count)
.

substring(begin int, end int) String
	if begin < 0 || begin > end
		Throw.stringIndexOutOfBoundsFault(begin)
	elsif end > count
		Throw.stringIndexOutOfBoundsFault(end)
	.
	if begin = 0 && end = count
		return this
	.
	return String(value, offset + begin, end - begin)
.

substring(begin int) String
	return substring(begin, count)
.

/*TODO from Java
	public String(char[] value)
	public String(char[] value, int offset, int count)
	public String toString();
	public boolean equals(Object anObject);
	public int hashCode();
	public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
	public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin)
	public char[] toCharArray();
	public boolean equalsIgnoreCase(String anotherString);
	public int compareTo(String anotherString)
	public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len)
	public String concat(String str)
	public String replace(char oldChar, char newChar);
	public String toLowerCase();
	public String toUpperCase();
	public String trim();
	public static String valueOf(Object obj);
	public static String valueOf(char[] data)
	public static String valueOf(char[] data, int offset, int count)
	public static String valueOf(boolean b);
	public static String valueOf(char c);
	public static String valueOf(int i);
	public static String valueOf(long l);
	public static String valueOf(float f);
	public static String valueOf(double d);
	public String intern();
*/

unitTest static()
	
.

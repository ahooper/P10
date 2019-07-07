class std.io.BufferedWriter
extends std.io.Writer // implicit import
/** The subordinate Writer that receives writes */
#w Writer
/** The buffer array */
#buffer byte[]
/** The number of valid bytes in the buffer */ 
#count int

/** Initialize a BufferedWriter to write to the specified
 *  subordinate Writer 'sub', with a default buffer size.
 */
new(sub Writer)
	w : sub
	buffer : Array.bytes(2048)
	count : 0
.

/** Initialize a BufferedWriter to write to the specified
 *  subordinate Writer 'sub', with a specified buffer size 'bufferSize'.
 */
new(sub Writer, bufferSize int)
	//TODO minimum bufferSize is 4 to hold a Unicode character
	w : sub
	buffer : Array.bytes(bufferSize)
	count : 0
.

/** Close this file and release any associated system resources.
 */
close()
	flush()
	w.close()
.

/** Write buffered data to this file
 */
flush()
	r int
	if count > 0
		r : w.write(buffer,0,count)
		//TODO check return
		count : 0
	.
.

/**	Write the byte 'b' to this file
 */
write(b byte)
	if count+1 > buffer.length
		flush()
	.
	buffer[count] : b
	count : count+1
.

/**	Write the character 'c' to this file with UTF-8 encoding
 */
write(c char)
	if c < 0x80
		if count+1 > buffer.length
			flush()
		.
		buffer[count] : c as byte
		count : count+1
	elsif c < 0x800
		if count+2 > buffer.length
			flush()
		.
		buffer[count  ] : ( (c >> 6)           | 0xC0) as byte
		buffer[count+1] : (( c         & 0x3F) | 0x80) as byte
		count : count+2
	elsif c < 0x10000
		if count+3 > buffer.length
			flush()
		.
		buffer[count  ] : ( (c >> 12)         | 0xE0) as byte
		buffer[count+1] : (((c >> 6)  & 0x3F) | 0x80) as byte
		buffer[count+2] : (( c        & 0x3F) | 0x80) as byte
		count : count+3
	elsif c < 0x110000
		if count+4 > buffer.length
			flush()
		.
		buffer[count  ] : ( (c >> 18)         | 0xF0) as byte
		buffer[count+1] : (((c >> 12) & 0x3F) | 0x80) as byte
		buffer[count+2] : (((c >> 6)  & 0x3F) | 0x80) as byte
		buffer[count+3] : (( c        & 0x3F) | 0x80) as byte
		count : count+4
	else
		//TODO throw invalid argument
	.
.

/** Write 'length' bytes from the specified byte array 'data', starting at 'offset', to
 *	this file.
 */
write(data byte[], offset int, length int)
	n int : buffer.length - count
	if length < n
		Array.copy(data,offset,length,buffer,count)
		count : count + length
	.
	if count > 0
		w.write(buffer,0,count,
		        data,offset,length)
				// two segments, one system call
		count : 0
	else
		w.write(data,offset,length)
	.
.

/** Write the string 's' to this file.
 */
write(s String)
	bytes int : s.byteSize()
	for x int : 0 while x < bytes next x : x + 1
		write(s.byteAt(x))
	.
.

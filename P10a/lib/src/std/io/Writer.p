class std.io.Writer
//LATER will be an interface

/** Release any associated system resources.*/
close()
	// dummy
.

/** Write 'length' bytes from the specified byte array 'buffer' starting at 'offset'.
 *  \return the actual number of bytes written
 */
write(buffer byte[], offset int, length int) int
	// dummy
	return length
.

/** Write 'buffer.length()' bytes from the specified byte array.
 * \return the actual number of bytes written
 */
write(buffer byte[]) int
	return write(buffer, 0, buffer.length)
.

/** Write 'length1' bytes from the first specified byte array 'buffer1' starting at index 'offset1',
 *  followed by 'length2' bytes from the second byte array 'buffer2' starting at index 'offset2'.
 *  This method is provided to allow an optimization in syscalls for
 *  FileWriter.
 *  \return the actual number of bytes written
 */
write(buffer1 byte[], offset1 int, length1 int,
      buffer2 byte[], offset2 int, length2 int) int
	r int : 0
	r : write(buffer1,offset1,length1)
	r : r + write(buffer2,offset2,length2)
	return r
.

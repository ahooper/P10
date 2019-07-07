class std.io.RandomAccessIO
//LATER extends FileIO

/** Platform reference (handle or descriptor) for this file
 */
#fileRef int

/** Close this file and releases any associated system resources.
 */
close() native

/** Read up to {length} bytes of data from this file into an array {buffer} of bytes.
 *  @return the actual number of bytes read
 */
read(buffer byte[], offset int, length int) int native

/** Write {length} bytes from the specified byte array {buffer} starting at {offset} to this file.
 *  @return the actual number of bytes written
 */
write(buffer byte[], offset int, length int) int native

/** Skip over and discard {length} bytes of data from the file.
 */
skip(length int) native

/** Return the current offset in this file.
 */
getPointer() long native

/** Set the file-pointer to {position}, measured from the beginning of this file, at which the next read or write occurs.
 */
seek(position long) native

/** Return the length of this file.
 */
length() long native

/** Set the length of this file.
 */
setLength(newLength long) native

/** Read up to {buffer.length()} bytes of data from this file into an array of bytes {buffer}.
 *  @return the actual number of bytes read
 */
read(buffer byte[]) int
	return read(buffer, 0, buffer.length)
.

/** Write {buffer.length()} bytes from the specified byte array {buffer} to this file.
 * @return the actual number of bytes written
 */
write(buffer byte[]) int
	return write(buffer, 0, buffer.length)
.

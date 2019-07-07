class std.io.Reader
//LATER will be an interface
/** Platform reference (handle or descriptor) for this file*/
#fileRef int

/** Closes this file and releases any associated system resources.*/
close() native

/** Reads up to length bytes of data from this file into an array of bytes.
 *  \returns the actual number of bytes read
 */
read(buffer byte[], offset int, length int) int native

/** Skips over and discards length bytes of data from the file.
 */
skip(length int) native

/** Reads up to buffer.length() bytes of data from this file into an array of bytes.
 *  \returns the actual number of bytes read
 */
read(buffer byte[]) int
	return read(buffer, 0, buffer.length)
.

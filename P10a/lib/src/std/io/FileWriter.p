class std.io.FileWriter
extends std.io.Writer
/** Platform reference (handle or descriptor) for this file*/
#fileRef int

new(fref int)
	fileRef : fref
.

/** Close this file and releases any associated system resources.*/
close() native

/** Write {length} bytes from the specified byte array {buffer} starting at {offset} to this file.
 *  @return the actual number of bytes written
 */
write(buffer byte[], offset int, length int) int native

/** Write {length} bytes from the first specified byte array {buffer1} starting at {offset1},
 *  followed by a second byte array {buffer2}.
 *  This method is allows an optimization in syscalls for FileWriter.
 *  @return the actual number of bytes written
 */
write(buffer1 byte[], offset1 int, length1 int,
      buffer2 byte[], offset2 int, length2 int) int native

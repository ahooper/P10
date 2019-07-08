/*	std_io_FileWriter.c

	Copyright 2019 Andrew Hooper

	This file is part of the P10 Compiler.

	The P10 Compiler is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
#include "native/p10a.h"
#include "std/io/FileWriter.h"

#include <fcntl.h>
#include <unistd.h>
#include <sys/uio.h>
//#include <stdio.h> 	/*DEBUG*/

// TODO obj_std_io_FileWriter* std_io_FileWriter___newInstance_Pint(int32_t);

/** Closes this file and releases any associated system resources.*/
// close() native
void std_io_FileWriter_close(obj_std_io_FileWriter* this) {
	//*DEBUG*/fprintf(stderr,"std_io_FileWriter_close %d\n", this->fileRef);
	if (this->fileRef == INT32_MAX) {
		// not open
	} else {
		// http://pubs.opengroup.org/onlinepubs/9699919799/functions/close.html
		int r = close(this->fileRef);
		if (r == 0) {
			this->fileRef = INT32_MAX;
		} else {
			// TODO failed, check errno
		}
	}
}

/** Writes length bytes from the specified byte array starting at offset to this file.
 *  \returns the actual number of bytes written
 */
// write(buffer byte[], offset int, length int) int native
int32_t std_io_FileWriter_write_Pbyte_Y_Pint_Pint(obj_std_io_FileWriter* this, array_int8_t* buffer, int32_t offset, int32_t length) {
	//*DEBUG*/fprintf(stderr,"std_io_FileWriter_write %d %d\n", this->fileRef, length);
	// TODO check null pointers, other argument validation
	// http://pubs.opengroup.org/onlinepubs/9699919799/functions/write.html
	ssize_t r = write(this->fileRef, buffer->elements + offset, length);
	if (r < 0) {
		// TODO failed, check errno
	}
	return r;
}

/** Writes length bytes from the first specified byte array starting at offset,
 *  followed by a second byte array.
 *  This method is allows an optimization in syscalls for FileWriter.
 *  \returns the actual number of bytes written
 */
// write(buffer1 byte[], offset1 int, length1 int,
//       buffer2 byte[], offset2 int, length2 int) int native
int32_t std_io_FileWriter_write_Pbyte_Y_Pint_Pint_Pbyte_Y_Pint_Pint(obj_std_io_FileWriter* this,
	 	array_int8_t* buffer1, int32_t offset1, int32_t length1,
		array_int8_t* buffer2, int32_t offset2, int32_t length2) {
	//*DEBUG*/fprintf(stderr,"std_io_FileWriter_write %d %d %d\n", this->fileRef, length1, length2);
	// TODO check null pointers, other argument validation
	// http://pubs.opengroup.org/onlinepubs/9699919799/functions/writev.html
	struct iovec iovs[2] = {
		{ .iov_base = buffer1+offset1, .iov_len = length1 },
		{ .iov_base = buffer2+offset2, .iov_len = length2 }
	};
	ssize_t r = writev(this->fileRef, iovs, 2);
	if (r < 0) {
		// TODO failed, check errno
	}
	return r;
}

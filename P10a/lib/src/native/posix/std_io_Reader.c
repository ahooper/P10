/*	std_io_Reader.c

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
#include "std/io/Reader.h"

#include <fcntl.h>
#include <unistd.h>

/** Closes this file and releases any associated system resources.*/
// close() native
void std_io_Reader_close(obj_std_io_Reader* this) {
	// TODO flush
	if (this->fileRef == INT32_MAX) {
		// not open
	} else {
		int r = close(this->fileRef);
		if (r == 0) {
			this->fileRef = INT32_MAX;
		} else {
			// TODO failed, check errno
		}
	}
}

/** Reads up to length bytes of data from this file into an array of bytes.
 *  \returns the actual number of bytes read
 */
// read(buffer byte[], offset int, length int) int native
int32_t std_io_Reader_read_Pbyte_Y_Pint_Pint(obj_std_io_Reader* this, array_int8_t* buffer, int32_t offset, int32_t length) {
	// TODO check null pointers, other argument validation
	ssize_t r = read(this->fileRef, buffer->elements + offset, length);
	if (r < 0) {
		// TODO failed, check errno
	}
	return r;
}

/** Skips over and discards length bytes of data from the file.
 */
// skip(length int) native
void std_io_Reader_skip_Pint(obj_std_io_Reader* this,int32_t length) {
	// TODO check null pointers, other argument validation
	off_t r = lseek(this->fileRef, length, SEEK_CUR);
	if (r < 0) {
		// TODO failed, check errno
	}
}


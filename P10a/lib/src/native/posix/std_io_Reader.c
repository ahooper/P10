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


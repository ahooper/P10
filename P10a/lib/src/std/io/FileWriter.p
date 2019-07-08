/*	FileWriter.p
 
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

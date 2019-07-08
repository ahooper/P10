/*	Writer.p
 
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

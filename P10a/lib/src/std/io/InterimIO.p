/*	InterimIO.p
 
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
class std.io.InterimIO

// Simple interim output interface, will be replaced with std.io

putByte static(b int) int native

putChar static(c int) int
	r int
	if c < 0x80
		return putByte(c)
	elsif c < 0x800
		r : putByte((c >> 6)           | 0xC0)
		if r ≠ 0
			return r
		.
		return putByte((c      & 0x3F) | 0x80)
	elsif c < 0x10000
		r : putByte( (c >> 12)         | 0xE0)
		if r ≠ 0
			return r
		.
		r : putByte(((c >> 6)  & 0x3F) | 0x80)
		if r ≠ 0
			return r
		.
		return putByte((c      & 0x3F) | 0x80)
	elsif c < 0x110000
		r : putByte((c >> 18)          | 0xF0)
		if r ≠ 0
			return r
		.
		r : putByte(((c >> 12) & 0x3F) | 0x80)
		if r ≠ 0
			return r
		.
		r : putByte(((c >> 6)  & 0x3F) | 0x80)
		if r ≠ 0
			return r
		.
		return putByte((c      & 0x3F) | 0x80)
	else
		//TODO throw invalid argument
	.
.

print static(s String)
	bytes int : s.byteSize()
	for x int : 0 while x < bytes next x : x + 1
		putByte(s.byteAt(x))
	.
.

print static(bytes byte[])
	length int : bytes.length
	for x int : 0 while x < length next x : x + 1
		putByte(bytes[x])
	.
.

print static(n int)
	if n < 0
		if n = -2147483648	// edge case for INTMIN, which would overflow if negated below
			print('-2147483648')
			return
		.
		putChar(`-)
		n : - n
	.
	if n < 10  // common cases
		putChar(n % 10 + `0)
	elsif n < 100
		putChar(n / 10 + `0)
		putChar(n % 10 + `0)
	else
		d int : 1000000000
		while d > n
			d : d / 10
		.
		while d > 0
			putChar(n / d + `0)
			n : n % d
			d : d / 10
		.
	.
.

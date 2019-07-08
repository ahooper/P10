/*	Formatter.p
 
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
class std.text.Formatter
import std.io.BufferedWriter
#writer BufferedWriter

new(bw BufferedWriter)
	writer : bw
.

/**	Format the character value {v}
 *	@return the formatter for fluent chaining
 */ 
c(v char) Formatter
	writer.write(v)
	return this
.

/**	Format the string value {v}, with blank padding to a width of {w}.
 *	@return the formatter for fluent chaining
 */ 
s(v String, w int) Formatter
	bytes int : v.byteSize()
	chars int : v.charCount()
	// leading spaces for w > 0
	for p int : w-chars while p > 0 next p -: 1
		writer.write(` )
	.
	for x int : 0 while x < bytes next x +: 1
		writer.write(v.byteAt(x))
	.
	// trailing spaces for w < 0
	for p int : w+chars while p < 0 next p +: 1
		writer.write(` )
	.
	return this
.

/**	Format the string value {v}.
 *	@return the formatter for fluent chaining
 */ 
s(v String) Formatter
	bytes int : v.byteSize()
	for x int : 0 while x < bytes next x +: 1
		writer.write(v.byteAt(x))
	.
	return this
.

/**	Format the integer value {v} in decimal, with blank padding to a width of {w}.
 *	@return the formatter for fluent chaining
 */ 
d(v int, w int) Formatter
    chars int : 0
	if v < 0
		if v = -2147483648	// edge case for INTMIN, which would overflow if negated below
			return s('-2147483648',w)
		.
		writer.write(`-)
		chars +: 1
		v : - v
	.
	div int
	if v < 10  // common cases
		div : 1
		chars +: 1
	elsif v < 100
		div : 10
		chars +: 2
	else
		div : 1000000000 // largest int power of 10
		chars +: 10
		while div > v
			div /: 10
			chars -: 1
		.
	.
	// leading spaces for w > 0
	for p int : w-chars while p > 0 next p -: 1
		writer.write(` )
	.
	while div > 0
		writer.write((v / div + `0) as char)
		v %: div
		div /: 10
	.
	// trailing spaces for w < 0
	for p int : w+chars while p < 0 next p +: 1
		writer.write(` )
	.
	return this
.

/**	Format the integer value {v} in decimal.
 *	@return the formatter for fluent chaining
 */ 
d(v int) Formatter
	return d(v,0)
.

/**	Format the long value {v} in decimal, with blank padding to a width of {w}.
 *	@return the formatter for fluent chaining
 */ 
d(v long, w int) Formatter
    chars int : 0
	if v < 0
		if v = -9223372036854775808L	// edge case for LONGMIN, which would overflow if negated below
			return s('-9223372036854775808',w)
		.
		writer.write(`-)
		chars +: 1
		v : - v
	.
	div long
	if v < 10L  // common cases
		div : 1L
		chars +: 1
	elsif v < 100L
		div : 10L
		chars +: 2
	else
		div : 1000000000000000000L // largest long power of 10
		chars +: 19
		while div > v
			div /: 10L
			chars -: 1
		.
	.
	// leading spaces for w > 0
	for p int : w-chars while p > 0 next p -: 1
		writer.write(` )
	.
	while div > 0L
		writer.write((v / div + `0) as char)
		v %: div
		div /: 10L
	.
	// trailing spaces for w < 0
	for p int : w+chars while p < 0 next p +: 1
		writer.write(` )
	.
	return this
.

/**	Format the long value {v} in decimal.
 *	@return the formatter for fluent chaining
 */ 
d(v long) Formatter
	return d(v,0)
.

/**	Format the integer value {v} in hexadecimal, with blank padding to a width of {w}.
 *	@return the formatter for fluent chaining
 */ 
x(v int, w int) Formatter
	s int : 28
	x int : 0xF << s // expressing directly as 0xF0000000 fails in Java Integer.decode()
		// also equals -0x10000000
	while s > 0 && v & x = 0
		x >>>: 4
		s -: 4
	.
	chars int : (s >> 2) + 1
	// leading spaces for w > 0
	for p int : w-chars while p > 0 next p -: 1
		writer.write(` )
	.
	while s >= 0
		d int : (v & x) >>> s
		if d < 10
			writer.write((d + `0) as char)
		else
			writer.write((d + (`A - 10)) as char)
		.
		x >>>: 4
		s -: 4
	.
	// trailing spaces for w < 0
	for p int : w+chars while p < 0 next p +: 1
		writer.write(` )
	.
	return this
.

/**	Format the integer value {v} in hexadecimal.
 *	@return the formatter for fluent chaining
 */ 
x(v int) Formatter
	return x(v,0)
.

/**	Format the long value {v} in hexadecimal, with blank padding to a width of {w}.
 *	@return the formatter for fluent chaining
 */ 
x(v long, w int) Formatter
	s int : 60
	x long : 0xFL << s // expressing directly as 0xF000000000000000L fails in Java Long.decode()
		// also equals - 0x1000000000000000L
	while s > 0 && v & x = 0L
		x >>>: 4
		s -: 4
	.
	chars int : (s >> 2) + 1
	// leading spaces for w > 0
	for p int : w-chars while p > 0 next p -: 1
		writer.write(` )
	.
	while s >= 0
		d int : (v & x) >>> s as int
		if d < 10
			writer.write((d + `0) as char)
		else
			writer.write((d + (`A - 10)) as char)
		.
		x >>>: 4
		s -: 4
	.
	// trailing spaces for w < 0
	for p int : w+chars while p < 0 next p +: 1
		writer.write(` )
	.
	return this
.

/**	Format the long value {v} in hexadecimal.
 *	@return the formatter for fluent chaining
 */ 
x(v long) Formatter
	return x(v,0)
.

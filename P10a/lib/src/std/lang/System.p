/*	System.p
 
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
class std.lang.System
import std.io.FileWriter
import std.io.BufferedWriter
import std.io.Reader
import std.io.Writer
#standardInputRef static() int native
#standardOutputRef static() int native
#standardErrorRef static() int native
in static Reader
out static BufferedWriter : BufferedWriter(FileWriter(standardOutputRef()) as Writer)
err static BufferedWriter : BufferedWriter(FileWriter(standardErrorRef()) as Writer)
//TODO flush out and err in finalizer

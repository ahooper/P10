/*	FieldSymbol.java
 
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
package ca.nevdull.p10a.compiler;

import org.antlr.v4.runtime.Token;

public class FieldSymbol extends Symbol {
	private static int sequence = 0;
	private int seq = ++sequence;

	public FieldSymbol(String name) {
		super(name);
	}

	public FieldSymbol(Token name) {
		super(name);
	}

	public String getSeq() {
		return Integer.toHexString(seq);
	}

}

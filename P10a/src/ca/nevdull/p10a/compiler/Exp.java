/*	Exp.java
 
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
// Not currently used

package ca.nevdull.p10a.compiler;

import ca.nevdull.util.Str;

public class Exp extends Str {
	Type type;
	protected boolean constant;

	public boolean isConstant() {
		return constant;
	}

	public Exp(Type type) {
		super();
		this.type = type;
		this.constant = false;
	}

	public Exp(Type type, String string) {
		super(string);
		this.type = type;
		this.constant = false;
	}

	public Exp(Type type, String... strings) {
		super(strings);
		this.type = type;
		this.constant = false;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Exp newConstant(Type type, String string) {
		Exp e = new Exp(type, string);
		e.constant = true;
		return e;
	}

}

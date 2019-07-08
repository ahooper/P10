/*	ObjectType.java
 
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

public class ObjectType extends Type.Reference {
	protected ClassSymbol csym;

	public ObjectType(ClassSymbol csym) {
		super(csym.getName());
		this.csym = csym;
	}

	public ClassSymbol getKlass() {
		return csym;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectType other = (ObjectType) obj;
		if (csym == null) {
			if (other.csym != null)
				return false;
		} else if (!csym.equals(other.csym))
			return false;
		return true;
	}

	public boolean isSupertypeOf(Type t) {
		if (!(t instanceof ObjectType)) return false;
		ClassSymbol k = getKlass();
		for (ClassSymbol tk = ((ObjectType)t).getKlass(); tk != null; tk = tk.getSuperClass()) {
			if (tk == k) return true;
		}
		return false;
	}

}

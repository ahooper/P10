/*	Type.java
 
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

public class Type extends Symbol {

	public Type(String name) {
		super(name);
	}
	
	@Override
	public String toString() {
		return getName();
	}

	// Primitive types

	static class Primitive extends Type {
		public Primitive(String name) {
			super(name);
		}
	}
	
	static class Numeric extends Primitive {
		public Numeric(String name) {
			super(name);
		}
	}
	
	final static Type booleanType	= new Primitive("boolean");
	final static Type byteType		= new Numeric("byte");
	final static Type charType		= new Numeric("char");
	final static Type doubleType	= new Numeric("double");
	final static Type floatType		= new Numeric("float");
	final static Type intType		= new Numeric("int");
	final static Type longType		= new Numeric("long");
	final static Type shortType		= new Numeric("short");
	final static Type voidType		= new Type("void");
	final static Type errorType		= new Type("unknown");

    static Type widestNumericType(Type left, Type right) {
    	if (left == Type.errorType || right == Type.errorType) return Type.errorType;
    	// LATER unboxing
    	if (left == Type.doubleType || right == Type.doubleType) return Type.doubleType;
    	if (left == Type.floatType || right == Type.floatType) return Type.floatType;
    	if (left == Type.longType || right == Type.longType) return Type.longType;
    	return Type.intType;
    }

    // Reference types
    
	static class Reference extends Type {
		public Reference(String name) {
			super(name);
		}
	}
	
	final static Type nullType		= new Reference("null");
   
}

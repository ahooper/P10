/*	Symbol.java
 
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

public class Symbol {
	protected String name;
	protected Type type;
	protected Token token;
	protected Scope definingScope;
	protected int index;
	protected boolean isStatic;
	enum Access {
		ACCESS_PUBLIC("public"),
		ACCESS_PRIVATE("private");
		private final String shortString;
		Access(String shortString) { this.shortString = shortString; }
		public String shortString() { return shortString; }
	}
	protected Access access = Access.ACCESS_PUBLIC;
	protected String debugInfo;

	public Symbol(String name) {
		this.name = name;
	}

	public Symbol(Token name) {
		this(name.getText());
		this.token = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public Scope getDefiningScope() {
		return definingScope;
	}

	public void setDefiningScope(Scope definingScope) {
		this.definingScope = definingScope;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	public String getDebugInfo() {
		return debugInfo;
	}

	public void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}

	@Override
	public String toString() {
		return (access==Access.ACCESS_PRIVATE?"#":"")+getClass().getSimpleName()+" "+getName();
	}

}

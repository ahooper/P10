/*	Scope.java
 
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

import java.util.LinkedHashMap;

import org.antlr.v4.runtime.Token;

public class Scope extends Symbol {
	protected LinkedHashMap<String,Symbol> symbols;
	protected Scope enclosingScope;

	public Scope(String name, Scope enclosingScope) {
		super(name);
		this.symbols = new LinkedHashMap<String,Symbol>(10);
		this.enclosingScope = enclosingScope;
	}

	public Scope(Token name, Scope enclosingScope) {
		super(name);
		this.symbols = new LinkedHashMap<String,Symbol>(10);
		this.enclosingScope = enclosingScope;
	}
	
	public Scope getEnclosingScope() {
		return enclosingScope;
	}

	public void add(Symbol symbol) {
		Symbol prev = getMembers().get(symbol.getName());
		if (prev != null) {
			Main.error(symbol.getToken(),"multiple definition of "+symbol.getName());
			if (prev.getToken() != null) Main.note(prev.getToken(),"is previous definition");
		} else {
			put(symbol);
		}
	}

	public void put(Symbol symbol) {
		symbol.setDefiningScope(this);
		//Main.debug("%08X %s put %08X %s", ((Object)this).hashCode(), this.toString(), ((Object)symbol).hashCode(), symbol.toString());
		getMembers().put(symbol.getName(),symbol);
	}

	public void putAlias(Symbol symbol, String alias) {
		getMembers().put(alias,symbol);
	}

	public Symbol find(String name) {
		Symbol r = getMembers().get(name);
		if (r == null && enclosingScope != null) return enclosingScope.find(name);
		return r;
	}
	
	public Symbol find(Token name) {
		return find(name.getText());
	}
	
	public Scope close() {
		//Main.debug("end %s %s", name, symbols.keySet().toString());
		return enclosingScope;
	}

	public LinkedHashMap<String,Symbol> getMembers() {
		return symbols;
	}

}

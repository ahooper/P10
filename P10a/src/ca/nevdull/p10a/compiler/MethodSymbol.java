/*	MethodSymbol.java
 
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public class MethodSymbol extends Scope {
	static boolean trace = false;
	protected MethodSymbol overloads;
	protected FieldSymbol implicitParameter;

	public MethodSymbol(Token name, Scope enclosingScope) {
		super(name, enclosingScope);
	}
	
	public MethodSymbol(String name, Scope enclosingScope) {
		super(name, enclosingScope);
	}

	public Set<Map.Entry<String, Symbol>> getParameters() {
		return this.getMembers().entrySet();
	}
	
	public List<Type> getParameterTypes() {  // i.e. Signature
		Set<Map.Entry<String, Symbol>> parameters = getParameters();
		ArrayList<Type> paramTypes = new ArrayList<Type>(parameters.size());
		for (Map.Entry<String, Symbol> param : parameters) {
			paramTypes.add(param.getValue().getType());
		}
		return paramTypes;
	}
	
	public FieldSymbol getImplicitParameter() {
		return implicitParameter;
	}

	public void setImplicitParameter(FieldSymbol implicitParameter) {
		this.implicitParameter = implicitParameter;
	}

	public MethodSymbol resolveOverload(List<Type> types) {
		MethodSymbol exact = resolveOverload(types, /*subtyping*/false);
		if (exact != null) return exact;
		return resolveOverload(types, /*subtyping*/true);
	}

	public MethodSymbol resolveOverload(List<Type> types, boolean subtyping) {
		//TODO check access visibility
		//TODO compare multiple matches for specificity
		// Overloads are searched sequentially from the most recent.
		// The order is important because a subtype override signature will
		// appear again on the chain in the supertype.
		for (MethodSymbol over = this; over != null; over = over.overloads) {
			if (over.matchParameterTypes(types, subtyping)) {
				return over;
			}
		}
		return null;
	}

	private boolean matchParameterTypes(List<Type> types, boolean subtyping) {
		//if (trace) Main.debug(name+" matchParameterTypes "+types+" subtyping "+subtyping+":"+this);
		Iterator<Type> typeIter = types.iterator();
		Iterator<Entry<String, Symbol>> paramIter = getParameters().iterator();
		while (paramIter.hasNext()) {
			Map.Entry<String, Symbol> param = paramIter.next();
			Type pt = param.getValue().getType();
			////if (trace) Main.debug("matchParameterTypes "+pt+" "+typeIter.hasNext());
			if (!typeIter.hasNext()) return false;  // differing number of parameters
			Type t = typeIter.next();
			//if (trace) Main.debug("matchParameterTypes "+pt+":"+t);
			if (pt == t) {
				//if (trace) Main.debug("matchParameterTypes == "+pt+":"+t);
			} else if (pt.equals(t)) {
				//if (trace) Main.debug("matchParameterTypes equals "+pt+":"+t);
			} else if (subtyping && pt instanceof Type.Numeric && t instanceof Type.Numeric && Type.widestNumericType(pt, t) == pt) {
				//TODO should check both ways
				//if (trace) Main.debug("matchParameterTypes numeric "+pt+":"+t+" widest "+Type.widestNumericType(pt, t));
			} else if (subtyping && pt instanceof ObjectType && ((ObjectType)pt).isSupertypeOf(t)) {
				//if (trace) Main.debug("matchParameterTypes subtype "+pt+":"+t);
			//TODO other matching array cases
			} else if (t == Type.errorType) {
				//if (trace) Main.debug("matchParameterTypes error "+pt+":"+t);
			} else {
				//if (trace) Main.debug("matchParameterTypes mismatch "+pt+":"+t);
				return false;
			}
			////if (trace) Main.debug("matchParameterTypes match "+pt+":"+t);
		}
		//if (trace) Main.debug("matchParameterTypes remaining "+typeIter.hasNext());
		if (typeIter.hasNext()) return false;  // differing number of parameters
		//if (trace) Main.debug("matchParameterTypes match");
		return true;
	}

	public MethodSymbol getOverloads() {
		return overloads;
	}

	public void setOverloads(MethodSymbol over) {
		overloads = over;
	}

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder(getName());//(super.toString());
		if (isStatic()) r.append(" static ");
		r.append("(");
		String s = "";
		for (Type pt : getParameterTypes()) {
			r.append(s).append(pt.toString());
			s = ",";
		}
		r.append(")");
		if (overloads!=null) r.append(" | ").append(overloads.toString());
		return r.toString();
	}
	
}

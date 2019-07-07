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

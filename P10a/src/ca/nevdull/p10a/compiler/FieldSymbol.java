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

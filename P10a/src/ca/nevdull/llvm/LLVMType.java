package ca.nevdull.llvm;

import java.util.List;

public class LLVMType {
	private String spec;
	
	private LLVMType(String spec) {
		super();
		this.spec = spec;
	}
	
	public String toString() {
		return spec;
	}
	
	private static LLVMType[] intCache = new LLVMType[65];

	public static LLVMType integer(int bits) {
		assert bits > 0;
		LLVMType t = null;
		if (bits < intCache.length) t = intCache[bits];
		if (t != null) return t;
		if (bits >= intCache.length) {
			LLVMType[] expand = new LLVMType[bits];
			System.arraycopy(intCache, 0, expand, 0, intCache.length);
			intCache = expand;
		}
		t = new LLVMType("i"+Integer.toString(bits));
		intCache[bits] = t;
		return t;
	}
	
	public final static LLVMType VOID = new LLVMType("void");
	public final static LLVMType I1 = integer(1);
	public final static LLVMType I8 = integer(8);
	public final static LLVMType I16 = integer(16);
	public final static LLVMType I32 = integer(32);
	public final static LLVMType I64 = integer(64);
	public final static LLVMType BOOLEAN = integer(1);
	public final static LLVMType HALF = new LLVMType("half");
	public final static LLVMType FLOAT = new LLVMType("float");
	public final static LLVMType DOUBLE = new LLVMType("double");
	public final static LLVMType FP128 = new LLVMType("fp128");
	public final static LLVMType ANY_POINTER = pointer(I8);
	public final static LLVMType LABEL = new LLVMType("label");
	public final static LLVMType OPAQUE = new LLVMType("opaque");
	public final static LLVMType TOKEN = new LLVMType("token");
	// also metadata, processor specific
	
	public static LLVMType name(String name) {
		return new LLVMType(name);
	}	

	public static LLVMType array(LLVMType elements, int size) {
		return new LLVMType("["+Integer.toString(size)+" x "+elements.toString()+"]");
	}	
	
	public static LLVMType vector(LLVMType elements, int size) {
		return new LLVMType("<"+Integer.toString(size)+" x "+elements.toString()+">");
	}	
	
	public static LLVMType pointer(LLVMType refers) {
		return new LLVMType(refers.toString()+"*");
	}
	
	public static LLVMType structure(LLVMType... elements) {
		StringBuilder s = new StringBuilder();
		String sep = "{";
		for (LLVMType e : elements) { s.append(sep).append(e.spec); sep = ","; }
		s.append("}");
		return new LLVMType(s.toString());
	}
	
	public static LLVMType packed(LLVMType... elements) {
		StringBuilder s = new StringBuilder();
		String sep = "<{";
		for (LLVMType e : elements) { s.append(sep).append(e.spec); sep = ","; }
		s.append("}>");
		return new LLVMType(s.toString());
	}
	
	public static LLVMType function(LLVMType returns, LLVMType... arguments) {
		StringBuilder s = new StringBuilder();
		s.append(returns.toString());
		String sep = "(";
		for (LLVMType a : arguments) { s.append(sep).append(a.spec); sep = ","; }
		s.append(")");
		return new LLVMType(s.toString());
	}

	public static LLVMType function(LLVMType returns, List<LLVMType> arguments) {
		StringBuilder s = new StringBuilder();
		s.append(returns.toString());
		String sep = "(";
		for (LLVMType a : arguments) { s.append(sep).append(a.spec); sep = ","; }
		s.append(")");
		return new LLVMType(s.toString());
	}
	
}

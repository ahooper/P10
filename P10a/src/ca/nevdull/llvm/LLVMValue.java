/*	LLVMValue.java
 
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
package ca.nevdull.llvm;

public class LLVMValue {
	private LLVMType type;
	private String spec;
	
	protected LLVMValue(LLVMType type, String spec) {
		super();
		this.type = type;
		this.spec = spec;
	}
	
	public LLVMType getType() {
		return type;
	}
	
	public String toString() {
		return type.toString()+" "+spec;
	}
	
	public static LLVMValue integer(int bits, int value) {
		return new LLVMValue(LLVMType.integer(bits),Integer.toString(value));
	}
	
	public static LLVMValue integer(int bits, long value) {
		return new LLVMValue(LLVMType.integer(bits),Long.toString(value));
	}
	
	public static LLVMValue float1(float value) {
		return new LLVMValue(LLVMType.FLOAT,Float.toString(value));
	}
	
	public static LLVMValue float1(String value) {
		assert value.startsWith("0x");
		return new LLVMValue(LLVMType.FLOAT,value);
	}
	
	public static LLVMValue float2(double value) {
		return new LLVMValue(LLVMType.DOUBLE,Double.toString(value));
	}
	
	public static LLVMValue float2(String value) {
		assert value.startsWith("0x");
		return new LLVMValue(LLVMType.DOUBLE,value);
	}
	
	public static LLVMValue float4(String value) {
		assert value.startsWith("0x");
		return new LLVMValue(LLVMType.FP128,value);
	}

	// more esoteric floating point not yet implemented

	public static LLVMValue nullPtr(LLVMType type) {
		return new LLVMValue(type,"null");
	}

	public final static LLVMValue FALSE = new LLVMValue(LLVMType.BOOLEAN,"false");
	public final static LLVMValue TRUE = new LLVMValue(LLVMType.BOOLEAN,"true");
	public final static LLVMValue NONE = new LLVMValue(LLVMType.TOKEN,"none");
	public final static LLVMValue VOID = new LLVMValue(LLVMType.VOID,"");
	public final static LLVMValue NULL = new LLVMValue(LLVMType.ANY_POINTER,"null");
	
	public static LLVMValue structure(LLVMType type, LLVMValue...elements) {
		StringBuilder s = new StringBuilder();
		String sep = "{";
		for (LLVMValue e : elements) { s.append(sep).append(e.toString()); sep = ","; }
		s.append("}");
		return new LLVMValue(type, s.toString());
	}
	
	public static LLVMValue array(LLVMType type, LLVMValue...elements) {
		StringBuilder s = new StringBuilder();
		String sep = "[";
		for (LLVMValue e : elements) { s.append(sep).append(e.toString()); sep = ","; }
		s.append("]");
		return new LLVMValue(type, s.toString());
	}
	
	public static LLVMValue vector(LLVMType type, LLVMValue...elements) {
		StringBuilder s = new StringBuilder();
		String sep = "[";
		for (LLVMValue e : elements) { s.append(sep).append(e.toString()); sep = ","; }
		s.append("]");
		return new LLVMValue(type, s.toString());
	}
	
	public static LLVMValue zeros(LLVMType type) {
		return new LLVMValue(type, "zeroinitializer");
	}
	
	public final static LLVMValue UNDEF = new LLVMValue(null,null) {
		public String toString() { return "undef"; }
	};
	// global addresses
	// poison
	
	public static LLVMValue convert(String op, LLVMValue value, LLVMType result) {
		return new LLVMValue(result, op+"("+value.toString()+" to "+result.toString());
	}
	public final static String TRUNC = "trunc";
	public final static String ZEXT = "zext";
	public final static String SEXT = "sext";
	public final static String FPTRUNC = "fptrunc";
	public final static String FPEXT = "fpext";
	public final static String FPTOUI = "fptoui";
	public final static String FPTOSI = "fptosi";
	public final static String UITOFP = "uitofp";
	public final static String SITOFP = "sitofp";
	public final static String PRTTOINT = "ptrtoint";
	public final static String INTTOPTR = "inttoptr";
	public final static String BITCAST = "bitcast";
	public final static String ADDRSPACECAST = "addrspacecast";

	public static LLVMValue dyadic(LLVMType result, String op, LLVMValue value1, LLVMValue value2) {
		return new LLVMValue(result, op+"("+value1.toString()+","+value2.toString()+")");
	}

	public static LLVMValue named(LLVMType type, String name) {
		return new LLVMValue(type, name);
	}
	
	/*
	getelementptr (TY, CSTPTR, IDX0, IDX1, ...), getelementptr inbounds (TY, CSTPTR, IDX0, IDX1, ...)
	select (COND, VAL1, VAL2)
	extractelement (VAL, IDX)
	insertelement (VAL, ELT, IDX)
	shufflevector (VEC1, VEC2, IDXMASK)
	extractvalue (VAL, IDX0, IDX1, ...)
	insertvalue (VAL, ELT, IDX0, IDX1, ...)
	*/
	// assembler
	// metadata
}

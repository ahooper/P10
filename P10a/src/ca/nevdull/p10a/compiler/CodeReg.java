package ca.nevdull.p10a.compiler;

import java.util.HashMap;
import java.util.List;

// LLVM Values and Types
public class CodeReg {
	protected Type type;
	protected boolean constant;
	protected boolean pointer;
	protected String regType;
	protected String name;
	
	public Type getType() {
		return type;
	}

	public boolean isConstant() {
		return constant;
	}

	public boolean isPointer() {
		return pointer;
	}

	public String getRegType() {
		if (isPointer()) return regType+"*";
		else return regType;
	}

	public String getPointedType() {
		if (!isPointer()) throw new IllegalArgumentException("must be a pointer value");
		return regType;
	}

	public String getName() {
		return name;
	}

	public String typeAndName() {
		return getRegType()+" "+getName();
	}
	
	// Generated intermediate names

	private static final String REG_PREFIX = "%r";
	
	private static int sequence = 0;

	private static String nextSequence() {
		return REG_PREFIX+Integer.toHexString(++sequence);
	}

	// Type mapping
	
	final static String LLVM_DOUBLE = "double";
	final static String LLVM_FLOAT = "float";
	final static String LLVM_I1 = "i1";
	final static String LLVM_I8 = "i8";
	final static String LLVM_I16 = "i16";
	final static String LLVM_I32 = "i32";
	final static String LLVM_I64 = "i64";
	final static String LLVM_VOID = "void";
	final static String LLVM_ANY_POINTER = "i8*";
	static final String LLVM_TOKEN = "token";

	private static HashMap<Type,String> llvmTypeMap = new HashMap<Type,String>();
	{
		// DebugInfo constructor must correspond to this mapping
		llvmTypeMap.put(Type.booleanType, LLVM_I1);
		llvmTypeMap.put(Type.byteType, LLVM_I8);
		llvmTypeMap.put(Type.charType, LLVM_I32);
		llvmTypeMap.put(Type.doubleType, LLVM_DOUBLE);
		llvmTypeMap.put(Type.floatType, LLVM_FLOAT);
		llvmTypeMap.put(Type.intType, LLVM_I32);
		llvmTypeMap.put(Type.longType, LLVM_I64);
		llvmTypeMap.put(Type.shortType, LLVM_I16);
		llvmTypeMap.put(Type.nullType, LLVM_ANY_POINTER);
		llvmTypeMap.put(Type.voidType, LLVM_VOID);
		llvmTypeMap.put(Type.errorType, LLVM_TOKEN);
	}
	
	static String llvmArrayType(ArrayType type, int size) {
		return "{"+CodeGen.ARRAY_MTAB_TYPE+"*, i32, ["+size+" x "+llvmType(type.getElement())+"]}";
	}
	
	static String llvmArrayType(ArrayType type) {
		return llvmArrayType(type, 0);
	}

	static String llvmObjType(ObjectType type) {
		ClassSymbol csym = type.getKlass();
		return CodePass.classPrefix(CodeGen.OBJ_TYPE_PREFIX,csym);
	}

	static String llvmArrayPtrType(ArrayType type) {
		return llvmArrayType(type)+"*";
	}

	static String llvmObjPtrType(ObjectType type) {
		return llvmObjType(type)+"*";
	}
	
	static String llvmType(Type type) {
		String t = llvmTypeMap.get(type);
		if (t != null) return t;
		if (type == null) {
			return "null_type";
		} else if (type instanceof ArrayType) {
			t = llvmArrayPtrType((ArrayType)type);
		} else if (type instanceof ObjectType) {
			t = llvmObjPtrType((ObjectType)type);
		} else {
			Main.debug("llvmType unknown %s", type.toString());
			t = "error_type:"+type.toString();
		}
		llvmTypeMap.put(type,t);
		return t;
	}

	static String llvmArgumentTypes(MethodSymbol msym) {
		StringBuilder r = new StringBuilder("(");
		String sep = "";
		FieldSymbol ip = msym.getImplicitParameter();
		if (ip != null) {
			String ipt = llvmType(ip.getType());
			r.append(sep).append(ipt);
			sep = ",";
		}
		List<Type> pl = msym.getParameterTypes();
		for (Type p : pl) {
		    String part = llvmType(p);
			r.append(sep).append(part);
			sep = ",";
		}
		r.append(")");
		return r.toString();
	}
	
	static String llvmMethodPtrType(MethodSymbol msym) {
		return llvmType(msym.getType())+llvmArgumentTypes(msym)+"*";
	}

	private CodeReg() {
	}

	private CodeReg(Type type, boolean constant, boolean pointer, String regType, String name) {
		super();
		this.type = type;
		this.constant = constant;
		this.pointer = pointer;
		this.regType = regType;
		this.name = name;
	}

	private CodeReg(Type type, boolean constant, boolean pointer, String name) {
		super();
		this.type = type;
		this.constant = constant;
		this.pointer = pointer;
		this.regType = llvmType(type);
		this.name = name;
	}
	
	public static CodeReg newValue(Type type) {
		return new CodeReg(type, false, false, nextSequence());
	}

	public static CodeReg newConstant(Type type, String llcValue) {
		return new CodeReg(type, true, false, llcValue);
	}
	
	static CodeReg VOID = newConstant(Type.voidType, "");
	static CodeReg TRUE = newConstant(Type.booleanType, "true");
	static CodeReg FALSE = newConstant(Type.booleanType, "false");
	static CodeReg ZERO = newConstant(Type.intType, "0");
	static CodeReg ONE = newConstant(Type.intType, "1");
	static CodeReg TWO = newConstant(Type.intType, "2");
	static CodeReg MINUS_ONE = newConstant(Type.intType, "-1");
	static CodeReg LONG_ZERO = newConstant(Type.longType, "0");
	static CodeReg FLOAT_ZERO = newConstant(Type.floatType, "0.0");
	static CodeReg DOUBLE_ZERO = newConstant(Type.doubleType, "0.0");
	static CodeReg NULL = newConstant(Type.nullType, "null");
	static CodeReg ERROR_VALUE = newConstant(Type.errorType, "ERROR_VALUE");

	public static CodeReg newName(Type type, String name) {
		return new CodeReg(type, false, false, name);
	}

	public static CodeReg newPointer(Type type, String name) {
		return new CodeReg(type, false, true, name);
	}

	public static CodeReg newPointer(Type type) {
		return new CodeReg(type, false, true, nextSequence());
	}

	public static CodeReg newInternal(String regType) {
		return new CodeReg(NOT_A_TYPE, false, false, regType, nextSequence());
	}
	
	private static final Type NOT_A_TYPE = new Type("Ceci n'est pas une type.");
	
	public static CodeReg newLabel(String label) {
		return new CodeReg(NOT_A_TYPE, true, false, "label", label);
	}

	public static CodeReg newStructureLiteral(List<CodeReg> elements) {
		StringBuilder s = new StringBuilder("{"),
					  t = new StringBuilder("{");
		String sep = "";
		for (CodeReg e : elements) {
			s.append(sep).append(e.typeAndName());
			t.append(sep).append(e.getRegType());
			sep = ",";
		}
		s.append("}");
		t.append("}");
		return new CodeReg(NOT_A_TYPE, true, false, t.toString(), s.toString());
	}
	
	public static CodeReg newArrayLiteral(List<CodeReg> elements) {
		StringBuilder s = new StringBuilder("[");
		String sep = "";
		for (CodeReg e : elements) {
			s.append(sep).append(e.getRegType());
			sep = ",";
		}
		s.append("]");
		String t = "["+Integer.toString(elements.size())+" x "+elements.get(0).getRegType()+"]";
		return new CodeReg(NOT_A_TYPE, true, false, t, s.toString());
	}
	
	public static CodeReg convertConst(Type type, String op, CodeReg value) {
		assert value.isConstant();
		String regType = llvmType(type);
		if (regType.equals(value.getRegType())) return value;  // no conversion necessary (char to int)
		return new CodeReg(type, true, false, regType, op+"("+value.typeAndName()+" to "+regType+")");
	}

	public static CodeReg dyadicConst(Type type, CodeReg value1, String op, CodeReg value2) {
		assert value1.isConstant() & value2.isConstant();
		String regType = llvmType(type);
		return new CodeReg(type, true, false, regType, op+"("+value1.typeAndName()+","+value2.typeAndName()+")");
	}

	public static CodeReg newMethodRef(String name, MethodSymbol mres) {
		String funt = llvmMethodPtrType(mres);
	    return new CodeReg(mres.getType(), false, false, funt, name);
	}
/*
	public static CodeReg newStacksave() {
		return new CodeReg(NOT_A_TYPE, false, true, LLVM_ANY_POINTER, nextSequence());
	}
*/
}

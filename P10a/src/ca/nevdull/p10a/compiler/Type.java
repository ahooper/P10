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

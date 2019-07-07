package ca.nevdull.p10a.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CIncludeGen {
	private boolean trace;
	private PrintWriter co;
	private String guardName;
	private static final String CEE_SUFFIX = ".h";
	private static final String MTABLE_TYPE_PREFIX = "mtab_";
	private static final String MTABLE_PREFIX	= "MTab_";
	private static final String CLASS_DATA_PREFIX = "Class_";
	        static final String OBJ_TYPE_PREFIX	= "obj_";
   	private static final String ARRAY_TYPE_PREFIX = "array_";
	private static final String STD_LANG		= "std_lang_";
    private static final String CLASS_CLASS		= STD_LANG+"Class";
    private static final String OBJECT_CLASS	= STD_LANG+"Object";
    private static final String ARRAY_CLASS		= STD_LANG+"Array";
    private static final String STRING_CLASS	= STD_LANG+"String";
            static final String THROW_CLASS		= STD_LANG+"Throw";
	private static final String OBJECT_OBJ		= OBJ_TYPE_PREFIX+OBJECT_CLASS;
	private static final String CLASS_OBJ		= OBJ_TYPE_PREFIX+CLASS_CLASS;
	private static final String ARRAY_OBJ		= OBJ_TYPE_PREFIX+ARRAY_CLASS;
	private static final String STRING_OBJ		= OBJ_TYPE_PREFIX+STRING_CLASS;
	private static final String CLASS_MTAB		= MTABLE_PREFIX+CLASS_CLASS;
	private static final String CLASS_MTAB_TYPE	= MTABLE_TYPE_PREFIX+CLASS_CLASS;
	private static final String STRING_MTAB		= MTABLE_PREFIX+STRING_CLASS;
	private static final String STRING_MTAB_TYPE= MTABLE_TYPE_PREFIX+STRING_CLASS+"*";
	private static final String ARRAY_MTAB		= MTABLE_PREFIX+ARRAY_CLASS;
	        static final String ARRAY_MTAB_TYPE	= MTABLE_TYPE_PREFIX+ARRAY_CLASS+"*";

	public CIncludeGen(String outputDirectory, String baseFileName, String sourceFileName) throws FileNotFoundException, UnsupportedEncodingException {
		File file = new File(outputDirectory, baseFileName+CEE_SUFFIX);
		co = new PrintWriter(file, "UTF-8");
		guardName = CodePass.escapeName(sourceFileName.replace('/','.')).toUpperCase()+"_H";
		emit("#ifndef ",guardName);
		emit("#define ",guardName);
		emit("// source_filename = \"",
				sourceFileName.replace("\"","\\22"),
				"\" ; ",
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		emit("// referenced object types");
	}
	
	private void emit(String... args) {
		emitn(args);
		co.println();
		co.flush();
	}

	private void emitn(String... args) {
		for (String a : args) co.print(a);
	}

	public void close() {
		emit("#endif /* ",guardName," */");
		co.close();
	}
	
	void emitClassStructs(ClassSymbol csym) {
		emit("// class structures");
		// virtual method table
		MethodSymbol[] vMethods = csym.getVirtualMethods();
		String mtt = CodePass.classPrefix(MTABLE_TYPE_PREFIX,csym);
		emit("struct "+mtt+" {");
		emit("    "+CLASS_OBJ+"* _class;");
		for (MethodSymbol m : vMethods) {
	        String name = methodName(m);
			String access = m.getAccess().shortString();
			emit("    ",type(m.getType())," (*",name,")",argumentTypes(m),"; //",Integer.toString(m.getIndex())," ",access," ",m.getName(),m.getParameterTypes().toString());
		}
		emit("};");
		String obj = CodePass.classPrefix(OBJ_TYPE_PREFIX,csym);
		emit("struct ",obj," {");
		emit("    struct "+mtt+"* _mtt;");
		// instance fields
		FieldSymbol[] iFields = csym.getInstanceFields();
		for (FieldSymbol f : iFields) {
	        String name = CodePass.escapeName(f.getName());
			String access = f.getAccess().shortString();
			emit("    ",type(f.getType())," ",name,"; //",Integer.toString(f.getIndex())," ",access," ",f.getName());
		}
		emit("};");
		emit("typedef struct {");
		emit("    struct mtab_std_lang_Array* _mtt;");
		emit("    int32_t length;");
		emit("    ",obj,"* elements[0];");
		emit("} ", CodePass.classPrefix(ARRAY_TYPE_PREFIX+OBJ_TYPE_PREFIX,csym),";");
		
		String glb = CodePass.classPrefix("",csym)+CodePass.NAME_SEPARATOR;
		Collection<Symbol> members = csym.getMembers().values();
		emit("// static fields");
		for (Symbol member : members) {
			if (member instanceof FieldSymbol) {
				FieldSymbol f = (FieldSymbol)member;
				if (f.isStatic()) {
			        Type vtype = f.getType();
			        String varn = f.getName();
					emit("extern ",type(vtype)," ",glb,varn,";");
				}
			}
		}
		emit("// static methods");
		for (Symbol member : members) {
			if (member instanceof MethodSymbol) {
				for (MethodSymbol m = (MethodSymbol)member; m != null; m = m.getOverloads()) {
    				if (m.isStatic()) {
	    				Type rtype = m.getType();
	    		        String fName = methodName(m);
	    		        emit("extern ",type(rtype)," ",glb,fName,argumentTypes(m),";");
    				}
				}
			}
		}
        emit("extern void ",glb,CodePass.CLASS_INITIALIZE,"();");
		emit("// referenced classes");
	}

	public void emitNativeMethod(MethodSymbol msym) {
		if (msym.isStatic()) return; // already declared by emitClassStructs
    	ClassSymbol csym = (ClassSymbol)msym.getDefiningScope();
		String glb = CodePass.classPrefix("",csym)+CodePass.NAME_SEPARATOR;
		Type rtype = msym.getType();
        String fName = methodName(msym);
        emit("extern ",type(rtype)," ",glb,fName,argumentTypes(msym),";");
	}
	
    static String methodName(MethodSymbol msym) {
     	StringBuilder n = new StringBuilder();
     	n.append(CodePass.escapeName(msym.getName()));
     	for (Type pt : msym.getParameterTypes()) {
     		n.append(CodePass.PARAMETER_SEPARATOR);
 			appendParameterType(n,pt);
       	}
		return n.toString();
	}

	private static void appendParameterType(StringBuilder n, Type pt) {
 		if (pt instanceof ArrayType) {
 			appendParameterType(n,((ArrayType) pt).getElement());
 			n.append(CodePass.ARRAY_PARAMETER);
 		} else {
 			n.append(CodePass.escapeName(pt.getName()));
 		}
	}

	public void emitClassType(ClassSymbol csym) {
		String obj = CodePass.classPrefix(OBJ_TYPE_PREFIX,csym);
		emit("typedef struct ",obj," ",obj,";");
	}

	public void emitInclude(ClassSymbol csym) {
		emit("#include \"", csym.getFullName().replace('.', '/'), CEE_SUFFIX, "\"");
	}
	
	// Type mapping
	
	final static String C_DOUBLE = "double";
	final static String C_FLOAT = "float";
	final static String C_I1 = "int1_t";  //TODO
	final static String C_I8 = "int8_t";
	final static String C_I16 = "int16_t";
	final static String C_I32 = "int32_t";
	final static String C_I64 = "int64_t";
	final static String C_VOID = "void";
	final static String C_ANY_POINTER = "void*";
	final static String C_TOKEN = "UNKNOWN";

	private static HashMap<Type,String> typeMap = new HashMap<Type,String>();
	{
		typeMap.put(Type.booleanType, C_I1);
		typeMap.put(Type.byteType, C_I8);
		typeMap.put(Type.charType, C_I32);
		typeMap.put(Type.doubleType, C_DOUBLE);
		typeMap.put(Type.floatType, C_FLOAT);
		typeMap.put(Type.intType, C_I32);
		typeMap.put(Type.longType, C_I64);
		typeMap.put(Type.shortType, C_I16);
		typeMap.put(Type.nullType, C_ANY_POINTER);
		typeMap.put(Type.voidType, C_VOID);
		typeMap.put(Type.errorType, C_TOKEN);
	}
	
	static String arrayType(ArrayType type) {
		Type elt = type.getElement();
		if (elt instanceof ObjectType) {
//			ClassSymbol csym = ((ObjectType)elt).getKlass();
//			return CodePass.classPrefix(ARRAY_TYPE_PREFIX,csym);
			return ARRAY_TYPE_PREFIX+objType((ObjectType)elt);
		} else {
			return ARRAY_TYPE_PREFIX+type(elt);
		}
	}

	static String objType(ObjectType type) {
		ClassSymbol csym = type.getKlass();
		return CodePass.classPrefix(OBJ_TYPE_PREFIX,csym);
	}

	static String arrayPtrType(ArrayType type) {
		return arrayType(type)+"*";
	}

	static String objPtrType(ObjectType type) {
		return objType(type)+"*";
	}
	
	static String type(Type type) {
		String t = typeMap.get(type);
		if (t != null) return t;
		if (type == null) {
			return "null_type";
		} else if (type instanceof ArrayType) {
			t = arrayPtrType((ArrayType)type);
		} else if (type instanceof ObjectType) {
			t = objPtrType((ObjectType)type);
		} else {
			Main.debug("type unknown %s", type.toString());
			t = "error_type:"+type.toString();
		}
		typeMap.put(type,t);
		return t;
	}

	static String argumentTypes(MethodSymbol msym) {
		StringBuilder r = new StringBuilder("(");
		String sep = "";
		FieldSymbol ip = msym.getImplicitParameter();
		if (ip != null) {
			String ipt = type(ip.getType());
			r.append(sep).append(ipt);
			sep = ",";
		}
		List<Type> pl = msym.getParameterTypes();
		for (Type p : pl) {
		    String part = type(p);
			r.append(sep).append(part);
			sep = ",";
		}
		r.append(")");
		return r.toString();
	}

}

/*	DebugInfo.java
 
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
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.Token;

public class DebugInfo {
	
	CodeGen code;
	private HashMap<Type,Integer> diTypeID = new HashMap<Type,Integer>();
	private HashMap<ClassSymbol,Integer> diClassID = new HashMap<ClassSymbol,Integer>();
	private HashMap<String,Integer> diFileID = new HashMap<String,Integer>();

	public DebugInfo(CodeGen code) {
		this.code = code;
		// Must include the primitive types defined in CodeReg llvmTypeMap
		diTypeID.put(Type.booleanType, code.emitMetadata("!DIBasicType(name: \"boolean\", size: 8, align: 8, encoding: DW_ATE_boolean)"));
		diTypeID.put(Type.byteType, code.emitMetadata("!DIBasicType(name: \"byte\", size: 8, align: 8, encoding: DW_ATE_signed)"));
		diTypeID.put(Type.charType, code.emitMetadata("!DIBasicType(name: \"char\", size: 16, align: 16, encoding: DW_ATE_unsigned_char)"));
		diTypeID.put(Type.doubleType, code.emitMetadata("!DIBasicType(name: \"double\", size: 64, align: 64, encoding: DW_ATE_float)"));
		diTypeID.put(Type.floatType, code.emitMetadata("!DIBasicType(name: \"float\", size: 32, align: 32, encoding: DW_ATE_float)"));
		diTypeID.put(Type.intType, code.emitMetadata("!DIBasicType(name: \"int\", size: 32, align: 32, encoding: DW_ATE_signed)"));
		diTypeID.put(Type.longType, code.emitMetadata("!DIBasicType(name: \"long\", size: 64, align: 64, encoding: DW_ATE_signed)"));
		diTypeID.put(Type.shortType, code.emitMetadata("!DIBasicType(name: \"short\", size: 16, align: 16, encoding: DW_ATE_signed)"));
		diTypeID.put(Type.nullType, code.emitMetadata("!DIBasicType(name: \"null\", size: 64, align: 64, encoding: DW_ATE_address)"));
		diTypeID.put(Type.voidType, code.emitMetadata("null"));
	}

	private static Integer newID() {
		return CodeGen.getNextMetadataID();
	}

	public Integer emitCompileUnit(Integer diFile, Scope globals) {
		return code.emitMetadata(
				"distinct !DICompileUnit(",
				"language: DW_LANG_C99", // closest approximation
				", file: !", diFile.toString(),
				", producer: ", quote(Main.VERSION_ID),
				", isOptimized: false",
				", runtimeVersion: 0",
				//", emissionKind: FullDebug",
				//", enums: !TODO",
				", globals: !TODO"); //TODO
	}

	Integer emitFile(String sourceFilePath, String sourceFileName, String sourceFileMD5) {
		Integer id = diFileID.get(sourceFileName);
		if (id != null) return id;
		ArrayList<String> sa = new ArrayList<String>(10);
		sa.add("!DIFile(filename: "); sa.add(quote(sourceFileName));
		if (sourceFilePath!=null) {
			sa.add(", directory: "); sa.add(quote(sourceFilePath));
		}
		if (sourceFileMD5!=null) {
			sa.add(", checksumkind: CSK_MD5, checksum: \""); sa.add(sourceFileMD5); sa.add("\"");
		}
		sa.add(")");
		id = code.emitMetadata(id, sa.toArray(new String[sa.size()]));
		diFileID.put(sourceFileName, id);
		return id;
	}

	private Integer emitFile(Token token) {
		return emitFile(null, token.getInputStream().getSourceName(), null);
	}

	private String diLine(Token token) {
		return Integer.toString(token.getLine());
	}
	
	String diArrayType(ArrayType type, int size) {
		return "!{"+/*TODO*/CodeGen.ARRAY_MTAB_TYPE+"*, i32, ["+size+" x "+emitType(type.getElement())+"]}";
	}
	
	String diArrayType(ArrayType type) {
		return diArrayType(type, 0);
	}

	Integer emitClassType(ClassSymbol csym) {
		Integer id = diClassID.get(csym);
		if (id != null) return id;
		id = newID();
		diClassID.put(csym, id); // must go in map before emitting elements, in case of recursive reference
		StringBuilder elements = new StringBuilder();  String sep = "!";
		final FieldSymbol[] iFields = csym.getInstanceFields();
		for (FieldSymbol f : iFields) {
			Integer dt = emitField(id, f);
			elements.append(sep).append(dt);  sep = ", !";
		}
		final String objt = CodePass.classPrefix(CodeGen.OBJ_TYPE_PREFIX,csym);
		String cname = csym.getFullName();
		Token ctoken = csym.getToken();
		return code.emitMetadata(id,
				"!DICompositeType(",
				"tag: DW_TAG_class_type",
				", name: ", quote(cname),
				", file: !", emitFile(ctoken).toString(),
				", line: ", diLine(ctoken),
				// sizeof from http://nondot.org/sabre/LLVMNotes/SizeOf-OffsetOf-VariableSizedStructs.txt
				// TODO not sure if calculated values are allowed in metadata
				", size: ptrtoint(",objt,"* getelementptr(",objt,", ",objt,"* null, i32 1) to i32)",
				", align: 64",
				//", identifier: \"_M4Enum\"", //TODO
				", elements: !{", elements.toString(),"})"
				);
	}

	private Integer emitField(Integer scopeID, FieldSymbol f) {
		Integer bt = emitType(f.getType());
		String fname = f.getName();
		Token ftoken = f.getToken();
		Integer dt = code.emitMetadata(
				"!DIDerivedType(",
				"tag: DW_TAG_member",
				", name: ", quote(fname),
				", scope: ", scopeID.toString(),
				", file: !", emitFile(ftoken).toString(),
				", line: ", diLine(ftoken),
				", baseType: !", bt.toString(),
				", size: TODO", //TODO
				", align: 32)"); //TODO
		return dt;
	}
	
	Integer emitType(Type type) {
		Integer id = diTypeID.get(type);
		if (id != null) return id;
		// primitive types should all be found, since the constructor defined them
		String t;
		if (type == null) {
			t = "null_type";
		} else if (type instanceof ArrayType) {
			t = diArrayType((ArrayType)type)+"*";
		} else if (type instanceof ObjectType) {
			t = emitClassType(((ObjectType)type).getKlass())+"*";
		} else {
			Main.debug("diIDType unknown %s", type.toString());
			t = "error_type:"+type.toString();
		}
		id = code.emitMetadata(t);
		diTypeID.put(type,id);
		return id;
	}
	
	String emitMethodPtrType(MethodSymbol msym) {
		StringBuilder paramTypes = new StringBuilder("(");
		String sep = "!";
		FieldSymbol ip = msym.getImplicitParameter();
		if (ip != null) {
			Integer ipt = emitType(ip.getType());
			paramTypes.append(sep).append(ipt);
			sep = ",!";
		}
		List<Type> pl = msym.getParameterTypes();
		for (Type p : pl) {
		    Integer part = emitType(p);
			paramTypes.append(sep).append(part);
			sep = ",!";
		}
		paramTypes.append(")");
		return emitType(msym.getType())+paramTypes.toString()+"*";
	}

	public Integer emitSubroutineType(MethodSymbol msym) {
        StringBuilder subrTypes = new StringBuilder();
        Type rtype = msym.getType();
        if (rtype == Type.voidType) subrTypes.append("null");
        else subrTypes.append(emitType(rtype));
		FieldSymbol ip = msym.getImplicitParameter();
		if (ip != null) {
		    Type ptype = ip.getType();
			subrTypes.append(",").append(emitType(ptype));
		}
		List<Type> pl = msym.getParameterTypes();
		for (Type p : pl) {
			subrTypes.append(",").append(emitType(p.type));
        }
        return code.emitMetadata(
        		"!DISubroutineType(types: !{", subrTypes.toString(), "})"
        		);
	}

	public Integer emitSubprogram(MethodSymbol msym) {
		String diclass = emitClassType((ClassSymbol)msym.getDefiningScope()).toString();
		String mname = msym.getName();
		Token mtoken = msym.getToken();
		return code.emitMetadata(
				"distinct !DISubprogram(",
				"name: ", quote(mname),
				", file: !", emitFile(mtoken).toString(),
				", line: ", diLine(mtoken),
				//", linkageName: ",escapedString("_"+fName),
				", scope: ", diclass,
				", type: !", emitSubroutineType(msym).toString(),
				", isLocal: true,",
				", isDefinition: true",
				", scopeLine: TODO", //TODO
				", containingType: ", diclass,
				", virtuality: DW_VIRTUALITY_pure_virtual", //TODO
				", virtualIndex: TODO",  //TODO
				//", flags: DIFlagPrototyped",
				//", isOptimized: true", 
				", unit: !TODO",  //TODO
				//", templateParams: !6",
				", declaration: !TODO",  //TODO
				", variables: !TODO",  //TODO
				//", thrownTypes: !TODO",
				")");
	}

	public static String quote(String str) {
    	int len = str.length();
    	int x = 0;
    	for ( ; x < len; x++) {
    		char c = str.charAt(x);
    		if (c >= ' ' && c <= '~' && c != '"') { 
    		} else break;  // hit a character that requires escaping
    	}
    	if (x == len) {
    		// no escapes needed
    		return "\""+str+'"';
    	}
    	StringBuilder escaped = new StringBuilder(str.length()*2);
    	escaped.append("\"").append(str.substring(0, x));
    	for ( ; x < len; x++) {
    		char c = str.charAt(x);
    		if (c >= ' ' && c <= '~' && c != '"') { 
    			escaped.append(c);
    		} else if (c < 0x80) {
    			addEscaped(escaped, (byte)c);
   			} else if (c < 0x800) { // encode to UTF-8 (LLVM LangRef does not specify encoding)
   				addEscaped(escaped, (byte)( (c >>  6)         | 0xC0));
   				addEscaped(escaped, (byte)(( c        & 0x3F) | 0x80));
			} else if (c < Character.MIN_HIGH_SURROGATE/*0xD800*/ || c > Character.MAX_LOW_SURROGATE/*0xDFFF*/) {
				addEscaped(escaped, (byte)( (c >> 12)         | 0xE0));
				addEscaped(escaped, (byte)(((c >>  6) & 0x3F) | 0x80));
				addEscaped(escaped, (byte)(( c        & 0x3F) | 0x80));
    		} else if (c < Character.MIN_LOW_SURROGATE/*0xDC00*/) {
    			// surrogate high
    			int d = str.codePointAt(x);  x++;
    			addEscaped(escaped, (byte)( (d >> 18)         | 0xF0));
    			addEscaped(escaped, (byte)(((d >> 12) & 0x3F) | 0x80));
    			addEscaped(escaped, (byte)(((d >>  6) & 0x3F) | 0x80));
    			addEscaped(escaped, (byte)(( d        & 0x3F) | 0x80));
    		} else {
    			throw new IllegalArgumentException("Invalid low surrogate code unit");
    	    }
    	}
    	return escaped.append('"').toString();
	}
	
	private static void addEscaped(StringBuilder escaped, byte b) {
		escaped.append('\\')
			   .append(Character.forDigit((b >> 4)&0xF,16))
			   .append(Character.forDigit( b      &0xF,16));
	}

}

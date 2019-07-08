/*	CodeGen.java
 
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

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

import ca.nevdull.util.UTF8Bytes;

public class CodeGen {
	private Writer code;
	private File codeFile;
	private static final String LLVM_SUFFIX		= ".ll";
	        static final String LOCAL_PREFIX	= "%";
	        static final String GLOBAL_PREFIX	= "@";
	private static final String MTABLE_TYPE_PREFIX = "%mtab_";
	private static final String MTABLE_PREFIX	= "@MTab_";
            static final String CLASS_DATA_PREFIX = "@Class_";
	        static final String OBJ_TYPE_PREFIX	= "%obj_";
   	private static final String STRING_VALUE_PREFIX = "@v_";
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
	private static final String OBJECT_MTAB_TYPE= MTABLE_TYPE_PREFIX+OBJECT_CLASS;
	private static final String CLASS_NEW_INSTANCE = "@"+CLASS_CLASS+"_newInstance";
	private static final String CLASS_MTAB		= MTABLE_PREFIX+CLASS_CLASS;
	private static final String CLASS_MTAB_TYPE	= MTABLE_TYPE_PREFIX+CLASS_CLASS;
	private static final String STRING_MTAB		= MTABLE_PREFIX+STRING_CLASS;
	private static final String STRING_MTAB_TYPE= MTABLE_TYPE_PREFIX+STRING_CLASS;
	private static final String ARRAY_MTAB		= MTABLE_PREFIX+ARRAY_CLASS;
	        static final String ARRAY_MTAB_TYPE	= MTABLE_TYPE_PREFIX+ARRAY_CLASS;
	private static final String THROW_ARRAY_INDEX_FAULT		= "@"+THROW_CLASS+"_arrayIndexFault_Pint";
	private static final String THROW_NULL_POINTER_FAULT	= "@"+THROW_CLASS+"_nullPointerFault";
	        static final String THROW_SWITCH_INDEX_FAULT	= "@"+THROW_CLASS+"_switchIndexFault_Pint";
	private static final String GLOBAL_XTORS_TYPE = "%Xtors";
	        static final int VIRTUAL_METHOD_OFFSET = 1;
	        static final int INSTANCE_FIELD_OFFSET = 1;
	        static final String lineSep = System.lineSeparator();

	public CodeGen(String outputDirectory, String baseFileName, String sourceFileName) throws FileNotFoundException, UnsupportedEncodingException {
		codeFile = new File(outputDirectory, baseFileName+LLVM_SUFFIX);
		code = new PrintWriter(codeFile, "UTF-8");
		emit("source_filename = \"",sourceFileName.replace("\"","\\22"),"\""," ; ",LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		emit(GLOBAL_XTORS_TYPE," = type { i32, void ()*, i8* }");
	}
	
	// not currently used
    static String quoteName(String name) {
    	int len = name.length();
    	int x = 0;
    	while (x < len) {
    		char c = name.charAt(x);
    		if (   (c >= 'a' && c <= 'z')
    			|| (c >= 'A' && c <= 'Z')
    			|| (c >= '0' && c <= '9' && x > 0)
    			|| c == '-' || c == '$' || c == '.' || c == '_' ) {
    		} else break;  // hit a character that requires quoting
    		x++;
    	}
    	if (x == len) return name;  // no quotes needed
    	StringBuilder quoted = new StringBuilder(name.length()+2);
    	quoted.append('"');
    	quoted.append(name);
    	// should be no quotes or backslashes in a name to escape
    	quoted.append('"');
    	return quoted.toString();
	}
	
	// Operation mapping
	
	static HashMap<String,String> llvmOpMap = new HashMap<String,String>();
	{
		llvmOpMap.put("~","xor");
		llvmOpMap.put("!","xor");
		llvmOpMap.put("*","mul");
		llvmOpMap.put("/","sdiv");
		llvmOpMap.put("%","srem");
		llvmOpMap.put("+","add");
		llvmOpMap.put("-","sub");
		llvmOpMap.put("<<","shl");
		llvmOpMap.put(">>","ashr");
		llvmOpMap.put(">>>","lshr");
		llvmOpMap.put("&","and");
		llvmOpMap.put("^","xor");
		llvmOpMap.put("|","or");
		llvmOpMap.put("=","icmp eq");
		llvmOpMap.put("!=","icmp ne");
		llvmOpMap.put("≠","icmp ne");
		llvmOpMap.put("<","icmp slt");
		llvmOpMap.put(">","icmp sgt");
		llvmOpMap.put("<=","icmp sle");
		llvmOpMap.put("≤","icmp sle");
		llvmOpMap.put(">=","icmp sge");
		llvmOpMap.put("≥","icmp sge");
	}
	
	// Type conversion operations used in CodePass
	static final String TRUNCATE = "trunc";
	static final String BITCAST = "bitcast";
	static final String FLOATING_TO_SIGNED = "fptosi";
	static final String ZERO_EXTEND = "zext";
	static final String SIGN_EXTEND = "sext";
	static final String FLOATING_EXTEND = "fpext";
	static final String SIGNED_TO_FLOATING = "sitofp";
	
	// Code output

	String currentBlock = null;
	
	private void emit(String... args) {
		emitn(args);
		endline();
	}

	private void endline() {
		try {
			code.write(lineSep);
			code.flush();
		} catch (IOException e) {
			// ignore
		}
	}

	private void emitn(String... args) {
		for (String a : args) {
			try {
				code.write(a);
			} catch (IOException e) {
				// ignore
			}
		}
	}

	void emitBreak() {
		emit("");
		emit(";--------------------------------------------------------------------------------");
		emit("");	
	}
	
	// Diversion buffering for initialization code
	
	public static class DiversionBuffer extends CharArrayWriter {}

	/** Begin diversion to a buffer
	 *  @return the current output writer to resume
	 */
	Writer divert(DiversionBuffer buffer) {
		flush();
		Writer current = code;
		code = buffer;
		//Main.debug("divert size="+buffer.size());
		return current;
	}
	
	/** Resume output to a writer saved from divert
	 * @param saved the previous writer returned by divert
	 */
	void resume(Writer saved) {
		flush();
		//Main.debug("divert size="+((DiversionBuffer)code).size());
		code = saved;
	}
	
	/** Insert text from a diversion buffer and clear the buffer 
	 * @param the buffer
	 */
	void undivert(DiversionBuffer diverted) {
		try {
			diverted.writeTo(code);
			diverted.reset();
		} catch (IOException e) {
			// ignore
		}
	}

	// Code sequence labels
	
	private int labelSequence = 0;
	
	String nextLabel() {
		return Integer.toHexString(++labelSequence);
	}

	void emitLabel(String label) {
		emitn(label);
		emit(":");
		currentBlock = label;
	}
	
	void emitFunctionDecl(Type rtype, String fName, List<CodeReg> args) {
		emitn("declare ",CodeReg.llvmType(rtype)," ",fName,"(");
		String sep = "";
		for (CodeReg a : args) {
			emitn(sep,a.typeAndName());
			sep = ",";
		}
		emit(")");
	}

	void emitFunctionDecl(Type rtype, String fName, String argTypes) {
        emit("declare ",CodeReg.llvmType(rtype)," ",fName,argTypes);
	}

	void emitFunctionStart(Type rtype, String fName, List<CodeReg> args) {
		emitn("define ",CodeReg.llvmType(rtype)," ",fName,"(");
		String sep = "";
		for (CodeReg a : args) {
			emitn(sep,a.typeAndName());
			sep = ",";
		}
        emit(") {");
		currentBlock = "0";
	}

	void emitFunctionEnd() {
		emit("}");
        currentBlock = null;
	}

	void emitGlobal(Type vtype, String varn, String init) {
		String vart = CodeReg.llvmType(vtype);
		emit(varn," = global ",vart," ",init);
	}

	void emitExternal(Type vtype, String varn) {
		String vart = CodeReg.llvmType(vtype);
		emit(varn," = external global ",vart);
	}

	void emitStatic(Type vtype, String varn, String init) {
		String vart = CodeReg.llvmType(vtype);
		emit(varn," = internal global ",vart," ",init);
	}
	
    CodeReg emitAlloc(Type t, String n) {
		CodeReg r = CodeReg.newPointer(t,n);
		emit("    ",n," = alloca ",r.getPointedType());
		return r;
	}

	CodeReg emitDyadic(Type t, String op, CodeReg a, CodeReg b) {
		op = llvmOpMap.get(op);
		if (a.isConstant() && b.isConstant()) {
			return CodeReg.dyadicConst(t, a, op, b);
		}
    	CodeReg r = CodeReg.newValue(t);
		emit("    ",r.getName()," = ",op," ",a.typeAndName(),", ",b.getName());
		return r;
	}

	CodeReg emitConvert(CodeReg a, String conv, Type t) {
		if (a.isConstant()) {
			return CodeReg.convertConst(t, conv, a);
		}
    	CodeReg r = CodeReg.newValue(t);
		emit("    ",r.getName()," = ",conv," ",a.typeAndName()," to ",r.getRegType());
		return r;
	}

	CodeReg emitLoad(CodeReg f) {
		if (!f.isPointer()) throw new IllegalArgumentException("must be a pointer value");
		Type ftype = f.getType();
		CodeReg r = CodeReg.newValue(ftype);
		emit("    ",r.getName()," = load ",r.getRegType(),", ",f.typeAndName());
		return r;
	}

	void emitStore(CodeReg v, CodeReg f) {
		if (f.getType() == Type.errorType) return;
		if (!f.isPointer()) throw new IllegalArgumentException("must be a pointer destination");
		emit("    store ",v.typeAndName(),", ",f.typeAndName());
	}

	void emitIndexCheck(CodeReg ap, CodeReg re) {
		//LATER would this be better as a function call?
		//hoping inline will result in some common subexpression elimination
    	String pb = currentBlock;
    	String lx=nextLabel(),lElse="else"+lx,lOr="or"+lx,lThrow="throw"+lx,lEnd="ok"+lx;
    	emit("    ; index check ",pb," ",lEnd);
		//	    %cmpz = icmp slt i32 %re, 0
    	CodeReg cmpz = emitDyadic(Type.booleanType, "<", re, CodeReg.ZERO);
		//	    br i1 %cmpz, label %ox, label %ex
    	emitBranchBool(cmpz, lOr, lElse);
		//	ex:
    	emitLabel(lElse);
		//	    %plen = getelementptr %obj.test_Simple, %obj.test_Simple* %this, i32 0, i32 0 ; length
    	CodeReg plen = emitArrayLength(ap);
		//	    %rlen = load i32, i32* %plen
    	CodeReg len = emitLoad(plen);
		//	    %cmpl = icmp sge i32 %re, %rlen
    	CodeReg cmpl = emitDyadic(Type.booleanType, ">=", re, len);
		//	    br label %ox
    	emitBranchUnc(lOr);
		//	ox:
    	emitLabel(lOr);
		//	    %join = phi i1 [ true, %pb], [ %cmpl, %ex]
    	CodeReg join = emitJoin(CodeReg.TRUE, pb, cmpl, lElse, "emitIndexCheck");
		//	    br i1 %join, label %tx, label %xx
    	emitBranchBool(join, lThrow, lEnd);
		//	tx:
    	emitLabel(lThrow);
		//	    call void @std_lang_Throw_arrayIndexFault_Pint(i32 %re)
    	emitCall(Type.voidType, THROW_ARRAY_INDEX_FAULT, Arrays.asList(re));
    	//	    br label %xx
    	emitUnreachable(); //emitBranchUnc(lEnd);
		//	xx:
    	emitLabel(lEnd);
	}

	CodeReg emitArrayIndex(Type etype, CodeReg ap, CodeReg ri) {
		CodeReg r = CodeReg.newPointer(etype);
		emit("    ",r.getName()," = getelementptr ",CodeReg.llvmArrayType((ArrayType)ap.getType()),", ",ap.typeAndName(),
								", ",CodeReg.ZERO.typeAndName(),
								", ",CodeReg.TWO.typeAndName(),
								", ",ri.typeAndName());
		return r;
	}

	void emitNullCheck(CodeReg op) {
    	String lx=nextLabel(),lThrow="throw"+lx,lOK="ok"+lx,lEnd="end"+lx;
		//		%r1e = icmp eq {i32, [0 x i32]}* %r1d, null
    	CodeReg cmpn = emitDyadic(Type.booleanType, "=", op, CodeReg.NULL);
		//	    br i1 %r1e, label %t3, label %f3
    	emitBranchBool(cmpn, lThrow, lEnd);
		//	t3:
    	emitLabel(lThrow);
		//	    call void @std_lang_Throw_nullPointerFault()
       	emitCall(Type.voidType, THROW_NULL_POINTER_FAULT, CodePass.EMPTY_REG_LIST);
		//	    br label %e3
       	emitUnreachable(); //emitBranchUnc(lEnd);
		//	f3:
    	emitLabel(lOK);
		//	    br label %e3
    	emitBranchUnc(lEnd);
		//	e3:
    	emitLabel(lEnd);
	}

	CodeReg emitFieldIndex(Type ftype, CodeReg op, int index, String comment) {
		if (op.isPointer()) throw new IllegalArgumentException("pointer value must be loaded first");
		if (!(op.getType() instanceof ObjectType)) throw new IllegalArgumentException("must be an object reference");
		ObjectType otype = (ObjectType) op.getType();
		CodeReg r = CodeReg.newPointer(ftype);
		emit("    ",r.getName()," = getelementptr ",CodeReg.llvmObjType(otype),", ",op.typeAndName(),
								", ",CodeReg.ZERO.typeAndName(),
								", ",CodeReg.LLVM_I32," ",Integer.toString(index),
								" ; ", comment);
		return r;
	}

	CodeReg emitArrayFieldIndex(Type ftype, CodeReg ap, int index, String comment) {
		if (ap.isPointer()) throw new IllegalArgumentException("pointer value must be loaded first");
		if (!(ap.getType() instanceof ArrayType)) throw new IllegalArgumentException("must be an array reference");
		ArrayType atype = (ArrayType) ap.getType();
		CodeReg r = CodeReg.newPointer(ftype);
		emit("    ",r.getName()," = getelementptr ",CodeReg.llvmArrayType(atype),", ",ap.typeAndName(),
								", ",CodeReg.ZERO.typeAndName(),
								", ",CodeReg.LLVM_I32," ",Integer.toString(index),
								" ; ", comment);
		return r;
	}

	CodeReg emitArrayLength(CodeReg ap) {
		// TODO lookup field index in std.lang.array
		return emitArrayFieldIndex(Type.intType, ap, 0+INSTANCE_FIELD_OFFSET, "length");
	}

	CodeReg emitMethodIndex(MethodSymbol msym, CodeReg op) {
		if (op.isPointer()) throw new IllegalArgumentException("pointer value must be loaded first "+op.typeAndName());
		if (!(op.getType() instanceof ObjectType)) throw new IllegalArgumentException("must be an object reference");
		ObjectType otype = (ObjectType) op.getType();
		String mtabType = CodePass.classPrefix(MTABLE_TYPE_PREFIX,otype.getKlass());
		CodeReg mtabPtr = CodeReg.newInternal(mtabType+"**");
		emit("    ",mtabPtr.getName()," = getelementptr ",CodeReg.llvmObjType(otype),", ",op.typeAndName(),
								", ",CodeReg.ZERO.typeAndName(),
								", ",CodeReg.ZERO.typeAndName(),
								" ; mtab");
		CodeReg mtab = CodeReg.newInternal(mtabType+"*");
		emit("    ",mtab.getName()," = load ",mtab.getRegType(),", ",mtabPtr.typeAndName());
		String mpType = CodeReg.llvmMethodPtrType(msym);
		CodeReg mtePtr = CodeReg.newInternal(mpType+"*");
		emit("    ",mtePtr.getName()," = getelementptr ",mtabType,", ",mtab.typeAndName(),
								", ",CodeReg.ZERO.typeAndName(),
								", ",CodeReg.LLVM_I32," ",Integer.toString(msym.getIndex()),
								" ; ", msym.getName());
		CodeReg mPtr = CodeReg.newInternal(mpType);
		emit("    ",mPtr.getName()," = load ",mpType,", ",mtePtr.typeAndName());
		return mPtr;
	}

	void emitBranchUnc(String l) {
		emit("    br label %",l);
	}
    
	void emitBranchBool(CodeReg cond, String lt, String lf) {
		emit("    br ",cond.typeAndName(),", label %",lt,", label %",lf);
	}

	void emitUnreachable() {
		emit("    unreachable");
	}

	CodeReg emitJoin(CodeReg ra, String la, CodeReg rb, String lb, String comment) {
		CodeReg r = CodeReg.newValue(Type.booleanType);
		emit("    ",r.getName()," = phi ",r.getRegType()," [",ra.getName(),", %",la,"], [",rb.getName(),", %",lb,"]"," ; ",comment);
		return r;
	}

	void emitSwitchStart(CodeReg sel, String ld) {
		emitn("    switch ",sel.typeAndName(),", label %",ld," [");
	}

	void emitSwitchCase(CodeReg sel, String lc, CodeReg cv) {
		if (!cv.isConstant()) throw new IllegalArgumentException("case value must be a constant");
		emitn(" ",sel.getRegType()/*not cv.getRegType()*/," ",cv.getName(),",label %",lc);
	}

	void emitSwitchEnd() {
		emit(" ]");
	}

	void emitReturn(CodeReg a) {
		emit("    ret ",a.typeAndName());
	}

	CodeReg emitCall(Type rtype, String fname, List<CodeReg> args) {
		StringBuilder al = new StringBuilder();
		String sep = "";
		for (CodeReg a : args) {
			al.append(sep).append(a.typeAndName());
			sep = ", ";
		}
		if (rtype == Type.voidType) {
			emit("    call void ",fname,"(",al.toString(),")");
			return CodeReg.VOID;
		} else {
			CodeReg r = CodeReg.newValue(rtype);
			emit("    ",r.getName()," = call ",r.getRegType()," ",fname,"(",al.toString(),")");
			return r;
		}
	}

	CodeReg emitNewInstance(ClassSymbol csym) {
		CodeReg ni = CodeReg.newInternal(OBJECT_OBJ+"*");
		String cdef = CodePass.classPrefix(CLASS_DATA_PREFIX,csym);
		String objt = CodePass.classPrefix(OBJ_TYPE_PREFIX,csym);
		String mtt = CodePass.classPrefix(MTABLE_TYPE_PREFIX,csym);
		String mtn = CodePass.classPrefix(MTABLE_PREFIX,csym);
		emit("    ",ni.getName()," = call ",OBJECT_OBJ,"* ",CLASS_NEW_INSTANCE,"(",
				CLASS_OBJ,"* ",	cdef,", ",
				OBJECT_MTAB_TYPE,"* ",BITCAST,"(",mtt,"* ",mtn," to ",OBJECT_MTAB_TYPE,"*))");
		return ni;
	}

	void emitStringTable(HashMap<UTF8Bytes, String> table) {
		emit("; string literals");
		for (Entry<UTF8Bytes, String> entry : table.entrySet()) {
		    UTF8Bytes s = entry.getKey();
		    int len = s.getLength();
			String l = Integer.toString(len);
		    String id = entry.getValue();
			/*
				%obj.std_lang_String = type {
				    %mtab.std_lang_String*
				   ,{%mtab.std_lang_Array*, i32, [0 x i8]}* ;1 value
				   ,i32 ;2 count
				   ,i32 ;3 offset
				}
			*/
		    String arrayName = STRING_VALUE_PREFIX+id.substring(CodeGen.GLOBAL_PREFIX.length());
		    String arrayType = stringArrayType(len);
		    emitn(arrayName," = internal constant ",arrayType," {",
		    		ARRAY_MTAB_TYPE,"* ",ARRAY_MTAB,
		    		",i32 ",l,
		    		",[",l," x i8][");
		    String sep = "";
		    for (int i = 0; i < len; i++) {
		    	emitn(sep,"i8 ",Integer.toString(s.getByte(i)));
		    	sep = ",";
		    }
		    emitn("]} ;");
		    //TODO add comment with printable characters
		    
		    emit();
		    emit(id," = internal constant ",STRING_OBJ," {",
		    		STRING_MTAB_TYPE,"* ",STRING_MTAB,
		    		", ",INDEFINITE_STRING_ARRAY_TYPE,
		    				"* ",BITCAST,"(",arrayType,"* ",arrayName," to ",INDEFINITE_STRING_ARRAY_TYPE,"*)",
		    		", i32 ",l,
		    		", i32 0}");
		}
	}
	
	final static ArrayType bytesType	= new ArrayType(Type.byteType);

	private static String stringArrayType(int size) {
		return CodeReg.llvmArrayType(bytesType, size);
	}
	private static String INDEFINITE_STRING_ARRAY_TYPE;
	{
		INDEFINITE_STRING_ARRAY_TYPE = stringArrayType(0);
	}
	
	public void flush() {
		try {
			code.flush();
		} catch (IOException excp) {
			Main.error("Exception writing "+codeFile.getAbsoluteFile()+": "+excp.getMessage());
		}
	}

	void emitClassTypes(ClassSymbol csym) {
		//Main.debug("emitClassTypes %08X %s members ", ((Object)csym).hashCode(), csym.toString(), csym.symbols.values().toString());
		emit(";; ",csym.getFullName()," class structures");
		// virtual method table
		final MethodSymbol[] vMethods = csym.getVirtualMethods();
		final String mtt = CodePass.classPrefix(MTABLE_TYPE_PREFIX,csym);
		emit(mtt," = type {");
		emit("    ",CLASS_OBJ,"*"," ;0");
		for (MethodSymbol m : vMethods) {
			String access = m.getAccess().shortString();
			emit("   ,",CodeReg.llvmMethodPtrType(m)," ;",Integer.toString(m.getIndex())," ",access," ",m.getName(),m.getParameterTypes().toString());
		}
		emit("}");
		final String objt = CodePass.classPrefix(OBJ_TYPE_PREFIX,csym);
		emit(objt," = type {");
		emit("    ",mtt,"*");
		// instance fields
		final FieldSymbol[] iFields = csym.getInstanceFields();
		for (FieldSymbol f : iFields) {
			String access = f.getAccess().shortString();
			emit("   ,",CodeReg.llvmType(f.getType())," ;",Integer.toString(f.getIndex())," ",access," ",f.getName());
		}
		emit("}");
	}

	void emitClassGlobals(ClassSymbol csym) {
		final String glb = CodePass.classPrefix(CodeGen.GLOBAL_PREFIX,csym)+CodePass.NAME_SEPARATOR;
		final String cdef = CodePass.classPrefix(CLASS_DATA_PREFIX,csym);
		emit(cdef," = external global ",CLASS_OBJ);
		final String mtt = CodePass.classPrefix(MTABLE_TYPE_PREFIX,csym);
		final String mtn = CodePass.classPrefix(MTABLE_PREFIX,csym);
		emit(mtn," = external global ",mtt);
		final Collection<Symbol> members = csym.getMembers().values();
		emit("; static fields");
		for (Symbol member : members) {
			if (member instanceof FieldSymbol) {
				FieldSymbol f = (FieldSymbol)member;
				if (f.isStatic()) {
			        Type vtype = f.getType();
					String varn = glb+f.getName();
					emitExternal(vtype, varn);
				}
			}
		}
		emit("; static methods");
		for (Symbol member : members) {
			if (member instanceof MethodSymbol) {
				for (MethodSymbol m = (MethodSymbol)member; m != null; m = m.getOverloads()) {
    				if (m.isStatic()) {
	    				Type rtype = m.getType();
	    		        String fName = CodePass.makeMethodName(m);
    			        emitFunctionDecl(rtype, fName, CodeReg.llvmArgumentTypes(m));
    				}
				}
			}
		}
	}

	void emitClassObject(ClassSymbol csym, String nameStringID) {
		/*
			%obj.std_lang_Class = type {
			    %mtab.std_lang_Class*
			   ,%obj.std_lang_String* ;1 name
			   ,i32 ;2 instanceSize
			}
		*/
		final String cdef = CodePass.classPrefix(CLASS_DATA_PREFIX,csym);
		final String objt = CodePass.classPrefix(OBJ_TYPE_PREFIX,csym);
		emit(cdef," = global ",CLASS_OBJ," {");
		emit("    ",CLASS_MTAB_TYPE,"* ",CLASS_MTAB);
		emit("   ,",STRING_OBJ,"* ",nameStringID);
				// NB assumes emitStringTable is called after emitClassObject
		// sizeof - http://nondot.org/sabre/LLVMNotes/SizeOf-OffsetOf-VariableSizedStructs.txt
		emit("   ,i32 ","ptrtoint(",objt,"* getelementptr(",objt,", ",objt,"* null, i32 1) to i32)");
		emit("}");
		/*TODO http://llvm.org/docs/LangRef.html#the-llvm-global-ctors-global-variable
		%0 = type { i32, void ()*, i8* }
		@llvm.global_ctors = appending global [1 x %0] [%0 { i32 65535, void ()* @ctor, i8* @data }]
		%0 = type { i32, void ()*, i8* }
		@llvm.global_dtors = appending global [1 x %0] [%0 { i32 65535, void ()* @dtor, i8* @data }]
		*/
		
		// Method table
		final MethodSymbol[] methods = csym.getVirtualMethods();
		// Declare any superclass methods that have not been overridden
		for (MethodSymbol m : methods) {
			if (m.getDefiningScope() != csym) {
				final Type rtype = m.getType();
		        final String fName = CodePass.makeMethodName(m);
		        emitFunctionDecl(rtype, fName, CodeReg.llvmArgumentTypes(m));
			}
		}
		final String mtt = CodePass.classPrefix(MTABLE_TYPE_PREFIX,csym);
		final String mtn = CodePass.classPrefix(MTABLE_PREFIX,csym);
		emit(mtn," = global ",mtt," {");
		emit("    ",CLASS_OBJ,"* ",cdef);
		for (MethodSymbol m : methods) {
			emit("   ,",CodeReg.llvmMethodPtrType(m)," ",CodePass.makeMethodName(m));
		}
		emit("}");
		emit("declare ",OBJECT_OBJ,"* @std_lang_Class_newInstance(",CLASS_OBJ,"*, ",OBJECT_MTAB_TYPE,"*)");
	}

	public void emitSourceComment(ParserRuleContext ctx) {
		emitSourceComment(ctx,ctx.getStop());
		/*
		final Token start = ctx.getStart(), stop = ctx.getStop();
		final CharStream inSt = start.getInputStream();
		assert stop.getInputStream() == inSt;  // assuming does not cross streams
		String text = inSt.getText(new Interval(start.getStartIndex(),stop.getStopIndex()));
		emit("    ; ",Integer.toString(start.getLine())," ",text.replace("\n","\\n"));
		*/
	}

	// for partial context, e.g. if expr
	public void emitSourceComment(ParserRuleContext ctx, Token stop) {
		final Token start = ctx.getStart();
		final CharStream inSt = start.getInputStream();
		assert stop.getInputStream() == inSt;  // assuming does not cross streams
		String text = inSt.getText(new Interval(start.getStartIndex(),stop.getStopIndex()));
		emit("    ; ",Integer.toString(start.getLine())," ",text.replace("\n","\\n"));
	}
/*
	public CodeReg emitStackSave() {
		CodeReg ss = CodeReg.newStacksave();
		emit("    ",ss.getName()," = call ",ss.getRegType()," @llvm.stacksave()");
		return ss;
	}

	public void emitStackRestore(CodeReg ss) {
		emit("    call void @llvm.stacksave(",ss.typeAndName()," )");
	}
*/
	public CodeReg emitAtomicXchg(CodeReg f, CodeReg v) {
		if (!f.isPointer()) throw new IllegalArgumentException("must be a pointer destination");
    	CodeReg r = CodeReg.newValue(f.getType());
		emit("    ",r.getName()," = atomicrmw xchg ",f.typeAndName(),", ",v.typeAndName()," acquire");
		return r;
	}

	public void emitConstructorLink(int priority, String funcName, String data) {
		emit("@llvm.global_ctors = appending global [1 x ",GLOBAL_XTORS_TYPE,"] [",GLOBAL_XTORS_TYPE," { i32 ",Integer.toString(priority),", void ()* ",funcName,", i8* ",data," }]");		
	}

	public void emitDestructorLink(int priority, String funcName, String data) {
		emit("@llvm.global_dtors = appending global [1 x ",GLOBAL_XTORS_TYPE,"] [",GLOBAL_XTORS_TYPE," { i32 ",Integer.toString(priority),", void ()* ",funcName,", i8* ",data," }]");		
	}

	private static int nextMetadataID = 0;
	
	public static Integer getNextMetadataID() {
		return new Integer(nextMetadataID++);
	}

	public Integer emitMetadata(Integer id, String... args) {
emitn(";");  // comment metadata until we have a look
		emitn("!");
		emitn(id.toString());
		emitn(" = ");
		emit(args);
		return id;
	}

	public Integer emitMetadata(String... args) {
		return emitMetadata(getNextMetadataID(), args);
	}
}

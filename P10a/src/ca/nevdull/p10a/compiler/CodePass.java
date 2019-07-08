/*	CodePass.java
 
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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import ca.nevdull.util.UTF8Bytes;

public class CodePass extends P10aBaseVisitor<CodeReg> {
	Main main;
	        static final ArrayList<CodeReg> EMPTY_REG_LIST = new ArrayList<CodeReg>(0);
	private static final ArrayList<Type> EMPTY_TYPE_LIST = new ArrayList<Type>(0);
	        static final String NAME_SEPARATOR	= "_";
	        static final String PARAMETER_SEPARATOR = "_P";
	        static final String ARRAY_PARAMETER	= "_Y";
	private static final String ARG_PREFIX		= CodeGen.LOCAL_PREFIX+"a";
	private static final String ARG_THIS		= CodeGen.LOCAL_PREFIX+DefinitionPass.THIS;
	private static final String VAR_PREFIX		= CodeGen.LOCAL_PREFIX+"v";
	        static final String CLASS_INITIALIZE = "_class_initialize";
	private static final String CLASS_INITIALIZED = "_class_initialized";
	private static final String INSTANCE_INITIALIZE = "_initialize";
	private CodeGen code;
	private DebugInfo di;
	private String classNamePrefix;
	private boolean trace;
	private ClassSymbol arrayClass;
	private ObjectType classType;
	private ObjectType stringType;
	private CIncludeGen include;
	private CodeGen.DiversionBuffer classInitializer;
	private CodeGen.DiversionBuffer instanceInitializer;
	
	private Integer diFile; // metadata ID

	public CodePass(Main main, String sourceFilePath, String sourceFileName, String sourceFileMD5) throws FileNotFoundException, UnsupportedEncodingException {
		this.main = main;
    	trace = main.trace.contains("CodePass");
    	code = new CodeGen(main.outputDirectory, Main.dropFileNameSuffix(sourceFileName), sourceFileName);
    	di = new DebugInfo(code);
    	include = new CIncludeGen(main.outputDirectory, Main.dropFileNameSuffix(sourceFileName), sourceFileName);
    	diFile = di.emitFile(sourceFilePath, sourceFileName, sourceFileMD5);
	}

	static String classPrefix(String prefix, ClassSymbol csym) {
		return prefix+escapeName(csym.getFullName());
	}
	
    // make names compatible with C
    static String escapeName(String name) {
    	int len = name.length();
    	int x = 0;
    	for ( ; x < len; x++) {
    		char c = name.charAt(x);
    		if (   (c >= 'a' && c <= 'z')
        		|| (c >= 'A' && c <= 'Z')
        		|| (c >= '0' && c <= '9' && x > 0) ) {
    		} else break;  // hit a character that requires escaping
    	}
    	if (x == len) return name;  // no escapes needed
    	StringBuilder escaped = new StringBuilder(name.length()*2);
    	escaped.append(name.substring(0, x));
    	for ( ; x < len; x++) {
    		char c = name.charAt(x);
    		if (   (c >= 'a' && c <= 'z')
        		|| (c >= 'A' && c <= 'Z')
        		|| (c >= '0' && c <= '9' && x > 0) ) {
    			escaped.append(c);
    		} else if (c == '_') {
    			escaped.append("__");
    		} else if (c == '.') {
    			escaped.append(NAME_SEPARATOR);
    		} else if (c < 0x80) {
    			escaped.append("_")
    				.append(Character.forDigit((c>> 4)&0xf,16))
    				.append(Character.forDigit( c     &0xf,16));
    		} else {
    			if (c >= 0x10000) throw new IllegalArgumentException("surrogate character in name");
    			escaped.append("_")
    				.append(Character.forDigit((c>>12)&0xf,16))
    				.append(Character.forDigit((c>> 8)&0xf,16))
    				.append(Character.forDigit((c>> 4)&0xf,16))
    				.append(Character.forDigit( c     &0xf,16));
    	    }
    	}
    	return escaped.toString();
	}
	
	// String table

	private int stringIDSequence = 0;
	HashMap<UTF8Bytes,String> stringTable = new HashMap<UTF8Bytes,String>();
	
	private String nextStringID() {
		return "@s"+Integer.toHexString(stringIDSequence++);
	}
	
	String stringID(UTF8Bytes utf8) {
		String id = stringTable.get(utf8);
		if (id == null) {
			id = nextStringID();
			stringTable.put(utf8,id);
		}
		return id;
	}
	
	// Loop exit and repeat labels
	
	ArrayDeque<String> loopDone = new ArrayDeque<String>();
	ArrayDeque<String> loopNext = new ArrayDeque<String>();

	// Parse tree visitor
	
	private Token getChildToken(ParseTree node, int index) {
		return ((TerminalNode)node.getChild(index)).getSymbol();
	}

	@Override
	public CodeReg visitFile(P10aParser.FileContext ctx) {
		// NL* 'class' qualifiedName NL+ ( extendDecl NL+ )? ( interfaceDecl NL+ )* ( member NL+ )* EOF
		// all importDeclarations, then fieldDeclarations, then all methodDeclarations
		classNamePrefix = classPrefix(CodeGen.GLOBAL_PREFIX,ctx.defn);
		
		// perform imports
		List<P10aParser.MemberContext> members = ctx.member();
		for (P10aParser.MemberContext m : members) {
			if (m.importDecl() != null) visit(m.importDecl());
		}
		
		// types for referenced classes
		Scope top = ctx.defn.getEnclosingScope();
		arrayClass = (ClassSymbol)top.find(DefinitionPass.STD_LANG_IMPORTS_ARRAY);
		classType = new ObjectType((ClassSymbol)top.find(DefinitionPass.STD_LANG_IMPORTS_CLASS));
		stringType = new ObjectType((ClassSymbol)top.find(DefinitionPass.STD_LANG_IMPORTS_STRING));
		HashSet<ClassSymbol> did = new HashSet<ClassSymbol>();
		for (Symbol s : top.getMembers().values()) {
			if (s instanceof ClassSymbol) {
				ClassSymbol csym = (ClassSymbol)s;
				if (!did.contains(csym)) {
					if (trace) Main.debug("top %s", csym.getFullName());
assert csym.isLoaded();
					code.emitClassTypes(csym);
					if (csym != ctx.defn) {
						code.emitClassGlobals(csym);
					}
					include.emitClassType(csym);
					di.emitClassType(csym);
					did.add(csym);
				}
			} else {
				Main.debug("skip %s", s.toString());
			}
		}
		di.emitCompileUnit(diFile, top);

		include.emitClassStructs(ctx.defn);
		di.emitClassType(ctx.defn);
		for (ClassSymbol csym : did) {
			if (csym != ctx.defn) include.emitInclude(csym);
		}
		
		// type and methods for this class
		code.emitBreak();
		classInitializer = new CodeGen.DiversionBuffer();
		instanceInitializer = new CodeGen.DiversionBuffer();
		for (P10aParser.MemberContext m : members) {
			if (m.fieldDecl() != null) visit(m.fieldDecl());
		}
		for (P10aParser.MemberContext m : members) {
			if (m.methodDecl() != null) visit(m.methodDecl());
		}
		defineInitializers(ctx.defn);
		code.emitClassObject(ctx.defn, stringID(new UTF8Bytes(ctx.defn.getFullName())));
		code.emitStringTable(stringTable);
		
		code.flush();
		include.close();
		return null;
	}

	@Override
	public CodeReg visitExtendDecl(P10aParser.ExtendDeclContext ctx) {
		// 'extends' qualifiedName
		// emitClassTypes done in visitFile
		return null;
	}

	@Override
	public CodeReg visitInterfaceDecl(P10aParser.InterfaceDeclContext ctx) {
		// 'interface' qualifiedName
		// emitClassTypes done in visitFile
		return null;
	}

	@Override
	public CodeReg visitImportDecl(P10aParser.ImportDeclContext ctx) {
		// 'import' qualifiedName
		// emitClassTypes done in visitFile
		return null;    	
	}
	
	@Override
	public CodeReg visitMethodDecl(P10aParser.MethodDeclContext ctx) {
		// Identifier stat='static'? '(' NL* formalParameters? ')' type? body
        // don't need to visit returnType in this pass
        P10aParser.ParameterListContext pl = ctx.parameterList();
        P10aParser.BodyContext body = ctx.body();
        boolean blockBody = body.block() != null;
        Type rtype = ctx.defn.getType();
        String fName = makeMethodName(ctx.defn);
        if (trace) Main.debug("visitMethodDecl %s", fName);
		
        // build parameter list, including the implicit 'this' for virtual methods
        ArrayList<CodeReg> params = new ArrayList<CodeReg>();
		FieldSymbol ip = ctx.defn.getImplicitParameter();
		if (ip != null) {
		    Type ptype = ip.getType();
			params.add(CodeReg.newConstant(ptype,ARG_THIS));
		}
        if (pl != null) {
		    for (P10aParser.ParameterContext p : pl.parameter()) {
		        Type ptype = p.type().tipe;
				String pid = p.Identifier().getSymbol().getText();
				String argn = ARG_PREFIX+escapeName(pid);
				params.add(CodeReg.newConstant(ptype,argn));
		    }
        }
        
        // method body
		if (blockBody) {
//			String diSubProgram = di.subprogram();
	        code.emitFunctionStart(rtype, fName, params);
	        // only static methods may need to initialize the class
	        if (ctx.stat != null) {
	        	String init = classNamePrefix+NAME_SEPARATOR+CLASS_INITIALIZE;
	    		code.emitCall(Type.voidType, init, EMPTY_REG_LIST);
	        }
	        // store parameter values
	        if (pl != null) {
	        	Iterator<CodeReg> argIter = params.iterator();
	        	if (ctx.stat==null) argIter.next(); // skip implicit 'this' parameter, it is read-only
			    for (P10aParser.ParameterContext p : pl.parameter()) {
			    	CodeReg param = argIter.next();
			        Type ptype = param.getType();
					String pid = p.Identifier().getSymbol().getText();
					String varn = VAR_PREFIX+escapeName(pid);
			        CodeReg pref = code.emitAlloc(ptype,varn);
			    	code.emitStore(param,pref);
			    }
	        }
	        visit(body);
	        assert loopDone.isEmpty() && loopNext.isEmpty();
	        //TODO check if all paths do a return, instead of returning this default value
	        if (rtype == Type.voidType) {
	        	code.emitReturn(CodeReg.VOID);
	        } else if (rtype instanceof Type.Reference) {
	        	code.emitReturn(CodeReg.newConstant(rtype, "null"));
	        } else if (rtype == Type.floatType || rtype == Type.doubleType){
	        	code.emitReturn(CodeReg.newConstant(rtype, "0.0"));
	        } else if (rtype instanceof Type.Numeric) {
	        	code.emitReturn(CodeReg.newConstant(rtype, "0"));
	        } else if (rtype == Type.booleanType) {
	        	code.emitReturn(CodeReg.newConstant(rtype, "0"));
	        } else {
	        	// TODO hasn't this covered all possible primitive types?
	        	Main.toDo(ctx.getStop(), "correct default return value "+rtype);
	        	code.emitReturn(CodeReg.newConstant(rtype, "0"));
	        }
	        code.emitFunctionEnd();
		} else {
			// native
	        code.emitFunctionDecl(rtype, fName, params);
	        include.emitNativeMethod(ctx.defn);
		}
		
		// special case for instance initialization methods
		if (ctx.defn.getDefiningScope() instanceof ClassSymbol
			&& ctx.defn.getName().equals(ClassSymbol.INITIALZER_METHOD_NAME)) {
			defineCreator((ClassSymbol)ctx.defn.getDefiningScope(), pl, fName);
		}
		
        return null;
    }

	private void defineCreator(ClassSymbol csym, P10aParser.ParameterListContext pl, String mnew) {
		// an instance allocation method to wrap each initialization method with a
		// matching parameter signature
		MethodSymbol creator = (MethodSymbol)csym.findMember(ClassSymbol.NEW_INSTANCE_METHOD_NAME);
		
		ArrayList<Type> argTypes;
		ArrayList<CodeReg> newRegs = new ArrayList<CodeReg>();
		if (pl != null) {
		    List<P10aParser.ParameterContext> params = pl.parameter();
			argTypes = new ArrayList<Type>(params.size());
		    for (P10aParser.ParameterContext p : params) {
		        Type ptype = p.type().tipe;
				argTypes.add(ptype);
				String pid = p.Identifier().getSymbol().getText();
				String argn = ARG_PREFIX+escapeName(pid);
				newRegs.add(CodeReg.newConstant(ptype,argn));
		    }
		} else {
			argTypes = CodePass.EMPTY_TYPE_LIST;
		}
		
		MethodSymbol cres = creator.resolveOverload(argTypes);
		
		Type rtype = cres.getType();
        String fName = makeMethodName(cres);
        if (trace) Main.debug("defineCreator %s", fName);
		code.emitFunctionStart(rtype, fName , newRegs);
		CodeReg ni = code.emitNewInstance(csym);
		CodeReg no = code.emitConvert(ni,CodeGen.BITCAST,rtype);
		newRegs.add(0,no);  // push in front
     	String init = classNamePrefix+NAME_SEPARATOR+INSTANCE_INITIALIZE;
     	ArrayList<CodeReg> initRegs = new ArrayList<CodeReg>(1);
     	initRegs.add(no);
		code.emitCall(Type.voidType, init, initRegs);
		code.emitCall(Type.voidType, mnew, newRegs);
		code.emitReturn(no);
		code.emitFunctionEnd();
	}

	private void defineInitializers(ClassSymbol csym) {
     	StringBuilder n = new StringBuilder();
     	
     	n.append(classNamePrefix).append(NAME_SEPARATOR).append(CLASS_INITIALIZED);
 		CodeReg initialized = CodeReg.newPointer(Type.byteType, n.toString());
		code.emitStatic(initialized.getType(), initialized.getName(), "0");
     	n.setLength(0);
     	n.append(classNamePrefix).append(NAME_SEPARATOR).append(CLASS_INITIALIZE);
    	String classInitName = n.toString();
		code.emitFunctionStart(Type.voidType, classInitName, EMPTY_REG_LIST);
		/*
		 * if atomic-xchg(classInitialized,true) then return
		 */
		String lx=code.nextLabel(), lTrue="then"+lx, lEnd="end"+lx;
		CodeReg ir = code.emitAtomicXchg(initialized,CodeReg.newConstant(initialized.getType(), "-1"));
		ir = code.emitDyadic(Type.booleanType, "!=", ir, CodeReg.newConstant(initialized.getType(), "0"));
    	code.emitBranchBool(ir, lTrue, lEnd);
    	code.emitLabel(lTrue);
    	code.emitReturn(CodeReg.VOID);
		code.emitBranchUnc(lEnd);
    	code.emitLabel(lEnd);
		code.undivert(classInitializer);
    	code.emitReturn(CodeReg.VOID);
		code.emitFunctionEnd();
		code.emitConstructorLink(65535, classInitName, "null");
		// TODO priority for dependent initializers
		// TODO decide about on-demand initialization versus mass initialization (currently doing both!)
		//TODO destructors (finalizers)
		
		n.setLength(0);
     	n.append(classNamePrefix).append(NAME_SEPARATOR).append(INSTANCE_INITIALIZE);
        ArrayList<CodeReg> params = new ArrayList<CodeReg>();
		params.add(CodeReg.newConstant(csym.getType(),ARG_THIS));
		code.emitFunctionStart(Type.voidType, n.toString(), params);
		code.undivert(instanceInitializer);
    	code.emitReturn(CodeReg.VOID);
		code.emitFunctionEnd();
		
	}

	@Override
	public CodeReg visitFieldDecl(P10aParser.FieldDeclContext ctx) {
    	// Identifier stat='static'? type ( init=':' NL* expression )?
        // don't need to visit type in this pass
        FieldSymbol fsym = ctx.defn;
		Scope defScope = fsym.getDefiningScope();
        boolean global = defScope instanceof ClassSymbol;
        Type ftype = fsym.getType();
		String fid = ctx.Identifier().getSymbol().getText();
		String pfx = (global) ? classNamePrefix+NAME_SEPARATOR
							  : VAR_PREFIX;
     	String varn = pfx+fid;
        // TODO subscope shadowing
		P10aParser.ExpressionContext init = ctx.expression();  // initializer
		if (trace&&init!=null) Main.debug(varn+" init "+init.getText());
		if (ctx.stat!=null) {
			String iv = (ftype instanceof Type.Reference) ? "null"
						: (ftype == Type.floatType || ftype == Type.doubleType) ? "0.0"
						: (ftype == Type.booleanType) ? "false"
						:"0";
			if (global) {
				code.emitGlobal(ftype, varn, iv);
			} 	else {
				code.emitStatic(ftype, varn, iv);
			}
			if (init != null) {
				Writer save = code.divert(classInitializer);
					code.emitSourceComment(ctx);
					CodeReg rf = CodeReg.newPointer(ftype, varn);
					CodeReg ri = visit(init);
					doAssign(ctx, rf, ri, ctx.init);
				code.resume(save);
			}
		} else if (global) {
			// instance field
			if (init != null) {
				Writer save = code.divert(instanceInitializer);
					code.emitSourceComment(ctx);
	    			ObjectType objType = (ObjectType)((ClassSymbol)defScope).getType();
	    			CodeReg t = CodeReg.newName(objType,ARG_THIS);
			    	CodeReg rf = code.emitFieldIndex(ftype,t,fsym.getIndex(),fsym.getName());
					CodeReg ri = visit(init);
					doAssign(ctx, rf, ri, ctx.init);
				code.resume(save);
			}
		} else {
			if (defScope instanceof BlockScope) {
				varn = varn+"."+fsym.getSeq();  // qualify local variable
			}
			CodeReg rv = code.emitAlloc(ftype,varn);
			if (init != null) {
				code.emitSourceComment(ctx);
				CodeReg ri = visit(init);
		    	if (ri == null) return null;
				doAssign(ctx, rv, ri, ctx.init);
			}
		}
        return null;
	}

	private void doAssign(ParserRuleContext ctx, CodeReg ref, CodeReg val, Token opToken) {
		Type vtype = ref.getType();
		if (vtype instanceof Type.Numeric) {
			val = widenNumericType(vtype, val, opToken);
		} else if (vtype instanceof ObjectType) {
			val = widenObjectType((ObjectType) vtype, val, opToken);
		} else if (vtype instanceof ArrayType
				&& val.getType().equals(vtype)) {
		//TODO other ArrayType conversion?
		} else if (vtype == Type.errorType) {
			// silent if error already reported
		} else {
			Main.toDo(ctx.getStart(), "cast non-numeric assignment "+vtype); // TODO
		}
		code.emitStore(val, ref);
	}   

	@Override
    public CodeReg visitIfStmt(P10aParser.IfStmtContext ctx) {
		// 'if' expression block ( 'elsif' expression block )* ( 'else' block )? '.'
		code.emitSourceComment(ctx,ctx.expression(0).getStop());
    	Iterator<P10aParser.ExpressionContext> exprs = ctx.expression().iterator();
    	Iterator<P10aParser.BlockContext> blocks = ctx.block().iterator();
		String lx=code.nextLabel(), lTrue="then"+lx, lFalse="else"+lx, lEnd="end"+lx;
		while (exprs.hasNext()) {
			P10aParser.ExpressionContext e = exprs.next();
    		P10aParser.BlockContext b = blocks.next();
    		CodeReg cond = visit(e);
	    	checkBoolean(cond,e.getStop());
	    	code.emitBranchBool(cond, lTrue, lFalse);
	    	code.emitLabel(lTrue);
	    	visit(b);
    		code.emitBranchUnc(lEnd);
	    	code.emitLabel(lFalse);
			if (exprs.hasNext()) { lx=code.nextLabel(); lTrue="then"+lx; lFalse="else"+lx; }
    	}
		if (blocks.hasNext()) {
			// else block
			P10aParser.BlockContext b = blocks.next();
			visit(b);
		}
		code.emitBranchUnc(lEnd);
    	code.emitLabel(lEnd);
        return null;
    }
    
	private void checkBoolean(CodeReg cond, Token token) {
    	if (!(cond.getType() == Type.booleanType || cond.getType() == Type.errorType)) {
    		Main.error(token, "condition type must be boolean");
    	}
	}

	@Override
    public CodeReg visitWhileStmt(P10aParser.WhileStmtContext ctx) {
		// 'while' expression block '.'
    	P10aParser.ExpressionContext exp = ctx.expression();
		code.emitSourceComment(ctx,exp.getStop());
		String lx=code.nextLabel(), lLoop="while"+lx, lTrue="true"+lx, lEnd="end"+lx;
		//TODO @llvm.stacksave / @llvm.stackrestore ?
		code.emitBranchUnc(lLoop);
    	code.emitLabel(lLoop);
    	CodeReg cond = visit(exp);
		checkBoolean(cond,exp.getStop());
    	code.emitBranchBool(cond,lTrue,lEnd);
    	code.emitLabel(lTrue);
    	loopDone.add(lEnd);
    	loopNext.add(lLoop);
    	visit(ctx.block());
    	loopDone.remove();
    	loopNext.remove();
		code.emitBranchUnc(lLoop);
    	code.emitLabel(lEnd);
        return null;
    }

	@Override
	public CodeReg visitForInStmt(P10aParser.ForInStmtContext ctx) {
		// 'for' Identifier type ':' expression block '.'
    	P10aParser.ExpressionContext exp = ctx.expression();
		code.emitSourceComment(ctx,exp.getStop());
    	P10aParser.BlockContext block = ctx.block();
    	//TODO iterator initialization
    	Main.toDo(exp.getStart(), "iterator initialization");
		String lx=code.nextLabel(), lLoop="l"+lx, lTrue="t"+lx, lNext="n"+lx, lEnd="e"+lx;
		//TODO @llvm.stacksave / @llvm.stackrestore ?
		code.emitBranchUnc(lLoop);
    	code.emitLabel(lLoop);
    	CodeReg cond = deref(exp);  // iterator
    	if (cond != null) {
    		checkBoolean(cond,exp.getStop());
	    	code.emitBranchBool(cond, lTrue, lEnd);
    	} else {
    		
    	}
    	code.emitLabel(lTrue);
    	loopDone.add(lEnd);
    	loopNext.add(lNext);
    	code.emitLabel(lNext);
    	// TODO iterator next
    	Main.toDo(ctx.getStop(), "iterator next");
    	visit(block);
    	loopDone.remove();
    	loopNext.remove();
		code.emitBranchUnc(lLoop);
    	code.emitLabel(lEnd);
        return null;
	}

	@Override
	public CodeReg visitForWhileStmt(P10aParser.ForWhileStmtContext ctx) {
		// 'for' statement ( 'while' expression )? ( 'next' statement )? block '.'
    	List<P10aParser.StatementContext> stmt = ctx.statement();
		code.emitSourceComment(ctx,stmt.get(0).getStop());
    	P10aParser.ExpressionContext exp = ctx.expression();
    	P10aParser.BlockContext block = ctx.block();
    	visit(stmt.get(0));  // initialization
		String lx=code.nextLabel(), lLoop="for"+lx, lTrue="true"+lx, lNext="next"+lx, lEnd="end"+lx;
		//TODO @llvm.stacksave / @llvm.stackrestore ?
		code.emitBranchUnc(lLoop);
    	code.emitLabel(lLoop);
    	CodeReg cond = deref(exp);  // while condition
    	if (cond != null) {
    		checkBoolean(cond,exp.getStop());
	    	code.emitBranchBool(cond, lTrue, lEnd);
    	} else {
    		
    	}
    	code.emitLabel(lTrue);
    	loopDone.add(lEnd);
    	loopNext.add(lNext);
    	visit(block);
    	loopDone.remove();
    	loopNext.remove();
		code.emitBranchUnc(lNext);
    	code.emitLabel(lNext);
    	if (stmt.size() > 1) {
    		if (trace) Main.debug("visitForWhileStmt next %s", stmt.get(1).getText());
    		visit(stmt.get(1));  // next
    	}
		code.emitBranchUnc(lLoop);
    	code.emitLabel(lEnd);
        return null;
	}

	@Override
	public CodeReg visitSwitchStmt(P10aParser.SwitchStmtContext ctx) {
		// 'switch' expression NL+ switchCase* ( 'else' block )? '.'
    	Token opToken = getChildToken(ctx,0);
    	P10aParser.ExpressionContext exp = ctx.expression();
		String lx=code.nextLabel(), lElse="else"+lx, lEnd="end"+lx;
    	CodeReg sel = deref(exp);
    	if (sel != null) {
        	sel = widenNumericType(Type.intType, sel, opToken);
        	code.emitSwitchStart(sel, lElse);
    	} else {
    		
    	}
    	
    	// Switch cases
    	Iterator<P10aParser.SwitchCaseContext> cases = ctx.switchCase().iterator();
		P10aParser.BlockContext els = ctx.block();
    	ArrayList<String> caseLabel = new ArrayList<String>();
		TreeSet<Integer> seen = new TreeSet<Integer>(); 
		int n = 0;
		while (cases.hasNext()) {
			P10aParser.SwitchCaseContext c = cases.next();
			// 'case' constExp ( ',' NL* constExp )* block
			Iterator<P10aParser.CaseExpContext> cexps = c.caseExp().iterator();
			String lCase = "s"+lx+"c"+n;
			caseLabel.add(lCase);
			while (cexps.hasNext()) {
				P10aParser.CaseExpContext ce = cexps.next();
				CodeReg cr = visit(ce);
				if (cr != null) {
					if (!cr.isConstant()) {
						Main.error(ce.getStart(),"case value must be a constant");
						continue;
					}
					int cv = Integer.decode(cr.getName());
					if (seen.contains(cv)) {
						Main.error(ce.getStop(), "case value is repeated");
					} else {
						seen.add(cv);
					}
					code.emitSwitchCase(sel, lCase, cr);
				}
			}
		}
		code.emitSwitchEnd();
		
		// Repeat for case bodies
    	cases = ctx.switchCase().iterator();  n = 0;
		while (cases.hasNext()) {
			P10aParser.SwitchCaseContext c = cases.next();
			// 'case' constExp ( ',' NL* constExp )* block
			P10aParser.BlockContext b = c.block();
	    	code.emitLabel(caseLabel.get(++n));
			visit(b);
	    	code.emitBranchUnc(lEnd);
		}
    	code.emitLabel(lElse);
		if (els != null) {
			visit(els);
		} else {
			if (sel != null) code.emitCall(Type.voidType,CodeGen.THROW_SWITCH_INDEX_FAULT,Arrays.asList(sel));
			code.emitUnreachable();
		}
    	code.emitLabel(lEnd);
		return null;
	}

	@Override
	public CodeReg visitIntegerCase(P10aParser.IntegerCaseContext ctx) {
		// Integer
    	String text = ctx.Integer().getText();
		CodeReg r = makeIntegerConstant(text);
		return r;
    }

	@Override
	public CodeReg visitCharacterCase(P10aParser.CharacterCaseContext ctx) {
		// Character
    	String text = ctx.Character().getText();
		CodeReg r = makeCharacterConstant(text);
    	return r;
    }

	@Override
    public CodeReg visitDoneStmt(P10aParser.DoneStmtContext ctx) {
		// 'done'
		code.emitSourceComment(ctx);
		String lDone = loopDone.peekLast();
		if (lDone != null) {
			code.emitBranchUnc(lDone);
		} else {
			Main.error(ctx.getStart(), "done is not in while or for");
		}
        return null;
    }

	@Override
    public CodeReg visitNextStmt(P10aParser.NextStmtContext ctx) {
		// 'next'
		code.emitSourceComment(ctx);
		String lNext = loopNext.peekLast();
		if (lNext != null) {
			code.emitBranchUnc(lNext);
		} else {
			Main.error(ctx.getStart(), "next is not in while or for");
		}
        return null;
    }
    
	@Override
    public CodeReg visitReturnStmt(P10aParser.ReturnStmtContext ctx) {
		// 'return' expression?
		code.emitSourceComment(ctx);
    	// Find enclosing method's return type
		P10aParser.MethodDeclContext md = (P10aParser.MethodDeclContext)findAncestor(ctx, P10aParser.MethodDeclContext.class);
		Type rt = md.defn.getType();
		P10aParser.ExpressionContext exp = ctx.expression();
        if (exp != null) {
        	CodeReg r = deref(exp);
        	//TODO can get null pointer in next
        	/*DEBUG*/if (r == null) Main.error("return expression type null "+r);
        	if (r.getType() != rt && !r.getType().equals(rt) && r.getType() != Type.errorType) {
        		// TODO coercion of return type
        		Main.error(exp.getStart(),r.getType()+" is not expected return type "+rt);
        	}
        	code.emitReturn(r);
        } else {
        	if (rt != Type.voidType) {
        		Main.error(ctx.getStart(),"expected return type is "+rt);
        	}
        	code.emitReturn(CodeReg.VOID);
        }
        return null;
    }

	private ParserRuleContext findAncestor(ParserRuleContext ctx, Class<?> type) {
		for (ParserRuleContext ancestor = ctx;  ancestor != null;  ancestor = ancestor.getParent()) {
    		if (type.isInstance(ancestor)) {
    	        return ancestor;
    		}
    	}
		return null;
	}
    
	@Override
    public CodeReg visitExprStmt(P10aParser.ExprStmtContext ctx) {
		// expression
		code.emitSourceComment(ctx);
		P10aParser.ExpressionContext e = ctx.expression();
		visit(e);
		if (e instanceof P10aParser.CompareExprContext && e.getChild(1).getText().equals("=")) {
			Main.error(e.getStart(), "comparison is likely a mis-written assignment; use parentheses if intentional");
		}
        return null;
    }
    
	@Override
	public CodeReg visitBlock(P10aParser.BlockContext ctx) {
		//NL+ ( statement NL+ )*
		for (P10aParser.StatementContext s : ctx.statement()) {
			visit(s);
		}
        return null;
    }

	@Override
	public CodeReg visitPrimExpr(P10aParser.PrimExprContext ctx) {
		//	primary
		P10aParser.PrimaryContext prim = ctx.primary();
		CodeReg r = visit(prim);
		ctx.defn = prim.defn; // possibly null
		return r;
	}

	@Override
	public CodeReg visitMemberExpr(P10aParser.MemberExprContext ctx) {
		//	expression '.' Identifier
		P10aParser.ExpressionContext ref = ctx.expression();
		CodeReg rr = deref(ref);
// 		if (ref.defn == null) return rr; // don't extend an error
// 		Type refType = ref.defn.getType();
		Type refType = rr.getType();
 		if (refType == Type.errorType) return rr; // don't extend an error
		Token id = ctx.Identifier().getSymbol();
		if (trace) Main.debug("visitMemberExpr %s.%s %s %s", ref.getText(), id.getText(), ref.defn==null?"null":ref.defn, rr==null?"null":rr.typeAndName());
 		if (refType instanceof ObjectType) {
    		ClassSymbol defClass = ((ObjectType)refType).getKlass();
    		Symbol member = defClass.findMember(id);
    		ctx.defn = member;
    		if (member == null) {
    			Main.error(id,id.getText()+" is not a member of type "+refType);
//    	    } else if (member.getType() == Type.errorType){
    		} else {
    			if (member instanceof FieldSymbol) {
    				FieldSymbol fsym = (FieldSymbol)ctx.defn;
    			    Type ftype = fsym.getType();
    		    	if (fsym.isStatic()) {
    		 			String pfx = classPrefix(CodeGen.GLOBAL_PREFIX,defClass)+NAME_SEPARATOR;
    		 			if (trace) Main.debug("static field "+fsym);
    		 			//TODO reference should be the class name
    					String varn = pfx+fsym.getName();
    				    return CodeReg.newPointer(ftype,varn);
    		    	} else {
    		    		if (trace) Main.debug("field "+fsym);
    		    		if (rr.isPointer()) rr = code.emitLoad(rr);
    		    		code.emitNullCheck(rr);
    			    	return code.emitFieldIndex(ftype,rr,fsym.getIndex(),fsym.getName());
    		    	}
    			} else if (member instanceof MethodSymbol) {
    				// Note method selection must wait for callExpr so overloading can be resolved
    				return rr;
    			} else {
    				//TODO
    				Main.error(id, "unexpected member reference "+ctx.defn);
    			}
    		}
    		return CodeReg.ERROR_VALUE;
 		} else if (refType instanceof ArrayType) {
			Symbol member = arrayClass.findMember(id);
			// array reference is not quite the same as object reference
    		ctx.defn = member;
    		if (member == null) {
    			Main.error(id,id.getText()+" is not a member of type "+refType);
//	    	} else if (member.getType() == Type.errorType){
    		} else {
	    		if (ctx.defn instanceof FieldSymbol) {
	    			FieldSymbol fsym = (FieldSymbol)ctx.defn;
	    		    Type ftype = fsym.getType();
	    		    assert !fsym.isStatic();
	        		if (trace) Main.debug("field "+fsym);
	        		if (rr.isPointer()) rr = code.emitLoad(rr);
	        		code.emitNullCheck(rr);
			    	return code.emitArrayFieldIndex(ftype,rr,fsym.getIndex(),fsym.getName());
	    		} else if (ctx.defn instanceof MethodSymbol) {
	    			// Note method selection must wait for callExpr so overloading can be resolved
	    			return rr;
	    		} else {
	    			//TODO
					Main.error(id, "unexpected member reference "+ctx.defn);
	    		}
    		}
    	} else {
    		Main.error(id,"member on non-object type "+refType);
    	}
		return CodeReg.ERROR_VALUE;
	}

	@Override
	public CodeReg visitIndexExpr(P10aParser.IndexExprContext ctx) {
		// expression '[' NL* expression ']'
		P10aParser.ExpressionContext ref = ctx.expression(0);
    	P10aParser.ExpressionContext ind = ctx.expression(1);
    	CodeReg ap = deref(ref);
    	if (trace) Main.debug("visitIndexExpr %s %s %s", ref.getText(), ref.defn==null?"null":ref.defn, ap==null?"null":ap.typeAndName());
		ctx.defn = ref.defn;  // non-null for visitReferExpr
    	CodeReg re = deref(ind);
    	Token opToken = getChildToken(ctx,1);
    	if (ap.getType() instanceof ArrayType) {
        	Type type = ((ArrayType)ap.getType()).getElement();
        	re = widenNumericType(Type.intType, re, opToken);
        	code.emitIndexCheck(ap, re);
        	return code.emitArrayIndex(type,ap,re);
    	} else {
    		Main.error(opToken,"index on non-array type "+ap.getType());
    	}
		return CodeReg.ERROR_VALUE;
	}

	@Override
	public CodeReg visitCallExpr(P10aParser.CallExprContext ctx) {
		// expression '(' NL* argumentList? ')'
		// argumentList: expression ( ',' NL* expression )*
		
		// object reference
		P10aParser.ExpressionContext ref = ctx.expression();
		CodeReg rr = visit(ref);
		Symbol defn;
		if (ref instanceof P10aParser.MemberExprContext) {
			defn = ((P10aParser.MemberExprContext)ref).defn;
			if (trace) Main.debug("visitCallExpr MemberExpr "+defn);
		} else if (ref instanceof P10aParser.PrimExprContext) {
			P10aParser.PrimaryContext prim = ((P10aParser.PrimExprContext)ref).primary();
			if (prim instanceof P10aParser.IdPrimContext) {
				defn = ((P10aParser.PrimExprContext)ref).primary().defn;
				if (trace) Main.debug("visitCallExpr PrimExpr "+defn);
			} else {
				Main.error(ref.getStop(), prim.getText()+" is not a method");
				return CodeReg.ERROR_VALUE;
			}
		} else {
			Main.error(ref.getStop(), "expression is not callable");
			return CodeReg.ERROR_VALUE;
		}
    	if (trace) Main.debug("visitCallExpr %s %s %s", ref.getText(), defn, rr==null?"null":rr.typeAndName());

		if (defn == null) return CodeReg.ERROR_VALUE; // not defined
    	if (defn instanceof ClassSymbol) {
    		Symbol m = ((ClassSymbol)defn).findMember(ClassSymbol.NEW_INSTANCE_METHOD_NAME);
    		if (m instanceof MethodSymbol) {
    			defn = m;
    		} else {
        		Main.error(ref.getStop(),defn.getName()+" does not have a '"+ClassSymbol.INITIALZER_METHOD_NAME+"' method");
    			return CodeReg.ERROR_VALUE;
        	}
    	} else if (!(defn instanceof MethodSymbol)) {
    		Main.error(ref.getStop(),ref.getStop().getText()+" is not a method ("+defn+")");
			return CodeReg.ERROR_VALUE;
    	}
    	MethodSymbol msym = (MethodSymbol)defn;
    	if (trace) Main.debug("visitCallExpr %s %s %s", ref.getText(), msym, rr==null?"null":rr.typeAndName());
    	
    	// evaluate arguments and collect types
		P10aParser.ArgumentListContext argList = ctx.argumentList();
	    ArrayList<CodeReg> argRegs;
		ArrayList<Type> argTypes;
        if (argList != null) {
		    List<P10aParser.ExpressionContext> aexp = argList.expression();
		    argRegs = new ArrayList<CodeReg>(aexp.size());
			argTypes = new ArrayList<Type>(aexp.size());
			for (P10aParser.ExpressionContext e : aexp) {
		    	CodeReg a = deref(e);
		    	if (a == null) break;
		    	argRegs.add(a);
				argTypes.add(a.getType());
			}
        } else {
		    argRegs = CodePass.EMPTY_REG_LIST;
			argTypes = CodePass.EMPTY_TYPE_LIST;
        }
        
        // resolve overload
		MethodSymbol mres = msym.resolveOverload(argTypes);
		if (mres == null) {
			Main.error(ref.getStart(), "no '"+ref.getStop().getText()+"' method definition applicable to argument types "+argTypes);
			Main.debug(msym.toString());
			return CodeReg.ERROR_VALUE;
		}
		ref.defn = mres;  // after overload resolution
	    Type rtype = mres.getType();

	    // get method address
    	CodeReg mp;
    	if (mres.isStatic()) {
 			if (trace) Main.debug("static method "+mres);
 			//if (rr != null) Main.error(ref.getStop(),"instance call to static method");
 			//if (rr != null) Main.debug("static method rr="+rr.typeAndName());
 			//TODO reference should be the class name
 			String funn = makeMethodName(mres);
		    mp = CodeReg.newMethodRef(funn, mres);
		    //Main.debug("visitCallExpr static %s  %s %s", mres, funn, mp.typeAndName());
    	} else {
    		if (trace) Main.debug("method "+mres);
			if (rr == null) {
				if (mres.getName().equals(ClassSymbol.INITIALZER_METHOD_NAME)
						&& ref instanceof P10aParser.MemberExprContext 
						&& ((P10aParser.MemberExprContext)ref).expression().defn instanceof ClassSymbol) {
					Main.debug("class call new %s", mres);
				}
				// reference from within class
	    		Scope defScope = mres.getDefiningScope();
	        	ClassSymbol defClass = (ClassSymbol)defScope;
				ObjectType objType = (ObjectType)defClass.getType();
				rr = CodeReg.newName(objType,ARG_THIS);
			} else if (!rr.getName().equals(ARG_THIS)) { // 'this' does not need null check
				if (rr.isPointer()) rr = code.emitLoad(rr);
        		code.emitNullCheck(rr);
			}
			mp = code.emitMethodIndex(mres,rr);
			//Main.debug("visitCallExpr virtual %s %s", mres, mp.typeAndName());
    	}
    	
    	// build argument list
        Iterator<Map.Entry<String, Symbol>> pli = mres.getParameters().iterator();
    	List<CodeReg> args = new ArrayList<CodeReg>();
    	if (!mres.isStatic()) {
    		//TODO ensure call not in a static method
    		rr = widenObjectType((ObjectType) mres.getDefiningScope().getType(), rr, ref.getStop());
   			args.add(rr);
    	}
        if (argList != null) {
        	Iterator<P10aParser.ExpressionContext> argIter = argList.expression().iterator();
			for (CodeReg a : argRegs) {
				assert argIter.hasNext();
				assert pli.hasNext();
				P10aParser.ExpressionContext ae = argIter.next();
				Map.Entry<String, Symbol> p = pli.next();
		    	Type ptype = p.getValue().getType();
				Type atype = a.getType();
				if (atype == ptype) {
		    	} else if (atype instanceof Type.Numeric && ptype instanceof Type.Numeric) {
		    		a = widenNumericType(ptype, a, ae.getStop()); 
//				} else if (atype instanceof ArrayType && ptype instanceof ArrayType
//						&& ((ArrayType)atype).getElement() == ((ArrayType)ptype).getElement()) {
		    	} else if (atype instanceof ObjectType && ptype instanceof ObjectType) {
		    		a = widenObjectType((ObjectType)ptype, a, ae.getStop()); 
		    	} else if (atype.equals(ptype)) {
		    	} else if (atype != Type.errorType) {
		    		Main.error(ae.getStop(),"argument type "+atype+" not compatible with parameter "+ptype);
		    	}
				args.add(a);
		    }
        }
        
        // emit the call
		CodeReg rv = code.emitCall(rtype, mp.getName(), args);
        
		return rv;
	}
    
    static String makeMethodName(MethodSymbol msym) {
    	ClassSymbol defScope = (ClassSymbol)msym.getDefiningScope();
     	StringBuilder n = new StringBuilder();
     	n.append(classPrefix(CodeGen.GLOBAL_PREFIX,defScope)).append(NAME_SEPARATOR).append(escapeName(msym.getName()));
     	for (Type pt : msym.getParameterTypes()) {
     		n.append(PARAMETER_SEPARATOR);
 			appendParameterType(n,pt);
       	}
		return n.toString();
	}

	private static void appendParameterType(StringBuilder n, Type pt) {
 		if (pt instanceof ArrayType) {
 			appendParameterType(n,((ArrayType) pt).getElement());
 			n.append(ARRAY_PARAMETER);
 		} else {
 			n.append(escapeName(pt.getName()));
 		}
	}

	private CodeReg widenNumericType(Type max, CodeReg r, Token op) {
    	if (max == Type.errorType || r.getType() == Type.errorType) return r;
    	if (r.isPointer()) r = code.emitLoad(r);
    	Type rtype = r.getType();
		if (rtype == max) return r;  // no widening if already required type
    	// LATER unboxing
		if (rtype == Type.errorType) return r;
		if (max instanceof ArrayType && rtype instanceof ArrayType
			&& ((ArrayType)max).getElement() == ((ArrayType)rtype).getElement()) return r;
    	if (   (max == Type.floatType || max == Type.doubleType)
    		&& rtype == Type.intType) {
    		return code.emitConvert(r,CodeGen.SIGNED_TO_FLOATING,max);
     	}
    	if (   max == Type.doubleType
        		&& rtype == Type.floatType) {
        		return code.emitConvert(r,CodeGen.FLOATING_EXTEND,max);
        }
    	if (   max == Type.intType
            	&& rtype == Type.charType) {
    		return code.emitConvert(r,CodeGen.BITCAST,max);  // no-op, since int and char representations are both i32
        }
    	if (   (max == Type.intType || max == Type.longType)
        	&& rtype == Type.charType) {
    		return code.emitConvert(r,CodeGen.ZERO_EXTEND,max);
        }
    	if (   (max == Type.longType || max == Type.intType)
            && (rtype == Type.intType || rtype == Type.shortType || rtype == Type.byteType)) {
    			return code.emitConvert(r,CodeGen.SIGN_EXTEND,max);
        }
    	Main.error(op,rtype+" operand cannot be widened to "+max);
    	return CodeReg.ERROR_VALUE;
    }
	
	private CodeReg widenObjectType(ObjectType max, CodeReg r, Token op) {
    	if (max == Type.errorType || r.getType() == Type.errorType) return r;
    	if (r.isPointer()) r = code.emitLoad(r);
    	Type rtype = r.getType();
		if (rtype == max) return r;  // no widening if already required type
		if (rtype == Type.errorType) return r;
		//TODO ArrayType
		if (rtype instanceof ObjectType) {
			if (rtype.equals(max)) return r;
			ObjectType rt = (ObjectType)rtype;
			if (max.isSupertypeOf(rtype)) {
				return code.emitConvert(r,CodeGen.BITCAST,max);
			}
		}
		Main.error(op, rtype+" is not a subtype of "+max);
    	return CodeReg.ERROR_VALUE;
    }


	@Override
	public CodeReg visitMinusExpr(P10aParser.MinusExprContext ctx) {
		// '-' NL* expression
    	P10aParser.ExpressionContext expb = ctx.expression();
    	CodeReg rb = deref(expb);
    	Token opToken = ((TerminalNode)ctx.getChild(0)).getSymbol();
    	Type type = rb.getType();
    	CodeReg ra = (type == Type.floatType) ? CodeReg.FLOAT_ZERO
    			   : (type == Type.doubleType) ? CodeReg.DOUBLE_ZERO
    			   : (type == Type.longType) ? CodeReg.LONG_ZERO
    			   : CodeReg.ZERO;
    	rb = widenNumericType(type, rb, opToken);
		CodeReg r = code.emitDyadic(type, opToken.getText(), ra, rb);
		return r;
	}

	@Override
	public CodeReg visitNotExpr(P10aParser.NotExprContext ctx) {
		// ( '~' | '!' ) NL* expression
    	P10aParser.ExpressionContext expb = ctx.expression();
    	CodeReg rb = deref(expb);
    	Token opToken = ((TerminalNode)ctx.getChild(0)).getSymbol();
    	if (opToken.getText().equals("~")) {
        	Type type = rb.getType();
        	if (type != Type.longType) type = Type.intType;
        	rb = widenNumericType(type, rb, opToken);
        	CodeReg ra = CodeReg.MINUS_ONE;
    		CodeReg r = code.emitDyadic(type, opToken.getText(), ra, rb);
    		return r;
    	} else {
			checkBoolean(rb,expb.getStop());
	    	CodeReg ra = CodeReg.TRUE;
			CodeReg r = code.emitDyadic(Type.booleanType, opToken.getText(), ra, rb);
			return r;
    	}
	}

	private CodeReg dyadicAithmeticOperation(P10aParser.ExpressionContext ctx, List<P10aParser.ExpressionContext> list, ParseTree opNode) {
		Iterator<P10aParser.ExpressionContext> expit = list.iterator();
    	P10aParser.ExpressionContext expa = expit.next();
    	P10aParser.ExpressionContext expb = expit.next();
    	CodeReg ra = deref(expa);
    	if (ra == null) return null;
    	CodeReg rb = deref(expb);
    	if (rb == null) return null;
    	Token opToken = ((TerminalNode)opNode).getSymbol();
    	Type type = Type.widestNumericType(ra.getType(),rb.getType());
    	ra = widenNumericType(type, ra, opToken);
    	rb = widenNumericType(type, rb, opToken);
		CodeReg r = code.emitDyadic(type, opToken.getText(), ra, rb);
		return r;
	}
    
	@Override
    public CodeReg visitMultiplyExpr(P10aParser.MultiplyExprContext ctx) {
		// expression ( '*' | '/' | '%' ) NL* expression
    	return dyadicAithmeticOperation(ctx, ctx.expression(), ctx.getChild(1));
    }
    
	@Override
    public CodeReg visitAddExpr(P10aParser.AddExprContext ctx) {
		// expression ( '+' | '-' ) NL* expression
    	return dyadicAithmeticOperation(ctx, ctx.expression(), ctx.getChild(1));
    }

	@Override
    public CodeReg visitShiftExpr(P10aParser.ShiftExprContext ctx) {
		// 	expression ( '<<' | '>>' | '>>>' ) NL* expression
		// llc requires matching operand types, despite shift amount is a small integer
    	return dyadicAithmeticOperation(ctx, ctx.expression(), ctx.getChild(1));
    }

	@Override
    public CodeReg visitBitAndExpr(P10aParser.BitAndExprContext ctx) {
		// expression '&' NL* expression
    	return dyadicAithmeticOperation(ctx, ctx.expression(), ctx.getChild(1));
    }
    
	@Override
    public CodeReg visitBitExclExpr(P10aParser.BitExclExprContext ctx) {
		// expression '^' NL* expression
    	return dyadicAithmeticOperation(ctx, ctx.expression(), ctx.getChild(1));
    }
    
	@Override
    public CodeReg visitBitOrExpr(P10aParser.BitOrExprContext ctx) {
		// expression '|' NL* expression
    	return dyadicAithmeticOperation(ctx, ctx.expression(), ctx.getChild(1));
    }
    
	@Override
	public CodeReg visitCastExpr(P10aParser.CastExprContext ctx) {
		// expression 'as' NL* type
    	P10aParser.ExpressionContext expa = ctx.expression();
    	P10aParser.TypeContext typ = ctx.type();
    	CodeReg ra = deref(expa);
    	if (ra == null) return null;
    	visit(typ);
    	Type vtype = ra.getType();
    	Type rtype = typ.tipe;
		if (vtype == rtype) return ra;  // no conversion if already required type
		if (rtype == Type.errorType) return ra;  // short exit if error
    	// LATER boxing/unboxing
		// widenings
    	if (   (rtype == Type.floatType || rtype == Type.doubleType)
    		&& vtype == Type.intType) {
    		return code.emitConvert(ra,CodeGen.SIGNED_TO_FLOATING,rtype);
     	}
    	if (   rtype == Type.doubleType
       		&& vtype == Type.floatType) {
        		return code.emitConvert(ra,CodeGen.FLOATING_EXTEND,rtype);
        }
    	if (   rtype == Type.longType
        	&& vtype == Type.charType) {
    		return code.emitConvert(ra,CodeGen.ZERO_EXTEND,rtype);
        }
    	if (   rtype == Type.longType
        && (vtype == Type.intType || vtype == Type.shortType || vtype == Type.byteType)) {
			return code.emitConvert(ra,CodeGen.SIGN_EXTEND,rtype);
        }
    	if (   rtype == Type.charType
    		&& (vtype == Type.shortType || vtype == Type.byteType)) {
			return code.emitConvert(ra,CodeGen.ZERO_EXTEND,rtype);
        }
		// narrowings
    	if (   (rtype == Type.byteType || rtype == Type.shortType || rtype == Type.intType || rtype == Type.longType)
    		&& (vtype == Type.floatType || vtype == Type.doubleType) ) {
    		return code.emitConvert(ra,CodeGen.FLOATING_TO_SIGNED,rtype);
     	}
    	if (   rtype == Type.intType
           	&& vtype == Type.charType) {
    		return code.emitConvert(ra,CodeGen.BITCAST,rtype);  // no-op, since int and char representations are both i32
        }
    	if (   rtype == Type.charType
           	&& vtype == Type.intType) {
    		return code.emitConvert(ra,CodeGen.BITCAST,rtype); // ignore sign
        }
    	if (   (rtype == Type.byteType || rtype == Type.shortType || rtype == Type.intType || rtype == Type.charType)
    	    && vtype == Type.longType) {
    		return code.emitConvert(ra,CodeGen.TRUNCATE,rtype);
     	}
    	if (   (rtype == Type.byteType || rtype == Type.shortType)
    	    && vtype == Type.intType) {
    		return code.emitConvert(ra,CodeGen.TRUNCATE,rtype);
     	}
    	if (   rtype == Type.byteType
    	    && vtype == Type.shortType) {
    		return code.emitConvert(ra,CodeGen.TRUNCATE,rtype);
     	}
    	if (   rtype == Type.byteType
        	    && vtype == Type.charType) {
        		return code.emitConvert(ra,CodeGen.TRUNCATE,rtype);
        }
    	if (rtype instanceof ObjectType) {
    		return widenObjectType((ObjectType)rtype,ra,expa.getStop());
		}
    	//TODO other class raising and lowering
    	Token opToken = getChildToken(ctx,1);
    	Main.error(opToken,vtype+" operand cannot be cast to "+rtype);
    	return CodeReg.ERROR_VALUE;
	}

	@Override
    public CodeReg visitCompareExpr(P10aParser.CompareExprContext ctx) {
		// expression ( '=' | '!=' | '≠' | '<=' | '≤' | '>=' | '≥' | '<' | '>' ) NL* expression
    	List<P10aParser.ExpressionContext> list = ctx.expression();
		Iterator<P10aParser.ExpressionContext> expit = list.iterator();
    	P10aParser.ExpressionContext expa = expit.next();
    	P10aParser.ExpressionContext expb = expit.next();
    	CodeReg ra = deref(expa);
    	CodeReg rb = deref(expb);
    	Token opToken = getChildToken(ctx,1);
    	String op = opToken.getText();
    	if (op.equals("=") || op.equals("!=") || op.equals("≠")) {
    		Type at = ra.getType(), bt = rb.getType();
    		if (at instanceof Type.Reference && bt instanceof Type.Reference) {
    			//TODO is a cast required for reference comparisons?
    		} else if (at == Type.booleanType && bt == Type.booleanType) {
    		} else {
				Type type = Type.widestNumericType(ra.getType(),rb.getType());
				ra = widenNumericType(type, ra, opToken);
				rb = widenNumericType(type, rb, opToken);
    		}
    	} else {
			Type type = Type.widestNumericType(ra.getType(),rb.getType());
			ra = widenNumericType(type, ra, opToken);
			rb = widenNumericType(type, rb, opToken);
    	}
		CodeReg r = code.emitDyadic(Type.booleanType, opToken.getText(), ra, rb);
		return r;
    }
    
	@Override
    public CodeReg visitAndThenExpr(P10aParser.AndThenExprContext ctx) {
		// expression '&&' NL* expression
    	P10aParser.ExpressionContext expa = ctx.expression(0);
    	P10aParser.ExpressionContext expb = ctx.expression(1);
    	CodeReg ra = deref(expa);
		checkBoolean(ra,expa.getStop());
		/* clang model
		  	  %3 = load i32, i32* %1, align 4
		  	  %4 = icmp ne i32 %3, 0
		  	  br i1 %4, label %5, label %8
		  	; <label>:5                                       ; preds = %0
		  	  %6 = load i32, i32* %2, align 4
		  	  %7 = icmp ne i32 %6, 0
		  	  br label %8
		  	; <label>:8                                       ; preds = %5, %0
		  	  %9 = phi i1 [ false, %0 ], [ %7, %5 ]
 		*/
    	String pb = code.currentBlock;
		String lx=code.nextLabel(), lThen="then"+lx, lAnd="and"+lx;
    	code.emitBranchBool(ra,lThen,lAnd);
    	code.emitLabel(lThen);
    	CodeReg rb = deref(expb);
		checkBoolean(rb,expb.getStop());
		String thenBlock = code.currentBlock;
    	code.emitBranchUnc(lAnd);
    	code.emitLabel(lAnd);
    	CodeReg r = code.emitJoin(CodeReg.FALSE, pb, rb, thenBlock, "visitAndThenExpr("+lThen+")");
		return r;
    }
    
	@Override
    public CodeReg visitOrElseExpr(P10aParser.OrElseExprContext ctx) {
		// expression '||' NL* expression
    	P10aParser.ExpressionContext expa = ctx.expression(0);
    	P10aParser.ExpressionContext expb = ctx.expression(1);
     	CodeReg ra = deref(expa);
		checkBoolean(ra,expa.getStop());
		/* clang model
			  %3 = load i32, i32* %1, align 4
			  %4 = icmp ne i32 %3, 0
			  br i1 %4, label %8, label %5
			; <label>:5                                       ; preds = %0
			  %6 = load i32, i32* %2, align 4
			  %7 = icmp ne i32 %6, 0
			  br label %8
			; <label>:8                                       ; preds = %5, %0
			  %9 = phi i1 [ true, %0 ], [ %7, %5 ]
 		*/
    	String pb = code.currentBlock;
		String lx=code.nextLabel(), lElse="else"+lx, lOr="or"+lx;
    	code.emitBranchBool(ra,lOr,lElse);
    	code.emitLabel(lElse);
    	CodeReg rb = deref(expb);
		checkBoolean(rb,expb.getStop());
		String elseBlock = code.currentBlock;
    	code.emitBranchUnc(lOr);
    	code.emitLabel(lOr);
    	CodeReg r = code.emitJoin(CodeReg.TRUE, pb, rb, elseBlock, "visitOrElseExpr("+lElse+")");
		return r;
    }
    
	@Override
	public CodeReg visitAssignExpr(P10aParser.AssignExprContext ctx) {
		// expression ( ':' | '+:' | '-:' | '*:' | '/:' | '&:' | '|:' | '^:'
		//            | '>>:' | '>>>:' | '<<:' | '%:' ) NL* expression
		P10aParser.ExpressionContext ref = ctx.expression(0);
    	P10aParser.ExpressionContext exp = ctx.expression(1);
		CodeReg rr = visit(ref);
		if (trace) Main.debug("visitAssignStmt %s %s %s", ref.getText(), ref.defn, rr==null?"null":rr.typeAndName());
		CodeReg rb = deref(exp);
		Token opToken = getChildToken(ctx,1);
		String op = opToken.getText();
		if (op.equals(":")) {
			doAssign(ctx,rr,rb,opToken);
		} else {
			//Main.toDo(opToken, opToken+" assignment not yet implemented");
	    	CodeReg ra = deref(ref);
	    	Type type = Type.widestNumericType(ra.getType(),rb.getType());
	    	ra = widenNumericType(type, ra, opToken);
	    	rb = widenNumericType(type, rb, opToken);
			CodeReg r = code.emitDyadic(type, op.substring(0,op.length()-1), ra, rb);
			doAssign(ctx,rr,r,opToken);
			rb = r;
		}
		return rb;
	}
   
    public CodeReg deref(P10aParser.ExpressionContext exp) {
        CodeReg rr = visit(exp);
        if (trace) Main.debug("deref %s %s %s", exp.getText(), exp.defn==null?"null":exp.defn, rr==null?"null":rr.typeAndName());
    	if (exp.defn == null) {
        	return rr;  // value
    	} else if (exp.defn.getType() == Type.errorType){
        	return rr;  // error value
    	} else if (exp.defn instanceof FieldSymbol) {
    		if (rr.isPointer()) return code.emitLoad(rr);
    		return rr;
    	} else if (exp.defn instanceof ClassSymbol) {
    		if (trace) Main.debug("Class "+exp.getText()+" deref "+rr);
    		// TODO confirm this is valid
    		return CodeReg.newName(exp.defn.getType(), classPrefix(CodeGen.GLOBAL_PREFIX, (ClassSymbol) exp.defn));
    	} else {
    		Main.error(exp.getStop(), exp.getText()+" is not a value");
    		return CodeReg.ERROR_VALUE;
        }
    }
/*
														CodeReg
primary													type	pointer
	'(' NL* expression ')'				# parenPrim		(expr)	(expr)	
	'this'								# thisPrim		(implicit) value
	'super'								# superPrim
	Integer								# integerPrim	int		constant
	Float								# floatPrim		float	constant
	Character							# characterPrim	char	constant
	String								# stringPrim	string	constant
	'null'								# nullPrim		null	constant
	'true'								# truePrim		boolean	constant
	'false'								# falsePrim		boolean	constant
	Identifier							# idPrim		(defn)	pointer
expression
	primary								# primExpr		(prim)	value
	expression '.' Identifier			# memberExpr	(defn)	pointer
	expression '[' NL* expression ']'	# indexExpr		(elem)	pointer
	expression '(' NL* argumentList? ')'# callExpr		(defn)	value
	( '+' | '-' ) NL* expression		# minusExpr		(expr)	value
	( '~' | '!' ) NL* expression		# notExpr		(expr)	value
	expression ( '*' | '/' | '%' ) expression	# multiplyExpr	(expr)	value
	expression ( '+' | '-' ) expression	# addExpr		(expr)	value
	expression ( '<<' | '>>' | '>>>' ) expression	# shiftExpr	(expr)	value
	expression '&' expression			# bitAndExpr	(expr)	value
	expression '^' expression			# bitExclExpr	(expr)	value
	expression '|' expression			# bitOrExpr		(expr)	value
	expression 'as' type				# castExpr		(type)	(expr)
	expression ( '=' | '!=' | '≠' | '<=' | '≤' | '>=' | '≥'| '<' | '>' ) expression	# compareExpr	boolean	value
	expression '&&' expression			# andThenExpr	boolean	value
	expression '||' expression			# orElseExpr	boolean	value
	expression ( ':' | '+:' | '-:' | '*:' | '/:'
	           | '&:' | '|:' | '^:' | '>>:' | '>>>:' | '<<:'
	           | '%:' ) NL* expression	# assignExpr
*/

	@Override
	public CodeReg visitParenPrim(P10aParser.ParenPrimContext ctx) {
    	// '(' NL* expression ')'
		P10aParser.ExpressionContext e = ctx.expression();
        CodeReg r = visit(e);
        return r;
    }

	@Override
    public CodeReg visitThisPrim(P10aParser.ThisPrimContext ctx) {
		// 'this'
    	MethodSymbol m = getEnclosingMethod(ctx);
    	if (m == null || m.isStatic()) {
    		Main.error(ctx.getStart(),"'this' is not defined in a static method");
        	return CodeReg.ERROR_VALUE;
    	} else {
			FieldSymbol fsym = m.getImplicitParameter();
    		ctx.defn = fsym;
		    Type ftype = fsym.getType();
		    return CodeReg.newName(ftype,ARG_THIS);
    	}
	}

	@Override
	public CodeReg visitSuperPrim(P10aParser.SuperPrimContext ctx) {
		// 'super'
		Main.toDo(ctx.getStart(), "not yet implemented");
		return CodeReg.ERROR_VALUE;
		/*
		CodeReg rr;
		Token id = ctx.Identifier().getSymbol();
		if (trace) Main.debug("visitSuperRefer super.%s", id.getText());
    	MethodSymbol m = getEnclosingMethod(ctx);
    	if (m == null || m.isStatic()) {
    		Main.error(ctx.getStart(),"'super' is not defined in a static method");
        	return CodeReg.ERROR_VALUE;
    	} else {
			FieldSymbol t = m.getImplicitParameter();
		    Type ttype = t.getType();
		    rr = CodeReg.newName(ttype,ARG_THIS);

		    ClassSymbol superClass = ((ObjectType)ttype).getKlass().getSuperClass();
		    Type scType = (superClass == null) ? Type.errorType : superClass.getType();
		    rr = code.emitConvert(rr,CodeGen.BITCAST,scType);
			return memberRefer(ctx, rr, scType, superClass, id);
    	}
    	*/
	}

	@Override
	public CodeReg visitIntegerPrim(P10aParser.IntegerPrimContext ctx) {
    	// Integer
    	String text = ctx.Integer().getText();
    	CodeReg r = makeIntegerConstant(text);
		return r;
    }

	private CodeReg makeIntegerConstant(String text) {
		if (text.endsWith("l") || text.endsWith("L")) {
    		text = text.substring(0,text.length()-1);
    		if (text.startsWith("0x") || text.startsWith("0X")) text = Long.toString(Long.decode(text)); // can't pass hexadecimal integer to llc
			CodeReg r = CodeReg.newConstant(Type.longType, text);
	        return r;
    	} else {
    		if (text.startsWith("0x") || text.startsWith("0X")) text = Integer.toString(Integer.decode(text)); // can't pass hexadecimal integer to llc
			CodeReg r = CodeReg.newConstant(Type.intType, text);
	        return r;
    	}
	}
    
	@Override
	public CodeReg visitFloatPrim(P10aParser.FloatPrimContext ctx) {
    	// Float
    	String text = ctx.Float().getText();
    	if (text.endsWith("d") || text.endsWith("D")) {
			CodeReg r = CodeReg.newConstant(Type.doubleType, text.substring(0,text.length()-1));
	        return r;
    	} else {
			CodeReg r = CodeReg.newConstant(Type.floatType, text);
	        return r;
    	}
	}

	@Override
	public CodeReg visitCharacterPrim(P10aParser.CharacterPrimContext ctx) {
		// Character
    	String text = ctx.Character().getText();
		CodeReg r = makeCharacterConstant(text);
        return r;
	}

	private CodeReg makeCharacterConstant(String text) {
		String v = Integer.toString(text.charAt(1));
    	CodeReg r = CodeReg.newConstant(Type.charType,v);
		return r;
	}

	@Override
    public CodeReg visitStringPrim(P10aParser.StringPrimContext ctx) {
    	// String
		Token st = ctx.String().getSymbol();
    	String text = st.getText();
    	UTF8Bytes utf8 = new UTF8Bytes(text.length()*2);
    	// drop quotes and decode escapes
    	for (int x = 1; x < text.length()-1; x += 1) {
    		int c = text.charAt(x);
    		if (c == '\\') {
    			int y = x, h, d;
    			c = text.charAt(++y);
    			switch (c) {
    			case '"':
    			case '\'':
    			case '\\':
    				/*as-is*/  break;
    			case 'b':
    				c = '\b';  break;
    			case 't':
    				c = '\t';  break;
    			case 'n':
    				c = '\n';  break;
    			case 'f':
    				c = '\f';  break;
    			case 'r':
    				c = '\r';  break;
    			case 'x':
    				c = 0;
    				while (y < text.length()-2 && y-x < 3) {
    					h = text.charAt(++y);
    					d = Character.digit(h, 16);
    					if (d < 0) break; // not a hexadecimal digit
						c = (c << 4) + d;
    				}
    				break;
    			case 'u':
    				c = 0;
    				while (y < text.length()-2 && y-x < 5) {
    					h = text.charAt(++y);
    					d = Character.digit(h, 16);
    					if (d < 0) break; // not a hexadecimal digit
						c = (c << 4) + d;
    				}
    				break;
    			case 'U':
    				c = 0;
    				while (y < text.length()-2 && y-x < 9) {
    					h = text.charAt(++y);
    					d = Character.digit(h, 16);
    					if (d < 0) break; // not a hexadecimal digit
						c = (c << 4) + d;
    				}
    				break;
    			default:
    				// no value, should not occur
    				throw new IllegalArgumentException("unrecognized escape code \\"+c);
    			}
    			x = y;
    		} else if (Character.isHighSurrogate((char)c)
    				   && x < text.length()-2
    				   && Character.isLowSurrogate(text.charAt(x+1))) {
    			c = Character.toCodePoint((char)c, text.charAt(x+1));
    			x += 1;
    		}
    		if (c < 0x110000) {
        		utf8.append(c);
    		} else  {
				Main.error(st, "invalid Unicode \\U"+Integer.toHexString(c));
    		}
    	}
    	CodeReg r = CodeReg.newConstant(stringType, stringID(utf8));
    	return r;
    }
	    
	@Override
    public CodeReg visitNullPrim(P10aParser.NullPrimContext ctx) {
    	// 'null'
    	CodeReg r = CodeReg.NULL;
        return r;
    }
    
	@Override
    public CodeReg visitTruePrim(P10aParser.TruePrimContext ctx) {
    	// 'true'
    	CodeReg r = CodeReg.TRUE;
        return r;
    }
    
	@Override
    public CodeReg visitFalsePrim(P10aParser.FalsePrimContext ctx) {
    	// 'false'
    	CodeReg r = CodeReg.FALSE;
        return r;
    }
	
	@Override
    public CodeReg visitIdPrim(P10aParser.IdPrimContext ctx) {
    	// Identifier
     	Token id = ctx.Identifier().getSymbol();
 		ctx.defn = ctx.refScope.find(id);
    	if (ctx.defn == null) {
    		Main.error(id,id.getText()+" is not defined");
        	return CodeReg.ERROR_VALUE;
    	} else if (ctx.defn.getType() == Type.errorType){
        	return CodeReg.ERROR_VALUE;
    	} else {
    		Scope defScope = ctx.defn.getDefiningScope();
    		if (ctx.defn instanceof FieldSymbol) {
    			FieldSymbol fsym = (FieldSymbol)ctx.defn;
    		    Type ftype = fsym.getType();
    	        if (defScope instanceof ClassSymbol) {
    	        	ClassSymbol defClass = (ClassSymbol)defScope;
    	        	if (fsym.isStatic()) {
    	     			String pfx = classPrefix(CodeGen.GLOBAL_PREFIX,defClass)+NAME_SEPARATOR;
    	     			if (trace) Main.debug("static field "+fsym);
    					String varn = pfx+fsym.getName();
    				    return CodeReg.newPointer(ftype,varn);
    	        	} else {
    	            	// Find enclosing method
    	        		P10aParser.MethodDeclContext md = (P10aParser.MethodDeclContext)findAncestor(ctx, P10aParser.MethodDeclContext.class);
    	        		if (md != null && md.defn.isStatic()) {
    	        			Main.error(id, "Instance field "+id.getText()+" cannot be referenced in a static method "+md.defn.getName());
    	        			return CodeReg.ERROR_VALUE;
    	        		}
    	        		if (trace) Main.debug("field "+fsym);
    	    			ObjectType objType = (ObjectType)defClass.getType();
   	    				CodeReg t = CodeReg.newName(objType,ARG_THIS);
    			    	return code.emitFieldIndex(ftype,t,fsym.getIndex(),fsym.getName());
    	        	}
    	        } else if (defScope instanceof MethodSymbol) {
    	        	if (trace) Main.debug("parameter "+fsym);
    				String varn = VAR_PREFIX+fsym.getName();
    			    return CodeReg.newPointer(ftype,varn);
    	        } else {
    	        	assert defScope instanceof BlockScope;
    	        	if (trace) Main.debug("variable "+fsym);
    	    		// token index of definition must be before token index of reference
    	            if ( id.getTokenIndex() < ctx.defn.getToken().getTokenIndex() ) {
    	            	Main.error(id, "forward reference to local variable "+id.getText());
    	            	return CodeReg.ERROR_VALUE;
    	            }
    				String varn = VAR_PREFIX+fsym.getName()+"."+fsym.getSeq();
    			    return CodeReg.newPointer(ftype,varn);
    	        }
    		} else if (ctx.defn instanceof MethodSymbol) {
		        if (defScope instanceof ClassSymbol) {
	    			// Note method selection must wait for callExpr so overloading can be resolved
	    			return null;
				} else {
					// should not happen
					Main.error(id, "method defined in non-class scope "+defScope);
		        }
       		} else if (ctx.defn instanceof ClassSymbol) {
       			// members must be static
       			return null;
    		} else {
				Main.error(id, "unexpected reference "+ctx.defn);
    		}
    	}
        return CodeReg.ERROR_VALUE;
    }

	private MethodSymbol getEnclosingMethod(P10aParser.PrimaryContext ctx) {
		Scope s = ctx.refScope;
		while (s != null && !(s instanceof MethodSymbol)) s = s.getEnclosingScope();
    	MethodSymbol m = (s == null) ? null : (MethodSymbol)s;
		return m;
	}

}

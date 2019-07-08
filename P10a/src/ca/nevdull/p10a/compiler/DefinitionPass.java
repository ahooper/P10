/*	DefinitionPass.java
 
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class DefinitionPass extends P10aBaseVisitor<Void> {
	Main main;
    static final String THIS = "this";
    static final String IMPORT_SUFFIX = ".imp";
    static final String TEMPORARY_SUFFIX = ".tmp";
    static final String SOURCE_SUFFIX = ".p";
	private boolean trace;
	private PrintWriter impOut;
	Scope currentScope;
	/*static*/ Scope topScope = null;
			//LATER maybe this can be shared across subcompiles
	ArrayList<ClassSymbol> imports = new ArrayList<ClassSymbol>();
	static final String[] std_lang = {"std","lang"};
    static final String STD_LANG_IMPORTS_OBJECT = "std.lang.Object";
    static final String STD_LANG_IMPORTS_CLASS = "std.lang.Class";
    static final String STD_LANG_IMPORTS_ARRAY = "std.lang.Array";
    static final String STD_LANG_IMPORTS_STRING = "std.lang.String";
	static final String[] CODEGEN_REQUIRED_IMPORTS = {
				// CodeGen assumes these are defined in the LLVM prologue
				 STD_LANG_IMPORTS_OBJECT
				,STD_LANG_IMPORTS_CLASS
				,STD_LANG_IMPORTS_ARRAY
				,STD_LANG_IMPORTS_STRING
				,"std.lang.Throw"
				};
	private ClassSymbol std_lang_Object = null;

	private static final ArrayList<Type> EMPTY_TYPE_LIST = new ArrayList<Type>(0);
	private File impOutName, impFinalName;

	public DefinitionPass(Main main, String sourceFilePath, String sourceFileName) throws FileNotFoundException, UnsupportedEncodingException {
		this.main = main;
    	trace = main.trace.contains("DefinitionPass");
    	//if (trace) ClassSymbol.trace = true;
Main.debug(sourceFilePath+" "+sourceFileName);		
		// directory list for standard imports
    	if (topScope == null) topScope = new Scope("top", null);
    	currentScope = topScope;
		String sls = String.join(File.separator, std_lang);
		String[] fqn = new String[std_lang.length+1];
		System.arraycopy(std_lang, 0, fqn, 0, std_lang.length);
		for (String cps : main.classPath) {
			File slp = new File(cps, sls);
			String[] list = slp.list();
			if (list == null) continue; // not a directory
			for (String f : list) {
				if (f.endsWith(IMPORT_SUFFIX)) {
					//if (trace) Main.debug(slp.toString()+" "+f);
					String name = f.substring(0,f.length()-IMPORT_SUFFIX.length());  // drop suffix to get class name
					fqn[std_lang.length] = name;
			        String fullName = buildQualifiedName(fqn);
					addImport(name, fullName, sls);
				}
			}
		}
		for (String req : CODEGEN_REQUIRED_IMPORTS) {
			int x = req.lastIndexOf('.');
			String shortName = req.substring(x+1);
			addImport(shortName, req, "REQUIRED_IMPORTS");
		}
		
		// Begin the export summary of this class, after determining what is available to import

    	impOutName = new File(main.outputDirectory,Main.dropFileNameSuffix(sourceFileName)+TEMPORARY_SUFFIX);
    	impFinalName = new File(main.outputDirectory,Main.dropFileNameSuffix(sourceFileName)+IMPORT_SUFFIX);
    	impOutName.delete(); // ignore failure return if file does not exist
    	impFinalName.delete(); // ignore failure return if file does not exist
	    File parent = impOutName.getParentFile();
		if (parent!=null) parent.mkdirs();
		impOut = new PrintWriter(impOutName, "UTF-8");
		impOut.print("// source_filename = \"");
		impOut.print(sourceFileName.replace("\"","\\22"));
		impOut.print("\" ; ");
		impOut.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	}

	private ClassSymbol addImport(Token shortName, String fullName, String debugText) {
		ClassSymbol csym = declareClass(shortName, fullName, debugText);
		imports.add(csym);
		return csym;
	}

	private ClassSymbol addImport(String shortName, String fullName, String debugText) {
		// same as above, except no source token available for shortName (std/lang members)
		ClassSymbol csym = declareClass(shortName, fullName, debugText);
		imports.add(csym);
		return csym;
	}

	private ClassSymbol declareClass(Token shortName, String fullName, String debugText) {
        Symbol f = topScope.find(fullName);
        if (trace) {
			Main.debug(currentScope.getName()+" declareClass "+shortName+" "+fullName+" f="+f+" ("+debugText+")");
			debugShowCurrentScope();
        }
		if (f != null) { // previously seen
			if (currentScope != topScope) currentScope.add(f);
			return (ClassSymbol) f; // previously seen 
		}
		ClassSymbol csym = new ClassSymbol(shortName, topScope);
		csym.setFullName(fullName);
		csym.setStatic(true);
		// isLoaded() still false
		currentScope.add(csym);
		topScope.putAlias(csym, csym.getFullName());
		return csym;
	}

	private ClassSymbol declareClass(String shortName, String fullName, String debugText) {
		// same as above, except no source token available for shortName
        Symbol f = topScope.find(fullName);
        if (trace) Main.debug(currentScope.getName()+" declareClass "+shortName+" "+fullName+" f="+f+" ("+debugText+")");
		if (f != null) { // previously seen
			if (currentScope != topScope) currentScope.add(f);
			return (ClassSymbol) f; // previously seen 
		}
		ClassSymbol csym = new ClassSymbol(shortName, topScope);
		csym.setFullName(fullName);
		csym.setStatic(true);
		currentScope.add(csym);
		topScope.putAlias(csym, csym.getFullName());
		return csym;
	}

	private String buildQualifiedName(String[] names) {
		return String.join(".",names);
	}
	
	String buildQualifiedName(List<TerminalNode> identifiers) {
		StringBuilder cname = new StringBuilder();
		String s = "";
    	for (TerminalNode nameComponent : identifiers) {
    		String nc = nameComponent.getText();
    		cname.append(s).append(nc); s = ".";
    	}
    	return cname.toString();
	}

	private ClassSymbol addClassReference(P10aParser.QualifiedNameContext qualifiedName, Token impToken) {
		List<TerminalNode> nameComponents = qualifiedName.Identifier();
		Token shortName = nameComponents.get(nameComponents.size()-1).getSymbol();
		String fullName = buildQualifiedName(nameComponents);
		ClassSymbol csym = addImport(shortName, fullName, qualifiedName.getText());
		csym.setReferenced(true);
		if (impToken != null) {
			impOut.print(impToken.getText());
			impOut.print(" ");
			impOut.println(fullName);
		}
		return csym;
	}

	@Override
	public Void visitFile(P10aParser.FileContext ctx) {
		// NL* 'class' qualifiedName NL+ ( extendDecl NL+ )? ( interfaceDecl NL+ )* ( member NL+ )* EOF
    	List<TerminalNode> identifiers = ctx.qualifiedName().Identifier();
    	String fullName = buildQualifiedName(identifiers);
		TerminalNode lastName = identifiers.get(identifiers.size()-1);
		
		// Defining mutually referencing classes, like Object and Class, is tricky.
		// Essentially this does a "forward" declaration of the class as an empty shell,
		// then declares and defines ("loads") referenced classes, then completes the
		// definition in the shell.
		// Only extends and interface references need be loaded before compiling this
		// class. import references can remain empty shells until the end of the pass.
    	
		ClassSymbol csym = declareClass(lastName.getSymbol(), fullName, "visitFile");
assert !csym.isLoading();
		currentScope = ctx.defn = csym;
		csym.setLoading(true); // class is now defined
		csym.setToken(lastName.getSymbol());  // link token for DebugInfo
		
		// explicitly visit parse children, so action can be made after the extend
		P10aParser.ExtendDeclContext ext = ctx.extendDecl();
		if (ext != null) {
			visitExtendDecl(ext);
			loadImport(ext.defn);
			csym.setSuperClass(ext.defn);
		} else if (fullName.equals(STD_LANG_IMPORTS_OBJECT)) {
			// Object superClass is set to null
			csym.setSuperClass(null);
			std_lang_Object = csym;
		} else {
			if (std_lang_Object == null) {
				std_lang_Object = (ClassSymbol) topScope.find(STD_LANG_IMPORTS_OBJECT);
				loadImport(std_lang_Object);
			}
			csym.setSuperClass(std_lang_Object);
		}
		List<P10aParser.InterfaceDeclContext> interfaceDecls = ctx.interfaceDecl();
		for (P10aParser.InterfaceDeclContext ifc : interfaceDecls) visit(ifc);
		
		List<P10aParser.MemberContext> members = ctx.member();
		for (P10aParser.MemberContext mem : members) visitMember(mem);
		
		if (trace) Main.debug(currentScope.getName()+" members "+currentScope.getMembers().values());
        currentScope = currentScope.close();		
		impOut.close();
		if (trace) Main.debug("visitFile rename "+impOutName+" "+impOutName.exists()+" "+impFinalName+" "+impFinalName.exists());
		if (!impOutName.renameTo(impFinalName)) {
			Main.error("Could not rename to "+impFinalName+" length="+impFinalName.length()
						+" age="+(System.currentTimeMillis()-impFinalName.lastModified())/1000f);
		}
		if (trace) Main.debug(currentScope.getName()+" members "+currentScope.getMembers().keySet().toString());
		csym.setLoaded(true); // members have now been defined

		// remaining class references can be loaded now
		for (int i = 0; i < imports.size(); i++) {
			// imports may grow while iterating
			ClassSymbol imp = imports.get(i);
			loadImport(imp);
		}

		return null;
	}

	@Override
	public Void visitExtendDecl(P10aParser.ExtendDeclContext ctx) {
		// 'extends' qualifiedName
		ctx.defn = addClassReference(ctx.qualifiedName(), ctx.getStart());
		// superclass is set in visitFile
		return null;    	
	}

	@Override
	public Void visitInterfaceDecl(P10aParser.InterfaceDeclContext ctx) {
		// 'interface' qualifiedName
		ctx.defn = addClassReference(ctx.qualifiedName(), ctx.getStart());
		//TODO
		Main.toDo(ctx.getStop(), "interfaces not yet implemented");
		return null;    	
	}

	@Override
	public Void visitImportDecl(P10aParser.ImportDeclContext ctx) {
		// 'import' qualifiedName
		ctx.defn = addClassReference(ctx.qualifiedName(), ctx.getStart());
		return null;    	
	}

	@Override
	public Void visitImportFile(P10aParser.ImportFileContext ctx) {
		// 	NL* ( importExtend NL+ )? ( importInterface NL+ )* ( importImport NL+ )* ( importMember NL+ )* EOF
		ClassSymbol csym = (ClassSymbol)currentScope;
		// similar to visitFile
		// explicitly visit parse children, so action can be made after the extend
		P10aParser.ImportExtendContext ext = ctx.importExtend();
		if (ext != null) {
			visitImportExtend(ext);
			P10aParser.ExtendDeclContext ex = ext.extendDecl();
			loadImport(ex.defn);
			csym.setSuperClass(ex.defn);
		} else if (csym.getFullName().equals(STD_LANG_IMPORTS_OBJECT)) {
			// Object superClass is set to null
			csym.setSuperClass(null);
			if (std_lang_Object == null) std_lang_Object = csym;
		} else {
			if (std_lang_Object == null) {
				std_lang_Object = (ClassSymbol) topScope.find(STD_LANG_IMPORTS_OBJECT);
				loadImport(std_lang_Object);
			}
			csym.setSuperClass(std_lang_Object);
		}
		List<P10aParser.ImportInterfaceContext> interfaces = ctx.importInterface();
		for (P10aParser.ImportInterfaceContext ifc : interfaces) visit(ifc);
		List<P10aParser.ImportMemberContext> members = ctx.importMember();
		for (P10aParser.ImportMemberContext mem : members) visit(mem);
		return null;    	
	}

	@Override
	public Void visitImportExtend(P10aParser.ImportExtendContext ctx) {
		// extendDecl
		P10aParser.ExtendDeclContext ext = ctx.extendDecl();
		// similar to visitExtendDecl
		ext.defn = addClassReference(ext.qualifiedName(), null/*don't include in referencing .imp file*/);		
		return null;    	
	}

	@Override
	public Void visitImportInterface(P10aParser.ImportInterfaceContext ctx) {
		// interfaceDecl
		P10aParser.InterfaceDeclContext ifc = ctx.interfaceDecl();
		// similar to visitInterfaceDecl
		ifc.defn = addClassReference(ifc.qualifiedName(), null/*don't include in referencing .imp file*/);		
		//TODO
		Main.toDo(ctx.getStop(), "interfaces not yet implemented");
		return null;    	
	}

	@Override
	public Void visitImportImport(P10aParser.ImportImportContext ctx) {
		// importDecl
		P10aParser.ImportDeclContext imp = ctx.importDecl();
		// similar to visitImportDecl
		imp.defn = addClassReference(imp.qualifiedName(), null/*don't include in referencing .imp file*/);
//currentScope.add(imp.defn);
		return null;    	
	}

	@Override
	public Void visitImportField(P10aParser.ImportFieldContext ctx) {
        // priv='#'? Identifier stat='static'? type
		fieldDeclaration(ctx.type(),ctx.Identifier(),ctx.priv,ctx.stat,null/*don't include in referencing .imp file*/);
        return null;
	}

	@Override
	public Void visitImportMethod(P10aParser.ImportMethodContext ctx) {
		// priv='#'? Identifier stat='static'? '(' NL* parameterList? ')' type?
		methodDeclaration(ctx.Identifier(),ctx.priv,ctx.stat,ctx.parameterList(),ctx.type(),null,null/*don't include in referencing .imp file*/);
        return null;
	}

	private void loadImport(ClassSymbol csym) {
		if (trace) Main.debug(currentScope.getName()+" loadImport %s %s %b %b", csym.getFullName(), csym.getName(), csym.isLoading(), csym.isLoaded());
		if (csym.isLoading()) return;  // previously loaded
		csym.setLoading(true);
    	String classFileBase = csym.getFullName().replace('.',File.separatorChar);
		String impFileName = classFileBase + IMPORT_SUFFIX;
    	String srcFileName = classFileBase + SOURCE_SUFFIX;
        try {
       		File impFile = new File(main.outputDirectory,impFileName);
       		if (!impFile.isFile()) {
       			String impDir = searchPath(impFileName, main.classPath);
       			impFile = (impDir==NOT_FOUND)? null : new File(impDir,impFileName);
           	}
   			String srcDir = searchPath(srcFileName, main.sourcePath);
   			File srcFile = (srcDir==NOT_FOUND)? null : new File(srcDir,srcFileName);
           	ClassSymbol comp = null;
           	if (srcFile != null && (impFile == null || impFile.lastModified() < srcFile.lastModified())) {
           		// full compile
           		if (impFile == null) {
           			Main.note(srcFile.getPath()+" is not compiled");
           		} else {
           			long now = System.currentTimeMillis();
           			Main.note(srcFile.getPath()+" is newer "+(now-srcFile.lastModified())/1000f
           			          +" : "+impFile.getPath()+" "+(now-impFile.lastModified())/1000f);
           		}
        		if (trace) Main.debug(topScope.getMembers().keySet().toString());
        		P10aParser.FileContext p = main.compile(srcDir, srcFileName);
        		if (trace) Main.debug(currentScope.getName()+" end subcompile "+srcFileName);
        		if (p != null) {
	        		comp = p.defn;
	        		//TODO how to use the compiled class result directly (p.defn), instead of
	        		//	reading the .imp file 
	        		// .imp file should now be available
	       			String impDir = searchPath(impFileName, main.classPath);
	       			impFile = (impDir==NOT_FOUND)? null : new File(impDir,impFileName);
        		} else {
        			//TODO subcompile error, create a placeholder
        		}
           	}
           	if (impFile != null) {
           		// can use the .imp outline
             	if (trace) Main.debug("import found "+impFile.getPath()/*.getAbsolutePath()*/);
    			MessageDigest digest = MessageDigest.getInstance(Main.SOURCE_MESSAGE_DIGEST_ALGORITHM);

				DigestInputStream impDigestStream = new DigestInputStream(new FileInputStream(impFile), digest);
				ANTLRInputStream input = new ANTLRInputStream(new InputStreamReader(impDigestStream,Main.STANDARD_CHARSET));
        		input.name = impFileName;

                P10aLexer lexer = new P10aLexer(input);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                P10aParser parser = new P10aParser(tokens);
                parser.removeErrorListeners(); // replace ConsoleErrorListener
                parser.addErrorListener(new Main.VerboseListener()); // with ours
                parser.setBuildParseTree(true);
                if (main.trace.contains("Parser")) parser.setTrace(true);
                P10aParser.ImportFileContext parse = parser.importFile();
         
                Scope saveScope = currentScope;
                currentScope = csym;
                currentScope.add(csym);  // short name is known in its own scope
                this.visit(parse);  // define the referenced class
        		if (trace) Main.debug(currentScope.getName()+" members "+currentScope.getMembers().keySet().toString());
        		assert comp == null || (csym.nextFieldIndex == comp.nextFieldIndex && csym.nextMethodIndex == comp.nextMethodIndex);
        		currentScope = saveScope;
                csym.setLoaded(true);
                csym.setToken(parse.getStart());  // link token for DebugInfo
                
                return;
        	}
        	Main.error("Could not find import "+csym.getFullName());
        } catch (IOException excp) {
			Main.error("Unable to read import "+impFileName+": "+excp.toString());
		} catch (NoSuchAlgorithmException excp) {
			Main.error("Source digest algorithm unavailable to read import "+impFileName+": "+excp.toString());
		}
	}

	private String searchPath(String fileName, String[] pathList) {
    	for (String path : pathList) {
			File file = new File(path, fileName);
           	if (file.isFile()) return path;
           	// try next in path
    	}
		return NOT_FOUND;
	}
	private static final String NOT_FOUND = "\0";
	
	@Override
	public Void visitMethodDecl(P10aParser.MethodDeclContext ctx) {
		// priv='#'? Identifier stat='static'? '(' NL* parameterList? ')' type? body
		ctx.defn = methodDeclaration(ctx.Identifier(),ctx.priv,ctx.stat,ctx.parameterList(),ctx.type(),ctx.body(),impOut);
		if (ctx.priv != null) ctx.defn.setAccess(Symbol.Access.ACCESS_PRIVATE);
		return null;
    }

	private MethodSymbol methodDeclaration(TerminalNode identifier, 
									Token priv,
									Token stat,
									P10aParser.ParameterListContext paramList,
									P10aParser.TypeContext rt,
									P10aParser.BodyContext body,
									PrintWriter impOut) {
		Token id = identifier.getSymbol();
		MethodSymbol msym = new MethodSymbol(id, currentScope);
		if (stat!=null) msym.setStatic(true);
		if (rt != null) {
	        visit(rt);
			msym.setType(rt.tipe);
		} else {
			msym.setType(Type.voidType);  // setting a non-null type simplifies subsequent processing
		}
		if (impOut != null) {
			if (priv!=null) impOut.print(priv.getText());
			impOut.print(id.getText());
			if (stat!=null) impOut.print(" static");
			impOut.print("(");
		}
		ArrayList<Type> paramTypes = null;
		String sep = "";
        if (paramList != null) {
        	List<P10aParser.ParameterContext> params = paramList.parameter();
			paramTypes = new ArrayList<Type>(params.size()+1);
			for (P10aParser.ParameterContext p : params) {
	        	P10aParser.TypeContext ptype = p.type();
				if (impOut != null) {
					impOut.print(sep);
		        	impOut.print(p.Identifier().getText());
		        	impOut.print(" ");
					impOut.print(ptype.getText());
				}
				visit(ptype);  // visit will be repeated below
				paramTypes.add(ptype.tipe);
				sep = ",";
			}
        } else {
    		paramTypes = DefinitionPass.EMPTY_TYPE_LIST;
        }
		if (impOut != null) {
			impOut.print(")");
			if (rt != null) {
				impOut.print(rt.getText());
			}
			impOut.println();
		}
		//if (impOut!=null) Main.note(msym.token, "methodDeclaration "+msym+" in "+currentScope.getName()+currentScope.getMembers());
		if (currentScope instanceof ClassSymbol) {
			ClassSymbol currClass = (ClassSymbol) currentScope;
			Symbol prev = currClass.findMember(msym.getName());
			if (prev == null) {
				//if (impOut!=null) Main.note(msym.token, msym.getName()+paramTypes.toString()+" no previous definition");
				currentScope.add(msym);  // report error if previous definition
				if (!msym.isStatic()) {
					currClass.assignVirtualMethod(msym);
				}
			} else if (prev instanceof MethodSymbol) {
				MethodSymbol mprev = (MethodSymbol)prev;
				MethodSymbol mover = mprev.resolveOverload(paramTypes, /*subtyping*/false);
				if (mover == null) {
					//TODO check same return type
					// not conflicting
					//if (impOut!=null) Main.note(msym.token, msym.getName()+paramTypes.toString()+" previous definition distinct "+mprev.toString());
					msym.setOverloads(mprev);
					currentScope.put(msym);  // replace in scope
					if (!msym.isStatic()) {
						currClass.assignVirtualMethod(msym);
					}
				} else if (mover.getDefiningScope() != currentScope) {
					//TODO prevent mix static/virtual
					//if (impOut!=null) Main.note(msym.getToken(), msym.getName()+paramTypes.toString()+" override "+mover.toString());
					msym.setOverloads(mprev); // retain previous overloads
					// The overridden signature will remain in the overload chain, but will
					// not be found since the search is sequential from the last added.
					// see MethodSymbol.resolveOverload
					currentScope.put(msym);
					if (!msym.isStatic()) {
						msym.setIndex(mover.getIndex());
					}
				} else {
					Main.note(msym.getToken(), "conflicting previous definition "+mprev.getParameterTypes()+":"+paramTypes);
					Main.error(msym.getToken(),"multiple definition of "+msym.getName()+paramTypes.toString());
					if (prev.getToken() != null) Main.note(prev.getToken(),"is previous definition");
					//TODO change definition to an error type to avoid useless messages
				}
			} else {
				Main.note(msym.getToken(), msym.getName()+" previous definition not a method "+prev.getClass().getSimpleName());
				currentScope.add(msym);  // report error if previous definition
			}
		} else {
			Main.error("Unexpected method scope "+currentScope);
		}
		if (stat == null) {
			// implicit first parameter
			// defining 'this' in the parameter list made more complications
			FieldSymbol tsym = new FieldSymbol(THIS);
			tsym.setType(currentScope.getType());
			tsym.setDefiningScope(currentScope);
			msym.setImplicitParameter(tsym);
		}
		currentScope = msym;
        if (paramList != null) visit(paramList);
        if (body != null) visit(body);
        currentScope = currentScope.close();
        
		if (currentScope instanceof ClassSymbol
			&& id.getText().equals(ClassSymbol.INITIALZER_METHOD_NAME)) {
			//TODO check rt == null
        	declareCreator(paramList, paramTypes);
        }
        return msym;
	}

	private void declareCreator(P10aParser.ParameterListContext paramList, ArrayList<Type> paramTypes) {
		//TODO how to retrieve this declaration in CodePass visitMethodDecl?
		MethodSymbol creator = new MethodSymbol(ClassSymbol.NEW_INSTANCE_METHOD_NAME, currentScope);
		creator.setStatic(true);
		creator.setType(new ObjectType((ClassSymbol) currentScope));
		Symbol prev = currentScope.find(creator.getName());
		if (prev == null) {
			//Main.note(creator, "no previous definition");
			currentScope.add(creator);  // report error if previous definition
		} else if (prev instanceof MethodSymbol) {
			MethodSymbol mprev = (MethodSymbol)prev;
			if (mprev.resolveOverload(paramTypes) == null) {
				// not conflicting
				//Main.note(creator.getToken(), "previous definition distinct "+mprev.getParameterTypes());
				creator.setOverloads(mprev);
				currentScope.put(creator);  // replace in scope
			//TODO override
			//TODO prevent mix static/virtual
			} else {
				Main.note(creator.getToken(), "conflicting previous definition "+mprev.getParameterTypes()+":"+paramTypes);
				Main.error(creator.getToken(),"multiple definition of "+creator.getName());
				if (prev.getToken() != null) Main.note(prev.getToken(),"is previous definition");
			}
		} else {
			Main.note(creator.getToken(), prev.getName()+" previous definition not a method "+prev.getClass().getSimpleName());
			currentScope.add(creator);  // report error if previous definition
		}
		currentScope = creator;
		// repeat visit parameters on creator
		if (paramList != null) visit(paramList);
		currentScope = currentScope.close();
	}
    
	@Override
    public Void visitParameter(P10aParser.ParameterContext ctx) {
        // Identifier type
		ctx.defn = fieldDeclaration(ctx.type(), ctx.Identifier(), null, null, null);
        return null;
    }
	
    @Override
	public Void visitFieldDecl(P10aParser.FieldDeclContext ctx) {
        // priv='#'? Identifier stat='static'? type ( ':' expression )?
		ctx.defn = fieldDeclaration(ctx.type(), ctx.Identifier(), ctx.priv, ctx.stat, impOut);
		if (ctx.priv != null) ctx.defn.setAccess(Symbol.Access.ACCESS_PRIVATE);
        P10aParser.ExpressionContext init = ctx.expression();   // initializer
		if (init != null) visit(init);
        return null;
	}

	private FieldSymbol fieldDeclaration(P10aParser.TypeContext type, TerminalNode id, Token priv, Token stat, PrintWriter impOut) {
		visit(type);
		FieldSymbol fsym = new FieldSymbol(id.getSymbol());
		fsym.setType(type.tipe);
		if (stat!=null) fsym.setStatic(true);
		currentScope.add(fsym);  // checks for multiple definition
		if (currentScope instanceof ClassSymbol) {
			if (!fsym.isStatic()) {
				((ClassSymbol)currentScope).assignInstanceField(fsym);
			}
		}
		if (impOut != null && currentScope instanceof ClassSymbol) {
			if (priv!=null) impOut.print(priv.getText());
			impOut.print(id.getSymbol().getText());
			impOut.print(" ");
			if (stat!=null) impOut.print("static ");
			impOut.print(type.getText());
			impOut.println();
		}
		return fsym;
	}
	
	@Override
    public Void visitType(P10aParser.TypeContext ctx) {
		P10aParser.SimpleTypeContext st = ctx.simpleType();
        visit(st);
        Type tipe = st.tipe;
        // don't need to visit dimensions, since it is all terminals
		int dim = (ctx.getChildCount() - 1) / 2;
		while (dim-- > 0) tipe = new ArrayType(tipe);
        ctx.tipe = tipe;
        return null;
    }
    
	@Override
    public Void visitBooleanType(P10aParser.BooleanTypeContext ctx) {
    	ctx.tipe = Type.booleanType;
        return null;
    }
    
	@Override
    public Void visitByteType(P10aParser.ByteTypeContext ctx) {
    	ctx.tipe = Type.byteType;
        return null;
    }
    
	@Override
    public Void visitCharType(P10aParser.CharTypeContext ctx) {
    	ctx.tipe = Type.charType;
        return null;
    }
    
	@Override
    public Void visitDoubleType(P10aParser.DoubleTypeContext ctx) {
    	ctx.tipe = Type.doubleType;
        return null;
    }
    
	@Override
    public Void visitFloatType(P10aParser.FloatTypeContext ctx) {
    	ctx.tipe = Type.floatType;
        return null;
    }
    
	@Override
    public Void visitIntType(P10aParser.IntTypeContext ctx) {
    	ctx.tipe = Type.intType;
        return null;
    }
    
	@Override
    public Void visitLongType(P10aParser.LongTypeContext ctx) {
    	ctx.tipe = Type.longType;
        return null;
    }
    
	@Override
    public Void visitShortType(P10aParser.ShortTypeContext ctx) {
    	ctx.tipe = Type.shortType;
        return null;
    }

	@Override
	public Void visitObjectType(P10aParser.ObjectTypeContext ctx) {
		Token id = ctx.Identifier().getSymbol();
		Symbol s = currentScope.find(id);
		if (s != null && s instanceof ClassSymbol) {
			ctx.tipe = s.getType();
			((ClassSymbol)s).setReferenced(true);
		} else {
			Main.error(id,id.getText()+" is not a defined class");
			debugShowCurrentScope();
			ctx.tipe = Type.errorType;
		}
		return null;
	}

	private void debugShowCurrentScope() {
		for (Scope cs = currentScope; cs != null; cs = cs.getEnclosingScope()) {
			Main.debug("    "+cs.getName()+cs.getMembers().keySet());
		}
	}

	@Override
	public Void visitForInStmt(P10aParser.ForInStmtContext ctx) {
		// 'for' Identifier type ':' expression block '.'
		currentScope = new BlockScope("for@"+ctx.getStart().getLine(), currentScope);
		fieldDeclaration(ctx.type(), ctx.Identifier(), null, null, null);
        visit(ctx.expression());
        visit(ctx.block());
		currentScope = currentScope.close();
        return null;
	}

	@Override
	public Void visitForWhileStmt(P10aParser.ForWhileStmtContext ctx) {
		// 'for' statement ( 'while' expression )? ( 'next' statement )? block '.'
		currentScope = new BlockScope("for@"+ctx.getStart().getLine(), currentScope);
    	List<P10aParser.StatementContext> stmt = ctx.statement();
    	visit(stmt.get(0));
    	visit(ctx.expression());
    	if (stmt.size() > 1) visit(stmt.get(1));  // next
        visit(ctx.block());
		currentScope = currentScope.close();
        return null;
	}

	@Override
    public Void visitBlock(P10aParser.BlockContext ctx) {
		// NL+ ( statement NL+ )*
		currentScope = new BlockScope("block@"+ctx.getStart().getLine(), currentScope);
        for (P10aParser.StatementContext s : ctx.statement()) visit(s);
		currentScope = currentScope.close();
        return null;
    }

	@Override
    public Void visitThisPrim(P10aParser.ThisPrimContext ctx) {
		// 'this'
    	ctx.refScope = currentScope;
        return null;
    }

	@Override
    public Void visitSuperPrim(P10aParser.SuperPrimContext ctx) {
		// 'super'
    	ctx.refScope = currentScope;
        return null;
    }

	@Override
    public Void visitIdPrim(P10aParser.IdPrimContext ctx) {
		// Identifier
    	ctx.refScope = currentScope;
        return null;
    }
}

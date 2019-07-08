/*	Main.java
 
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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

public class Main {
	public static final String VERSION_ID = "p10a version 0";

	static final String STANDARD_CHARSET = "UTF-8";
	static final String SOURCE_MESSAGE_DIGEST_ALGORITHM = "MD5";
	
	public static void main(String[] args) {
		Main compiler = new Main();
		boolean any = false;
		for (ListIterator<String> argIter = Arrays.asList(args).listIterator();
			 argIter.hasNext(); ) {
			String arg = argIter.next();
			if (arg.startsWith("-")) {
				compiler.option(arg,argIter);
			} else {
        		compiler.compile(EMPTY_PATH,arg);
        		any = true;
        	}
        }
        if (!any) compiler.compile(null,null);
        if (errorCount > 0) System.exit(1);
	}

	private void option(String arg, ListIterator<String> argIter) {
		if (arg.equals(CLASS_PATH_OPTION) && argIter.hasNext()) {
			classPath = pathSplit(argIter.next());
		} else if (arg.equals(SOURCE_PATH_OPTION) && argIter.hasNext()) {
			sourcePath = pathSplit(argIter.next());
		} else if (arg.equals(OUTPUT_DIRECTORY_OPTION) && argIter.hasNext()) {
			outputDirectory = argIter.next();
		} else if (arg.equals(TRACE_OPTION) && argIter.hasNext()) {
			trace.add(argIter.next());
		} else if (arg.equals(HIGHLIGHT_ERRORS_OPTION)) {
			errorHighlight = ANSI_BOLD; errorReset = ANSI_RESET;
		} else {
			error("Unrecognized option "+arg);
		}
	}
	
	public static final String EMPTY_PATH = null;
	
	public static final String CLASS_PATH_OPTION = "-L"; // or -classpath
	public static final String[] defaultClassPath = {EMPTY_PATH/*i.e. current directory*/};
	String[] classPath = defaultClassPath;

	public static final String SOURCE_PATH_OPTION = "-S"; // or -sourcepath
	public static final String[] defaultSourcePath = {EMPTY_PATH/*i.e. current directory*/};
	String[] sourcePath = defaultSourcePath;

	public static final String OUTPUT_DIRECTORY_OPTION = "-d";
	public static final String defaultOutputDirectory = EMPTY_PATH/*i.e. from input file*/; 
	String outputDirectory = defaultOutputDirectory;

	public static final String TRACE_OPTION = "-t";
	HashSet<String> trace = new HashSet<String>();

	public static final String HIGHLIGHT_ERRORS_OPTION = "-highlightErrors";
	//LATER would be nice to detect with isatty
	static String errorHighlight="", errorReset="";
	private static final String ANSI_BOLD = "\033[1m", ANSI_RESET="\033[0m";
	
	public static final String PATH_SEPARATOR = ":";
	// could use File.pathSeparator, but prefer a platform-independent separator

	static Pattern pathPat = Pattern.compile(PATH_SEPARATOR,Pattern.LITERAL);
	private String[] pathSplit(String arg) {
		return pathPat.split(arg,-1);
	}

	public static String dropFileNameSuffix(String fileName) {
		int ext = fileName.lastIndexOf('.');
		return (ext > 0) ? fileName.substring(0, ext)
						 : fileName;
	}

	// Compilation of a single input file
	
	P10aParser.FileContext compile(String sourceFilePath, String sourceFileName) {
		ANTLRInputStream input;
		try {
			// https://www.rgagnon.com/javadetails/java-0416.html
			MessageDigest digest = MessageDigest.getInstance(SOURCE_MESSAGE_DIGEST_ALGORITHM);
			
			if (sourceFileName != null) {
				File inFile = new File(sourceFilePath, sourceFileName);
				DigestInputStream inDigestStream = new DigestInputStream(new FileInputStream(inFile), digest);
				input = new ANTLRInputStream(new InputStreamReader(inDigestStream,STANDARD_CHARSET));
				input.name = inFile.getPath();
				System.out.print("---------- ");
				System.out.println(sourceFileName);
				System.out.flush();
			} else {
				DigestInputStream inDigestStream = new DigestInputStream(System.in, digest);
				input = new ANTLRInputStream(new InputStreamReader(inDigestStream,STANDARD_CHARSET));
				input.name = "<System.in>";
			}
	        
	        P10aLexer lexer = new P10aLexer(input);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        P10aParser parser = new P10aParser(tokens);
	        parser.removeErrorListeners(); // replace ConsoleErrorListener
	        parser.addErrorListener(new VerboseListener()); // with ours
	        parser.setBuildParseTree(true);
	        if (trace.contains("Parser")) parser.setTrace(true);
	        P10aParser.FileContext parse = parser.file();
	        if (trace.contains("ParseTree")) printTree(parse, parser);

	        if (errorCount == 0) {
		        DefinitionPass definitions = new DefinitionPass(this, sourceFilePath, sourceFileName);
		        definitions.visit(parse);
		        if (errorCount == 0) {
			        CodePass code = new CodePass(this, sourceFilePath, sourceFileName, getHexadecimal(digest.digest()));
			        code.visit(parse);
		        }
	        }
	        return (errorCount == 0) ? parse : null;
	        
		} catch (IOException excp) {
			error(excp.getMessage());
		} catch (NoSuchAlgorithmException excp) {
			error(excp.getMessage());
		}
		return null;
	}

	public static void printTree(P10aParser.FileContext parse, P10aParser parser) {
		printTree(0,parse,Arrays.asList(parser.getRuleNames()));
	}
	
	public static void printTree(int indent, final Tree t, final List<String> ruleNames) {
		for (int i = 0;  i < indent;  ++i) System.out.print("    ");
		System.out.println(Trees.getNodeText(t, ruleNames));
		for (int i = 0; i<t.getChildCount(); i++) {
			printTree(indent+1,t.getChild(i),ruleNames);
		}
	}

	// Error display
	
	private static int errorCount = 0;
	private static final int ERROR_LIMIT = 100; 

	private static void errprintf(String format, Object... args) {
		System.err.print(errorHighlight);
		System.err.printf(format, args);
		System.err.print(errorReset);
        System.err.flush();
	}

	private static final String ERROR_D_D_S = "%d:%d:︎︎ %s\n";

	public static void error(int line, int charPositionInLine, String text) {
        errprintf(Main.ERROR_D_D_S, line, charPositionInLine, text);
    }

	public static void error(Token t, String text) {
		//if (errorCount >= ERROR_LIMIT) throw new Exception("too many errors");
		if (t != null) {
	    	String source = t.getInputStream().getSourceName();
	    	if (!source.isEmpty()) { System.err.print(source); System.err.print(":"); }
	        error(t.getLine(), t.getCharPositionInLine()+1, text);
		} else {
			error(text);
		}
        errorCount++;
    }

	public static void error(TerminalNode tn, String text) {
		if (tn != null) error(tn.getSymbol(), text);
		else error(text);
    }
	
    public static void error(String text) {
		//if (errorCount >= ERROR_LIMIT) throw new Exception("too many errors");
        errprintf("%s\n", text);
        errorCount++;
    }

    public static void note(Token t, String text) {
    	String source = t.getInputStream().getSourceName();
    	if (!source.isEmpty()) { System.err.print(source); System.err.print(":"); }
        errprintf(Main.ERROR_D_D_S, t.getLine(), t.getCharPositionInLine()+1, text);
    }

	public static void note(String text) {
		System.err.println(text);
	}

	public static void debug(String format, Object... args) {
		System.out.print("#");
		System.out.printf(format, args);
		System.out.println();
        System.out.flush();
	}

	public static void debug(String text) {
		System.out.print("#");
		System.out.println(text);
        System.out.flush();
	}

	public static void toDo(Token t, String text) {
    	String source = t.getInputStream().getSourceName();
    	if (!source.isEmpty()) { System.err.print(source); System.err.print(":"); }
        errprintf(Main.ERROR_D_D_S, t.getLine(), t.getCharPositionInLine()+1, "TODO "+text);
        errorCount++;
	}
	
	// Syntax error display for ANTLR parsing

	public static class VerboseListener extends BaseErrorListener {
	    @Override
	    public void syntaxError(Recognizer<?, ?> recognizer,
	                            Object offendingSymbol,
	                            int line, int charPositionInLine,
	                            String msg,
	                            RecognitionException e) {
	        error((Token)offendingSymbol,msg);
	        List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
	        Collections.reverse(stack);
	        System.err.println("    rule stack: "+stack);
	    }
	}

	// https://www.rgagnon.com/javadetails/java-0596.html
	public static String getHexadecimal(byte [] raw) {
		if (raw == null) return null;
		final StringBuilder hex = new StringBuilder(2*raw.length);
		for (final byte b : raw) {
			hex.append(Character.forDigit((b >> 4)&0xF,16))
			   .append(Character.forDigit( b      &0xF,16));
		}
		return hex.toString();
	}
}

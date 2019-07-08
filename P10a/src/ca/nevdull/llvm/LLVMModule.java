/*	LLVMModule.java
 
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LLVMModule {
	private PrintWriter code;
	private static final String LLVM_SUFFIX		= ".ll";

	public LLVMModule(String outputDirectory, String baseFileName, String sourceFileName) throws FileNotFoundException, UnsupportedEncodingException {
		File file = new File(outputDirectory, baseFileName+LLVM_SUFFIX);
		code = new PrintWriter(file, "UTF-8");
		emit("source_filename = \"",sourceFileName.replace("\"","\\22"),"\""," ; ",LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	}
    
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

	public void emit(String... args) {
		emitn(args);
		code.println();
		code.flush();
	}

	public void emitn(String... args) {
		for (String a : args) code.print(a);
	}

	public void declareFunction(LLVMType rtype, String fName, List<LLVMValue> args) {
		emitn("declare ",rtype.toString()," ",fName,"(");
		String sep = "";
		for (LLVMValue a : args) {
			emitn(sep,a.toString());
			sep = ",";
		}
		emit(")");
	}

	LLVMBlock currentBlock;
	
	public void functionStart(LLVMType rtype, String fName, List<LLVMValue> args) {
		emitn("define ",rtype.toString()," ",fName,"(");
		String sep = "";
		for (LLVMValue a : args) {
			emitn(sep,a.toString());
			sep = ",";
		}
		emit(") {");
		currentBlock = new LLVMBlock();
	}
	
	public void functionEnd() {
		emit("}");
		currentBlock = null;
	}

	public void defineGlobal(LLVMType vart, String varn, String init) {
		emit(varn," = global ",vart.toString()," ",init);
	}

	public void declareExternal(LLVMType vart, String varn) {
		emit(varn," = external global ",vart.toString());
	}

	public void defineStatic(LLVMType vart, String varn, String init) {
		emit(varn," = internal global ",vart.toString()," ",init);
	}
	
	LLVMBlock add(LLVMInstruction instruction) {
		return currentBlock.add(instruction);
	}
	
	LLVMInstruction add(String... text) {
		LLVMInstruction instruction = new LLVMInstruction(text);
		currentBlock.add(instruction);
		return instruction;
	}
	
	LLVMInstruction add(LLVMType result, String... text) {
		LLVMInstruction instruction = new LLVMInstruction(result, text);
		currentBlock.add(instruction);
		return instruction;
	}
	
	LLVMInstruction add(String name, LLVMType result, String... text) {
		LLVMInstruction instruction = new LLVMInstruction(name, result, text);
		currentBlock.add(instruction);
		return instruction;
	}
	/*
	branch
	join
	terminate
	*/

}

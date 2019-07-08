/*	LLVMBlock.java
 
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

import java.util.ArrayList;

public class LLVMBlock extends LLVMValue {
	
	ArrayList<LLVMInstruction> instructions = new ArrayList<LLVMInstruction>();
	
	LLVMInstruction terminator;
	
	private static int sequence = 0;
	private static String nextSequence() {
		return Integer.toHexString(++sequence);
	}

	public LLVMBlock(String label) {
		super(LLVMType.LABEL, label);
	}

	public LLVMBlock() {
		this("B"+nextSequence());
	}

	LLVMBlock add(LLVMInstruction instruction) {
		instructions.add(instruction);
		return this; // for fluent interface
	}
	
	LLVMBlock add(String... text) {
		add(new LLVMInstruction(text));
		return this; // for fluent interface
	}
	
	LLVMBlock add(LLVMType result, String... text) {
		add(new LLVMInstruction(result, text));
		return this; // for fluent interface
	}
	
	LLVMBlock add(String name, LLVMType result, String... text) {
		add(new LLVMInstruction(name, result, text));
		return this; // for fluent interface
	}
	
}

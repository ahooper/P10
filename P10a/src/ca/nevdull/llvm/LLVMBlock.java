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

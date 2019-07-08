/*	LLVMInstruction.java
 
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

public class LLVMInstruction extends LLVMValue {
	
	String text;
	
	private static final String REG_PREFIX = "%r";
	private static int sequence = 0;
	private static String nextSequence(String prefix) {
		return prefix+Integer.toHexString(++sequence);
	}

	public LLVMInstruction(String... text) {
		super(null,null);
		StringBuilder b = new StringBuilder();
		for (String t : text) b.append(t);
		this.text = b.toString();
	}
	
	public LLVMInstruction(String name, LLVMType result, String... text) {
		super(result, name);
		StringBuilder b = new StringBuilder(super.toString());
		b.append(" = ");
		for (String t : text) b.append(t);
		this.text = b.toString();
	}

	public LLVMInstruction(LLVMType result, String... text) {
		this(nextSequence(REG_PREFIX), result, text);
	}

/*
case Ret:    return "ret";
case Br:     return "br";
case Switch: return "switch";
case IndirectBr: return "indirectbr";
case Invoke: return "invoke";
case Resume: return "resume";
case Unreachable: return "unreachable";
case CleanupRet: return "cleanupret";
case CatchRet: return "catchret";
case CatchPad: return "catchpad";
case CatchSwitch: return "catchswitch";

// Standard unary operators...
case FNeg: return "fneg";

// Standard binary operators...
case Add: return "add";
case FAdd: return "fadd";
case Sub: return "sub";
case FSub: return "fsub";
case Mul: return "mul";
case FMul: return "fmul";
case UDiv: return "udiv";
case SDiv: return "sdiv";
case FDiv: return "fdiv";
case URem: return "urem";
case SRem: return "srem";
case FRem: return "frem";

// Logical operators...
case And: return "and";
case Or : return "or";
case Xor: return "xor";

// Memory instructions...
case Alloca:        return "alloca";
case Load:          return "load";
case Store:         return "store";
case AtomicCmpXchg: return "cmpxchg";
case AtomicRMW:     return "atomicrmw";
case Fence:         return "fence";
case GetElementPtr: return "getelementptr";

// Convert instructions...
case Trunc:         return "trunc";
case ZExt:          return "zext";
case SExt:          return "sext";
case FPTrunc:       return "fptrunc";
case FPExt:         return "fpext";
case FPToUI:        return "fptoui";
case FPToSI:        return "fptosi";
case UIToFP:        return "uitofp";
case SIToFP:        return "sitofp";
case IntToPtr:      return "inttoptr";
case PtrToInt:      return "ptrtoint";
case BitCast:       return "bitcast";
case AddrSpaceCast: return "addrspacecast";

// Other instructions...
case ICmp:           return "icmp";
case FCmp:           return "fcmp";
case PHI:            return "phi";
case Select:         return "select";
case Call:           return "call";
case Shl:            return "shl";
case LShr:           return "lshr";
case AShr:           return "ashr";
case VAArg:          return "va_arg";
case ExtractElement: return "extractelement";
case InsertElement:  return "insertelement";
case ShuffleVector:  return "shufflevector";
case ExtractValue:   return "extractvalue";
case InsertValue:    return "insertvalue";
case LandingPad:     return "landingpad";
case CleanupPad:     return "cleanuppad";
*/
	
}

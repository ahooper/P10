/*	LLVMMetadata.java
 
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

import java.util.LinkedHashMap;

public class LLVMMetadata {
	private static int nextSequence = 1;
	String s;
	int id;

	public LLVMMetadata(LLVMValue value) {
		id = nextSequence++;
		s = value.toString();
	}

	public LLVMMetadata() {
		id = nextSequence++;
	}
	
	public static class MdString extends LLVMMetadata {
		MdString(String str) {
	    	int len = str.length();
	    	int x = 0;
	    	for ( ; x < len; x++) {
	    		char c = str.charAt(x);
	    		if (c >= ' ' && c <= '~' && c != '"') { 
	    		} else break;  // hit a character that requires escaping
	    	}
	    	if (x == len) {
	    		// no escapes needed
	    		s = '"'+str+'"';
	    		return;
	    	}
	    	StringBuilder escaped = new StringBuilder(str.length()*2);
	    	escaped.append('"').append(str.substring(0, x));
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
	    	s = escaped.append('"').toString();
		}
		
		private static void addEscaped(StringBuilder escaped, byte b) {
			escaped.append('\\')
				.append(Character.forDigit((b>> 4)&0xf,16))
				.append(Character.forDigit( b     &0xf,16));
		}
	}
	
	public static class MdStruct extends LLVMMetadata {
		LinkedHashMap<String,LLVMMetadata> struct = new LinkedHashMap<String,LLVMMetadata>();
		public void def(String name, LLVMMetadata data) {
			struct.put(name, data);
		}
	}
	// Basic types
	public final static int DW_ATE_address       = 1;
	public final static int DW_ATE_boolean       = 2;
	public final static int DW_ATE_float         = 4;
	public final static int DW_ATE_signed        = 5;
	public final static int DW_ATE_signed_char   = 6;
	public final static int DW_ATE_unsigned      = 7;
	public final static int DW_ATE_unsigned_char = 8;
	// Derived types
	public final static int DW_TAG_member             = 13;
	public final static int DW_TAG_pointer_type       = 15;
	public final static int DW_TAG_reference_type     = 16;
	public final static int DW_TAG_typedef            = 22;
	public final static int DW_TAG_inheritance        = 28;
	public final static int DW_TAG_ptr_to_member_type = 31;
	public final static int DW_TAG_const_type         = 38;
	public final static int DW_TAG_friend             = 42;
	public final static int DW_TAG_volatile_type      = 53;
	public final static int DW_TAG_restrict_type      = 55;
	public final static int DW_TAG_atomic_type        = 71;
	// Composite types
	public final static int DW_TAG_array_type       = 1;
	public final static int DW_TAG_class_type       = 2;
	public final static int DW_TAG_enumeration_type = 4;
	public final static int DW_TAG_structure_type   = 19;
	public final static int DW_TAG_union_type       = 23;
}

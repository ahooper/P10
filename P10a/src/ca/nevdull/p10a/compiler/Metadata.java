// Not currently used
package ca.nevdull.p10a.compiler;

import ca.nevdull.util.Str;

public class Metadata extends ca.nevdull.util.Str {

	private static int nextSequence = 1;
	
	int id = nextSequence++;
	
	Str mdQuote(String s) {
    	Str r = new Str("\"");
    	int x = 0, w = 0, len = s.length();
    	for ( ; x < len; x = w) {
    		char c = s.charAt(x);
    		if (c >= ' ' && c <= '~' && c != '"') { 
    			continue; // does not need escape
    		}
    		if (x > w) {
    			r.add(s.substring(w,x));  // unescaped segment
    		}
			w = x+1;
    		if (c < 0x80) {
    			r.add(mdEscaped((byte)c));
   			} else if (c < 0x800) { // encode to UTF-8 (LLVM LangRef does not specify encoding)
   				r.add(mdEscaped((byte)( (c >>  6)         | 0xC0)));
   				r.add(mdEscaped((byte)(( c        & 0x3F) | 0x80)));
			} else if (c < Character.MIN_HIGH_SURROGATE/*0xD800*/ || c > Character.MAX_LOW_SURROGATE/*0xDFFF*/) {
				r.add(mdEscaped((byte)( (c >> 12)         | 0xE0)));
				r.add(mdEscaped((byte)(((c >>  6) & 0x3F) | 0x80)));
				r.add(mdEscaped((byte)(( c        & 0x3F) | 0x80)));
    		} else if (c < Character.MIN_LOW_SURROGATE/*0xDC00*/) {
    			// surrogate high
    			int d = s.codePointAt(x);  w = x+2;
				r.add(mdEscaped((byte)( (d >> 18)         | 0xF0)));
				r.add(mdEscaped((byte)(((d >> 12) & 0x3F) | 0x80)));
				r.add(mdEscaped((byte)(((d >>  6) & 0x3F) | 0x80)));
				r.add(mdEscaped((byte)(( d        & 0x3F) | 0x80)));
    		} else {
    			throw new IllegalArgumentException("Invalid low surrogate code unit");
    		}
    	}
		if (x > w) {
			r.add(s.substring(w,x));  // ending unescaped segment
		}
    	r.add("\"");
    	return r;
	}
	
	private static char[] mdEscaped(byte b) {
		char[] c = /*new*/{'\\',Character.forDigit((b>>4)&0xF,16),Character.forDigit(b&0xF,16)};
		return c;
	}
	
	Str mdReference() {
		return new Str("!",Integer.toString(id));
	}
	
}

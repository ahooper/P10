package ca.nevdull.util;

// Buildable and hashMapable bytes array
public class UTF8Bytes {
	private byte[] content;
	private int length;
	
	public UTF8Bytes(int size) {
		content = new byte[size];
		length = 0;
	}
	
	public UTF8Bytes(byte[] b) {
		content = b;
		length = b.length;
	}
	
	public UTF8Bytes(String s) {
		this(s.length()*3/2);
		// https://stackoverflow.com/a/1527891
		final int length = s.length();
		for (int offset = 0; offset < length; ) {
		   final int codepoint = s.codePointAt(offset);
		   append(codepoint);
		   offset += Character.charCount(codepoint);
		}
	}
	
	public int hashCode() {
		int hc = 1;
		for (int i = 0; i < length; i++) {
			hc = 31 * hc + content[i];
		}
		return hc;
	}
	
	public boolean equals(UTF8Bytes comp) {
		byte[] c = comp.content;
	    if (length != comp.length) return false;
    	for (int i = length; --i >= 0; ) {
    		if (content[i] != c[i]) return false;
    	}
	    return true;
	}
	
	private int extend(int add) {
		int p = length;
		length += add;
		if (length > content.length) {
			int exp = content.length * 2;
			if (length > exp) exp = length;
			byte[] con = new byte[exp];
			System.arraycopy(content, 0, con, 0, length);
			content = con;
		}
		return p;
	}
	
	public void append(int codepoint) {
		int p;
		if (codepoint < 0x80) {
			p = extend(1);
			content[p  ] = (byte) codepoint;
		} else if (codepoint < 0x800) {
			p = extend(2);
			content[p  ] = (byte)( (codepoint >>  6)         | 0xC0);
			content[p+1] = (byte)(( codepoint        & 0x3F) | 0x80);
		} else if (codepoint < 0x10000) {
			p = extend(3);
			content[p  ] = (byte)( (codepoint >> 12)         | 0xE0);
			content[p+1] = (byte)(((codepoint >>  6) & 0x3F) | 0x80);
			content[p+2] = (byte)(( codepoint        & 0x3F) | 0x80);
		} else if (codepoint < 0x110000) {
			p = extend(4);
			content[p  ] = (byte)( (codepoint >> 18)         | 0xF0);
			content[p+1] = (byte)(((codepoint >> 12) & 0x3F) | 0x80);
			content[p+2] = (byte)(((codepoint >>  6) & 0x3F) | 0x80);
			content[p+3] = (byte)(( codepoint        & 0x3F) | 0x80);
		} else {
			throw new IllegalArgumentException("Invalid Unicode \\U"+Integer.toHexString(codepoint));
		}
	}
	
	public int getLength() {
		return length;
	}
	
	public byte getByte(int ind) {
		if (ind < 0 || ind >= length) throw new ArrayIndexOutOfBoundsException(ind);
		return content[ind];
	}
	
	@Override
	public String toString() {
		// Not tested
		StringBuilder s = new StringBuilder(length*3/2);
		for (int i = 0; i < length; ++i) {
			byte b1 = content[i], b2, b3, b4;
			switch ((b1 & 0xFF) >> 4) {
            case 0: case 1: case 2: case 3:
            case 4: case 5: case 6: case 7:
            	s.append(b1);
				break;
            case 0xC: case 0xD:
            	if (length-i < 2) throw new IllegalArgumentException("Invalid UTF-8 encoding");
            	b2 = content[++i];  // should check b2 & 0xC0 == 0x80
            	// code point is < 0x0800, therefore surrogate pair not required in UTF-16
            	s.append(( (b1 & 0x1F) << 6)
            			 | (b2 & 0x3F) );
            	break;
            case 0xE:
            	if (length-i < 3) throw new IllegalArgumentException("Invalid UTF-8 encoding");
            	b2 = content[++i];  // should check b2 & 0xC0 == 0x80
            	b3 = content[++i];  // should check b3 & 0xC0 == 0x80
            	// surrogate pair may be required in UTF-16
            	s.appendCodePoint(( ( ( (b1 & 0x0F)   << 6)
            			              | (b2 & 0x3F) ) << 6)
            			          |     (b3 & 0x3F) );
            	break;
            case 0xF:
            	if (length-i < 4) throw new IllegalArgumentException("Invalid UTF-8 encoding");
            	b2 = content[++i];  // should check b2 & 0xC0 == 0x80
            	b3 = content[++i];  // should check b3 & 0xC0 == 0x80
            	b4 = content[++i];  // should check b4 & 0xC0 == 0x80
            	// surrogate pair required in UTF-16
            	s.appendCodePoint(((( (( ( (b1 & 0x03)   << 6)
       			                         | (b2 & 0x3F) ) << 6)
       			                   |       (b3 & 0x3F) ) << 6)
	                              |        (b4 & 0x3F) ));
            	break;
            default:
            	throw new IllegalArgumentException("Invalid UTF-8 encoding");
			}
		}
		return s.toString();
	}
	
}


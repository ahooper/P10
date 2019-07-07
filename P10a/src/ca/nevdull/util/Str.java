// Tree-structure string

package ca.nevdull.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

public class Str {
	ArrayList<Str> list;
	
	// Leaf element of structure
	static class StrString extends Str {
		// list is not used in leaf elements
		String string;
		public StrString(String string) {
			this.string = string;
		}
		public void write(Writer writer) throws IOException {
			writer.write(string);
		}
		public String toString() {
			return string;
		}
	}
	
	static class StrChars extends Str {
		// list is not used in leaf elements
		char[] chars;
		public StrChars(char... chars) {
			this.chars = chars;
		}
		public void write(Writer writer) throws IOException {
			writer.write(chars);
		}
		public String toString() {
			return new String(chars);
		}
	}

	public Str() {
		list = new ArrayList<Str>();
	}

	public Str(String string) {
		list = new ArrayList<Str>();
		list.add(new StrString(string));
	}

	public Str(String... strings) {
		list = new ArrayList<Str>();
		add(strings);
	}

	public Str add(String string) {
		list.add(new StrString(string));
		return this;  // for "fluent" interface
	}

	public Str add(char... chars) {
		list.add(new StrChars(chars));
		return this;  // for "fluent" interface
	}

	public Str add(String... strings) {
		int n = strings.length;
		for (int i = 0;  i < n;  i++) list.add(new StrString(strings[i]));
		return this;  // for "fluent" interface
	}

	public Str add(Str s) {
		assert s != null;
		list.add(s);
		return this;  // for "fluent" interface
	}
	
	public void write(Writer writer) throws IOException {
		for (Str s : list) s.write(writer);
	}
	
	public String toString() {
		return list.toString();
	}

}

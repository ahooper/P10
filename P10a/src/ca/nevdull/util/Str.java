/*	Str.java
 
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

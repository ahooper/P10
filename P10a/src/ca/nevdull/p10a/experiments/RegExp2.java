// https://swtch.com/~rsc/regexp/regexp2.html

package ca.nevdull.p10a.experiments;

import java.io.PrintStream;
import java.util.ArrayList;

public class RegExp2 {

	public RegExp2(String s) {
		chars = s.toCharArray();
		x = 0; z = chars.length;
		re = alt();
		assert x==z; // alt should be greedy
		re.print(0);
	}

	///// parsing
	
	private char[] chars;
	private int x, z;
	RE re;
	
	private boolean xc(char c) {
		if (x<z && chars[x]==c) {
			x++;
			return true;
		}
		return false;
	}
	
	private boolean nc() {
		return x<z;
	}

	RE alt() {
		RE r = concat();
		while (xc('|')) {
			r = new Alt(r,concat());
		}
		return r;
	}

	RE concat() {
		RE r = repeat();
		while (nc() && !xc(')')) {
			r = new Cat(r,repeat());
		}
		return r;
	}
	
	RE repeat() {
		RE r = single();
		if (xc('*')) {
			r = new Star(r,null);
			if (xc('?')) r.n = true;
		} else if (xc('+')) {
			r = new Plus(r,null);
			if (xc('?')) r.n = true;
		} else if (xc('?')) {
			r = new Quest(r,null);
			if (xc('?')) r.n = true;
		}
		return r;
	}
	
	RE single() {
		if (xc('(')) {
			return alt();
		} else if (xc('.')) {
			return new Dot();
		} else if (x<z) {
			return new Lit(chars[x++]);
		} else {
			return null;
		}
	}
	
	
	///// representation

	static class NFAEdge {
		NFAState to;
	}
	static class CharEdge extends NFAEdge {
		char c;
		@Override
		public String toString() {
			return c+"->"+to;
		}
	}
	static class EpsilonEdge extends NFAEdge {
		@Override
		public String toString() {
			return "ε->"+to;
		}
	}
	static class NFAState {
		ArrayList<NFAEdge> edges = new ArrayList<NFAEdge>(5);
		static int nextNumber = 0;
		int number = nextNumber++;
	}
	
	public static void main(String[] args) {
		RegExp2 test1 = new RegExp2("abc(de*|fg+)?");
	}
}

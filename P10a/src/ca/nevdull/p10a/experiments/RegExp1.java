// https://swtch.com/~rsc/regexp/regexp2.html

package ca.nevdull.p10a.experiments;

public class RegExp1 {

	public RegExp1(String s) {
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
	
	static abstract class RE {
		boolean n;
		abstract void print(int indent);
	}
	
	static class Link extends RE {
		RE left, right;
		Link(RE l, RE r) {
			left = l; right = r;
		}
		void print(int indent) {
			for (int i=0; i<indent; i++) System.out.print("    ");
			System.out.println(getClass().getSimpleName());
			left.print(indent+1);
			if (right!=null) right.print(indent);
		}
	}
	
	static class Alt extends Link {
		Alt(RE l, RE r) {
			super(l, r);
		}
	}
	
	static class Cat extends Link {
		Cat(RE l, RE r) {
			super(l, r);
		}
	}
	
	static class Star extends Link {
		Star(RE l, RE r) {
			super(l, r);
		}
	}
	
	static class Plus extends Link {
		Plus(RE l, RE r) {
			super(l, r);
		}
	}
	
	static class Quest extends Link {
		Quest(RE l, RE r) {
			super(l, r);
		}
	}
	
	static class Paren extends Link {
		Paren(RE l, RE r) {
			super(l, r);
		}
	}
	
	static class Lit extends RE {
		Lit(char c) {
			this.c = c;
		}
		char c;
		void print(int indent) {
			for (int i=0; i<indent; i++) System.out.print("    ");
			System.out.print(getClass().getSimpleName());
			System.out.print(' ');
			System.out.println(c);
		}
	}
	
	static class Dot extends RE {
		Dot() {
		}
		void print(int indent) {
			for (int i=0; i<indent; i++) System.out.print("    ");
			System.out.println(getClass().getSimpleName());
		}
	}

	public static void main(String[] args) {
		RegExp1 test1 = new RegExp1("abc(de*|fg+)?");
	}
}

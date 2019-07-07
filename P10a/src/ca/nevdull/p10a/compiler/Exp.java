// Not currently used

package ca.nevdull.p10a.compiler;

import ca.nevdull.util.Str;

public class Exp extends Str {
	Type type;
	protected boolean constant;

	public boolean isConstant() {
		return constant;
	}

	public Exp(Type type) {
		super();
		this.type = type;
		this.constant = false;
	}

	public Exp(Type type, String string) {
		super(string);
		this.type = type;
		this.constant = false;
	}

	public Exp(Type type, String... strings) {
		super(strings);
		this.type = type;
		this.constant = false;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Exp newConstant(Type type, String string) {
		Exp e = new Exp(type, string);
		e.constant = true;
		return e;
	}

}
